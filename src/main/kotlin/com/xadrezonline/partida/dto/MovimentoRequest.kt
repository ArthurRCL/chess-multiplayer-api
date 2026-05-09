package com.xadrezonline.partida.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class MovimentoRequest(
    @field:NotBlank(message = "Casa de origem obrigatória")
    @field:Pattern(regexp = "[a-h][1-8]", message = "Casa de origem inválida (ex: e2)")
    val from: String,

    @field:NotBlank(message = "Casa de destino obrigatória")
    @field:Pattern(regexp = "[a-h][1-8]", message = "Casa de destino inválida (ex: e4)")
    val to: String,

    // Letra da peça para promoção de peão: "q"=rainha, "r"=torre, "b"=bispo, "n"=cavalo
    val promocao: String? = null
)
