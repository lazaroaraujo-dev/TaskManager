# TaskManager

> **Gerenciador de Tarefas** — API REST construída com **Java 21 + Spring Boot** e persistência em banco **NoSQL chave-valor (Valkey / Redis)**.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Valkey](https://img.shields.io/badge/Valkey-NoSQL-1D4ED8?style=for-the-badge&logo=redis&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## Sumário

- [Sobre o projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Modelo de dados (Valkey)](#-modelo-de-dados-valkey)
- [Regras de negócio](#-regras-de-negócio)
- [Endpoints da API](#-endpoints-da-api)
- [Como executar](#-como-executar)
- [Documentação interativa (Swagger)](#-documentação-interativa-swagger)
- [Exemplos de uso](#-exemplos-de-uso)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Testes](#-testes)
- [Melhorias futuras](#-melhorias-futuras)

---

## Sobre o projeto

O **TaskManager** é um gerenciador de tarefas que explora um **banco NoSQL de chave-valor** em vez de um banco relacional tradicional. Todo o estado da aplicação (usuários, projetos e tarefas) é persistido no **Valkey** (um fork open-source do Redis) usando estruturas nativas como *Hashes* e *Sets*, o que **elimina a necessidade de ORM/JPA** e demonstra o uso direto de drivers de acesso.

O projeto foi desenvolvido como trabalho acadêmico com foco em:

- Modelagem de dados em NoSQL chave-valor;
- Uso de índices secundários manuais (Sets) para consultas reversas;
- Boas práticas de API REST (DTOs, validação de entrada, camadas bem definidas).

---

## Funcionalidades

- ✅ **CRUD completo de Usuários** (criação, busca, listagem e exclusão)
- ✅ **Gestão de Projetos** por usuário
- ✅ **Gestão de Tarefas** com associação opcional a projeto e tags
- ✅ **Conclusão de tarefas** via endpoint dedicado (`PATCH`) com mudança de status
- ✅ **Validação de entrada** com Bean Validation (`@NotBlank`, `@Email`, `@Size`)
- ✅ **TTL (Time To Live)** opcional por tarefa, com expiração automática no banco
- ✅ **Índices secundários** para listagens por usuário e por projeto
- ✅ **Documentação interativa** com Swagger UI
- ✅ **Infraestrutura automatizada** via Docker Compose

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Web MVC) |
| Banco de dados | Valkey (Redis-compatível), driver Lettuce |
| Persistência | `StringRedisTemplate` (Hashes, Sets, Strings) |
| Validação | Jakarta Bean Validation |
| Documentação | springdoc-openapi (Swagger UI 3.1.0) |
| Utilities | Lombok |
| Build | Maven |
| Infra | Docker / Docker Compose |

---

## Arquitetura

Aplicação em **camadas**, com dependências apontando sempre "de fora para dentro":

```
┌─────────────────────────────────────────────────────────┐
│                    Cliente HTTP (REST)                   │
└────────────────────────────┬────────────────────────────┘
                             │ JSON
┌────────────────────────────▼────────────────────────────┐
│  Controller      TaskController · ProjectController ·    │
│                  UserController                          │
│                  → Recebe requisições, aplica @Valid     │
└────────────────────────────┬────────────────────────────┘
                             │ DTO (Request/Response)
┌────────────────────────────▼────────────────────────────┐
│  Service         TaskService · ProjectService ·          │
│                  UserService                             │
│                  → Regras de negócio e validações        │
└────────────────────────────┬────────────────────────────┘
                             │ Model
┌────────────────────────────▼────────────────────────────┐
│  Repository      TaskRepository · ProjectRepository ·    │
│                  UserRepository                          │
│                  → Acesso ao Valkey (Hash/Set/String)    │
└────────────────────────────┬────────────────────────────┘
                             │ Lettuce
┌────────────────────────────▼────────────────────────────┐
│                      Valkey (6379)                      │
└─────────────────────────────────────────────────────────┘
```

**Fluxo de uma requisição:** o `Controller` converte o JSON em um *DTO de Request*, delega ao `Service` (que contém as regras de negócio), o qual usa o `Repository` para ler/escrever no Valkey e devolve um *DTO de Response* — nunca a entidade interna diretamente.

---

## Modelo de dados (Valkey)

Como um banco chave-valor não possui tabelas nem relacionamentos, os "índices" são mantidos **manualmente** com Sets:

| Chave no Valkey | Tipo | Conteúdo | Finalidade |
|---|---|---|---|
| `user:{id}` | Hash | id, name, email, password | Dados do usuário |
| `user:email:{email}` | String | id do usuário | Índice de unicidade de e-mail |
| `users:all` | Set | todos os IDs de usuário | Listagem geral |
| `project:{id}` | Hash | id, name, description, userId | Dados do projeto |
| `projects:all` | Set | todos os IDs de projeto | Listagem geral |
| `user:{userId}:projects` | Set | projetos do usuário | Listar projetos por dono |
| `task:{id}` | Hash | id, title, description, status, userId, projectId, tags | Dados da tarefa |
| `tasks:all` | Set | todos os IDs de tarefa | Listagem geral |
| `user:{userId}:tasks` | Set | tarefas do usuário | Listar tarefas por usuário |
| `project:{projectId}:tasks` | Set | tarefas do projeto | Listar tarefas por projeto |

> **Consistência preguiçosa:** ao listar, IDs apontando para registros já excluídos são removidos automaticamente dos Sets, mantendo os índices limpos.

---

## Regras de negócio

### Tarefas
- O **título** é obrigatório.
- O **ID do usuário** é obrigatório e deve existir.
- Se um **projeto** for informado, ele deve existir.
- Toda tarefa nasce com status **`PENDENTE`**.
- Status possíveis: `PENDENTE` e `CONCLUIDA`.
- Concluir uma tarefa é feito via `PATCH /api/tasks/{id}/concluir`.
- O **TTL** (em segundos), quando informado, define expiração automática da tarefa.
- Tags nulas são normalizadas para conjunto vazio.

### Usuários
- **Nome**, **e-mail** (formato válido) e **senha** (mínimo 6 caracteres) são obrigatórios.
- O **e-mail** deve ser **único** no sistema.
- A senha nunca é exposta nas respostas (`UserResponse` a omite).

### Projetos
- **Nome** e **ID do usuário dono** são obrigatórios.
- O usuário dono deve existir.
- Um projeto pode agrupar várias tarefas.

---

## Endpoints da API

Base local: `http://localhost:8080`

### Tarefas — `/api/tasks`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/tasks` | Cria uma nova tarefa |
| `GET` | `/api/tasks/{id}` | Busca tarefa por ID |
| `GET` | `/api/tasks` | Lista todas as tarefas |
| `GET` | `/api/tasks/usuario/{userId}` | Lista tarefas de um usuário |
| `GET` | `/api/tasks/project/{projectId}` | Lista tarefas de um projeto |
| `PATCH` | `/api/tasks/{id}/concluir` | Marca a tarefa como concluída |
| `DELETE` | `/api/tasks/{id}` | Remove uma tarefa |

### Projetos — `/api/projects`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/projects` | Cria um novo projeto |
| `GET` | `/api/projects/{id}` | Busca projeto por ID |
| `GET` | `/api/projects/usuario/{userId}` | Lista projetos de um usuário |
| `DELETE` | `/api/projects/{id}` | Remove um projeto |

### Usuários — `/api/users`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/users` | Cria um novo usuário |
| `GET` | `/api/users/{id}` | Busca usuário por ID |
| `GET` | `/api/users` | Lista todos os usuários |
| `DELETE` | `/api/users/{id}` | Remove um usuário |

---

## Como executar

### Pré-requisitos

- **Java 21** (JDK)
- **Maven 3.9+**
- **Docker** (para subir o Valkey) — ou um Valkey/Redis em `localhost:6379`

### Passo a passo

1. **Clone o repositório**
   ```bash
   git clone <url-do-repositorio>
   cd taskmanager
   ```

2. **Suba o banco Valkey** (opcional — o Spring já sobe o container automaticamente via Docker Compose ao iniciar a aplicação)
   ```bash
   docker compose up -d
   ```

3. **Execute a aplicação**
   ```bash
   ./mvnw spring-boot:run
   # ou, com Maven instalado:
   mvn spring-boot:run
   ```

4. A API estará disponível em **`http://localhost:8080`**.

> **Configuração do banco:** definida em `src/main/resources/application.yml` (host `localhost`, porta `6379`).

### Build do artefato

```bash
mvn clean package
java -jar target/taskmanager-0.0.1-SNAPSHOT.jar
```

---

## Documentação interativa (Swagger)

Com a aplicação rodando, acesse:

- **Swagger UI:** [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Título da API: *Task Manager API - Valkey NoSQL* (v1.0.0).

---

## Exemplos de uso

<details>
<summary><b>Criar um usuário</b></summary>

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Lázaro",
        "email": "lazaro@exemplo.com",
        "password": "senha123"
      }'
```
</details>

<details>
<summary><b>Criar um projeto</b></summary>

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Projeto Faculdade",
        "description": "Trabalhos do semestre",
        "userId": "<ID_DO_USUARIO>"
      }'
```
</details>

<details>
<summary><b>Criar uma tarefa</b></summary>

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Finalizar relatório",
        "description": "Revisar e entregar",
        "userId": "<ID_DO_USUARIO>",
        "projectId": "<ID_DO_PROJETO>",
        "tags": ["faculdade", "urgente"],
        "ttl": 86400
      }'
```
</details>

<details>
<summary><b>Concluir uma tarefa</b></summary>

```bash
curl -X PATCH http://localhost:8080/api/tasks/<ID_DA_TAREFA>/concluir
```
</details>

---

## Estrutura do projeto

```
taskmanager/
├── compose.yaml                 # Container Valkey
├── pom.xml                      # Dependências e build Maven
├── README.md
└── src/
    ├── main/
    │   ├── java/com/lazaro/taskmanager/
    │   │   ├── TaskmanagerApplication.java   # Classe main
    │   │   ├── config/
    │   │   │   ├── SwaggerConfig.java        # Configuração do OpenAPI
    │   │   │   └── ValkeyConfig.java         # Config do cliente Valkey
    │   │   ├── controller/                   # Endpoints REST
    │   │   ├── dto/                          # Requests/Responses (records)
    │   │   ├── model/                        # Entidades + enum StatusTarefa
    │   │   ├── repository/                   # Acesso ao Valkey
    │   │   └── service/                      # Regras de negócio
    │   └── resources/
    │       └── application.yml               # Configuração da aplicação
    └── test/                                 # Testes
```

---

## Testes

Para executar a suíte de testes:

```bash
mvn test
```

> Atualmente a suíte contém apenas o teste de carregamento de contexto (`TaskmanagerApplicationTests`). Escrever testes para `Service` e `Repository` está entre as [melhorias futuras](#-melhorias-futuras).

---

## Melhorias futuras

- [ ] **Autenticação e autorização** (JWT / Spring Security) com papéis de usuário
- [ ] **Hash de senha** (BCrypt) — hoje a senha é persistida em texto plano
- [ ] **Endpoints de atualização** (`PUT`/`PATCH`) para usuários, projetos e tarefas
- [ ] **Filtros e paginação** nas listagens (por status, tag, texto, etc.)
- [ ] **Exclusão em cascata** (remover projetos/tarefas junto do usuário)
- [ ] **Tratamento global de exceções** (`@ControllerAdvice`) com respostas HTTP adequadas
- [ ] **Testes de unidade e integração** cobrindo as regras de negócio
- [ ] **Dockerização completa** da aplicação

---

## Licença

Projeto acadêmico — uso educacional. Sinta-se à vontade para estudar e adaptar.

---

<p align="center">Desenvolvido com ☕ e <b>Spring Boot</b> + <b>Valkey</b></p>
