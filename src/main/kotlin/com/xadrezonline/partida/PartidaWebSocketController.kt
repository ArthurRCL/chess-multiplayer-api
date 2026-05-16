package com.xadrezonline.partida

import com.xadrezonline.partida.dto.EstadoPartidaResponse
import com.xadrezonline.partida.dto.MovimentoRequest
import com.xadrezonline.usuario.UsuarioRepository
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
            val jogador = usuarioRepository.findByEmail(principal.name)
                .orElseThrow { NoSuchElementException("Usuário não encontrado") }

            val estado = partidaService.processarMovimento(
                UUID.fromString(id),
                jogador,
                request
            )

            // Broadcast do novo estado para todos na sala
            messagingTemplate.convertAndSend("/topic/partida/$id/estado", estado)

        } catch (e: Exception) {
            // Erro privado apenas para o jogador que tentou o movimento
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
            val jogador = usuarioRepository.findByEmail(principal.name)
                .orElseThrow()

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
}
