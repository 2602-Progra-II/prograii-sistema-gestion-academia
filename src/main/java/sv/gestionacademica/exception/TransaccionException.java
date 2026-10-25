package sv.gestionacademica.exception;

/**
 * Error TECNICO durante una transaccion (BD caida, SQL invalido, fallo al hacer commit...).
 * Se distingue de BusinessException, que representa una regla de negocio incumplida.
 */
public class TransaccionException extends RuntimeException {

    public TransaccionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
