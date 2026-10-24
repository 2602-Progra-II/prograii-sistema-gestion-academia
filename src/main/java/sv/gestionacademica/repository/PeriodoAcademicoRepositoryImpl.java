package sv.gestionacademica.repository;

import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PeriodoAcademicoRepositoryImpl implements PeriodoAcademicoRepository {

    @Override
    public PeriodoAcademico guardar(PeriodoAcademico periodo) {
        String sql = "INSERT INTO periodo_academico (nombre_periodo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, periodo.getNombrePeriodo());
            pstmt.setObject(2, periodo.getFechaInicio());
            pstmt.setObject(3, periodo.getFechaFin());
            pstmt.setString(4, periodo.getEstado() != null ? periodo.getEstado().name() : "PLANIFICADO");

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    periodo.setIdPeriodo(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el periodo académico", e);
        }
        return periodo;
    }

    @Override
    public Optional<PeriodoAcademico> buscarPorId(int idPeriodo) {
        String sql = "SELECT id_periodo, nombre_periodo, fecha_inicio, fecha_fin, estado FROM periodo_academico WHERE id_periodo = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPeriodo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PeriodoAcademico periodo = mapearPeriodo(rs);
                    return Optional.of(periodo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el periodo académico por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PeriodoAcademico> buscarPorNombre(String nombrePeriodo) {
        String sql = "SELECT id_periodo, nombre_periodo, fecha_inicio, fecha_fin, estado FROM periodo_academico WHERE nombre_periodo = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombrePeriodo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PeriodoAcademico periodo = mapearPeriodo(rs);
                    return Optional.of(periodo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el periodo académico por nombre", e);
        }
        return Optional.empty();
    }

    @Override
    public List<PeriodoAcademico> listarTodos() {
        List<PeriodoAcademico> periodos = new ArrayList<>();
        String sql = "SELECT id_periodo, nombre_periodo, fecha_inicio, fecha_fin, estado FROM periodo_academico";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                periodos.add(mapearPeriodo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los periodos académicos", e);
        }
        return periodos;
    }

    @Override
    public List<PeriodoAcademico> listarVigentes() {
        List<PeriodoAcademico> periodos = new ArrayList<>();
        String sql = "SELECT id_periodo, nombre_periodo, fecha_inicio, fecha_fin, estado FROM periodo_academico WHERE estado = 'ACTIVO' OR estado = 'Activo'";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                periodos.add(mapearPeriodo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los periodos vigentes", e);
        }
        return periodos;
    }

    @Override
    public PeriodoAcademico actualizar(PeriodoAcademico periodo) {
        String sql = "UPDATE periodo_academico SET nombre_periodo = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_periodo = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, periodo.getNombrePeriodo());
            pstmt.setObject(2, periodo.getFechaInicio());
            pstmt.setObject(3, periodo.getFechaFin());
            pstmt.setString(4, periodo.getEstado() != null ? periodo.getEstado().name() : "PLANIFICADO");
            pstmt.setInt(5, periodo.getIdPeriodo());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el periodo académico", e);
        }
        return periodo;
    }

    @Override
    public void eliminar(int idPeriodo) {
        String sql = "DELETE FROM periodo_academico WHERE id_periodo = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPeriodo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el periodo académico", e);
        }
    }

    private PeriodoAcademico mapearPeriodo(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_periodo");
        String nombre = rs.getString("nombre_periodo");

        LocalDate fechaInicio = null;
        Date fechaInicioSql = rs.getDate("fecha_inicio");
        if (fechaInicioSql != null) {
            fechaInicio = fechaInicioSql.toLocalDate();
        }

        LocalDate fechaFin = null;
        Date fechaFinSql = rs.getDate("fecha_fin");
        if (fechaFinSql != null) {
            fechaFin = fechaFinSql.toLocalDate();
        }

        String estadoStr = rs.getString("estado");
        EstadoPeriodo estado = EstadoPeriodo.PLANIFICADO;
        if (estadoStr != null) {
            try {
                estado = EstadoPeriodo.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                estado = EstadoPeriodo.PLANIFICADO;
            }
        }

        return new PeriodoAcademico(id, nombre, fechaInicio, fechaFin, estado);
    }
}