package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepositoryImpl implements UsuarioRepository {

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, apellido, email, password, id_rol, activo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, usuario.getApellidoUsuario());

            // El email es la credencial de login; solo se genera uno si no se proporcionó.
            String email = usuario.getEmail();
            if (email == null || email.isBlank()) {
                email = "user_" + System.currentTimeMillis() + "@academia.edu";
                usuario.setEmail(email);
            }
            pstmt.setString(3, email.trim().toLowerCase());

            pstmt.setString(4, usuario.getContrasena());
            pstmt.setInt(5, usuario.getRol() != null ? usuario.getRol().getIdRol() : 1);

            boolean activo = usuario.getEstado() == null || usuario.getEstado() == EstadoUsuario.ACTIVO;
            pstmt.setBoolean(6, activo);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setIdUsuario(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el usuario en la base de datos", e);
        }
    }

    @Override
    public Usuario buscarPorId(int idUsuario) {
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido, u.email, u.password, u.activo, " +
                "r.id_rol, r.nombre_rol, r.descripcion " +
                "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol WHERE u.id_usuario = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario por ID", e);
        }
        return null;
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido, u.email, u.password, u.activo, " +
                "r.id_rol, r.nombre_rol, r.descripcion " +
                "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol WHERE LOWER(u.email) = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario por email", e);
        }
        return null;
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre, u.apellido, u.email, u.password, u.activo, " +
                "r.id_rol, r.nombre_rol, r.descripcion " +
                "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los usuarios", e);
        }
        return usuarios;
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, apellido = ?, password = ?, id_rol = ?, activo = ? WHERE id_usuario = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, usuario.getApellidoUsuario());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setInt(4, usuario.getRol() != null ? usuario.getRol().getIdRol() : 1);

            boolean activo = usuario.getEstado() == null || usuario.getEstado() == EstadoUsuario.ACTIVO;
            pstmt.setBoolean(5, activo);

            pstmt.setInt(6, usuario.getIdUsuario());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el usuario", e);
        }
    }

    @Override
    public void eliminar(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el usuario", e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Rol rol = new Rol(
                rs.getInt("id_rol"),
                rs.getString("nombre_rol"),
                rs.getString("descripcion")
        );

        boolean activo = rs.getBoolean("activo");
        EstadoUsuario estado = activo ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO;

        Usuario usuario = new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("password"),
                estado,
                rol
        );
        usuario.setEmail(rs.getString("email"));
        return usuario;
    }
}
