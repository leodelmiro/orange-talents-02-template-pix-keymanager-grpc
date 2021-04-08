package br.com.leodelmiro.registro

import br.com.leodelmiro.KeyManagerGrpcServiceGrpc
import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.RegistroChaveResponse
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage
import br.com.leodelmiro.compartilhado.validacao.errorResponse
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import br.com.leodelmiro.registro.validacao.validaRequest
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RegistroGrpcEndpoint(private val registraChaveService: RegistraChaveService) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registrarChave(request: RegistroChaveRequest?, responseObserver: StreamObserver<RegistroChaveResponse>?) {

        val possibleValidationError = validaRequest(request)
        possibleValidationError?.let {
            responseObserver?.errorResponse(Status.INVALID_ARGUMENT, it)
            return
        }

        try {
            val chavePix = registraChaveService.registra(request)

            responseObserver!!.onNext(
                    RegistroChaveResponse.newBuilder()
                            .setIdPix(chavePix.id.toString())
                            .setChavePix(chavePix.chave)
                            .build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            when (e) {
                is PixJaExistenteException -> responseObserver?.errorResponse(Status.ALREADY_EXISTS, ErrorMessage(e.message))
                is java.lang.IllegalStateException -> responseObserver?.errorResponse(Status.INVALID_ARGUMENT, ErrorMessage(e.message))
                else -> responseObserver?.errorResponse(Status.INTERNAL, ErrorMessage(e.message))
            }
        }
    }
}