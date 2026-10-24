-- =========================================================================
-- Script de Creación de Base de Datos - Sistema de Gestión Academia
-- Motor: SQL Server / SQL Server Express
-- Reejecución limpia mediante DROP IF EXISTS y tipos VARCHAR(MAX)
-- =========================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'sistema_gestion_academia')
    CREATE DATABASE sistema_gestion_academia;
GO
USE sistema_gestion_academia;
GO

-- 1. Eliminar restricciones de clave foránea (FK) para evitar bloqueos al recrear
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql += N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + N'.' + QUOTENAME(OBJECT_NAME(parent_object_id)) + N' DROP CONSTRAINT ' + QUOTENAME(name) + N';' + CHAR(13)
FROM sys.foreign_keys;
EXEC sp_executesql @sql;
GO

-- 2. Eliminar tablas en orden inverso a sus dependencias
IF OBJECT_ID('dbo.detalle_pago', 'U') IS NOT NULL DROP TABLE dbo.detalle_pago;
IF OBJECT_ID('dbo.pago', 'U') IS NOT NULL DROP TABLE dbo.pago;
IF OBJECT_ID('dbo.factura', 'U') IS NOT NULL DROP TABLE dbo.factura;
IF OBJECT_ID('dbo.entrega_evaluacion', 'U') IS NOT NULL DROP TABLE dbo.entrega_evaluacion;
IF OBJECT_ID('dbo.actividad_evaluativa', 'U') IS NOT NULL DROP TABLE dbo.actividad_evaluativa;
IF OBJECT_ID('dbo.material_didactico', 'U') IS NOT NULL DROP TABLE dbo.material_didactico;
IF OBJECT_ID('dbo.matricula', 'U') IS NOT NULL DROP TABLE dbo.matricula;
IF OBJECT_ID('dbo.curso', 'U') IS NOT NULL DROP TABLE dbo.curso;
IF OBJECT_ID('dbo.periodo_academico', 'U') IS NOT NULL DROP TABLE dbo.periodo_academico;
IF OBJECT_ID('dbo.usuario', 'U') IS NOT NULL DROP TABLE dbo.usuario;
IF OBJECT_ID('dbo.rol', 'U') IS NOT NULL DROP TABLE dbo.rol;
GO

-- 3. Creación de Tablas sincronizadas con la capa JDBC
CREATE TABLE rol (
                     id_rol INT IDENTITY(1,1) PRIMARY KEY,
                     nombre_rol VARCHAR(50) NOT NULL UNIQUE,
                     descripcion VARCHAR(MAX) NULL
);
GO

CREATE TABLE usuario (
                         id_usuario INT IDENTITY(1,1) PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         apellido VARCHAR(100) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         id_rol INT NOT NULL,
                         activo BIT DEFAULT 1,
                         CONSTRAINT FK_Usuario_Rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);
GO

CREATE TABLE periodo_academico (
                                   id_periodo INT IDENTITY(1,1) PRIMARY KEY,
                                   nombre_periodo VARCHAR(50) NOT NULL,
                                   fecha_inicio DATE NOT NULL,
                                   fecha_fin DATE NOT NULL,
                                   estado VARCHAR(20) DEFAULT 'Activo'
);
GO

CREATE TABLE curso (
                       id_curso INT IDENTITY(1,1) PRIMARY KEY,
                       nombre_curso VARCHAR(100) NOT NULL,
                       codigo_curso VARCHAR(20) NOT NULL UNIQUE,
                       descripcion VARCHAR(MAX) NULL,
    id_periodo INT NOT NULL,
    CONSTRAINT FK_Curso_Periodo FOREIGN KEY (id_periodo) REFERENCES periodo_academico(id_periodo)
);
GO

CREATE TABLE matricula (
                           id_matricula INT IDENTITY(1,1) PRIMARY KEY,
                           fecha_matricula DATE DEFAULT GETDATE(),
                           estado_matricula VARCHAR(20) DEFAULT 'ACTIVA',
                           id_estudiante VARCHAR(50) NOT NULL,
                           id_curso INT NOT NULL,
                           CONSTRAINT FK_Matricula_Curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
);
GO

CREATE TABLE material_didactico (
                                    id_material INT IDENTITY(1,1) PRIMARY KEY,
                                    titulo VARCHAR(150) NOT NULL,
                                    descripcion VARCHAR(MAX) NULL,
    url_enlace VARCHAR(MAX) NOT NULL,
    id_curso INT NOT NULL,
    CONSTRAINT FK_Material_Curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
);
GO

CREATE TABLE actividad_evaluativa (
                                      id_actividad INT IDENTITY(1,1) PRIMARY KEY,
                                      nombre_actividad VARCHAR(150) NOT NULL,
                                      descripcion VARCHAR(MAX) NULL,
    fecha_limite DATE NULL,
    ponderacion DECIMAL(5,2) NOT NULL,
    id_curso INT NOT NULL,
    CONSTRAINT FK_Actividad_Curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
);
GO

CREATE TABLE entrega_evaluacion (
                                    id_entrega INT IDENTITY(1,1) PRIMARY KEY,
                                    ruta_archivo_entrega VARCHAR(MAX) NULL,
    fecha_entrega DATE NULL,
    calificacion DECIMAL(5,2) DEFAULT 0.0,
    observaciones VARCHAR(MAX) NULL,
    id_actividad INT NOT NULL,
    id_estudiante VARCHAR(50) NOT NULL,
    CONSTRAINT FK_Entrega_Actividad FOREIGN KEY (id_actividad) REFERENCES actividad_evaluativa(id_actividad)
);
GO

CREATE TABLE factura (
                         id_factura INT IDENTITY(1,1) PRIMARY KEY,
                         numero_factura VARCHAR(50) NOT NULL UNIQUE,
                         fecha_emision DATE NOT NULL,
                         monto_total DECIMAL(10,2) NOT NULL
);
GO

CREATE TABLE pago (
                      id_pago INT IDENTITY(1,1) PRIMARY KEY,
                      monto DECIMAL(10,2) NOT NULL,
                      fecha_pago DATE NULL,
                      ruta_comprobante VARCHAR(MAX) NULL,
    estado_pago VARCHAR(20) DEFAULT 'PENDIENTE',
    id_estudiante VARCHAR(50) NOT NULL,
    id_asesor_validador VARCHAR(50) NULL
);
GO

CREATE TABLE detalle_pago (
                              id_detalle INT IDENTITY(1,1) PRIMARY KEY,
                              id_pago INT NOT NULL,
                              concepto VARCHAR(150) NOT NULL,
                              subtotal DECIMAL(10,2) NOT NULL,
                              CONSTRAINT FK_DetallePago_Pago FOREIGN KEY (id_pago) REFERENCES pago(id_pago)
);
GO