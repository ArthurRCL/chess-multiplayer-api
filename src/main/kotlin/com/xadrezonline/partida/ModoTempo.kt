package com.xadrezonline.partida

/**
 * Define os modos de controle de tempo disponíveis para uma partida.
 * [limiteMs] é o tempo total por jogador em milissegundos.
 * SEM_LIMITE desativa o relógio completamente.
 */
enum class ModoTempo(val limiteMs: Long) {
    BULLET(60_000L),
    BLITZ_3(180_000L),
    BLITZ_5(300_000L),
    RAPIDO(600_000L),
    SEM_LIMITE(-1L)
}
