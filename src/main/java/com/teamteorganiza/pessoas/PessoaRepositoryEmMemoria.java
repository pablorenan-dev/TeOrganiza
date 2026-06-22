package com.teamteorganiza.pessoas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PessoaRepositoryEmMemoria implements PessoaRepository {

    private final List<Pessoa> pessoas = new ArrayList<>();

    @Override
    public void salvar(Pessoa pessoa) {
        pessoas.add(pessoa);
    }

    @Override
    public Optional<Pessoa> buscarPorId(Integer id) {
        return pessoas.stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public List<Pessoa> listarTodos() {
        return new ArrayList<>(pessoas);
    }

    @Override
    public void remover(Integer id) {
        pessoas.removeIf(p -> p.getId() == id);
    }
}
