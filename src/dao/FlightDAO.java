package dao;

import model.Flight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {

       public List<Flight> getAllAvailableFlights() {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_available_flights";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapFlight(rs));

        } catch (SQLException e) {
            System.err.println("[FlightDAO.getAllAvailableFlights] " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches for available flights via the {@code sp_search_flights} stored procedure.
     *
     * @param origin      departure city / airport code
     * @param destination arrival city / airport code
     * @param travelDate  date of travel (DATE portion only, time ignored by SP)
     * @return matching flights, or an empty list on failure
     */
    public List<Flight> searchFlights(String origin, String destination, Date travelDate) {
        List<Flight> list = new ArrayList<>();
        String sql = "{CALL sp_search_flights(?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, origin);
            cs.setString(2, destination);
            cs.setDate(3, travelDate);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) list.add(mapFlight(rs));
            }

        } catch (SQLException e) {
            System.err.println("[FlightDAO.searchFlights] " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns a single non-cancelled flight by its primary key.
     * Used before booking to verify the flight still exists and is bookable.
     *
     * @return the {@link Flight}, or {@code null} if not found or cancelled
     */
    public Flight getFlightById(int flightId) {
        String sql = "SELECT * FROM Flight WHERE flight_id = ? AND flight_status != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, flightId);
            ps.setString(2, Flight.STATUS_CANCELLED);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapFlight(rs);
            }

        } catch (SQLException e) {
            System.err.println("[FlightDAO.getFlightById] " + e.getMessage());
        }
        return null;
    }

    // ================================================================
    //  PRIVATE MAPPER
    // ================================================================

    private Flight mapFlight(ResultSet rs) throws SQLException {
        // available_seats may be aliased in the view; fall back to total_seats if absent
        int available;
        try {
            available = rs.getInt("available_seats");
        } catch (SQLException ex) {
            available = rs.getInt("total_seats");
        }

        int total;
        try {
            total = rs.getInt("total_seats");
        } catch (SQLException ex) {
            total = available;
        }

        int managedBy;
        try {
            managedBy = rs.getInt("managed_by");
        } catch (SQLException ex) {
            managedBy = 0;
        }

        return new Flight(
                rs.getInt("flight_id"),
                rs.getString("flight_number"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getTimestamp("departure_time"),
                rs.getTimestamp("arrival_time"),
                total,
                available,
                rs.getDouble("price_per_seat"),
                rs.getString("flight_status"),
                managedBy
        );
    }
}
