package br.com.leodelmiro.compartilhado.chavepix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "tb_chaves")
class ChavePix(
        @field:NotBlank @Column(nullable = false) val idCliente: String,
        @field:NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) val tipoConta: TipoConta,
        @field:NotBlank @Column(unique = true, nullable = false, length = 77) val chave: String,
        @field:NotNull @Enumerated(EnumType.STRING) @Column(nullable = false) val tipoChave: TipoChave,
        @field:NotNull @Embedded val conta: ContaUsuario
) {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(updatable = false, nullable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()
}