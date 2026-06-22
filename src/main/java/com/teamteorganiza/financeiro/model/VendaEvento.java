package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class VendaEvento extends Lancamento {

    private int quantidadeFichas;
    private FormaPagamento forma;

    public VendaEvento(int quantidadeFichas, double valor, FormaPagamento forma) {
        super(quantidadeFichas + " ficha(s) - " + forma, valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.quantidadeFichas = quantidadeFichas;
        this.forma = forma;
    }

    public int getQuantidadeFichas() { return quantidadeFichas; }
    public FormaPagamento getForma() { return forma; }

    @Override
    public String detalhar() {
        return String.format("Venda #%d | %d ficha(s) | R$ %.2f | %s | %s",
                id, quantidadeFichas, valor, forma, data);
    }
}
