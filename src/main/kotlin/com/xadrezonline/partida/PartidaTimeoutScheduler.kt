package com.xadrezonline.partida

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Responsabilidade única: varrer partidas em andamento a cada 500ms e declarar
 * o vencedor automaticamente quando o tempo de algum jogador se esgota.
 *
 * Separado do [PartidaService] para respeitar SRP — ele não contém lógica de
 * xadrez, apenas orquestra a detecção de timeout e dispara o broadcast.
 */
@Component
class PartidaTimeoutScheduler(
    private val partidaRepository: PartidaRepository,
    private val relogioService: RelogioService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * Executado a cada 500ms. Verifica apenas partidas:
     * - Com status EM_ANDAMENTO
     * - Com modoTempo diferente de SEM_LIMITE
     *
     * Ao detectar timeout, encerra a partida e faz broadcast do estado final.
     */
    @Scheduled(fixedDelay = 500)
    @Transactional
    fun verificarTimeouts() {
        val partidasAtivas = partidaRepository.findAtivasComRelogio()

        for (partida in partidasAtivas) {
            val perdedor = relogioService.verificarTimeout(partida) ?: continue

            partida.status = StatusPartida.FINALIZADA
            partida.vencedor = if (partida.jogadorBrancas.id == perdedor.id) {
                partida.jogadorNegras
            } else {
                partida.jogadorBrancas
            }

            partidaRepository.save(partida)

            val (brancasMs, negrasMs) = relogioService.calcularTemposAtuais(partida)

            messagingTemplate.convertAndSend(
                "/topic/partida/${partida.id}/estado",
                com.xadrezonline.partida.dto.EstadoPartidaResponse(
                    partidaId = partida.id,
                    fen = partida.fenAtual,
                    status = partida.status,
                    vezDe = "BRANCAS",
                    vencedorEmail = partida.vencedor?.email,
                    xeque = false,
                    xequeMate = false,
                    afogamento = false,
                    ultimoMovimento = null,
                    tempoBrancasMs = brancasMs,
                    tempoNegrasMs = negrasMs,
                    motivoFim = "TIMEOUT"
                )
            )
        }
    }
}
