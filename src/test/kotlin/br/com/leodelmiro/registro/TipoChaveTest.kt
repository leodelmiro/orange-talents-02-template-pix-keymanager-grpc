package br.com.leodelmiro.registro

import br.com.leodelmiro.compartilhado.chavepix.TipoChave
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class TipoChaveTest {

    @ParameterizedTest
    @CsvSource(
            "118.421.000-41 , Por favor insira um CPF válido , Formato esperado é 12345678901 e deve ser válido",
            "12345678901 , Por favor insira um CPF válido , Formato esperado é 12345678901 e deve ser válido",
            ", CPF é Obrigatório, ",
            "'', CPF é Obrigatório, "
    )
    fun `valida do tipo CPF deve retornar um ErrorMessage quando for cpf invalido`(cpf: String?,
                                                                                   expectedDescription: String,
                                                                                   expectedAugment: String?) {

        val result = TipoChave.CPF.valida(cpf)

        assertEquals(expectedDescription, result?.description)
        assertEquals(expectedAugment, result?.augmentDescription)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `valida do tipo CPF deve retornar null quando for cpf valido`() {
        val result = TipoChave.CPF.valida("11842100041")

        assertEquals(null, result)
    }

    @ParameterizedTest
    @CsvSource(
            "12345678, Por favor insira um formato de telefone celular válido , Formato esperado é +5585988714077",
            "+ddddddddddedes, Por favor insira um formato de telefone celular válido , Formato esperado é +5585988714077",
            "+55(85)98871-4077 , Por favor insira um formato de telefone celular válido , Formato esperado é +5585988714077",
            ", Por favor insira um formato de telefone celular válido , Formato esperado é +5585988714077",
            "'', Por favor insira um formato de telefone celular válido , Formato esperado é +5585988714077"
    )
    fun `valida do tipo Celular deve retornar um ErrorMessage quando for celular invalido`(celular: String?,
                                                                                           expectedDescription: String,
                                                                                           expectedAugment: String) {

        val result = TipoChave.CELULAR.valida(celular)

        assertEquals(expectedDescription, result?.description)
        assertEquals(expectedAugment, result?.augmentDescription)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `valida do tipo Celular deve retornar null quando for celular valido`() {
        val result = TipoChave.CELULAR.valida("+5585988714077")

        assertEquals(null, result)
    }

    @ParameterizedTest
    @CsvSource(
            "email.email.com , Por favor insira um formato de email válido",
            "teste@ , Por favor insira um formato de email válido",
            ", Email é obrigatório",
            "'', Email é obrigatório"
    )
    fun `valida do tipo Email deve retornar um ErrorMessage quando for email invalido`(email: String?,
                                                                                       expectedDescription: String) {

        val result = TipoChave.EMAIL.valida(email)

        assertEquals(expectedDescription, result?.description)
        assertEquals("Formato esperado é email@email.com", result?.augmentDescription)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `valida do tipo Email deve retornar null quando for email valido`() {
        val result = TipoChave.EMAIL.valida("email@email.com")

        assertEquals(null, result)
    }

    @ParameterizedTest
    @CsvSource("email@email.com", "1234566", "qualquercoisa")
    fun `valida do tipo Aleatoria deve retornar um ErrorMessage quando for preenchuda`(chave: String) {
        val result = TipoChave.ALEATORIA.valida(chave)

        assertEquals("Chave aleatória não deve ter chave preenchida", result?.description)
        assertEquals("Tente novamente sem preencher a chave", result?.augmentDescription)
        assertTrue(result is ErrorMessage)
    }

    @ParameterizedTest
    @CsvSource(",", "''")
    fun `valida do tipo Aleatoria deve retornar null quando nao for preenchida`(chave: String?) {
        val result = TipoChave.ALEATORIA.valida(chave)

        assertEquals(null, result)
    }

    @ParameterizedTest
    @CsvSource("email@email.com", "1234566", "qualquercoisa", "+5585988714077")
    fun `valida do tipo Invalida deve retornar sempre um ErrorMessage`(chave: String) {
        val result = TipoChave.INVALIDA.valida(chave)

        assertEquals("Tipo de chave inválido", result?.description)
        assertEquals("Por favor insira um tipo de chave válido", result?.augmentDescription)
        assertTrue(result is ErrorMessage)
    }
}