package com.xadrezonline.partida.dto

import com.xadrezonline.partida.ModoTempo
import java.util.UUID

data class CriarPartidaResponse(
    val id: UUID,
    val linkConvite: String,
    val jogadorBrancasEmail: String,
    val modoTempo: ModoTempo = ModoTempo.SEM_LIMITE
)
