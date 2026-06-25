package com.teamteorganiza.pessoas;

import java.time.LocalDate;
import java.util.List;

public class PessoaService {

    private final PessoaRepository repositorio;

    public PessoaService(PessoaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void cadastrar(String nome, LocalDate nascimento, String cpf, String telefone, String email) {
        for (Pessoa p : repositorio.listarTodos()) {
            if (p.getCpf().equals(cpf)) {
                throw new IllegalArgumentException("CPF já cadastrado: " + cpf);
            }
        }
        repositorio.salvar(new Pessoa(nome, nascimento, cpf, telefone, email, true));
    }

    public List<Pessoa> listar() {
        return repositorio.listarTodos();
    }

    public void editar(int id, String nome, LocalDate nascimento, String telefone, String email) {
        repositorio.buscarPorId(id).ifPresent(p -> {
            p.setNome(nome);
            p.setDataDeNascimento(nascimento);
            p.setTelefone(telefone);
            p.setEmail(email);
        });
    }

    public void desativar(int id) {
        repositorio.buscarPorId(id).ifPresent(p -> p.setAtivo(!p.isAtivo()));
    }

    public void remover(int id) {
        repositorio.remover(id);
    }
}
