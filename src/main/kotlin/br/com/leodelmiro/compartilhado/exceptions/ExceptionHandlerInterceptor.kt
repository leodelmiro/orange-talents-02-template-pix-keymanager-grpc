package br.com.leodelmiro.compartilhado.exceptions

import br.com.leodelmiro.registro.exceptions.ClienteNaoEncontradoException
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import br.com.leodelmiro.remocao.exceptions.ChaveInexistenteException
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto.toStatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptor : MethodInterceptor<BindableService, Any> {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        try {
            return context.proceed()
        } catch (e: Exception) {
            LOGGER.error(e.message)

            val statusError = when (e) {
                is ChaveInexistenteException -> Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                is ClienteNaoEncontradoException -> Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                is PixJaExistenteException -> Status.ALREADY_EXISTS.withDescription(e.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException()
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> handleConstraintValidationException(e)
                else -> Status.UNKNOWN.withDescription("Erro inesperado aconteceu").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null
        }
    }

    private fun handleConstraintValidationException(e: ConstraintViolationException): StatusRuntimeException {
        val badRequest = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name)
                    .setDescription(it.message)
                    .build()
            }
            ).build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Request com parametros inválidos")
            .addDetails(com.google.protobuf.Any.pack(badRequest))
            .build()

        LOGGER.info("$statusProto")
        return toStatusRuntimeException(statusProto)
    }
}