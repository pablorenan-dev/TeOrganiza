package com.teamteorganiza.estoque;

import com.teamteorganiza.estoque.model.Produto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstoqueRepositoryEmMemoria implements EstoqueRepository {

    private final List<Produto> produtos = new ArrayList<>();

    @Override
    public void salvar(Produto produto) {
        produtos.removeIf(p -> p.getId().equals(produto.getId()));
        produtos.add(produto);
    }

    @Override
    public Optional<Produto> buscarPorId(String id) {
        return produtos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Produto> buscarPorNome(String nome) {
        return produtos.stream().filter(p -> p.getNome().equalsIgnoreCase(nome)).findFirst();
    }

    @Override
    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    @Override
    public void remover(String id) {
        produtos.removeIf(p -> p.getId().equals(id));
    }
}
