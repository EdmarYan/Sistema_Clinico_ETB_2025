-- =====================================================================
-- RevitaFisio - Sistema Clinico ETB 2025
-- Script DDL (MySQL 8.x / MariaDB)
-- REVISADO E VALIDADO CONTRA O CODIGO REAL (@Entity) do repositorio
-- https://github.com/EdmarYan/Sistema_Clinico_ETB_2025 (pasta revitafisio/)
-- em 01/09/2026. Onde o codigo Java diverge do EER Diagram da documentacao
-- (secao 5.6 do TCC), o CODIGO tem prioridade -- e' o que o Hibernate
-- vai de fato tentar mapear/validar contra este banco.
-- =====================================================================
-- IMPORTANTE - NAO ENCONTREI application.properties/application.yml
-- no repositorio (nao foi commitado). Sem esse arquivo o Spring Boot
-- NAO SOBE. Veja o modelo pronto no final deste arquivo (comentado) para
-- copiar em revitafisio/src/main/resources/application.properties.
-- =====================================================================
-- COMO USAR NO DBEAVER:
-- 1. Crie/edite uma conexao MySQL apontando para seu servidor local
--    (ou suba um MySQL via Docker: docker run --name revitafisio-db
--     -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.0)
-- 2. Abra um SQL Editor nessa conexao
-- 3. Rode este script inteiro (Execute SQL Script, nao "Execute Statement")
-- =====================================================================

CREATE DATABASE IF NOT EXISTS revitafisio
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE revitafisio;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 1. USUARIOS
-- Tabela unica para Admin, Fisioterapeuta, Recepcionista e Paciente
-- (discriminada por tipo_usuario), conforme diagrama de classes
-- (Usuario e especializacoes Paciente/Fisioterapeuta).
-- =====================================================================
DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    -- CONFIRMADO no codigo (Usuario.java): nome length=255, cpf length=14 (unique).
    nome            VARCHAR(255)    NOT NULL,
    cpf             VARCHAR(14)     NOT NULL,
    data_nascimento DATE            NULL,
    senha           VARCHAR(255)    NOT NULL,
    data_cadastro   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo           TINYINT(1)      NOT NULL DEFAULT 1,
    -- CONFIRMADO no codigo: Usuario.java usa @Inheritance(SINGLE_TABLE) +
    -- @DiscriminatorColumn(name="tipo_usuario"). O Hibernate cria essa coluna
    -- como VARCHAR (tamanho padrao 31), NAO como ENUM do MySQL -- por isso
    -- usamos VARCHAR aqui (um ENUM do MySQL quebraria o schema-validation
    -- do Hibernate). Valores gravados pelas subclasses (@DiscriminatorValue):
    -- 'ADMIN' (Admin.java), 'FISIOTERAPEUTA' (Fisioterapeuta.java),
    -- 'RECEPCIONISTA' (Recepcionista.java), 'PACIENTE' (Paciente.java).
    tipo_usuario    VARCHAR(31) NOT NULL,
    CONSTRAINT uq_usuarios_cpf UNIQUE (cpf)
) ENGINE=InnoDB;

-- =====================================================================
-- 2. ESPECIALIDADES
-- =====================================================================
DROP TABLE IF EXISTS especialidades;
-- CONFIRMADO no codigo (Especialidade.java): nome length=100 (unique, not null),
-- cor length=7 e NOT NULL (regex valida formato hex #RRGGBB na aplicacao).
CREATE TABLE especialidades (
    id_especialidade INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL,
    cor               VARCHAR(7)   NOT NULL,
    CONSTRAINT uq_especialidades_nome UNIQUE (nome)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. CARGOS (perfis de acesso: Admin, Fisioterapeuta, Recepcionista...)
-- =====================================================================
DROP TABLE IF EXISTS cargos;
-- CONFIRMADO no codigo (Cargo.java): nome_cargo length=50 (unique, not null).
CREATE TABLE cargos (
    id_cargo   INT AUTO_INCREMENT PRIMARY KEY,
    nome_cargo VARCHAR(50) NOT NULL,
    CONSTRAINT uq_cargos_nome UNIQUE (nome_cargo)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. PERMISSOES (granulares, controle de acesso RN01/RN04)
-- =====================================================================
DROP TABLE IF EXISTS permissoes;
-- CONFIRMADO no codigo (Permissao.java): codigo length=50 (unique, not null),
-- descricao NOT NULL (sem length explicito -> padrao Hibernate VARCHAR(255)).
CREATE TABLE permissoes (
    id_permissao INT AUTO_INCREMENT PRIMARY KEY,
    codigo       VARCHAR(50)  NOT NULL,
    descricao    VARCHAR(255) NOT NULL,
    CONSTRAINT uq_permissoes_codigo UNIQUE (codigo)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. CONTATOS (1:N com usuarios)
-- =====================================================================
DROP TABLE IF EXISTS contatos;
CREATE TABLE contatos (
    id_contato INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    -- CONFIRMADO no codigo: Contato.TipoContato { TELEFONE, CELULAR, EMAIL, WHATSAPP }
    -- persistido via @Enumerated(EnumType.STRING).
    tipo       ENUM('TELEFONE','CELULAR','EMAIL','WHATSAPP') NOT NULL,
    valor      VARCHAR(255) NOT NULL,
    principal  TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_contatos_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 6. CARGO_PERMISSOES (N:N entre cargos e permissoes)
-- NOME DA TABELA CORRIGIDO: Cargo.java define @JoinTable(name = "cargo_permissoes")
-- (plural). Se a tabela se chamasse "cargo_permissao" o Hibernate NAO
-- encontraria a tabela e a aplicacao falharia ao subir (schema-validation)
-- ou ao tentar gravar/ler cargos e permissoes.
-- =====================================================================
DROP TABLE IF EXISTS cargo_permissoes;
CREATE TABLE cargo_permissoes (
    id_cargo     INT NOT NULL,
    id_permissao INT NOT NULL,
    PRIMARY KEY (id_cargo, id_permissao),
    CONSTRAINT fk_cargoperm_cargo
        FOREIGN KEY (id_cargo) REFERENCES cargos(id_cargo)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cargoperm_permissao
        FOREIGN KEY (id_permissao) REFERENCES permissoes(id_permissao)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 7. USUARIO_CARGO (N:N entre usuarios e cargos)
-- ATENCAO: esta tabela existe no Modelo Logico (diagrama) mas eu NAO
-- encontrei nenhuma entidade Java que a referencie -- Usuario.java nao tem
-- campo "cargos", e nenhuma outra classe do projeto usa Cargo alem da
-- propria Cargo.java. Ou seja: hoje o vinculo Usuario<->Cargo NAO esta
-- implementado no backend (o controle de acesso real parece ser feito via
-- UsuarioPermissao, direto Usuario->Permissao). Mantive a tabela (nao
-- atrapalha o Hibernate, que so' reclama de tabelas que FALTAM, nao das
-- que sobram), mas ela ficara' vazia ate' voces implementarem essa parte.
-- =====================================================================
DROP TABLE IF EXISTS usuario_cargo;
CREATE TABLE usuario_cargo (
    id_usuario INT NOT NULL,
    id_cargo   INT NOT NULL,
    PRIMARY KEY (id_usuario, id_cargo),
    CONSTRAINT fk_usuariocargo_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_usuariocargo_cargo
        FOREIGN KEY (id_cargo) REFERENCES cargos(id_cargo)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 8. FISIOTERAPEUTA_ESPECIALIDADES (N:N entre usuarios(fisio) e especialidades)
-- NOME DA TABELA CORRIGIDO: Fisioterapeuta.java define
-- @JoinTable(name = "fisioterapeuta_especialidades") (plural).
-- =====================================================================
DROP TABLE IF EXISTS fisioterapeuta_especialidades;
CREATE TABLE fisioterapeuta_especialidades (
    fisioterapeuta_id INT NOT NULL,
    especialidade_id  INT NOT NULL,
    PRIMARY KEY (fisioterapeuta_id, especialidade_id),
    CONSTRAINT fk_fisioesp_usuario
        FOREIGN KEY (fisioterapeuta_id) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_fisioesp_especialidade
        FOREIGN KEY (especialidade_id) REFERENCES especialidades(id_especialidade)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 9. USUARIO_PERMISSOES (N:N entre usuarios e permissoes,
--    com escopo opcional por especialidade)
-- NOME DA TABELA CORRIGIDO: UsuarioPermissao.java define
-- @Table(name = "usuario_permissoes") (plural). A PK composta bate com
-- UsuarioPermissaoId (usuario_id + permissao_id via @MapsId).
-- =====================================================================
DROP TABLE IF EXISTS usuario_permissoes;
CREATE TABLE usuario_permissoes (
    usuario_id       INT NOT NULL,
    permissao_id     INT NOT NULL,
    especialidade_id INT NULL, -- CONFIRMADO nullable no codigo (permissao pode nao ter escopo de especialidade)
    PRIMARY KEY (usuario_id, permissao_id),
    CONSTRAINT fk_usuarioperm_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_usuarioperm_permissao
        FOREIGN KEY (permissao_id) REFERENCES permissoes(id_permissao)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_usuarioperm_especialidade
        FOREIGN KEY (especialidade_id) REFERENCES especialidades(id_especialidade)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 10. HORARIOS_TRABALHO (grade fixa semanal do fisioterapeuta)
-- =====================================================================
DROP TABLE IF EXISTS horarios_trabalho;
CREATE TABLE horarios_trabalho (
    id_horario_trabalho INT AUTO_INCREMENT PRIMARY KEY,
    ativo               TINYINT(1) NOT NULL DEFAULT 1,
    -- CONFIRMADO no codigo: HorarioTrabalho.diaDaSemana e' um java.time.DayOfWeek,
    -- convertido para INT via DayOfWeekConverter.java (autoApply=true):
    -- 1=MONDAY (Segunda), 2=TUESDAY, 3=WEDNESDAY, 4=THURSDAY, 5=FRIDAY,
    -- 6=SATURDAY, 7=SUNDAY (Domingo). Nao e' 1=Domingo.
    dia_semana           INT NOT NULL,
    hora_fim             TIME NOT NULL,
    hora_inicio          TIME NOT NULL,
    id_fisioterapeuta     INT NOT NULL,
    CONSTRAINT fk_horariotrabalho_fisio
        FOREIGN KEY (id_fisioterapeuta) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_horariotrabalho_intervalo CHECK (hora_inicio < hora_fim)
) ENGINE=InnoDB;

-- =====================================================================
-- 11. HORARIOS_DISPONIVEIS (slots concretos gerados a partir da grade)
-- =====================================================================
DROP TABLE IF EXISTS horarios_disponiveis;
CREATE TABLE horarios_disponiveis (
    id_horario        BIGINT AUTO_INCREMENT PRIMARY KEY,
    data              DATE NOT NULL,
    disponivel        TINYINT(1) NOT NULL DEFAULT 1,
    hora_fim          TIME NOT NULL,
    hora_inicio       TIME NOT NULL,
    id_fisioterapeuta INT NOT NULL,
    CONSTRAINT fk_horariodisp_fisio
        FOREIGN KEY (id_fisioterapeuta) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_horariodisp_slot UNIQUE (id_fisioterapeuta, data, hora_inicio)
) ENGINE=InnoDB;

-- =====================================================================
-- 12. AGENDAMENTOS
-- CORRECOES CRITICAS vs. o diagrama:
-- 1) id_agendamento e' INT, NAO BIGINT. O proprio codigo (Agendamento.java)
--    tem um comentario do autor avisando disso: "O tipo foi mudado para
--    Long no DDL, mas a classe usa Integer... Manterei Integer". Se a
--    coluna fosse BIGINT, o Hibernate com ddl-auto=validate reprovaria o
--    schema (mismatch INTEGER x BIGINT) na subida da aplicacao.
-- 2) confirmacao_whatsapp e prazo_confirmacao NAO EXISTEM na entidade
--    Agendamento.java atual (so' existem no diagrama da documentacao).
--    Removi essas colunas do DDL para casar exatamente com o codigo; se
--    voces quiserem essa funcionalidade, precisam primeiro adicionar os
--    campos na classe Agendamento.java.
-- =====================================================================
DROP TABLE IF EXISTS agendamentos;
CREATE TABLE agendamentos (
    id_agendamento       INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente          INT NOT NULL,
    id_fisioterapeuta    INT NOT NULL,
    id_especialidade     INT NOT NULL,
    data_hora_inicio     DATETIME NOT NULL,
    data_hora_fim        DATETIME NOT NULL,
    -- CONFIRMADO no codigo: Agendamento.StatusAgendamento { CONFIRMADO,
    -- CANCELADO, PENDENTE, REALIZADO, NAO_COMPARECEU }, sem valor default
    -- na aplicacao (sempre setado explicitamente antes de salvar).
    status               ENUM('CONFIRMADO','CANCELADO','PENDENTE','REALIZADO','NAO_COMPARECEU')
                         NOT NULL,
    CONSTRAINT fk_agendamento_paciente
        FOREIGN KEY (id_paciente) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_agendamento_fisio
        FOREIGN KEY (id_fisioterapeuta) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_agendamento_especialidade
        FOREIGN KEY (id_especialidade) REFERENCES especialidades(id_especialidade)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_agendamento_intervalo CHECK (data_hora_inicio < data_hora_fim)
) ENGINE=InnoDB;

-- =====================================================================
-- 13. EVOLUCAO (evolucao clinica do paciente por sessao)
-- =====================================================================
DROP TABLE IF EXISTS evolucao;
CREATE TABLE evolucao (
    id_evolucao       INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente       INT NOT NULL,
    id_fisioterapeuta INT NOT NULL,
    data              DATE NOT NULL,
    -- CONFIRMADO no codigo: Evolucao.descricao e' @Lob @Column(columnDefinition="TEXT", nullable=false)
    descricao         TEXT NOT NULL,
    preenchida        TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_evolucao_paciente
        FOREIGN KEY (id_paciente) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_evolucao_fisio
        FOREIGN KEY (id_fisioterapeuta) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 14. AVALIACAO_ORTOPEDIA (ficha de avaliacao - especialidade Ortopedia)
-- =====================================================================
DROP TABLE IF EXISTS avaliacao_ortopedia;
CREATE TABLE avaliacao_ortopedia (
    id_avaliacao               INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente                INT NOT NULL,
    id_fisioterapeuta          INT NOT NULL,
    data_avaliacao              DATE NOT NULL,
    profissao                   VARCHAR(255) NULL,
    alergias                    LONGTEXT NULL,
    indicacao_medica            LONGTEXT NULL,
    frequencia_cardiaca         INT NULL,
    frequencia_respiratoria     INT NULL,
    -- CORRIGIDO vs. diagrama: AvaliacaoOrtopedia.java usa
    -- @Column(precision = 5, scale = 2) -> DECIMAL(5,2), nao (38,2).
    temperatura                 DECIMAL(5,2) NULL,
    pressao_arterial            VARCHAR(255) NULL,
    queixa_principal             LONGTEXT NULL,
    hda_hdp                     LONGTEXT NULL,
    doencas_cardiacas           LONGTEXT NULL,
    comorbidades                LONGTEXT NULL,
    medicacoes                  LONGTEXT NULL,
    avaliacao_postural           VARCHAR(255) NULL,
    diagnostico_fisioterapeutico LONGTEXT NULL,
    objetivos                    LONGTEXT NULL,
    conduta                      LONGTEXT NULL,
    observacoes                  LONGTEXT NULL,
    CONSTRAINT fk_avortopedia_paciente
        FOREIGN KEY (id_paciente) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_avortopedia_fisio
        FOREIGN KEY (id_fisioterapeuta) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 15. AVALIACAO_RPG (ficha de avaliacao - especialidade RPG)
-- =====================================================================
DROP TABLE IF EXISTS avaliacao_rpg;
CREATE TABLE avaliacao_rpg (
    id_avaliacao            INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente             INT NOT NULL,
    id_fisioterapeuta       INT NOT NULL,
    data_avaliacao          DATE NOT NULL,
    diagnostico_clinico     VARCHAR(255) NULL,
    hma                     TEXT NULL,
    posicao_dor             VARCHAR(255) NULL,
    outras_patologias       TEXT NULL,
    -- CORRIGIDO vs. diagrama: Boolean wrapper no Java -> nullable no banco.
    ressonancia_magnetica   TINYINT(1) NULL,
    raio_x                  TINYINT(1) NULL,
    tomografia              TINYINT(1) NULL,
    outros_exames           VARCHAR(255) NULL,
    -- CORRIGIDO vs. diagrama: os 4 campos abaixo sao Boolean (wrapper), nao
    -- boolean primitivo -> nullable no banco (comentario no proprio codigo
    -- explica que NULL significa "nao informado", diferente de false).
    uso_medicamentos        TINYINT(1) NULL,
    medicamentos_descricao  VARCHAR(255) NULL,
    -- CORRIGIDO vs. diagrama: valores exatos copiados dos enums Java
    -- declarados dentro de AvaliacaoRpg.java (nao sao os que eu tinha
    -- assumido antes). Todos sao Enumerated(STRING) e nullable.
    grau_dor                 ENUM('LEVE','MODERADA','INTENSA') NULL,
    cabeca                   ENUM('ALINHADA','RODADA_DIREITA','RODADA_ESQUERDA','INCLINADA_ESQUERDA','INCLINADA_DIREITA') NULL,
    ombros                   ENUM('NIVELADOS','ESQUERDO_ELEVADO','DIREITO_ELEVADO') NULL,
    maos                     ENUM('SIMETRICOS','DIREITA_ALTA','ESQUERDA_ALTA') NULL,
    eias                     ENUM('SIMETRICAS','DIREITA_ALTA','ESQUERDA_ALTA') NULL,
    joelhos                  ENUM('VALGO','VARO','NORMAL') NULL,
    lombar                   ENUM('HIPERLORDOSE','RETIFICADA','NORMAL') NULL,
    -- ATENCAO: os valores de PosicaoPelve no codigo tem acento (Ã), porque
    -- identificadores Java aceitam letras Unicode. Mantive exatamente como
    -- esta' em AvaliacaoRpg.java. O banco precisa estar em utf8mb4 (ja
    -- configurado no CREATE DATABASE acima) para gravar isso sem corromper.
    pelve                    ENUM('ANTEVERSÃO','RETROVERSÃO','NORMAL') NULL,
    escapulas                ENUM('DIREITA_ALTA','ESQUERDA_ALTA') NULL,
    outros_desequilibrios    TEXT NULL,
    tratamento_proposto      TEXT NULL,
    observacoes              TEXT NULL,
    CONSTRAINT fk_avrpg_paciente
        FOREIGN KEY (id_paciente) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_avrpg_fisio
        FOREIGN KEY (id_fisioterapeuta) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- FIM DO SCRIPT
-- =====================================================================
--
-- PROXIMO PASSO OBRIGATORIO (fora do DBeaver):
-- Nao existe application.properties no repositorio -- crie o arquivo
-- revitafisio/src/main/resources/application.properties com o conteudo
-- abaixo (ajuste usuario/senha para os que voce configurou no seu MySQL):
--
-- spring.datasource.url=jdbc:mysql://localhost:3306/revitafisio?useSSL=false&serverTimezone=America/Sao_Paulo
-- spring.datasource.username=root
-- spring.datasource.password=SUASENHA
-- spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
--
-- # 'validate' checa se as entidades batem com este schema sem alterar nada.
-- # E' o melhor modo pra confirmar se este DDL esta correto.
-- spring.jpa.hibernate.ddl-auto=validate
-- spring.jpa.show-sql=true
-- spring.jpa.properties.hibernate.format_sql=true
-- =====================================================================
