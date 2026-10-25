package sv.gestionacademica.exception;

/**
 * Error TECNICO de acceso a datos (BD caida, SQL invalido, proceso interrumpido...).
 * Es unchecked para no obligar a cada capa a declararla; se distingue de
 * BusinessException, que representa una regla de negocio incumplida.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
