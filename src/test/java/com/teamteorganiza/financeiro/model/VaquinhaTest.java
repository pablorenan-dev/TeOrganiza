package com.teamteorganiza.financeiro.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class VaquinhaTest {

    private Vaquinha vaquinha;

    @Before
    public void setUp() {
        vaquinha = new Vaquinha("Reforma da quadra", "Arrecadar para reforma", 100.0);
    }

    @Test
    public void contribuirDeveAdicionarContribuicaoESomarAoTotal() {
        ContribuicaoVaquinha c1 = vaquinha.contribuir("pessoa-1", 30.0, "Doação inicial");
        ContribuicaoVaquinha c2 = vaquinha.contribuir("pessoa-2", 20.0, "");

        assertEquals(2, vaquinha.getContribuicoes().size());
        assertEquals(50.0, vaquinha.totalArrecadado(), 0.0001);
        assertEquals("Doação inicial", c1.getDescricao());
        assertEquals("Doação", c2.getDescricao());
        assertFalse(vaquinha.metaAtingida());
        assertEquals(50.0, vaquinha.quantoFalta(), 0.0001);
    }

    @Test
    public void metaAtingidaDeveSerTrueQuandoTotalAlcancaAMeta() {
        vaquinha.contribuir("pessoa-1", 60.0, "Doação");
        vaquinha.contribuir("pessoa-2", 40.0, "Doação");

        assertTrue(vaquinha.metaAtingida());
        assertEquals(0.0, vaquinha.quantoFalta(), 0.0001);
    }

    @Test
    public void removerContribuicaoDeveAtualizarOTotalArrecadado() {
        ContribuicaoVaquinha c1 = vaquinha.contribuir("pessoa-1", 30.0, "Doação");
        vaquinha.contribuir("pessoa-2", 20.0, "Doação");

        vaquinha.removerContribuicao(c1.getId());

        assertEquals(1, vaquinha.getContribuicoes().size());
        assertEquals(20.0, vaquinha.totalArrecadado(), 0.0001);
    }
}
