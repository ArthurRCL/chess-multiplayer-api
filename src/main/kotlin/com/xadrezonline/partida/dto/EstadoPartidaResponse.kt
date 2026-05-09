package com.xadrezonline.partida.dto

import com.xadrezonline.partida.StatusPartida
import java.util.UUID

data class EstadoPartidaResponse(
    val partidaId: UUID,
    val fen: String,
    val status: StatusPartida,
    val vezDe: String,       // "BRANCAS" ou "NEGRAS"
    val vencedorEmail: String?,
    val xeque: Boolean,
    val xequeMate: Boolean,
    val afogamento: Boolean,
    val ultimoMovimento: String?
)

data class PartidaInfoResponse(
    val id: UUID,
    val jogadorBrancasEmail: String,
    val jogadorNegrasEmail: String?,
    val status: StatusPartida,
    val fen: String,
    val vencedorEmail: String?
)
