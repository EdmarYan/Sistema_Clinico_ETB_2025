-- =====================================================================
-- RevitaFisio - Sistema Clinico ETB 2025
-- Script DDL (MySQL 8.x / MariaDB)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS revitafisio
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE revitafisio;

SET FOREIGN_KEY_CHECKS = 0;
-- =====================================================================
-- =====================================================================
-- =====================================================================
-- =====================================================================
-- =====================================================================
-- =====================================================================
DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(255)    NOT NULL,
    cpf             VARCHAR(14)     NOT NULL,
    data_nascimento DATE            NULL,
    senha           VARCHAR(255)    NOT NULL,
    data_cadastro   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo           TINYINT(1)      NOT NULL DEFAULT 1,
    tipo_usuario    VARCHAR(31) NOT NULL,
    CONSTRAINT uq_usuarios_cpf UNIQUE (cpf)
) ENGINE=InnoDB;

-- =====================================================================
-- 2. ESPECIALIDADES
-- =====================================================================
DROP TABLE IF EXISTS especialidades;
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
CREATE TABLE cargos (
    id_cargo   INT AUTO_INCREMENT PRIMARY KEY,
    nome_cargo VARCHAR(50) NOT NULL,
    CONSTRAINT uq_cargos_nome UNIQUE (nome_cargo)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. PERMISSOES (granulares, controle de acesso RN01/RN04)
-- =====================================================================
DROP TABLE IF EXISTS permissoes;
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
    tipo       ENUM('TELEFONE','CELULAR','EMAIL','WHATSAPP') NOT NULL,
    valor      VARCHAR(255) NOT NULL,
    principal  TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_contatos_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 6. CARGO_PERMISSOES (N:N entre cargos e permissoes)
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
-- =====================================================================
DROP TABLE IF EXISTS usuario_permissoes;
CREATE TABLE usuario_permissoes (
    usuario_id       INT NOT NULL,
    permissao_id     INT NOT NULL,
    especialidade_id INT NULL, 
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
-- =====================================================================
DROP TABLE IF EXISTS agendamentos;
CREATE TABLE agendamentos (
    id_agendamento       INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente          INT NOT NULL,
    id_fisioterapeuta    INT NOT NULL,
    id_especialidade     INT NOT NULL,
    data_hora_inicio     DATETIME NOT NULL,
    data_hora_fim        DATETIME NOT NULL,
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
    ressonancia_magnetica   TINYINT(1) NULL,
    raio_x                  TINYINT(1) NULL,
    tomografia              TINYINT(1) NULL,
    outros_exames           VARCHAR(255) NULL,
    uso_medicamentos        TINYINT(1) NULL,
    medicamentos_descricao  VARCHAR(255) NULL,
    grau_dor                 ENUM('LEVE','MODERADA','INTENSA') NULL,
    cabeca                   ENUM('ALINHADA','RODADA_DIREITA','RODADA_ESQUERDA','INCLINADA_ESQUERDA','INCLINADA_DIREITA') NULL,
    ombros                   ENUM('NIVELADOS','ESQUERDO_ELEVADO','DIREITO_ELEVADO') NULL,
    maos                     ENUM('SIMETRICOS','DIREITA_ALTA','ESQUERDA_ALTA') NULL,
    eias                     ENUM('SIMETRICAS','DIREITA_ALTA','ESQUERDA_ALTA') NULL,
    joelhos                  ENUM('VALGO','VARO','NORMAL') NULL,
    lombar                   ENUM('HIPERLORDOSE','RETIFICADA','NORMAL') NULL,
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
