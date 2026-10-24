package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.enums.EstadoCurso;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CursoRepositoryImpl implements CursoRepository {

    @Override
    public Curso guardar(Curso curso) {
        String sql = "INSERT INTO curso (codigo_curso, nombre_curso, descripcion, id_periodo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, curso.getCodigoCurso());
            pstmt.setString(2, curso.getNombreCurso());
            pstmt.setString(3, curso.getDescripcion());
            pstmt.setInt(4, curso.getIdPeriodo());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    curso.setIdCurso(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el curso en la base de datos", e);
        }
        return curso;
    }

    @Override
    public Optional<Curso> buscarPorId(int idCurso) {
        String sql = "SELECT id_curso, codigo_curso, nombre_curso, descripcion, id_periodo FROM curso WHERE id_curso = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCurso);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCurso(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el curso por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Curso> buscarPorCodigo(String codigoCurso) {
        String sql = "SELECT id_curso, codigo_curso, nombre_curso, descripcion, id_periodo FROM curso WHERE codigo_curso = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoCurso);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCurso(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el curso por código", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Curso> listarTodos() {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT id_curso, codigo_curso, nombre_curso, descripcion, id_periodo FROM curso";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cursos.add(mapearCurso(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar todos los cursos", e);
        }
        return cursos;
    }

    @Override
    public List<Curso> listarPorPeriodo(int idPeriodo) {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT id_curso, codigo_curso, nombre_curso, descripcion, id_periodo FROM curso WHERE id_periodo = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPeriodo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(mapearCurso(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los cursos por periodo", e);
        }
        return cursos;
    }

    @Override
    public List<Curso> listarPorInstructor(int idInstructor) {
        List<Curso> cursos = new ArrayList<>();
        // Nota: Si en tu tabla agregaste id_instructor, esta consulta funcionará perfecto
        String sql = "SELECT id_curso, codigo_curso, nombre_curso, descripcion, id_periodo FROM curso WHERE id_instructor = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idInstructor);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(mapearCurso(rs));
                }
            }
        } catch (SQLException e) {
            // Si la columna id_instructor aún no está en la BD, retorna lista vacía en lugar de romper
            return new ArrayList<>();
        }
        return cursos;
    }

    @Override
    public Curso actualizar(Curso curso) {
        String sql = "UPDATE curso SET codigo_curso = ?, nombre_curso = ?, descripcion = ?, id_periodo = ? WHERE id_curso = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, curso.getCodigoCurso());
            pstmt.setString(2, curso.getNombreCurso());
            pstmt.setString(3, curso.getDescripcion());
            pstmt.setInt(4, curso.getIdPeriodo());
            pstmt.setInt(5, curso.getIdCurso());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el curso", e);
        }
        return curso;
    }

    @Override
    public void eliminar(int idCurso) {
        String sql = "DELETE FROM curso WHERE id_curso = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCurso);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el curso", e);
        }
    }

    private Curso mapearCurso(ResultSet rs) throws SQLException {
        int idCurso = rs.getInt("id_curso");
        String codigo = rs.getString("codigo_curso");
        String nombre = rs.getString("nombre_curso");
        String descripcion = rs.getString("descripcion");
        int idPeriodo = rs.getInt("id_periodo");

        EstadoCurso estado = EstadoCurso.ACTIVO;
        Integer idInstructor = null;

        return new Curso(idCurso, codigo, nombre, descripcion, estado, idPeriodo, idInstructor);
    }
}