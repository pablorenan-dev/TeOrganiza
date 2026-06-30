package com.teamteorganiza.financeiro;

import com.teamteorganiza.financeiro.model.MovimentacaoFinanceira;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovimentacaoFinanceiraRepositoryEmMemoria implements MovimentacaoFinanceiraRepository {

    private final List<MovimentacaoFinanceira> movimentacoes = new ArrayList<>();

    @Override
    public void salvar(MovimentacaoFinanceira m) {
        movimentacoes.removeIf(x -> x.getId().equals(m.getId()));
        movimentacoes.add(m);
    }

    @Override
    public Optional<MovimentacaoFinanceira> buscarPorId(String id) {
        return movimentacoes.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    @Override
    public List<MovimentacaoFinanceira> listarTodos() { return new ArrayList<>(movimentacoes); }

    @Override
    public void remover(String id) { movimentacoes.removeIf(m -> m.getId().equals(id)); }
}
