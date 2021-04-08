package br.com.leodelmiro.registro

import br.com.leodelmiro.RegistroChaveRequest

enum class TipoConta {
    CONTA_CORRENTE, CONTA_POUPANCA, INVALIDA
}

fun requestParaTipoConta(message: RegistroChaveRequest.TipoConta?): TipoConta {
    return when (message) {
        RegistroChaveRequest.TipoConta.CONTA_CORRENTE -> TipoConta.CONTA_CORRENTE
        RegistroChaveRequest.TipoConta.CONTA_POUPANCA -> TipoConta.CONTA_POUPANCA
        else -> TipoConta.INVALIDA
    }
}