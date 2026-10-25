
/*
    Usuario de aplicación para Azure SQL Database.
    Ejecutar conectado a GestionAcademicaDB.

    El usuario tendrá permisos de lectura y escritura
    sobre los datos, sin permisos administrativos.
*/

CREATE USER academia_app WITH PASSWORD = 'REEMPLAZAR_POR_UNA_CLAVE_NUEVA';

ALTER ROLE db_datareader ADD MEMBER academia_app;
ALTER ROLE db_datawriter ADD MEMBER academia_app;

PRINT 'Usuario de aplicación creado con permisos de lectura y escritura.';
