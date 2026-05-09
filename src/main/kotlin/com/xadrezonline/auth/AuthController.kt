package com.xadrezonline.auth

import com.xadrezonline.auth.dto.LoginRequest
import com.xadrezonline.auth.dto.RegisterRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de registro e login de usuários")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário", description = "Cria uma conta com email e senha")
    fun register(@Valid @RequestBody request: RegisterRequest) =
        ResponseEntity.ok(authService.register(request))

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica o usuário e retorna um JWT")
    fun login(@Valid @RequestBody request: LoginRequest) =
        ResponseEntity.ok(authService.login(request))
}
