package com.xadrezonline.movimento

import com.xadrezonline.partida.Partida
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "movimentos")
class Movimento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    val partida: Partida,

    @Column(nullable = false)
    val notacaoAlgebrica: String,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)
