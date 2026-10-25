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

    private static final String COLUMNAS =
            "id_pago, monto, fecha_pago, ruta_comprobante, estado_pago, id_estudiante, id_asesor_validador, id_matricula";

    // ------------------------------------------------------------------
    // Operaciones que abren y cierran su propia conexion (devuelta al pool)
    // ------------------------------------------------------------------

    @Override
    public void guardar(Pago pago) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            guardar(conn, pago);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para guardar el pago", e);
        }
    }

    @Override
    public Optional<Pago> buscarPorId(int id) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            return buscarPorId(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para buscar el pago", e);
        }
    }

    @Override
    public void actualizar(Pago pagoActualizado) {
        try (Connection conn = DataSourceConfig.getConnection()) {
            actualizar(conn, pagoActualizado);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión para actualizar el pago", e);
        }
    }

    // ------------------------------------------------------------------
    // Operaciones que usan la Connection recibida (participan en transaccion)
    // ------------------------------------------------------------------

    @Override
    public void guardar(Connection con, Pago pago) {
        String sql = "INSERT INTO pago (monto, fecha_pago, ruta_comprobante, estado_pago, id_estudiante, id_asesor_validador, id_matricula) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, pago.getMonto());
            pstmt.setObject(2, pago.getFechaPago());
            pstmt.setString(3, pago.getRutaComprobante());
            pstmt.setString(4, pago.getEstadoPago() != null ? pago.getEstadoPago().name() : "PENDIENTE");
            pstmt.setString(5, pago.getIdEstudiante());
            pstmt.setString(6, pago.getIdAsesorValidador());
            asignarMatricula(pstmt, 7, pago);

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
    public Optional<Pago> buscarPorId(Connection con, int id) {
        return buscar(con, id, false);
    }

    @Override
    public Optional<Pago> buscarPorIdBloqueando(Connection con, int id) {
        return buscar(con, id, true);
    }

    @Override
    public void actualizar(Connection con, Pago pagoActualizado) {
        String sql = "UPDATE pago SET monto = ?, fecha_pago = ?, ruta_comprobante = ?, estado_pago = ?, id_estudiante = ?, id_asesor_validador = ?, id_matricula = ? WHERE id_pago = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setDouble(1, pagoActualizado.getMonto());
            pstmt.setObject(2, pagoActualizado.getFechaPago());
            pstmt.setString(3, pagoActualizado.getRutaComprobante());
            pstmt.setString(4, pagoActualizado.getEstadoPago() != null ? pagoActualizado.getEstadoPago().name() : "PENDIENTE");
            pstmt.setString(5, pagoActualizado.getIdEstudiante());
            pstmt.setString(6, pagoActualizado.getIdAsesorValidador());
            asignarMatricula(pstmt, 7, pagoActualizado);
            pstmt.setInt(8, pagoActualizado.getIdPago());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el pago", e);
        }
    }

    // ------------------------------------------------------------------
    // Resto de operaciones
    // ------------------------------------------------------------------

    @Override
    public List<Pago> listarPendientes() {
        List<Pago> pendientes = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM pago WHERE estado_pago = 'PENDIENTE' OR estado_pago = 'Pendiente'";

        try (Connection conn = DataSourceConfig.getConnection();
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
    public List<Pago> listarPorEstudiante(String idEstudiante) {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM pago WHERE id_estudiante = ? ORDER BY id_pago";

        try (Connection conn = DataSourceConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idEstudiante);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapearPago(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los pagos del estudiante", e);
        }
        return pagos;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Optional<Pago> buscar(Connection con, int id, boolean bloquear) {
        // UPDLOCK: quien lee para modificar retiene la fila hasta terminar su transaccion,
        // asi dos aprobaciones simultaneas del mismo pago no pueden pasar a la vez.
        String sql = "SELECT " + COLUMNAS + " FROM pago" + (bloquear ? " WITH (UPDLOCK, ROWLOCK)" : "") + " WHERE id_pago = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
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

    private void asignarMatricula(PreparedStatement pstmt, int indice, Pago pago) throws SQLException {
        if (pago.getIdMatricula() != null) {
            pstmt.setInt(indice, pago.getIdMatricula());
        } else {
            pstmt.setNull(indice, Types.INTEGER);
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

        int idMatriculaBd = rs.getInt("id_matricula");
        Integer idMatricula = rs.wasNull() ? null : idMatriculaBd;

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
        pago.setIdMatricula(idMatricula);

        return pago;
    }
}
