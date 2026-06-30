package com.teamteorganiza.financeiro;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.financeiro.model.MovimentacaoFinanceira;
import com.teamteorganiza.financeiro.model.TipoLancamento;
import com.teamteorganiza.infra.SupabaseClient;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MovimentacaoFinanceiraRepositorySupabase implements MovimentacaoFinanceiraRepository {

    private String orgId() { return SessaoAtual.get().getOrgId(); }

    @Override
    public void salvar(MovimentacaoFinanceira m) {
        String sql = """
            INSERT INTO movimentacoes (id, organizacao_id, pessoa_id, descricao, valor, data, tipo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              pessoa_id = EXCLUDED.pessoa_id,
              descricao = EXCLUDED.descricao,
              valor     = EXCLUDED.valor,
              tipo      = EXCLUDED.tipo
            """;
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(m.getId()));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.setObject(3, m.getPessoaId() != null && !m.getPessoaId().isEmpty()
                ? UUID.fromString(m.getPessoaId()) : null);
            ps.setString(4, m.getDescricao());
            ps.setDouble(5, m.getValor());
            ps.setDate(6, Date.valueOf(m.getData()));
            ps.setString(7, m.getTipo().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MovimentacaoFinanceira> buscarPorId(String id) {
        String sql = "SELECT * FROM movimentacoes WHERE id = ? AND organizacao_id = ?";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MovimentacaoFinanceira> listarTodos() {
        String sql = "SELECT * FROM movimentacoes WHERE organizacao_id = ? ORDER BY data";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            List<MovimentacaoFinanceira> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar movimentações: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String id) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM movimentacoes WHERE id = ? AND organizacao_id = ?")) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MovimentacaoFinanceira mapear(ResultSet rs) throws SQLException {
        String pessoaId = rs.getString("pessoa_id");
        return new MovimentacaoFinanceira(
            rs.getString("id"),
            pessoaId != null ? pessoaId : "",
            rs.getString("descricao"),
            rs.getDouble("valor"),
            rs.getDate("data").toLocalDate(),
            TipoLancamento.valueOf(rs.getString("tipo"))
        );
    }
}
