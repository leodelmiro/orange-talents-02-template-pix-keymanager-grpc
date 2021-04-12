package br.com.leodelmiro.remocao

import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.remocao.exceptions.ChaveInexistenteException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class RemoveChaveService(private val repository: ChavePixRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(idChavePix: UUID, idCliente: UUID) {
        val chavePix = repository.findByIdAndIdCliente(idChavePix, idCliente)
                ?: throw ChaveInexistenteException("Chave pix não encontrada ou não pertencente ao cliente informado!")

        repository.deleteById(chavePix.id!!)
        logger.info("Chave deletada com sucesso!")
    }
}