package com.xadrezonline.auth

import com.xadrezonline.auth.dto.AuthResponse
import com.xadrezonline.auth.dto.LoginRequest
import com.xadrezonline.auth.dto.RegisterRequest
import com.xadrezonline.usuario.Usuario
import com.xadrezonline.usuario.UsuarioRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (usuarioRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email já cadastrado")
        }
        val usuario = Usuario(
            email = request.email,
            senhaHash = passwordEncoder.encode(request.senha)
        )
        val saved = usuarioRepository.save(usuario)
        return AuthResponse(
            token = jwtService.generateToken(saved),
            email = saved.email,
            id = saved.id
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.senha)
        )
        val usuario = usuarioRepository.findByEmail(request.email).orElseThrow()
        return AuthResponse(
            token = jwtService.generateToken(usuario),
            email = usuario.email,
            id = usuario.id
        )
    }
}
