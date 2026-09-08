# RevitaFisio

Sistema de gestão para clínicas de fisioterapia — agendamento de consultas, controle de horários dos fisioterapeutas, fichas de avaliação clínica (Ortopedia e RPG), evolução de pacientes e controle de acesso por permissões.

> TCC do curso Técnico em Informática, Escola Técnica de Brasília (ETB/GDF-SEE), 2025.
> Projeto desenvolvido em grupo — fui responsável pelo **backend (Java/Spring Boot), modelagem e implementação do banco de dados**.

## Stack

- **Java 21** + **Spring Boot 3.5.0**
- **Spring Data JPA** / **Hibernate 6.6** — persistência e mapeamento objeto-relacional
- **MySQL 8** — banco de dados relacional
- **Maven** — build e gerenciamento de dependências
- **Bean Validation** (Jakarta) — validação de entrada nos endpoints
- **Lombok** — redução de boilerplate nas entidades
- HTML/CSS/JS (estático, servido pelo Spring Boot) no front-end

## Arquitetura

Back-end organizado por **módulo de domínio** (não por camada técnica), cada um com sua própria fatia de `controller` / `service` / `repository` / `dto`:

```
com.revitafisio
├── auth            # login (CPF + senha)
├── entities        # modelo de dados (JPA)
│   ├── usuarios     # Usuario (classe-base) -> Admin, Fisioterapeuta, Recepcionista, Paciente
│   │                # (herança single-table, discriminada por tipo_usuario)
│   ├── agendamentos # Agendamento, HorarioTrabalho, HorarioDisponivel
│   ├── paciente     # Evolucao, AvaliacaoOrtopedia, AvaliacaoRpg
│   └── permissoes   # Cargo, Permissao, UsuarioPermissao (RBAC)
├── agendamento     # regras de agendamento e disponibilidade de horários
├── paciente        # cadastro de pacientes, avaliações e evolução clínica
├── funcionario     # gestão de equipe (fisioterapeutas, recepcionistas, especialidades)
├── relatorio       # relatório gerencial de atendimentos por período/fisioterapeuta
├── converters      # DayOfWeekConverter (mapeia java.time.DayOfWeek <-> INT no banco)
└── exception       # tratamento de erros de regra de negócio
```

Modelagem do banco: 15 tabelas, com herança de usuários em tabela única (`usuarios` + coluna discriminadora `tipo_usuario`), relacionamentos N:N para especialidades/permissões, e um schema validado via `spring.jpa.hibernate.ddl-auto=validate` contra o DDL versionado em [`database/revitafisio_ddl.sql`](database/revitafisio_ddl.sql).

## Funcionalidades

- Cadastro e autenticação de usuários (Admin, Fisioterapeuta, Recepcionista, Paciente)
- Agendamento de consultas com controle de horários disponíveis por fisioterapeuta
- Grade de horário de trabalho por dia da semana, por profissional
- Fichas de avaliação clínica especializadas (Ortopedia e RPG), com campos estruturados por enum (grau de dor, posturas, etc.)
- Registro de evolução do paciente por sessão
- Gestão de equipe: cargos, permissões e vínculo fisioterapeuta ↔ especialidade
- Relatório mensal de atendimentos por fisioterapeuta

## Status e Evolução do Projeto

O projeto foi entregue com sucesso no TCC e desde então venho evoluindo o repositório para fins de estudos e portfólio. As seguintes melhorias estruturais e arquiteturais foram aplicadas:

- [x] **Segurança e JWT:** Autenticação com Token JWT (`jjwt`), criptografia de senhas (BCrypt) e filtro Stateless blindando a API REST.
- [x] **Documentação interativa:** Implantação do Swagger UI / OpenAPI 3 para testes diretos na API.
- [x] **Containerização (Docker):** Criação de um Dockerfile Multi-stage para empacotamento da aplicação e de um ambiente reprodutível.
- [x] **Testes Automatizados (JUnit 5):** Cobertura com testes unitários (Mockito) e testes de integração Web (MockMvc).
- [ ] Deploy na nuvem pública (Railway/Render).

## Como rodar localmente (Docker)

O projeto agora é **100% Dockerizado**, facilitando a execução sem precisar instalar o Java ou o Maven na sua máquina.

Pré-requisito: Ter o Docker instalado.

**1. Suba o banco de dados (MySQL 8):**
```bash
docker run --name revitafisio-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=revitafisio \
  -p 3306:3306 \
  -d mysql:8.0
```

**2. Crie o schema e popule o banco inicial:**
```bash
docker exec -i revitafisio-mysql mysql -uroot -proot < database/revitafisio_ddl.sql
docker exec -i revitafisio-mysql mysql -uroot -proot < database/seed_admin.sql
```

**3. Construa a imagem do sistema (Backend + Frontend):**
```bash
cd revitafisio
docker build -t revitafisio-app .
```

**4. Execute o sistema conectando-se ao banco local (Ubuntu/Linux):**
```bash
docker run --network host revitafisio-app
```
*(Se estiver usando Windows/Mac, passe o IP do container do banco via flag `-e DB_URL=...`).*

A aplicação estará disponível em:
- **Frontend / Sistema:** `http://localhost:8080`
- **Documentação da API (Swagger):** `http://localhost:8080/swagger-ui.html`

O login padrão gerado pelo seed é:
- **CPF:** `12345678901`
- **Senha:** `admin123`

## Autor

**Edmar Yan** — Técnico em Informática (ETB, 2025)
[GitHub](https://github.com/EdmarYan) | [LinkedIn](https://www.linkedin.com/in/edmar-yan-faria-de-melo/)
