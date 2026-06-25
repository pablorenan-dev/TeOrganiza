package com.teamteorganiza.pessoas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pessoa {

    private static int idCounter = 0;
    private int id;
    private String nome;
    private LocalDate dataDeNascimento;
    private String cpf;
    private String telefone;
    private String email;
    private boolean ativo;
    private List<TipoPessoa> tipos = new ArrayList<>();

    public Pessoa(String nome, LocalDate nascimento, String cpf, String telefone, String email, boolean ativo) {
        this.id = ++idCounter;
        this.nome = nome;
        this.dataDeNascimento = nascimento;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.ativo = ativo;
    }

    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataDeNascimento() { return dataDeNascimento; }
    public void setDataDeNascimento(LocalDate dataDeNascimento) { this.dataDeNascimento = dataDeNascimento; }

    public String getCpf() { return cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public List<TipoPessoa> getTipos() { return tipos; }
    public void setTipos(List<TipoPessoa> tipos) { this.tipos = tipos != null ? tipos : new ArrayList<>(); }
}
