package com.teamteorganiza.pessoas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PessoaService {

    private final PessoaRepository repositorio;
    private final InstrutorDadosRepository instrutorDadosRepo;

    public PessoaService(PessoaRepository repositorio, InstrutorDadosRepository instrutorDadosRepo) {
        this.repositorio = repositorio;
        this.instrutorDadosRepo = instrutorDadosRepo;
    }

    public Pessoa cadastrar(String nome, LocalDate nascimento, String cpf, String telefone, String email,
                            List<TipoPessoa> tipos) {
        for (Pessoa p : repositorio.listarTodos()) {
            if (p.getCpf().equals(cpf)) {
                throw new IllegalArgumentException("CPF já cadastrado: " + cpf);
            }
        }
        Pessoa nova = new Pessoa(nome, nascimento, cpf, telefone, email, true);
        nova.setTipos(new ArrayList<>(tipos));
        repositorio.salvar(nova);
        return nova;
    }

    public List<Pessoa> listar() {
        return repositorio.listarTodos();
    }

    public void editar(int id, String nome, LocalDate nascimento, String telefone, String email,
                       List<TipoPessoa> tipos) {
        repositorio.buscarPorId(id).ifPresent(p -> {
            p.setNome(nome);
            p.setDataDeNascimento(nascimento);
            p.setTelefone(telefone);
            p.setEmail(email);
            p.setTipos(new ArrayList<>(tipos));
            boolean continuaInstrutor = tipos.stream()
                .anyMatch(t -> t.getNome().equalsIgnoreCase("instrutor"));
            if (!continuaInstrutor) {
                instrutorDadosRepo.remover(id);
            }
        });
    }

    public void desativar(int id) {
        repositorio.buscarPorId(id).ifPresent(p -> p.setAtivo(!p.isAtivo()));
    }

    public void remover(int id) {
        instrutorDadosRepo.remover(id);
        repositorio.remover(id);
    }

    public void salvarDadosInstrutor(int pessoaId, double salario, String especialidades) {
        instrutorDadosRepo.salvarOuAtualizar(new InstrutorDados(pessoaId, salario, especialidades));
    }

    public Optional<InstrutorDados> buscarDadosInstrutor(int pessoaId) {
        return instrutorDadosRepo.buscarPorPessoaId(pessoaId);
    }
}
