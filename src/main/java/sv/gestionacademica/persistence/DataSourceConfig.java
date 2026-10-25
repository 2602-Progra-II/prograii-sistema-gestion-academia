package sv.gestionacademica.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DataSourceConfig {

    private static HikariDataSource dataSource;

    private DataSourceConfig() {
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(requireEnv("DB_URL"));
            config.setUsername(requireEnv("DB_USER"));
            config.setPassword(requireEnv("DB_PASSWORD"));

            config.setPoolName("AcademiaPool");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30_000);
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);

            dataSource = new HikariDataSource(config);
        }

        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }


    private static String requireEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno: " + name
            );
        }

        return value;
    }

}