
package br.edu.pos.apiprodutos.controller;
/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/
import br.edu.pos.apiprodutos.model.Produto;
import br.edu.pos.apiprodutos.service.ProdutoService;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.http.HttpStatus;

import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiParam;

import java.util.NoSuchElementException;

public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    public void registrarRotas(Javalin app) {

        app.get("/produtos", this::listarProdutos);

        // Precisa vir antes de "/produtos/{id}", senão o Javalin
        // trataria "estoque-baixo" como se fosse um ID.
        app.get("/produtos/estoque-baixo", this::listarEstoqueBaixo);

        app.get("/produtos/{id}", this::buscarProduto);

        app.post("/produtos", this::cadastrarProduto);

        app.post("/produtos/{id}/estoque/entrada", this::registrarEntrada);

        app.post("/produtos/{id}/estoque/saida", this::registrarSaida);

        app.put("/produtos/{id}", this::atualizarProduto);

        app.delete("/produtos/{id}", this::excluirProduto);

        app.exception(NoSuchElementException.class, (erro, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new MensagemErro(erro.getMessage()));
        });


        app.exception(IllegalArgumentException.class, (erro, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new MensagemErro(erro.getMessage()));
        });
    }

    @OpenApi(
            path = "/produtos",
            methods = {HttpMethod.GET},
            summary = "Listar todos os produtos",
            tags = {"Produtos"},
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {
                                    @OpenApiContent(from = Produto[].class)
                            }
                    )
            }
    )
    public void listarProdutos(Context ctx) {
        ctx.json(service.listar());
    }


    @OpenApi(
            path = "/produtos/{id}",
            methods = {HttpMethod.GET},
            summary = "Buscar produto por ID",
            tags = {"Produtos"},
            pathParams = {
                    @OpenApiParam(
                            name = "id",
                            type = Long.class,
                            description = "Identificador do produto",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {
                                    @OpenApiContent(from = Produto.class)
                            }
                    ),
                    @OpenApiResponse(status = "404")
            }
    )
    public void buscarProduto(Context ctx) {

        Long id = lerId(ctx);

        Produto produto = service.buscarPorId(id);

        ctx.json(produto);
    }


    @OpenApi(
            path = "/produtos",
            methods = {HttpMethod.POST},
            summary = "Cadastrar um novo produto",
            tags = {"Produtos"},
            requestBody = @OpenApiRequestBody(
                    content = {
                            @OpenApiContent(from = Produto.class)
                    }
            ),
            responses = {
                    @OpenApiResponse(
                            status = "201",
                            content = {
                                    @OpenApiContent(from = Produto.class)
                            }
                    ),
                    @OpenApiResponse(status = "400")
            }
    )
    public void cadastrarProduto(Context ctx) {

        Produto dados = lerCorpo(ctx, Produto.class);

        Produto produto = service.cadastrar(
                dados.getNome(),
                dados.getPreco(),
                dados.getQuantidadeEstoque()
        );

        ctx.status(HttpStatus.CREATED);
        ctx.json(produto);
    }


    @OpenApi(
            path = "/produtos/{id}",
            methods = {HttpMethod.PUT},
            summary = "Atualizar um produto existente",
            tags = {"Produtos"},
            pathParams = {
                    @OpenApiParam(
                            name = "id",
                            type = Long.class,
                            description = "Identificador do produto",
                            required = true
                    )
            },
            requestBody = @OpenApiRequestBody(
                    content = {
                            @OpenApiContent(from = Produto.class)
                    }
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {
                                    @OpenApiContent(from = Produto.class)
                            }
                    ),
                    @OpenApiResponse(status = "400"),
                    @OpenApiResponse(status = "404")
            }
    )
    public void atualizarProduto(Context ctx) {

        Long id = lerId(ctx);

        Produto dados = lerCorpo(ctx, Produto.class);

        Produto atualizado = service.atualizar(
                id,
                dados.getNome(),
                dados.getPreco()
        );

        ctx.json(atualizado);
    }


    @OpenApi(
            path = "/produtos/{id}",
            methods = {HttpMethod.DELETE},
            summary = "Excluir um produto",
            tags = {"Produtos"},
            pathParams = {
                    @OpenApiParam(
                            name = "id",
                            type = Long.class,
                            description = "Identificador do produto",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "404")
            }
    )
    public void excluirProduto(Context ctx) {

        Long id = lerId(ctx);

        service.excluir(id);

        ctx.status(HttpStatus.NO_CONTENT);
    }

    @OpenApi(
            path = "/produtos/{id}/estoque/entrada",
            methods = {HttpMethod.POST},
            summary = "Registrar entrada de unidades no estoque",
            tags = {"Estoque"},
            pathParams = {
                    @OpenApiParam(
                            name = "id",
                            type = Long.class,
                            description = "Identificador do produto",
                            required = true
                    )
            },
            requestBody = @OpenApiRequestBody(
                    content = {
                            @OpenApiContent(from = MovimentacaoEstoqueRequest.class)
                    }
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {
                                    @OpenApiContent(from = Produto.class)
                            }
                    ),
                    @OpenApiResponse(status = "400"),
                    @OpenApiResponse(status = "404")
            }
    )
    public void registrarEntrada(Context ctx) {

        Long id = lerId(ctx);

        MovimentacaoEstoqueRequest dados =
                lerCorpo(ctx, MovimentacaoEstoqueRequest.class);

        ctx.json(service.registrarEntrada(id, dados.getQuantidade()));
    }


    @OpenApi(
            path = "/produtos/{id}/estoque/saida",
            methods = {HttpMethod.POST},
            summary = "Registrar saída de unidades do estoque",
            tags = {"Estoque"},
            pathParams = {
                    @OpenApiParam(
                            name = "id",
                            type = Long.class,
                            description = "Identificador do produto",
                            required = true
                    )
            },
            requestBody = @OpenApiRequestBody(
                    content = {
                            @OpenApiContent(from = MovimentacaoEstoqueRequest.class)
                    }
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {
                                    @OpenApiContent(from = Produto.class)
                            }
                    ),
                    @OpenApiResponse(status = "400"),
                    @OpenApiResponse(status = "404")
            }
    )
    public void registrarSaida(Context ctx) {

        Long id = lerId(ctx);

        MovimentacaoEstoqueRequest dados =
                lerCorpo(ctx, MovimentacaoEstoqueRequest.class);

        ctx.json(service.registrarSaida(id, dados.getQuantidade()));
    }


    @OpenApi(
            path = "/produtos/estoque-baixo",
            methods = {HttpMethod.GET},
            summary = "Listar produtos com estoque menor ou igual ao limite",
            tags = {"Estoque"},
            queryParams = {
                    @OpenApiParam(
                            name = "limite",
                            type = Integer.class,
                            description = "Estoque máximo (inclusive) para listar",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {
                                    @OpenApiContent(from = Produto[].class)
                            }
                    ),
                    @OpenApiResponse(status = "400")
            }
    )
    public void listarEstoqueBaixo(Context ctx) {

        String texto = ctx.queryParam("limite");

        if (texto == null) {
            throw new IllegalArgumentException(
                    "O parâmetro 'limite' é obrigatório."
            );
        }

        int limite;

        try {
            limite = Integer.parseInt(texto);
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException(
                    "O limite deve ser um número inteiro."
            );
        }

        ctx.json(service.listarEstoqueBaixo(limite));
    }


    private Long lerId(Context ctx) {

        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("ID inválido.");
        }
    }

    private <T> T lerCorpo(Context ctx, Class<T> tipo) {

        try {
            return ctx.bodyAsClass(tipo);
        } catch (Exception erro) {
            throw new IllegalArgumentException(
                    "Corpo da requisição inválido."
            );
        }
    }

    public record MensagemErro(String mensagem) {
    }
}
