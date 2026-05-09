package com.xadrezonline.partida.dto

import java.util.UUID

data class CriarPartidaResponse(
    val id: UUID,
    val linkConvite: String,
    val jogadorBrancasEmail: String
)
