package com.teamteorganiza;

import com.teamteorganiza.estoque.ui.EstoquePanel;
import com.teamteorganiza.eventos.ui.EventosPanel;
import com.teamteorganiza.financeiro.CaixaRepositoryEmMemoria;
import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.MensalidadeRepositoryEmMemoria;
import com.teamteorganiza.financeiro.ui.FinanceiroPanel;
import com.teamteorganiza.pessoas.InstrutorDadosRepositoryEmMemoria;
import com.teamteorganiza.pessoas.PessoaRepositoryEmMemoria;
import com.teamteorganiza.pessoas.PessoaService;
import com.teamteorganiza.pessoas.TipoPessoaRepositoryEmMemoria;
import com.teamteorganiza.pessoas.TipoPessoaService;
import com.teamteorganiza.pessoas.ui.PessoaPanel;
import com.teamteorganiza.pessoas.ui.TipoPessoaPanel;
import com.teamteorganiza.ui.HomePanel;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            PessoaService pessoaService = new PessoaService(
                new PessoaRepositoryEmMemoria(),
                new InstrutorDadosRepositoryEmMemoria()
            );
            TipoPessoaService tipoPessoaService = new TipoPessoaService(
                new TipoPessoaRepositoryEmMemoria()
            );
            FinanceiroService financeiroService = new FinanceiroService(
                new MensalidadeRepositoryEmMemoria(),
                new CaixaRepositoryEmMemoria()
            );

            JPanel root = new JPanel(new CardLayout());
            CardLayout cards = (CardLayout) root.getLayout();

            HomePanel       homePanel       = new HomePanel();
            PessoaPanel     pessoaPanel     = new PessoaPanel(pessoaService, tipoPessoaService);
            TipoPessoaPanel tipoPessoaPanel = new TipoPessoaPanel(tipoPessoaService, pessoaService);
            FinanceiroPanel financeiroPanel = new FinanceiroPanel(financeiroService);
            EstoquePanel    estoquePanel    = new EstoquePanel();
            EventosPanel    eventosPanel    = new EventosPanel();

            root.add(homePanel,       "HOME");
            root.add(pessoaPanel,     "PESSOAS");
            root.add(tipoPessoaPanel, "TIPO_PESSOA");
            root.add(financeiroPanel, "FINANCEIRO");
            root.add(estoquePanel,    "ESTOQUE");
            root.add(eventosPanel,    "EVENTOS");

            homePanel.setOnPessoas(   () -> cards.show(root, "PESSOAS"));
            homePanel.setOnFinanceiro(() -> cards.show(root, "FINANCEIRO"));
            homePanel.setOnEstoque(   () -> cards.show(root, "ESTOQUE"));
            homePanel.setOnEventos(   () -> cards.show(root, "EVENTOS"));

            pessoaPanel.setOnVoltar(    () -> cards.show(root, "HOME"));
            pessoaPanel.setOnTipos(     () -> cards.show(root, "TIPO_PESSOA"));
            tipoPessoaPanel.setOnVoltar(() -> {
                pessoaPanel.refresh();
                cards.show(root, "PESSOAS");
            });
            financeiroPanel.setOnVoltar(() -> cards.show(root, "HOME"));
            estoquePanel.setOnVoltar(   () -> cards.show(root, "HOME"));
            eventosPanel.setOnVoltar(   () -> cards.show(root, "HOME"));

            JFrame frame = new JFrame("TeOrganiza");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(root);
            frame.setSize(950, 620);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
