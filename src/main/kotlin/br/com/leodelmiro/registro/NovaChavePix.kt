package br.com.leodelmiro.registro

import br.com.leodelmiro.compartilhado.chavepix.ChavePix
import br.com.leodelmiro.compartilhado.chavepix.ContaUsuario
import br.com.leodelmiro.compartilhado.chavepix.TipoChave
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import io.micronaut.core.annotation.Introspected
import org.hibernate.validator.constraints.Length
import java.util.*
import javax.persistence.Embedded
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
class NovaChavePix(
        @field:NotBlank val idCliente: String,
        @field:NotNull val tipoConta: TipoConta,
        @field:Length(max = 77) var chave: String,
        @field:NotNull val tipoChave: TipoChave,
        @field:NotNull @Embedded val conta: ContaUsuario
) {
    init {
        if (tipoChave == TipoChave.ALEATORIA) chave = UUID.randomUUID().toString()
    }

    fun toModel(): ChavePix {
        return ChavePix(UUID.fromString(idCliente), tipoConta, chave, tipoChave, conta)
    }
}