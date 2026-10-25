package sv.gestionacademica.service;

import org.junit.jupiter.api.Test;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.service.ProcesoConcurrenteService;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProcesoConcurrenteServiceTest {

    @Test
    public void testEjecutarProcesoConcurrente() {

        DataSource dataSource = DataSourceConfig.getDataSource();

        assertNotNull(dataSource, "El DataSource no debe ser nulo. Verifique la configuración de la base de datos.");


        ProcesoConcurrenteService service = new ProcesoConcurrenteService(dataSource);


        assertDoesNotThrow(() -> {
            service.ejecutarProcesoConcurrente();
        }, "El proceso concurrente falló durante su ejecución en paralelo.");

        System.out.println("¡Prueba unitaria del Frente 7 (Concurrencia) ejecutada y superada con éxito!");
    }
}