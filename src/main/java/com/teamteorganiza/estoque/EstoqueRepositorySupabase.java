package com.teamteorganiza.estoque;

import com.teamteorganiza.auth.SessaoAtual;
import com.teamteorganiza.estoque.model.Produto;
import com.teamteorganiza.estoque.model.UnidadeMedida;
import com.teamteorganiza.infra.SupabaseClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EstoqueRepositorySupabase implements EstoqueRepository {

    private String orgId() { return SessaoAtual.get().getOrgId(); }

    @Override
    public void salvar(Produto p) {
        String sql = """
            INSERT INTO produtos (id, organizacao_id, nome, categoria, unidade, quantidade, estoque_minimo, custo_medio, preco_venda)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              nome           = EXCLUDED.nome,
              categoria      = EXCLUDED.categoria,
              unidade        = EXCLUDED.unidade,
              quantidade     = EXCLUDED.quantidade,
              estoque_minimo = EXCLUDED.estoque_minimo,
              custo_medio    = EXCLUDED.custo_medio,
              preco_venda    = EXCLUDED.preco_venda
            """;
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(p.getId()));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.setString(3, p.getNome());
            ps.setString(4, p.getCategoria());
            ps.setString(5, p.getUnidade().name());
            ps.setDouble(6, p.getQuantidade());
            ps.setDouble(7, p.getEstoqueMinimo());
            ps.setDouble(8, p.getCustoMedio());
            ps.setDouble(9, p.getPrecoVenda());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(String id) {
        String sql = "SELECT * FROM produtos WHERE id = ? AND organizacao_id = ?";
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
    public Optional<Produto> buscarPorNome(String nome) {
        return listarTodos().stream().filter(p -> p.getNome().equalsIgnoreCase(nome)).findFirst();
    }

    @Override
    public List<Produto> listarTodos() {
        String sql = "SELECT * FROM produtos WHERE organizacao_id = ? ORDER BY nome";
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(orgId()));
            ResultSet rs = ps.executeQuery();
            List<Produto> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage(), e);
        }
    }

    @Override
    public void remover(String id) {
        try (PreparedStatement ps = SupabaseClient.getConnection().prepareStatement(
                "DELETE FROM produtos WHERE id = ? AND organizacao_id = ?")) {
            ps.setObject(1, UUID.fromString(id));
            ps.setObject(2, UUID.fromString(orgId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("categoria"),
            UnidadeMedida.valueOf(rs.getString("unidade")),
            rs.getDouble("quantidade"),
            rs.getDouble("estoque_minimo"),
            rs.getDouble("custo_medio"),
            rs.getDouble("preco_venda")
        );
    }
}
