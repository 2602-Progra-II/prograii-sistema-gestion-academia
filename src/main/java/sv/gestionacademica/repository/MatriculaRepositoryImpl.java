package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatriculaRepositoryImpl implements MatriculaRepository {

    @Override
    public Matricula save(Matricula matricula) {
        String sql = "INSERT INTO matricula (fecha_matricula, estado_matricula, id_estudiante, id_curso) VALUES (?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setObject(1, matricula.getFechaMatricula() != null ? matricula.getFechaMatricula() : LocalDate.now());
            pstmt.setString(2, matricula.getEstadoMatricula() != null ? matricula.getEstadoMatricula().name() : "ACTIVA");

            if (matricula.getEstudiante() != null && matricula.getEstudiante().getIdUsuario() > 0) {
                pstmt.setInt(3, matricula.getEstudiante().getIdUsuario());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            if (matricula.getCurso() != null && matricula.getCurso().getIdCurso() > 0) {
                pstmt.setInt(4, matricula.getCurso().getIdCurso());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    matricula.setIdMatricula(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la matrícula en la base de datos", e);
        }
        return matricula;
    }

    @Override
    public Optional<Matricula> findById(int idMatricula) {
        String sql = "SELECT id_matricula, fecha_matricula, estado_matricula, id_estudiante, id_curso FROM matricula WHERE id_matricula = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMatricula);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMatricula(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la matrícula por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Matricula> findAll() {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT id_matricula, fecha_matricula, estado_matricula, id_estudiante, id_curso FROM matricula";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                matriculas.add(mapearMatricula(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las matrículas", e);
        }
        return matriculas;
    }

    @Override
    public List<Matricula> findByEstudiante(String idEstudiante) {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT id_matricula, fecha_matricula, estado_matricula, id_estudiante, id_curso FROM matricula WHERE id_estudiante = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try {
                pstmt.setInt(1, Integer.parseInt(idEstudiante));
            } catch (NumberFormatException e) {
                pstmt.setString(1, idEstudiante);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    matriculas.add(mapearMatricula(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar matrículas por estudiante", e);
        }
        return matriculas;
    }

    @Override
    public List<Matricula> findByCurso(int idCurso) {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT id_matricula, fecha_matricula, estado_matricula, id_estudiante, id_curso FROM matricula WHERE id_curso = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCurso);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    matriculas.add(mapearMatricula(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar matrículas por curso", e);
        }
        return matriculas;
    }

    @Override
    public void update(Matricula matricula) {
        String sql = "UPDATE matricula SET fecha_matricula = ?, estado_matricula = ?, id_estudiante = ?, id_curso = ? WHERE id_matricula = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, matricula.getFechaMatricula());
            pstmt.setString(2, matricula.getEstadoMatricula() != null ? matricula.getEstadoMatricula().name() : "ACTIVA");

            if (matricula.getEstudiante() != null && matricula.getEstudiante().getIdUsuario() > 0) {
                pstmt.setInt(3, matricula.getEstudiante().getIdUsuario());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            if (matricula.getCurso() != null && matricula.getCurso().getIdCurso() > 0) {
                pstmt.setInt(4, matricula.getCurso().getIdCurso());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, matricula.getIdMatricula());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la matrícula", e);
        }
    }

    @Override
    public boolean deleteById(int idMatricula) {
        String sql = "DELETE FROM matricula WHERE id_matricula = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMatricula);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la matrícula", e);
        }
    }

    @Override
    public boolean existsById(int idMatricula) {
        String sql = "SELECT COUNT(1) FROM matricula WHERE id_matricula = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMatricula);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si existe la matrícula", e);
        }
        return false;
    }

    private Matricula mapearMatricula(ResultSet rs) throws SQLException {
        int idMatricula = rs.getInt("id_matricula");

        LocalDate fechaMatricula = null;
        Date fechaSql = rs.getDate("fecha_matricula");
        if (fechaSql != null) {
            fechaMatricula = fechaSql.toLocalDate();
        }

        String estadoStr = rs.getString("estado_matricula");
        EstadoMatricula estado = EstadoMatricula.ACTIVA;
        if (estadoStr != null) {
            try {
                estado = EstadoMatricula.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                estado = EstadoMatricula.ACTIVA;
            }
        }

        int idEstudiante = rs.getInt("id_estudiante");
        int idCurso = rs.getInt("id_curso");

        // Instanciación pasando los argumentos requeridos por los constructores completos
        Usuario estudiante = new Usuario(idEstudiante, null, null, null, null, null);
        Curso curso = new Curso(idCurso, null, null, null, null, 0, null);

        return new Matricula(idMatricula, fechaMatricula, estado, estudiante, curso);
    }
}