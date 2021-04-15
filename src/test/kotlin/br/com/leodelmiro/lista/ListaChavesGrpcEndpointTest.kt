package br.com.leodelmiro.lista

import br.com.leodelmiro.KeyManagerListaGrpcServiceGrpc
import br.com.leodelmiro.ListaChavesRequest
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ListaChavesGrpcEndpointTest(@Inject private val repository: ChavePixRepository,
                                           @Inject private val grpcClient: KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceBlockingStub) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar chaves quando for passado um IdCliente existente`() {
        val chavePix = chavePixModel()
        repository.save(chavePix)

        val result = grpcClient.listaChaves(ListaChavesRequest.newBuilder()
                .setIdCliente(chavePix.idCliente.toString())
                .build()
        )

        with(result) {
            assertEquals(chavePix.idCliente.toString(), idCliente)
            assertEquals(1, chavesPixCount)
            assertEquals(chavePix.id.toString(), chavesPixList[0].idPix)
            assertEquals(chavePix.tipoChave.name, chavesPixList[0].tipoChave.name)
            assertEquals(chavePix.tipoConta.name, chavesPixList[0].tipoConta.name)
            assertNotNull(chavesPixList[0].criadoEm)
        }
    }

    @Test
    fun `deve devolver lista de chaves vazia quando nao for encontrada chave`() {
        val idInexistente = UUID.randomUUID().toString()

        val result = grpcClient.listaChaves(ListaChavesRequest.newBuilder()
                .setIdCliente(idInexistente)
                .build()
        )

        with(result) {
            assertEquals(idInexistente, idCliente)
            assertTrue(chavesPixList.isEmpty())
        }
    }

    @Test
    fun `deve devolver INVALID_ARGUMENT quando idCliente passado for nulo ou vazio`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.listaChaves(ListaChavesRequest.newBuilder()
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Id cliente não pode ser nulo ou vazio!", status.description)
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

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceBlockingStub {
            return KeyManagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}