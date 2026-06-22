package com.teamteorganiza.financeiro.model;

import java.util.ArrayList;
import java.util.List;

public class Vaquinha {

    private String titulo;
    private String objetivo;
    private double meta;
    private ArrayList<ContribuicaoVaquinha> contribuicoes;

    public Vaquinha(String titulo, String objetivo, double meta) {
        this.titulo = titulo;
        this.objetivo = objetivo;
        this.meta = meta;
        this.contribuicoes = new ArrayList<>();
    }

    public String getTitulo() { return titulo; }
    public String getObjetivo() { return objetivo; }
    public double getMeta() { return meta; }
    public List<ContribuicaoVaquinha> getContribuicoes() { return contribuicoes; }

    public void contribuir(int pessoaId, double valor, boolean anonima) {
        contribuicoes.add(new ContribuicaoVaquinha(pessoaId, valor, anonima));
    }

    public double totalArrecadado() {
        double total = 0;
        for (ContribuicaoVaquinha c : contribuicoes) total += c.getValor();
        return total;
    }

    public double quantoFalta() {
        return Math.max(meta - totalArrecadado(), 0);
    }

    public boolean metaAtingida() {
        return totalArrecadado() >= meta;
    }

    public void listarContribuicoes() {
        System.out.println("===== Vaquinha: " + titulo + " =====");
        System.out.println("Objetivo: " + objetivo);
        if (contribuicoes.isEmpty()) System.out.println("(nenhuma contribuição ainda)");
        for (ContribuicaoVaquinha c : contribuicoes) System.out.println(c.detalhar());
        System.out.printf("Arrecadado: R$ %.2f de R$ %.2f | Falta: R$ %.2f | Meta atingida: %s%n",
                totalArrecadado(), meta, quantoFalta(), metaAtingida() ? "SIM" : "não");
    }
}
