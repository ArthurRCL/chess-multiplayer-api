package com.xadrezonline.partida

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.xadrezonline.movimento.Movimento
import com.xadrezonline.movimento.MovimentoRepository
import com.xadrezonline.partida.dto.*
import com.xadrezonline.usuario.Usuario
import com.xadrezonline.usuario.UsuarioRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PartidaService(
    private val partidaRepository: PartidaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val movimentoRepository: MovimentoRepository,
    private val relogioService: RelogioService
) {
    @Value("\${app.base-url:http://localhost:3001/#/}")
    private lateinit var baseUrl: String

    // ── Criar nova partida ────────────────────────────────────────────────────

    @Transactional
    fun criarPartida(jogador: Usuario, modoTempo: ModoTempo = ModoTempo.SEM_LIMITE): CriarPartidaResponse {
        val partida = Partida(
            jogadorBrancas = jogador,
            modoTempo = modoTempo
        )
        partidaRepository.save(partida)
        return CriarPartidaResponse(
            id = partida.id,
            linkConvite = "${baseUrl}partida/${partida.id}",
            jogadorBrancasEmail = jogador.email,
            modoTempo = modoTempo
        )
    }

    // ── Jogador B ingressa na partida ─────────────────────────────────────────

    @Transactional
    fun entrarNaPartida(partidaId: UUID, jogador: Usuario): EstadoPartidaResponse {
        val partida = buscarPartida(partidaId)

        if (partida.status != StatusPartida.AGUARDANDO) {
            throw IllegalStateException("A partida não está mais aguardando jogadores")
        }
        if (partida.jogadorBrancas.id == jogador.id) {
            throw IllegalStateException("Você já é o criador desta partida")
        }

        partida.jogadorNegras = jogador
        partida.status = StatusPartida.EM_ANDAMENTO

        // Inicia o relógio quando ambos os jogadores estão presentes
        relogioService.iniciarRelogio(partida)

        partidaRepository.save(partida)

        return construirEstado(partida, null)
    }

    // ── Modo Solo: o mesmo usuário entra como os dois lados (apenas para testes) ──

    @Transactional
    fun entrarModoSolo(partidaId: UUID, jogador: Usuario): EstadoPartidaResponse {
        val partida = buscarPartida(partidaId)

        if (partida.status != StatusPartida.AGUARDANDO) {
            throw IllegalStateException("A partida não está mais aguardando jogadores")
        }
        // Permite que o criador entre como jogador de negras também (modo solo/teste)
        partida.jogadorNegras = jogador
        partida.status = StatusPartida.EM_ANDAMENTO

        relogioService.iniciarRelogio(partida)
        partidaRepository.save(partida)

        return construirEstado(partida, null)
    }

    // ── Processar movimento ───────────────────────────────────────────────────

    @Transactional
    fun processarMovimento(
        partidaId: UUID,
        jogador: Usuario,
        request: MovimentoRequest
    ): EstadoPartidaResponse {
        val partida = buscarPartida(partidaId)

        if (partida.status != StatusPartida.EM_ANDAMENTO) {
            throw IllegalStateException("A partida não está em andamento")
        }

        val board = Board()
        board.loadFromFen(partida.fenAtual)

        // Verificar se é a vez do jogador
        val isBrancas = partida.jogadorBrancas.id == jogador.id
        val isNegras = partida.jogadorNegras?.id == jogador.id
        val ehModoSolo = isBrancas && isNegras // mesmo usuário dos dois lados

        if (!isBrancas && !isNegras) {
            throw IllegalArgumentException("Você não é um jogador desta partida")
        }

        val vezAtualEhBrancas = board.sideToMove == Side.WHITE
        // No modo solo o mesmo usuário controla os dois lados, então a verificação de vez é ignorada
        if (!ehModoSolo && ((isBrancas && !vezAtualEhBrancas) || (isNegras && vezAtualEhBrancas))) {
            throw IllegalArgumentException("Não é sua vez")
        }

        // Descontar o tempo do jogador que está movendo
        relogioService.registrarMovimento(partida, jogador)

        // Construir o movimento
        val from = Square.valueOf(request.from.uppercase())
        val to = Square.valueOf(request.to.uppercase())

        val promocaoPiece = resolverPromocao(request.promocao, board.sideToMove)
        val move = if (promocaoPiece != Piece.NONE) Move(from, to, promocaoPiece) else Move(from, to)

        // Validar se o movimento é legal
        val movimentosLegais = MoveGenerator.generateLegalMoves(board)
        if (!movimentosLegais.contains(move)) {
            throw IllegalArgumentException("Movimento ilegal: ${request.from}-${request.to}")
        }

        // Executar o movimento
        board.doMove(move)
        val novoFen = board.fen

        // Salvar histórico
        val notacao = "${request.from}${request.to}${request.promocao ?: ""}"
        movimentoRepository.save(Movimento(partida = partida, notacaoAlgebrica = notacao))

        // Verificar estado do jogo
        val movimentosAposMovimento = MoveGenerator.generateLegalMoves(board)
        val xeque = board.isKingAttacked
        val xequeMate = movimentosAposMovimento.isEmpty() && xeque
        val afogamento = movimentosAposMovimento.isEmpty() && !xeque

        partida.fenAtual = novoFen

        val motivoFim: String? = when {
            xequeMate -> {
                partida.status = StatusPartida.FINALIZADA
                partida.vencedor = jogador
                "XEQUE_MATE"
            }
            afogamento -> {
                partida.status = StatusPartida.FINALIZADA
                "AFOGAMENTO"
            }
            else -> null
        }

        partidaRepository.save(partida)

        return construirEstado(partida, notacao, xeque, xequeMate, afogamento, motivoFim)
    }

    // ── Desistência ───────────────────────────────────────────────────────────

    @Transactional
    fun desistir(partidaId: UUID, jogador: Usuario): EstadoPartidaResponse {
        val partida = buscarPartida(partidaId)

        if (partida.status != StatusPartida.EM_ANDAMENTO) {
            throw IllegalStateException("A partida não está em andamento")
        }

        partida.status = StatusPartida.FINALIZADA
        partida.vencedor = if (partida.jogadorBrancas.id == jogador.id) {
            partida.jogadorNegras
        } else {
            partida.jogadorBrancas
        }
        partidaRepository.save(partida)

        return construirEstado(partida, null, motivoFim = "DESISTENCIA")
    }

    // ── Revanche ──────────────────────────────────────────────────────────────

    /**
     * Cria uma nova partida com as cores invertidas entre os mesmos jogadores.
     * O ex-jogador de negras torna-se o criador (brancas) da nova partida.
     *
     * @return Par de (novaPartidaId, EstadoPartidaResponse) para broadcast imediato.
     */
    @Transactional
    fun solicitarRevanche(partidaOrigemId: UUID, solicitante: Usuario): Pair<UUID, EstadoPartidaResponse> {
        val origem = buscarPartida(partidaOrigemId)

        if (origem.status != StatusPartida.FINALIZADA) {
            throw IllegalStateException("A revanche só pode ser solicitada após o término da partida")
        }

        val adversario = when (solicitante.id) {
            origem.jogadorBrancas.id -> origem.jogadorNegras
                ?: throw IllegalStateException("Partida não tinha adversário")
            origem.jogadorNegras?.id -> origem.jogadorBrancas
            else -> throw IllegalArgumentException("Você não é um jogador desta partida")
        }

        // Inverte as cores: quem era negras vira brancas
        val novasBrancas = adversario
        val novasNegras = solicitante

        val novaPartida = Partida(
            jogadorBrancas = novasBrancas,
            jogadorNegras = novasNegras,
            status = StatusPartida.EM_ANDAMENTO,
            modoTempo = origem.modoTempo
        )
        relogioService.iniciarRelogio(novaPartida)
        partidaRepository.save(novaPartida)

        return Pair(novaPartida.id, construirEstado(novaPartida, null))
    }

    // ── Buscar partida por ID ─────────────────────────────────────────────────

    fun buscarPartidaInfo(partidaId: UUID): PartidaInfoResponse {
        val p = buscarPartida(partidaId)
        return PartidaInfoResponse(
            id = p.id,
            jogadorBrancasEmail = p.jogadorBrancas.email,
            jogadorNegrasEmail = p.jogadorNegras?.email,
            status = p.status,
            fen = p.fenAtual,
            vencedorEmail = p.vencedor?.email,
            modoTempo = p.modoTempo
        )
    }

    // ── Histórico do usuário ──────────────────────────────────────────────────

    fun historico(usuario: Usuario): List<PartidaInfoResponse> =
        partidaRepository.findByJogador(usuario).map { p ->
            PartidaInfoResponse(
                id = p.id,
                jogadorBrancasEmail = p.jogadorBrancas.email,
                jogadorNegrasEmail = p.jogadorNegras?.email,
                status = p.status,
                fen = p.fenAtual,
                vencedorEmail = p.vencedor?.email,
                modoTempo = p.modoTempo
            )
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buscarPartida(id: UUID): Partida =
        partidaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Partida não encontrada: $id") }

    private fun resolverPromocao(letra: String?, lado: Side): Piece {
        if (letra == null) return Piece.NONE
        return when (letra.uppercase()) {
            "Q" -> if (lado == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
            "R" -> if (lado == Side.WHITE) Piece.WHITE_ROOK else Piece.BLACK_ROOK
            "B" -> if (lado == Side.WHITE) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
            "N" -> if (lado == Side.WHITE) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
            else -> Piece.NONE
        }
    }

    private fun construirEstado(
        partida: Partida,
        ultimoMovimento: String?,
        xeque: Boolean = false,
        xequeMate: Boolean = false,
        afogamento: Boolean = false,
        motivoFim: String? = null
    ): EstadoPartidaResponse {
        val board = Board()
        board.loadFromFen(partida.fenAtual)
        val (brancasMs, negrasMs) = relogioService.calcularTemposAtuais(partida)
        return EstadoPartidaResponse(
            partidaId = partida.id,
            fen = partida.fenAtual,
            status = partida.status,
            vezDe = if (board.sideToMove == Side.WHITE) "BRANCAS" else "NEGRAS",
            vencedorEmail = partida.vencedor?.email,
            xeque = xeque,
            xequeMate = xequeMate,
            afogamento = afogamento,
            ultimoMovimento = ultimoMovimento,
            tempoBrancasMs = brancasMs,
            tempoNegrasMs = negrasMs,
            motivoFim = motivoFim
        )
    }
}
