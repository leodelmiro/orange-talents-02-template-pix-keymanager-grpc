package br.com.leodelmiro.registro

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class NovaChavePixTest {

    @Test
    fun `deve retornar NovaChavePix para classe modelo ChavePix`() {
    }

    @Test
    fun `deve gerar chave uuid se for do tipo Aleatoria`() {
        val result = NovaChavePix(
                UUID.randomUUID().toString(),
                TipoConta.CONTA_POUPANCA,
                "",
                TipoChave.ALEATORIA,
                ContaUsuario("Teste", "1234", "Teste", "11111111111", "12345", "123")
        )

        assertNotEquals("", result.chave);
    }
}