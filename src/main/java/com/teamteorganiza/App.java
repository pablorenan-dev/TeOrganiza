package com.teamteorganiza;

import com.teamteorganiza.auth.LoginDialog;
import com.teamteorganiza.auth.UsuariosDialog;
import com.teamteorganiza.estoque.EstoqueRepositorySupabase;
import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.ui.EstoquePanel;
import com.teamteorganiza.eventos.CompromissoRepositorySupabase;
import com.teamteorganiza.eventos.EventosService;
import com.teamteorganiza.eventos.ui.EventosPanel;
import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.MensalidadeRepositorySupabase;
import com.teamteorganiza.financeiro.ui.FinanceiroPanel;
import com.teamteorganiza.pessoas.InstrutorDadosRepositorySupabase;
import com.teamteorganiza.pessoas.PessoaRepositorySupabase;
import com.teamteorganiza.pessoas.PessoaService;
import com.teamteorganiza.pessoas.TipoPessoaRepositorySupabase;
import com.teamteorganiza.pessoas.TipoPessoaService;
import com.teamteorganiza.pessoas.ui.PessoaPanel;
import com.teamteorganiza.pessoas.ui.TipoPessoaPanel;
import com.teamteorganiza.ui.HomePanel;

import com.teamteorganiza.auth.SessaoAtual;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TeOrganiza");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(950, 620);
            frame.setLocationRelativeTo(null);

            LoginDialog login = new LoginDialog(frame);
            login.setVisible(true);
            if (!login.isLogado()) {
                System.exit(0);
                return;
            }

            PessoaService pessoaService = new PessoaService(
                new PessoaRepositorySupabase(),
                new InstrutorDadosRepositorySupabase()
            );
            TipoPessoaService tipoPessoaService = new TipoPessoaService(
                new TipoPessoaRepositorySupabase()
            );
            FinanceiroService financeiroService = new FinanceiroService(
                new MensalidadeRepositorySupabase()
            );
            EstoqueService estoqueService = new EstoqueService(
                new EstoqueRepositorySupabase()
            );
            EventosService eventosService = new EventosService(
                new CompromissoRepositorySupabase()
            );

            JPanel root = new JPanel(new CardLayout());
            CardLayout cards = (CardLayout) root.getLayout();

            HomePanel       homePanel       = new HomePanel();
            PessoaPanel     pessoaPanel     = new PessoaPanel(pessoaService, tipoPessoaService);
            TipoPessoaPanel tipoPessoaPanel = new TipoPessoaPanel(tipoPessoaService, pessoaService);
            FinanceiroPanel financeiroPanel = new FinanceiroPanel(financeiroService, pessoaService);
            EstoquePanel    estoquePanel    = new EstoquePanel(estoqueService);
            EventosPanel    eventosPanel    = new EventosPanel(eventosService);

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
            homePanel.setOnUsuarios(  () -> new UsuariosDialog(frame).setVisible(true));

            pessoaPanel.setOnVoltar(    () -> cards.show(root, "HOME"));
            pessoaPanel.setOnTipos(     () -> cards.show(root, "TIPO_PESSOA"));
            tipoPessoaPanel.setOnVoltar(() -> {
                pessoaPanel.refresh();
                cards.show(root, "PESSOAS");
            });
            financeiroPanel.setOnVoltar(() -> cards.show(root, "HOME"));
            estoquePanel.setOnVoltar(   () -> cards.show(root, "HOME"));
            eventosPanel.setOnVoltar(   () -> cards.show(root, "HOME"));

            JLabel lblNome = new JLabel(SessaoAtual.get().getNome());
            lblNome.setFont(lblNome.getFont().deriveFont(Font.BOLD, 13f));

            JLabel lblOrg = new JLabel(SessaoAtual.get().getNomeOrg());
            lblOrg.setFont(lblOrg.getFont().deriveFont(10f));
            lblOrg.setForeground(Color.GRAY);

            JPanel infoUsuario = new JPanel();
            infoUsuario.setLayout(new BoxLayout(infoUsuario, BoxLayout.Y_AXIS));
            infoUsuario.setOpaque(false);
            lblNome.setAlignmentX(Component.RIGHT_ALIGNMENT);
            lblOrg.setAlignmentX(Component.RIGHT_ALIGNMENT);
            infoUsuario.add(lblNome);
            infoUsuario.add(lblOrg);

            JPanel header = new JPanel(new BorderLayout());
            header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
            header.add(infoUsuario, BorderLayout.EAST);

            JPanel conteudo = new JPanel(new BorderLayout());
            conteudo.add(header, BorderLayout.NORTH);
            conteudo.add(root,   BorderLayout.CENTER);

            frame.setContentPane(conteudo);
            frame.setVisible(true);
        });
    }
}
