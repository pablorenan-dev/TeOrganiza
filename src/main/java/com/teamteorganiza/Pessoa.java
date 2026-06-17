package com.teamteorganiza;
import java.time.LocalDate;

public class Pessoa 
{
    private static int idCounter = 0; //Contador dos ids das pessoas, começa em 0 -> 1 -> 2, etc
    private int id;
    private String nome;
    private LocalDate dataDeNascimento; //para criar data depois: LocalDate nascimento = LocalDate.of(2005, 3, 15);  // 15 de março de 2005
    private String cpf;
    private String telefone;
    private String email;
    private boolean ativo;

    public Pessoa( String nome, LocalDate nascimento, String cpf, String telefone, String email, boolean ativo )
    {
        this.id = ++idCounter;
        this.nome = nome;
        this.dataDeNascimento = nascimento;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.ativo = ativo;
        
    }
}
