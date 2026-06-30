package com.teamteorganiza.auth;

import com.teamteorganiza.infra.SupabaseClient;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class 
LoginDialog extends JDialog {

    private boolean logado = false;

    public LoginDialog(Frame parent) {
        super(parent, "TeOrganiza — Login", true);
        setLayout(new BorderLayout(12, 12));
        setSize(380, 320);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JLabel lblTitulo = new JLabel("TeOrganiza Login", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 26f));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel campos = new JPanel(new GridLayout(4, 1, 4, 6));
        campos.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));

        JTextField     tfEmail = new JTextField();
        JPasswordField tfSenha = new JPasswordField();

        campos.add(new JLabel("E-mail:"));
        campos.add(tfEmail);
        campos.add(new JLabel("Senha:"));
        campos.add(tfSenha);

        JLabel lblErro = new JLabel(" ");
        lblErro.setForeground(Color.RED);
        lblErro.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel rodape = new JPanel(new GridLayout(3, 1, 4, 4));
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 28, 12, 28));

        JButton btnEntrar    = new JButton("Entrar");
        JButton btnCadastrar = new JButton("Criar conta");

        rodape.add(btnEntrar);
        rodape.add(btnCadastrar);
        rodape.add(lblErro);

        btnEntrar.addActionListener(e    -> entrar(tfEmail, tfSenha, lblErro, btnEntrar, btnCadastrar));
        btnCadastrar.addActionListener(e -> cadastrar(lblErro));

        add(lblTitulo, BorderLayout.NORTH);
        add(campos, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnEntrar);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    private void entrar(JTextField tfEmail, JPasswordField tfSenha,
                        JLabel lblErro, JButton btnEntrar, JButton btnCadastrar) {
        btnEntrar.setEnabled(false);
        btnCadastrar.setEnabled(false);
        lblErro.setText("Aguarde...");
        String email = tfEmail.getText().trim();
        String senha = new String(tfSenha.getPassword());

        new SwingWorker<Void, Void>() {
            private String erro;

            @Override
            protected Void doInBackground() {
                try {
                    String sql = """
                        SELECT id, organizacao_id, nome, perfil, senha_hash
                        FROM usuarios
                        WHERE email = ? AND ativo = true
                        """;
                    Connection conn = SupabaseClient.getConnection();
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, email);
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                            erro = "E-mail ou senha inválidos.";
                            return null;
                        }
                        String senhaHash = rs.getString("senha_hash");
                        if (!BCrypt.checkpw(senha, senhaHash)) {
                            erro = "E-mail ou senha inválidos.";
                            return null;
                        }
                        String orgId   = rs.getString("organizacao_id");
                        String nomeOrg = orgId;
                        try (PreparedStatement psOrg = conn.prepareStatement(
                                "SELECT nome FROM organizacoes WHERE id = ?")) {
                            psOrg.setObject(1, java.util.UUID.fromString(orgId));
                            ResultSet rsOrg = psOrg.executeQuery();
                            if (rsOrg.next()) nomeOrg = rsOrg.getString("nome");
                        }
                        SessaoAtual.get().iniciar(
                            rs.getString("id"),
                            orgId,
                            nomeOrg,
                            rs.getString("nome"),
                            rs.getString("perfil")
                        );
                    }
                } catch (Exception ex) {
                    erro = "Erro de conexão: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (erro != null) {
                    lblErro.setText(erro);
                    btnEntrar.setEnabled(true);
                    btnCadastrar.setEnabled(true);
                } else {
                    logado = true;
                    dispose();
                }
            }
        }.execute();
    }

    // ── Cadastro de nova org + usuário admin ──────────────────────────────────

    private void cadastrar(JLabel lblErro) {
        JTextField tfOrg   = new JTextField();
        JTextField tfNome  = new JTextField();
        JTextField tfEmail = new JTextField();
        JPasswordField tfSenha    = new JPasswordField();
        JPasswordField tfConfirma = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Nome da organização:"));  form.add(tfOrg);
        form.add(new JLabel("Seu nome:"));              form.add(tfNome);
        form.add(new JLabel("E-mail:"));                form.add(tfEmail);
        form.add(new JLabel("Senha:"));                 form.add(tfSenha);
        form.add(new JLabel("Confirmar senha:"));       form.add(tfConfirma);

        int op = JOptionPane.showConfirmDialog(this, form,
            "Criar conta — novo administrador",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;

        String org   = tfOrg.getText().trim();
        String nome  = tfNome.getText().trim();
        String email = tfEmail.getText().trim();
        String senha = new String(tfSenha.getPassword());
        String conf  = new String(tfConfirma.getPassword());

        if (org.isEmpty() || nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            lblErro.setText("Preencha todos os campos.");
            return;
        }
        if (!senha.equals(conf)) {
            lblErro.setText("As senhas não coincidem.");
            return;
        }

        lblErro.setText("Criando conta...");
        new SwingWorker<Void, Void>() {
            private String erro;

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = SupabaseClient.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        String orgId;
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO organizacoes (nome) VALUES (?) RETURNING id")) {
                            ps.setString(1, org);
                            ResultSet rs = ps.executeQuery();
                            rs.next();
                            orgId = rs.getString("id");
                        }
                        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO usuarios (organizacao_id, email, senha_hash, nome, perfil) " +
                                "VALUES (?, ?, ?, ?, 'ADMIN')")) {
                            ps.setObject(1, java.util.UUID.fromString(orgId));
                            ps.setString(2, email);
                            ps.setString(3, senhaHash);
                            ps.setString(4, nome);
                            ps.executeUpdate();
                        }
                        conn.commit();
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("organizacoes_nome_unico")) {
                        erro = "Já existe uma organização com esse nome.";
                    } else if (msg != null && msg.contains("usuarios_email_key")) {
                        erro = "Esse e-mail já está cadastrado.";
                    } else {
                        erro = "Erro ao criar conta: " + msg;
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (erro != null) {
                    lblErro.setText(erro);
                } else {
                    lblErro.setText("Conta criada! Faça login.");
                }
            }
        }.execute();
    }

    public boolean isLogado() { return logado; }
}
