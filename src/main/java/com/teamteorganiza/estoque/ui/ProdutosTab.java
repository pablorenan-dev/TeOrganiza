package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.UnidadeMedida;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Aba de cadastro de produtos: criar, editar, deletar e listar o catálogo. */
public class ProdutosTab extends JPanel {

    private final EstoqueService service;
    private final Runnable onChange;

    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final JTextField tfNome = new JTextField(16);
    private final JTextField tfCategoria = new JTextField(16);
    private final JComboBox<UnidadeMedida> cbUnidade = new JComboBox<>(UnidadeMedida.values());
    private final JTextField tfMinimo = new JTextField(8);
    private final JTextField tfPrecoVenda = new JTextField(8);

    private List<Produto> linhas = List.of();

    public ProdutosTab(EstoqueService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] colunas = {"ID", "Nome", "Categoria", "Un.", "Qtd", "Mínimo", "Custo médio", "Preço venda", "Status"};
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

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Produto"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; painel.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.gridy = 0; painel.add(tfNome, c);

        c.gridx = 0; c.gridy = 1; painel.add(new JLabel("Categoria:"), c);
        c.gridx = 1; c.gridy = 1; painel.add(tfCategoria, c);

        c.gridx = 0; c.gridy = 2; painel.add(new JLabel("Unidade:"), c);
        c.gridx = 1; c.gridy = 2; painel.add(cbUnidade, c);

        c.gridx = 0; c.gridy = 3; painel.add(new JLabel("Estoque mínimo:"), c);
        c.gridx = 1; c.gridy = 3; painel.add(tfMinimo, c);

        c.gridx = 0; c.gridy = 4; painel.add(new JLabel("Preço de venda (R$):"), c);
        c.gridx = 1; c.gridy = 4; painel.add(tfPrecoVenda, c);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnCriar = new JButton("Criar");
        JButton btnEditar = new JButton("Editar");
        JButton btnDeletar = new JButton("Deletar");
        JButton btnLimpar = new JButton("Limpar");
        botoes.add(btnCriar); botoes.add(btnEditar); botoes.add(btnDeletar); botoes.add(btnLimpar);
        c.gridx = 1; c.gridy = 5; painel.add(botoes, c);

        btnCriar.addActionListener(e -> criar());
        btnEditar.addActionListener(e -> editar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limpar());

        return painel;
    }

    private void criar() {
        Double minimo = lerMinimo();
        if (minimo == null) return;
        Double preco = lerPreco();
        if (preco == null) return;
        try {
            service.cadastrarProduto(tfNome.getText().trim(), tfCategoria.getText().trim(),
                    (UnidadeMedida) cbUnidade.getSelectedItem(), minimo, preco);
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        onChange.run();
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um produto para editar."); return; }
        Double minimo = lerMinimo();
        if (minimo == null) return;
        Double preco = lerPreco();
        if (preco == null) return;
        try {
            service.editarProduto(linhas.get(row).getId(), tfNome.getText().trim(),
                    tfCategoria.getText().trim(), (UnidadeMedida) cbUnidade.getSelectedItem(), minimo, preco);
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        onChange.run();
    }

    private void deletar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione um produto para deletar."); return; }
        try {
            service.removerProduto(linhas.get(row).getId());
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        Produto p = linhas.get(row);
        tfNome.setText(p.getNome());
        tfCategoria.setText(p.getCategoria());
        cbUnidade.setSelectedItem(p.getUnidade());
        tfMinimo.setText(String.format("%.2f", p.getEstoqueMinimo()));
        tfPrecoVenda.setText(String.format("%.2f", p.getPrecoVenda()));
    }

    private void limpar() {
        tfNome.setText("");
        tfCategoria.setText("");
        cbUnidade.setSelectedIndex(0);
        tfMinimo.setText("");
        tfPrecoVenda.setText("");
        tabela.clearSelection();
    }

    public void recarregar() {
        linhas = service.listar();
        tableModel.setRowCount(0);
        for (Produto p : linhas) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                p.getUnidade(),
                String.format("%.2f", p.getQuantidade()),
                String.format("%.2f", p.getEstoqueMinimo()),
                String.format("R$ %.2f", p.getCustoMedio()),
                String.format("R$ %.2f", p.getPrecoVenda()),
                p.abaixoDoMinimo() ? "REPOR" : "OK"
            });
        }
    }

    private Double lerMinimo() {
        try {
            return CampoUtil.numero(tfMinimo.getText());
        } catch (NumberFormatException ex) {
            aviso("Estoque mínimo inválido.");
            return null;
        }
    }

    private Double lerPreco() {
        try {
            return CampoUtil.numero(tfPrecoVenda.getText());
        } catch (NumberFormatException ex) {
            aviso("Preço de venda inválido.");
            return null;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
