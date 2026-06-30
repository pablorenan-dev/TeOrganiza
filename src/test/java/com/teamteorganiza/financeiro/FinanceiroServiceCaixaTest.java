package com.teamteorganiza.financeiro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.teamteorganiza.estoque.EstoqueRepositoryEmMemoria;
import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.UnidadeMedida;
import com.teamteorganiza.financeiro.model.MovimentacaoFinanceira;
import com.teamteorganiza.financeiro.model.TipoLancamento;
import com.teamteorganiza.financeiro.model.VendaCaixa;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FinanceiroServiceCaixaTest {

    private FinanceiroService service;
    private EstoqueService estoqueService;
    private Produto produto;

    @Before
    public void setUp() {
        MensalidadeRepository mensalidadeRepo = new MensalidadeRepositoryEmMemoria();
        MovimentacaoFinanceiraRepository movimentacaoRepo = new MovimentacaoFinanceiraRepositoryEmMemoria();
        estoqueService = new EstoqueService(new EstoqueRepositoryEmMemoria());
        service = new FinanceiroService(mensalidadeRepo, movimentacaoRepo, estoqueService);

        produto = estoqueService.cadastrarProduto("Refrigerante", "Bebidas", UnidadeMedida.UN, 0, 5.0);
        estoqueService.registrarEntrada(produto.getId(), 100, 2.0);
    }

    @Test
    public void fecharCaixaComVendasDeveGerarMovimentacaoEReiniciarCaixa() {
        service.registrarVenda(null, produto.getId(), 3, "Vendedor 1");
        service.registrarVenda(null, produto.getId(), 2, "Vendedor 1");
        double totalEsperado = service.totalCaixa();
        assertEquals(25.0, totalEsperado, 0.0001);

        service.fecharCaixa("Evento 2");

        List<MovimentacaoFinanceira> entradas = service.getEntradas();
        assertEquals(1, entradas.size());
        assertEquals(totalEsperado, entradas.get(0).getValor(), 0.0001);
        assertEquals(TipoLancamento.RECEITA, entradas.get(0).getTipo());

        assertEquals("Evento 2", service.getNomeEvento());
        assertEquals(0, service.getVendasCaixa().size());
        assertEquals(0.0, service.totalCaixa(), 0.0001);
    }

    @Test
    public void fecharCaixaSemVendasNaoDeveGerarMovimentacao() {
        service.fecharCaixa("Evento 2");

        assertTrue(service.getEntradas().isEmpty());
        assertEquals("Evento 2", service.getNomeEvento());
    }

    @Test
    public void registrarVendaDeveAbaterEstoqueESomarNoCaixa() {
        VendaCaixa venda = service.registrarVenda(null, produto.getId(), 4, "Vendedor 1");

        assertEquals(20.0, venda.getValor(), 0.0001);
        assertEquals(96, estoqueService.buscarProduto(produto.getId()).getQuantidade(), 0.0001);
        assertEquals(20.0, service.totalCaixa(), 0.0001);
    }

    @Test
    public void removerVendaDeveEstornarEstoqueERemoverDoCaixa() {
        VendaCaixa venda = service.registrarVenda(null, produto.getId(), 4, "Vendedor 1");

        service.removerVenda(venda.getId());

        assertEquals(100.0, estoqueService.buscarProduto(produto.getId()).getQuantidade(), 0.0001);
        assertEquals(0.0, service.totalCaixa(), 0.0001);
        assertEquals(0, service.getVendasCaixa().size());
    }
}
