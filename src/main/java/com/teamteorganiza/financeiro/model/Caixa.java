package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Caixa {

    private TipoCaixa tipo;
    private ArrayList<LancamentoCaixa> movimentos;

    public Caixa(TipoCaixa tipo) {
        this.tipo = tipo;
        this.movimentos = new ArrayList<>();
    }

    public TipoCaixa getTipo() { return tipo; }
    public List<LancamentoCaixa> getMovimentos() { return movimentos; }

    public void registrarEntrada(String descricao, double valor, String responsavel) {
        movimentos.add(new LancamentoCaixa(descricao, valor, LocalDate.now(),
                TipoLancamento.RECEITA, tipo, responsavel));
    }

    public void registrarSaida(String descricao, double valor, String responsavel) {
        movimentos.add(new LancamentoCaixa(descricao, valor, LocalDate.now(),
                TipoLancamento.DESPESA, tipo, responsavel));
    }

    public double saldoAtual() {
        double saldo = 0;
        for (LancamentoCaixa m : movimentos) {
            if (m.getTipo() == TipoLancamento.RECEITA) saldo += m.getValor();
            else saldo -= m.getValor();
        }
        return saldo;
    }

    public void extrato() {
        System.out.println("===== Extrato do caixa " + tipo + " =====");
        if (movimentos.isEmpty()) System.out.println("(sem movimentações)");
        for (LancamentoCaixa m : movimentos) System.out.println(m.detalhar());
        System.out.printf("Saldo atual: R$ %.2f%n", saldoAtual());
    }
}
