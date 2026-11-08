package br.edu.pos.apiprodutos.model;
/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/
public class Produto {

    private Long id;
    private String nome;
    private double preco;
    private int quantidadeEstoque;

    public Produto() {
    }

    // Construtor antigo preservado: produto sem estoque informado começa com 0
    public Produto(Long id, String nome, double preco) {
        this(id, nome, preco, 0);
    }

    public Produto(Long id, String nome, double preco, int quantidadeEstoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
}
