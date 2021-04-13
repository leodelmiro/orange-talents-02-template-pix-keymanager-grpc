package br.com.leodelmiro.compartilhado.apis

import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import br.com.leodelmiro.registro.ContaUsuarioResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${apis.itau.erp.url}")
interface ErpClient {

    @Get("/{clientId}/contas")
    fun consulta(@PathVariable clientId: String, @QueryValue tipo: TipoConta): HttpResponse<ContaUsuarioResponse>
}