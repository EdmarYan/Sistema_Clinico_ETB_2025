-- =====================================================================
-- Usuario ADMIN de teste, para primeiro login no RevitaFisio
--
-- A senha abaixo e' o hash BCrypt de "admin123". O AuthService usa
-- BCryptPasswordEncoder para validar, entao a coluna 'senha' DEVE
-- conter o hash, nao o texto puro. Se voce quiser trocar a senha,
-- gere um novo hash (por ex. via https://bcrypt-generator.com ou
-- com o proprio PasswordEncoder do Spring) e substitua aqui.
-- =====================================================================

USE revitafisio;

INSERT INTO usuarios (nome, cpf, data_nascimento, senha, ativo, tipo_usuario)
VALUES ('Admin Teste', '00000000000', '2000-01-01',
        '$2b$10$lFd7fgPPqgyhhCG3fkfgfuuDTj3Rwvx6K3fRWW6vmpbfhr4mvWWS6',
        1, 'ADMIN');

-- Conferir que criou certo:
SELECT id_usuario, nome, cpf, tipo_usuario, ativo FROM usuarios WHERE tipo_usuario = 'ADMIN';
