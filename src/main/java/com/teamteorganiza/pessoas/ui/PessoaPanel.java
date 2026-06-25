package com.teamteorganiza.pessoas.ui;

import com.teamteorganiza.pessoas.Pessoa;
import com.teamteorganiza.pessoas.PessoaService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PessoaPanel extends JPanel {

    private final PessoaService service;
    private final DefaultTableModel tableModel;
    private Runnable onVoltar;

    private JTable tabela;
    private JTextField tfNome, tfDia, tfMes, tfAno, tfCpf, tfTelefone, tfEmail;
    private Integer idSelecionado = null;

    private JTextField tfBusca;
    private JComboBox<String> cbStatus;
    private JSpinner spIdadeMin, spIdadeMax;
    private JComboBox<String> cbOrdem;

    public PessoaPanel(PessoaService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(montarTopBar(), BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome", "CPF", "Idade", "Telefone", "E-mail", "Ativo"};
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
                    boolean ativo = "Sim".equals(t.getValueAt(row, 6));
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

    private JPanel montarTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(8, 4));

        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        tfBusca = new JTextField(16);
        tfBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { atualizarTabela(); }
            public void removeUpdate(DocumentEvent e)  { atualizarTabela(); }
            public void changedUpdate(DocumentEvent e) { atualizarTabela(); }
        });

        cbStatus = new JComboBox<>(new String[]{"Todos", "Ativos", "Inativos"});
        cbStatus.addActionListener(e -> atualizarTabela());

        spIdadeMin = new JSpinner(new SpinnerNumberModel(0, 0, 150, 1));
        spIdadeMax = new JSpinner(new SpinnerNumberModel(150, 0, 150, 1));
        spIdadeMin.addChangeListener(e -> atualizarTabela());
        spIdadeMax.addChangeListener(e -> atualizarTabela());
        spIdadeMin.setPreferredSize(new Dimension(55, spIdadeMin.getPreferredSize().height));
        spIdadeMax.setPreferredSize(new Dimension(55, spIdadeMax.getPreferredSize().height));

        cbOrdem = new JComboBox<>(new String[]{"A → Z", "Z → A"});
        cbOrdem.addActionListener(e -> atualizarTabela());

        filtros.add(new JLabel("Buscar:"));   filtros.add(tfBusca);
        filtros.add(new JLabel("Status:"));   filtros.add(cbStatus);
        filtros.add(new JLabel("Idade:"));    filtros.add(spIdadeMin);
        filtros.add(new JLabel("a"));         filtros.add(spIdadeMax);
        filtros.add(new JLabel("Ordem:"));    filtros.add(cbOrdem);

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        esquerda.add(btnVoltar);

        topBar.add(esquerda, BorderLayout.WEST);
        topBar.add(filtros,  BorderLayout.CENTER);
        return topBar;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        tfNome     = new JTextField();
        tfDia      = new JTextField(3);
        tfMes      = new JTextField(3);
        tfAno      = new JTextField(5);
        tfCpf      = new JTextField();
        tfTelefone = new JTextField();
        tfEmail    = new JTextField();

        adicionarCampo(painel, "Nome",     tfNome);
        adicionarCampoData(painel);
        adicionarCampo(painel, "CPF",      tfCpf);
        adicionarCampo(painel, "Telefone", tfTelefone);
        adicionarCampo(painel, "E-mail",   tfEmail);

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

    private void adicionarCampoData(JPanel painel) {
        JLabel lbl = new JLabel("Data de Nascimento");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lbl);

        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.add(new JLabel("Dia:"));  linha.add(tfDia);
        linha.add(new JLabel("Mês:")); linha.add(tfMes);
        linha.add(new JLabel("Ano:"));  linha.add(tfAno);
        painel.add(linha);
        painel.add(Box.createVerticalStrut(4));
    }

    private LocalDate lerData() {
        try {
            int dia = Integer.parseInt(tfDia.getText().trim());
            int mes = Integer.parseInt(tfMes.getText().trim());
            int ano = Integer.parseInt(tfAno.getText().trim());
            return LocalDate.of(ano, mes, dia);
        } catch (NumberFormatException | java.time.DateTimeException ex) {
            throw new IllegalArgumentException("Data inválida. Preencha dia, mês e ano com números válidos.");
        }
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
            service.cadastrar(
                tfNome.getText().trim(), lerData(),
                tfCpf.getText().trim(),
                tfTelefone.getText().trim(),
                tfEmail.getText().trim()
            );
            limparFormulario();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoEditar() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma pessoa na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            service.editar(idSelecionado,
                tfNome.getText().trim(), lerData(),
                tfTelefone.getText().trim(),
                tfEmail.getText().trim()
            );
            limparFormulario();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoDesativar() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma pessoa na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        service.desativar(idSelecionado);
        limparFormulario();
        atualizarTabela();
    }

    private void acaoExcluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma pessoa na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Excluir a pessoa selecionada?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.remover(idSelecionado);
            limparFormulario();
            atualizarTabela();
        }
    }

    private void popularFormulario() {
        int row = tabela.getSelectedRow();
        if (row < 0) { idSelecionado = null; return; }
        idSelecionado = (int) tableModel.getValueAt(row, 0);
        service.listar().stream()
            .filter(p -> p.getId() == idSelecionado)
            .findFirst()
            .ifPresent(p -> {
                tfNome.setText(p.getNome());
                tfDia.setText(String.valueOf(p.getDataDeNascimento().getDayOfMonth()));
                tfMes.setText(String.valueOf(p.getDataDeNascimento().getMonthValue()));
                tfAno.setText(String.valueOf(p.getDataDeNascimento().getYear()));
                tfCpf.setText(p.getCpf());
                tfTelefone.setText(p.getTelefone());
                tfEmail.setText(p.getEmail());
            });
    }

    private void limparFormulario() {
        idSelecionado = null;
        tabela.clearSelection();
        tfNome.setText(""); tfDia.setText(""); tfMes.setText(""); tfAno.setText(""); tfCpf.setText("");
        tfTelefone.setText(""); tfEmail.setText("");
    }

    private void atualizarTabela() {
        String busca    = tfBusca  != null ? tfBusca.getText().trim().toLowerCase() : "";
        String status   = cbStatus != null ? (String) cbStatus.getSelectedItem()    : "Todos";
        int    idadeMin = spIdadeMin != null ? (int) spIdadeMin.getValue()           : 0;
        int    idadeMax = spIdadeMax != null ? (int) spIdadeMax.getValue()           : 150;
        boolean reverso = cbOrdem  != null && "Z → A".equals(cbOrdem.getSelectedItem());

        List<Pessoa> pessoas = service.listar().stream()
            .filter(p -> busca.isEmpty() || p.getNome().toLowerCase().contains(busca))
            .filter(p -> {
                if ("Ativos".equals(status))   return p.isAtivo();
                if ("Inativos".equals(status)) return !p.isAtivo();
                return true;
            })
            .filter(p -> {
                int idade = Period.between(p.getDataDeNascimento(), LocalDate.now()).getYears();
                return idade >= idadeMin && idade <= idadeMax;
            })
            .sorted(reverso
                ? Comparator.comparing(Pessoa::getNome, String.CASE_INSENSITIVE_ORDER).reversed()
                : Comparator.comparing(Pessoa::getNome, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (Pessoa p : pessoas) {
            int idade = Period.between(p.getDataDeNascimento(), LocalDate.now()).getYears();
            tableModel.addRow(new Object[]{
                p.getId(), p.getNome(), p.getCpf(), idade,
                p.getTelefone(), p.getEmail(), p.isAtivo() ? "Sim" : "Não"
            });
        }
    }
}
