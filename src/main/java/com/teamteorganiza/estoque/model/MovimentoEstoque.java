package com.teamteorganiza.estoque.model;

import java.time.LocalDate;
import java.util.UUID;

public class MovimentoEstoque {

    private final String id;
    private final String produtoId;
    private final TipoMovimentoEstoque tipo;
    private final double quantidade;
    private final double custoUnitario;
    private final LocalDate data;
    private String observacao;

    public MovimentoEstoque(String produtoId, TipoMovimentoEstoque tipo, double quantidade,
                            double custoUnitario, String observacao) {
        this.id = UUID.randomUUID().toString();
        this.produtoId = produtoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.data = LocalDate.now();
        this.observacao = observacao;
    }

    public MovimentoEstoque(String id, String produtoId, TipoMovimentoEstoque tipo, double quantidade,
                            double custoUnitario, LocalDate data, String observacao) {
        this.id = id;
        this.produtoId = produtoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.data = data;
        this.observacao = observacao;
    }

    public String getId() { return id; }
    public String getProdutoId() { return produtoId; }
    public TipoMovimentoEstoque getTipo() { return tipo; }
    public double getQuantidade() { return quantidade; }
    public double getCustoUnitario() { return custoUnitario; }
    public LocalDate getData() { return data; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    @Override
    public String toString() {
        String sinal = tipo.isBaixa() ? "-" : "+";
        return String.format("produto %s | %s | %s%.2f | %s", produtoId, tipo, sinal, quantidade, data);
    }
}
