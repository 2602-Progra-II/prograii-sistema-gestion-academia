package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FacturaRepositoryImpl implements FacturaRepository {

    @Override
    public void guardar(Factura factura) {
        String sql = "INSERT INTO factura (numero_factura, fecha_emision, monto_total) VALUES (?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, factura.getNumeroFactura());
            pstmt.setObject(2, factura.getFechaEmision());
            pstmt.setDouble(3, factura.getMontoTotal());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    factura.setIdFactura(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la factura en la base de datos", e);
        }
    }

    @Override
    public Factura buscarPorId(int idFactura) {
        String sql = "SELECT id_factura, numero_factura, fecha_emision, monto_total FROM factura WHERE id_factura = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idFactura);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFactura(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la factura por ID", e);
        }
        return null;
    }

    @Override
    public Factura buscarPorNumero(String numeroFactura) {
        String sql = "SELECT id_factura, numero_factura, fecha_emision, monto_total FROM factura WHERE numero_factura = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numeroFactura);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFactura(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la factura por número", e);
        }
        return null;
    }

    @Override
    public List<Factura> listarTodas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT id_factura, numero_factura, fecha_emision, monto_total FROM factura";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                facturas.add(mapearFactura(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las facturas", e);
        }
        return facturas;
    }

    @Override
    public void actualizar(Factura factura) {
        String sql = "UPDATE factura SET numero_factura = ?, fecha_emision = ?, monto_total = ? WHERE id_factura = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, factura.getNumeroFactura());
            pstmt.setObject(2, factura.getFechaEmision());
            pstmt.setDouble(3, factura.getMontoTotal());
            pstmt.setInt(4, factura.getIdFactura());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la factura", e);
        }
    }

    @Override
    public void eliminar(int idFactura) {
        String sql = "DELETE FROM factura WHERE id_factura = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idFactura);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la factura", e);
        }
    }

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        int idFactura = rs.getInt("id_factura");
        String numeroFactura = rs.getString("numero_factura");

        LocalDate fechaEmision = null;
        Date fechaSql = rs.getDate("fecha_emision");
        if (fechaSql != null) {
            fechaEmision = fechaSql.toLocalDate();
        }

        double montoTotal = rs.getDouble("monto_total");

        return new Factura(idFactura, numeroFactura, fechaEmision, montoTotal);
    }
}