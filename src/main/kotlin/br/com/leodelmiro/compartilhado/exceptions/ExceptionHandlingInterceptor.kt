package br.com.leodelmiro.compartilhado.exceptions

import br.com.leodelmiro.registro.exceptions.ClienteNaoEncontradoException
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import br.com.leodelmiro.remocao.exceptions.ChaveInexistenteException
import io.grpc.*
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ExceptionHandlingInterceptor : ServerInterceptor {

    private val logger = LoggerFactory.getLogger(ExceptionHandlingInterceptor::class.java)

    override fun <ReqT : Any, RespT : Any> interceptCall(
            call: ServerCall<ReqT, RespT>,
            headers: Metadata,
            next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {

        fun handleException(call: ServerCall<ReqT, RespT>, e: Exception) {
            logger.info("Exception capturada: ${e.javaClass.simpleName}")

            val exceptionParaStatus = when (e) {
                is ChaveInexistenteException -> Status.NOT_FOUND
                is ClienteNaoEncontradoException -> Status.NOT_FOUND
                is PixJaExistenteException -> Status.ALREADY_EXISTS
                is IllegalStateException -> Status.FAILED_PRECONDITION
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                else -> Status.UNKNOWN
            }

            val messageRetorno = exceptionParaStatus.withDescription(e.message)
            call.close(messageRetorno, headers)
        }

        val listener: ServerCall.Listener<ReqT> = try {
            next.startCall(call, headers)
        } catch (ex: Exception) {
            handleException(call, ex)
            throw ex
        }

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            override fun onHalfClose() {
                try {
                    super.onHalfClose()
                } catch (ex: Exception) {
                    handleException(call, ex)
                }
            }
        }
    }
}