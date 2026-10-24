package sv.gestionacademica.repository;

import sv.gestionacademica.entity.MaterialDidactico;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialRepositoryImpl implements MaterialRepository {

    @Override
    public MaterialDidactico save(MaterialDidactico material) {
        String sql = "INSERT INTO material_didactico (titulo, descripcion, url_enlace, id_curso) VALUES (?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, material.getTitulo());
            pstmt.setString(2, material.getDescripcion());
            pstmt.setString(3, material.getUrlEnlace());
            pstmt.setInt(4, obtenerIdCursoValido(conn, material));

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    material.setId(String.valueOf(generatedKeys.getInt(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el material didáctico en la base de datos", e);
        }
        return material;
    }

    @Override
    public Optional<MaterialDidactico> findById(String id) {
        String sql = "SELECT id_material, titulo, descripcion, url_enlace, id_curso FROM material_didactico WHERE id_material = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMaterial(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el material didáctico por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MaterialDidactico> findAll() {
        List<MaterialDidactico> materiales = new ArrayList<>();
        String sql = "SELECT id_material, titulo, descripcion, url_enlace, id_curso FROM material_didactico";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                materiales.add(mapearMaterial(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los materiales didácticos", e);
        }
        return materiales;
    }

    @Override
    public void update(MaterialDidactico material) {
        String sql = "UPDATE material_didactico SET titulo = ?, descripcion = ?, url_enlace = ?, id_curso = ? WHERE id_material = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, material.getTitulo());
            pstmt.setString(2, material.getDescripcion());
            pstmt.setString(3, material.getUrlEnlace());
            pstmt.setInt(4, obtenerIdCursoValido(conn, material));
            pstmt.setInt(5, Integer.parseInt(material.getId()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el material didáctico", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM material_didactico WHERE id_material = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el material didáctico", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT COUNT(1) FROM material_didactico WHERE id_material = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si existe el material didáctico", e);
        }
        return false;
    }

    private int obtenerIdCursoValido(Connection conn, MaterialDidactico material) {
        try {
            var m = material.getClass().getMethod("getIdCurso");
            Object res = m.invoke(material);
            if (res instanceof Integer && (Integer) res > 0) return (Integer) res;
        } catch (Exception ignored) {}

        try {
            var f = material.getClass().getDeclaredField("idCurso");
            f.setAccessible(true);
            Object res = f.get(material);
            if (res instanceof Integer && (Integer) res > 0) return (Integer) res;
        } catch (Exception ignored) {}

        String sql = "SELECT TOP 1 id_curso FROM curso";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id_curso");
            }
        } catch (SQLException ignored) {}

        return 1;
    }

    private MaterialDidactico mapearMaterial(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("id_material"));
        String titulo = rs.getString("titulo");
        String descripcion = rs.getString("descripcion");
        String urlEnlace = rs.getString("url_enlace");
        int idCurso = rs.getInt("id_curso");

        MaterialDidactico mat = new MaterialDidactico(id, titulo, descripcion, urlEnlace);

        try {
            var f = mat.getClass().getDeclaredField("idCurso");
            f.setAccessible(true);
            f.set(mat, idCurso);
        } catch (Exception ignored) {}

        return mat;
    }
}