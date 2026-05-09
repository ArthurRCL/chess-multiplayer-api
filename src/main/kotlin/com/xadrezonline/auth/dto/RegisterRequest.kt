package com.xadrezonline.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "Email obrigatório")
    val email: String,

    @field:NotBlank(message = "Senha obrigatória")
    @field:Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    val senha: String
)
