package br.com.leodelmiro.remocao

import br.com.leodelmiro.KeyManagerRemoveGrpcServiceGrpc
import br.com.leodelmiro.RemocaoChaveRequest
import br.com.leodelmiro.RemocaoChaveResponse
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ErrorHandler
import br.com.leodelmiro.compartilhado.validacao.errorResponse
import br.com.leodelmiro.remocao.validacao.valida
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemocaoGrpcEndpoint(private val removeChaveService: RemoveChaveService) :
    KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase() {


    override fun removerChave(
        request: RemocaoChaveRequest?,
        responseObserver: StreamObserver<RemocaoChaveResponse>?
    ) {

        val possivelErroValidacao = request?.valida()
        possivelErroValidacao?.let {
            responseObserver?.errorResponse(Status.INVALID_ARGUMENT, it)
            return
        }

        val idChavePix = UUID.fromString(request!!.idPix)
        val idCliente = UUID.fromString(request.idCliente)

        removeChaveService.remove(idChavePix, idCliente)

        responseObserver!!.onNext(
            RemocaoChaveResponse.newBuilder()
                .setIdPix(idChavePix.toString())
                .setIdCliente(idCliente.toString())
                .build()
        )
        responseObserver.onCompleted()
    }

}