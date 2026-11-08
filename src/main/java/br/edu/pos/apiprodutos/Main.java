
package br.edu.pos.apiprodutos;
/*
* @NOME: KAMILLY AMANCIO BATISTA
* @MATRICULA: 202609091751
*
*
*/

import br.edu.pos.apiprodutos.config.Database;
import br.edu.pos.apiprodutos.controller.ProdutoController;
import br.edu.pos.apiprodutos.repository.ProdutoRepository;
import br.edu.pos.apiprodutos.repository.ProdutoRepositoryJDBC;
import br.edu.pos.apiprodutos.service.ProdutoService;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import org.h2.tools.Server;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {


        Database.inicializar();


        Server h2Console = Server.createWebServer(
                "-webPort", "8082"
        ).start();

        System.out.println(
                "Console H2: http://localhost:8082"
        );


        ProdutoRepository repository =
                new ProdutoRepositoryJDBC();

        ProdutoService service =
                new ProdutoService(repository);

        ProdutoController controller =
                new ProdutoController(service);


        Javalin app = Javalin.create(config -> {

            config.registerPlugin(
                    new OpenApiPlugin(openApi -> {
                        openApi.withDefinitionConfiguration(
                                (version, definition) -> {
                                    definition.withInfo(info -> {
                                        info.setTitle("API de Produtos");
                                        info.setVersion("1.0.0");
                                    });
                                }
                        );
                    })
            );

            config.registerPlugin(new SwaggerPlugin());
        });


        controller.registrarRotas(app);

        app.get("/", ctx ->
                ctx.result("API de Produtos funcionando!")
        );


        app.start(7000);

        System.out.println("API: http://localhost:7000");
        System.out.println("OpenAPI: http://localhost:7000/openapi");
        System.out.println("Swagger: http://localhost:7000/swagger");
    }
}
