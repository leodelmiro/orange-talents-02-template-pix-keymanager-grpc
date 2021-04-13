package br.com.leodelmiro.registro.validacao

import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.chavepix.TipoChave
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage

fun RegistroChaveRequest?.valida(): ErrorMessage? {
    var possibleErrorMessage = validaIdCliente(this?.idCliente)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = validaTipoChave(this?.tipoChave)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = TipoChave.by(this?.tipoChave).valida(this?.chave)
    possibleErrorMessage?.let {
        return it
    }

    possibleErrorMessage = validaTipoConta(this?.tipoConta)
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
    return null
}

fun validaTipoConta(requestTipoConta: RegistroChaveRequest.TipoConta?): ErrorMessage? {
    if (requestTipoConta == null) {
        return ErrorMessage(description = "Tipo de conta é obrigatório")
    }

    if (TipoConta.by(requestTipoConta) == TipoConta.INVALIDA) {
        return ErrorMessage(description = "Tipo de conta deve ser válida")
    }

    return null
}

fun validaTipoChave(requestTipoChave: RegistroChaveRequest.TipoChave?): ErrorMessage? {
    if (requestTipoChave == null) {
        return ErrorMessage(description = "Tipo de chave é obrigatório")
    }

    if (TipoChave.by(requestTipoChave) == TipoChave.INVALIDA) {
        return ErrorMessage(description = "Tipo de chave deve ser válida")
    }

    return null
}
