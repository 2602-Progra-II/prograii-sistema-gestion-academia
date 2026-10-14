-- 1. Roles del Sistema
SET IDENTITY_INSERT rol ON;
INSERT INTO rol (id_rol, nombre_rol, descripcion) VALUES 
(1, 'ADMINISTRADOR', 'Administrador general de la plataforma'),
(2, 'COORDINADOR_ACADEMICO', 'Encargado de la oferta académica y asignación de cursos'),
(3, 'INSTRUCTOR', 'Profesor encargado de impartir clases y evaluar'),
(4, 'ESTUDIANTE', 'Alumno inscrito en la academia'),
(5, 'ASESOR_COMERCIAL', 'Encargado de validar pagos y estados de cuenta');
SET IDENTITY_INSERT rol OFF;
GO

-- 2. Usuarios de Prueba
SET IDENTITY_INSERT usuario ON;
INSERT INTO usuario (id_usuario, nombre_usuario, correo, contrasena, estado, id_rol) VALUES 
(1, 'Admin Principal', 'admin@academia.com', 'admin123', 'ACTIVO', 1),
(2, 'Coordinador Academico', 'coordinador@academia.com', 'coord123', 'ACTIVO', 2),
(3, 'Profesor Java', 'instructor@academia.com', 'inst123', 'ACTIVO', 3),
(4, 'Estudiante Carlos', 'estudiante@academia.com', 'est123', 'ACTIVO', 4),
(5, 'Asesor Comercial', 'asesor@academia.com', 'asesor123', 'ACTIVO', 5);
SET IDENTITY_INSERT usuario OFF;
GO

-- 3. Período Académico
SET IDENTITY_INSERT periodo_academico ON;
INSERT INTO periodo_academico (id_periodo, nombre_periodo, fecha_inicio, fecha_fin, estado) VALUES 
(1, 'Ciclo 2026-02', '2026-08-01', '2026-12-15', 'ACTIVO');
SET IDENTITY_INSERT periodo_academico OFF;
GO

-- 4. Curso de Prueba
SET IDENTITY_INSERT curso ON;
INSERT INTO curso (id_curso, codigo_curso, nombre_curso, descripcion, id_periodo, id_instructor) VALUES 
(1, 'PROG-II', 'Programación II', 'Curso avanzado de POO, JDBC y Concurrencia en Java', 1, 3);
SET IDENTITY_INSERT curso OFF;
GO

-- 5. Matrícula de Prueba
SET IDENTITY_INSERT matricula ON;
INSERT INTO matricula (id_matricula, id_estudiante, id_curso) VALUES 
(1, 4, 1);
SET IDENTITY_INSERT matricula OFF;
GO