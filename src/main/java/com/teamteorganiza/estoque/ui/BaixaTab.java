package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.TipoMovimentoEstoque;

import javax.swing.*;
import java.awt.*;

/** Aba de baixa de estoque por consumo interno ou venda. */
public class BaixaTab extends JPanel {

    private final EstoqueService service;
    private final Runnable onChange;

    private final JComboBox<Produto> cbProduto = new JComboBox<>();
    private final JComboBox<TipoMovimentoEstoque> cbTipo =
            new JComboBox<>(new TipoMovimentoEstoque[]{
                    TipoMovimentoEstoque.BAIXA_CONSUMO, TipoMovimentoEstoque.BAIXA_VENDA});
    private final JTextField tfQuantidade = new JTextField(10);
    private final JTextField tfObservacao = new JTextField(18);

    public BaixaTab(EstoqueService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cbProduto.setRenderer(new ProdutoRenderer());
        add(montarFormulario(), BorderLayout.NORTH);
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Dar baixa (consumo / venda)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; painel.add(new JLabel("Produto:"), c);
        c.gridx = 1; c.gridy = 0; painel.add(cbProduto, c);

        c.gridx = 0; c.gridy = 1; painel.add(new JLabel("Tipo:"), c);
        c.gridx = 1; c.gridy = 1; painel.add(cbTipo, c);

        c.gridx = 0; c.gridy = 2; painel.add(new JLabel("Quantidade:"), c);
        c.gridx = 1; c.gridy = 2; painel.add(tfQuantidade, c);

        c.gridx = 0; c.gridy = 3; painel.add(new JLabel("Observação:"), c);
        c.gridx = 1; c.gridy = 3; painel.add(tfObservacao, c);

        JButton btnBaixar = new JButton("Dar baixa");
        btnBaixar.addActionListener(e -> baixar());
        c.gridx = 1; c.gridy = 4; painel.add(btnBaixar, c);

        return painel;
    }

    private void baixar() {
        Produto produto = (Produto) cbProduto.getSelectedItem();
        if (produto == null) { aviso("Cadastre um produto primeiro."); return; }
        Double quantidade;
        try {
            quantidade = CampoUtil.numero(tfQuantidade.getText());
        } catch (NumberFormatException ex) {
            aviso("Quantidade inválida.");
            return;
        }
        try {
            service.darBaixa(produto.getId(), quantidade,
                    (TipoMovimentoEstoque) cbTipo.getSelectedItem(), tfObservacao.getText().trim());
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        tfQuantidade.setText("");
        tfObservacao.setText("");
        onChange.run();
    }

    public void recarregar() {
        Produto selecionado = (Produto) cbProduto.getSelectedItem();
        cbProduto.removeAllItems();
        for (Produto p : service.listar()) {
            cbProduto.addItem(p);
        }
        if (selecionado != null) {
            for (int i = 0; i < cbProduto.getItemCount(); i++) {
                if (cbProduto.getItemAt(i).getId().equals(selecionado.getId())) {
                    cbProduto.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
