package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolRepositoryImpl implements RolRepository {

    @Override
    public Rol save(Rol rol) {
        String sql = "INSERT INTO rol (nombre_rol, descripcion) VALUES (?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, rol.getNombreRol());
            pstmt.setString(2, rol.getDescripcion());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rol.setIdRol(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el rol en la base de datos", e);
        }
        return rol;
    }

    @Override
    public Optional<Rol> findById(int idRol) {
        String sql = "SELECT id_rol, nombre_rol, descripcion FROM rol WHERE id_rol = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRol);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearRol(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el rol por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Rol> findAll() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre_rol, descripcion FROM rol";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(mapearRol(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los roles", e);
        }
        return roles;
    }

    @Override
    public void update(Rol rol) {
        String sql = "UPDATE rol SET nombre_rol = ?, descripcion = ? WHERE id_rol = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rol.getNombreRol());
            pstmt.setString(2, rol.getDescripcion());
            pstmt.setInt(3, rol.getIdRol());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el rol", e);
        }
    }

    @Override
    public boolean deleteById(int idRol) {
        String sql = "DELETE FROM rol WHERE id_rol = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRol);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el rol", e);
        }
    }

    @Override
    public boolean existsById(int idRol) {
        String sql = "SELECT COUNT(1) FROM rol WHERE id_rol = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRol);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si existe el rol", e);
        }
        return false;
    }

    private Rol mapearRol(ResultSet rs) throws SQLException {
        int idRol = rs.getInt("id_rol");
        String nombreRol = rs.getString("nombre_rol");
        String descripcion = rs.getString("descripcion");

        return new Rol(idRol, nombreRol, descripcion);
    }
}