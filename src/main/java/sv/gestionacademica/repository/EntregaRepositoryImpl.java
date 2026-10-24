package sv.gestionacademica.repository;

import sv.gestionacademica.entity.EntregaEvaluacion;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntregaRepositoryImpl implements EntregaRepository {

    @Override
    public EntregaEvaluacion guardar(EntregaEvaluacion entrega) {
        if (entrega.getIdEntrega() > 0) {
            String updateSql = "UPDATE entrega_evaluacion SET ruta_archivo_entrega = ?, fecha_entrega = ?, calificacion = ?, observaciones = ?, id_actividad = ?, id_estudiante = ? WHERE id_entrega = ?";
            try (Connection conn = DataSourceConfig.getDataSource().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                pstmt.setString(1, entrega.getRutaArchivoEntrega());
                pstmt.setObject(2, entrega.getFechaEntrega());
                pstmt.setDouble(3, entrega.getCalificacion());
                pstmt.setString(4, entrega.getObservaciones());
                pstmt.setInt(5, entrega.getIdActividad());
                pstmt.setString(6, entrega.getIdEstudiante());
                pstmt.setInt(7, entrega.getIdEntrega());

                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar la entrega de evaluación en la base de datos", e);
            }
            return entrega;
        }

        String sql = "INSERT INTO entrega_evaluacion (ruta_archivo_entrega, fecha_entrega, calificacion, observaciones, id_actividad, id_estudiante) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, entrega.getRutaArchivoEntrega());
            pstmt.setObject(2, entrega.getFechaEntrega());
            pstmt.setDouble(3, entrega.getCalificacion());
            pstmt.setString(4, entrega.getObservaciones());
            pstmt.setInt(5, entrega.getIdActividad());
            pstmt.setString(6, entrega.getIdEstudiante());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entrega.setIdEntrega(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la entrega de evaluación en la base de datos", e);
        }
        return entrega;
    }

    @Override
    public Optional<EntregaEvaluacion> buscarPorId(int idEntrega) {
        String sql = "SELECT id_entrega, ruta_archivo_entrega, fecha_entrega, calificacion, observaciones, id_actividad, id_estudiante FROM entrega_evaluacion WHERE id_entrega = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEntrega);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEntrega(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la entrega por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<EntregaEvaluacion> listarPorActividad(int idActividad) {
        List<EntregaEvaluacion> entregas = new ArrayList<>();
        String sql = "SELECT id_entrega, ruta_archivo_entrega, fecha_entrega, calificacion, observaciones, id_actividad, id_estudiante FROM entrega_evaluacion WHERE id_actividad = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idActividad);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entregas.add(mapearEntrega(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las entregas por actividad", e);
        }
        return entregas;
    }

    @Override
    public List<EntregaEvaluacion> listarPorEstudiante(String idEstudiante) {
        List<EntregaEvaluacion> entregas = new ArrayList<>();
        String sql = "SELECT id_entrega, ruta_archivo_entrega, fecha_entrega, calificacion, observaciones, id_actividad, id_estudiante FROM entrega_evaluacion WHERE id_estudiante = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idEstudiante);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entregas.add(mapearEntrega(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las entregas por estudiante", e);
        }
        return entregas;
    }

    @Override
    public List<EntregaEvaluacion> listarTodas() {
        List<EntregaEvaluacion> entregas = new ArrayList<>();
        String sql = "SELECT id_entrega, ruta_archivo_entrega, fecha_entrega, calificacion, observaciones, id_actividad, id_estudiante FROM entrega_evaluacion";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entregas.add(mapearEntrega(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar todas las entregas", e);
        }
        return entregas;
    }

    private EntregaEvaluacion mapearEntrega(ResultSet rs) throws SQLException {
        int idEntrega = rs.getInt("id_entrega");
        String rutaArchivo = rs.getString("ruta_archivo_entrega");

        LocalDate fechaEntrega = null;
        Date fechaSql = rs.getDate("fecha_entrega");
        if (fechaSql != null) {
            fechaEntrega = fechaSql.toLocalDate();
        }

        double calificacion = rs.getDouble("calificacion");
        String observaciones = rs.getString("observaciones");
        int idActividad = rs.getInt("id_actividad");
        String idEstudiante = rs.getString("id_estudiante");

        return new EntregaEvaluacion(idEntrega, rutaArchivo, fechaEntrega, calificacion, observaciones, idActividad, idEstudiante);
    }
}