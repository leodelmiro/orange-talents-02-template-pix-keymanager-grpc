package br.com.leodelmiro.registro

import br.com.leodelmiro.KeyManagerRegistraGrpcServiceGrpc
import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.apis.ErpClient
import br.com.leodelmiro.compartilhado.chavepix.*
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
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistroGrpcEndpointTest(val repository: ChavePixRepository,
                                        val grpcClient: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub
) {

    @Inject
    lateinit var erpClient: ErpClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar chave quando tudo ok`() {
        val chavePixCpf = "96498610093"
        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
                .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        val result = grpcClient.registrarChave(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                .setChave(chavePixCpf)
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(result) {
            assertEquals(chavePixCpf, chavePix)
            assertNotNull(idPix)
            assertNotNull(repository.findById(UUID.fromString(result.idPix)))
        }
    }


    @Test
    fun `nao deve registrar chave quando algum dado passado for invalido`() {
        val chavePixCpf = "22223333333333232"

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(RegistroChaveRequest.newBuilder()
                    .setIdCliente(CLIENTE_ID.toString())
                    .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                    .setChave(chavePixCpf)
                    .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `nao deve registrar chave e deve lancar ALREADY_EXISTS quando chave pix ja existir`() {
        val chave = "96498610093"
        val chavePixModel = chavePixModel(chave)
        repository.save(chavePixModel)

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(RegistroChaveRequest.newBuilder()
                    .setIdCliente(CLIENTE_ID.toString())
                    .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                    .setChave(chave)
                    .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertNotNull(status.description)
        }
    }

    @Test
    fun `nao deve registrar chave e deve lancar NOT_FOUND quando clientId nao existir no erpClient`() {
        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
                .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registrarChave(RegistroChaveRequest.newBuilder()
                    .setIdCliente(CLIENTE_ID.toString())
                    .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                    .setChave("96498610093")
                    .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
        }
    }

    private fun contaUsuarioResponse(): ContaUsuarioResponse {
        return ContaUsuarioResponse(
                TitularResponse("Leonardo Delmiro", "96498610093"),
                InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
                "0001",
                "291900"
        )
    }

    private fun chavePixModel(chave: String): ChavePix {
        return ChavePix(CLIENTE_ID.toString(),
                TipoConta.CONTA_CORRENTE,
                chave,
                TipoChave.CPF,
                ContaUsuario("ITAÚ UNIBANCO S.A.",
                        "60701190",
                        "Leonardo Delmiro",
                        "96498610093",
                        "0001",
                        "291900"))
    }


    @MockBean(ErpClient::class)
    fun erpClient(): ErpClient? {
        return Mockito.mock(ErpClient::class.java)
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}