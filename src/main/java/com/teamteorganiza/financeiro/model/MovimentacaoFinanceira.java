package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class MovimentacaoFinanceira extends Lancamento {

    private String pessoaId;

    public MovimentacaoFinanceira(String pessoaId, String descricao, double valor, TipoLancamento tipo) {
        super(descricao, valor, LocalDate.now(), tipo);
        this.pessoaId = pessoaId;
    }

    public MovimentacaoFinanceira(String id, String pessoaId, String descricao, double valor, java.time.LocalDate data, TipoLancamento tipo) {
        super(id, descricao, valor, data, tipo);
        this.pessoaId = pessoaId;
    }

    public String getPessoaId() { return pessoaId; }
    public void setPessoaId(String pessoaId) { this.pessoaId = pessoaId; }

    @Override
    public String detalhar() {
        String sinal = (tipo == TipoLancamento.RECEITA) ? "+" : "-";
        return String.format("#%s | pessoa %s | %s | %sR$ %.2f | %s",
                id, pessoaId, descricao, sinal, valor, data);
    }
}
