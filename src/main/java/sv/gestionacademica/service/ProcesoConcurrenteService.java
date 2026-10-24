package sv.gestionacademica.service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProcesoConcurrenteService {

    private final DataSource dataSource;


    public ProcesoConcurrenteService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void ejecutarProcesoConcurrente() {

        ExecutorService executor = Executors.newFixedThreadPool(4);


        for (int i = 1; i <= 5; i++) {
            final int taskId = i;


            executor.submit(() -> {
                System.out.println("Hilo " + Thread.currentThread().getName() + " procesando tarea " + taskId);


                String sql = "SELECT 1";


                try (Connection conn = dataSource.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.executeQuery();
                    System.out.println("-> Tarea " + taskId + " ejecutada exitosamente con conexión de la BD.");

                } catch (SQLException e) {

                    System.err.println("Error en la tarea concurrente " + taskId + ": " + e.getMessage());
                }
            });
        }


        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("¡Proceso concurrente finalizado correctamente!");
    }
}