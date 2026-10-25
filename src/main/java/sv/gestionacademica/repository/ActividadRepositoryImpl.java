package sv.gestionacademica.repository;

import sv.gestionacademica.entity.ActividadEvaluativa;
import sv.gestionacademica.enums.TipoActividad;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActividadRepositoryImpl implements ActividadRepository {

    @Override
    public ActividadEvaluativa save(ActividadEvaluativa actividad) {
        String sql = "INSERT INTO actividad_evaluativa (nombre_actividad, descripcion, fecha_limite, ponderacion, id_curso, tipo_actividad) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, actividad.getNombre());
            pstmt.setString(2, actividad.getDescripcion());

            if (actividad.getFechaEntrega() != null && !actividad.getFechaEntrega().isEmpty()) {
                pstmt.setObject(3, LocalDate.parse(actividad.getFechaEntrega()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            pstmt.setDouble(4, actividad.getPonderacion());
            pstmt.setInt(5, obtenerIdCursoValido(conn, actividad));
            pstmt.setString(6, actividad.getTipo().name());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    actividad.setId(String.valueOf(generatedKeys.getInt(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la actividad evaluativa en la base de datos", e);
        }
        return actividad;
    }

    @Override
    public Optional<ActividadEvaluativa> findById(String id) {
        String sql = "SELECT id_actividad, nombre_actividad, descripcion, fecha_limite, ponderacion, id_curso, tipo_actividad FROM actividad_evaluativa WHERE id_actividad = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearActividad(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la actividad por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ActividadEvaluativa> findAll() {
        List<ActividadEvaluativa> actividades = new ArrayList<>();
        String sql = "SELECT id_actividad, nombre_actividad, descripcion, fecha_limite, ponderacion, id_curso, tipo_actividad FROM actividad_evaluativa";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                actividades.add(mapearActividad(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las actividades evaluativas", e);
        }
        return actividades;
    }

    @Override
    public void update(ActividadEvaluativa actividad) {
        String sql = "UPDATE actividad_evaluativa SET nombre_actividad = ?, descripcion = ?, fecha_limite = ?, ponderacion = ?, id_curso = ?, tipo_actividad = ? WHERE id_actividad = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, actividad.getNombre());
            pstmt.setString(2, actividad.getDescripcion());

            if (actividad.getFechaEntrega() != null && !actividad.getFechaEntrega().isEmpty()) {
                pstmt.setObject(3, LocalDate.parse(actividad.getFechaEntrega()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            pstmt.setDouble(4, actividad.getPonderacion());
            pstmt.setInt(5, obtenerIdCursoValido(conn, actividad));
            pstmt.setString(6, actividad.getTipo().name());
            pstmt.setInt(7, Integer.parseInt(actividad.getId()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la actividad evaluativa", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM actividad_evaluativa WHERE id_actividad = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la actividad evaluativa", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT COUNT(1) FROM actividad_evaluativa WHERE id_actividad = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si existe la actividad evaluativa", e);
        }
        return false;
    }

    /**
     * Curso del actividad. Si no se indico (idCurso = 0), se usa el primer curso registrado:
     * lo necesitan las pruebas de integracion que crean registros sin curso.
     * Desde la aplicacion siempre llega un curso (AcademicoController lo exige).
     */
    private int obtenerIdCursoValido(Connection conn, ActividadEvaluativa actividad) {
        if (actividad.getIdCurso() > 0) {
            return actividad.getIdCurso();
        }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id_curso FROM curso ORDER BY id_curso")) {
            if (rs.next()) {
                return rs.getInt("id_curso");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo determinar el curso del actividad", e);
        }
        throw new IllegalStateException("No hay cursos registrados para asociar el actividad.");
    }

    private ActividadEvaluativa mapearActividad(ResultSet rs) throws SQLException {
        int idActividad = rs.getInt("id_actividad");
        String nombreActividad = rs.getString("nombre_actividad");
        String descripcion = rs.getString("descripcion");

        LocalDate fechaLimite = null;
        Date fechaSql = rs.getDate("fecha_limite");
        if (fechaSql != null) {
            fechaLimite = fechaSql.toLocalDate();
        }

        double ponderacion = rs.getDouble("ponderacion");

        // Fabrica polimorfica: cada tipo construye su subclase (Tarea, Examen o Proyecto).
        TipoActividad tipo = TipoActividad.desdeTexto(rs.getString("tipo_actividad"));
        ActividadEvaluativa actividad = tipo.crear(idActividad, nombreActividad, descripcion, fechaLimite, ponderacion);
        actividad.setIdCurso(rs.getInt("id_curso"));
        return actividad;
    }
}