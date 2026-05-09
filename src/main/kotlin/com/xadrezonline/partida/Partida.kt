package com.xadrezonline.partida

import com.xadrezonline.usuario.Usuario
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

enum class StatusPartida {
    AGUARDANDO, EM_ANDAMENTO, FINALIZADA
}

@Entity
@Table(name = "partidas")
class Partida(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_brancas_id", nullable = false)
    val jogadorBrancas: Usuario,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_negras_id")
    var jogadorNegras: Usuario? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusPartida = StatusPartida.AGUARDANDO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vencedor_id")
    var vencedor: Usuario? = null,

    // FEN notation — armazena o estado completo do tabuleiro
    @Column(nullable = false, columnDefinition = "TEXT")
    var fenAtual: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",

    @Column(nullable = false)
    val dataCriacao: LocalDateTime = LocalDateTime.now()
)
