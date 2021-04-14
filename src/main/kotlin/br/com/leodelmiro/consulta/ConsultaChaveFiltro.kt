package br.com.leodelmiro.consulta

import br.com.leodelmiro.compartilhado.apis.BcbClient
import br.com.leodelmiro.compartilhado.apis.PixKeyDetailsResponse.Companion.paraDetalhesChavePix
import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.compartilhado.validacao.ValidUUID
import br.com.leodelmiro.remocao.exceptions.ChaveInexistenteException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class ConsultaChaveFiltro {

    abstract fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): DetalhesChavePix

    @Introspected
    data class PorPixEClientId(@field: NotBlank @field:ValidUUID val idCliente: String,
                               @field: NotBlank @field:ValidUUID val idPix: String) : ConsultaChaveFiltro() {

        override fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): DetalhesChavePix {
            val idPixUUID = UUID.fromString(idPix)
            val idClientUUID = UUID.fromString(idCliente)

            val chavePix = repository.findByIdAndIdCliente(idPixUUID, idClientUUID)
                    ?: throw ChaveInexistenteException("Chave pix não encontrada")

            return DetalhesChavePix(chavePix)
        }
    }

    @Introspected
    data class PorChave(@field: NotBlank @field:Size(max = 77) val chave: String) : ConsultaChaveFiltro() {

        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): DetalhesChavePix {
            return repository.findByChave(chave).map {
                DetalhesChavePix(it)
            }.orElseGet {
                logger.info("Realizando consulta chave $chave no Banco Central")

                val response = bcbClient.consultaChave(chave)
                when (response.status) {
                    HttpStatus.OK -> response.body().paraDetalhesChavePix()
                    else -> throw ChaveInexistenteException("Chave pix não encontrada")
                }
            }
        }
    }

    @Introspected
    class Invalido : ConsultaChaveFiltro() {
        override fun consulta(repository: ChavePixRepository, bcbClient: BcbClient): DetalhesChavePix {
            throw IllegalArgumentException("Chave pix inválida ou não informada")
        }
    }
}