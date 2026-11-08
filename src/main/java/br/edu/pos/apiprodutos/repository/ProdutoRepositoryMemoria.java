package br.edu.pos.apiprodutos.repository;

/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/

import br.edu.pos.apiprodutos.model.Produto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProdutoRepositoryMemoria implements ProdutoRepository {

    private final List<Produto> produtos = new ArrayList<>();

    @Override
    public synchronized void salvar(Produto produto) {

        produtos.removeIf(p -> p.getId().equals(produto.getId()));

        produtos.add(produto);
    }

    @Override
    public synchronized List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    @Override
    public synchronized Optional<Produto> buscarPorId(Long id) {

        return produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized void excluir(Long id) {

        produtos.removeIf(p -> p.getId().equals(id));
    }

    @Override
    public synchronized boolean adicionarEstoque(Long id, int quantidade) {

        Optional<Produto> encontrado = buscarPorId(id);

        if (encontrado.isEmpty()) {
            return false;
        }

        Produto produto = encontrado.get();

        if (produto.getQuantidadeEstoque() > Integer.MAX_VALUE - quantidade) {
            return false;
        }

        produto.setQuantidadeEstoque(
                produto.getQuantidadeEstoque() + quantidade
        );

        return true;
    }

    @Override
    public synchronized boolean removerEstoque(Long id, int quantidade) {

        Optional<Produto> encontrado = buscarPorId(id);

        if (encontrado.isEmpty()
                || encontrado.get().getQuantidadeEstoque() < quantidade) {
            return false;
        }

        Produto produto = encontrado.get();

        produto.setQuantidadeEstoque(
                produto.getQuantidadeEstoque() - quantidade
        );

        return true;
    }

    @Override
    public synchronized List<Produto> listarComEstoqueAte(int limite) {

        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() <= limite)
                .sorted(Comparator
                        .comparingInt(Produto::getQuantidadeEstoque)
                        .thenComparing(Produto::getId))
                .toList();
    }
}
