package dao;

import model.Booking;
import model.Employee;
import model.Flight;
import model.Passenger;
import model.PassengerRequest;
import model.Payment;
import model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeDAO – Data Access Object for all employee-side operations.
 * Pattern:
 *   AeroSysAdminUI  →  EmployeeService  →  EmployeeDAO  →  MySQL
 */
public class EmployeeDAO {

    public String addEmployee(String name,     String email,
                              String phone,    String role,
                              String username, String password) {
        String sql = "INSERT INTO Employee (name, email, phone, role, username, password, account_status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone.trim());
            ps.setString(4, role);
            ps.setString(5, username.trim());
            ps.setString(6, password);
            ps.setString(7, Employee.STATUS_ACTIVE);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                String idStr = keys.next() ? " (ID: " + keys.getInt(1) + ")" : "";
                return "SUCCESS:Employee '" + name + "' added successfully." + idStr;
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("username"))
                return "ERROR:Username '" + username + "' is already taken. Please choose another.";
            if (msg.contains("email"))
                return "ERROR:Email '" + email + "' is already registered.";
            return "ERROR:Duplicate entry detected. Check username and email.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.addEmployee] " + e.getMessage());
            return "ERROR:Database error while adding employee. Please try again.";
        }
    }

    public String deactivateEmployee(int employeeId, int doneByAdminId) {
        String checkSql = "SELECT role, name FROM Employee WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Guard: block deactivation of ADMIN
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, employeeId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) return "ERROR:Employee not found.";
                    if (Employee.ROLE_ADMIN.equals(rs.getString("role")))
                        return "ERROR:ADMIN accounts cannot be deactivated.";
                }
            }

            String sql = "UPDATE Employee SET account_status = ?, "
                       + "deactivated_at = CURRENT_TIMESTAMP, deactivated_by = ? "
                       + "WHERE employee_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, Employee.STATUS_DEACTIVATED);
                ps.setInt(2, doneByAdminId);
                ps.setInt(3, employeeId);
                int rows = ps.executeUpdate();
                if (rows > 0)
                    return "SUCCESS:Employee #" + employeeId + " account has been deactivated.";
                return "ERROR:No changes made. Employee may already be deactivated.";
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.deactivateEmployee] " + e.getMessage());
            return "ERROR:Database error while deactivating employee.";
        }
    }

    public String reactivateEmployee(int employeeId) {
        String sql = "UPDATE Employee SET account_status = ?, "
                   + "deactivated_at = NULL, deactivated_by = NULL "
                   + "WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Employee.STATUS_ACTIVE);
            ps.setInt(2, employeeId);
            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Employee account reactivated successfully.";
            return "ERROR:Employee not found or already active.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.reactivateEmployee] " + e.getMessage());
            return "ERROR:Database error while reactivating employee.";
        }
    }

    public List<Employee> getAllEmployees() {
        return queryEmployees("SELECT * FROM Employee ORDER BY role DESC, created_at ASC");
    }

    public List<Employee> getAllActiveEmployees() {
        return queryEmployees("SELECT * FROM vw_active_employees");
    }

  
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT * FROM Employee WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployee(rs);
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.getEmployeeById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches employees by name, username, role, or account status.
     */
    public List<Employee> searchEmployees(String query) {
        String sql = "SELECT * FROM Employee WHERE "
                   + "LOWER(name)           LIKE ? OR "
                   + "LOWER(username)       LIKE ? OR "
                   + "LOWER(role)           LIKE ? OR "
                   + "LOWER(account_status) LIKE ?";
        return queryEmployeesWithParam(sql, query);
    }

    
    public String addFlight(String flightNumber, String origin,   String destination,
                            String departureTime, String arrivalTime,
                            int    totalSeats,    double pricePerSeat,
                            String flightStatus,  int    managedBy) {
        String sql = "INSERT INTO Flight "
                   + "(flight_number, origin, destination, departure_time, arrival_time, "
                   + "total_seats, available_seats, price_per_seat, flight_status, managed_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, flightNumber.trim().toUpperCase());
            ps.setString(2, origin.trim());
            ps.setString(3, destination.trim());
            ps.setString(4, departureTime);
            ps.setString(5, arrivalTime);
            ps.setInt(6, totalSeats);
            ps.setInt(7, totalSeats);         // available starts = total
            ps.setDouble(8, pricePerSeat);
            ps.setString(9, flightStatus);
            ps.setInt(10, managedBy);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                String idStr = keys.next() ? " (Flight ID: " + keys.getInt(1) + ")" : "";
                return "SUCCESS:Flight " + flightNumber.toUpperCase() + " added successfully." + idStr;
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            return "ERROR:Flight number '" + flightNumber + "' already exists.";

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("chk_flt_time"))
                return "ERROR:Arrival time must be after departure time.";
            System.err.println("[EmployeeDAO.addFlight] " + msg);
            return "ERROR:Database error while adding flight. Check date format (YYYY-MM-DD HH:MM:SS).";
        }
    }

    /**
     * Updates an existing flight's details.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String updateFlight(int flightId,     String origin,      String destination,
                               String departureTime, String arrivalTime,
                               int    totalSeats,    double pricePerSeat,
                               String flightStatus,  int    managedBy) {
        String sql = "UPDATE Flight SET origin = ?, destination = ?, "
                   + "departure_time = ?, arrival_time = ?, "
                   + "total_seats = ?, price_per_seat = ?, "
                   + "flight_status = ?, managed_by = ? "
                   + "WHERE flight_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, origin.trim());
            ps.setString(2, destination.trim());
            ps.setString(3, departureTime);
            ps.setString(4, arrivalTime);
            ps.setInt(5, totalSeats);
            ps.setDouble(6, pricePerSeat);
            ps.setString(7, flightStatus);
            ps.setInt(8, managedBy);
            ps.setInt(9, flightId);

            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Flight updated successfully.";
            return "ERROR:Flight not found.";

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("chk_flt_time"))
                return "ERROR:Arrival time must be after departure time.";
            System.err.println("[EmployeeDAO.updateFlight] " + msg);
            return "ERROR:Database error while updating flight.";
        }
    }

    /**
     * Cancels a flight via the {@code sp_cancel_flight} stored procedure.
     * Blocked by the SP if the flight has confirmed bookings.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelFlight(int flightId, int cancelledByEmpId, String reason) {
        String sql = "{CALL sp_cancel_flight(?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, flightId);
            cs.setInt(2, cancelledByEmpId);
            cs.setString(3, reason != null ? reason : "Cancelled by employee.");
            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                if (rs != null && rs.next())
                    return "SUCCESS:" + rs.getString("result_message");
            }
            return "SUCCESS:Flight cancelled successfully.";

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Cannot cancel"))
                return "ERROR:Cannot cancel flight — it has confirmed bookings. Cancel all bookings first.";
            System.err.println("[EmployeeDAO.cancelFlight] " + msg);
            return "ERROR:Database error while cancelling flight.";
        }
    }

    /**
     * Updates only the flight status field.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String updateFlightStatus(int flightId, String newStatus) {
        String sql = "UPDATE Flight SET flight_status = ? WHERE flight_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, flightId);
            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Flight status updated to '" + newStatus + "'.";
            return "ERROR:Flight not found.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.updateFlightStatus] " + e.getMessage());
            return "ERROR:Database error while updating flight status.";
        }
    }

    /**
     * Returns all non-cancelled flights via the {@code vw_all_flights} view.
     */
    public List<Flight> getAllFlights() {
        return queryFlights("SELECT * FROM vw_all_flights");
    }

    /**
     * Searches flights by flight number, origin, destination, or status.
     */
    public List<Flight> searchFlights(String query) {
        String sql = "SELECT * FROM vw_all_flights WHERE "
                   + "LOWER(flight_number) LIKE ? OR "
                   + "LOWER(origin)        LIKE ? OR "
                   + "LOWER(destination)   LIKE ? OR "
                   + "LOWER(flight_status) LIKE ?";
        return queryFlightsWithParam(sql, query);
    }

    // ================================================================
    //  BOOKING MANAGEMENT
    // ================================================================

    /**
     * Confirms a pending booking (sets status → Confirmed).
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String confirmBooking(int bookingId, int processedByEmpId) {
        String sql = "UPDATE Booking SET booking_status = ?, processed_by = ? "
                   + "WHERE booking_id = ? AND booking_status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Booking.STATUS_CONFIRMED);
            ps.setInt(2, processedByEmpId);
            ps.setInt(3, bookingId);
            ps.setString(4, Booking.STATUS_PENDING);

            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Booking #" + bookingId + " confirmed successfully.";
            return "ERROR:Booking not found or is not in Pending status.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.confirmBooking] " + e.getMessage());
            return "ERROR:Database error while confirming booking.";
        }
    }

    /**
     * Cancels a booking.
     * DB trigger T3 automatically restores available seats and cancels tickets.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelBooking(int bookingId) {
        String sql = "UPDATE Booking SET booking_status = ? "
                   + "WHERE booking_id = ? AND booking_status != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Booking.STATUS_CANCELLED);
            ps.setInt(2, bookingId);
            ps.setString(3, Booking.STATUS_CANCELLED);

            int rows = ps.executeUpdate();
            if (rows > 0)
                return "SUCCESS:Booking #" + bookingId + " cancelled. Seats restored and tickets cancelled.";
            return "ERROR:Booking not found or already cancelled.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.cancelBooking] " + e.getMessage());
            return "ERROR:Database error while cancelling booking.";
        }
    }

    /**
     * Returns all bookings via the {@code vw_all_bookings} view.
     */
    public List<Booking> getAllBookings() {
        return queryBookings("SELECT * FROM vw_all_bookings");
    }

    /**
     * Searches bookings by passenger name, flight number, passport, or status.
     */
    public List<Booking> searchBookings(String query) {
        String sql = "SELECT * FROM vw_all_bookings WHERE "
                   + "LOWER(passenger_name) LIKE ? OR "
                   + "LOWER(flight_number)  LIKE ? OR "
                   + "LOWER(booking_status) LIKE ? OR "
                   + "LOWER(passport_no)    LIKE ?";
        return queryBookingsWithParam(sql, query);
    }

    // ================================================================
    //  TICKET MANAGEMENT
    // ================================================================

    /**
     * Generates a ticket for a confirmed and paid booking.
     * DB trigger T5 enforces: booking must be Confirmed AND payment Paid.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String generateTicket(int bookingId, int passengerId,
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
                String ticketId = keys.next() ? String.valueOf(keys.getInt(1)) : "?";
                return "SUCCESS:Ticket #" + ticketId + " generated for seat "
                        + seatNumber.toUpperCase() + ".";
            }

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Booking must be Confirmed"))
                return "ERROR:Cannot generate ticket — booking is not Confirmed yet.";
            if (msg.contains("Payment must be completed"))
                return "ERROR:Cannot generate ticket — payment has not been completed.";
            if (msg.contains("uq_seat_per_flight"))
                return "ERROR:Seat " + seatNumber.toUpperCase() + " is already taken on this flight.";
            System.err.println("[EmployeeDAO.generateTicket] " + msg);
            return "ERROR:Database error while generating ticket.";
        }
    }

    /**
     * Cancels an Active ticket.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelTicket(int ticketId) {
        String sql = "UPDATE Ticket SET ticket_status = ? "
                   + "WHERE ticket_id = ? AND ticket_status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Ticket.STATUS_CANCELLED);
            ps.setInt(2, ticketId);
            ps.setString(3, Ticket.STATUS_ACTIVE);

            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Ticket #" + ticketId + " cancelled successfully.";
            return "ERROR:Ticket not found or already cancelled/used.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.cancelTicket] " + e.getMessage());
            return "ERROR:Database error while cancelling ticket.";
        }
    }

    /**
     * Returns all tickets via the {@code vw_all_tickets} view.
     */
    public List<Ticket> getAllTickets() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_all_tickets";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapTicket(rs));

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.getAllTickets] " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    //  PAYMENT MANAGEMENT
    // ================================================================

    /**
     * Records or updates a payment for a booking as Paid.
     * If a Pending payment record already exists it is updated in-place;
     * otherwise a fresh Paid record is inserted.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String recordPayment(int bookingId, double amount,
                                String paymentMethod, int processedByEmpId) {
        String checkSql = "SELECT payment_id, payment_status FROM Payment WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Check for existing payment row
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, bookingId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        if (Payment.STATUS_PAID.equals(rs.getString("payment_status")))
                            return "ERROR:Payment for booking #" + bookingId + " is already completed.";

                        // Update existing Pending → Paid
                        String updateSql = "UPDATE Payment SET amount = ?, payment_method = ?, "
                                         + "payment_status = ?, paid_at = CURRENT_TIMESTAMP, "
                                         + "processed_by = ? WHERE booking_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                            ps.setDouble(1, amount);
                            ps.setString(2, paymentMethod);
                            ps.setString(3, Payment.STATUS_PAID);
                            ps.setInt(4, processedByEmpId);
                            ps.setInt(5, bookingId);
                            ps.executeUpdate();
                            return "SUCCESS:Payment of $" + String.format("%.2f", amount)
                                    + " recorded for booking #" + bookingId + ".";
                        }
                    }
                }
            }

            // No existing row — insert fresh Paid record
            String insertSql = "INSERT INTO Payment "
                             + "(booking_id, amount, payment_method, payment_status, paid_at, processed_by) "
                             + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, bookingId);
                ps.setDouble(2, amount);
                ps.setString(3, paymentMethod);
                ps.setString(4, Payment.STATUS_PAID);
                ps.setInt(5, processedByEmpId);
                ps.executeUpdate();
                return "SUCCESS:Payment of $" + String.format("%.2f", amount)
                        + " recorded for booking #" + bookingId + ".";
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.recordPayment] " + e.getMessage());
            return "ERROR:Database error while recording payment.";
        }
    }
// ================================================================
//  TICKET MANAGEMENT 
// ================================================================

/**
 * Counts non-cancelled tickets already issued for a given booking.
 * @return ticket count, or -1 on DB error
 */
public int countIssuedTickets(int bookingId) {
    String sql = "SELECT COUNT(*) FROM Ticket "
               + "WHERE booking_id = ? AND ticket_status != ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, bookingId);
        ps.setString(2, Ticket.STATUS_CANCELLED);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }

    } catch (SQLException e) {
        System.err.println("[EmployeeDAO.countIssuedTickets] " + e.getMessage());
    }
    return -1;
}
    /**
     * Returns all payments via the {@code vw_all_payments} view.
     */
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_all_payments";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapPayment(rs));

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.getAllPayments] " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches payments by passenger name, method, status, or flight number.
     */
    public List<Payment> searchPayments(String query) {
        String sql = "SELECT * FROM vw_all_payments WHERE "
                   + "LOWER(passenger_name) LIKE ? OR "
                   + "LOWER(payment_method) LIKE ? OR "
                   + "LOWER(payment_status) LIKE ? OR "
                   + "LOWER(flight_number)  LIKE ?";
        List<Payment> list = new ArrayList<>();
        String q = "%" + query.toLowerCase().trim() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q); ps.setString(2, q);
            ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPayment(rs));
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.searchPayments] " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    //  PASSENGER REQUEST MANAGEMENT
    // ================================================================

    /**
     * Returns all Pending registration requests.
     */
    public List<PassengerRequest> getPendingRequests() {
        return queryRequests("SELECT * FROM vw_pending_requests");
    }

    /**
     * Returns all registration requests regardless of status.
     */
    public List<PassengerRequest> getAllRequests() {
        return queryRequests("SELECT * FROM Passenger_Requests ORDER BY submitted_at DESC");
    }

    /**
     * Approves a Pending registration request.
     * DB trigger T6 auto-creates the Passenger account.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String approveRequest(int requestId, int reviewedByEmpId) {
    // Call the stored procedure that does BOTH:
    // 1. UPDATE Passenger_Requests SET status='Approved'
    // 2. INSERT INTO Passenger (creates the account)
    String sql = "{CALL sp_approve_request(?, ?, ?)}";

    try (Connection conn = DatabaseConnection.getConnection();
         CallableStatement cs = conn.prepareCall(sql)) {

        cs.setInt(1, requestId);
        cs.setInt(2, reviewedByEmpId);
        cs.registerOutParameter(3, java.sql.Types.VARCHAR);  // OUT p_result
        cs.execute();

        String result = cs.getString(3);
        if (result != null && result.startsWith("ERROR:"))
            return result;
        return "SUCCESS:" + (result != null
                ? result.replace("SUCCESS:", "")
                : "Request #" + requestId + " approved. Passenger account created.");

    } catch (SQLException e) {
        System.err.println("[EmployeeDAO.approveRequest] " + e.getMessage());
        return "ERROR:Database error while approving request #" + requestId + ".";
    }
}

    /**
     * Rejects a Pending registration request.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String rejectRequest(int requestId, int reviewedByEmpId) {
        // DB stores "Rejected" (title-case) — use literal, not model constant (which is UPPERCASE)
        return reviewRequest(requestId, reviewedByEmpId,
                "Rejected",
                "Request #" + requestId + " rejected successfully.");
    }

    // ================================================================
    //  PASSENGER MANAGEMENT  (Employee-side)
    // ================================================================

    /**
     * Returns all non-deactivated passengers.
     */
    public List<Passenger> getAllActivePassengers() {
        return queryPassengers("SELECT * FROM vw_active_passengers");
    }

    /**
     * Suspends an Active passenger account.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String suspendPassenger(int passengerId, int doneByEmpId) {
        String sql = "UPDATE Passenger SET account_status = ? "
                   + "WHERE passenger_id = ? AND account_status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Passenger.STATUS_SUSPENDED);
            ps.setInt(2, passengerId);
            ps.setString(3, Passenger.STATUS_ACTIVE);

            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Passenger #" + passengerId + " suspended successfully.";
            return "ERROR:Passenger not found or already suspended/deactivated.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.suspendPassenger] " + e.getMessage());
            return "ERROR:Database error while suspending passenger.";
        }
    }

    /**
     * Restores a Suspended passenger account to Active.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String unsuspendPassenger(int passengerId) {
        String sql = "UPDATE Passenger SET account_status = ? "
                   + "WHERE passenger_id = ? AND account_status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Passenger.STATUS_ACTIVE);
            ps.setInt(2, passengerId);
            ps.setString(3, Passenger.STATUS_SUSPENDED);

            int rows = ps.executeUpdate();
            if (rows > 0) return "SUCCESS:Passenger #" + passengerId + " account restored to Active.";
            return "ERROR:Passenger not found or not currently suspended.";

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.unsuspendPassenger] " + e.getMessage());
            return "ERROR:Database error while restoring passenger.";
        }
    }

    /**
     * Soft-deactivates a passenger via {@code sp_deactivate_passenger}.
     * Blocked by the SP if the passenger has active confirmed bookings.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String deactivatePassenger(int passengerId, int doneByEmpId) {
        String sql = "{CALL sp_deactivate_passenger(?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, passengerId);
            cs.setInt(2, doneByEmpId);
            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                if (rs != null && rs.next())
                    return "SUCCESS:" + rs.getString("result_message");
            }
            return "SUCCESS:Passenger account deactivated.";

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("active confirmed bookings"))
                return "ERROR:Cannot deactivate — passenger has active confirmed bookings. Cancel them first.";
            System.err.println("[EmployeeDAO.deactivatePassenger] " + msg);
            return "ERROR:Database error while deactivating passenger.";
        }
    }

    /**
     * Searches passengers by name, passport, nationality, or username.
     */
    public List<Passenger> searchPassengers(String query) {
        String sql = "SELECT * FROM Passenger WHERE account_status != ? AND ("
                   + "LOWER(name)        LIKE ? OR "
                   + "LOWER(passport_no) LIKE ? OR "
                   + "LOWER(nationality) LIKE ? OR "
                   + "LOWER(username)    LIKE ?)";
        List<Passenger> list = new ArrayList<>();
        String q = "%" + query.toLowerCase().trim() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Passenger.STATUS_DEACTIVATED);
            ps.setString(2, q); ps.setString(3, q);
            ps.setString(4, q); ps.setString(5, q);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPassenger(rs));
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.searchPassengers] " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    //  DASHBOARD STATISTICS
    // ================================================================

    /**
     * Fetches all dashboard statistics via {@code sp_dashboard_stats()}.
     *
     * @return a {@link DashboardStats} instance; check {@link DashboardStats#success}
     */
    public DashboardStats getDashboardStats() {
        String sql = "{CALL sp_dashboard_stats()}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            if (rs.next()) {
                return new DashboardStats(
                        rs.getInt("total_flights"),
                        rs.getInt("scheduled_flights"),
                        rs.getInt("delayed_flights"),
                        rs.getInt("total_passengers"),
                        rs.getInt("total_bookings"),
                        rs.getInt("confirmed_bookings"),
                        rs.getInt("pending_bookings"),
                        rs.getInt("active_tickets"),
                        rs.getDouble("total_revenue"),
                        rs.getInt("pending_requests")
                );
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.getDashboardStats] " + e.getMessage());
            return DashboardStats.error("ERROR:Could not load dashboard statistics.");
        }
        return DashboardStats.error("ERROR:No statistics returned from database.");
    }

    // ================================================================
    //  DashboardStats 
    // ================================================================

    /**
     * Immutable snapshot of admin dashboard statistics.
     * Built by {@link EmployeeDAO#getDashboardStats()}.
     */
    public static final class DashboardStats {

        public final boolean success;
        public final String  errorMessage;
        public final int     totalFlights;
        public final int     scheduledFlights;
        public final int     delayedFlights;
        public final int     totalPassengers;
        public final int     totalBookings;
        public final int     confirmedBookings;
        public final int     pendingBookings;
        public final int     activeTickets;
        public final double  totalRevenue;
        public final int     pendingRequests;

        /** Full constructor for a successful result. */
        DashboardStats(int totalFlights, int scheduledFlights, int delayedFlights,
                       int totalPassengers, int totalBookings, int confirmedBookings,
                       int pendingBookings, int activeTickets, double totalRevenue,
                       int pendingRequests) {
            this.success          = true;
            this.errorMessage     = null;
            this.totalFlights     = totalFlights;
            this.scheduledFlights = scheduledFlights;
            this.delayedFlights   = delayedFlights;
            this.totalPassengers  = totalPassengers;
            this.totalBookings    = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.pendingBookings  = pendingBookings;
            this.activeTickets    = activeTickets;
            this.totalRevenue     = totalRevenue;
            this.pendingRequests  = pendingRequests;
        }

        /** Factory for a failed result. */
        static DashboardStats error(String message) {
            return new DashboardStats(message);
        }

        /** Private error constructor — all numeric fields zero, success = false. */
        private DashboardStats(String errorMessage) {
            this.success          = false;
            this.errorMessage     = errorMessage;
            this.totalFlights     = 0;
            this.scheduledFlights = 0;
            this.delayedFlights   = 0;
            this.totalPassengers  = 0;
            this.totalBookings    = 0;
            this.confirmedBookings= 0;
            this.pendingBookings  = 0;
            this.activeTickets    = 0;
            this.totalRevenue     = 0.0;
            this.pendingRequests  = 0;
        }

        @Override
        public String toString() {
            return success
                    ? String.format("DashboardStats[flights=%d bookings=%d revenue=%.2f]",
                                    totalFlights, totalBookings, totalRevenue)
                    : "DashboardStats[ERROR: " + errorMessage + "]";
        }
    }

    // ================================================================
    //  PRIVATE HELPERS – query wrappers
    // ================================================================

    /** Run a no-param employee query and map all rows. */
    private List<Employee> queryEmployees(String sql) {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapEmployee(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.queryEmployees] " + e.getMessage());
        }
        return list;
    }

    /** Run a 4-param LIKE search on employees. */
    private List<Employee> queryEmployeesWithParam(String sql, String query) {
        List<Employee> list = new ArrayList<>();
        String q = "%" + query.toLowerCase().trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q);
            ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.searchEmployees] " + e.getMessage());
        }
        return list;
    }

    /** Run a no-param flight query and map all rows. */
    private List<Flight> queryFlights(String sql) {
        List<Flight> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapFlight(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.queryFlights] " + e.getMessage());
        }
        return list;
    }

    /** Run a 4-param LIKE search on flights. */
    private List<Flight> queryFlightsWithParam(String sql, String query) {
        List<Flight> list = new ArrayList<>();
        String q = "%" + query.toLowerCase().trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q);
            ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFlight(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.searchFlights] " + e.getMessage());
        }
        return list;
    }

    /** Run a no-param booking query and map all rows. */
    private List<Booking> queryBookings(String sql) {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapBooking(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.queryBookings] " + e.getMessage());
        }
        return list;
    }

    /** Run a 4-param LIKE search on bookings. */
    private List<Booking> queryBookingsWithParam(String sql, String query) {
        List<Booking> list = new ArrayList<>();
        String q = "%" + query.toLowerCase().trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q);
            ps.setString(3, q); ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.searchBookings] " + e.getMessage());
        }
        return list;
    }

    /** Run a no-param passenger query and map all rows. */
    private List<Passenger> queryPassengers(String sql) {
        List<Passenger> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapPassenger(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.queryPassengers] " + e.getMessage());
        }
        return list;
    }

    /** Run a no-param request query and map all rows. */
    private List<PassengerRequest> queryRequests(String sql) {
        List<PassengerRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRequest(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.queryRequests] " + e.getMessage());
        }
        return list;
    }

    /**
     * Shared logic for approveRequest / rejectRequest.
     * Guards against acting on an already-reviewed request.
     */
    private String reviewRequest(int requestId, int reviewedByEmpId,
                                 String newStatus, String successMessage) {
        String checkSql = "SELECT request_status FROM Passenger_Requests WHERE request_id = ?";
        String updateSql = "UPDATE Passenger_Requests SET request_status = ?, "
                         + "reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP "
                         + "WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, requestId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next())
                        return "ERROR:Request #" + requestId + " not found.";
                    String current = rs.getString("request_status");
                    // DB stores "Pending" (title-case) — compare directly
                    if (!"Pending".equals(current))
                        return "ERROR:Request #" + requestId + " is already " + current + ".";
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus);
                ps.setInt(2, reviewedByEmpId);
                ps.setInt(3, requestId);
                int rows = ps.executeUpdate();
                if (rows > 0) return "SUCCESS:" + successMessage;
                return "ERROR:Failed to update request #" + requestId + ".";
            }

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO.reviewRequest] " + e.getMessage());
            return "ERROR:Database error while processing request #" + requestId + ".";
        }
    }

    // ================================================================
    //  PRIVATE MAPPERS  (ResultSet → Model objects)
    // ================================================================

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("employee_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("role"),
                rs.getString("username"),
                null,                            // password never loaded into model
                rs.getString("account_status"),
                rs.getTimestamp("created_at")
        );
    }

    private Flight mapFlight(ResultSet rs) throws SQLException {
        return new Flight(
                rs.getInt("flight_id"),
                rs.getString("flight_number"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getTimestamp("departure_time"),
                rs.getTimestamp("arrival_time"),
                rs.getInt("total_seats"),
                rs.getInt("available_seats"),
                rs.getDouble("price_per_seat"),
                rs.getString("flight_status"),
                safeInt(rs, "managed_by")
        );
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        return new Booking(
                safeInt(rs, "booking_id"),
                safeInt(rs, "passenger_id"),
                safeInt(rs, "flight_id"),
                safeTimestamp(rs, "booked_at"),
                safeInt(rs, "seat_count"),
                rs.getDouble("total_price"),
                safeStr(rs, "booking_status"),
                safeInt(rs, "processed_by")
        );
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
                safeInt(rs, "payment_id"),
                safeInt(rs, "booking_id"),
                rs.getDouble("amount"),
                safeTimestamp(rs, "paid_at"),
                safeStr(rs, "payment_method"),
                safeStr(rs, "payment_status"),
                safeInt(rs, "processed_by")
        );
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        return new Ticket(
                safeInt(rs, "ticket_id"),
                safeInt(rs, "booking_id"),
                safeInt(rs, "passenger_id"),
                safeInt(rs, "flight_id"),
                rs.getString("seat_number"),
                safeTimestamp(rs, "issued_at"),
                safeStr(rs, "ticket_status")
        );
    }

    private Passenger mapPassenger(ResultSet rs) throws SQLException {
        return new Passenger(
                rs.getInt("passenger_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("passport_no"),
                rs.getString("nationality"),
                safeDate(rs, "date_of_birth"),
                rs.getString("username"),
                null,                            // password never loaded into model
                rs.getString("account_status"),
                rs.getTimestamp("created_at")
        );
    }

    private PassengerRequest mapRequest(ResultSet rs) throws SQLException {
        return new PassengerRequest(
                safeInt(rs, "request_id"),
                safeStr(rs, "name"),
                safeStr(rs, "email"),
                safeStr(rs, "phone"),
                safeStr(rs, "passport_no"),
                safeStr(rs, "nationality"),
                safeDate(rs, "date_of_birth"),
                safeStr(rs, "username"),
                null,                            // password not returned by view
                safeTimestamp(rs, "submitted_at"),
                safeStr(rs, "request_status"),
                safeInt(rs, "reviewed_by")
        );
    }

    // ================================================================
    //  PRIVATE SAFE GETTERS  (handle missing / null view columns)
    // ================================================================

    /**
     * Reads an int column; returns 0 if the column is absent or the value
     * is non-numeric (e.g. a view joins a non-numeric string into that slot).
     */
    private int safeInt(ResultSet rs, String col) {
        try {
            String val = rs.getString(col);
            if (val == null || val.isBlank()) return 0;
            return Integer.parseInt(val.trim());
        } catch (SQLException | NumberFormatException e) {
            return 0;
        }
    }

    private String safeStr(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return null; }
    }

    private Timestamp safeTimestamp(ResultSet rs, String col) {
        try { return rs.getTimestamp(col); } catch (SQLException e) { return null; }
    }

    private java.sql.Date safeDate(ResultSet rs, String col) {
        try { return rs.getDate(col); } catch (SQLException e) { return null; }
    }
}
