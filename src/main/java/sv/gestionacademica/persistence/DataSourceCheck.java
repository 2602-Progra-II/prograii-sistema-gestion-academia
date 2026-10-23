
package sv.gestionacademica.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataSourceCheck {

    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 3; i++) {

            try (Connection con = DataSourceConfig.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DB_NAME()")) {

                rs.next();
                System.out.println(
                        "Conexión " + i + " OK -> base: " + rs.getString(1)
                );
            }

        }

        DataSourceConfig.close();
        System.out.println("Pool cerrado correctamente.");
    }
}
