package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;

import javax.swing.*;
import java.awt.*;

/** Aba de entrada de mercadoria: soma quantidade e recalcula o custo médio. */
public class EntradaTab extends JPanel {

    private final EstoqueService service;
    private final Runnable onChange;

    private final JComboBox<Produto> cbProduto = new JComboBox<>();
    private final JTextField tfQuantidade = new JTextField(10);
    private final JTextField tfCusto = new JTextField(10);

    public EntradaTab(EstoqueService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cbProduto.setRenderer(new ProdutoRenderer());
        add(montarFormulario(), BorderLayout.NORTH);
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Registrar entrada (compra/reposição)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; painel.add(new JLabel("Produto:"), c);
        c.gridx = 1; c.gridy = 0; painel.add(cbProduto, c);

        c.gridx = 0; c.gridy = 1; painel.add(new JLabel("Quantidade:"), c);
        c.gridx = 1; c.gridy = 1; painel.add(tfQuantidade, c);

        c.gridx = 0; c.gridy = 2; painel.add(new JLabel("Custo unitário (R$):"), c);
        c.gridx = 1; c.gridy = 2; painel.add(tfCusto, c);

        JButton btnRegistrar = new JButton("Registrar entrada");
        btnRegistrar.addActionListener(e -> registrar());
        c.gridx = 1; c.gridy = 3; painel.add(btnRegistrar, c);

        return painel;
    }

    private void registrar() {
        Produto produto = (Produto) cbProduto.getSelectedItem();
        if (produto == null) { aviso("Cadastre um produto primeiro."); return; }
        Double quantidade = ler(tfQuantidade, "Quantidade inválida.");
        Double custo = ler(tfCusto, "Custo unitário inválido.");
        if (quantidade == null || custo == null) return;
        try {
            service.registrarEntrada(produto.getId(), quantidade, custo);
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        tfQuantidade.setText("");
        tfCusto.setText("");
        onChange.run();
    }

    public void recarregar() {
        Produto selecionado = (Produto) cbProduto.getSelectedItem();
        cbProduto.removeAllItems();
        for (Produto p : service.listar()) {
            cbProduto.addItem(p);
        }
        if (selecionado != null) reselecionar(selecionado.getId());
    }

    private void reselecionar(String produtoId) {
        for (int i = 0; i < cbProduto.getItemCount(); i++) {
            if (cbProduto.getItemAt(i).getId().equals(produtoId)) {
                cbProduto.setSelectedIndex(i);
                return;
            }
        }
    }

    private Double ler(JTextField campo, String erro) {
        try {
            return CampoUtil.numero(campo.getText());
        } catch (NumberFormatException ex) {
            aviso(erro);
            return null;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
