package br.com.leodelmiro.compartilhado.apis

import br.com.leodelmiro.compartilhado.chavepix.ChavePix
import br.com.leodelmiro.compartilhado.chavepix.TipoChave
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${apis.bancocentral.url}")
interface BcbClient {

    @Post("/keys", processes = [MediaType.APPLICATION_XML])
    fun cadastraChave(@Body cadastro: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>?

    @Delete("/keys/{chave}", processes = [MediaType.APPLICATION_XML])
    fun removeChave(@PathVariable chave: String, @Body remocao: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>
}

data class DeletePixKeyRequest(val key: String, val participant: String) {
    companion object {
        fun ChavePix.toRequest(): DeletePixKeyRequest {
            return DeletePixKeyRequest(key = chave, participant = conta.instituicaoIspb)
        }
    }
}

data class DeletePixKeyResponse(val key: String, val participant: String)

data class CreatePixKeyRequest(
        val keyType: KeyType,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
) {

    companion object {
        fun ChavePix.toRequest(): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                    keyType = KeyType.by(tipoChave),
                    key = chave,
                    bankAccount = BankAccount(
                            participant = conta.instituicaoIspb,
                            branch = conta.agencia,
                            accountNumber = conta.numero,
                            accountType = BankAccount.AccountType.by(tipoConta)
                    ),
                    owner = Owner(
                            type = Owner.Type.NATURAL_PERSON,
                            name = conta.nomeTitular,
                            taxIdNumber = conta.cpfTitular
                    )
            )
        }
    }
}

class CreatePixKeyResponse(val keyType: KeyType,
                           val key: String?,
                           val bankAccount: BankAccount,
                           val owner: Owner
)

enum class KeyType(val tipoModelo: TipoChave?) {
    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {

        private val mapping = values().associateBy(KeyType::tipoModelo)

        fun by(tipoModelo: TipoChave): KeyType {
            return mapping[tipoModelo]
                    ?: throw IllegalArgumentException("KeyType inválida ou não encontrada: $tipoModelo")
        }
    }

}

data class BankAccount(val participant: String,
                       val branch: String,
                       val accountNumber: String,
                       val accountType: AccountType) {

    enum class AccountType {
        CACC, SVGS;

        companion object {
            fun by(tipoConta: TipoConta): AccountType {
                return when (tipoConta) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    TipoConta.CONTA_POUPANCA -> SVGS
                    TipoConta.INVALIDA -> throw IllegalArgumentException("AccountType inválida ou não encontrada: $tipoConta")
                }
            }
        }
    }
}


data class Owner(val type: Type, val name: String, val taxIdNumber: String) {
    enum class Type {
        NATURAL_PERSON, LEGAL_PERSON
    }
}


