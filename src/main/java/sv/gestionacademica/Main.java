package sv.gestionacademica;

import sv.gestionacademica.controller.AcademicoController;
import sv.gestionacademica.controller.LoginController;
import sv.gestionacademica.controller.MatriculaController;
import sv.gestionacademica.controller.UsuarioController;
import sv.gestionacademica.menu.Consola;
import sv.gestionacademica.menu.MenuAcademico;
import sv.gestionacademica.menu.MenuPeriodosCursos;
import sv.gestionacademica.menu.MenuPrincipal;
import sv.gestionacademica.menu.MenuReportes;
import sv.gestionacademica.menu.MenuUsuarios;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.CursoRepository;
import sv.gestionacademica.repository.CursoRepositoryImpl;
import sv.gestionacademica.repository.EntregaRepositoryImpl;
import sv.gestionacademica.repository.FacturaRepository;
import sv.gestionacademica.repository.FacturaRepositoryImpl;
import sv.gestionacademica.repository.MatriculaRepository;
import sv.gestionacademica.repository.MatriculaRepositoryImpl;
import sv.gestionacademica.repository.PagoRepository;
import sv.gestionacademica.repository.PagoRepositoryImpl;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;
import sv.gestionacademica.repository.PeriodoAcademicoRepositoryImpl;
import sv.gestionacademica.repository.RolRepositoryImpl;
import sv.gestionacademica.repository.UsuarioRepository;
import sv.gestionacademica.repository.UsuarioRepositoryImpl;
import sv.gestionacademica.service.ActividadService;
import sv.gestionacademica.service.AutenticacionServiceImpl;
import sv.gestionacademica.service.CursoService;
import sv.gestionacademica.service.EstadoCuentaService;
import sv.gestionacademica.service.EvaluacionService;
import sv.gestionacademica.service.FlujoMatriculaService;
import sv.gestionacademica.service.MaterialService;
import sv.gestionacademica.service.PeriodoAcademicoService;
import sv.gestionacademica.service.ProcesoConcurrenteService;
import sv.gestionacademica.service.ReporteService;
import sv.gestionacademica.service.UsuarioServiceImpl;

import java.util.Scanner;

/**
 * Punto de entrada de la aplicacion.
 *
 * Arma las capas (Repository -> Service -> Controller -> Menu), pide login
 * y muestra el menu segun el rol del usuario.
 *
 * Requisitos: BD creada con database/azure_schema.sql y database/azure_data.sql.
 * Conexion: variables de entorno DB_URL, DB_USER y DB_PASSWORD.
 * Las tres variables son obligatorias; no existen valores por defecto.
 */
public class Main {

    public static void main(String[] args) {
        try {
            construirAplicacion().ejecutar();
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudo conectar con la base de datos o iniciar la aplicación."
            );
            System.err.println("Causa: " + causaRaiz(e).getMessage());
            System.err.println(
                    "Verifica que Azure SQL Database esté disponible, " +
                            "que hayas configurado DB_URL, DB_USER y DB_PASSWORD " +
                            "y que el usuario tenga los permisos necesarios. " +
                            "La base debe estar preparada con los scripts de Azure."
            );
        } finally {
            DataSourceConfig.close();
        }
    }

    /** Arma las capas Repository -> Service -> Controller -> Menu (inyeccion de dependencias manual). */
    private static MenuPrincipal construirAplicacion() {
        // Repositories (JDBC)
        UsuarioRepository usuarios = new UsuarioRepositoryImpl();
        CursoRepository cursos = new CursoRepositoryImpl();
        PeriodoAcademicoRepository periodos = new PeriodoAcademicoRepositoryImpl();
        MatriculaRepository matriculas = new MatriculaRepositoryImpl();
        PagoRepository pagos = new PagoRepositoryImpl();
        FacturaRepository facturas = new FacturaRepositoryImpl();

        // Services
        FlujoMatriculaService flujo = new FlujoMatriculaService(
                DataSourceConfig.getDataSource(), usuarios, cursos, periodos, matriculas, pagos, facturas);
        EstadoCuentaService estadoCuenta = new EstadoCuentaService(pagos, usuarios);
        ProcesoConcurrenteService concurrente = new ProcesoConcurrenteService(DataSourceConfig.getDataSource());

        // Controllers
        LoginController login = new LoginController(new AutenticacionServiceImpl(usuarios));
        MatriculaController matriculaController = new MatriculaController(flujo);
        UsuarioController usuarioController = new UsuarioController(new UsuarioServiceImpl(usuarios));
        AcademicoController academico = new AcademicoController(
                new PeriodoAcademicoService(periodos), new CursoService(cursos, periodos),
                new MaterialService(), new ActividadService(),
                new EvaluacionService(new EntregaRepositoryImpl()), matriculas);

        // Menus (todos comparten el mismo Scanner)
        Scanner scanner = new Scanner(System.in);
        Consola consola = new Consola(scanner);

        return new MenuPrincipal(consola, login, cursos, matriculas)
                // El asesor que aprueba/rechaza pagos es el usuario con sesion iniciada.
                .setMenuCore(sesion -> new MenuCore(matriculaController, usuarios, cursos, pagos, matriculas,
                        scanner, String.valueOf(sesion.getUsuario().getIdUsuario())).ejecutar())
                .setMenuUsuarios(new MenuUsuarios(consola, usuarioController, usuarios, new RolRepositoryImpl()))
                .setMenuPeriodosCursos(new MenuPeriodosCursos(consola, academico, cursos, usuarios))
                .setMenuReportes(new MenuReportes(consola, estadoCuenta, new ReporteService(), pagos, facturas, periodos))
                .setProcesoConcurrente(sesion -> concurrente.ejecutarProcesoConcurrente())
                .setMenuAcademico(new MenuAcademico(consola, academico, cursos, usuarios));
    }

    private static Throwable causaRaiz(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t;
    }
}
