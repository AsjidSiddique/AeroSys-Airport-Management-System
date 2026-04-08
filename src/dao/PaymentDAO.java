package dao;

import model.Payment;

import java.sql.*;

public class PaymentDAO {

       public Payment createPayment(int bookingId, double amount, String paymentMethod) {
        String method = (paymentMethod == null || paymentMethod.isBlank()) ? "Cash" : paymentMethod;

        String sql = "INSERT INTO Payment (booking_id, amount, payment_method, payment_status) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, bookingId);
            ps.setDouble(2, amount);
            ps.setString(3, method);
            ps.setString(4, Payment.STATUS_PENDING);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Payment(id, bookingId, amount,
                            new Timestamp(System.currentTimeMillis()),
                            method, Payment.STATUS_PENDING, 0);
                }
            }

        } catch (SQLException e) {
            System.err.println("[PaymentDAO.createPayment] " + e.getMessage());
        }
        return null;
    }

    // ================================================================
    //  READ
    // ================================================================

    public Payment getPaymentByBookingId(int bookingId) {
        String sql = "SELECT * FROM Payment WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayment(rs);
            }

        } catch (SQLException e) {
            System.err.println("[PaymentDAO.getPaymentByBookingId] " + e.getMessage());
        }
        return null;
    }

    // ================================================================
    //  UPDATE
    // ================================================================

    public boolean markAsPaid(int bookingId, String paymentMethod, int processedBy) {

        // ── STEP 1: Confirm the booking first ────────────────────────
        // The DB trigger trg_before_ticket_insert blocks ticket creation
        // unless booking_status = 'Confirmed'. Passenger self-service
        // bookings are created as 'Pending' so we must confirm them here
        // before touching Payment or Ticket tables.
        String confirmSql = "UPDATE Booking SET booking_status = 'Confirmed' "
                          + "WHERE booking_id = ? AND booking_status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(confirmSql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate(); // no-op if already Confirmed — safe to call always
        } catch (SQLException e) {
            System.err.println("[PaymentDAO.markAsPaid/confirm] " + e.getMessage());
            return false;
        }

        // ── STEP 2: Update or insert the Payment row ──────────────────
        Payment existing = getPaymentByBookingId(bookingId);

        if (existing != null) {
            // Row exists — update it to Paid
            String sql = "UPDATE Payment SET payment_status = ?, payment_method = ?, "
                       + "paid_at = CURRENT_TIMESTAMP, processed_by = ? "
                       + "WHERE booking_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, Payment.STATUS_PAID);
                ps.setString(2, paymentMethod);
                // FIX: 0 is not a valid employee_id — NULL for self-service
                if (processedBy > 0) ps.setInt(3, processedBy);
                else                 ps.setNull(3, java.sql.Types.INTEGER);
                ps.setInt(4, bookingId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[PaymentDAO.markAsPaid/update] " + e.getMessage());
                return false;
            }

        } else {
            // No row yet — insert directly as Paid using booking's total_price
            String sql = "INSERT INTO Payment (booking_id, amount, payment_method, "
                       + "payment_status, paid_at, processed_by) "
                       + "SELECT ?, total_price, ?, ?, CURRENT_TIMESTAMP, ? "
                       + "FROM Booking WHERE booking_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookingId);
                ps.setString(2, paymentMethod);
                ps.setString(3, Payment.STATUS_PAID);
                // FIX: 0 is not a valid employee_id — NULL for self-service
                if (processedBy > 0) ps.setInt(4, processedBy);
                else                 ps.setNull(4, java.sql.Types.INTEGER);
                ps.setInt(5, bookingId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[PaymentDAO.markAsPaid/insert] " + e.getMessage());
                return false;
            }
        }
    }

    // ================================================================
    //  PRIVATE MAPPER
    // ================================================================

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("booking_id"),
                rs.getDouble("amount"),
                rs.getTimestamp("paid_at"),
                rs.getString("payment_method"),
                rs.getString("payment_status"),
                rs.getInt("processed_by")
        );
    }
}