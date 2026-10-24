Frente 7: Concurrencia Integrada con Base de Datos


Responsable:Bryan Reyes  
Actividades Agrupadas:11 y 12


Se identificó la necesidad de procesar operaciones masivas o solicitudes independientes del sistema académico (como la validación o procesamiento por lotes de registros) que se benefician directamente de la ejecución en paralelo para reducir el tiempo de respuesta global.


Uso de `ExecutorService`:** En lugar de crear hilos independientes de manera descontrolada, se implementó un pool de hilos (`Executors.newFixedThreadPool`) que administra de forma eficiente los recursos del procesador.
Optimización del Rendimiento:** Las tareas de consulta o inserción en la base de datos se ejecutan concurrentemente, evitando que el hilo principal se bloquee mientras espera respuestas individuales de la BD.
Uso del `DataSource`:** Cada hilo concurrente solicita su propia conexión de forma segura al pool configurado en el sistema.
Prevención de Fugas (Leaks):* Se implementó la estructura `try-with-resources` en cada tarea, garantizando que los objetos `Connection` y `PreparedStatement` se cierren y se devuelvan al pool automáticamente al finalizar su uso, incluso si ocurre una excepción de SQL.