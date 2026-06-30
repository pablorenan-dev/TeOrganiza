package com.teamteorganiza.estoque.model;

import java.util.UUID;

public class Produto {

    private final String id;
    private String nome;
    private String categoria;
    private UnidadeMedida unidade;
    private double quantidade;
    private double estoqueMinimo;
    private double custoMedio;
    private double precoVenda;

    public Produto(String nome, String categoria, UnidadeMedida unidade, double estoqueMinimo,
                   double precoVenda) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.categoria = categoria;
        this.unidade = unidade;
        this.estoqueMinimo = estoqueMinimo;
        this.precoVenda = precoVenda;
        this.quantidade = 0;
        this.custoMedio = 0;
    }

    public Produto(String id, String nome, String categoria, UnidadeMedida unidade,
                   double quantidade, double estoqueMinimo, double custoMedio, double precoVenda) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
        this.custoMedio = custoMedio;
        this.precoVenda = precoVenda;
    }

    public String getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public UnidadeMedida getUnidade() { return unidade; }
    public void setUnidade(UnidadeMedida unidade) { this.unidade = unidade; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public double getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(double estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public double getCustoMedio() { return custoMedio; }
    public void setCustoMedio(double custoMedio) { this.custoMedio = custoMedio; }

    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }

    public boolean abaixoDoMinimo() { return quantidade <= estoqueMinimo; }

    public double valorEmEstoque() { return quantidade * custoMedio; }

    @Override
    public String toString() {
        return String.format("%s [%s] | qtd: %.2f %s | mín: %.2f | custo médio: R$ %.2f | preço: R$ %.2f",
                nome, categoria, quantidade, unidade, estoqueMinimo, custoMedio, precoVenda);
    }
}
