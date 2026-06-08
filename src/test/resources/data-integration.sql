-- Usuário Administrador (O DB vai gerar o ID 1)
INSERT INTO tb_users (is_active, registration, name, password, role)
VALUES (true, 'admin001', 'Admin User', '$2a$10$aA.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o3.a.a.o', 'ADMIN');

-- Usuário Estudante para os testes (O DB vai gerar o ID 2)
INSERT INTO tb_users (is_active, registration, name, password, role)
VALUES (true, 'student001', 'Student User', '$2a$10$bB.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o4.b.b.o', 'STUDENT');

-- Localização padrão (O DB vai gerar o ID 1)
INSERT INTO tb_locations (name) VALUES ('Biblioteca Central');

-- Status Padrão (O DB vai gerar ID 1 e 2)
INSERT INTO tb_status_step (name, description, color) VALUES
                                                          ('LOST', 'Item foi reportado como perdido.', '#FF0000'),
                                                          ('FOUND', 'Item foi encontrado por outro usuário.', '#00FF00');

-- Relatório existente para o teste de busca (deixando o DB gerar o ID 1)
INSERT INTO tb_report (title, description, type_report, image_url, fk_location_id, created_by, date_report, is_closed, deleted, position_latitude, position_longitude, created_at, updated_at)
VALUES ('Chaves perdidas', 'Um molho de chaves com chaveiro do Star Wars', 'LOST', 'https://res.cloudinary.com/test/image/upload/v1/caip/reports/key.jpg', 1, 2, '2023-10-27T10:00:00Z', false, false, -22.9068, -43.1729, NOW(), NOW());
