package br.edu.pos.apiprodutos.repository;

import br.edu.pos.apiprodutos.model.Produto;
import java.util.List;
import java.util.Optional;

/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/

public interface ProdutoRepository {

    void salvar(Produto produto);

    List<Produto> listarTodos();

    Optional<Produto> buscarPorId(Long id);

    void excluir(Long id);

    /**
     * Soma unidades ao estoque de forma atômica.
     *
     * @return true se a entrada foi aplicada; false se o produto não existe
     *         ou se a soma ultrapassaria o limite do tipo int.
     */
    boolean adicionarEstoque(Long id, int quantidade);

    /**
     * Subtrai unidades do estoque de forma atômica, somente se houver saldo
     * suficiente (o saldo nunca fica negativo, nem com acessos concorrentes).
     *
     * @return true se a saída foi aplicada; false se o produto não existe
     *         ou se o saldo é insuficiente.
     */
    boolean removerEstoque(Long id, int quantidade);

    /**
     * Lista produtos cujo estoque é menor ou igual ao limite informado.
     */
    List<Produto> listarComEstoqueAte(int limite);
}
