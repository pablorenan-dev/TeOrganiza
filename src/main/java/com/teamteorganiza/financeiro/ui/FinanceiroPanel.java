package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.Caixa;
import com.teamteorganiza.financeiro.model.TipoCaixa;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FinanceiroPanel extends JPanel {

    private final FinanceiroService service;
    private final JPanel painelSaldos;

    public FinanceiroPanel(FinanceiroService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        painelSaldos = new JPanel();
        painelSaldos.setLayout(new BoxLayout(painelSaldos, BoxLayout.Y_AXIS));
        painelSaldos.setBorder(BorderFactory.createTitledBorder("Saldos dos caixas"));
        add(new JScrollPane(painelSaldos), BorderLayout.NORTH);

        add(montarPainelAcoes(), BorderLayout.CENTER);

        atualizarSaldos();
    }

    private JPanel montarPainelAcoes() {
        JPanel painel = new JPanel(new GridLayout(0, 1, 4, 4));
        painel.setBorder(BorderFactory.createTitledBorder("Registrar movimentação"));

        for (TipoCaixa tipo : TipoCaixa.values()) {
            JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

            JLabel lblTipo    = new JLabel(tipo.name() + ":");
            lblTipo.setPreferredSize(new Dimension(90, 24));
            linha.add(lblTipo);

            JTextField tfDesc  = new JTextField(14);
            JTextField tfValor = new JTextField(8);
            JTextField tfResp  = new JTextField(10);

            linha.add(new JLabel("Descrição:")); linha.add(tfDesc);
            linha.add(new JLabel("Valor:"));     linha.add(tfValor);
            linha.add(new JLabel("Resp.:"));     linha.add(tfResp);

            JButton btnEntrada = new JButton("+ Entrada");
            JButton btnSaida   = new JButton("- Saída");
            linha.add(btnEntrada);
            linha.add(btnSaida);

            btnEntrada.addActionListener(e -> {
                try {
                    double valor = Double.parseDouble(tfValor.getText().trim().replace(",", "."));
                    service.registrarEntrada(tipo, tfDesc.getText().trim(), valor, tfResp.getText().trim());
                    tfDesc.setText(""); tfValor.setText(""); tfResp.setText("");
                    atualizarSaldos();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnSaida.addActionListener(e -> {
                try {
                    double valor = Double.parseDouble(tfValor.getText().trim().replace(",", "."));
                    service.registrarSaida(tipo, tfDesc.getText().trim(), valor, tfResp.getText().trim());
                    tfDesc.setText(""); tfValor.setText(""); tfResp.setText("");
                    atualizarSaldos();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            painel.add(linha);
        }

        return painel;
    }

    private void atualizarSaldos() {
        painelSaldos.removeAll();
        List<Caixa> caixas = service.getCaixas();
        for (Caixa c : caixas) {
            String texto = String.format("  %s:   R$ %.2f", c.getTipo().name(), c.saldoAtual());
            JLabel label = new JLabel(texto);
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
            painelSaldos.add(label);
        }
        painelSaldos.revalidate();
        painelSaldos.repaint();
    }
}
