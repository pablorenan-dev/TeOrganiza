package com.teamteorganiza.financeiro;

import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.TipoMovimentoEstoque;
import com.teamteorganiza.financeiro.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceiroService {

    private final MensalidadeRepository mensalidadeRepo;
    private final MovimentacaoFinanceiraRepository movimentacaoRepo;
    private final EstoqueService estoqueService;
    private final List<MovimentacaoFinanceira> movimentacoes;
    private final List<Vaquinha> vaquinhas = new ArrayList<>();
    private final CaixaEvento caixa = new CaixaEvento("Evento 1");

    public FinanceiroService(MensalidadeRepository mensalidadeRepo, MovimentacaoFinanceiraRepository movimentacaoRepo,
                             EstoqueService estoqueService) {
        this.mensalidadeRepo = mensalidadeRepo;
        this.movimentacaoRepo = movimentacaoRepo;
        this.estoqueService = estoqueService;
        this.movimentacoes = new ArrayList<>(movimentacaoRepo.listarTodos());
    }

    // ===================== Mensalidades =====================

    public void emitirMensalidade(String pessoaId, String mes, double valor, LocalDate venc) {
        mensalidadeRepo.salvar(new Mensalidade(pessoaId, mes, valor, venc));
    }

    public void pagarMensalidade(String id) {
        mensalidadeRepo.buscarPorId(id).ifPresent(m -> {
            m.pagar();
            mensalidadeRepo.salvar(m);
        });
    }

    public List<Mensalidade> getMensalidades() { return mensalidadeRepo.listarTodos(); }

    // ===================== Entradas / Despesas =====================

    public MovimentacaoFinanceira registrarEntrada(String pessoaId, String descricao, double valor) {
        MovimentacaoFinanceira m = new MovimentacaoFinanceira(pessoaId, descricao, valor, TipoLancamento.RECEITA);
        movimentacoes.add(m);
        movimentacaoRepo.salvar(m);
        return m;
    }

    public MovimentacaoFinanceira registrarDespesa(String pessoaId, String descricao, double valor) {
        MovimentacaoFinanceira m = new MovimentacaoFinanceira(pessoaId, descricao, valor, TipoLancamento.DESPESA);
        movimentacoes.add(m);
        movimentacaoRepo.salvar(m);
        return m;
    }

    public List<MovimentacaoFinanceira> getEntradas() { return filtrar(TipoLancamento.RECEITA); }
    public List<MovimentacaoFinanceira> getDespesas() { return filtrar(TipoLancamento.DESPESA); }

    private List<MovimentacaoFinanceira> filtrar(TipoLancamento tipo) {
        List<MovimentacaoFinanceira> resultado = new ArrayList<>();
        for (MovimentacaoFinanceira m : movimentacoes) {
            if (m.getTipo() == tipo) resultado.add(m);
        }
        return resultado;
    }

    public void editarMovimentacao(String id, String pessoaId, String descricao, double valor) {
        for (MovimentacaoFinanceira m : movimentacoes) {
            if (m.getId().equals(id)) {
                m.setPessoaId(pessoaId);
                m.setDescricao(descricao);
                m.setValor(valor);
                movimentacaoRepo.salvar(m);
                return;
            }
        }
    }

    public void removerMovimentacao(String id) {
        movimentacoes.removeIf(m -> m.getId().equals(id));
        movimentacaoRepo.remover(id);
    }

    public double totalEntradas() { return somar(getEntradas()); }
    public double totalDespesas() { return somar(getDespesas()); }

    private double somar(List<MovimentacaoFinanceira> lista) {
        double total = 0;
        for (MovimentacaoFinanceira m : lista) total += m.getValor();
        return total;
    }

    // ===================== Caixa de evento =====================

    public CaixaEvento getCaixa() { return caixa; }
    public List<VendaCaixa> getVendasCaixa() { return caixa.getVendas(); }
    public double totalCaixa() { return caixa.total(); }
    public String getNomeEvento() { return caixa.getNomeEvento(); }
    public void setNomeEvento(String nome) { caixa.setNomeEvento(nome); }

    public VendaCaixa registrarVenda(String pessoaId, String produtoId, double quantidade, String vendedorNome) {
        Produto produto = estoqueService.buscarProduto(produtoId);
        // Dá baixa primeiro: se faltar estoque, lança e a venda não é registrada.
        estoqueService.darBaixa(produtoId, quantidade, TipoMovimentoEstoque.BAIXA_VENDA,
                "Venda no caixa: " + caixa.getNomeEvento());
        double valor = produto.getPrecoVenda() * quantidade;
        return caixa.registrarVenda(pessoaId, produtoId, quantidade, vendedorNome, produto.getNome(), valor);
    }

    public void editarVenda(String id, String pessoaId, String produtoId, double quantidade, String vendedorNome) {
        VendaCaixa venda = null;
        for (VendaCaixa v : caixa.getVendas()) {
            if (v.getId().equals(id)) { venda = v; break; }
        }
        if (venda == null) return;

        Produto produto = estoqueService.buscarProduto(produtoId);
        // Ajusta o estoque dando baixa antes de estornar, para abortar sem efeito se faltar estoque.
        if (produtoId.equals(venda.getProdutoId())) {
            double delta = quantidade - venda.getQuantidade();
            if (delta > 0) estoqueService.darBaixa(produtoId, delta, TipoMovimentoEstoque.BAIXA_VENDA,
                    "Ajuste de venda no caixa: " + caixa.getNomeEvento());
            else if (delta < 0) estoqueService.reporEstoque(produtoId, -delta);
        } else {
            estoqueService.darBaixa(produtoId, quantidade, TipoMovimentoEstoque.BAIXA_VENDA,
                    "Venda no caixa (edição): " + caixa.getNomeEvento());
            estoqueService.reporEstoque(venda.getProdutoId(), venda.getQuantidade());
        }

        venda.setPessoaId(pessoaId);
        venda.setProdutoId(produtoId);
        venda.setQuantidade(quantidade);
        venda.setVendedorNome(vendedorNome);
        venda.setDescricao(produto.getNome());
        venda.setValor(produto.getPrecoVenda() * quantidade);
    }

    public void removerVenda(String id) {
        for (VendaCaixa v : caixa.getVendas()) {
            if (v.getId().equals(id)) {
                estoqueService.reporEstoque(v.getProdutoId(), v.getQuantidade());
                break;
            }
        }
        caixa.removerVenda(id);
    }

    public void fecharCaixa(String nomeNovoEvento) {
        double total = caixa.total();
        if (total > 0) {
            String desc = "Vendas do evento: " + caixa.getNomeEvento()
                    + " (" + caixa.quantidadeVendas() + " venda(s))";
            MovimentacaoFinanceira m = new MovimentacaoFinanceira("", desc, total, TipoLancamento.RECEITA);
            movimentacoes.add(m);
            movimentacaoRepo.salvar(m);
        }
        caixa.novoEvento(nomeNovoEvento);
    }

    // ===================== Vaquinhas =====================

    public List<Vaquinha> getVaquinhas() { return new ArrayList<>(vaquinhas); }

    public Vaquinha criarVaquinha(String titulo, String objetivo, double meta) {
        Vaquinha v = new Vaquinha(titulo, objetivo, meta);
        vaquinhas.add(v);
        return v;
    }

    public ContribuicaoVaquinha contribuir(Vaquinha v, String pessoaId, double valor, String descricao) {
        return v.contribuir(pessoaId, valor, descricao);
    }

    public void editarContribuicao(Vaquinha v, String id, String pessoaId, String descricao, double valor) {
        for (ContribuicaoVaquinha c : v.getContribuicoes()) {
            if (c.getId().equals(id)) {
                c.setPessoaId(pessoaId);
                c.setDescricao(descricao);
                c.setValor(valor);
                return;
            }
        }
    }

    public void removerContribuicao(Vaquinha v, String id) { v.removerContribuicao(id); }

    public List<Doador> top3Doadores() {
        Map<String, Double> totais = new HashMap<>();
        for (Vaquinha v : vaquinhas) {
            for (ContribuicaoVaquinha c : v.getContribuicoes()) {
                totais.merge(c.getPessoaId(), c.getValor(), Double::sum);
            }
        }
        return totais.entrySet().stream()
                .map(e -> new Doador(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(Doador::total).reversed())
                .limit(3)
                .toList();
    }

    // ===================== Extrato (consolidado) =====================

    public List<Lancamento> todosLancamentos() {
        List<Lancamento> todos = new ArrayList<>();
        todos.addAll(mensalidadeRepo.listarTodos());
        todos.addAll(movimentacoes);
        for (Vaquinha v : vaquinhas) todos.addAll(v.getContribuicoes());
        todos.addAll(caixa.getVendas());
        return todos;
    }
}
