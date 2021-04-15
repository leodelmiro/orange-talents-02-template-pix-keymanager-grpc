package br.com.leodelmiro.consulta

import br.com.leodelmiro.ConsultaChaveRequest
import br.com.leodelmiro.ConsultaChaveResponse
import br.com.leodelmiro.KeyManagerConsultaGrpcServiceGrpc
import br.com.leodelmiro.compartilhado.apis.BcbClient
import br.com.leodelmiro.compartilhado.chavepix.ChavePixRepository
import br.com.leodelmiro.consulta.utils.ConsultaChaveResponseConverter
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
class ConsultaGrpcEndpoint(@Inject private val repository: ChavePixRepository,
                           @Inject private val bcbClient: BcbClient,
                           @Inject private val validator: Validator
) : KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceImplBase() {

    override fun consultaChave(request: ConsultaChaveRequest, responseObserver: StreamObserver<ConsultaChaveResponse>) {

        val filtro = request.filtro(validator)
        val consulta = filtro.consulta(repository, bcbClient)

        responseObserver.onNext(ConsultaChaveResponseConverter().converte(consulta))
        responseObserver.onCompleted()
    }


}