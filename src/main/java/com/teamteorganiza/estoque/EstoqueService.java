package com.teamteorganiza.estoque;

import com.teamteorganiza.estoque.model.MovimentoEstoque;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.TipoMovimentoEstoque;
import com.teamteorganiza.estoque.model.UnidadeMedida;

import java.util.ArrayList;
import java.util.List;

public class EstoqueService {

    private final EstoqueRepository repositorio;
    private final List<MovimentoEstoque> movimentos = new ArrayList<>();

    public EstoqueService(EstoqueRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Produto cadastrarProduto(String nome, String categoria, UnidadeMedida unidade,
                                    double estoqueMinimo, double precoVenda) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        if (estoqueMinimo < 0)
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
        if (precoVenda < 0)
            throw new IllegalArgumentException("Preço de venda não pode ser negativo.");
        repositorio.buscarPorNome(nome).ifPresent(p -> {
            throw new IllegalArgumentException("Já existe um produto com o nome: " + nome);
        });
        Produto produto = new Produto(nome, categoria, unidade, estoqueMinimo, precoVenda);
        repositorio.salvar(produto);
        return produto;
    }

    public List<Produto> listar() {
        return repositorio.listarTodos();
    }

    public void editarProduto(String id, String nome, String categoria, UnidadeMedida unidade,
                              double estoqueMinimo, double precoVenda) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        if (estoqueMinimo < 0)
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
        if (precoVenda < 0)
            throw new IllegalArgumentException("Preço de venda não pode ser negativo.");
        repositorio.buscarPorNome(nome)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new IllegalArgumentException("Já existe outro produto com o nome: " + nome);
                });
        Produto produto = buscarProduto(id);
        produto.setNome(nome);
        produto.setCategoria(categoria);
        produto.setUnidade(unidade);
        produto.setEstoqueMinimo(estoqueMinimo);
        produto.setPrecoVenda(precoVenda);
        repositorio.salvar(produto);
    }

    public void removerProduto(String id) {
        repositorio.remover(id);
    }

    public Produto buscarProduto(String produtoId) {
        return repositorio.buscarPorId(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));
    }

    public MovimentoEstoque registrarEntrada(String produtoId, double quantidade, double custoUnitario) {
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade da entrada deve ser maior que zero.");
        if (custoUnitario < 0)
            throw new IllegalArgumentException("Custo unitário não pode ser negativo.");
        Produto produto = buscarProduto(produtoId);
        atualizarCustoMedio(produto, quantidade, custoUnitario);
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        repositorio.salvar(produto);
        MovimentoEstoque mov = new MovimentoEstoque(produtoId, TipoMovimentoEstoque.ENTRADA,
                quantidade, custoUnitario, "Entrada de mercadoria");
        movimentos.add(mov);
        return mov;
    }

    private void atualizarCustoMedio(Produto produto, double qtdEntrada, double custoUnitario) {
        double qtdAtual = produto.getQuantidade();
        double valorAtual = qtdAtual * produto.getCustoMedio();
        double valorEntrada = qtdEntrada * custoUnitario;
        double novaQtd = qtdAtual + qtdEntrada;
        produto.setCustoMedio(novaQtd > 0 ? (valorAtual + valorEntrada) / novaQtd : 0);
    }

    public MovimentoEstoque darBaixa(String produtoId, double quantidade,
                                     TipoMovimentoEstoque tipo, String observacao) {
        if (tipo == null || !tipo.isBaixa())
            throw new IllegalArgumentException("Tipo de baixa inválido. Use BAIXA_CONSUMO ou BAIXA_VENDA.");
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade da baixa deve ser maior que zero.");
        Produto produto = buscarProduto(produtoId);
        if (produto.getQuantidade() < quantidade)
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente de '%s': disponível %.2f, solicitado %.2f.",
                    produto.getNome(), produto.getQuantidade(), quantidade));
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        repositorio.salvar(produto);
        MovimentoEstoque mov = new MovimentoEstoque(produtoId, tipo, quantidade, 0, observacao);
        movimentos.add(mov);
        return mov;
    }

    /**
     * Devolve quantidade ao estoque (estorno de uma venda do caixa que foi
     * editada ou removida). Não recalcula o custo médio, pois não é uma compra.
     */
    public MovimentoEstoque reporEstoque(String produtoId, double quantidade) {
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade do estorno deve ser maior que zero.");
        Produto produto = buscarProduto(produtoId);
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        repositorio.salvar(produto);
        MovimentoEstoque mov = new MovimentoEstoque(produtoId, TipoMovimentoEstoque.ENTRADA,
                quantidade, produto.getCustoMedio(), "Estorno de venda do caixa");
        movimentos.add(mov);
        return mov;
    }

    public List<Produto> listarAbaixoDoEstoqueMinimo() {
        return repositorio.listarTodos().stream()
                .filter(Produto::abaixoDoMinimo)
                .toList();
    }

    public List<MovimentoEstoque> getMovimentos() {
        return new ArrayList<>(movimentos);
    }

    public List<MovimentoEstoque> getMovimentosPorProduto(String produtoId) {
        return movimentos.stream()
                .filter(m -> m.getProdutoId().equals(produtoId))
                .toList();
    }

    public double valorTotalEmEstoque() {
        return repositorio.listarTodos().stream().mapToDouble(Produto::valorEmEstoque).sum();
    }
}
