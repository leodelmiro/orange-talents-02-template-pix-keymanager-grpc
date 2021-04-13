package br.com.leodelmiro.compartilhado.chavepix

import br.com.leodelmiro.RegistroChaveRequest

enum class TipoConta {
    CONTA_CORRENTE, CONTA_POUPANCA, INVALIDA;

    companion object {
        fun by(message: RegistroChaveRequest.TipoConta?): TipoConta {
            return when (message) {
                RegistroChaveRequest.TipoConta.CONTA_CORRENTE -> CONTA_CORRENTE
                RegistroChaveRequest.TipoConta.CONTA_POUPANCA -> CONTA_POUPANCA
                else -> INVALIDA
            }
        }
    }
}