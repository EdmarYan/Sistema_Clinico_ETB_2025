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

## Como rodar localmente

Pré-requisitos: JDK 21, Maven, Docker (ou um MySQL 8 já rodando).

**1. Suba um MySQL** (via Docker, mais simples):
```bash
docker run --name revitafisio-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=revitafisio \
  -p 3306:3306 \
  -d mysql:8.0
```

**2. Crie o schema:**
```bash
docker exec -i revitafisio-mysql mysql -uroot -proot < database/revitafisio_ddl.sql
```

**3. Configure a aplicação:**
```bash
cp revitafisio/src/main/resources/application.properties.example \
   revitafisio/src/main/resources/application.properties
# edite usuario/senha do datasource conforme seu ambiente
```

**4. Rode:**
```bash
cd revitafisio
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Use `database/seed_admin.sql` para criar um usuário administrador de teste (login por CPF + senha).

## Status / próximos passos

Projeto de TCC já entregue; este repositório está sendo mantido/organizado como parte do meu portfólio. Melhorias que pretendo aplicar:

- [ ] Hash de senha (BCrypt via Spring Security) — hoje a senha é comparada em texto puro
- [ ] Autenticação com token (JWT) em vez de resposta simples de login
- [ ] Testes automatizados (unitários e de integração)
- [ ] Deploy em ambiente público (Railway/Render) com banco gerenciado

## Autor

**Edmar Yan** — Técnico em Informática (ETB, 2025)
[GitHub](https://github.com/EdmarYan)
