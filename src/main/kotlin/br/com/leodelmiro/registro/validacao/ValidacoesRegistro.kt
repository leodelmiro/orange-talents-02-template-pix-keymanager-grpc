package br.com.leodelmiro.registro.validacao

import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage
import br.com.leodelmiro.registro.TipoChave
import br.com.leodelmiro.registro.TipoConta
import br.com.leodelmiro.registro.requestParaTipoChave
import br.com.leodelmiro.registro.requestParaTipoConta

fun validaRequest(request: RegistroChaveRequest?): ErrorMessage? {
    var possibleErrorMessage = validaIdCliente(request?.idCliente)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = validaTipoChave(request?.tipoChave)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = requestParaTipoChave(request?.tipoChave).valida(request?.chave)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = validaTipoConta(request?.tipoConta)
    possibleErrorMessage?.let {
        return it
    }

    return null
}

fun validaIdCliente(clientId: String?): ErrorMessage? {
    if (clientId.isNullOrBlank()) {
        return ErrorMessage(description = "Id do cliente é obrigatório")
    }

    if (!clientId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".toRegex())) {
        return ErrorMessage(description = "Id do cliente deve conter um formato UUID válido")
    }

    clientId?.let {
        return null
    }


}

fun validaTipoConta(requestTipoConta: RegistroChaveRequest.TipoConta?): ErrorMessage? {
    if (requestTipoConta == null) {
        return ErrorMessage(description = "Tipo de conta é obrigatório")
    }

    if (requestParaTipoConta(requestTipoConta) == TipoConta.INVALIDA) {
        return ErrorMessage(description = "Tipo de conta deve ser válida")
    }

    return null
}

fun validaTipoChave(requestTipoChave: RegistroChaveRequest.TipoChave?): ErrorMessage? {
    if (requestTipoChave == null) {
        return ErrorMessage(description = "Tipo de chave é obrigatório")
    }

    if (requestParaTipoChave(requestTipoChave) == TipoChave.INVALIDA) {
        return ErrorMessage(description = "Tipo de chave deve ser válida")
    }

    return null
}
