package br.com.leodelmiro.compartilhado.validacao

import javax.validation.Constraint
import javax.validation.constraints.Pattern

@MustBeDocumented
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
annotation class ValidUUID(val message: String = "UUID com formato invalido")