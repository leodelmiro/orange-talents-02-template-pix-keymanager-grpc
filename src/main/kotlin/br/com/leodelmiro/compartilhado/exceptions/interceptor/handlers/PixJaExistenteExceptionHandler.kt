package br.com.leodelmiro.compartilhado.exceptions.interceptor.handlers

import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler.StatusWithDetails
import br.com.leodelmiro.registro.exceptions.PixJaExistenteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixJaExistenteExceptionHandler : ExceptionHandler<PixJaExistenteException> {
    override fun handle(e: PixJaExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is PixJaExistenteException
    }
}