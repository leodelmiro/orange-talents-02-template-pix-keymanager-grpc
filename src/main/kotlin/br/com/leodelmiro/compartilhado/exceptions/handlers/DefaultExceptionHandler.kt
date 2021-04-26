package br.com.leodelmiro.compartilhado.exceptions.handlers

import br.com.leodelmiro.compartilhado.exceptions.ExceptionHandler
import br.com.leodelmiro.compartilhado.exceptions.ExceptionHandler.StatusWithDetails
import br.com.leodelmiro.registro.exceptions.ClienteNaoEncontradoException
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import br.com.leodelmiro.remocao.exceptions.ChaveInexistenteException
import io.grpc.Status

/**
 * By design, this class must NOT be managed by Micronaut
 */
class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is ChaveInexistenteException -> Status.NOT_FOUND.withDescription(e.message)
            is ClienteNaoEncontradoException -> Status.NOT_FOUND.withDescription(e.message)
            is PixJaExistenteException -> Status.ALREADY_EXISTS.withDescription(e.message)
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