package br.com.leodelmiro.consulta

import br.com.leodelmiro.compartilhado.chavepix.ChavePix
import br.com.leodelmiro.compartilhado.chavepix.ContaUsuario
import br.com.leodelmiro.compartilhado.chavepix.TipoChave
import br.com.leodelmiro.compartilhado.chavepix.TipoConta
import java.time.LocalDateTime

class DetalhesChavePix(val idPix: String? = "",
                       val idCliente: String? = "",
                       val tipoChave: TipoChave?,
                       val chavePix: String,
                       val tipoConta: TipoConta,
                       val conta: ContaUsuario,
                       val criadoEm: LocalDateTime) {

    constructor(entidade: ChavePix) : this(
            idPix = entidade.id.toString(),
            idCliente = entidade.idCliente.toString(),
            tipoChave = entidade.tipoChave,
            chavePix = entidade.chave,
            tipoConta = entidade.tipoConta,
            conta = entidade.conta,
            criadoEm = entidade.criadoEm,
    )

}

