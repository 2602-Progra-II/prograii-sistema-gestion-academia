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
('Carlos', 'Mendoza', 'admin@academia.edu', 'Admin123', 1, 1),
('Ana', 'García', 'ana.garcia@academia.edu', 'Prof123', 2, 1),
('Roberto', 'Gutiérrez', 'roberto.gutierrez@academia.edu', 'Est123', 3, 1),
('Fransheska', 'Pérez', 'fransheska.perez@academia.edu', 'Est123', 3, 1);
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

-- 5. Matrículas (Alineado con MatriculaRepositoryImpl)
INSERT INTO matricula (fecha_matricula, estado_matricula, id_estudiante, id_curso) VALUES
('2026-02-02', 'ACTIVA', 'roberto.gutierrez@academia.edu', 1),
('2026-02-02', 'ACTIVA', 'roberto.gutierrez@academia.edu', 2),
('2026-02-03', 'ACTIVA', 'fransheska.perez@academia.edu', 1);
GO

-- 6. Material Didáctico (Alineado con MaterialRepositoryImpl)
INSERT INTO material_didactico (titulo, descripcion, url_enlace, id_curso) VALUES
('Guía de JDBC y Conexión SQL Server', 'Guía completa para conectar Java con SQL Server usando JDBC.', '/docs/materiales/guia_jdbc.pdf', 1),
('Formulario de Derivadas y Límites', 'Fórmulas esenciales para cálculo diferencial.', '/docs/materiales/formulario_calculo.pdf', 2);
GO

-- 7. Actividades Evaluativas (Alineado con ActividadRepositoryImpl)
INSERT INTO actividad_evaluativa (nombre_actividad, descripcion, fecha_limite, ponderacion, id_curso) VALUES
('Entrega 3: Sistema de Gestión Academia', 'Desarrollo de capa JDBC y base de datos SQL Server', '2026-11-15', 35.00, 1),
('Laboratorio Parcial de Límites', 'Resolución de problemas de derivadas y razón de cambio', '2026-05-10', 20.00, 2);
GO

-- 8. Entregas de Evaluación (Alineado con EntregaRepositoryImpl)
INSERT INTO entrega_evaluacion (ruta_archivo_entrega, fecha_entrega, calificacion, observaciones, id_actividad, id_estudiante) VALUES
('/uploads/entregas/roberto_prog2.zip', '2026-10-18', 0.0, 'Pendiente de revisión', 1, 'roberto.gutierrez@academia.edu');
GO

-- 9. Facturas (Alineado con FacturaRepositoryImpl)
INSERT INTO factura (numero_factura, fecha_emision, monto_total) VALUES
('FAC-2026-001', '2026-02-05', 150.00);
GO

-- 10. Pagos (Alineado con PagoRepositoryImpl)
INSERT INTO pago (monto, fecha_pago, ruta_comprobante, estado_pago, id_estudiante, id_asesor_validador) VALUES
(150.00, '2026-02-05', '/comprobantes/pago_001.pdf', 'PENDIENTE', 'roberto.gutierrez@academia.edu', 'admin@academia.edu');
GO

-- 11. Detalle de Pago
INSERT INTO detalle_pago (id_pago, concepto, subtotal) VALUES
(1, 'Matrícula Ciclo I - 2026', 100.00),
(1, 'Carnetización y Aranceles', 50.00);
GO