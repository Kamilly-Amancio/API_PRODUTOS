# API de Produtos: Controle de Estoque

Desafio Prático Final: evolução da API de Produtos com implementação de
controle de estoque, usando Java, Javalin, JDBC e H2.

## Descrição

A API já oferecia as operações REST de cadastro, consulta, atualização e
exclusão de produtos. Este projeto a evolui com **controle de unidades
disponíveis em estoque**, preservando a arquitetura existente
(Controller, Service e Repository) e aplicando orientação a objetos,
interfaces, princípios SOLID, REST, SQL e JDBC.

## Tecnologias utilizadas

- Java 21
- Javalin 6.7.0 (framework da API REST)
- JDBC (acesso ao banco de dados)
- H2 Database 2.3.232 (banco de dados)
- Jackson (serialização JSON)
- Swagger / OpenAPI (documentação dos endpoints)
- SLF4J Simple (logs)
- Maven (gerenciamento de dependências e build)
- Git e GitHub

## Arquitetura

O projeto é organizado em camadas:

- **Controller**: recebe as requisições HTTP e devolve as respostas.
- **Service**: concentra as regras de negócio (incluindo as regras de estoque).
- **Repository**: acesso ao banco via JDBC.

## Regras de estoque implementadas

- Produtos possuem o campo `quantidadeEstoque`.
- Não é permitido cadastrar produto com estoque negativo.
- Endpoint para adicionar unidades ao estoque.
- Endpoint para remover unidades (baixa), bloqueando quando o estoque for insuficiente.

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/produtos` | Lista os produtos |
| GET | `/produtos/{id}` | Busca produto por id |
| POST | `/produtos` | Cadastra produto |
| PUT | `/produtos/{id}` | Atualiza produto |
| DELETE | `/produtos/{id}` | Remove produto |
| [AJUSTE] | [rota de estoque] | [descrição] |

## Autor:
KAMILLY AMANCIO

## Disciplina: 
Projeto e Implementacao Orientado a Objetos

