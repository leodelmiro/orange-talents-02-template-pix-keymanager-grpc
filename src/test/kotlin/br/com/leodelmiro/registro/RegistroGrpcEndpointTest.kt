package br.com.leodelmiro.registro

import br.com.leodelmiro.KeyManagerRegistraGrpcServiceGrpc
import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.apis.*
import br.com.leodelmiro.compartilhado.apis.CreatePixKeyRequest.Companion.toRequest
import br.com.leodelmiro.compartilhado.chavepix.*
import br.com.leodelmiro.utils.MockitoHelper
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class RegistroGrpcEndpointTest(private val repository: ChavePixRepository,
                                        private val grpcClient: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub
) {

    @Inject
    lateinit var erpClient: ErpClient

    @Inject
    lateinit var bcbClient: BcbClient

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

        `when`(bcbClient.cadastraChave(chavePixModel(chavePixCpf, "CPF").toRequest()))
                .thenReturn(HttpResponse.ok(createPixKeyResponse()))

        val result = grpcClient.registrarChave(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.CPF)
                .setChave(chavePixCpf)
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(result) {
            verify(erpClient, times(1)).consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE)
            verify(bcbClient, times(1)).cadastraChave(chavePixModel(chavePixCpf, "CPF").toRequest())
            assertEquals(chavePixCpf, chavePix)
            assertNotNull(idPix)
            assertNotNull(repository.findById(UUID.fromString(result.idPix)))
        }
    }

    @Test
    fun `deve gerar chave aletoria pelo sistema do BCB quando tudo ok e for do tipo Aleatoria`() {
        val chaveBcb = UUID.randomUUID()

        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
                .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        `when`(bcbClient.cadastraChave(chavePixModel(MockitoHelper.anyObject()).toRequest()))
                .thenReturn(HttpResponse.ok(createPixKeyResponse("RANDOM", chaveBcb.toString())))

        val result = grpcClient.registrarChave(RegistroChaveRequest.newBuilder()
                .setIdCliente(CLIENTE_ID.toString())
                .setTipoChave(RegistroChaveRequest.TipoChave.ALEATORIA)
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(result) {
            verify(erpClient, times(1)).consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE)
            verify(bcbClient, times(1)).cadastraChave(chavePixModel(MockitoHelper.anyObject()).toRequest())
            assertEquals(chaveBcb.toString(), chavePix)
            assertNotNull(idPix)
            assertNotNull(repository.findById(chaveBcb))
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
    fun `nao deve registrar chave e deve lancar ALREADY_EXISTS quando chave pix ja existir no bcb(erro 422)`() {
        val chave = "96498610093"

        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
                .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        `when`(bcbClient.cadastraChave(chavePixModel(chave, "CPF").toRequest()))
                .thenThrow(HttpClientResponseException("Erro", HttpResponse.unprocessableEntity<Any>()))

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
    fun `nao deve registrar chave e deve lancar ALREADY_EXISTS quando chave pix ja existir no banco`() {
        val chave = "96498610093"
        val chavePixModel = chavePixModel(chave, "CPF")
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
    fun `nao deve registrar chave e deve lancar INTERNAL quando der algum erro no BCB diferente de 422`() {
        val chave = "96498610093"

        `when`(erpClient.consulta(CLIENTE_ID.toString(), TipoConta.CONTA_CORRENTE))
                .thenReturn(HttpResponse.ok(contaUsuarioResponse()))

        `when`(bcbClient.cadastraChave(chavePixModel(chave, "CPF").toRequest()))
                .thenThrow(HttpClientResponseException("Erro", HttpResponse.serverError<Any>()))

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
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
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

    private fun chavePixModel(chave: String?, tipoChave: String = "ALEATORIA"): ChavePix {
        return ChavePix(CLIENTE_ID,
                TipoConta.CONTA_CORRENTE,
                chave ?: UUID.randomUUID().toString(),
                TipoChave.valueOf(tipoChave),
                ContaUsuario("ITAÚ UNIBANCO S.A.",
                        "60701190",
                        "Leonardo Delmiro",
                        "96498610093",
                        "0001",
                        "291900"))
    }

    private fun createPixKeyResponse(keyType: String = "CPF", key: String = "96498610093"): CreatePixKeyResponse {
        return CreatePixKeyResponse(
                keyType = KeyType.valueOf(keyType),
                key = key,
                bankAccount = BankAccount(
                        participant = "60701190",
                        branch = "0001",
                        accountType = BankAccount.AccountType.CACC,
                        accountNumber = "291900",
                ),
                owner = Owner(
                        type = Owner.Type.NATURAL_PERSON,
                        name = "Leonardo Delmiro",
                        taxIdNumber = "96498610093"
                )
        )
    }

    @MockBean(ErpClient::class)
    fun erpClient(): ErpClient? {
        return mock(ErpClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}