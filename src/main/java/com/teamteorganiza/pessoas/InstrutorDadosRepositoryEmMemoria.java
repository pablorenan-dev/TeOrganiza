package com.teamteorganiza.pessoas;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstrutorDadosRepositoryEmMemoria implements InstrutorDadosRepository {

    private final Map<Integer, InstrutorDados> dados = new HashMap<>();

    @Override
    public void salvarOuAtualizar(InstrutorDados d) {
        dados.put(d.getPessoaId(), d);
    }

    @Override
    public Optional<InstrutorDados> buscarPorPessoaId(int pessoaId) {
        return Optional.ofNullable(dados.get(pessoaId));
    }

    @Override
    public void remover(int pessoaId) {
        dados.remove(pessoaId);
    }
}
