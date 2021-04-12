package br.com.leodelmiro.remocao.validacao

import br.com.leodelmiro.RemocaoChaveRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ValidacoesRemocaoKtTest {

    @Test
    fun `valida deve retornar null quando tudo Ok`() {
        val request = remocaoChaveRequest()

        val result = request.valida()

        assertNull(result)
    }

    @Test
    fun `valida deve retornar ErrorMessage se pix for branco`() {
        val request = remocaoChaveRequest(idPix = "")

        val result = request.valida()

        assertEquals("Id do pix deve ser informado", result!!.description)
    }

    @Test
    fun `valida deve retornar ErrorMessage se idCliente for branco`() {
        val request = remocaoChaveRequest(idCliente = "")

        val result = request.valida()

        assertEquals("Id do cliente deve ser informado", result!!.description)
    }

    @ParameterizedTest
    @CsvSource("c56dfef4-7901-44fb-a2cefb157890", "c56dfef4-7901-44fb-84e2-a2", "teste", "12345678")
    fun `valida deve retornar ErrorMessage se idCliente for formato diferente de UUID`(idCliente: String) {
        val request = remocaoChaveRequest(idCliente = idCliente)

        val result = request.valida()

        assertEquals("Id do cliente deve ter um formato UUID válido", result!!.description)
    }

    @ParameterizedTest
    @CsvSource("c56dfef4-7901-44fb-a2cefb157890", "c56dfef4-7901-44fb-84e2-a2", "teste", "12345678")
    fun `valida deve retornar ErrorMessage se idPix for formato diferente de UUID`(idPix: String) {
        val request = remocaoChaveRequest(idPix = idPix)

        val result = request.valida()

        assertEquals("Id do pix deve ter um formato UUID válido", result!!.description)
    }


    private fun remocaoChaveRequest(idPix: String = "324aa21c-3d23-472d-b1ff-7d855ede51fd",
                            idCliente: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"): RemocaoChaveRequest {
        return RemocaoChaveRequest.newBuilder()
                .setIdPix(idPix)
                .setIdCliente(idCliente)
                .build()
    }
}