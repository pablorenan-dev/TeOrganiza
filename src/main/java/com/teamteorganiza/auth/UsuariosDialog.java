package com.teamteorganiza.auth;

import com.teamteorganiza.infra.SupabaseClient;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/** Tela de administração: lista os usuários da organização atual e permite cadastrar novos. */
public class UsuariosDialog extends JDialog {

    private final DefaultTableModel tableModel;
    private final JLabel lblErro = new JLabel(" ");

    public UsuariosDialog(Frame parent) {
        super(parent, "Usuários — " + SessaoAtual.get().getNomeOrg(), true);
        setLayout(new BorderLayout(8, 8));
        setSize(520, 420);
        setLocationRelativeTo(parent);

        String[] colunas = {"Nome", "E-mail", "Perfil", "Ativo"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabela = new JTable(tableModel);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        lblErro.setForeground(Color.RED);
        lblErro.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnAdicionar = new JButton("Adicionar usuário");
        btnAdicionar.addActionListener(e -> abrirFormularioNovoUsuario());

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        rodape.add(lblErro, BorderLayout.NORTH);
        rodape.add(btnAdicionar, BorderLayout.SOUTH);
        add(rodape, BorderLayout.SOUTH);

        carregar();
    }

    private void carregar() {
        tableModel.setRowCount(0);
        String sql = "SELECT nome, email, perfil, ativo FROM usuarios WHERE organizacao_id = ? ORDER BY nome";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(SessaoAtual.get().getOrgId()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("perfil"),
                    rs.getBoolean("ativo") ? "Sim" : "Não"
                });
            }
        } catch (Exception ex) {
            lblErro.setText("Erro ao carregar usuários: " + ex.getMessage());
        }
    }

    private void abrirFormularioNovoUsuario() {
        JTextField tfNome  = new JTextField();
        JTextField tfEmail = new JTextField();
        JPasswordField tfSenha    = new JPasswordField();
        JPasswordField tfConfirma = new JPasswordField();
        JComboBox<String> cbPerfil = new JComboBox<>(new String[]{"USUARIO", "ADMIN"});

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Nome:"));            form.add(tfNome);
        form.add(new JLabel("E-mail:"));          form.add(tfEmail);
        form.add(new JLabel("Senha:"));            form.add(tfSenha);
        form.add(new JLabel("Confirmar senha:")); form.add(tfConfirma);
        form.add(new JLabel("Perfil:"));          form.add(cbPerfil);

        int op = JOptionPane.showConfirmDialog(this, form,
            "Adicionar usuário — " + SessaoAtual.get().getNomeOrg(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;

        String nome  = tfNome.getText().trim();
        String email = tfEmail.getText().trim();
        String senha = new String(tfSenha.getPassword());
        String conf  = new String(tfConfirma.getPassword());
        String perfil = (String) cbPerfil.getSelectedItem();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            lblErro.setText("Preencha todos os campos.");
            return;
        }
        if (!senha.equals(conf)) {
            lblErro.setText("As senhas não coincidem.");
            return;
        }

        lblErro.setText("Adicionando...");
        new SwingWorker<Void, Void>() {
            private String erro;

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = SupabaseClient.getConnection();
                    String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO usuarios (organizacao_id, email, senha_hash, nome, perfil) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                        ps.setObject(1, UUID.fromString(SessaoAtual.get().getOrgId()));
                        ps.setString(2, email);
                        ps.setString(3, senhaHash);
                        ps.setString(4, nome);
                        ps.setString(5, perfil);
                        ps.executeUpdate();
                    }
                } catch (Exception ex) {
                    erro = "Erro ao adicionar usuário: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (erro != null) {
                    lblErro.setText(erro);
                } else {
                    lblErro.setText(" ");
                    carregar();
                }
            }
        }.execute();
    }
}
