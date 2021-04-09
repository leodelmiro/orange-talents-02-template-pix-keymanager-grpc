package br.com.leodelmiro.registro

import br.com.leodelmiro.compartilhado.chavepix.ContaUsuario
import br.com.leodelmiro.compartilhado.chavepix.TipoChave
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
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