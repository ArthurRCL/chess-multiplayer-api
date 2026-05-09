package com.xadrezonline.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "Email obrigatório")
    val email: String,

    @field:NotBlank(message = "Senha obrigatória")
    val senha: String
)
