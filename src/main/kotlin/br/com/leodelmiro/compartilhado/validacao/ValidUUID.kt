package br.com.leodelmiro.compartilhado.validacao

import javax.validation.constraints.Pattern

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
annotation class ValidUUID(val message: String = "UUID com formato invalido")