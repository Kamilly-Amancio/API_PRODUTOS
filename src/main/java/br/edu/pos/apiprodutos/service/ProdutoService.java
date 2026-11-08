package br.edu.pos.apiprodutos.service;

/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/

import br.edu.pos.apiprodutos.model.Produto;
import br.edu.pos.apiprodutos.repository.ProdutoRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class ProdutoService {

    private final ProdutoRepository repository;

    // Injeção de dependência pelo construtor
    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    // Mantido para não quebrar quem já usava: estoque inicial 0
    public Produto cadastrar(String nome, double preco) {
        return cadastrar(nome, preco, 0);
    }

    public Produto cadastrar(String nome, double preco, int estoqueInicial) {

        validarProduto(nome, preco);
        validarEstoqueInicial(estoqueInicial);

        Produto produto = new Produto(null, nome, preco, estoqueInicial);

        repository.salvar(produto);

        return produto;
    }

    public List<Produto> listar() {
        return repository.listarTodos();
    }

    public Produto buscarPorId(Long id) {

        return repository.buscarPorId(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Produto não encontrado: " + id
                        )
                );
    }

    public Produto atualizar(Long id, String nome, double preco) {

        validarProduto(nome, preco);

        Produto produto = buscarPorId(id);

        produto.setNome(nome);
        produto.setPreco(preco);

        repository.salvar(produto);

        return produto;
    }

    public void excluir(Long id) {

        buscarPorId(id);

        repository.excluir(id);
    }

    public Produto registrarEntrada(Long id, Integer quantidade) {

        validarQuantidadeMovimentacao(quantidade);

        boolean aplicado = repository.adicionarEstoque(id, quantidade);

        if (!aplicado) {
            // Lança 404 se o produto não existe
            buscarPorId(id);

            // Existe, então a soma estouraria o limite do tipo inteiro
            throw new IllegalArgumentException(
                    "A entrada ultrapassa o limite máximo de estoque."
            );
        }

        return buscarPorId(id);
    }

    public Produto registrarSaida(Long id, Integer quantidade) {

        validarQuantidadeMovimentacao(quantidade);

        boolean aplicado = repository.removerEstoque(id, quantidade);

        if (!aplicado) {
            // Lança 404 se o produto não existe
            Produto produto = buscarPorId(id);

            // Existe, então o saldo era insuficiente
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: "
                            + produto.getQuantidadeEstoque()
            );
        }

        return buscarPorId(id);
    }

    public List<Produto> listarEstoqueBaixo(Integer limite) {

        if (limite == null || limite < 0) {
            throw new IllegalArgumentException(
                    "O limite deve ser um número inteiro maior ou igual a zero."
            );
        }

        return repository.listarComEstoqueAte(limite);
    }

    private void validarProduto(String nome, double preco) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do produto é obrigatório."
            );
        }

        if (!Double.isFinite(preco) || preco <= 0) {
            throw new IllegalArgumentException(
                    "O preço deve ser maior que zero."
            );
        }
    }

    private void validarEstoqueInicial(int estoqueInicial) {

        if (estoqueInicial < 0) {
            throw new IllegalArgumentException(
                    "O estoque inicial não pode ser negativo."
            );
        }
    }

    private void validarQuantidadeMovimentacao(Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser um número inteiro maior que zero."
            );
        }
    }
}
