package sv.gestionacademica.dto;

import sv.gestionacademica.entity.EstadoCuenta;

import java.util.List;

/**
 * Resultado del cierre de cartera concurrente (ProcesoConcurrenteService).
 *
 * @param cuentas        estado de cuenta calculado para cada estudiante procesado
 * @param totalPendiente suma de pagos PENDIENTE de todos los estudiantes
 * @param totalPagado    suma de pagos APROBADO de todos los estudiantes
 * @param estudiantesEnMora cuantos estudiantes quedaron en mora
 * @param errores        tareas que fallaron (un fallo no detiene a las demas)
 * @param hilos          tamaño del pool de hilos usado
 * @param milisegundos   duracion total del proceso
 */
public record ResultadoCierreCartera(List<EstadoCuenta> cuentas,
                                     double totalPendiente,
                                     double totalPagado,
                                     int estudiantesEnMora,
                                     int errores,
                                     int hilos,
                                     long milisegundos) {
}
