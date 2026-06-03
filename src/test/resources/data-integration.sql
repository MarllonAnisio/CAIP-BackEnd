-- Usuário Administrador
INSERT INTO tb_users (id, is_active, registration, name, password, role)
VALUES (1, true, 'admin001', 'Admin User', '$2a$10$aA.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o', 'ADMIN');

-- Usuário Estudante para os testes
INSERT INTO tb_users (id, is_active, registration, name, password, role)
VALUES (2, true, 'student001', 'Student User', '$2a$10$bB.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o', 'STUDENT');

-- Localização padrão
INSERT INTO tb_locations (id, name) VALUES (1, 'Biblioteca Central');

-- Status Padrão
INSERT INTO tb_status_step (id, name, description, color) VALUES
(1, 'LOST', 'Item foi reportado como perdido.', '#FF0000'),
(2, 'FOUND', 'Item foi encontrado por outro usuário.', '#00FF00');

-- Relatório existente para o teste de busca (deixando o DB gerar o ID 1)
INSERT INTO tb_report (title, description, type_report, image_url, fk_location_id, created_by, date_report, is_closed, deleted, position_latitude, position_longitude, created_at, updated_at)
VALUES ('Chaves perdidas', 'Um molho de chaves com chaveiro do Star Wars', 'LOST', 'https://res.cloudinary.com/test/image/upload/v1/caip/reports/key.jpg', 1, 2, '2023-10-27T10:00:00Z', false, false, -22.9068, -43.1729, NOW(), NOW());
