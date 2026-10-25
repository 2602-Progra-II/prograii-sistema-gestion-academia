
/*
    Datos semilla para Azure SQL Database.
    Base de datos: GestionAcademicaDB

    Ejecutar después de azure_schema.sql.
    Diseñado para una base recién creada y vacía.
    No elimina tablas ni registros.
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Comprobar que las tablas estén vacías.
IF EXISTS (SELECT 1 FROM dbo.rol)
   OR EXISTS (SELECT 1 FROM dbo.usuario)
   OR EXISTS (SELECT 1 FROM dbo.periodo_academico)
   OR EXISTS (SELECT 1 FROM dbo.curso)
   OR EXISTS (SELECT 1 FROM dbo.matricula)
   OR EXISTS (SELECT 1 FROM dbo.material_didactico)
   OR EXISTS (SELECT 1 FROM dbo.actividad_evaluativa)
   OR EXISTS (SELECT 1 FROM dbo.entrega_evaluacion)
   OR EXISTS (SELECT 1 FROM dbo.pago)
   OR EXISTS (SELECT 1 FROM dbo.factura)
   OR EXISTS (SELECT 1 FROM dbo.detalle_pago)
BEGIN
    THROW 50002,
        'Hay registros existentes. No se insertaron datos semilla.',
        1;
END;

BEGIN TRY
BEGIN TRANSACTION;

    -- 1. Roles
INSERT INTO dbo.rol (nombre_rol, descripcion)
VALUES
    ('Administrador', 'Control total del sistema académico y gestión de usuarios'),
    ('Profesor', 'Gestión de cursos, calificaciones y materiales didácticos'),
    ('Estudiante', 'Inscripción a cursos, entrega de evaluaciones y pagos');

-- 2. Usuarios
INSERT INTO dbo.usuario
(nombre, apellido, email, password, id_rol, activo)
VALUES
    ('Carlos', 'Mendoza', 'admin@academia.edu', 'Admin123', 1, 1),
    ('Ana', 'García', 'ana.garcia@academia.edu', 'Prof123', 2, 1),
    ('Roberto', 'Gutiérrez', 'roberto.gutierrez@academia.edu', 'Est123', 3, 1),
    ('Fransheska', 'Pérez', 'fransheska.perez@academia.edu', 'Est123', 3, 1);

-- 3. Periodos académicos
INSERT INTO dbo.periodo_academico
(nombre_periodo, fecha_inicio, fecha_fin, estado)
VALUES
    ('Ciclo I - 2026', '2026-02-01', '2026-06-30', 'Activo'),
    ('Ciclo II - 2026', '2026-08-01', '2026-12-15', 'Planificacion');

-- 4. Cursos
INSERT INTO dbo.curso
(nombre_curso, codigo_curso, descripcion, id_periodo, estado, id_instructor)
VALUES
    ('Programación II', 'PROG-II',
     'Curso avanzado de programación en Java, estructuras y bases de datos.',
     1, 'ACTIVO', 2),
    ('Cálculo Diferencial', 'MAT-CAL',
     'Estudio de límites, derivadas, optimización y teoremas fundamentales.',
     1, 'ACTIVO', 2);

-- 5. Matrículas
INSERT INTO dbo.matricula
(fecha_matricula, estado_matricula, id_estudiante, id_curso)
VALUES
    ('2026-02-02', 'ACTIVA', '3', 1),
    ('2026-02-02', 'ACTIVA', '3', 2),
    ('2026-02-03', 'ACTIVA', '4', 1);

-- 6. Material didáctico
INSERT INTO dbo.material_didactico
(titulo, descripcion, url_enlace, id_curso)
VALUES
    ('Guía de JDBC y Conexión SQL Server',
     'Guía completa para conectar Java con SQL Server usando JDBC.',
     '/docs/materiales/guia_jdbc.pdf', 1),
    ('Formulario de Derivadas y Límites',
     'Fórmulas esenciales para cálculo diferencial.',
     '/docs/materiales/formulario_calculo.pdf', 2);

-- 7. Actividades evaluativas
INSERT INTO dbo.actividad_evaluativa
(nombre_actividad, descripcion, fecha_limite, ponderacion, id_curso, tipo_actividad)
VALUES
    ('Entrega 3: Sistema de Gestión Academia',
     'Desarrollo de capa JDBC y base de datos SQL Server',
     '2026-11-15', 35.00, 1, 'PROYECTO'),
    ('Laboratorio Parcial de Límites',
     'Resolución de problemas de derivadas y razón de cambio',
     '2026-05-10', 20.00, 2, 'EXAMEN'),
    ('Tarea 1: Colecciones y genéricos',
     'Ejercicios con List, Map y clases genéricas',
     '2026-10-20', 15.00, 1, 'TAREA');

-- 8. Entregas de evaluación
INSERT INTO dbo.entrega_evaluacion
(ruta_archivo_entrega, fecha_entrega, calificacion,
 observaciones, id_actividad, id_estudiante)
VALUES
    ('/uploads/entregas/roberto_prog2.zip',
     '2026-10-18', 0.0, NULL, 1, '3');

-- 9. Pagos
INSERT INTO dbo.pago
(monto, fecha_pago, ruta_comprobante, estado_pago,
 id_estudiante, id_asesor_validador, id_matricula)
VALUES
    (150.00, '2026-02-05', '/comprobantes/pago_001.pdf',
     'APROBADO', '3', '1', 1);

-- 10. Facturas
INSERT INTO dbo.factura
(numero_factura, fecha_emision, monto_total, id_pago)
VALUES
    ('FAC-2026-000001', '2026-02-05', 150.00, 1);

-- 11. Detalles de pago
INSERT INTO dbo.detalle_pago (id_pago, concepto, subtotal)
VALUES
    (1, 'Matrícula Ciclo I - 2026', 100.00),
    (1, 'Carnetización y Aranceles', 50.00);

COMMIT TRANSACTION;

PRINT 'Datos semilla insertados correctamente.';
END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
