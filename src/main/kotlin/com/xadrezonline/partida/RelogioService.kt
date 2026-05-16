package com.xadrezonline.partida

import com.xadrezonline.usuario.Usuario
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Responsabilidade única: gerenciar o relógio de uma partida de xadrez.
 *
 * Separado do [PartidaService] para respeitar o SRP — toda lógica de tempo
 * vive aqui, sem misturar com regras de xadrez ou persistência.
 */
@Service
class RelogioService {

    /**
     * Inicializa os tempos de ambos os jogadores com base no [ModoTempo] da partida.
     * Deve ser chamado quando o segundo jogador entra e a partida inicia.
     */
    fun iniciarRelogio(partida: Partida) {
        if (partida.modoTempo == ModoTempo.SEM_LIMITE) return
        val limiteMs = partida.modoTempo.limiteMs
        partida.tempoBrancasMs = limiteMs
        partida.tempoNegrasMs = limiteMs
        partida.ultimoMovimentoAt = Instant.now()
    }

    /**
     * Desconta o tempo do jogador que acabou de mover.
     * Deve ser chamado *antes* de salvar o novo estado no banco.
     *
     * @param partida      A partida em andamento.
     * @param jogadorMoveu O jogador que acabou de fazer o movimento.
     */
    fun registrarMovimento(partida: Partida, jogadorMoveu: Usuario) {
        if (partida.modoTempo == ModoTempo.SEM_LIMITE) return
        val agora = Instant.now()
        val inicio = partida.ultimoMovimentoAt ?: agora
        val tempoGastoMs = agora.toEpochMilli() - inicio.toEpochMilli()

        val ehBrancas = partida.jogadorBrancas.id == jogadorMoveu.id
        if (ehBrancas) {
            partida.tempoBrancasMs = maxOf(0L, partida.tempoBrancasMs - tempoGastoMs)
        } else {
            partida.tempoNegrasMs = maxOf(0L, partida.tempoNegrasMs - tempoGastoMs)
        }
        partida.ultimoMovimentoAt = agora
    }

    /**
     * Verifica se algum jogador estourou o tempo durante o turno atual.
     *
     * Retorna o [Usuario] que **perdeu** por timeout, ou null se ninguém estourou.
     * Nota: só checa o jogador *cujo turno está ativo* (o relógio só corre para quem
     * deve jogar).
     */
    fun verificarTimeout(partida: Partida): Usuario? {
        if (partida.modoTempo == ModoTempo.SEM_LIMITE) return null
        if (partida.status != StatusPartida.EM_ANDAMENTO) return null

        val inicio = partida.ultimoMovimentoAt ?: return null
        val agora = Instant.now()
        val tempoDecorridoMs = agora.toEpochMilli() - inicio.toEpochMilli()

        // Detecta de quem é a vez via FEN (campo 2: 'w' = brancas)
        val vezDeBrancas = partida.fenAtual.split(" ").getOrNull(1) == "w"

        return if (vezDeBrancas) {
            val tempoRestante = partida.tempoBrancasMs - tempoDecorridoMs
            if (tempoRestante <= 0) partida.jogadorBrancas else null
        } else {
            val tempoRestante = partida.tempoNegrasMs - tempoDecorridoMs
            if (tempoRestante <= 0) partida.jogadorNegras else null
        }
    }

    /**
     * Calcula os tempos restantes *em tempo real* para a resposta ao cliente,
     * descontando o tempo já decorrido no turno atual (sem alterar o estado persistido).
     */
    fun calcularTemposAtuais(partida: Partida): Pair<Long, Long> {
        if (partida.modoTempo == ModoTempo.SEM_LIMITE) return Pair(-1L, -1L)

        val inicio = partida.ultimoMovimentoAt
        val agora = Instant.now()
        val decorrido = if (inicio != null) agora.toEpochMilli() - inicio.toEpochMilli() else 0L

        val vezDeBrancas = partida.fenAtual.split(" ").getOrNull(1) == "w"
        return if (vezDeBrancas) {
            Pair(maxOf(0L, partida.tempoBrancasMs - decorrido), partida.tempoNegrasMs)
        } else {
            Pair(partida.tempoBrancasMs, maxOf(0L, partida.tempoNegrasMs - decorrido))
        }
    }
}
