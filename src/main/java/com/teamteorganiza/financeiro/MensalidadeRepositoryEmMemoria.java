package com.teamteorganiza.financeiro;

import com.teamteorganiza.financeiro.model.Mensalidade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MensalidadeRepositoryEmMemoria implements MensalidadeRepository {

    private final List<Mensalidade> mensalidades = new ArrayList<>();

    @Override
    public void salvar(Mensalidade m) {
        mensalidades.add(m);
    }

    @Override
    public Optional<Mensalidade> buscarPorId(Integer id) {
        return mensalidades.stream().filter(m -> m.getId() == id).findFirst();
    }

    @Override
    public List<Mensalidade> listarTodos() {
        return new ArrayList<>(mensalidades);
    }

    @Override
    public void remover(Integer id) {
        mensalidades.removeIf(m -> m.getId() == id);
    }
}
