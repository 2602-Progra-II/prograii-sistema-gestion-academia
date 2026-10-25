package sv.gestionacademica.service;

import sv.gestionacademica.dto.ResultadoCierreCartera;
import sv.gestionacademica.entity.EstadoCuenta;
import sv.gestionacademica.exception.PersistenciaException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Frente 7 - Concurrencia integrada con la BD: CIERRE DE CARTERA.
 *
 * Recalcula el estado de cuenta de TODOS los estudiantes a partir de la tabla pago.
 * Cada estudiante es una tarea independiente (sus pagos no dependen de los de otro),
 * por eso el trabajo se reparte en un pool de hilos y cada tarea:
 *   1. pide su PROPIA conexion al DataSource (HikariCP) y la devuelve con try-with-resources,
 *   2. ejecuta una consulta agregada sobre sus pagos,
 *   3. publica su resultado en estructuras seguras para hilos.
 *
 * Recursos compartidos y condiciones de carrera:
 *   - resultados: ConcurrentHashMap (cada hilo escribe una clave distinta, sin bloqueos globales).
 *   - totales: DoubleAdder / AtomicInteger (sumas concurrentes sin perder actualizaciones;
 *     con un double normal y "total += x" dos hilos podrian pisarse).
 *   - Connection: NUNCA se comparte entre hilos (no es thread-safe); cada tarea abre la suya.
 *   - El proceso solo LEE la BD, asi que no hay escrituras que puedan entrar en conflicto.
 *
 * El numero de hilos se limita por debajo del maximo del pool de conexiones (10)
 * para no agotar conexiones que necesita el resto de la aplicacion.
 */
public class ProcesoConcurrenteService {

    public static final int HILOS_POR_DEFECTO = 4;
    private static final long TIEMPO_MAXIMO_SEGUNDOS = 60;

    private static final String SQL_ESTUDIANTES =
            "SELECT u.id_usuario, u.nombre, u.apellido FROM usuario u "
            + "INNER JOIN rol r ON u.id_rol = r.id_rol WHERE r.nombre_rol = 'Estudiante'";

    // id_estudiante es VARCHAR en pago: se compara como texto.
    private static final String SQL_CUENTA =
            "SELECT "
            + "SUM(CASE WHEN UPPER(estado_pago) = 'PENDIENTE' THEN monto ELSE 0 END) AS pendiente, "
            + "SUM(CASE WHEN UPPER(estado_pago) = 'APROBADO' THEN monto ELSE 0 END) AS pagado, "
            + "MIN(CASE WHEN UPPER(estado_pago) = 'PENDIENTE' THEN fecha_pago END) AS pendiente_mas_antiguo "
            + "FROM pago WHERE id_estudiante = ?";

    private final DataSource dataSource;
    private final int hilos;
    private final int diasGracia;

    public ProcesoConcurrenteService(DataSource dataSource) {
        this(dataSource, HILOS_POR_DEFECTO, EstadoCuentaService.DIAS_GRACIA_POR_DEFECTO);
    }

    public ProcesoConcurrenteService(DataSource dataSource, int hilos, int diasGracia) {
        if (hilos < 1) {
            throw new IllegalArgumentException("Se necesita al menos un hilo.");
        }
        this.dataSource = dataSource;
        this.hilos = hilos;
        this.diasGracia = diasGracia;
    }

    /** Ejecuta el cierre de cartera y muestra el resumen por consola (opcion del menu y prueba de integracion). */
    public ResultadoCierreCartera ejecutarProcesoConcurrente() {
        ResultadoCierreCartera r = ejecutarCierreDeCartera();
        System.out.println("\n--- Cierre de cartera (" + r.hilos() + " hilos, " + r.milisegundos() + " ms) ---");
        for (EstadoCuenta c : r.cuentas()) {
            System.out.printf("  [%s] %-28s pendiente $%9.2f%s%n", c.getIdEstudiante(), c.getNombreEstudiante(),
                    c.getSaldoPendiente(), c.isEnMora() ? "  EN MORA" : "");
        }
        System.out.printf("  Estudiantes: %d | En mora: %d | Pendiente total: $%.2f | Cobrado total: $%.2f | Errores: %d%n",
                r.cuentas().size(), r.estudiantesEnMora(), r.totalPendiente(), r.totalPagado(), r.errores());
        return r;
    }

    /** Recalcula en paralelo el estado de cuenta de cada estudiante. */
    public ResultadoCierreCartera ejecutarCierreDeCartera() {
        long inicio = System.nanoTime();
        List<String[]> estudiantes = listarEstudiantes();

        Map<String, EstadoCuenta> resultados = new ConcurrentHashMap<>();
        DoubleAdder totalPendiente = new DoubleAdder();
        DoubleAdder totalPagado = new DoubleAdder();
        AtomicInteger enMora = new AtomicInteger();
        AtomicInteger errores = new AtomicInteger();
        LocalDate limiteMora = LocalDate.now().minusDays(diasGracia);

        List<Callable<Void>> tareas = new ArrayList<>();
        for (String[] est : estudiantes) {
            tareas.add(() -> {
                Cuenta cuenta = consultarCuenta(est[0]);
                boolean mora = cuenta.pendienteMasAntiguo() != null && cuenta.pendienteMasAntiguo().isBefore(limiteMora);

                resultados.put(est[0], new EstadoCuenta(est[0], est[1], cuenta.pendiente(), mora));
                totalPendiente.add(cuenta.pendiente());
                totalPagado.add(cuenta.pagado());
                if (mora) {
                    enMora.incrementAndGet();
                }
                return null;
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        try {
            // invokeAll espera a que TODAS las tareas terminen (coordinacion de finalizacion).
            for (Future<Void> f : executor.invokeAll(tareas, TIEMPO_MAXIMO_SEGUNDOS, TimeUnit.SECONDS)) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    // Un estudiante con error no detiene el cierre de los demas.
                    errores.incrementAndGet();
                    System.err.println("[Cierre de cartera] " + e.getCause().getMessage());
                } catch (java.util.concurrent.CancellationException e) {
                    errores.incrementAndGet();
                    System.err.println("[Cierre de cartera] Tarea cancelada por exceder el tiempo máximo.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("El cierre de cartera fue interrumpido.", e);
        } finally {
            executor.shutdownNow();
        }

        List<EstadoCuenta> cuentas = new ArrayList<>(resultados.values());
        cuentas.sort(Comparator.comparing(c -> Integer.parseInt(c.getIdEstudiante())));
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
        return new ResultadoCierreCartera(cuentas, totalPendiente.sum(), totalPagado.sum(),
                enMora.get(), errores.get(), hilos, ms);
    }

    // ------------------------------------------------------------------ acceso a datos

    /** {id, "Nombre Apellido"} de cada estudiante. Se lee una sola vez, antes de repartir el trabajo. */
    private List<String[]> listarEstudiantes() {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ESTUDIANTES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{String.valueOf(rs.getInt("id_usuario")),
                        rs.getString("nombre") + " " + rs.getString("apellido")});
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo obtener la lista de estudiantes.", e);
        }
        return lista;
    }

    /** Se ejecuta DENTRO de cada hilo, con su propia conexion del pool. */
    private Cuenta consultarCuenta(String idEstudiante) throws SQLException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_CUENTA)) {
            ps.setString(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new Cuenta(0, 0, null);
                }
                Date antiguo = rs.getDate("pendiente_mas_antiguo");
                return new Cuenta(rs.getDouble("pendiente"), rs.getDouble("pagado"),
                        antiguo != null ? antiguo.toLocalDate() : null);
            }
        }
    }

    private record Cuenta(double pendiente, double pagado, LocalDate pendienteMasAntiguo) {
    }
}
