package com.teamteorganiza.pessoas;

import java.util.Optional;

public interface InstrutorDadosRepository {
    void salvarOuAtualizar(InstrutorDados dados);
    Optional<InstrutorDados> buscarPorPessoaId(int pessoaId);
    void remover(int pessoaId);
}
