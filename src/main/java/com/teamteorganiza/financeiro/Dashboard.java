package com.teamteorganiza.financeiro;

import com.teamteorganiza.financeiro.model.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class Dashboard {

    private final FinanceiroService service;

    public Dashboard(FinanceiroService service) {
        this.service = service;
    }

    public void resumoMensal(String mesReferencia) {
        double receitas = 0, despesas = 0;
        for (Lancamento l : service.todosLancamentos()) {
            if (!l.mesDoLancamento().equals(mesReferencia)) continue;
            if (l.getTipo() == TipoLancamento.RECEITA) receitas += l.getValor();
            else despesas += l.getValor();
        }
        System.out.println("===== Resumo mensal: " + mesReferencia + " =====");
        System.out.printf("Receitas: R$ %.2f%n", receitas);
        System.out.printf("Despesas: R$ %.2f%n", despesas);
        System.out.printf("Saldo:    R$ %.2f%n", receitas - despesas);
    }

    public void gastosPorCategoria() {
        Map<String, Double> receitasPorCat = new LinkedHashMap<>();
        Map<String, Double> despesasPorCat = new LinkedHashMap<>();
        for (Lancamento l : service.todosLancamentos()) {
            String categoria = categoriaDe(l);
            Map<String, Double> alvo = (l.getTipo() == TipoLancamento.RECEITA) ? receitasPorCat : despesasPorCat;
            alvo.merge(categoria, l.getValor(), Double::sum);
        }
        System.out.println("===== Movimentação por categoria =====");
        System.out.println("-- Receitas --");
        imprimirMapa(receitasPorCat);
        System.out.println("-- Despesas --");
        imprimirMapa(despesasPorCat);
    }

    public void prestacaoDeContas() {
        System.out.println("======================================");
        System.out.println("        PRESTAÇÃO DE CONTAS");
        System.out.println("======================================");
        double receitas = 0, despesas = 0;
        System.out.println("-- Lançamentos --");
        for (Lancamento l : service.todosLancamentos()) {
            System.out.println(l.detalhar());
            if (l.getTipo() == TipoLancamento.RECEITA) receitas += l.getValor();
            else despesas += l.getValor();
        }
        System.out.println("--------------------------------------");
        System.out.printf("Total de entradas: R$ %.2f%n", receitas);
        System.out.printf("Total de saídas:   R$ %.2f%n", despesas);
        System.out.printf("RESULTADO:         R$ %.2f%n", receitas - despesas);
        System.out.println("======================================");
    }

    private String categoriaDe(Lancamento l) {
        if (l instanceof Mensalidade) return "Mensalidades";
        else if (l instanceof LancamentoCaixa lc) return "Caixa " + lc.getCaixa();
        else if (l instanceof ContribuicaoVaquinha) return "Vaquinha";
        else if (l instanceof VendaEvento) return "Baile";
        return "Outros";
    }

    private void imprimirMapa(Map<String, Double> mapa) {
        if (mapa.isEmpty()) { System.out.println("(nada)"); return; }
        for (Map.Entry<String, Double> e : mapa.entrySet()) {
            System.out.printf("  %-18s R$ %.2f%n", e.getKey(), e.getValue());
        }
    }
}
