package br.com.leodelmiro.registro

import br.com.leodelmiro.KeyManagerGrpcServiceGrpc
import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.RegistroChaveResponse
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage
import br.com.leodelmiro.compartilhado.validacao.errorResponse
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import br.com.leodelmiro.registro.validacao.valida
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RegistroGrpcEndpoint(private val registraChaveService: RegistraChaveService) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registrarChave(request: RegistroChaveRequest?, responseObserver: StreamObserver<RegistroChaveResponse>?) {

        val possibleValidationError = request.valida()
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
        } catch (e: PixJaExistenteException) {
            responseObserver?.errorResponse(Status.ALREADY_EXISTS, ErrorMessage(e.message))
        } catch (e: IllegalStateException) {
            responseObserver?.errorResponse(Status.NOT_FOUND, ErrorMessage(e.message))
        }
    }
}
