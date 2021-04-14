package br.com.leodelmiro.compartilhado.chavepix

import br.com.leodelmiro.TipoConta as TipoContaProto

enum class TipoConta {
    CONTA_CORRENTE, CONTA_POUPANCA, INVALIDA;

    companion object {
        fun by(message: TipoContaProto?): TipoConta {
            return when (message) {
                TipoContaProto.CONTA_CORRENTE -> CONTA_CORRENTE
                TipoContaProto.CONTA_POUPANCA -> CONTA_POUPANCA
                else -> INVALIDA
            }
        }
    }
}