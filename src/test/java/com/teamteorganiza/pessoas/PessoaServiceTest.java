package com.teamteorganiza.pessoas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PessoaServiceTest {

    private PessoaRepository pessoaRepo;
    private InstrutorDadosRepository instrutorDadosRepo;
    private PessoaService service;

    @Before
    public void setUp() {
        pessoaRepo = new PessoaRepositoryEmMemoria();
        instrutorDadosRepo = new InstrutorDadosRepositoryEmMemoria();
        service = new PessoaService(pessoaRepo, instrutorDadosRepo);
    }

    @Test
    public void cadastrarComCpfNovoDeveSerPermitido() {
        Pessoa pessoa = service.cadastrar("João Silva", LocalDate.of(1990, 1, 1), "123.456.789-00",
                "11999990000", "joao@email.com", Collections.emptyList());

        assertEquals(1, service.listar().size());
        assertEquals("123.456.789-00", pessoa.getCpf());
    }

    @Test
    public void cadastrarComCpfJaCadastradoDeveLancarExcecao() {
        service.cadastrar("João Silva", LocalDate.of(1990, 1, 1), "123.456.789-00",
                "11999990000", "joao@email.com", Collections.emptyList());

        try {
            service.cadastrar("João da Silva", LocalDate.of(1985, 5, 5), "123.456.789-00",
                    "11988887777", "joao2@email.com", Collections.emptyList());
            fail("Deveria ter lançado IllegalArgumentException para CPF duplicado.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("123.456.789-00"));
        }

        List<Pessoa> pessoas = service.listar();
        assertEquals("A pessoa duplicada não deve ter sido cadastrada", 1, pessoas.size());
    }

    @Test
    public void cadastrarComCpfsDiferentesDeveSerPermitido() {
        service.cadastrar("João Silva", LocalDate.of(1990, 1, 1), "111.111.111-11",
                "11999990000", "joao@email.com", Collections.emptyList());
        service.cadastrar("Maria Souza", LocalDate.of(1992, 2, 2), "222.222.222-22",
                "11988887777", "maria@email.com", Collections.emptyList());

        assertEquals(2, service.listar().size());
    }
}
