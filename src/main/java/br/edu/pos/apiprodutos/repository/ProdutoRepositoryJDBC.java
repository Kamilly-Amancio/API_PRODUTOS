
package br.edu.pos.apiprodutos.repository;
/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/


import br.edu.pos.apiprodutos.config.Database;
import br.edu.pos.apiprodutos.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoRepositoryJDBC implements ProdutoRepository {

    private static final String COLUNAS =
            "id, nome, preco, quantidade_estoque";

    @Override
    public void salvar(Produto produto) {

        if (produto.getId() == null) {
            inserir(produto);
        } else {
            atualizar(produto);
        }
    }

    private void inserir(Produto produto) {

        String sql = """
                INSERT INTO produtos (nome, preco, quantidade_estoque)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            comando.setString(1, produto.getNome());
            comando.setDouble(2, produto.getPreco());
            comando.setInt(3, produto.getQuantidadeEstoque());

            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {

                if (chaves.next()) {
                    produto.setId(chaves.getLong(1));
                } else {
                    throw new SQLException(
                            "O banco não retornou o ID do produto"
                    );
                }
            }

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao inserir produto",
                    erro
            );
        }
    }

    /**
     * Atualiza apenas nome e preço. O estoque NÃO é alterado aqui de
     * propósito: ele só muda pelas operações de entrada/saída, que são
     * atômicas. Assim um PUT com dados antigos nunca sobrescreve o saldo.
     */
    private void atualizar(Produto produto) {

        String sql = """
                UPDATE produtos
                SET nome = ?, preco = ?
                WHERE id = ?
                """;

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {

            comando.setString(1, produto.getNome());
            comando.setDouble(2, produto.getPreco());
            comando.setLong(3, produto.getId());

            comando.executeUpdate();

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao atualizar produto",
                    erro
            );
        }
    }

    @Override
    public List<Produto> listarTodos() {

        List<Produto> produtos = new ArrayList<>();

        String sql = "SELECT " + COLUNAS + " FROM produtos ORDER BY id";

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql);
                ResultSet resultado = comando.executeQuery()
        ) {

            while (resultado.next()) {
                produtos.add(converterProduto(resultado));
            }

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao listar produtos",
                    erro
            );
        }

        return produtos;
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {

        String sql = "SELECT " + COLUNAS + " FROM produtos WHERE id = ?";

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return Optional.of(converterProduto(resultado));
                }
            }

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao buscar produto",
                    erro
            );
        }

        return Optional.empty();
    }

    @Override
    public void excluir(Long id) {

        String sql = "DELETE FROM produtos WHERE id = ?";

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {

            comando.setLong(1, id);
            comando.executeUpdate();

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao excluir produto",
                    erro
            );
        }
    }

    /**
     * UPDATE único e atômico: o saldo é calculado pelo próprio banco
     * (quantidade_estoque + ?), sem ler-e-depois-gravar na aplicação.
     * A condição do WHERE impede estourar o limite do tipo INT.
     */
    @Override
    public boolean adicionarEstoque(Long id, int quantidade) {

        String sql = """
                UPDATE produtos
                SET quantidade_estoque = quantidade_estoque + ?
                WHERE id = ? AND quantidade_estoque <= ?
                """;

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {

            comando.setInt(1, quantidade);
            comando.setLong(2, id);
            comando.setInt(3, Integer.MAX_VALUE - quantidade);

            return comando.executeUpdate() == 1;

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao registrar entrada de estoque",
                    erro
            );
        }
    }

    /**
     * UPDATE único e atômico com a verificação de saldo dentro do WHERE.
     * Duas saídas simultâneas não conseguem, juntas, deixar o saldo
     * negativo: a segunda é reavaliada pelo banco após a primeira e
     * simplesmente não atualiza nenhuma linha. A constraint CHECK da tabela
     * é uma segunda barreira.
     */
    @Override
    public boolean removerEstoque(Long id, int quantidade) {

        String sql = """
                UPDATE produtos
                SET quantidade_estoque = quantidade_estoque - ?
                WHERE id = ? AND quantidade_estoque >= ?
                """;

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {

            comando.setInt(1, quantidade);
            comando.setLong(2, id);
            comando.setInt(3, quantidade);

            return comando.executeUpdate() == 1;

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao registrar saída de estoque",
                    erro
            );
        }
    }

    @Override
    public List<Produto> listarComEstoqueAte(int limite) {

        List<Produto> produtos = new ArrayList<>();

        String sql = "SELECT " + COLUNAS + """
                 FROM produtos
                WHERE quantidade_estoque <= ?
                ORDER BY quantidade_estoque, id
                """;

        try (
                Connection conexao = Database.conectar();
                PreparedStatement comando = conexao.prepareStatement(sql)
        ) {

            comando.setInt(1, limite);

            try (ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {
                    produtos.add(converterProduto(resultado));
                }
            }

        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao listar produtos com estoque baixo",
                    erro
            );
        }

        return produtos;
    }

    private Produto converterProduto(ResultSet resultado)
            throws SQLException {

        return new Produto(
                resultado.getLong("id"),
                resultado.getString("nome"),
                resultado.getDouble("preco"),
                resultado.getInt("quantidade_estoque")
        );
    }
}
