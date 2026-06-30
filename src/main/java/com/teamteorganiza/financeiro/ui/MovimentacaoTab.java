package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.MovimentacaoFinanceira;
import com.teamteorganiza.financeiro.model.TipoLancamento;
import com.teamteorganiza.pessoas.Pessoa;
import com.teamteorganiza.pessoas.PessoaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class MovimentacaoTab extends JPanel {

    private final FinanceiroService service;
    private final PessoaService pessoaService;
    private final Function<String, String> nomeResolver;
    private final Runnable onChange;
    private final TipoLancamento tipo;

    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final JComboBox<Pessoa> comboPessoa = new JComboBox<>();
    private final JTextField tfDescricao = new JTextField(24);
    private final JTextField tfValor = new JTextField(10);

    private List<MovimentacaoFinanceira> linhas = List.of();

    public MovimentacaoTab(FinanceiroService service, PessoaService pessoaService,
                           Function<String, String> nomeResolver, Runnable onChange, TipoLancamento tipo) {
        this.service = service;
        this.pessoaService = pessoaService;
        this.nomeResolver = nomeResolver;
        this.onChange = onChange;
        this.tipo = tipo;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        comboPessoa.setRenderer(new PessoaRenderer());

        String[] colunas = {"Nome", "Valor", "Descrição"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private boolean isEntrada() { return tipo == TipoLancamento.RECEITA; }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder(isEntrada() ? "Entrada" : "Despesa"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; painel.add(new JLabel("Pessoa:"), c);
        c.gridx = 1; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; painel.add(comboPessoa, c);
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0; c.gridy = 1; painel.add(new JLabel("Descrição:"), c);
        c.gridx = 1; c.gridy = 1; painel.add(tfDescricao, c);

        c.gridx = 0; c.gridy = 2; painel.add(new JLabel("Valor (R$):"), c);
        c.gridx = 1; c.gridy = 2; painel.add(tfValor, c);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnCriar   = new JButton("Criar");
        JButton btnEditar  = new JButton("Editar");
        JButton btnDeletar = new JButton("Deletar");
        JButton btnLimpar  = new JButton("Limpar");
        botoes.add(btnCriar); botoes.add(btnEditar); botoes.add(btnDeletar); botoes.add(btnLimpar);
        c.gridx = 1; c.gridy = 3; painel.add(botoes, c);

        btnCriar.addActionListener(e -> criar());
        btnEditar.addActionListener(e -> editar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limpar());

        return painel;
    }

    private void criar() {
        String pessoaId = lerPessoaId();
        if (pessoaId == null) return;
        Double valor = lerValor();
        if (valor == null) return;
        if (isEntrada()) service.registrarEntrada(pessoaId, tfDescricao.getText().trim(), valor);
        else             service.registrarDespesa(pessoaId, tfDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um item para editar."); return; }
        String pessoaId = lerPessoaId();
        if (pessoaId == null) return;
        Double valor = lerValor();
        if (valor == null) return;
        service.editarMovimentacao(linhas.get(row).getId(), pessoaId, tfDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void deletar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um item para deletar."); return; }
        service.removerMovimentacao(linhas.get(row).getId());
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        MovimentacaoFinanceira m = linhas.get(row);
        selecionarNoComboPessoa(m.getPessoaId());
        tfDescricao.setText(m.getDescricao());
        tfValor.setText(String.format("%.2f", m.getValor()));
    }

    private void limpar() {
        if (comboPessoa.getItemCount() > 0) comboPessoa.setSelectedIndex(0);
        tfDescricao.setText("");
        tfValor.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        atualizarComboPessoas();
        linhas = isEntrada() ? service.getEntradas() : service.getDespesas();
        tableModel.setRowCount(0);
        for (MovimentacaoFinanceira m : linhas) {
            tableModel.addRow(new Object[]{
                nomeResolver.apply(m.getPessoaId()),
                String.format("R$ %.2f", m.getValor()),
                m.getDescricao()
            });
        }
    }

    private void atualizarComboPessoas() {
        Pessoa selecionada = (Pessoa) comboPessoa.getSelectedItem();
        comboPessoa.removeAllItems();
        for (Pessoa p : pessoaService.listar()) comboPessoa.addItem(p);
        if (selecionada != null) {
            for (int i = 0; i < comboPessoa.getItemCount(); i++) {
                if (comboPessoa.getItemAt(i).getId().equals(selecionada.getId())) {
                    comboPessoa.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void selecionarNoComboPessoa(String pessoaId) {
        if (pessoaId == null || pessoaId.isEmpty()) { comboPessoa.setSelectedIndex(0); return; }
        for (int i = 0; i < comboPessoa.getItemCount(); i++) {
            if (comboPessoa.getItemAt(i).getId().equals(pessoaId)) {
                comboPessoa.setSelectedIndex(i);
                return;
            }
        }
    }

    private String lerPessoaId() {
        Pessoa p = (Pessoa) comboPessoa.getSelectedItem();
        if (p == null) { aviso("Selecione uma pessoa."); return null; }
        return p.getId();
    }

    private Double lerValor() {
        try {
            return CampoUtil.valor(tfValor.getText());
        } catch (NumberFormatException ex) {
            aviso("Valor inválido.");
            return null;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private static class PessoaRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(value instanceof Pessoa p ? p.getNome() : "");
            return this;
        }
    }
}
