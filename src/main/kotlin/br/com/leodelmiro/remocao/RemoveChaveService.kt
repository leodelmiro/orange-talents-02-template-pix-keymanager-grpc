package br.com.leodelmiro.remocao

import br.com.leodelmiro.compartilhado.apis.BcbClient
import br.com.leodelmiro.compartilhado.apis.DeletePixKeyRequest.Companion.toRequest
import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.remocao.exceptions.ChaveInexistenteException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Validated
class RemoveChaveService(@Inject private val repository: ChavePixRepository,
                         @Inject private val bcbClient: BcbClient) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remove(idChavePix: UUID, idCliente: UUID) {
        val chavePix = repository.findByIdAndIdCliente(idChavePix, idCliente)
                ?: throw ChaveInexistenteException("Chave pix não encontrada ou não pertencente ao cliente informado!")

        repository.deleteById(chavePix.id!!)

        val bcbResponse = bcbClient.removeChave(chave = chavePix.chave, remocao = chavePix.toRequest())

        if(bcbResponse.status != HttpStatus.OK) throw IllegalStateException("Erro ao remover a chave no banco central")

        logger.info("Chave deletada com sucesso!")
    }
}