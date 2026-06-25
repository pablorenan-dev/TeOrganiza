package com.teamteorganiza.pessoas;

import com.teamteorganiza.common.Repository;
import java.util.Optional;

public interface TipoPessoaRepository extends Repository<TipoPessoa, Integer> {
    Optional<TipoPessoa> buscarPorNome(String nome);
}
