-- Repare que agora passamos 'ADMIN' ou 'STUDENT' direto na coluna role
INSERT INTO tb_users (is_active, registration, name, password, role)
VALUES (true, '123456', 'Marllon', 'senha_criptografada_aqui', 'ADMIN');

--INSERT INTO tb_user_role ( fk_user_id, fk_role_id) VALUES (1, 1);