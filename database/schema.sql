-- Creación de la base de datos (opcional si aún no ha sido creada)
-- CREATE DATABASE sistema_gestion_academia;
-- GO
-- USE sistema_gestion_academia;
-- GO

-- 1. Tabla Rol
CREATE TABLE rol (
    id_rol INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);
GO

-- 2. Tabla Usuario
CREATE TABLE usuario (
    id_usuario INT IDENTITY(1,1) PRIMARY KEY,
    nombre_usuario VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    id_rol INT NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);
GO

-- 3. Tabla Periodo Académico
CREATE TABLE periodo_academico (
    id_periodo INT IDENTITY(1,1) PRIMARY KEY,
    nombre_periodo VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO'
);
GO

-- 4. Tabla Curso
CREATE TABLE curso (
    id_curso INT IDENTITY(1,1) PRIMARY KEY,
    codigo_curso VARCHAR(50) NOT NULL UNIQUE,
    nombre_curso VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_periodo INT NOT NULL,
    id_instructor INT,
    CONSTRAINT fk_curso_periodo FOREIGN KEY (id_periodo) REFERENCES periodo_academico(id_periodo),
    CONSTRAINT fk_curso_instructor FOREIGN KEY (id_instructor) REFERENCES usuario(id_usuario)
);
GO

-- 5. Tabla Matrícula
CREATE TABLE matricula (
    id_matricula INT IDENTITY(1,1) PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_curso INT NOT NULL,
    fecha_matricula DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_matricula_estudiante FOREIGN KEY (id_estudiante) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_matricula_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT uq_estudiante_curso UNIQUE (id_estudiante, id_curso)
);
GO

-- 6. Tabla Pago
CREATE TABLE pago (
    id_pago INT IDENTITY(1,1) PRIMARY KEY,
    id_estudiante INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_pago DATETIME DEFAULT GETDATE(),
    ruta_comprobante VARCHAR(255),
    estado_pago VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    id_asesor_validador INT,
    CONSTRAINT fk_pago_estudiante FOREIGN KEY (id_estudiante) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_pago_asesor FOREIGN KEY (id_asesor_validador) REFERENCES usuario(id_usuario)
);
GO

-- 7. Tabla Factura
CREATE TABLE factura (
    id_factura INT IDENTITY(1,1) PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE,
    id_pago INT NOT NULL,
    id_estudiante INT NOT NULL,
    fecha_emision DATETIME DEFAULT GETDATE(),
    monto_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_factura_pago FOREIGN KEY (id_pago) REFERENCES pago(id_pago),
    CONSTRAINT fk_factura_estudiante FOREIGN KEY (id_estudiante) REFERENCES usuario(id_usuario)
);
GO

-- 8. Tabla Material Didáctico
CREATE TABLE material_didactico (
    id_material INT IDENTITY(1,1) PRIMARY KEY,
    id_curso INT NOT NULL,
    titulo_material VARCHAR(150) NOT NULL,
    ruta_archivo VARCHAR(255) NOT NULL,
    fecha_subida DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_material_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
);
GO

-- 9. Tabla Actividad Evaluativa
CREATE TABLE actividad_evaluativa (
    id_actividad INT IDENTITY(1,1) PRIMARY KEY,
    id_curso INT NOT NULL,
    nombre_actividad VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_limite DATETIME NOT NULL,
    ponderacion DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_actividad_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso)
);
GO

-- 10. Tabla Entrega de Evaluación
CREATE TABLE entrega_evaluacion (
    id_entrega INT IDENTITY(1,1) PRIMARY KEY,
    id_actividad INT NOT NULL,
    id_estudiante INT NOT NULL,
    ruta_archivo_entrega VARCHAR(255),
    fecha_entrega DATETIME DEFAULT GETDATE(),
    calificacion DECIMAL(5,2),
    observaciones TEXT,
    CONSTRAINT fk_entrega_actividad FOREIGN KEY (id_actividad) REFERENCES actividad_evaluativa(id_actividad),
    CONSTRAINT fk_entrega_estudiante FOREIGN KEY (id_estudiante) REFERENCES usuario(id_usuario)
);
GO