package br.com.leodelmiro.lista

import br.com.leodelmiro.*
import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesGrpcEndpoint(@Inject private val repository: ChavePixRepository) :
        KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceImplBase() {

    override fun listaChaves(request: ListaChavesRequest, responseObserver: StreamObserver<ListaChavesResponse>) {

        if (request.idCliente.isNullOrBlank())
            throw IllegalArgumentException("Id cliente não pode ser nulo ou vazio!")

        val idCliente = UUID.fromString(request.idCliente)

        val listaChavesPix = repository.findAllByIdCliente(idCliente).map {
            ListaChavesResponse.ChaveResponse.newBuilder()
                    .setIdPix(it.id.toString())
                    .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                    .setChave(it.chave)
                    .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                    .setCriadoEm(it.criadoEm.let { criadoEm ->
                        val instantCriadoEm = criadoEm.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                                .setSeconds(instantCriadoEm.epochSecond)
                                .setNanos(instantCriadoEm.nano)
                                .build()
                    })
                    .build()
        }

        responseObserver.onNext(ListaChavesResponse.newBuilder()
                .setIdCliente(idCliente.toString())
                .addAllChavesPix(listaChavesPix)
                .build()
        )

        responseObserver.onCompleted()
    }
}