
/*
    Esquema para Azure SQL Database
    Base de datos esperada: GestionAcademicaDB

    IMPORTANTE:
    - Ejecutar conectado a GestionAcademicaDB.
    - No elimina tablas existentes.
    - Si encuentra alguna tabla del esquema, se detiene.
*/

SET NOCOUNT ON;

-- 1. Validar que no existan ya tablas del esquema.
IF OBJECT_ID('dbo.rol', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.usuario', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.periodo_academico', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.curso', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.matricula', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.material_didactico', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.actividad_evaluativa', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.entrega_evaluacion', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.pago', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.factura', 'U') IS NOT NULL
   OR OBJECT_ID('dbo.detalle_pago', 'U') IS NOT NULL
BEGIN
    THROW 50001,
        'El esquema ya contiene una o mas tablas. No se realizaron cambios.',
        1;
END;

-- 2. Roles.
CREATE TABLE dbo.rol (
                         id_rol INT IDENTITY(1,1) PRIMARY KEY,
                         nombre_rol VARCHAR(50) NOT NULL UNIQUE,
                         descripcion VARCHAR(MAX) NULL
);

-- 3. Usuarios.
CREATE TABLE dbo.usuario (
                             id_usuario INT IDENTITY(1,1) PRIMARY KEY,
                             nombre VARCHAR(100) NOT NULL,
                             apellido VARCHAR(100) NOT NULL,
                             email VARCHAR(150) NOT NULL UNIQUE,
                             password VARCHAR(255) NOT NULL,
                             id_rol INT NOT NULL,
                             activo BIT DEFAULT 1,
                             CONSTRAINT FK_Usuario_Rol
                                 FOREIGN KEY (id_rol) REFERENCES dbo.rol(id_rol)
);

-- 4. Periodos académicos.
CREATE TABLE dbo.periodo_academico (
                                       id_periodo INT IDENTITY(1,1) PRIMARY KEY,
                                       nombre_periodo VARCHAR(50) NOT NULL,
                                       fecha_inicio DATE NOT NULL,
                                       fecha_fin DATE NOT NULL,
                                       estado VARCHAR(20) DEFAULT 'Activo'
);

-- 5. Cursos.
CREATE TABLE dbo.curso (
                           id_curso INT IDENTITY(1,1) PRIMARY KEY,
                           nombre_curso VARCHAR(100) NOT NULL,
                           codigo_curso VARCHAR(20) NOT NULL UNIQUE,
                           descripcion VARCHAR(MAX) NULL,
    id_periodo INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    id_instructor INT NULL,
    CONSTRAINT FK_Curso_Periodo
        FOREIGN KEY (id_periodo)
        REFERENCES dbo.periodo_academico(id_periodo),
    CONSTRAINT FK_Curso_Instructor
        FOREIGN KEY (id_instructor)
        REFERENCES dbo.usuario(id_usuario)
);

-- 6. Matrículas.
CREATE TABLE dbo.matricula (
                               id_matricula INT IDENTITY(1,1) PRIMARY KEY,
                               fecha_matricula DATE DEFAULT GETDATE(),
                               estado_matricula VARCHAR(20) DEFAULT 'ACTIVA',
                               id_estudiante VARCHAR(50) NOT NULL,
                               id_curso INT NOT NULL,
                               CONSTRAINT FK_Matricula_Curso
                                   FOREIGN KEY (id_curso) REFERENCES dbo.curso(id_curso)
);

-- 7. Evitar matrículas vigentes duplicadas.
CREATE UNIQUE INDEX UX_matricula_estudiante_curso_vigente
    ON dbo.matricula (id_estudiante, id_curso)
    WHERE estado_matricula <> 'CANCELADA';

-- 8. Material didáctico.
CREATE TABLE dbo.material_didactico (
                                        id_material INT IDENTITY(1,1) PRIMARY KEY,
                                        titulo VARCHAR(150) NOT NULL,
                                        descripcion VARCHAR(MAX) NULL,
    url_enlace VARCHAR(MAX) NOT NULL,
    id_curso INT NOT NULL,
    CONSTRAINT FK_Material_Curso
        FOREIGN KEY (id_curso) REFERENCES dbo.curso(id_curso)
);

-- 9. Actividades evaluativas.
CREATE TABLE dbo.actividad_evaluativa (
                                          id_actividad INT IDENTITY(1,1) PRIMARY KEY,
                                          nombre_actividad VARCHAR(150) NOT NULL,
                                          descripcion VARCHAR(MAX) NULL,
    fecha_limite DATE NULL,
    ponderacion DECIMAL(5,2) NOT NULL,
    id_curso INT NOT NULL,
    tipo_actividad VARCHAR(20) NOT NULL DEFAULT 'TAREA',
    CONSTRAINT CK_Actividad_Tipo
        CHECK (tipo_actividad IN ('TAREA', 'EXAMEN', 'PROYECTO')),
    CONSTRAINT FK_Actividad_Curso
        FOREIGN KEY (id_curso) REFERENCES dbo.curso(id_curso)
);

-- 10. Entregas de evaluaciones.
CREATE TABLE dbo.entrega_evaluacion (
                                        id_entrega INT IDENTITY(1,1) PRIMARY KEY,
                                        ruta_archivo_entrega VARCHAR(MAX) NULL,
    fecha_entrega DATE NULL,
    calificacion DECIMAL(5,2) DEFAULT 0.0,
    observaciones VARCHAR(MAX) NULL,
    id_actividad INT NOT NULL,
    id_estudiante VARCHAR(50) NOT NULL,
    CONSTRAINT FK_Entrega_Actividad
        FOREIGN KEY (id_actividad)
        REFERENCES dbo.actividad_evaluativa(id_actividad)
);

-- 11. Pagos.
CREATE TABLE dbo.pago (
                          id_pago INT IDENTITY(1,1) PRIMARY KEY,
                          monto DECIMAL(10,2) NOT NULL,
                          fecha_pago DATE NULL,
                          ruta_comprobante VARCHAR(MAX) NULL,
    estado_pago VARCHAR(20) DEFAULT 'PENDIENTE',
    id_estudiante VARCHAR(50) NOT NULL,
    id_asesor_validador VARCHAR(50) NULL,
    id_matricula INT NULL,
    CONSTRAINT FK_Pago_Matricula
        FOREIGN KEY (id_matricula)
        REFERENCES dbo.matricula(id_matricula)
        ON DELETE SET NULL
);

-- 12. Facturas.
CREATE TABLE dbo.factura (
                             id_factura INT IDENTITY(1,1) PRIMARY KEY,
                             numero_factura VARCHAR(50) NOT NULL UNIQUE,
                             fecha_emision DATE NOT NULL,
                             monto_total DECIMAL(10,2) NOT NULL,
                             id_pago INT NULL,
                             CONSTRAINT FK_Factura_Pago
                                 FOREIGN KEY (id_pago)
                                     REFERENCES dbo.pago(id_pago)
                                     ON DELETE SET NULL
);

-- 13. Detalles de pago.
CREATE TABLE dbo.detalle_pago (
                                  id_detalle INT IDENTITY(1,1) PRIMARY KEY,
                                  id_pago INT NOT NULL,
                                  concepto VARCHAR(150) NOT NULL,
                                  subtotal DECIMAL(10,2) NOT NULL,
                                  CONSTRAINT FK_DetallePago_Pago
                                      FOREIGN KEY (id_pago)
                                          REFERENCES dbo.pago(id_pago)
                                          ON DELETE CASCADE
);

PRINT 'Esquema creado correctamente.';
