package com.teamteorganiza.financeiro;

import com.teamteorganiza.financeiro.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceiroService {

    private final MensalidadeRepository mensalidadeRepo;
    private final CaixaRepository caixaRepo;
    private final List<Vaquinha> vaquinhas = new ArrayList<>();
    private final List<Evento> bailes = new ArrayList<>();

    public FinanceiroService(MensalidadeRepository mensalidadeRepo, CaixaRepository caixaRepo) {
        this.mensalidadeRepo = mensalidadeRepo;
        this.caixaRepo = caixaRepo;
    }

    public void emitirMensalidade(int pessoaId, String mes, double valor, LocalDate venc) {
        mensalidadeRepo.salvar(new Mensalidade(pessoaId, mes, valor, venc));
    }

    public void pagarMensalidade(int id) {
        mensalidadeRepo.buscarPorId(id).ifPresentOrElse(
            Mensalidade::pagar,
            () -> System.out.println("Mensalidade #" + id + " não encontrada.")
        );
    }

    public Caixa getCaixa(TipoCaixa tipo) {
        return caixaRepo.buscarPorId(tipo).orElse(null);
    }

    public List<Caixa> getCaixas() {
        return caixaRepo.listarTodos();
    }

    public void registrarEntrada(TipoCaixa tipo, String descricao, double valor, String responsavel) {
        Caixa c = getCaixa(tipo);
        if (c != null) c.registrarEntrada(descricao, valor, responsavel);
    }

    public void registrarSaida(TipoCaixa tipo, String descricao, double valor, String responsavel) {
        Caixa c = getCaixa(tipo);
        if (c != null) c.registrarSaida(descricao, valor, responsavel);
    }

    public Vaquinha criarVaquinha(String titulo, String objetivo, double meta) {
        Vaquinha v = new Vaquinha(titulo, objetivo, meta);
        vaquinhas.add(v);
        return v;
    }

    public Evento criarBaile(String nome, LocalDate data, double precoFicha) {
        Evento b = new Evento(nome, data, precoFicha);
        bailes.add(b);
        return b;
    }

    public List<Mensalidade> getMensalidades() { return mensalidadeRepo.listarTodos(); }
    public List<Vaquinha> getVaquinhas() { return new ArrayList<>(vaquinhas); }
    public List<Evento> getBailes() { return new ArrayList<>(bailes); }

    public List<Lancamento> todosLancamentos() {
        List<Lancamento> todos = new ArrayList<>();
        todos.addAll(mensalidadeRepo.listarTodos());
        for (Caixa c : caixaRepo.listarTodos()) todos.addAll(c.getMovimentos());
        for (Vaquinha v : vaquinhas) todos.addAll(v.getContribuicoes());
        for (Evento b : bailes) todos.addAll(b.getVendas());
        return todos;
    }
}
