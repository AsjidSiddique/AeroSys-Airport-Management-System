package dao;

import model.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    public Passenger getPassengerById(int passengerId) {
        String sql = "SELECT * FROM Passenger WHERE passenger_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPassenger(rs);
            }

        } catch (SQLException e) {
            System.err.println("[PassengerDAO.getPassengerById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns a map of dashboard statistics for the passenger portal
     * via the {@code sp_passenger_stats} stored procedure.
     *
     * <p>Keys: {@code passenger_name}, {@code passport_no},
     * {@code total_bookings}, {@code confirmed_bookings},
     * {@code active_tickets}, {@code total_spent}, {@code available_flights_count}.
     *
     * @return populated map, or an empty map on failure
     */
    public java.util.Map<String, Object> getPassengerStats(int passengerId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        String sql = "{CALL sp_passenger_stats(?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, passengerId);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    stats.put("passenger_name",          rs.getString("passenger_name"));
                    stats.put("passport_no",             rs.getString("passport_no"));
                    stats.put("total_bookings",          rs.getInt("total_bookings"));
                    stats.put("confirmed_bookings",      rs.getInt("confirmed_bookings"));
                    stats.put("active_tickets",          rs.getInt("active_tickets"));
                    stats.put("total_spent",             rs.getDouble("total_spent"));
                    stats.put("available_flights_count", rs.getInt("available_flights_count"));
                }
            }

        } catch (SQLException e) {
            System.err.println("[PassengerDAO.getPassengerStats] " + e.getMessage());
        }
        return stats;
    }

    // ================================================================
    //  UPDATE
    // ================================================================

    /**
     * Updates a passenger's editable profile fields (name, email, phone).
     *
     * @param passenger the passenger object with the new values set
     * @return {@code true} if the record was found and updated
     */
    public boolean updatePassenger(Passenger passenger) {
        String sql = "UPDATE Passenger SET name = ?, email = ?, phone = ? "
                   + "WHERE passenger_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, passenger.getName());
            ps.setString(2, passenger.getEmail());
            ps.setString(3, passenger.getPhone());
            ps.setInt(4, passenger.getPassengerId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PassengerDAO.updatePassenger] " + e.getMessage());
        }
        return false;
    }

    // ================================================================
    //  REGISTRATION REQUEST
    // ================================================================

    /**
     * Submits a new passenger registration request.
     *
     * <p>The record goes into {@code Passenger_Requests}; DB trigger T6 will
     * auto-create the Passenger account once an employee approves it.
     *
     * @return {@code true} if the row was inserted successfully
     */
    public boolean registerRequest(String name,       String email,
                                   String phone,      String passportNo,
                                   String nationality, Date dob,
                                   String username,   String password) {
        String sql = "INSERT INTO Passenger_Requests "
                   + "(name, email, phone, passport_no, nationality, date_of_birth, username, password) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, passportNo);
            ps.setString(5, nationality);
            ps.setDate(6, dob);
            ps.setString(7, username);
            ps.setString(8, password);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("username"))
                System.err.println("[PassengerDAO.registerRequest] Username already taken: " + username);
            else if (msg.contains("email"))
                System.err.println("[PassengerDAO.registerRequest] Email already registered: " + email);
            else if (msg.contains("passport_no"))
                System.err.println("[PassengerDAO.registerRequest] Passport already registered: " + passportNo);
            else
                System.err.println("[PassengerDAO.registerRequest] Duplicate entry: " + msg);

        } catch (SQLException e) {
            System.err.println("[PassengerDAO.registerRequest] " + e.getMessage());
        }
        return false;
    }

    // ================================================================
    //  PRIVATE MAPPER
    // ================================================================

    private Passenger mapPassenger(ResultSet rs) throws SQLException {
        return new Passenger(
                rs.getInt("passenger_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("passport_no"),
                rs.getString("nationality"),
                rs.getDate("date_of_birth"),
                rs.getString("username"),
                null,                            // password never loaded into model
                rs.getString("account_status"),
                rs.getTimestamp("created_at")
        );
    }
}
