package com.teamteorganiza.pessoas.ui;

import com.teamteorganiza.pessoas.Pessoa;
import com.teamteorganiza.pessoas.PessoaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.time.Period;

public class PessoaPanel extends JPanel {

    private final PessoaService service;
    private final DefaultTableModel tableModel;
    private Runnable onVoltar;

    public PessoaPanel(PessoaService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome", "CPF", "Idade", "Telefone", "E-mail", "Ativo"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(montarFormulario(), BorderLayout.SOUTH);

        atualizarTabela();
    }

    public void setOnVoltar(Runnable r) { this.onVoltar = r; }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Nova pessoa"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        JTextField tfNome     = new JTextField(18);
        JTextField tfNasc     = new JTextField(10);
        JTextField tfCpf      = new JTextField(14);
        JTextField tfTelefone = new JTextField(12);
        JTextField tfEmail    = new JTextField(18);

        int linha = 0;
        adicionarLinha(painel, c, linha++, "Nome:",                     tfNome);
        adicionarLinha(painel, c, linha++, "Nascimento (AAAA-MM-DD):",  tfNasc);
        adicionarLinha(painel, c, linha++, "CPF:",                      tfCpf);
        adicionarLinha(painel, c, linha++, "Telefone:",                 tfTelefone);
        adicionarLinha(painel, c, linha++, "E-mail:",                   tfEmail);

        JButton btnCadastrar = new JButton("Cadastrar");
        c.gridx = 1; c.gridy = linha;
        painel.add(btnCadastrar, c);

        btnCadastrar.addActionListener(e -> {
            try {
                LocalDate nasc = LocalDate.parse(tfNasc.getText().trim());
                service.cadastrar(
                    tfNome.getText().trim(),
                    nasc,
                    tfCpf.getText().trim(),
                    tfTelefone.getText().trim(),
                    tfEmail.getText().trim()
                );
                atualizarTabela();
                tfNome.setText(""); tfNasc.setText(""); tfCpf.setText("");
                tfTelefone.setText(""); tfEmail.setText("");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                    "Data inválida. Use o formato AAAA-MM-DD.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }

    private void adicionarLinha(JPanel p, GridBagConstraints c, int linha, String label, JTextField campo) {
        c.gridx = 0; c.gridy = linha;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        p.add(campo, c);
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        List<Pessoa> pessoas = service.listar();
        for (Pessoa p : pessoas) {
            int idade = Period.between(p.getDataDeNascimento(), LocalDate.now()).getYears();
            tableModel.addRow(new Object[]{
                p.getId(), p.getNome(), p.getCpf(), idade,
                p.getTelefone(), p.getEmail(), p.isAtivo() ? "Sim" : "Não"
            });
        }
    }
}
