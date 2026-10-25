
package sv.gestionacademica.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexionAzure {

    public static void main(String[] args) {
        try (Connection conexion = DataSourceConfig.getConnection();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(
                     "SELECT DB_NAME() AS base_datos, SUSER_SNAME() AS usuario"
             )) {

            if (resultado.next()) {
                System.out.println("¡Conexión exitosa!");
                System.out.println(
                        "Base de datos: " + resultado.getString("base_datos")
                );
                System.out.println(
                        "Usuario: " + resultado.getString("usuario")
                );
            }

        } catch (Exception e) {
            System.err.println("Error al conectar con Azure SQL:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            DataSourceConfig.close();
        }
    }
}
