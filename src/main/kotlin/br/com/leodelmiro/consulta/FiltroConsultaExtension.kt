package br.com.leodelmiro.consulta

import br.com.leodelmiro.ConsultaChaveRequest
import br.com.leodelmiro.ConsultaChaveRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChaveRequest.filtro(validator: Validator): ConsultaChaveFiltro {
    val filtro = when (filtroCase) {
        PIXECLIENTEID -> {
            pixEClienteId.idPix.let {
                ConsultaChaveFiltro.PorPixEClientId(
                        idCliente = pixEClienteId.idCliente,
                        idPix = pixEClienteId.idPix
                )
            }
        }
        CHAVEPIX -> ConsultaChaveFiltro.PorChave(chavePix)
        FILTRO_NOT_SET -> ConsultaChaveFiltro.Invalido()
    }

    val possiveisErros = validator.validate(filtro)
    if (possiveisErros.isNotEmpty()) {
        throw ConstraintViolationException(possiveisErros)
    }

    return filtro
}