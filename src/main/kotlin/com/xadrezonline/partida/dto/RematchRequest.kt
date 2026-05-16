package com.xadrezonline.partida.dto

import java.util.UUID

/** Corpo da requisição para criar uma revanche. */
data class RematchRequest(
    val partidaOrigemId: UUID
)
