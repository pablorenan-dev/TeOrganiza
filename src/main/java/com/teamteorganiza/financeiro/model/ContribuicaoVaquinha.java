package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class ContribuicaoVaquinha extends Lancamento {

    private int pessoaId;
    private boolean anonima;

    public ContribuicaoVaquinha(int pessoaId, double valor, boolean anonima) {
        super(anonima ? "Doação anônima" : "Doação pessoa " + pessoaId,
              valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
        this.anonima = anonima;
    }

    public int getPessoaId() { return pessoaId; }
    public boolean isAnonima() { return anonima; }

    @Override
    public String detalhar() {
        String quem = anonima ? "anônimo" : ("pessoa " + pessoaId);
        return String.format("Contribuição #%d | %s | R$ %.2f | %s", id, quem, valor, data);
    }
}
