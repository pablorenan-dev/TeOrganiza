package com.teamteorganiza.financeiro.model;

import java.time.LocalDate;

public class VendaCaixa extends Lancamento {

    private String pessoaId;
    private String produtoId;
    private double quantidade;
    private String vendedorNome;

    public VendaCaixa(String pessoaId, String produtoId, double quantidade, String vendedorNome,
                      String descricao, double valor) {
        super(descricao, valor, LocalDate.now(), TipoLancamento.RECEITA);
        this.pessoaId = pessoaId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.vendedorNome = vendedorNome;
    }

    public String getPessoaId() { return pessoaId; }
    public void setPessoaId(String pessoaId) { this.pessoaId = pessoaId; }

    public String getProdutoId() { return produtoId; }
    public void setProdutoId(String produtoId) { this.produtoId = produtoId; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public String getVendedorNome() { return vendedorNome; }
    public void setVendedorNome(String vendedorNome) { this.vendedorNome = vendedorNome; }

    @Override
    public String detalhar() {
        return String.format("Venda #%s | pessoa %s | %s x%.2f | vendedor %s | R$ %.2f | %s",
                id, pessoaId, descricao, quantidade, vendedorNome, valor, data);
    }
}
