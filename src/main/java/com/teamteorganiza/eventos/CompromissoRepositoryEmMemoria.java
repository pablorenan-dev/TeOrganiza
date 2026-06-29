package com.teamteorganiza.eventos;

import com.teamteorganiza.eventos.model.Compromisso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompromissoRepositoryEmMemoria implements CompromissoRepository {

    private final List<Compromisso> compromissos = new ArrayList<>();

    @Override
    public void salvar(Compromisso compromisso) {
        compromissos.removeIf(c -> c.getId().equals(compromisso.getId()));
        compromissos.add(compromisso);
    }

    @Override
    public Optional<Compromisso> buscarPorId(String id) {
        return compromissos.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    @Override
    public List<Compromisso> listarTodos() {
        return new ArrayList<>(compromissos);
    }

    @Override
    public void remover(String id) {
        compromissos.removeIf(c -> c.getId().equals(id));
    }
}
