package com.xadrezonline.auth.dto

data class AuthResponse(
    val token: String,
    val email: String,
    val id: Long
)
