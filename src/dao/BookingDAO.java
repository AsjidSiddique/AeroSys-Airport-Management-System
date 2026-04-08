package dao;

import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

       public Booking createBooking(int passengerId, int flightId,
                                 int seatCount,  double totalPrice) {
        String sql = "INSERT INTO Booking (passenger_id, flight_id, seat_count, total_price, "
                   + "booking_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, passengerId);
            ps.setInt(2, flightId);
            ps.setInt(3, seatCount);
            ps.setDouble(4, totalPrice);
            ps.setString(5, Booking.STATUS_PENDING);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Booking(
                            id, passengerId, flightId,
                            new Timestamp(System.currentTimeMillis()),
                            seatCount, totalPrice, Booking.STATUS_PENDING, 0);
                }
            }

        } catch (SQLException e) {
            System.err.println("[BookingDAO.createBooking] " + e.getMessage());
        }
        return null;
    }

    // ================================================================
    //  READ
    // ================================================================

    /**
     * Returns a single booking by its primary key.
     *
     * @return the {@link Booking}, or {@code null} if not found
     */
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM Booking WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBooking(rs);
            }

        } catch (SQLException e) {
            System.err.println("[BookingDAO.getBookingById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all bookings for a passenger joined with their flight details.
     * Used by the passenger portal to populate the My Bookings panel.
     *
     * <p>Each {@code Object[]} row has nine elements:
     * <ol>
     *   <li>booking_id (Integer)</li>
     *   <li>flight_number (String)</li>
     *   <li>origin (String)</li>
     *   <li>destination (String)</li>
     *   <li>departure_time (Timestamp)</li>
     *   <li>seat_count (Integer)</li>
     *   <li>total_price (Double)</li>
     *   <li>booking_status (String)</li>
     *   <li>booked_at (Timestamp)</li>
     * </ol>
     */
    public List<Object[]> getPassengerBookingsWithDetails(int passengerId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT b.booking_id, f.flight_number, f.origin, f.destination, "
                   + "f.departure_time, b.seat_count, b.total_price, b.booking_status, "
                   + "b.booked_at "
                   + "FROM Booking b "
                   + "JOIN Flight f ON b.flight_id = f.flight_id "
                   + "WHERE b.passenger_id = ? "
                   + "ORDER BY b.booked_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("booking_id"),
                        rs.getString("flight_number"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getTimestamp("departure_time"),
                        rs.getInt("seat_count"),
                        rs.getDouble("total_price"),
                        rs.getString("booking_status"),
                        rs.getTimestamp("booked_at")
                    };
                    rows.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("[BookingDAO.getPassengerBookingsWithDetails] " + e.getMessage());
        }
        return rows;
    }

    // ================================================================
    //  UPDATE
    // ================================================================

    /**
     * Cancels a booking by setting its status to {@link Booking#STATUS_CANCELLED}.
     * The DB trigger T3 will automatically restore available seats and cancel tickets.
     *
     * @return {@code true} if the booking was found and updated
     */
    public boolean cancelBooking(int bookingId) {
        String sql = "UPDATE Booking SET booking_status = ? WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Booking.STATUS_CANCELLED);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[BookingDAO.cancelBooking] " + e.getMessage());
        }
        return false;
    }

    // ================================================================
    //  PRIVATE MAPPER
    // ================================================================

    private Booking mapBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("booking_id"),
                rs.getInt("passenger_id"),
                rs.getInt("flight_id"),
                rs.getTimestamp("booked_at"),
                rs.getInt("seat_count"),
                rs.getDouble("total_price"),
                rs.getString("booking_status"),
                rs.getInt("processed_by")
        );
    }
}
