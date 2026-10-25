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

    // codigo_curso y nombre_curso se traen para que la matricula muestre su curso
    // (antes el Curso quedaba solo con el id y el resto en null).
    private static final String COLUMNAS =
            "id_matricula, fecha_matricula, estado_matricula, id_estudiante, id_curso, "
            + "(SELECT c.codigo_curso FROM curso c WHERE c.id_curso = matricula.id_curso) AS codigo_curso, "
            + "(SELECT c.nombre_curso FROM curso c WHERE c.id_curso = matricula.id_curso) AS nombre_curso";

    // ------------------------------------------------------------------
    // Operaciones que abren y cierran su propia conexion (devuelta al pool)
    // ------------------------------------------------------------------

    @Override
    public Matricula save(Matricula matricula) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            return save(conn, matricula);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para guardar la matrícula", e);
        }
    }

    @Override
    public Optional<Matricula> findById(int idMatricula) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            return findById(conn, idMatricula);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para buscar la matrícula", e);
        }
    }

    @Override
    public List<Matricula> findByEstudiante(String idEstudiante) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            return findByEstudiante(conn, idEstudiante);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para listar matrículas por estudiante", e);
        }
    }

    @Override
    public void update(Matricula matricula) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            update(conn, matricula);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para actualizar la matrícula", e);
        }
    }

    // ------------------------------------------------------------------
    // Operaciones que usan la Connection recibida (participan en transaccion)
    // ------------------------------------------------------------------

    @Override
    public Matricula save(Connection con, Matricula matricula) {
        String sql = "INSERT INTO matricula (fecha_matricula, estado_matricula, id_estudiante, id_curso) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setObject(1, matricula.getFechaMatricula() != null ? matricula.getFechaMatricula() : LocalDate.now());
            pstmt.setString(2, matricula.getEstadoMatricula() != null ? matricula.getEstadoMatricula().name() : "ACTIVA");
            asignarEstudiante(pstmt, 3, matricula);
            asignarCurso(pstmt, 4, matricula);

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
    public Optional<Matricula> findById(Connection con, int idMatricula) {
        String sql = "SELECT " + COLUMNAS + " FROM matricula WHERE id_matricula = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
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
    public List<Matricula> findByEstudiante(Connection con, String idEstudiante) {
        List<Matricula> matriculas = new ArrayList<>();
        // id_estudiante es VARCHAR en la BD: se compara siempre como texto
        // (comparar contra un int obligaria a convertir toda la columna).
        String sql = "SELECT " + COLUMNAS + " FROM matricula WHERE id_estudiante = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, idEstudiante);

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
    public void update(Connection con, Matricula matricula) {
        String sql = "UPDATE matricula SET fecha_matricula = ?, estado_matricula = ?, id_estudiante = ?, id_curso = ? WHERE id_matricula = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setObject(1, matricula.getFechaMatricula());
            pstmt.setString(2, matricula.getEstadoMatricula() != null ? matricula.getEstadoMatricula().name() : "ACTIVA");
            asignarEstudiante(pstmt, 3, matricula);
            asignarCurso(pstmt, 4, matricula);
            pstmt.setInt(5, matricula.getIdMatricula());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la matrícula", e);
        }
    }

    // ------------------------------------------------------------------
    // Resto de operaciones (sin cambios de comportamiento)
    // ------------------------------------------------------------------

    @Override
    public List<Matricula> findAll() {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM matricula";

        try (Connection conn = DataSourceConfig.getConnection();
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
    public List<Matricula> findByCurso(int idCurso) {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM matricula WHERE id_curso = ?";

        try (Connection conn = DataSourceConfig.getConnection();
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
    public boolean deleteById(int idMatricula) {
        String sql = "DELETE FROM matricula WHERE id_matricula = ?";

        try (Connection conn = DataSourceConfig.getConnection();
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

        try (Connection conn = DataSourceConfig.getConnection();
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

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private void asignarEstudiante(PreparedStatement pstmt, int indice, Matricula matricula) throws SQLException {
        if (matricula.getEstudiante() != null && matricula.getEstudiante().getIdUsuario() > 0) {
            pstmt.setString(indice, String.valueOf(matricula.getEstudiante().getIdUsuario()));
        } else {
            pstmt.setNull(indice, Types.VARCHAR);
        }
    }

    private void asignarCurso(PreparedStatement pstmt, int indice, Matricula matricula) throws SQLException {
        if (matricula.getCurso() != null && matricula.getCurso().getIdCurso() > 0) {
            pstmt.setInt(indice, matricula.getCurso().getIdCurso());
        } else {
            pstmt.setNull(indice, Types.INTEGER);
        }
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

        // id_estudiante se guarda como texto con el id numerico del usuario ("3", "4"...)
        int idEstudiante = 0;
        String idEstudianteTexto = rs.getString("id_estudiante");
        if (idEstudianteTexto != null) {
            try {
                idEstudiante = Integer.parseInt(idEstudianteTexto.trim());
            } catch (NumberFormatException e) {
                idEstudiante = 0;
            }
        }
        int idCurso = rs.getInt("id_curso");

        Usuario estudiante = new Usuario(idEstudiante, null, null, null, null, null);
        Curso curso = new Curso(idCurso, rs.getString("codigo_curso"), rs.getString("nombre_curso"),
                null, null, 0, null);

        return new Matricula(idMatricula, fechaMatricula, estado, estudiante, curso);
    }
}
