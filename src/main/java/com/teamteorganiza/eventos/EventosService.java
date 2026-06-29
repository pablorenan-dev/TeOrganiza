package com.teamteorganiza.eventos;

import com.teamteorganiza.eventos.model.Compromisso;
import com.teamteorganiza.eventos.model.TipoCompromisso;

import java.time.LocalDate;
import java.util.List;

public class EventosService {

    private final CompromissoRepository repositorio;

    public EventosService(CompromissoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Compromisso cadastrar(String titulo, TipoCompromisso tipo, String categoria, LocalDate data,
                                 String horario, String local, String responsavel, String descricao) {
        validar(titulo, tipo, data);
        Compromisso c = new Compromisso(titulo, tipo, categoria, data, horario, local, responsavel, descricao);
        repositorio.salvar(c);
        return c;
    }

    public List<Compromisso> listar() {
        return repositorio.listarTodos();
    }

    public void editar(String id, String titulo, TipoCompromisso tipo, String categoria, LocalDate data,
                       String horario, String local, String responsavel, String descricao) {
        validar(titulo, tipo, data);
        repositorio.buscarPorId(id).ifPresent(c -> {
            c.setTitulo(titulo);
            c.setTipo(tipo);
            c.setCategoria(categoria);
            c.setData(data);
            c.setHorario(horario);
            c.setLocal(local);
            c.setResponsavel(responsavel);
            c.setDescricao(descricao);
            repositorio.salvar(c);
        });
    }

    public void remover(String id) {
        repositorio.remover(id);
    }

    private void validar(String titulo, TipoCompromisso tipo, LocalDate data) {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título é obrigatório.");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo é obrigatório.");
        if (data == null)
            throw new IllegalArgumentException("Data é obrigatória.");
    }
}
