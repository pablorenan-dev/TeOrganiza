package com.teamteorganiza.eventos;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.eventos.model.Compromisso;
import com.teamteorganiza.eventos.model.TipoCompromisso;
import com.teamteorganiza.infra.SupabaseClient;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CompromissoRepositorySupabase implements CompromissoRepository {

    private String orgId() { return SessaoAtual.get().getOrgId(); }

    @Override
    public void salvar(Compromisso c) {
        String sql = """
            INSERT INTO compromissos (id, organizacao_id, titulo, tipo, categoria, data, horario, local, responsavel, descricao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              titulo      = EXCLUDED.titulo,
              tipo        = EXCLUDED.tipo,
              categoria   = EXCLUDED.categoria,
              data        = EXCLUDED.data,
              horario     = EXCLUDED.horario,
              local       = EXCLUDED.local,
              responsavel = EXCLUDED.responsavel,
              descricao   = EXCLUDED.descricao
            """;
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(c.getId()));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.setString(3, c.getTitulo());
            ps.setString(4, c.getTipo().name());
            ps.setString(5, c.getCategoria() != null ? c.getCategoria() : "");
            ps.setDate(6, Date.valueOf(c.getData()));
            ps.setString(7, c.getHorario() != null ? c.getHorario() : "");
            ps.setString(8, c.getLocal() != null ? c.getLocal() : "");
            ps.setString(9, c.getResponsavel() != null ? c.getResponsavel() : "");
            ps.setString(10, c.getDescricao() != null ? c.getDescricao() : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar compromisso: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Compromisso> buscarPorId(String id) {
        String sql = "SELECT * FROM compromissos WHERE id = ? AND organizacao_id = ?";
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
    public List<Compromisso> listarTodos() {
        String sql = "SELECT * FROM compromissos WHERE organizacao_id = ? ORDER BY data";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            List<Compromisso> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar compromissos: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String id) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM compromissos WHERE id = ? AND organizacao_id = ?")) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Compromisso mapear(ResultSet rs) throws SQLException {
        return new Compromisso(
            rs.getString("id"),
            rs.getString("titulo"),
            TipoCompromisso.valueOf(rs.getString("tipo")),
            rs.getString("categoria"),
            rs.getDate("data").toLocalDate(),
            rs.getString("horario"),
            rs.getString("local"),
            rs.getString("responsavel"),
            rs.getString("descricao")
        );
    }
}
