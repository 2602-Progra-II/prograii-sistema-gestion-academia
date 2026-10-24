package sv.gestionacademica.menu;

import sv.gestionacademica.controller.LoginController;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.MatriculaRepository;

import java.io.Console;
import java.util.List;
import java.util.Optional;

/**
 * Login + menu principal segun el rol del usuario (Administrador, Profesor, Estudiante).
 *
 * Los menus de cada modulo se registran desde Main como ModuloMenu.
 * El modulo de material/actividades/entregas/calificaciones (del compañero) se conecta con
 * setMenuAcademico(...): mientras no se registre, la opcion avisa que esta en construccion.
 */
public class MenuPrincipal {

    private static final int INTENTOS_LOGIN = 3;

    private final Consola consola;
    private final LoginController loginController;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    private ModuloMenu menuCore;          // matricula -> pago -> factura
    private ModuloMenu menuUsuarios;
    private ModuloMenu menuPeriodosCursos;
    private MenuReportes menuReportes;
    private ModuloMenu procesoConcurrente;
    private ModuloMenu menuAcademico;     // material, actividades, entregas, calificaciones

    public MenuPrincipal(Consola consola, LoginController loginController,
                         CursoRepository cursoRepository, MatriculaRepository matriculaRepository) {
        this.consola = consola;
        this.loginController = loginController;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public MenuPrincipal setMenuCore(ModuloMenu m) { this.menuCore = m; return this; }
    public MenuPrincipal setMenuUsuarios(ModuloMenu m) { this.menuUsuarios = m; return this; }
    public MenuPrincipal setMenuPeriodosCursos(ModuloMenu m) { this.menuPeriodosCursos = m; return this; }
    public MenuPrincipal setMenuReportes(MenuReportes m) { this.menuReportes = m; return this; }
    public MenuPrincipal setProcesoConcurrente(ModuloMenu m) { this.procesoConcurrente = m; return this; }
    public MenuPrincipal setMenuAcademico(ModuloMenu m) { this.menuAcademico = m; return this; }

    /** Ciclo completo: login -> menu del rol -> cerrar sesion -> login ... hasta que el usuario salga. */
    public void ejecutar() {
        while (true) {
            Optional<Sesion> sesion = login();
            if (sesion.isEmpty()) {
                break;
            }
            try {
                menuSegunRol(sesion.get());
            } finally {
                loginController.cerrarSesion(sesion.get());
                System.out.println("Sesión cerrada.");
            }
        }
        System.out.println("Hasta luego.");
    }

    // ------------------------------------------------------------------ login

    private Optional<Sesion> login() {
        System.out.println("\n===== SISTEMA DE GESTIÓN ACADEMIA - INICIAR SESIÓN =====");
        System.out.println("(deja el email vacío para salir)");
        for (int intento = 1; intento <= INTENTOS_LOGIN; intento++) {
            String email = consola.leerTexto("Email: ");
            if (email.isEmpty()) {
                return Optional.empty();
            }
            Optional<Sesion> sesion = loginController.iniciarSesion(email, leerContrasena());
            if (sesion.isPresent()) {
                Usuario u = sesion.get().getUsuario();
                System.out.println("\nBienvenido/a, " + u.getNombreUsuario() + " " + u.getApellidoUsuario()
                        + " (" + u.getRol().getNombreRol() + ").");
                return sesion;
            }
            System.out.println("Intento " + intento + " de " + INTENTOS_LOGIN + ".");
        }
        System.out.println("Demasiados intentos fallidos.");
        return Optional.empty();
    }

    /** En terminal real oculta la contraseña; en la consola del IDE (sin System.console()) se lee normal. */
    private String leerContrasena() {
        Console c = System.console();
        if (c != null) {
            char[] pass = c.readPassword("Contraseña: ");
            return pass == null ? "" : new String(pass);
        }
        return consola.leerTexto("Contraseña: ");
    }

    // ------------------------------------------------------------------ menus por rol

    private void menuSegunRol(Sesion sesion) {
        String rol = sesion.getUsuario().getRol().getNombreRol();
        switch (rol.toLowerCase()) {
            case "administrador" -> menuAdministrador(sesion);
            case "profesor" -> menuProfesor(sesion);
            case "estudiante" -> menuEstudiante(sesion);
            default -> System.out.println("El rol \"" + rol + "\" no tiene un menú asignado.");
        }
    }

    private void menuAdministrador(Sesion sesion) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n===== MENÚ ADMINISTRADOR =====");
            System.out.println("1. Usuarios");
            System.out.println("2. Períodos y cursos");
            System.out.println("3. Matrícula, pagos y facturas");
            System.out.println("4. Material, actividades y calificaciones");
            System.out.println("5. Reportes y estado de cuenta");
            System.out.println("6. Proceso concurrente (demo de pool de conexiones)");
            System.out.println("0. Cerrar sesión");
            switch (consola.leerTexto("Opción: ")) {
                case "1" -> abrir(menuUsuarios, sesion);
                case "2" -> abrir(menuPeriodosCursos, sesion);
                case "3" -> abrir(menuCore, sesion);
                case "4" -> abrir(menuAcademico, sesion);
                case "5" -> abrir(menuReportes, sesion);
                case "6" -> abrir(procesoConcurrente, sesion);
                case "0" -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void menuProfesor(Sesion sesion) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n===== MENÚ PROFESOR =====");
            System.out.println("1. Mis cursos");
            System.out.println("2. Material, actividades y calificaciones");
            System.out.println("0. Cerrar sesión");
            switch (consola.leerTexto("Opción: ")) {
                case "1" -> misCursos(sesion.getUsuario());
                case "2" -> abrir(menuAcademico, sesion);
                case "0" -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void menuEstudiante(Sesion sesion) {
        int idEstudiante = sesion.getUsuario().getIdUsuario();
        boolean salir = false;
        while (!salir) {
            System.out.println("\n===== MENÚ ESTUDIANTE =====");
            System.out.println("1. Mis matrículas");
            System.out.println("2. Mi estado de cuenta");
            System.out.println("3. Material, entregas y calificaciones");
            System.out.println("0. Cerrar sesión");
            switch (consola.leerTexto("Opción: ")) {
                case "1" -> misMatriculas(idEstudiante);
                case "2" -> ejecutarSeguro(() -> menuReportes.mostrarEstadoCuenta(idEstudiante));
                case "3" -> abrir(menuAcademico, sesion);
                case "0" -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    // ------------------------------------------------------------------ vistas propias

    private void misCursos(Usuario profesor) {
        ejecutarSeguro(() -> {
            List<Curso> cursos = cursoRepository.listarPorInstructor(profesor.getIdUsuario());
            System.out.println("\n--- Mis cursos ---");
            if (cursos.isEmpty()) {
                System.out.println("  (no tienes cursos asignados)");
            }
            for (Curso c : cursos) {
                System.out.println("  [" + c.getIdCurso() + "] " + c.getCodigoCurso() + " - "
                        + c.getNombreCurso() + " (" + c.getEstado() + ")");
            }
        });
    }

    private void misMatriculas(int idEstudiante) {
        ejecutarSeguro(() -> {
            List<Matricula> lista = matriculaRepository.findByEstudiante(String.valueOf(idEstudiante));
            System.out.println("\n--- Mis matrículas ---");
            if (lista.isEmpty()) {
                System.out.println("  (sin matrículas)");
            }
            for (Matricula m : lista) {
                String curso = m.getCurso() != null ? m.getCurso().getNombreCurso() : "?";
                System.out.println("  Matrícula [" + m.getIdMatricula() + "] " + curso + " - " + m.getEstadoMatricula());
            }
        });
    }

    // ------------------------------------------------------------------ auxiliares

    private void abrir(ModuloMenu modulo, Sesion sesion) {
        if (modulo == null) {
            System.out.println("Módulo en construcción.");
            return;
        }
        ejecutarSeguro(() -> modulo.ejecutar(sesion));
    }

    /** Un error en un modulo no debe tumbar la sesion completa. */
    private void ejecutarSeguro(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            System.err.println("[Error] " + e.getMessage());
        }
    }
}
