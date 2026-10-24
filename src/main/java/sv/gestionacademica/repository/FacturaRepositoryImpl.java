package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Factura;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FacturaRepositoryImpl implements FacturaRepository {

    private static final String COLUMNAS = "id_factura, numero_factura, fecha_emision, monto_total, id_pago";

    // ------------------------------------------------------------------
    // Operaciones que abren y cierran su propia conexion (devuelta al pool)
    // ------------------------------------------------------------------

    @Override
    public void guardar(Factura factura) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            guardar(conn, factura);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para guardar la factura", e);
        }
    }

    // ------------------------------------------------------------------
    // Operaciones que usan la Connection recibida (participan en transaccion)
    // ------------------------------------------------------------------

    @Override
    public void guardar(Connection con, Factura factura) {
        String sql = "INSERT INTO factura (numero_factura, fecha_emision, monto_total, id_pago) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, factura.getNumeroFactura());
            pstmt.setObject(2, factura.getFechaEmision());
            pstmt.setDouble(3, factura.getMontoTotal());
            if (factura.getIdPago() != null) {
                pstmt.setInt(4, factura.getIdPago());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

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
    public Factura buscarPorPago(Connection con, int idPago) {
        String sql = "SELECT " + COLUMNAS + " FROM factura WHERE id_pago = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, idPago);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFactura(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la factura por pago", e);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Resto de operaciones
    // ------------------------------------------------------------------

    @Override
    public Factura buscarPorId(int idFactura) {
        String sql = "SELECT " + COLUMNAS + " FROM factura WHERE id_factura = ?";

        try (Connection conn = DataSourceConfig.getConnection();
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
        String sql = "SELECT " + COLUMNAS + " FROM factura WHERE numero_factura = ?";

        try (Connection conn = DataSourceConfig.getConnection();
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
        String sql = "SELECT " + COLUMNAS + " FROM factura";

        try (Connection conn = DataSourceConfig.getConnection();
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
        String sql = "UPDATE factura SET numero_factura = ?, fecha_emision = ?, monto_total = ?, id_pago = ? WHERE id_factura = ?";

        try (Connection conn = DataSourceConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, factura.getNumeroFactura());
            pstmt.setObject(2, factura.getFechaEmision());
            pstmt.setDouble(3, factura.getMontoTotal());
            if (factura.getIdPago() != null) {
                pstmt.setInt(4, factura.getIdPago());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, factura.getIdFactura());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la factura", e);
        }
    }

    @Override
    public void eliminar(int idFactura) {
        String sql = "DELETE FROM factura WHERE id_factura = ?";

        try (Connection conn = DataSourceConfig.getConnection();
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

        int idPagoBd = rs.getInt("id_pago");
        Integer idPago = rs.wasNull() ? null : idPagoBd;

        Factura factura = new Factura(idFactura, numeroFactura, fechaEmision, montoTotal);
        factura.setIdPago(idPago);
        return factura;
    }
}
