-- =====================================================================
-- Usuario ADMIN de teste, para primeiro login no RevitaFisio
-- Login e feito por CPF + senha (texto puro por enquanto -- veja o
-- comentario no proprio AuthService.java: "Em um sistema de producao,
-- a senha NUNCA deve ser comparada em texto plano". Ok pra este estagio
-- academico, mas vale melhorar depois com BCrypt/Spring Security.)
-- =====================================================================

USE revitafisio;

INSERT INTO usuarios (nome, cpf, data_nascimento, senha, ativo, tipo_usuario)
VALUES ('Admin Teste', '00000000000', '2000-01-01', 'admin123', 1, 'ADMIN');

-- Conferir que criou certo:
SELECT id_usuario, nome, cpf, tipo_usuario, ativo FROM usuarios WHERE tipo_usuario = 'ADMIN';
