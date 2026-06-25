package com.teamteorganiza.pessoas;

import java.util.List;
import java.util.stream.Collectors;

public class TipoPessoaService {

    private final TipoPessoaRepository repositorio;

    public TipoPessoaService(TipoPessoaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void criar(String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do tipo é obrigatório.");
        }
        if (repositorio.buscarPorNome(nome).isPresent()) {
            throw new IllegalArgumentException("Já existe um tipo com o nome \"" + nome + "\".");
        }
        repositorio.salvar(new TipoPessoa(nome, descricao));
    }

    public List<TipoPessoa> listar() {
        return repositorio.listarTodos();
    }

    public List<TipoPessoa> listarAtivos() {
        return repositorio.listarTodos().stream()
            .filter(TipoPessoa::isAtivo)
            .collect(Collectors.toList());
    }

    public void editar(int id, String nome, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do tipo é obrigatório.");
        }
        repositorio.buscarPorNome(nome).ifPresent(existente -> {
            if (existente.getId() != id) {
                throw new IllegalArgumentException("Já existe um tipo com o nome \"" + nome + "\".");
            }
        });
        repositorio.buscarPorId(id).ifPresent(t -> {
            t.setNome(nome);
            t.setDescricao(descricao);
        });
    }

    public void desativar(int id) {
        repositorio.buscarPorId(id).ifPresent(t -> t.setAtivo(!t.isAtivo()));
    }

    public void remover(int id, List<Pessoa> todasPessoas) {
        boolean emUso = todasPessoas.stream()
            .anyMatch(p -> p.getTipos().stream().anyMatch(t -> t.getId() == id));
        if (emUso) {
            throw new IllegalStateException(
                "Este tipo está associado a uma ou mais pessoas. Desative-o em vez de excluir.");
        }
        repositorio.remover(id);
    }
}
