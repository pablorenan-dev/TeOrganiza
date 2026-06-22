package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Evento {

    private String nome;
    private LocalDate data;
    private double precoFicha;
    private ArrayList<VendaEvento> vendas;

    public Evento(String nome, LocalDate data, double precoFicha) {
        this.nome = nome;
        this.data = data;
        this.precoFicha = precoFicha;
        this.vendas = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public LocalDate getData() { return data; }
    public double getPrecoFicha() { return precoFicha; }
    public List<VendaEvento> getVendas() { return vendas; }

    public void venderFichas(int quantidade, FormaPagamento forma) {
        double total = quantidade * precoFicha;
        vendas.add(new VendaEvento(quantidade, total, forma));
    }

    public double totalArrecadado() {
        double total = 0;
        for (VendaEvento v : vendas) total += v.getValor();
        return total;
    }

    public int totalFichasVendidas() {
        int total = 0;
        for (VendaEvento v : vendas) total += v.getQuantidadeFichas();
        return total;
    }

    public void relatorioVendas() {
        System.out.println("===== Evento: " + nome + " (" + data + ") =====");
        System.out.printf("Preço da ficha: R$ %.2f%n", precoFicha);
        if (vendas.isEmpty()) System.out.println("(nenhuma venda registrada)");
        for (VendaEvento v : vendas) System.out.println(v.detalhar());
        System.out.printf("Total de fichas: %d | Total arrecadado: R$ %.2f%n",
                totalFichasVendidas(), totalArrecadado());
    }
}
