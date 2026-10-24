package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.exception.MaterialInvalidoException;
import sv.gestionacademica.exception.MaterialNoEncontradoException;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MaterialServiceTest {

    private MaterialService materialService;
    private MaterialRepository materialRepository;
    private Curso cursoBase;

    @BeforeEach
    void setUp() {
        materialRepository = new MaterialRepositoryImpl();
        materialService = new MaterialService(materialRepository);

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            try { stmt.executeUpdate("EXEC sp_msforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("DELETE FROM material_didactico"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("DELETE FROM curso"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("DELETE FROM periodo_academico"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("EXEC sp_msforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL'"); } catch (Exception ignored) {}
        } catch (Exception ignored) {}

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);

        // Crear Periodo y Curso base requeridos para la Foreign Key
        PeriodoAcademicoRepository periodoRepo = new PeriodoAcademicoRepositoryImpl();
        PeriodoAcademico periodo = new PeriodoAcademico(0, "P_" + suffix, LocalDate.now(), LocalDate.now().plusMonths(4), EstadoPeriodo.ACTIVO);
        periodoRepo.guardar(periodo);

        CursoRepository cursoRepo = new CursoRepositoryImpl();
        cursoBase = new Curso(0, "C_" + suffix, "Curso Materiales", "Desc", EstadoCurso.ACTIVO, periodo.getIdPeriodo(), null);
        cursoRepo.guardar(cursoBase);
    }

    @Test
    @DisplayName("Debería registrar un material didáctico exitosamente en SQL Server")
    void testRegistrarMaterialExitoso() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        MaterialDidactico material = crearMaterial("0", "Guía " + suffix, "Contenido PDF", "http://link.com/file", cursoBase.getIdCurso());

        MaterialDidactico registrado = materialService.registrarMaterial(material);

        assertNotNull(registrado);
        assertNotNull(registrado.getId());
        assertNotEquals("0", registrado.getId());
    }

    @Test
    @DisplayName("Debería lanzar MaterialInvalidoException si faltan datos obligatorios")
    void testRegistrarMaterialInvalido() {
        MaterialDidactico materialVacio = crearMaterial("0", "", "Descripción", "url", cursoBase.getIdCurso());

        assertThrows(MaterialInvalidoException.class, () -> materialService.registrarMaterial(materialVacio));
    }

    @Test
    @DisplayName("Debería obtener todos los materiales desde la BD")
    void testObtenerTodosLosMateriales() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        materialService.registrarMaterial(crearMaterial("0", "Doc 1 " + suffix, "Desc", "url1", cursoBase.getIdCurso()));
        materialService.registrarMaterial(crearMaterial("0", "Doc 2 " + suffix, "Desc", "url2", cursoBase.getIdCurso()));

        List<MaterialDidactico> lista = materialService.obtenerTodosLosMateriales();

        assertTrue(lista.size() >= 2);
    }

    @Test
    @DisplayName("Debería eliminar un material didáctico en la BD")
    void testEliminarMaterialExitoso() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        MaterialDidactico material = materialService.registrarMaterial(crearMaterial("0", "Doc a eliminar " + suffix, "Desc", "url", cursoBase.getIdCurso()));

        boolean eliminado = materialService.eliminarMaterial(material.getId());

        assertTrue(eliminado);
        assertThrows(MaterialNoEncontradoException.class, () -> materialService.obtenerMaterialPorId(material.getId()));
    }

    private MaterialDidactico crearMaterial(String id, String titulo, String desc, String url, int idCurso) {
        MaterialDidactico mat = new MaterialDidactico(id, titulo, desc, url);
        try {
            var f = mat.getClass().getDeclaredField("idCurso");
            f.setAccessible(true);
            f.set(mat, idCurso);
        } catch (Exception ignored) {}
        return mat;
    }
}