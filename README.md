# Desafio Prático Final: Evolução da API de Produtos

**Tema:** Implementação de Controle de Estoque com Java, Javalin, JDBC e H2

A API de Produtos já disponibilizava operações REST de cadastro, consulta,
atualização e exclusão, organizadas em Controller, Service e Repository, com
persistência em H2 via JDBC e documentação Swagger. Faltava o controle de
unidades disponíveis. Este projeto evolui a aplicação existente, preservando
a arquitetura, e implementa as regras de estoque.

Conceitos aplicados: orientação a objetos, interfaces, SOLID, separação de
responsabilidades, REST, SQL e JDBC.

## Tecnologias utilizadas

- Java 21
- Javalin 6.7.0 (API REST)
- JDBC com `PreparedStatement` (acesso ao banco)
- H2 Database 2.3.232 (banco em arquivo)
- Jackson 2.18.3 (serialização JSON)
- Javalin OpenAPI e Swagger plugins 6.7.0 (documentação dos endpoints)
- SLF4J Simple 2.0.17 (logs)
- Maven (dependências e build)
- Git e GitHub

## Arquitetura

| Camada | Classe(s) | Responsabilidade |
|--------|-----------|------------------|
| Controller | `ProdutoController`, `MovimentacaoEstoqueRequest` | Receber requisições HTTP, converter JSON e devolver os códigos de status |
| Service | `ProdutoService` | Regras de negócio e validações |
| Repository | `ProdutoRepository` (interface), `ProdutoRepositoryJDBC`, `ProdutoRepositoryMemoria` | Acesso a dados |
| Config | `Database` | Conexão e inicialização/migração do esquema |

O `ProdutoService` depende apenas da interface `ProdutoRepository`
(injeção de dependência pelo construtor), e o acesso real ao banco fica em
`ProdutoRepositoryJDBC`.

## Endpoints

| Método | Rota | Descrição | Respostas |
|--------|------|-----------|-----------|
| GET | `/produtos` | Lista todos os produtos | 200 |
| GET | `/produtos/{id}` | Busca produto por ID | 200, 404 |
| POST | `/produtos` | Cadastra produto (aceita estoque inicial) | 201, 400 |
| PUT | `/produtos/{id}` | Atualiza nome e preço | 200, 400, 404 |
| DELETE | `/produtos/{id}` | Exclui produto | 204, 404 |
| POST | `/produtos/{id}/estoque/entrada` | Soma unidades ao estoque | 200, 400, 404 |
| POST | `/produtos/{id}/estoque/saida` | Subtrai unidades do estoque | 200, 400, 404 |
| GET | `/produtos/estoque-baixo?limite=5` | Lista produtos com estoque menor ou igual ao limite | 200, 400 |

### Exemplos de corpo (JSON)

Cadastro (o campo `quantidadeEstoque` é opcional e vale 0 se omitido):

```json
{ "nome": "Teclado", "preco": 150.0, "quantidadeEstoque": 10 }
```

Entrada ou saída de estoque:

```json
{ "quantidade": 10 }
```

Resposta de erro (400 ou 404):

```json
{ "mensagem": "Estoque insuficiente. Disponível: 3" }
```

## Regras de estoque

- O estoque inicial no cadastro deve ser um inteiro maior ou igual a zero.
- Entradas e saídas exigem quantidade inteira maior que zero.
- Uma saída maior que o saldo disponível é rejeitada (400).
- Produto inexistente retorna 404.
- O `PUT /produtos/{id}` altera apenas nome e preço. O estoque só muda pelas
  operações de entrada e saída, para que uma atualização com dados antigos
  nunca sobrescreva o saldo.

### Consistência em acessos concorrentes

A atualização do saldo é feita por **um único `UPDATE` atômico**, e o cálculo
e a verificação ficam no próprio banco, sem ler e depois gravar na aplicação:

```sql
-- saída
UPDATE produtos
SET quantidade_estoque = quantidade_estoque - ?
WHERE id = ? AND quantidade_estoque >= ?
```

Se duas saídas simultâneas disputarem o mesmo saldo, a segunda é reavaliada
pelo banco depois da primeira e, sem saldo suficiente, não altera nenhuma
linha, então o estoque nunca fica negativo. Como segunda barreira, a tabela
possui a constraint `CHECK (quantidade_estoque >= 0)`. A entrada usa a mesma
técnica e também impede o estouro do limite do tipo `int`.

## Estratégia de atualização do esquema do banco

O comando `CREATE TABLE IF NOT EXISTS` **não adiciona colunas** a uma tabela
que já existe. Por isso, ao iniciar, a classe `Database` executa, em ordem:

1. `CREATE TABLE IF NOT EXISTS produtos (...)`, que já inclui a coluna
   `quantidade_estoque` para bancos novos;
2. `ALTER TABLE produtos ADD COLUMN IF NOT EXISTS quantidade_estoque INT DEFAULT 0 NOT NULL`,
   que adiciona a coluna em bancos antigos (criados antes do controle de estoque);
3. `ALTER TABLE produtos ADD CONSTRAINT IF NOT EXISTS ck_produtos_estoque_nao_negativo CHECK (quantidade_estoque >= 0)`.

Todos os comandos são **idempotentes**:

- **banco novo:** a coluna nasce no `CREATE TABLE` e o `ALTER` não faz nada;
- **banco antigo (sem estoque):** o `ALTER` adiciona a coluna e os produtos já
  cadastrados ficam com estoque 0 (`DEFAULT 0`);
- **reinício da API:** nenhum comando altera nada.

Assim, nenhum dado existente é perdido e não é preciso apagar o banco.

## Como executar

Pré-requisitos: **Java 21** e **Maven** (ou uma IDE como o IntelliJ IDEA).

1. Clone o repositório:
   ```bash
   git clone https://github.com/Kamilly-Amancio/API_PRODUTOS.git
   cd API_PRODUTOS
   ```
2. Abra o projeto na IDE (ele é um projeto Maven), aguarde o download das
   dependências e execute a classe `br.edu.pos.apiprodutos.Main`.

Ao subir, a aplicação cria a pasta `dados/` com o banco H2 em arquivo. O
estoque continua salvo depois de reiniciar a API.

| Recurso | Endereço |
|---------|----------|
| API | http://localhost:7000 |
| Swagger (documentação e testes) | http://localhost:7000/swagger |
| Definição OpenAPI | http://localhost:7000/openapi |
| Console do H2 | http://localhost:8082 |

Para entrar no console do H2, use a URL JDBC `jdbc:h2:file:./dados/produtos`,
usuário `sa` e senha em branco.

> A pasta `dados/` está no `.gitignore`, então o arquivo do banco não é
> versionado. Cada pessoa que rodar o projeto gera o seu próprio banco.

## Testando rapidamente (curl)

```bash
# cadastrar com estoque inicial
curl -X POST http://localhost:7000/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teclado","preco":150.0,"quantidadeEstoque":10}'

# entrada de 5 unidades
curl -X POST http://localhost:7000/produtos/1/estoque/entrada \
  -H "Content-Type: application/json" -d '{"quantidade":5}'

# saída de 3 unidades
curl -X POST http://localhost:7000/produtos/1/estoque/saida \
  -H "Content-Type: application/json" -d '{"quantidade":3}'

# saída maior que o estoque (esperado: 400)
curl -i -X POST http://localhost:7000/produtos/1/estoque/saida \
  -H "Content-Type: application/json" -d '{"quantidade":999}'

# produtos com estoque menor ou igual a 5
curl "http://localhost:7000/produtos/estoque-baixo?limite=5"
```

## Autora

Kamilly Amancio Batista (matrícula 202609091751)

## Disciplina

Projeto e Implementação Orientada a Objetos
