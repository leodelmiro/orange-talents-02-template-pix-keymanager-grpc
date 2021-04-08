package br.com.leodelmiro.registro

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class ContaUsuario(
        @field:NotBlank @Column(name = "conta_instituicao_nome", nullable = false) val instituicaoNome: String,
        @field:NotBlank @Column(name = "conta_instituicao_ispb", nullable = false) val instituicaoIspb: String,
        @field:NotBlank @Column(name = "conta_titular_nome", nullable = false) val nomeTitular: String,
        @field:NotBlank @Column(name = "conta_titular_cpf", nullable = false) val cpfTitular: String,
        @field:NotBlank @Column(name = "conta_agencia", nullable = false) val agencia: String,
        @field:NotBlank @Column(name = "conta_numero", nullable = false) val numero: String,

)