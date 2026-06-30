package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.Doador;
import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.ContribuicaoVaquinha;
import com.teamteorganiza.financeiro.model.Vaquinha;
import com.teamteorganiza.pessoas.Pessoa;
import com.teamteorganiza.pessoas.PessoaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class VaquinhaTab extends JPanel {

    private final FinanceiroService service;
    private final PessoaService pessoaService;
    private final Function<String, String> nomeResolver;
    private final Runnable onChange;

    private final JComboBox<VaquinhaItem> combo = new JComboBox<>();
    private final JLabel lblInfo = new JLabel(" ");
    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final DefaultTableModel topModel;
    private final JComboBox<Pessoa> comboPessoa = new JComboBox<>();
    private final JTextField tfDescricao = new JTextField(20);
    private final JTextField tfValor = new JTextField(10);

    private List<ContribuicaoVaquinha> linhas = List.of();
    private boolean atualizandoCombo = false;

    public VaquinhaTab(FinanceiroService service, PessoaService pessoaService,
                       Function<String, String> nomeResolver, Runnable onChange) {
        this.service = service;
        this.pessoaService = pessoaService;
        this.nomeResolver = nomeResolver;
        this.onChange = onChange;

        comboPessoa.setRenderer(new PessoaRenderer());

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(montarTopo(), BorderLayout.NORTH);

        String[] colunas = {"Nome", "Valor", "Descrição"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });

        topModel = new DefaultTableModel(new String[]{"#", "Nome", "Total"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable topTabela = new JTable(topModel);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Top 3 doadores (todas as vaquinhas)"));
        topPanel.add(new JScrollPane(topTabela), BorderLayout.CENTER);
        topPanel.setPreferredSize(new Dimension(240, 0));

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        centro.add(topPanel, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        add(montarFormulario(), BorderLayout.SOUTH);
    }

    private JPanel montarTopo() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topo.add(new JLabel("Vaquinha:"));
        topo.add(combo);
        JButton btnNova = new JButton("Nova vaquinha");
        topo.add(btnNova);
        topo.add(Box.createHorizontalStrut(16));
        topo.add(lblInfo);

        combo.addActionListener(e -> {
            if (atualizandoCombo) return;
            recarregarTabela();
            atualizarInfo();
        });
        btnNova.addActionListener(e -> novaVaquinha());
        return topo;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Contribuição"));
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

    private void novaVaquinha() {
        JTextField tfTitulo  = new JTextField(18);
        JTextField tfObjetivo = new JTextField(18);
        JTextField tfMeta    = new JTextField(10);
        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("Título:"));    form.add(tfTitulo);
        form.add(new JLabel("Objetivo:"));  form.add(tfObjetivo);
        form.add(new JLabel("Meta (R$):")); form.add(tfMeta);

        int opcao = JOptionPane.showConfirmDialog(this, form, "Nova vaquinha",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return;

        String titulo = tfTitulo.getText().trim();
        if (titulo.isEmpty()) { aviso("Informe o título da vaquinha."); return; }
        double meta;
        try {
            meta = CampoUtil.valor(tfMeta.getText());
        } catch (NumberFormatException ex) {
            aviso("Meta inválida.");
            return;
        }
        Vaquinha nova = service.criarVaquinha(titulo, tfObjetivo.getText().trim(), meta);
        onChange.run();
        selecionar(nova);
    }

    private void criar() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) { aviso("Crie ou selecione uma vaquinha primeiro."); return; }
        String pessoaId = lerPessoaId();
        if (pessoaId == null) return;
        Double valor = lerValor();
        if (valor == null) return;
        service.contribuir(v, pessoaId, valor, tfDescricao.getText().trim());
        limpar();
        onChange.run();
    }

    private void editar() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) return;
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma contribuição para editar."); return; }
        String pessoaId = lerPessoaId();
        if (pessoaId == null) return;
        Double valor = lerValor();
        if (valor == null) return;
        service.editarContribuicao(v, linhas.get(row).getId(), pessoaId, tfDescricao.getText().trim(), valor);
        limpar();
        onChange.run();
    }

    private void deletar() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) return;
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma contribuição para deletar."); return; }
        service.removerContribuicao(v, linhas.get(row).getId());
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        ContribuicaoVaquinha c = linhas.get(row);
        selecionarNoComboPessoa(c.getPessoaId());
        tfDescricao.setText(c.getDescricao());
        tfValor.setText(String.format("%.2f", c.getValor()));
    }

    private void limpar() {
        if (comboPessoa.getItemCount() > 0) comboPessoa.setSelectedIndex(0);
        tfDescricao.setText("");
        tfValor.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        atualizarComboPessoas();

        atualizandoCombo = true;
        Vaquinha selecionada = vaquinhaSelecionada();
        combo.removeAllItems();
        for (Vaquinha v : service.getVaquinhas()) combo.addItem(new VaquinhaItem(v));
        if (selecionada != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).vaquinha == selecionada) { combo.setSelectedIndex(i); break; }
            }
        }
        atualizandoCombo = false;

        recarregarTabela();
        atualizarInfo();
        atualizarTop3();
    }

    private void atualizarComboPessoas() {
        Pessoa selecionada = (Pessoa) comboPessoa.getSelectedItem();
        comboPessoa.removeAllItems();
        comboPessoa.addItem(null);
        for (Pessoa p : pessoaService.listar()) comboPessoa.addItem(p);
        if (selecionada != null) {
            for (int i = 1; i < comboPessoa.getItemCount(); i++) {
                if (comboPessoa.getItemAt(i).getId().equals(selecionada.getId())) {
                    comboPessoa.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void selecionarNoComboPessoa(String pessoaId) {
        if (pessoaId == null || pessoaId.isEmpty()) { comboPessoa.setSelectedIndex(0); return; }
        for (int i = 1; i < comboPessoa.getItemCount(); i++) {
            if (comboPessoa.getItemAt(i).getId().equals(pessoaId)) {
                comboPessoa.setSelectedIndex(i);
                return;
            }
        }
    }

    private void recarregarTabela() {
        Vaquinha v = vaquinhaSelecionada();
        linhas = (v == null) ? List.of() : v.getContribuicoes();
        tableModel.setRowCount(0);
        for (ContribuicaoVaquinha c : linhas) {
            tableModel.addRow(new Object[]{
                nomeResolver.apply(c.getPessoaId()),
                String.format("R$ %.2f", c.getValor()),
                c.getDescricao()
            });
        }
    }

    private void atualizarInfo() {
        Vaquinha v = vaquinhaSelecionada();
        if (v == null) { lblInfo.setText("Nenhuma vaquinha cadastrada."); return; }
        lblInfo.setText(String.format(
                "Objetivo: %s  |  Arrecadado: R$ %.2f de R$ %.2f  |  Falta: R$ %.2f",
                v.getObjetivo(), v.totalArrecadado(), v.getMeta(), v.quantoFalta()));
    }

    private void atualizarTop3() {
        topModel.setRowCount(0);
        int pos = 1;
        for (Doador d : service.top3Doadores()) {
            topModel.addRow(new Object[]{
                pos++,
                nomeResolver.apply(d.pessoaId()),
                String.format("R$ %.2f", d.total())
            });
        }
    }

    private void selecionar(Vaquinha v) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).vaquinha == v) { combo.setSelectedIndex(i); break; }
        }
    }

    private Vaquinha vaquinhaSelecionada() {
        VaquinhaItem item = (VaquinhaItem) combo.getSelectedItem();
        return item == null ? null : item.vaquinha;
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
            setText(value instanceof Pessoa p ? p.getNome() : "(Anônimo)");
            return this;
        }
    }

    private static class VaquinhaItem {
        final Vaquinha vaquinha;
        VaquinhaItem(Vaquinha vaquinha) { this.vaquinha = vaquinha; }
        @Override public String toString() { return vaquinha.getTitulo(); }
    }
}
