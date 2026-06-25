package com.teamteorganiza.pessoas;

public class InstrutorDados {

    private int pessoaId;
    private double salario;
    private String especialidades;

    public InstrutorDados(int pessoaId, double salario, String especialidades) {
        this.pessoaId = pessoaId;
        this.salario = salario;
        this.especialidades = especialidades != null ? especialidades : "";
    }

    public int getPessoaId() { return pessoaId; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public String getEspecialidades() { return especialidades; }
    public void setEspecialidades(String especialidades) { this.especialidades = especialidades != null ? especialidades : ""; }
}
