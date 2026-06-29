package com.teamteorganiza.eventos.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Compromisso {

    private final String id;
    private String titulo;
    private TipoCompromisso tipo;
    private String categoria;
    private LocalDate data;
    private String horario;
    private String local;
    private String responsavel;
    private String descricao;

    private final List<String> participantes = new ArrayList<>();
    private final List<String> caronas = new ArrayList<>();

    public Compromisso(String titulo, TipoCompromisso tipo, String categoria, LocalDate data,
                       String horario, String local, String responsavel, String descricao) {
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo;
        this.tipo = tipo;
        this.categoria = categoria;
        this.data = data;
        this.horario = horario;
        this.local = local;
        this.responsavel = responsavel;
        this.descricao = descricao;
    }

    public Compromisso(String id, String titulo, TipoCompromisso tipo, String categoria, LocalDate data,
                       String horario, String local, String responsavel, String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.categoria = categoria;
        this.data = data;
        this.horario = horario;
        this.local = local;
        this.responsavel = responsavel;
        this.descricao = descricao;
    }

    public String getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public TipoCompromisso getTipo() { return tipo; }
    public void setTipo(TipoCompromisso tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<String> getParticipantes() { return participantes; }
    public List<String> getCaronas() { return caronas; }

    @Override
    public String toString() {
        String cat = (categoria != null && !categoria.isEmpty()) ? " | Categoria: " + categoria : "";
        return String.format("%s (%s)%s - Data: %s | Local: %s | Resp: %s",
                titulo, tipo, cat, data, local, responsavel);
    }
}
