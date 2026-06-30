package com.teamteorganiza.financeiro.model;

import java.util.ArrayList;
import java.util.List;

public class CaixaEvento {

    private String nomeEvento;
    private final List<VendaCaixa> vendas = new ArrayList<>();

    public CaixaEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
    }

    public String getNomeEvento() { return nomeEvento; }
    public void setNomeEvento(String nomeEvento) { this.nomeEvento = nomeEvento; }

    public List<VendaCaixa> getVendas() { return vendas; }

    public VendaCaixa registrarVenda(String pessoaId, String produtoId, double quantidade,
                                     String vendedorNome, String descricao, double valor) {
        VendaCaixa venda = new VendaCaixa(pessoaId, produtoId, quantidade, vendedorNome, descricao, valor);
        vendas.add(venda);
        return venda;
    }

    public void removerVenda(String id) {
        vendas.removeIf(v -> v.getId().equals(id));
    }

    public double total() {
        double total = 0;
        for (VendaCaixa v : vendas) total += v.getValor();
        return total;
    }

    public int quantidadeVendas() { return vendas.size(); }

    public void novoEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
        vendas.clear();
    }
}
