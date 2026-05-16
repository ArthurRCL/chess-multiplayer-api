package com.xadrezonline.partida

import com.xadrezonline.usuario.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface PartidaRepository : JpaRepository<Partida, UUID> {

    @Query("""
        SELECT p FROM Partida p
        WHERE p.jogadorBrancas = :usuario OR p.jogadorNegras = :usuario
        ORDER BY p.dataCriacao DESC
    """)
    fun findByJogador(usuario: Usuario): List<Partida>

    /** Retorna partidas EM_ANDAMENTO com controle de tempo ativo — usado pelo scheduler. */
    @Query("""
        SELECT p FROM Partida p
        WHERE p.status = com.xadrezonline.partida.StatusPartida.EM_ANDAMENTO
          AND p.modoTempo <> com.xadrezonline.partida.ModoTempo.SEM_LIMITE
    """)
    fun findAtivasComRelogio(): List<Partida>
}
