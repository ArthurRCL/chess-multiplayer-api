package com.xadrezonline.usuario

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    private val senhaHash: String,

    @Column(nullable = false)
    val dataCriacao: LocalDateTime = LocalDateTime.now()
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    override fun getPassword(): String = senhaHash
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
