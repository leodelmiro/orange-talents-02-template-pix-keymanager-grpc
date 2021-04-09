package br.com.leodelmiro.registro.validacao

import br.com.leodelmiro.RegistroChaveRequest
import br.com.leodelmiro.compartilhado.validacao.ErrorMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito


internal class ValidacoesRegistroKtTest {

    @ParameterizedTest
    @CsvSource(
            "8d91cebf-c17b-33333-ac3e-d26dcfa7d041, Id do cliente deve conter um formato UUID válido",
            ", Id do cliente é obrigatório",
            "'', Id do cliente é obrigatório",
            "24ba4c9b-c80d-48e1-12132131-17e9a5bb790c, Id do cliente deve conter um formato UUID válido",
            "562a3a5f-206e-49fe-0fe3cbd2b3cd, Id do cliente deve conter um formato UUID válido"
    )
    fun `validaIdCliente deve retornar um ErrorMessage quando nao for um uuid valido, blank ou null`(uuid: String?, expectedResult: String) {
        val result = validaIdCliente(uuid)

        assertEquals(expectedResult, result?.description)
        assertTrue(result is ErrorMessage)
    }

    @ParameterizedTest
    @CsvSource(
            "8d91cebf-c17b-4ba3-ac3e-d26dcfa7d041",
            "24ba4c9b-c80d-48e1-ba47-17e9a5bb790c",
            "562a3a5f-206e-49fe-8d56-0fe3cbd2b3cd"
    )
    fun `validaIdCliente deve retornar null quando for um uuid valido`(uuid: String) {
        val result = validaIdCliente(uuid)

        assertEquals(null, result?.description)
    }

    @Test
    fun `validaTipoConta deve retornar um ErrorMessage quando nao for um Tipo Conta valido`() {
        val result = validaTipoConta(RegistroChaveRequest.TipoConta.CONTA_DESCONHECIDA)

        assertEquals("Tipo de conta deve ser válida", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `validaTipoConta deve retornar um ErrorMessage quando for null`() {
        val result = validaTipoConta(null)

        assertEquals("Tipo de conta é obrigatório", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @ParameterizedTest
    @CsvSource(
            "CONTA_POUPANCA",
            "CONTA_CORRENTE"
    )
    fun `validaTipoConta deve retornar null quando tipo conta valido`(tipoContaString: String) {
        val tipoConta = RegistroChaveRequest.TipoConta.valueOf(tipoContaString)
        val result = validaTipoConta(tipoConta)

        assertEquals(null, result?.description)
    }

    @Test
    fun `validaTipoChave deve retornar um ErrorMessage quando nao for um Tipo Chave valido`() {
        val result = validaTipoChave(RegistroChaveRequest.TipoChave.CHAVE_DESCONHECIDA)

        assertEquals("Tipo de chave deve ser válida", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @Test
    fun `validaTipoChave deve retornar um ErrorMessage quando for null`() {
        val result = validaTipoChave(null)

        assertEquals("Tipo de chave é obrigatório", result?.description)
        assertTrue(result is ErrorMessage)
    }

    @ParameterizedTest
    @CsvSource(
            "CPF",
            "ALEATORIA",
            "EMAIL",
            "CELULAR"
    )
    fun `validaTipoChave deve retornar null quando tipo chave valido`(tipoChaveString: String) {
        val tipoChave = RegistroChaveRequest.TipoChave.valueOf(tipoChaveString)
        val result = validaTipoChave(tipoChave)

        assertEquals(null, result?.description)
    }

    @Test
    fun `validaRequest deve retornar null quando request for valido`() {
        val registroRequest = RegistroChaveRequest.newBuilder()
                .setIdCliente("8d91cebf-c17b-4ba3-ac3e-d26dcfa7d041")
                .setTipoChave(RegistroChaveRequest.TipoChave.EMAIL)
                .setChave("teste@teste.com")
                .setTipoConta(RegistroChaveRequest.TipoConta.CONTA_POUPANCA)
                .build()

        val result = registroRequest.valida()

        assertEquals(null, result)
    }


    @ParameterizedTest
    @CsvSource(
            "eeee-eeee-eeee-eee,EMAIL,teste@teste.com,CONTA_POUPANCA",
            "8d91cebf-c17b-4ba3-ac3e-d26dcfa7d041, CHAVE_DESCONHECIDA, teste@teste.com , CONTA_CORRENTE",
            "24ba4c9b-c80d-48e1-ba47-17e9a5bb790c, EMAIL, eeee.eee, CONTA_CORRENTE",
            "562a3a5f-206e-49fe-8d56-0fe3cbd2b3cd, EMAIL, teste@teste.com , CONTA_DESCONHECIDA"
    )
    fun `validaRequest deve retornar ErrorResponse quando request for invalido`(
            idCliente: String,
            tipoChave: String,
            chave: String,
            tipoConta: String
    ) {
        val registroRequest = RegistroChaveRequest.newBuilder()
                .setIdCliente(idCliente)
                .setTipoChave(RegistroChaveRequest.TipoChave.valueOf(tipoChave))
                .setChave(chave)
                .setTipoConta(RegistroChaveRequest.TipoConta.valueOf(tipoConta))
                .build()

        val result = registroRequest.valida()

        assertTrue(result is ErrorMessage)
    }

}