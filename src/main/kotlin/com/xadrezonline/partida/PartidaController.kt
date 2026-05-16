package com.xadrezonline.partida

import com.xadrezonline.partida.dto.MovimentoRequest
import com.xadrezonline.usuario.UsuarioRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.util.UUID

@RestController
@RequestMapping("/api/partidas")
@Tag(name = "Partidas", description = "Gerenciamento de partidas de xadrez")
@SecurityRequirement(name = "bearerAuth")
class PartidaController(
    private val partidaService: PartidaService,
    private val usuarioRepository: UsuarioRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    @PostMapping
    @Operation(summary = "Criar nova partida e obter link de convite")
    fun criarPartida(@AuthenticationPrincipal userDetails: UserDetails) =
        ResponseEntity.ok(
            partidaService.criarPartida(resolverUsuario(userDetails))
        )

    @GetMapping("/{id}")
    @Operation(summary = "Buscar informações de uma partida")
    fun buscarPartida(@PathVariable id: UUID) =
        ResponseEntity.ok(partidaService.buscarPartidaInfo(id))

    @PostMapping("/{id}/entrar")
    @Operation(summary = "Ingressar em uma partida via link de convite")
    fun entrarNaPartida(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<com.xadrezonline.partida.dto.EstadoPartidaResponse> {
        val estado = partidaService.entrarNaPartida(id, resolverUsuario(userDetails))
        messagingTemplate.convertAndSend("/topic/partida/$id/estado", estado)
        return ResponseEntity.ok(estado)
    }

    @PostMapping("/{id}/desistir")
    @Operation(summary = "Desistir da partida atual")
    fun desistir(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails
    ) = ResponseEntity.ok(
        partidaService.desistir(id, resolverUsuario(userDetails))
    )

    @GetMapping("/historico")
    @Operation(summary = "Listar histórico de partidas do usuário autenticado")
    fun historico(@AuthenticationPrincipal userDetails: UserDetails) =
        ResponseEntity.ok(
            partidaService.historico(resolverUsuario(userDetails))
        )

    private fun resolverUsuario(userDetails: UserDetails) =
        usuarioRepository.findByEmail(userDetails.username)
            .orElseThrow { NoSuchElementException("Usuário não encontrado") }
}
