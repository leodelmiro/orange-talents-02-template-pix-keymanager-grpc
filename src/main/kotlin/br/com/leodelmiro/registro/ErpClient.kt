package br.com.leodelmiro.registro

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${apis.itau.erp.url}")
interface ErpClient {

    @Get("/{clientId}/contas?tipo={tipoConta}")
    fun consulta(@PathVariable clientId: String, @PathVariable tipoConta: TipoConta): HttpResponse<ContaUsuarioResponse>
}