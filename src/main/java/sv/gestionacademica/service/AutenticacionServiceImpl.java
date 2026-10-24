package sv.gestionacademica.service;

import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.exception.BusinessException;
import sv.gestionacademica.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Autenticacion contra la tabla usuario (email + password).
 *
 * Las sesiones viven en memoria mientras corre la aplicacion de consola;
 * la BD no tiene tabla de sesiones.
 *
 * Nota: data.sql guarda las contraseñas en texto plano, por eso la comparacion es directa.
 * Si el equipo agrega hash, solo cambia {@link #coincide(String, String)}.
 */
public class AutenticacionServiceImpl implements AutenticacionService {

    private static final String CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos.";

    private final UsuarioRepository usuarioRepository;
    private final Map<Integer, Sesion> sesionesActivas = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(1);

    public AutenticacionServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Sesion iniciarSesion(String email, String contrasena) {
        if (email == null || email.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new BusinessException("Ingresa email y contraseña.");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email.trim());
        // Mismo mensaje si no existe o si la contraseña falla: no revela qué emails existen.
        if (usuario == null || !coincide(contrasena, usuario.getContrasena())) {
            throw new BusinessException(CREDENCIALES_INVALIDAS);
        }
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessException("El usuario está inactivo. Contacta al administrador.");
        }

        Sesion sesion = new Sesion(secuencia.getAndIncrement(), LocalDateTime.now(), null, true);
        sesion.setUsuario(usuario);
        sesionesActivas.put(sesion.getIdSesion(), sesion);
        return sesion;
    }

    @Override
    public boolean validarCredenciales(String email, String contrasena) {
        if (email == null || contrasena == null) {
            return false;
        }
        Usuario usuario = usuarioRepository.buscarPorEmail(email.trim());
        return usuario != null
                && usuario.getEstado() == EstadoUsuario.ACTIVO
                && coincide(contrasena, usuario.getContrasena());
    }

    @Override
    public void cerrarSesion(int idSesion) {
        Sesion sesion = sesionesActivas.remove(idSesion);
        if (sesion != null) {
            sesion.setActiva(false);
            sesion.setFechaFin(LocalDateTime.now());
        }
    }

    private boolean coincide(String ingresada, String guardada) {
        return guardada != null && guardada.equals(ingresada);
    }
}
