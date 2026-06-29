package com.teamteorganiza;

import java.time.LocalDate;
import java.util.ArrayList;

public class Compromissos {

    private static int contadorId = 0;
    // Lista estática que a interface Swing vai usar para salvar os dados em memória
    private static ArrayList<Compromissos> listaCompromissos = new ArrayList<>();

    private int id;
    private String titulo;
    private TipoCompromisso tipo;
    private String invernada;
    private LocalDate data;
    private String horario;
    private String local;
    private String responsavel;
    private String descricao;

    private ArrayList<String> participantes;
    private ArrayList<String> caronas;

    public Compromissos(String titulo,
                        TipoCompromisso tipo,
                        String invernada,
                        LocalDate data,
                        String horario,
                        String local,
                        String responsavel,
                        String descricao) {

        this.id = ++contadorId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.invernada = invernada;
        this.data = data;
        this.horario = horario;
        this.local = local;
        this.responsavel = responsavel;
        this.descricao = descricao;

        this.participantes = new ArrayList<>();
        this.caronas = new ArrayList<>();
    }

    // Métodos estáticos para a interface gráfica funcionar
    public static void adicionarCompromisso(Compromissos c) {
        listaCompromissos.add(c);
    }

    public static ArrayList<Compromissos> getListaCompromissos() {
        return listaCompromissos;
    }

    // Método toString para exibir as informações bonitas na JTextArea
    @Override
    public String toString() {

        String categoria = "";

        if (invernada != null && !invernada.isEmpty()) {
                categoria = " | Categoria: " + invernada;
    }

    return String.format(
            "[%d] %s (%s)%s - Data: %s | Local: %s | Resp: %s",
            id,
            titulo,
            tipo,
            categoria,
            data,
            local,
            responsavel
    );
}
}
//ruan