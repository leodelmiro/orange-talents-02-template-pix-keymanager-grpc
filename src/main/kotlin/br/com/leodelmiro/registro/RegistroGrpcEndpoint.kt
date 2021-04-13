package br.com.leodelmiro.registro

import br.com.leodelmiro.KeyManagerRegistraGrpcServiceGrpc
import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.RegistroChaveResponse
import br.com.leodelmiro.compartilhado.validacao.errorResponse
import br.com.leodelmiro.registro.validacao.valida
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RegistroGrpcEndpoint(private val registraChaveService: RegistraChaveService) : KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceImplBase() {

    override fun registrarChave(request: RegistroChaveRequest?, responseObserver: StreamObserver<RegistroChaveResponse>?) {

        val possivelErroValidacao = request.valida()
        possivelErroValidacao?.let {
            responseObserver?.errorResponse(Status.INVALID_ARGUMENT, it)
            return
        }

        val chavePix = registraChaveService.registra(request)

        responseObserver!!.onNext(
                RegistroChaveResponse.newBuilder()
                        .setIdPix(chavePix.id.toString())
                        .setChavePix(chavePix.chave)
                        .build())
        responseObserver.onCompleted()

    }
}
