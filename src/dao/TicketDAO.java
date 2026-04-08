package dao;

import model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

      public Ticket generateTicket(int bookingId, int passengerId,
                                 int flightId,  String seatNumber) {
        String sql = "INSERT INTO Ticket (booking_id, passenger_id, flight_id, "
                   + "seat_number, ticket_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, passengerId);
            ps.setInt(3, flightId);
            ps.setString(4, seatNumber.trim().toUpperCase());
            ps.setString(5, Ticket.STATUS_ACTIVE);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Ticket(
                            id, bookingId, passengerId, flightId,
                            seatNumber.trim().toUpperCase(),
                            new Timestamp(System.currentTimeMillis()),
                            Ticket.STATUS_ACTIVE);
                }
            }

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Booking must be Confirmed"))
                System.err.println("[TicketDAO.generateTicket] Booking not yet confirmed.");
            else if (msg.contains("Payment must be completed"))
                System.err.println("[TicketDAO.generateTicket] Payment not yet completed.");
            else if (msg.contains("uq_seat_per_flight"))
                System.err.println("[TicketDAO.generateTicket] Seat " + seatNumber + " already taken.");
            else
                System.err.println("[TicketDAO.generateTicket] " + msg);
        }
        return null;
    }

    // ================================================================
    //  READ
    // ================================================================

    /**
     * Returns all tickets for a passenger joined with their flight details.
     * Used by the passenger portal My Tickets panel.
     *
     * <p>Each {@code Object[]} row has eight elements:
     * <ol>
     *   <li>ticket_id (Integer)</li>
     *   <li>seat_number (String)</li>
     *   <li>flight_number (String)</li>
     *   <li>origin (String)</li>
     *   <li>destination (String)</li>
     *   <li>departure_time (Timestamp)</li>
     *   <li>issued_at (Timestamp)</li>
     *   <li>ticket_status (String)</li>
     * </ol>
     */
    public List<Object[]> getPassengerTicketsWithDetails(int passengerId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT t.ticket_id, t.seat_number, f.flight_number, f.origin, "
                   + "f.destination, f.departure_time, t.issued_at, t.ticket_status "
                   + "FROM Ticket t "
                   + "JOIN Flight f ON t.flight_id = f.flight_id "
                   + "WHERE t.passenger_id = ? "
                   + "ORDER BY t.issued_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("ticket_id"),
                        rs.getString("seat_number"),
                        rs.getString("flight_number"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getTimestamp("departure_time"),
                        rs.getTimestamp("issued_at"),
                        rs.getString("ticket_status")
                    };
                    rows.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("[TicketDAO.getPassengerTicketsWithDetails] " + e.getMessage());
        }
        return rows;
    }

    // ================================================================
    //  UTILITY
    // ================================================================

    /**
     * Generates a seat label for a given 0-based seat index.
     *
     * <p>Layout: rows of 6 seats labelled A–F.
     * Seat index 0 → "1A", 5 → "1F", 6 → "2A", etc.
     *
     * @param flightId  kept as a parameter for future per-flight seat-map logic
     * @param seatIndex 0-based index of the seat
     * @return seat label such as "3C"
     */
    public String generateSeatNumber(int flightId, int seatIndex) {
        final String[] COL_LETTERS = {"A", "B", "C", "D", "E", "F"};
        int rowNum    = (seatIndex / COL_LETTERS.length) + 1;
        String letter = COL_LETTERS[seatIndex % COL_LETTERS.length];
        return rowNum + letter;
    }

    // ================================================================
    //  PRIVATE MAPPER
    // ================================================================

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getInt("ticket_id"),
                rs.getInt("booking_id"),
                rs.getInt("passenger_id"),
                rs.getInt("flight_id"),
                rs.getString("seat_number"),
                rs.getTimestamp("issued_at"),
                rs.getString("ticket_status")
        );
    }
}
