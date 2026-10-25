# Frente 7: Concurrencia integrada con la base de datos

**Responsable:** Bryan Reyes · **Actividades:** 11 y 12
**Clase:** `sv.gestionacademica.service.ProcesoConcurrenteService`
**Menú:** Administrador → opción 6, *Cierre de cartera concurrente*

## 1. Operación real elegida: cierre de cartera

Al cerrar un período, la academia necesita el **estado de cuenta de todos los estudiantes**: cuánto deben (pagos `PENDIENTE`), cuánto pagaron (pagos `APROBADO`) y quién está en mora (algún pago pendiente con más de 30 días).

El cálculo de cada estudiante **no depende del de ningún otro**: solo lee los pagos de ese estudiante. Por eso el trabajo se divide en una tarea por estudiante, y las tareas se ejecutan en paralelo.

## 2. Por qué conviene la concurrencia

Cada tarea pasa casi todo su tiempo **esperando a la base de datos**: abre la conexión, ejecuta la consulta y espera la respuesta. Si se hiciera en secuencia, el tiempo total sería la suma de las esperas. Con 4 hilos, mientras uno espera a SQL Server, los otros ya están consultando, y el tiempo total se acerca a *esperas / 4*.

## 3. Diseño

```
Hilo principal                       Pool de 4 hilos (ExecutorService)
──────────────                       ─────────────────────────────────
1. Lee la lista de estudiantes  ──►  Tarea(estudiante 1): conexión propia → SELECT agregado → resultado
   (1 conexión, luego la devuelve)   Tarea(estudiante 2): conexión propia → SELECT agregado → resultado
2. invokeAll(tareas)                 ...
3. Espera a TODAS (timeout 60 s)     Cada conexión se devuelve al pool (try-with-resources)
4. Arma el ResultadoCierreCartera
```

- **`ExecutorService` (`Executors.newFixedThreadPool(4)`)** en lugar de `Thread` sueltos: reutiliza hilos y limita cuántos corren a la vez.
- **`Callable` + `invokeAll`**: `invokeAll` bloquea hasta que terminan todas las tareas, con lo que se coordina la finalización. Después, `shutdownNow()` en un `finally`.
- **4 hilos frente a 10 conexiones del pool (HikariCP)**: el proceso nunca ocupa todas las conexiones y deja libres las que necesita el resto de la aplicación.
- **Una sola consulta por estudiante**, con agregados (`SUM(CASE ...)`, `MIN(...)`) y `PreparedStatement`.

## 4. Conexiones y DataSource

- Cada tarea llama a `dataSource.getConnection()` **dentro de su propio hilo**.
- Una `Connection` **no se comparte entre hilos**, porque no es thread-safe.
- `try-with-resources` cierra `ResultSet`, `PreparedStatement` y `Connection`, y con eso la conexión vuelve al pool **incluso si la consulta falla**. La prueba `CierreCarteraConcurrenteTest` comprueba que el número de conexiones abiertas sea igual al de cerradas, también cuando una tarea falla.

## 5. Recursos compartidos y condiciones de carrera

| Recurso compartido | Riesgo si se hiciera mal | Solución |
|---|---|---|
| Mapa de resultados | Con `HashMap`, dos `put` simultáneos pueden corromper la estructura | `ConcurrentHashMap` (además, cada hilo escribe una clave distinta) |
| Totales pendiente/pagado | `total += x` no es atómico: dos hilos leen el mismo valor y se pierde una suma | `DoubleAdder` |
| Contadores de mora y errores | `contador++` tampoco es atómico | `AtomicInteger` |
| Base de datos | Escrituras concurrentes en conflicto | El proceso **solo lee**, así que no hay escrituras que choquen |

En el flujo transaccional (`FlujoMatriculaService`) sí hay concurrencia de escritura: dos asesores podrían aprobar el mismo pago a la vez. Ese caso se resuelve con `SELECT ... WITH (UPDLOCK, ROWLOCK)` y con el índice único `UX_matricula_estudiante_curso_vigente`.

## 6. Manejo de excepciones

- Si falla **un estudiante**, su `Future` lanza `ExecutionException`. Se cuenta como error y **las demás tareas continúan**.
- Si una tarea pasa del tiempo máximo, se cancela (`CancellationException`) y también se cuenta como error.
- Si el hilo principal es interrumpido, se restaura la bandera (`Thread.currentThread().interrupt()`) y se lanza `PersistenciaException`.
- Si falla la lectura inicial de estudiantes, se lanza `PersistenciaException`, que es un error técnico.

## 7. Pruebas

- **`CierreCarteraConcurrenteTest`** es una prueba unitaria sin BD, con un `DataSource` simulado que agrega 15 ms de latencia por consulta. Verifica los totales, la mora, que el trabajo se reparta en **varios hilos**, que **no haya fugas de conexiones** y que un fallo aislado no detenga el proceso.
- **`ProcesoConcurrenteServiceTest`** es la prueba de integración: ejecuta el cierre de cartera contra SQL Server con el `DataSource` real.
