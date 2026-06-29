package com.teamteorganiza.eventos.ui;

import javax.swing.*;
import com.teamteorganiza.Compromissos;
import com.teamteorganiza.TipoCompromisso; // Importado o Enum
import java.awt.*;
import java.time.format.DateTimeParseException;

public class EventosPanel extends JPanel {

    private Runnable onVoltar;

    public EventosPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnVoltar = new JButton("← Voltar");
        btnVoltar.addActionListener(e -> { if (onVoltar != null) onVoltar.run(); });
        topBar.add(btnVoltar);
        add(topBar, BorderLayout.NORTH);

        JPanel painel = new JPanel(new BorderLayout(10,10));
        JPanel formulario = new JPanel(new GridLayout(8,2,5,5));

        JTextField txtTitulo = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>();
        JComboBox<String> cbCategoria = new JComboBox<>();

        cbTipo.addItem("INVERNADA");
        cbTipo.addItem("CAMPEIRA");
        cbTipo.addItem("CTG");
        cbTipo.addActionListener(
        e -> atualizarCategorias(cbTipo, cbCategoria));
        atualizarCategorias(cbTipo, cbCategoria);

        JTextField txtData = new JTextField("2026-06-27"); // Padrão aceito pelo parse()
        JTextField txtResponsavel = new JTextField();
        JTextField txtLocal = new JTextField();
        JTextField txtDescricao = new JTextField();

        formulario.add(new JLabel("Título:"));
        formulario.add(txtTitulo);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(cbTipo);
        formulario.add(new JLabel("Categoria:"));
        formulario.add(cbCategoria);
        formulario.add(new JLabel("Data (AAAA-MM-DD):"));
        formulario.add(txtData);
        formulario.add(new JLabel("Responsável:"));
        formulario.add(txtResponsavel);
        formulario.add(new JLabel("Local:"));
        formulario.add(txtLocal);
        formulario.add(new JLabel("Descrição:"));
        formulario.add(txtDescricao);

        JTextArea areaEventos = new JTextArea();
        areaEventos.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaEventos);

        JButton btnCadastrar = new JButton("Cadastrar Evento");

        btnCadastrar.addActionListener(e -> {
            try {
                Compromissos c = new Compromissos(
                    txtTitulo.getText(),
                    TipoCompromisso.valueOf(cbTipo.getSelectedItem().toString()),
                    cbCategoria.getSelectedItem().toString(),
                    java.time.LocalDate.parse(txtData.getText()),
                    "",
                    txtLocal.getText(),
                    txtResponsavel.getText(),
                    txtDescricao.getText()
                );

                Compromissos.adicionarCompromisso(c);

                areaEventos.setText("");
                for (Compromissos comp : Compromissos.getListaCompromissos()) {
                    areaEventos.append(comp.toString() + "\n");
                }

                // Limpa os campos
                txtTitulo.setText("");
                txtResponsavel.setText("");
                txtLocal.setText("");
                txtDescricao.setText("");
                
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Formato de data inválido! Use o padrão AAAA-MM-DD.", 
                        "Erro na Data", JOptionPane.ERROR_MESSAGE);
            }
        });

        painel.add(formulario, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        painel.add(btnCadastrar, BorderLayout.SOUTH);

        add(painel, BorderLayout.CENTER);
    }
    private void atualizarCategorias(
        JComboBox<String> cbTipo,
        JComboBox<String> cbCategoria) {

    cbCategoria.removeAllItems();

    String tipo = cbTipo.getSelectedItem().toString();

    switch (tipo) {

        case "INVERNADA":
            cbCategoria.addItem("Pré-Mirim");
            cbCategoria.addItem("Mirim");
            cbCategoria.addItem("Juvenil");
            cbCategoria.addItem("Adulta");
            cbCategoria.addItem("Veterana");
            cbCategoria.addItem("Xirú");
            break;

        case "CAMPEIRA":
            cbCategoria.addItem("Rodeio");
            cbCategoria.addItem("Tiro de Laço");
            cbCategoria.addItem("Treino");
            cbCategoria.addItem("Cavalgada");
            break;

        case "CTG":
            cbCategoria.addItem("Baile");
            cbCategoria.addItem("Janta");
            cbCategoria.addItem("Reunião");
            cbCategoria.addItem("Festival");
            cbCategoria.addItem("Assembleia");
            break;
    }
}
    public void setOnVoltar(Runnable r) { this.onVoltar = r; }
}
