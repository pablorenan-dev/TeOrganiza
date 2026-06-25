package com.teamteorganiza.pessoas;

public class TipoPessoa {

    private static int idCounter = 0;
    private int id;
    private String nome;
    private String descricao;
    private boolean ativo;

    public TipoPessoa(String nome, String descricao) {
        this.id = ++idCounter;
        this.nome = nome;
        this.descricao = descricao != null ? descricao : "";
        this.ativo = true;
    }

    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao != null ? descricao : ""; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() { return nome; }
}
