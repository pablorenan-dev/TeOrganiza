package com.teamteorganiza.estoque.ui;

import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.MovimentoEstoque;
import com.teamteorganiza.estoque.model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** Aba só-leitura com o histórico (extrato) de entradas e baixas. */
public class MovimentosTab extends JPanel {

    private final EstoqueService service;

    private final DefaultTableModel tableModel;
    private final JLabel resumo = new JLabel();

    public MovimentosTab(EstoqueService service) {
        this.service = service;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] colunas = {"ID", "Produto", "Tipo", "Quantidade", "Custo unit.", "Data", "Observação"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabela = new JTable(tableModel);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        resumo.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(resumo, BorderLayout.SOUTH);
    }

    public void recarregar() {
        Map<String, String> nomes = new HashMap<>();
        for (Produto p : service.listar()) {
            nomes.put(p.getId(), p.getNome());
        }

        tableModel.setRowCount(0);
        for (MovimentoEstoque m : service.getMovimentos()) {
            tableModel.addRow(new Object[]{
                m.getId(),
                nomes.getOrDefault(m.getProdutoId(), "(removido #" + m.getProdutoId() + ")"),
                m.getTipo(),
                String.format("%.2f", m.getQuantidade()),
                m.getTipo().isBaixa() ? "-" : String.format("R$ %.2f", m.getCustoUnitario()),
                m.getData(),
                m.getObservacao()
            });
        }
        resumo.setText(String.format("Valor total em estoque: R$ %.2f", service.valorTotalEmEstoque()));
    }
}
