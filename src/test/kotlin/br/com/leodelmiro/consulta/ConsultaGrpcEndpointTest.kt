package br.com.leodelmiro.consulta

import br.com.leodelmiro.ConsultaChaveRequest
import br.com.leodelmiro.KeyManagerConsultaGrpcServiceGrpc
import br.com.leodelmiro.compartilhado.apis.*
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaGrpcEndpointTest(private val repository: ChavePixRepository,
                                        private val grpcClient: KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub) {

    @Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve consultar chave localmente quando for passado idCliente e idPix`() {
        val chavePix = chavePixModel()
        repository.save(chavePix)

        val result = grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                .setPixEClienteId(ConsultaChaveRequest.ConsultaPorPixEClienteId.newBuilder()
                        .setIdCliente(chavePix.idCliente.toString())
                        .setIdPix(chavePix.id.toString())
                )
                .build())

        with(result) {
            assertEquals(chavePix.id.toString(), result.idPix)
            assertEquals(chavePix.tipoChave.name, chave.tipoChave.name)
            assertEquals(chavePix.chave, chave.chavePix)
            assertEquals(chavePix.idCliente.toString(), idClient)
            assertEquals(chavePix.tipoConta.name, chave.conta.tipoConta.name)
            assertEquals(chavePix.conta.nomeTitular, chave.conta.nomeTitular)
            assertEquals(chavePix.conta.cpfTitular, chave.conta.cpfTitular)
            assertEquals(chavePix.conta.agencia, chave.conta.agencia)
            assertEquals(chavePix.conta.numero, chave.conta.numero)
            assertEquals(chavePix.conta.instituicaoNome, chave.conta.instituicao)
            assertNotNull(result.chave.criadoEm)
        }
    }

    @Test
    fun `deve consultar chave localmente quando for passado apenas ela e existir no banco`() {
        val chavePix = chavePixModel()
        repository.save(chavePix)

        val result = grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                .setChavePix(chavePix.chave)
                .build())

        with(result) {
            assertEquals(chavePix.id.toString(), result.idPix)
            assertEquals(chavePix.tipoChave.name, chave.tipoChave.name)
            assertEquals(chavePix.chave, chave.chavePix)
            assertEquals(chavePix.idCliente.toString(), idClient)
            assertEquals(chavePix.tipoConta.name, chave.conta.tipoConta.name)
            assertEquals(chavePix.conta.nomeTitular, chave.conta.nomeTitular)
            assertEquals(chavePix.conta.cpfTitular, chave.conta.cpfTitular)
            assertEquals(chavePix.conta.agencia, chave.conta.agencia)
            assertEquals(chavePix.conta.numero, chave.conta.numero)
            assertEquals(chavePix.conta.instituicaoNome, chave.conta.instituicao)
            assertNotNull(result.chave.criadoEm)
        }
    }

    @Test
    fun `deve consultar chave no bcb quando for passado apenas ela e nao existir no banco`() {
        val chavePix = chavePixModel()

        `when`(bcbClient.consultaChave(chavePix.chave))
                .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val result = grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                .setChavePix(chavePix.chave)
                .build())

        with(result) {
            assertEquals(chavePix.tipoChave.name, chave.tipoChave.name)
            assertEquals(chavePix.chave, chave.chavePix)
            assertEquals(chavePix.tipoConta.name, chave.conta.tipoConta.name)
            assertEquals(chavePix.conta.nomeTitular, chave.conta.nomeTitular)
            assertEquals(chavePix.conta.cpfTitular, chave.conta.cpfTitular)
            assertEquals(chavePix.conta.agencia, chave.conta.agencia)
            assertEquals(chavePix.conta.numero, chave.conta.numero)
            assertEquals(chavePix.conta.instituicaoNome, chave.conta.instituicao)
            assertNotNull(result.chave.criadoEm)
        }
    }

    @Test
    fun `deve retornar NOT_FOUND quando for passado chave que nao existir nem local nem no bcb`() {
        `when`(bcbClient.consultaChave("1234567890"))
                .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                    .setChavePix("1234567890")
                    .build())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `deve retornar NOT_FOUND quando for passado idCliente e idPix que nao existir local`() {
        val idInexistente = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChave(ConsultaChaveRequest.newBuilder()
                    .setPixEClienteId(ConsultaChaveRequest.ConsultaPorPixEClienteId.newBuilder()
                            .setIdPix(idInexistente)
                            .setIdCliente(idInexistente)
                    )
                    .build())
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando filtro for invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consultaChave(ConsultaChaveRequest.newBuilder().build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
        }
    }


    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
                KeyType.CPF,
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
                createdAt = LocalDateTime.MIN
        )
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

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub {
            return KeyManagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}