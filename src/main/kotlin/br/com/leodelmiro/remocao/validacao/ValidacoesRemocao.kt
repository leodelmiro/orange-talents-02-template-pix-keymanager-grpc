package br.com.leodelmiro.remocao.validacao

import br.com.leodelmiro.RemocaoChaveRequest
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage

fun RemocaoChaveRequest.valida(): ErrorMessage? {
    val uuidFormat = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".toRegex()

    if(idPix.isNullOrBlank()) return ErrorMessage("Id do pix deve ser informado")
    if (idCliente.isNullOrBlank()) return ErrorMessage("Id do cliente deve ser informado")
    if (!idCliente.matches(uuidFormat)) return ErrorMessage("Id do cliente deve ter um formato UUID válido")
    if (!idPix.matches(uuidFormat)) return ErrorMessage("Id do pix deve ter um formato UUID válido")

    return null
}