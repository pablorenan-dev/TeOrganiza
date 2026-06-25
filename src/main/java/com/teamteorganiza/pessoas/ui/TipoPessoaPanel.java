package com.teamteorganiza.pessoas.ui;

import com.teamteorganiza.pessoas.PessoaService;
import com.teamteorganiza.pessoas.TipoPessoa;
import com.teamteorganiza.pessoas.TipoPessoaService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TipoPessoaPanel extends JPanel {

    private final TipoPessoaService tipoPessoaService;
    private final PessoaService pessoaService;
    private Runnable onVoltar;

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField tfNome, tfDescricao;
    private Integer idSelecionado = null;

    public TipoPessoaPanel(TipoPessoaService tipoPessoaService, PessoaService pessoaService) {
        this.tipoPessoaService = tipoPessoaService;
        this.pessoaService = pessoaService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome", "Descrição", "Ativo"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final Color INATIVO = new Color(210, 210, 210);
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    boolean ativo = "Sim".equals(t.getValueAt(row, 3));
                    c.setBackground(ativo ? Color.WHITE : INATIVO);
                }
                return c;
            }
        });
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) popularFormulario();
        });
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(4, 6));
        bottomPanel.add(montarFormulario(), BorderLayout.CENTER);
        bottomPanel.add(montarBotoes(), BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        atualizarTabela();
    }

    public void setOnVoltar(Runnable r) { this.onVoltar = r; }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        tfNome      = new JTextField();
        tfDescricao = new JTextField();

        adicionarCampo(painel, "Nome",      tfNome);
        adicionarCampo(painel, "Descrição", tfDescricao);

        return painel;
    }

    private void adicionarCampo(JPanel painel, String label, JTextField campo) {
        JLabel lbl = new JLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, campo.getPreferredSize().height));
        painel.add(lbl);
        painel.add(campo);
        painel.add(Box.createVerticalStrut(4));
    }

    private JPanel montarBotoes() {
        JPanel painel = new JPanel(new GridLayout(1, 4, 8, 0));

        JButton btnCriar     = new JButton("Criar");
        JButton btnEditar    = new JButton("Editar");
        JButton btnDesativar = new JButton("Desativar/Ativar");
        JButton btnExcluir   = new JButton("Excluir");

        btnCriar.addActionListener(e     -> acaoCriar());
        btnEditar.addActionListener(e    -> acaoEditar());
        btnDesativar.addActionListener(e -> acaoDesativar());
        btnExcluir.addActionListener(e   -> acaoExcluir());

        painel.add(btnCriar);
        painel.add(btnEditar);
        painel.add(btnDesativar);
        painel.add(btnExcluir);
        return painel;
    }

    private void acaoCriar() {
        try {
            tipoPessoaService.criar(tfNome.getText().trim(), tfDescricao.getText().trim());
            limparFormulario();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoEditar() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um tipo na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            tipoPessoaService.editar(idSelecionado, tfNome.getText().trim(), tfDescricao.getText().trim());
            limparFormulario();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoDesativar() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um tipo na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tipoPessoaService.desativar(idSelecionado);
        limparFormulario();
        atualizarTabela();
    }

    private void acaoExcluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um tipo na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Excluir o tipo selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                tipoPessoaService.remover(idSelecionado, pessoaService.listar());
                limparFormulario();
                atualizarTabela();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Tipo em uso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void popularFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) { idSelecionado = null; return; }
        idSelecionado = (int) tableModel.getValueAt(row, 0);
        tipoPessoaService.listar().stream()
            .filter(t -> t.getId() == idSelecionado)
            .findFirst()
            .ifPresent(t -> {
                tfNome.setText(t.getNome());
                tfDescricao.setText(t.getDescricao());
            });
    }

    private void limparFormulario() {
        idSelecionado = null;
        tabela.clearSelection();
        tfNome.setText("");
        tfDescricao.setText("");
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        List<TipoPessoa> tipos = tipoPessoaService.listar();
        for (TipoPessoa t : tipos) {
            tableModel.addRow(new Object[]{
                t.getId(), t.getNome(), t.getDescricao(), t.isAtivo() ? "Sim" : "Não"
            });
        }
    }
}
