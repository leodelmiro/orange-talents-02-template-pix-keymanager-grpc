package br.com.leodelmiro.compartilhado.exceptions.interceptor.handlers

import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler
import br.com.leodelmiro.compartilhado.exceptions.interceptor.ExceptionHandler.StatusWithDetails
import br.com.leodelmiro.registro.exceptions.ClienteNaoEncontradoException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ClienteNaoEncontradoExceptionHandler : ExceptionHandler<ClienteNaoEncontradoException> {
    override fun handle(e: ClienteNaoEncontradoException): StatusWithDetails {
        return StatusWithDetails(Status.NOT_FOUND.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ClienteNaoEncontradoException
    }
}