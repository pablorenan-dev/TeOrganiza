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
}
