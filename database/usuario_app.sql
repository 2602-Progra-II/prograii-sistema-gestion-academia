-- =========================================================================
-- Usuario de SQL Server con el que se conecta la aplicacion (DataSourceConfig).
-- Ejecutar UNA vez, DESPUES de schema.sql, con una cuenta administradora (ej. sa).
-- Requiere que SQL Server tenga habilitada la autenticacion mixta
-- (SQL Server y Windows). Si cambias la contraseña, usa las variables
-- de entorno DB_USER / DB_PASSWORD al ejecutar la aplicacion.
-- =========================================================================
USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'academia_app')
    CREATE LOGIN academia_app WITH PASSWORD = 'Academia2026_Segura!', CHECK_POLICY = OFF;
GO

USE sistema_gestion_academia;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'academia_app')
    CREATE USER academia_app FOR LOGIN academia_app;
GO
-- db_owner: las pruebas de integracion crean y borran registros.
ALTER ROLE db_owner ADD MEMBER academia_app;
GO
