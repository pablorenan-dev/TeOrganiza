package com.teamteorganiza.financeiro.ui;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.estoque.EstoqueService;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.financeiro.FinanceiroService;
import com.teamteorganiza.financeiro.model.VendaCaixa;
import com.teamteorganiza.pessoas.Pessoa;
import com.teamteorganiza.pessoas.PessoaService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class CaixaTab extends JPanel {

    private final FinanceiroService service;
    private final PessoaService pessoaService;
    private final EstoqueService estoqueService;
    private final Function<String, String> nomeResolver;
    private final Runnable onChange;

    private final JTextField tfEvento = new JTextField(16);
    private final JLabel lblTotal = new JLabel();
    private final DefaultTableModel tableModel;
    private final JTable tabela;
    private final JComboBox<Pessoa> comboComprador = new JComboBox<>();
    private final JComboBox<Produto> comboProduto = new JComboBox<>();
    private final JTextField tfQuantidade = new JTextField(8);
    private final JLabel lblValor = new JLabel("R$ 0,00");
    private final JLabel lblVendedor = new JLabel();

    private List<VendaCaixa> linhas = List.of();

    public CaixaTab(FinanceiroService service, PessoaService pessoaService, EstoqueService estoqueService,
                    Function<String, String> nomeResolver, Runnable onChange) {
        this.service = service;
        this.pessoaService = pessoaService;
        this.estoqueService = estoqueService;
        this.nomeResolver = nomeResolver;
        this.onChange = onChange;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        comboComprador.setRenderer(new PessoaRenderer());
        comboProduto.setRenderer(new ProdutoRenderer());

        add(montarTopo(), BorderLayout.NORTH);

        String[] colunas = {"Produto", "Qtd", "Valor", "Comprador", "Vendedor"};
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

    private JPanel montarTopo() {
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topo.setBorder(BorderFactory.createTitledBorder("Caixa do evento"));

        topo.add(new JLabel("Evento:"));
        topo.add(tfEvento);
        JButton btnSalvarNome = new JButton("Salvar nome");
        topo.add(btnSalvarNome);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        topo.add(Box.createHorizontalStrut(16));
        topo.add(lblTotal);

        JButton btnFechar = new JButton("Fechar caixa / Novo evento");
        topo.add(Box.createHorizontalStrut(16));
        topo.add(btnFechar);

        btnSalvarNome.addActionListener(e -> {
            String nome = tfEvento.getText().trim();
            if (nome.isEmpty()) { aviso("Informe o nome do evento."); return; }
            service.setNomeEvento(nome);
            recarregar();
        });
        btnFechar.addActionListener(e -> fecharCaixa());

        return topo;
    }

    private JPanel montarFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Venda"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; painel.add(new JLabel("Produto:"), c);
        c.gridx = 1; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; painel.add(comboProduto, c);
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0; c.gridy = 1; painel.add(new JLabel("Quantidade:"), c);
        c.gridx = 1; c.gridy = 1; painel.add(tfQuantidade, c);

        c.gridx = 0; c.gridy = 2; painel.add(new JLabel("Valor (R$):"), c);
        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD));
        c.gridx = 1; c.gridy = 2; painel.add(lblValor, c);

        c.gridx = 0; c.gridy = 3; painel.add(new JLabel("Comprador:"), c);
        c.gridx = 1; c.gridy = 3; c.fill = GridBagConstraints.HORIZONTAL; painel.add(comboComprador, c);
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0; c.gridy = 4; painel.add(new JLabel("Vendedor:"), c);
        c.gridx = 1; c.gridy = 4; painel.add(lblVendedor, c);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnCriar   = new JButton("Criar");
        JButton btnEditar  = new JButton("Editar");
        JButton btnDeletar = new JButton("Deletar");
        JButton btnLimpar  = new JButton("Limpar");
        botoes.add(btnCriar); botoes.add(btnEditar); botoes.add(btnDeletar); botoes.add(btnLimpar);
        c.gridx = 1; c.gridy = 5; painel.add(botoes, c);

        btnCriar.addActionListener(e -> criar());
        btnEditar.addActionListener(e -> editar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limpar());

        // Recalcula o valor sempre que mudar o produto ou a quantidade.
        comboProduto.addActionListener(e -> atualizarValor());
        tfQuantidade.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { atualizarValor(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizarValor(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizarValor(); }
        });

        return painel;
    }

    private void criar() {
        Produto produto = (Produto) comboProduto.getSelectedItem();
        if (produto == null) { aviso("Selecione um produto."); return; }
        Double qtd = lerQuantidade();
        if (qtd == null) return;
        try {
            service.registrarVenda(lerCompradorId(), produto.getId(), qtd, vendedorAtual());
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        onChange.run();
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma venda para editar."); return; }
        Produto produto = (Produto) comboProduto.getSelectedItem();
        if (produto == null) { aviso("Selecione um produto."); return; }
        Double qtd = lerQuantidade();
        if (qtd == null) return;
        try {
            service.editarVenda(linhas.get(row).getId(), lerCompradorId(), produto.getId(), qtd, vendedorAtual());
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        onChange.run();
    }

    private void deletar() {
        int row = tabela.getSelectedRow();
        if (row < 0) { aviso("Selecione uma venda para deletar."); return; }
        try {
            service.removerVenda(linhas.get(row).getId());
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        }
        limpar();
        onChange.run();
    }

    private void fecharCaixa() {
        double total = service.totalCaixa();
        if (total <= 0) { aviso("Não há vendas no caixa para fechar."); return; }
        String nomeNovo = JOptionPane.showInputDialog(this,
                String.format("Fechar o caixa de \"%s\" (total R$ %.2f) e lançar como 1 entrada.%n"
                        + "Nome do novo evento:", service.getNomeEvento(), total),
                "Novo evento");
        if (nomeNovo == null) return;
        if (nomeNovo.trim().isEmpty()) nomeNovo = "Novo evento";
        service.fecharCaixa(nomeNovo.trim());
        limpar();
        onChange.run();
    }

    private void preencher() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= linhas.size()) return;
        VendaCaixa v = linhas.get(row);
        selecionarNoComboProduto(v.getProdutoId());
        tfQuantidade.setText(String.format("%.2f", v.getQuantidade()));
        selecionarNoComboComprador(v.getPessoaId());
        atualizarValor();
    }

    private void limpar() {
        if (comboProduto.getItemCount() > 0) comboProduto.setSelectedIndex(0);
        if (comboComprador.getItemCount() > 0) comboComprador.setSelectedIndex(0);
        tfQuantidade.setText("1");
        tabela.clearSelection();
        atualizarValor();
    }

    public void recarregar() {
        atualizarComboCompradores();
        atualizarComboProdutos();
        lblVendedor.setText(vendedorAtual());
        if (!tfEvento.isFocusOwner()) tfEvento.setText(service.getNomeEvento());
        linhas = service.getVendasCaixa();
        tableModel.setRowCount(0);
        for (VendaCaixa v : linhas) {
            tableModel.addRow(new Object[]{
                v.getDescricao(),
                String.format("%.2f", v.getQuantidade()),
                String.format("R$ %.2f", v.getValor()),
                nomeResolver.apply(v.getPessoaId()),
                v.getVendedorNome()
            });
        }
        lblTotal.setText(String.format("Total do caixa: R$ %.2f  (%d venda(s))",
                service.totalCaixa(), linhas.size()));
        atualizarValor();
    }

    private void atualizarValor() {
        Produto p = (Produto) comboProduto.getSelectedItem();
        double qtd;
        try { qtd = CampoUtil.valor(tfQuantidade.getText()); } catch (NumberFormatException ex) { qtd = 0; }
        double valor = (p == null ? 0 : p.getPrecoVenda()) * qtd;
        lblValor.setText(String.format("R$ %.2f", valor));
    }

    private void atualizarComboCompradores() {
        Pessoa selecionada = (Pessoa) comboComprador.getSelectedItem();
        comboComprador.removeAllItems();
        comboComprador.addItem(null);
        for (Pessoa p : pessoaService.listar()) comboComprador.addItem(p);
        if (selecionada != null) {
            for (int i = 1; i < comboComprador.getItemCount(); i++) {
                if (comboComprador.getItemAt(i).getId().equals(selecionada.getId())) {
                    comboComprador.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void atualizarComboProdutos() {
        Produto selecionado = (Produto) comboProduto.getSelectedItem();
        comboProduto.removeAllItems();
        for (Produto p : estoqueService.listar()) comboProduto.addItem(p);
        if (selecionado != null) {
            for (int i = 0; i < comboProduto.getItemCount(); i++) {
                if (comboProduto.getItemAt(i).getId().equals(selecionado.getId())) {
                    comboProduto.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void selecionarNoComboProduto(String produtoId) {
        if (produtoId == null) return;
        for (int i = 0; i < comboProduto.getItemCount(); i++) {
            if (comboProduto.getItemAt(i).getId().equals(produtoId)) {
                comboProduto.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selecionarNoComboComprador(String pessoaId) {
        if (pessoaId == null || pessoaId.isEmpty()) { comboComprador.setSelectedIndex(0); return; }
        for (int i = 1; i < comboComprador.getItemCount(); i++) {
            if (comboComprador.getItemAt(i).getId().equals(pessoaId)) {
                comboComprador.setSelectedIndex(i);
                return;
            }
        }
    }

    private String lerCompradorId() {
        Pessoa p = (Pessoa) comboComprador.getSelectedItem();
        return p == null ? "" : p.getId();
    }

    private Double lerQuantidade() {
        try {
            double qtd = CampoUtil.valor(tfQuantidade.getText());
            if (qtd <= 0) { aviso("Quantidade deve ser maior que zero."); return null; }
            return qtd;
        } catch (NumberFormatException ex) {
            aviso("Quantidade inválida.");
            return null;
        }
    }

    private String vendedorAtual() {
        String nome = SessaoAtual.get().getNome();
        return nome == null ? "—" : nome;
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

    private static class ProdutoRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Produto p) {
                setText(String.format("%s — R$ %.2f (estoque: %.2f)",
                        p.getNome(), p.getPrecoVenda(), p.getQuantidade()));
            }
            return this;
        }
    }
}
