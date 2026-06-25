package com.teamteorganiza.pessoas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipoPessoaRepositoryEmMemoria implements TipoPessoaRepository {

    private final List<TipoPessoa> tipos = new ArrayList<>();

    @Override
    public void salvar(TipoPessoa tipo) { tipos.add(tipo); }

    @Override
    public Optional<TipoPessoa> buscarPorId(Integer id) {
        return tipos.stream().filter(t -> t.getId() == id).findFirst();
    }

    @Override
    public List<TipoPessoa> listarTodos() { return new ArrayList<>(tipos); }

    @Override
    public void remover(Integer id) { tipos.removeIf(t -> t.getId() == id); }

    @Override
    public Optional<TipoPessoa> buscarPorNome(String nome) {
        return tipos.stream()
            .filter(t -> t.getNome().equalsIgnoreCase(nome))
            .findFirst();
    }
}
