-- =========================================================================
-- Script de Datos Semilla (Seed Data) - Sistema de Gestión Academia
-- Motor: SQL Server / SQL Server Express
-- =========================================================================

USE sistema_gestion_academia;
GO

-- 1. Roles
INSERT INTO rol (nombre_rol, descripcion) VALUES 
('Administrador', 'Control total del sistema académico y gestión de usuarios'),
('Profesor', 'Gestión de cursos, calificaciones y materiales didácticos'),
('Estudiante', 'Inscripción a cursos, entrega de evaluaciones y pagos');
GO

-- 2. Usuarios
INSERT INTO usuario (nombre, apellido, email, password, id_rol, activo) VALUES 
('Carlos', 'Mendoza', 'admin@academia.edu', 'Admin123*', 1, 1),
('Ana', 'García', 'ana.garcia@academia.edu', 'Prof123*', 2, 1),
('Roberto', 'Gutiérrez', 'roberto.gutierrez@academia.edu', 'Est123*', 3, 1),
('Fransheska', 'Pérez', 'fransheska.perez@academia.edu', 'Est123*', 3, 1);
GO

-- 3. Períodos Académicos
INSERT INTO periodo_academico (nombre_periodo, fecha_inicio, fecha_fin, estado) VALUES 
('Ciclo I - 2026', '2026-02-01', '2026-06-30', 'Activo'),
('Ciclo II - 2026', '2026-08-01', '2026-12-15', 'Planificacion');
GO

-- 4. Cursos
INSERT INTO curso (nombre_curso, codigo_curso, descripcion, id_periodo) VALUES 
('Programación II', 'PROG-II', 'Curso avanzado de programación en Java, estructuras y bases de datos.', 1),
('Cálculo Diferencial', 'MAT-CAL', 'Estudio de límites, derivadas, optimización y teoremas fundamentales.', 1);
GO

-- 5. Matrículas
INSERT INTO matricula (id_estudiante, id_curso, fecha_matricula) VALUES 
(3, 1, '2026-02-02'),
(3, 2, '2026-02-02'),
(4, 1, '2026-02-03');
GO

-- 6. Material Didáctico
INSERT INTO material_didactico (titulo, enlace_archivo, id_curso) VALUES 
('Guía de JDBC y Conexión SQL Server', '/docs/materiales/guia_jdbc.pdf', 1),
('Formulario de Derivadas y Límites', '/docs/materiales/formulario_calculo.pdf', 2);
GO

-- 7. Actividades Evaluativas
INSERT INTO actividad_evaluativa (titulo, descripcion, ponderacion, fecha_entrega, id_curso) VALUES 
('Entrega 3: Sistema de Gestión Academia', 'Desarrollo de capa JDBC y base de datos SQL Server', 35.00, '2026-11-15 23:59:00', 1),
('Laboratorio Parcial de Límites', 'Resolución de problemas de derivadas y razón de cambio', 20.00, '2026-05-10 18:00:00', 2);
GO

-- 8. Entregas de Evaluación
INSERT INTO entrega_evaluacion (id_actividad, id_estudiante, archivo_entrega, calificacion, retroalimentacion, fecha_subida) VALUES 
(1, 3, '/uploads/entregas/roberto_prog2.zip', NULL, NULL, '2026-10-18 14:30:00');
GO

-- 9. Facturas
INSERT INTO factura (id_estudiante, numero_factura, monto_total, fecha_emision, estado) VALUES 
(3, 'FAC-2026-001', 150.00, '2026-02-05 09:00:00', 'Pagada');
GO

-- 10. Pagos
INSERT INTO pago (id_factura, id_estudiante, monto_pagado, fecha_pago, metodo_pago) VALUES 
(1, 3, 150.00, '2026-02-05 10:00:00', 'Tarjeta de Débito');
GO

-- 11. Detalle de Pago
INSERT INTO detalle_pago (id_pago, concepto, subtotal) VALUES 
(1, 'Matrícula Ciclo I - 2026', 100.00),
(1, 'Carnetización y Aranceles', 50.00);
GO