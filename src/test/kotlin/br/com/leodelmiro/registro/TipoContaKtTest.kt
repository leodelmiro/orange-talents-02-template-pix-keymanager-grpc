package br.com.leodelmiro.registro

import br.com.leodelmiro.TipoConta as TipoContaProto
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import br.com.leodelmiro.compartilhado.chavepix.TipoConta.Companion.by
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class TipoContaKtTest {

    @ParameterizedTest
    @CsvSource(
            "CONTA_CORRENTE, CONTA_CORRENTE",
            "CONTA_POUPANCA, CONTA_POUPANCA",
            "CONTA_DESCONHECIDA, INVALIDA"
    )
    fun `TipoConta by deve transformar TipoConta request para TipoConta model`(requestString: String, tipoContaEsperado: String) {
        val tipoContaRequest = TipoContaProto.valueOf(requestString)
        val resultadoEsperado = TipoConta.valueOf(tipoContaEsperado)

        val result = by(tipoContaRequest)

        assertEquals(resultadoEsperado, result)
    }
}