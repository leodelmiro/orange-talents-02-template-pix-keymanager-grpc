package br.com.leodelmiro.compartilhado.chavepix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave: String): Boolean

    fun existsByIdCliente(idCliente: UUID): Boolean

    fun findByChave(chave: String): Optional<ChavePix>

    fun findByIdAndIdCliente(id: UUID, idCliente: UUID): ChavePix?

    fun findAllByIdCliente(idCliente: UUID): List<ChavePix>


}