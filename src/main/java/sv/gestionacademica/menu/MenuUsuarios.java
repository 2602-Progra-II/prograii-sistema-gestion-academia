package sv.gestionacademica.menu;

import sv.gestionacademica.controller.UsuarioController;
import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.repository.RolRepository;
import sv.gestionacademica.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

/** Gestion de usuarios (solo Administrador): registrar, activar/desactivar y asignar rol. */
public class MenuUsuarios implements ModuloMenu {

    private final Consola consola;
    private final UsuarioController controller;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public MenuUsuarios(Consola consola, UsuarioController controller,
                        UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.consola = consola;
        this.controller = controller;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public void ejecutar(Sesion sesion) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n===== USUARIOS =====");
            System.out.println("1. Ver usuarios");
            System.out.println("2. Registrar usuario");
            System.out.println("3. Activar usuario");
            System.out.println("4. Desactivar usuario");
            System.out.println("5. Asignar rol");
            System.out.println("0. Volver");
            String opcion = consola.leerTexto("Opción: ");
            try {
                switch (opcion) {
                    case "1" -> listarUsuarios();
                    case "2" -> registrar();
                    case "3" -> activar();
                    case "4" -> desactivar(sesion);
                    case "5" -> asignarRol(sesion);
                    case "0" -> volver = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                System.err.println("[Error] " + e.getMessage());
            }
        }
    }

    private void listarUsuarios() {
        System.out.println("\n--- Usuarios ---");
        for (Usuario u : usuarioRepository.listarTodos()) {
            String rol = u.getRol() != null ? u.getRol().getNombreRol() : "?";
            System.out.println("  [" + u.getIdUsuario() + "] " + u.getNombreUsuario() + " " + u.getApellidoUsuario()
                    + " <" + u.getEmail() + "> - " + rol + " (" + u.getEstado() + ")");
        }
    }

    private void listarRoles() {
        System.out.println("\n--- Roles ---");
        for (Rol r : rolRepository.findAll()) {
            System.out.println("  [" + r.getIdRol() + "] " + r.getNombreRol());
        }
    }

    private void registrar() {
        String nombre = consola.leerTextoObligatorio("Nombre: ");
        String apellido = consola.leerTextoObligatorio("Apellido: ");
        String email = consola.leerTextoObligatorio("Email (será su usuario de login): ");
        if (usuarioRepository.buscarPorEmail(email) != null) {
            System.out.println("Ya existe un usuario con ese email.");
            return;
        }
        String contrasena = consola.leerTextoObligatorio("Contraseña: ");
        listarRoles();
        Optional<Rol> rol = rolRepository.findById(consola.leerEntero("Id del rol: "));
        if (rol.isEmpty()) {
            System.out.println("Ese rol no existe.");
            return;
        }

        Usuario nuevo = new Usuario(0, nombre, apellido, contrasena, EstadoUsuario.ACTIVO, rol.get());
        nuevo.setEmail(email);
        controller.registrar(nuevo);
        System.out.println("Usuario registrado con id " + nuevo.getIdUsuario() + ".");
    }

    private void activar() {
        listarUsuarios();
        Usuario u = buscarUsuario(consola.leerEntero("Id del usuario a activar: "));
        if (u != null) {
            controller.activar(u.getIdUsuario());
            System.out.println("Usuario " + u.getIdUsuario() + " activado.");
        }
    }

    private void desactivar(Sesion sesion) {
        listarUsuarios();
        Usuario u = buscarUsuario(consola.leerEntero("Id del usuario a desactivar: "));
        if (u == null) {
            return;
        }
        if (u.getIdUsuario() == sesion.getUsuario().getIdUsuario()) {
            System.out.println("No puedes desactivar tu propio usuario mientras tienes la sesión abierta.");
            return;
        }
        controller.desactivar(u.getIdUsuario());
        System.out.println("Usuario " + u.getIdUsuario() + " desactivado.");
    }

    private void asignarRol(Sesion sesion) {
        listarUsuarios();
        Usuario u = buscarUsuario(consola.leerEntero("Id del usuario: "));
        if (u == null) {
            return;
        }
        listarRoles();
        Optional<Rol> rol = rolRepository.findById(consola.leerEntero("Id del nuevo rol: "));
        if (rol.isEmpty()) {
            System.out.println("Ese rol no existe.");
            return;
        }
        controller.asignarRol(u.getIdUsuario(), rol.get());
        System.out.println("Rol de " + u.getNombreUsuario() + " cambiado a " + rol.get().getNombreRol() + ".");
        if (u.getIdUsuario() == sesion.getUsuario().getIdUsuario()) {
            System.out.println("Cambiaste tu propio rol: se aplicará en tu próximo inicio de sesión.");
        }
    }

    private Usuario buscarUsuario(int id) {
        Usuario u = usuarioRepository.buscarPorId(id);
        if (u == null) {
            System.out.println("No existe el usuario con id " + id + ".");
        }
        return u;
    }

    /** Usado por otros menus para mostrar usuarios de un rol (ej. profesores al asignar instructor). */
    public static void listarPorRol(List<Usuario> usuarios, String nombreRol) {
        boolean hay = false;
        for (Usuario u : usuarios) {
            if (u.getRol() != null && nombreRol.equalsIgnoreCase(u.getRol().getNombreRol())) {
                System.out.println("  [" + u.getIdUsuario() + "] " + u.getNombreUsuario() + " " + u.getApellidoUsuario());
                hay = true;
            }
        }
        if (!hay) {
            System.out.println("  (no hay usuarios con rol " + nombreRol + ")");
        }
    }
}
