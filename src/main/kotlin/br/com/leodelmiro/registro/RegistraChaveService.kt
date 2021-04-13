package br.com.leodelmiro.registro

import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.apis.BcbClient
import br.com.leodelmiro.compartilhado.apis.CreatePixKeyRequest.Companion.toRequest
import br.com.leodelmiro.compartilhado.apis.ErpClient
import br.com.leodelmiro.compartilhado.chavepix.ChavePix
import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import br.com.leodelmiro.registro.exceptions.ClienteNaoEncontradoException
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import io.micronaut.http.HttpStatus.UNPROCESSABLE_ENTITY
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Validated
class RegistraChaveService(@Inject private val bcbClient: BcbClient,
                           @Inject private val erpClient: ErpClient,
                           @Inject private val repository: ChavePixRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(request: RegistroChaveRequest?): ChavePix {
        if (repository.existsByChave(request!!.chave)) throw PixJaExistenteException("Pix já existente no sistema")

        val contaResponse = erpClient.consulta(request.idCliente, TipoConta.by(request.tipoConta))
        val conta = contaResponse.body()?.toModel() ?: throw ClienteNaoEncontradoException("Cliente não encontrado")

        val chavePix = NovaChavePix(request, conta).toModel()
        repository.save(chavePix)

        logger.info("Registrando chave no banco central")
        try {
            val bcbResponse = bcbClient.cadastraChave(chavePix.toRequest())?.body()

            chavePix.atualizaChave(bcbResponse!!.key!!)

            logger.info("Chave: ${chavePix.chave} salva no banco e registrada no Banco central")

            return chavePix
        } catch (e: HttpClientResponseException) {
            when {
                (e.status == UNPROCESSABLE_ENTITY) ->
                    throw PixJaExistenteException("Chave pix já existente no Banco Central")

                else ->
                    throw IllegalStateException("Erro ao realizar registro da chave pix no Banco Central")
            }
        }
    }
}