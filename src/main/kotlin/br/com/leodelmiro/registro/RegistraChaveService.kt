package br.com.leodelmiro.registro

import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.apis.ErpClient
import br.com.leodelmiro.compartilhado.chavepix.ChavePix
import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.compartilhado.chavepix.requestParaTipoChave
import br.com.leodelmiro.compartilhado.chavepix.requestParaTipoConta
import br.com.leodelmiro.registro.exceptions.ClienteNaoEncontradoException
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class RegistraChaveService(private val erpClient: ErpClient, private val repository: ChavePixRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun registra(request: RegistroChaveRequest?): ChavePix {
        if (repository.existsByChave(request!!.chave)) throw PixJaExistenteException("Pix já existente no sistema")

        val tipoConta = requestParaTipoConta(request.tipoConta)
        val contaResponse = erpClient.consulta(request.idCliente, tipoConta)
        val conta = contaResponse.body()?.toModel() ?: throw ClienteNaoEncontradoException("Cliente não encontrado")

        val chavePix = NovaChavePix(request.idCliente, tipoConta, request.chave, requestParaTipoChave(request.tipoChave), conta).toModel()
        repository.save(chavePix)
        logger.info("Chave salva no banco")

        return chavePix
    }
}