package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Pago;
import sv.gestionacademica.enums.EstadoPago;
import sv.gestionacademica.persistence.DataSourceConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PagoRepositoryImpl implements PagoRepository {

    @Override
    public void guardar(Pago pago) {
        String sql = "INSERT INTO pago (monto, fecha_pago, ruta_comprobante, estado_pago, id_estudiante, id_asesor_validador) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, pago.getMonto());
            pstmt.setObject(2, pago.getFechaPago());
            pstmt.setString(3, pago.getRutaComprobante());
            pstmt.setString(4, pago.getEstadoPago() != null ? pago.getEstadoPago().name() : "PENDIENTE");
            pstmt.setString(5, pago.getIdEstudiante());
            pstmt.setString(6, pago.getIdAsesorValidador());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pago.setIdPago(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el pago en la base de datos", e);
        }
    }

    @Override
    public Optional<Pago> buscarPorId(int id) {
        String sql = "SELECT id_pago, monto, fecha_pago, ruta_comprobante, estado_pago, id_estudiante, id_asesor_validador FROM pago WHERE id_pago = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPago(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el pago por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Pago> listarPendientes() {
        List<Pago> pendientes = new ArrayList<>();
        String sql = "SELECT id_pago, monto, fecha_pago, ruta_comprobante, estado_pago, id_estudiante, id_asesor_validador FROM pago WHERE estado_pago = 'PENDIENTE' OR estado_pago = 'Pendiente'";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pendientes.add(mapearPago(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los pagos pendientes", e);
        }
        return pendientes;
    }

    @Override
    public void actualizar(Pago pagoActualizado) {
        String sql = "UPDATE pago SET monto = ?, fecha_pago = ?, ruta_comprobante = ?, estado_pago = ?, id_estudiante = ?, id_asesor_validador = ? WHERE id_pago = ?";

        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, pagoActualizado.getMonto());
            pstmt.setObject(2, pagoActualizado.getFechaPago());
            pstmt.setString(3, pagoActualizado.getRutaComprobante());
            pstmt.setString(4, pagoActualizado.getEstadoPago() != null ? pagoActualizado.getEstadoPago().name() : "PENDIENTE");
            pstmt.setString(5, pagoActualizado.getIdEstudiante());
            pstmt.setString(6, pagoActualizado.getIdAsesorValidador());
            pstmt.setInt(7, pagoActualizado.getIdPago());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el pago", e);
        }
    }

    private Pago mapearPago(ResultSet rs) throws SQLException {
        int idPago = rs.getInt("id_pago");
        double monto = rs.getDouble("monto");

        LocalDate fechaPago = null;
        Date fechaSql = rs.getDate("fecha_pago");
        if (fechaSql != null) {
            fechaPago = fechaSql.toLocalDate();
        }

        String rutaComprobante = rs.getString("ruta_comprobante");
        String idEstudiante = rs.getString("id_estudiante");
        String idAsesor = rs.getString("id_asesor_validador");

        String estadoStr = rs.getString("estado_pago");
        EstadoPago estado = EstadoPago.PENDIENTE;
        if (estadoStr != null) {
            try {
                estado = EstadoPago.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                estado = EstadoPago.PENDIENTE;
            }
        }

        Pago pago = new Pago(idPago, monto, fechaPago, rutaComprobante, idEstudiante);
        pago.setEstadoPago(estado);
        pago.setIdAsesorValidador(idAsesor);

        return pago;
    }
}