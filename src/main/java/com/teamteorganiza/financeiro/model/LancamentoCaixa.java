package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class LancamentoCaixa extends Lancamento {

    private TipoCaixa caixa;
    private String responsavel;

    public LancamentoCaixa(String descricao, double valor, LocalDate data,
                           TipoLancamento tipo, TipoCaixa caixa, String responsavel) {
        super(descricao, valor, data, tipo);
        this.caixa = caixa;
        this.responsavel = responsavel;
    }

    public TipoCaixa getCaixa() { return caixa; }
    public String getResponsavel() { return responsavel; }

    @Override
    public String detalhar() {
        String sinal = (tipo == TipoLancamento.RECEITA) ? "+" : "-";
        return String.format("Caixa %s #%d | %s | %sR$ %.2f | %s | resp: %s",
                caixa, id, descricao, sinal, valor, data, responsavel);
    }
}
