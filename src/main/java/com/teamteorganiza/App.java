package com.teamteorganiza;

import com.teamteorganiza.financeiro.CaixaRepositoryEmMemoria;
import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.MensalidadeRepositoryEmMemoria;
import com.teamteorganiza.financeiro.ui.FinanceiroPanel;
import com.teamteorganiza.pessoas.PessoaRepositoryEmMemoria;
import com.teamteorganiza.pessoas.PessoaService;
import com.teamteorganiza.pessoas.ui.PessoaPanel;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            PessoaService pessoaService = new PessoaService(new PessoaRepositoryEmMemoria());
            FinanceiroService financeiroService = new FinanceiroService(
                new MensalidadeRepositoryEmMemoria(),
                new CaixaRepositoryEmMemoria()
            );

            JTabbedPane abas = new JTabbedPane();
            abas.addTab("Pessoas",    new PessoaPanel(pessoaService));
            abas.addTab("Financeiro", new FinanceiroPanel(financeiroService));

            JFrame frame = new JFrame("TeOrganiza");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(abas);
            frame.setSize(950, 620);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
