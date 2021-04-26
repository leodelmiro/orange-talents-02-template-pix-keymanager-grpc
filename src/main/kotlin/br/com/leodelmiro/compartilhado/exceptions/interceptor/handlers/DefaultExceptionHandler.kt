package br.com.leodelmiro.compartilhado.exceptions.interceptor.handlers

import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler.StatusWithDetails
import io.grpc.Status

/**
 * By design, this class must NOT be managed by Micronaut
 */
class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            else -> Status.UNKNOWN.withDescription("Erro inesperado aconteceu")
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}