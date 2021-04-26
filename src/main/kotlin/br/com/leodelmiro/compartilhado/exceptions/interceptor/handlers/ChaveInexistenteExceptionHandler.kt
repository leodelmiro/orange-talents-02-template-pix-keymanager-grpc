package br.com.leodelmiro.compartilhado.exceptions.interceptor.handlers

import br.com.leodelmiro.compartilhado.exceptions.ChaveInexistenteException
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChaveInexistenteExceptionHandler : ExceptionHandler<ChaveInexistenteException> {
    override fun handle(e: ChaveInexistenteException): StatusWithDetails {
        return StatusWithDetails(Status.NOT_FOUND.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChaveInexistenteException
    }

}