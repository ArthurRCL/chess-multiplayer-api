package com.xadrezonline.partida

import com.xadrezonline.partida.dto.MovimentoRequest
import com.xadrezonline.usuario.UsuarioRepository
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class PartidaWebSocketController(
    private val partidaService: PartidaService,
    private val usuarioRepository: UsuarioRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    /**
     * Cliente envia para: /app/partida/{id}/mover
     * Servidor faz broadcast para: /topic/partida/{id}/estado
     * Em caso de erro, envia para: /queue/errors (privado do remetente)
     */
    @MessageMapping("/partida/{id}/mover")
    fun processarMovimento(
        @DestinationVariable id: String,
        @Valid request: MovimentoRequest,
        principal: java.security.Principal
    ) {
        try {
            val jogador = resolverUsuario(principal.name)
            val estado = partidaService.processarMovimento(UUID.fromString(id), jogador, request)
            messagingTemplate.convertAndSend("/topic/partida/$id/estado", estado)
        } catch (e: Exception) {
            messagingTemplate.convertAndSendToUser(
                principal.name,
                "/queue/errors",
                mapOf("erro" to (e.message ?: "Erro ao processar movimento"))
            )
        }
    }

    /**
     * Cliente envia para: /app/partida/{id}/desistir
     * Servidor faz broadcast para: /topic/partida/{id}/estado
     */
    @MessageMapping("/partida/{id}/desistir")
    fun processarDesistencia(
        @DestinationVariable id: String,
        principal: java.security.Principal
    ) {
        try {
            val jogador = resolverUsuario(principal.name)
            val estado = partidaService.desistir(UUID.fromString(id), jogador)
            messagingTemplate.convertAndSend("/topic/partida/$id/estado", estado)
        } catch (e: Exception) {
            messagingTemplate.convertAndSendToUser(
                principal.name,
                "/queue/errors",
                mapOf("erro" to (e.message ?: "Erro ao processar desistência"))
            )
        }
    }

    /**
     * Cliente envia para: /app/partida/{id}/revanche
     * Servidor faz broadcast para: /topic/partida/{id}/revanche com o ID da nova partida.
     * Ambos os jogadores recebem e devem navegar para a nova partida.
     */
    @MessageMapping("/partida/{id}/revanche")
    fun processarRevanche(
        @DestinationVariable id: String,
        principal: java.security.Principal
    ) {
        try {
            val jogador = resolverUsuario(principal.name)
            val (novaPartidaId, estado) = partidaService.solicitarRevanche(UUID.fromString(id), jogador)

            // Notifica ambos os jogadores na sala antiga para navegarem para a nova partida
            messagingTemplate.convertAndSend(
                "/topic/partida/$id/revanche",
                mapOf("novaPartidaId" to novaPartidaId)
            )
            // Já envia o estado inicial da nova partida no canal dela
            messagingTemplate.convertAndSend("/topic/partida/$novaPartidaId/estado", estado)
        } catch (e: Exception) {
            messagingTemplate.convertAndSendToUser(
                principal.name,
                "/queue/errors",
                mapOf("erro" to (e.message ?: "Erro ao processar revanche"))
            )
        }
    }

    private fun resolverUsuario(email: String) =
        usuarioRepository.findByEmail(email)
            .orElseThrow { NoSuchElementException("Usuário não encontrado") }
}
