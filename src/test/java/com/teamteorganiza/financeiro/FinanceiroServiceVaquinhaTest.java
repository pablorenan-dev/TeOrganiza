package com.teamteorganiza.financeiro;

import static org.junit.Assert.assertEquals;

import com.teamteorganiza.estoque.EstoqueRepositoryEmMemoria;
import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.financeiro.model.ContribuicaoVaquinha;
import com.teamteorganiza.financeiro.model.Vaquinha;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FinanceiroServiceVaquinhaTest {

    private FinanceiroService service;
    private Vaquinha vaquinha;

    @Before
    public void setUp() {
        MensalidadeRepository mensalidadeRepo = new MensalidadeRepositoryEmMemoria();
        MovimentacaoFinanceiraRepository movimentacaoRepo = new MovimentacaoFinanceiraRepositoryEmMemoria();
        EstoqueService estoqueService = new EstoqueService(new EstoqueRepositoryEmMemoria());
        service = new FinanceiroService(mensalidadeRepo, movimentacaoRepo, estoqueService);

        vaquinha = service.criarVaquinha("Reforma da quadra", "Arrecadar para reforma", 100.0);
    }

    @Test
    public void contribuirDeveRegistrarContribuicaoNaVaquinha() {
        ContribuicaoVaquinha c = service.contribuir(vaquinha, "pessoa-1", 25.0, "Doação");

        assertEquals(1, vaquinha.getContribuicoes().size());
        assertEquals(25.0, vaquinha.totalArrecadado(), 0.0001);
        assertEquals(c.getId(), vaquinha.getContribuicoes().get(0).getId());
    }

    @Test
    public void editarContribuicaoDeveAtualizarValorEDescricao() {
        ContribuicaoVaquinha c = service.contribuir(vaquinha, "pessoa-1", 25.0, "Doação");

        service.editarContribuicao(vaquinha, c.getId(), "pessoa-1", "Doação ajustada", 40.0);

        ContribuicaoVaquinha atualizada = vaquinha.getContribuicoes().get(0);
        assertEquals(40.0, atualizada.getValor(), 0.0001);
        assertEquals("Doação ajustada", atualizada.getDescricao());
        assertEquals(40.0, vaquinha.totalArrecadado(), 0.0001);
    }

    @Test
    public void removerContribuicaoDeveRemoverDaVaquinha() {
        ContribuicaoVaquinha c = service.contribuir(vaquinha, "pessoa-1", 25.0, "Doação");

        service.removerContribuicao(vaquinha, c.getId());

        assertEquals(0, vaquinha.getContribuicoes().size());
    }

    @Test
    public void top3DoadoresDeveSomarPorPessoaEOrdenarDecrescente() {
        service.contribuir(vaquinha, "pessoa-1", 10.0, "Doação");
        service.contribuir(vaquinha, "pessoa-2", 50.0, "Doação");
        service.contribuir(vaquinha, "pessoa-1", 20.0, "Doação");
        service.contribuir(vaquinha, "pessoa-3", 5.0, "Doação");

        List<Doador> top3 = service.top3Doadores();

        assertEquals(3, top3.size());
        assertEquals("pessoa-2", top3.get(0).pessoaId());
        assertEquals(50.0, top3.get(0).total(), 0.0001);
        assertEquals("pessoa-1", top3.get(1).pessoaId());
        assertEquals(30.0, top3.get(1).total(), 0.0001);
        assertEquals("pessoa-3", top3.get(2).pessoaId());
        assertEquals(5.0, top3.get(2).total(), 0.0001);
    }
}
