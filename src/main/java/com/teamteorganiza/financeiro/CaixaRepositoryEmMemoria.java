package com.teamteorganiza.financeiro;

import com.teamteorganiza.financeiro.model.Caixa;
import com.teamteorganiza.financeiro.model.TipoCaixa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CaixaRepositoryEmMemoria implements CaixaRepository {

    private final List<Caixa> caixas = new ArrayList<>();

    public CaixaRepositoryEmMemoria() {
        for (TipoCaixa t : TipoCaixa.values()) {
            caixas.add(new Caixa(t));
        }
    }

    @Override
    public void salvar(Caixa caixa) {
        caixas.add(caixa);
    }

    @Override
    public Optional<Caixa> buscarPorId(TipoCaixa tipo) {
        return caixas.stream().filter(c -> c.getTipo() == tipo).findFirst();
    }

    @Override
    public List<Caixa> listarTodos() {
        return new ArrayList<>(caixas);
    }

    @Override
    public void remover(TipoCaixa tipo) {
        caixas.removeIf(c -> c.getTipo() == tipo);
    }
}
