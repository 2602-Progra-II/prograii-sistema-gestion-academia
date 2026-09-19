package sv.gestionacademica;

import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoUsuario;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== PRUEBA DE GESTIÓN ACADÉMICA ===");

        // 1. Crear un Rol
        Rol rolEstudiante = new Rol(
                1,
                "Estudiante",
                "Usuario con rol de estudiante"
        );

        System.out.println("\n--- Rol ---");
        System.out.println("ID: " + rolEstudiante.getIdRol());
        System.out.println("Nombre: " + rolEstudiante.getNombreRol());
        System.out.println("Descripción: " + rolEstudiante.getDescripcion());


        // 2. Crear un Usuario
        Usuario usuario = new Usuario(
                101,
                "Edgard",
                "Granados",
                "1234",
                EstadoUsuario.ACTIVO,
                rolEstudiante
        );

        System.out.println("\n--- Usuario ---");
        System.out.println("ID: " + usuario.getIdUsuario());
        System.out.println("Nombre: " + usuario.getNombreUsuario());
        System.out.println("Apellido: " + usuario.getApellidoUsuario());
        System.out.println("Estado: " + usuario.getEstado());
        System.out.println("Rol: " + usuario.getRol().getNombreRol());


        // 3. Modificar el Usuario
        usuario.setNombreUsuario("Edgard Santiago");
        usuario.setEstado(EstadoUsuario.INACTIVO);

        System.out.println("\n--- Usuario modificado ---");
        System.out.println("Nombre: " + usuario.getNombreUsuario());
        System.out.println("Estado: " + usuario.getEstado());


        // 4. Crear una Sesión
        LocalDateTime inicio = LocalDateTime.now();

        Sesion sesion = new Sesion(
                1001,
                inicio,
                null,
                true
        );

        System.out.println("\n--- Sesión ---");
        System.out.println("ID: " + sesion.getIdSesion());
        System.out.println("Fecha inicio: " + sesion.getFechaInicio());
        System.out.println("Sesión activa: " + sesion.estaActiva());


        // 5. Cerrar la Sesión
        sesion.setFechaFin(LocalDateTime.now());
        sesion.setActiva(false);

        System.out.println("\n--- Sesión cerrada ---");
        System.out.println("Fecha fin: " + sesion.getFechaFin());
        System.out.println("Sesión activa: " + sesion.estaActiva());


        System.out.println("\n=== PRUEBA FINALIZADA ===");
    }
}