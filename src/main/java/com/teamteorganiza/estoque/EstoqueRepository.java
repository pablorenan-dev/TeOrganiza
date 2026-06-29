package com.teamteorganiza.estoque;

import com.teamteorganiza.common.Repository;
import com.teamteorganiza.estoque.model.Produto;

import java.util.Optional;

public interface EstoqueRepository extends Repository<Produto, String> {

    Optional<Produto> buscarPorNome(String nome);
}
