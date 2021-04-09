package br.com.leodelmiro.registro

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ContaUsuarioResponseTest {

    @Test
    fun `deve retornar ContaUsuarioResponse para classe modelo ContaUsuario`() {
        val contaUsuarioResponse = ContaUsuarioResponse(TitularResponse("Teste", "11111111111"),
                InstituicaoResponse("Teste", "1234"),
                "12345",
                "123")

        val result = contaUsuarioResponse.toModel()

        assertEquals("Teste", result.nomeTitular)
        assertEquals("11111111111", result.cpfTitular)
        assertEquals("Teste", result.instituicaoNome)
        assertEquals("1234", result.instituicaoIspb)
        assertEquals("12345", result.agencia)
        assertEquals("123", result.numero)
    }
}