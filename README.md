# Sistema Biblioteca - Microserviço de Usuários

Microserviço responsável pelo gerenciamento de usuários da biblioteca, incluindo cadastro, autenticação, consulta de perfis e atualização/exclusão de dados.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Token)
- OpenAPI / Swagger
- Gradle
- Docker

## Funcionalidades

- Cadastro de usuários
- Login com autenticação JWT
- Consulta de usuário por e-mail
- Consulta do próprio perfil
- Atualização do perfil autenticado
- Exclusão de usuário com controle de permissões
- Validação de dados e gerenciamento de exceções
- Documentação da API com Swagger UI

## Estrutura do Projeto

```text
.
├── .env-example
├── .github/
├── Dockerfile
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
├── gradle/
├── src/
│   ├── main/
│   │   ├── java/com/biblioteca/usuario/
│   │   │   ├── business/
│   │   │   ├── controller/
│   │   │   ├── handler/
│   │   │   ├── infrastructure/
│   │   │   └── UsuarioApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/biblioteca/usuario/
└── README.md
```

## Requisitos

Antes de executar o projeto, certifique-se de ter instalado:

- JDK 17+
- Gradle
- PostgreSQL
- Docker (opcional)

## Configuração do ambiente

1. Crie um banco PostgreSQL local com o nome `biblioteca_usuario`.
2. Copie o arquivo `.env-example` para `.env`.
3. Ajuste as variáveis conforme seu ambiente:

```env
DB_URL=jdbc:postgresql://localhost:5432/biblioteca_usuario
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
```

O arquivo `application.properties` já importa esse arquivo automaticamente:

```properties
spring.config.import=optional:file:./.env[.properties]
```

## Execução local

Na raiz do projeto, execute:

```bash
./gradlew clean build
./gradlew bootRun
```

A aplicação será iniciada em:

```text
http://localhost:8080
```

## Documentação da API

A documentação Swagger está disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

## Endpoints principais

### Usuário

- `POST /usuario` - cadastro de usuário
- `POST /usuario/login` - autenticação e geração de token
- `GET /usuario/me` - consulta do próprio perfil
- `GET /usuario/email/{email}` - consulta de usuário por e-mail
- `PUT /usuario/me` - atualização do perfil autenticado
- `DELETE /usuario/{email}` - exclusão de usuário

## Autenticação

A API utiliza JWT para proteger endpoints de usuários. Após realizar o login, o token deve ser enviado no header:

```http
Authorization: Bearer <token>
```

## Testes

Para rodar os testes automatizados:

```bash
./gradlew test
```

## Docker

O projeto também inclui um `Dockerfile` para empacotar a aplicação.

Para construir a imagem:

```bash
docker build -t biblioteca-usuario .
```

Para rodar o container:

```bash
docker run -p 8080:8080 biblioteca-usuario
```

## Observações

- O projeto usa Spring Security para autenticação e autorização.
- A configuração da base de dados é feita via variáveis de ambiente.
- O JPA cria/atualiza as tabelas automaticamente com `spring.jpa.hibernate.ddl-auto=update`.

## Licença

Este projeto não possui licença definida neste repositório.

## Autor

Lucas Vinícius
