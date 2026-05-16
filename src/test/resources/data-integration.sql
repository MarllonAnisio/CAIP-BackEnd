-- 1. Cria a Role (Perfil) de ADMIN
INSERT INTO tb_role (id, name) VALUES (1, 'ADMIN');

INSERT INTO tb_users (id, registration, name, password, is_active)
VALUES (1, '123456', 'Administrador do Sistema', '$2a$10$nUlXpALjPjBzJ4YYhvjq7.nH.1GEPj/rHEEYL76L9tKdv2b0wWJd2', true);


--INSERT INTO tb_user_role ( fk_user_id, fk_role_id) VALUES (1, 1);