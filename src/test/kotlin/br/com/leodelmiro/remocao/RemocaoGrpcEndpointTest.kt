package br.com.leodelmiro.remocao

import br.com.leodelmiro.KeyManagerRegistraGrpcServiceGrpc
import br.com.leodelmiro.KeyManagerRemoveGrpcServiceGrpc
import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.RemocaoChaveRequest
import br.com.leodelmiro.compartilhado.chavepix.*
import br.com.leodelmiro.registro.RegistroGrpcEndpointTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.*

@MicronautTest(transactional = false)
internal class RemocaoGrpcEndpointTest(private val repository: ChavePixRepository,
                                       private val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub) {
    companion object {
        var idPix: UUID? = null
        var idCliente: UUID? = null
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()

        val chavePixModel = chavePixModel()
        repository.save(chavePixModel)
        idPix = chavePixModel.id
        idCliente = chavePixModel.idCliente
    }

    @Test
    fun `deve remover chave quando estiver tudo ok`() {
        grpcClient.removerChave(remocaoChaveRequest(idPix = idPix.toString(), idCliente = idCliente.toString()))

        assertNull(repository.findByIdAndIdCliente(idPix!!, idCliente!!))
    }

    @Test
    fun `nao deve remover chave quando algum dado passado for invalido`() {
        val idClienteRequest = "c56dfef4-7901-44fb-84e2"

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(remocaoChaveRequest(idPix = idPix.toString(), idCliente = idClienteRequest)
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
            assertNotNull(repository.findById(idPix!!))
        }
    }

    @Test
    fun `nao deve remover chave quando id da chave nao for encontrado`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(remocaoChaveRequest(idPix = "c56dfef4-7901-44fb-84e2-a2cefb157890"))
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertencente ao cliente informado!", status.description)
            assertNotNull(repository.findById(idPix!!))
        }
    }

    @Test
    fun `nao deve remover chave quando id do cliente nao pertencer ao cliente`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(remocaoChaveRequest(idPix = idPix.toString(), idCliente = "c56dfef4-7902-44fb-84e2-a2cefb157890"))
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertencente ao cliente informado!", status.description)
            assertNotNull(repository.findById(idPix!!))
        }
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePixModel(): ChavePix {
        return ChavePix(RegistroGrpcEndpointTest.CLIENTE_ID,
                TipoConta.CONTA_CORRENTE,
                "96498610093",
                TipoChave.CPF,
                ContaUsuario("ITAÚ UNIBANCO S.A.",
                        "60701190",
                        "Leonardo Delmiro",
                        "96498610093",
                        "0001",
                        "291900"))
    }

    private fun remocaoChaveRequest(idPix: String,
                                    idCliente: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"): RemocaoChaveRequest {
        return RemocaoChaveRequest.newBuilder()
                .setIdPix(idPix)
                .setIdCliente(idCliente)
                .build()
    }

}