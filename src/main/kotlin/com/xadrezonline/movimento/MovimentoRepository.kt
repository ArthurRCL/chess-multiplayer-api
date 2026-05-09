package com.xadrezonline.movimento

import com.xadrezonline.partida.Partida
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MovimentoRepository : JpaRepository<Movimento, Long> {
    fun findByPartidaIdOrderByTimestampAsc(partidaId: UUID): List<Movimento>
}
