package br.com.leodelmiro.remocao

import br.com.leodelmiro.KeyManagerRemoveGrpcServiceGrpc
import br.com.leodelmiro.RemocaoChaveRequest
import br.com.leodelmiro.compartilhado.apis.*
import br.com.leodelmiro.compartilhado.apis.DeletePixKeyRequest.Companion.toRequest
import br.com.leodelmiro.compartilhado.chavepix.*
import br.com.leodelmiro.registro.RegistroGrpcEndpointTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemocaoGrpcEndpointTest(private val repository: ChavePixRepository,
                                       private val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub) {

    @Inject
    lateinit var bcbClient: BcbClient

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
        val chavePix = chavePixModel()

        Mockito.`when`(bcbClient.removeChave(chavePix.chave, chavePix.toRequest()))
                .thenReturn(HttpResponse.ok(DeletePixKeyResponse(chavePix.chave, chavePix.conta.instituicaoIspb)))

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
    fun `nao deve remover chave quando acontecer algum erro no bcb`() {
        val chavePix = chavePixModel()

        Mockito.`when`(bcbClient.removeChave(chavePix.chave, chavePix.toRequest()))
                .thenReturn(HttpResponse.unauthorized())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChave(remocaoChaveRequest(idPix = idPix.toString(), idCliente = idCliente.toString()))
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover a chave no banco central", status.description)
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

    private fun pixDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(KeyType.CPF,
                "96498610093",
                BankAccount(
                        "60701190",
                        "0001",
                        "291900",
                        BankAccount.AccountType.CACC
                ),
                Owner(
                        Owner.Type.NATURAL_PERSON,
                        "Leonardo Delmiro",
                        "96498610093"
                ),
                LocalDateTime.MIN
        )
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}