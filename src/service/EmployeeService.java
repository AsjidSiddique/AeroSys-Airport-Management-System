package service;

import dao.EmployeeDAO;
import dao.EmployeeDAO.DashboardStats;
import model.*;

import java.util.List;

/** 
 * EmployeeService – Business logic layer for all employee-side operations.

 * Flow:
 *   AeroSysAdminUI  →  AirportSystem.getEmployeeService()  →  EmployeeService  →  EmployeeDAO
 */
public class EmployeeService {

    private final EmployeeDAO dao = new EmployeeDAO();

    public EmployeeService() {}

       private Employee getEmployee() {
        return SessionManager.getInstance().requireEmployee();
    }

    // ================================================================
    //  EMPLOYEE ACCOUNT MANAGEMENT  (Admin only)
    // ================================================================

    /**
     * Adds a new employee. Only ADMIN may call this.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String addEmployee(String name,     String email,
                              String phone,    String role,
                              String username, String password,
                              String confirmPassword) {
        Employee me = getEmployee();
        if (!me.isAdmin())
            return "ERROR:Only ADMIN can add new employees.";

        // ── Presence checks ───────────────────────────────────────────
        if (isBlank(name))     return "ERROR:Employee name is required.";
        if (isBlank(email))    return "ERROR:Email address is required.";
        if (isBlank(phone))    return "ERROR:Phone number is required.";
        if (isBlank(username)) return "ERROR:Username is required.";
        if (isBlank(password)) return "ERROR:Password is required.";
        if (isBlank(role))     return "ERROR:Role is required.";

        // ── Format / business rules ───────────────────────────────────
        if (name.trim().length() < 3)
            return "ERROR:Name must be at least 3 characters.";
        if (!InputValidator.isValidEmail(email))
            return "ERROR:Please enter a valid email address.";
        if (!InputValidator.isValidPhone(phone))
            return "ERROR:Phone must be 10–15 digits only.";
        if (!InputValidator.isValidUsername(username))
            return "ERROR:Username must be 4–20 characters: letters, numbers, underscore only.";
        if (password.length() < 6)
            return "ERROR:Password must be at least 6 characters.";
        if (!password.equals(confirmPassword))
            return "ERROR:Passwords do not match.";
        if (!Employee.ROLE_ADMIN.equals(role) && !Employee.ROLE_EMPLOYEE.equals(role))
            return "ERROR:Role must be ADMIN or EMPLOYEE.";

        return dao.addEmployee(name.trim(), email.trim().toLowerCase(),
                               phone.trim(), role, username.trim(), password);
    }

    /**
     * Deactivates an employee account. Only ADMIN; cannot self-deactivate.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String deactivateEmployee(int targetEmployeeId) {
        Employee me = getEmployee();
        if (!me.isAdmin())
            return "ERROR:Only ADMIN can deactivate employee accounts.";
        if (targetEmployeeId == me.getEmployeeId())
            return "ERROR:You cannot deactivate your own account.";
        if (targetEmployeeId <= 0)
            return "ERROR:Invalid employee ID.";
        return dao.deactivateEmployee(targetEmployeeId, me.getEmployeeId());
    }

    /**
     * Reactivates a deactivated employee account. Only ADMIN.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String reactivateEmployee(int targetEmployeeId) {
        if (!getEmployee().isAdmin())
            return "ERROR:Only ADMIN can reactivate employee accounts.";
        if (targetEmployeeId <= 0)
            return "ERROR:Invalid employee ID.";
        return dao.reactivateEmployee(targetEmployeeId);
    }

    /**
     * Returns all employees. Admin sees everyone; Employee sees active only.
     */
    public List<Employee> getAllEmployees() {
        return getEmployee().isAdmin()
                ? dao.getAllEmployees()
                : dao.getAllActiveEmployees();
    }

    /**
     * Searches employees. Falls back to getAllEmployees() on blank query.
     */
    public List<Employee> searchEmployees(String query) {
        if (isBlank(query)) return getAllEmployees();
        return dao.searchEmployees(query);
    }

    // ================================================================
    //  FLIGHT MANAGEMENT
    // ================================================================

    /**
     * Adds a new flight.
     * {@code totalSeatsStr} and {@code priceStr} are parsed and validated here.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String addFlight(String flightNumber, String origin,      String destination,
                            String departureTime, String arrivalTime,
                            String totalSeatsStr, String priceStr,
                            String flightStatus) {
        if (isBlank(flightNumber))  return "ERROR:Flight number is required.";
        if (isBlank(origin))        return "ERROR:Origin city is required.";
        if (isBlank(destination))   return "ERROR:Destination city is required.";
        if (isBlank(departureTime)) return "ERROR:Departure time is required.";
        if (isBlank(arrivalTime))   return "ERROR:Arrival time is required.";
        if (isBlank(totalSeatsStr)) return "ERROR:Total seats is required.";
        if (isBlank(priceStr))      return "ERROR:Price per seat is required.";

        if (origin.trim().equalsIgnoreCase(destination.trim()))
            return "ERROR:Origin and destination cannot be the same city.";

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(totalSeatsStr.trim());
            if (totalSeats < 1) return "ERROR:Total seats must be at least 1.";
        } catch (NumberFormatException e) {
            return "ERROR:Total seats must be a whole number.";
        }

        double pricePerSeat;
        try {
            pricePerSeat = Double.parseDouble(priceStr.trim());
            if (pricePerSeat <= 0) return "ERROR:Price must be greater than zero.";
        } catch (NumberFormatException e) {
            return "ERROR:Price must be a valid number (e.g. 299.99).";
        }

        if (isBlank(flightStatus)) flightStatus = Flight.STATUS_SCHEDULED;

        return dao.addFlight(flightNumber, origin, destination,
                             departureTime, arrivalTime,
                             totalSeats, pricePerSeat,
                             flightStatus, getEmployee().getEmployeeId());
    }

    /**
     * Updates an existing flight's details.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String updateFlight(int flightId,      String origin,      String destination,
                               String departureTime, String arrivalTime,
                               String totalSeatsStr, String priceStr,
                               String flightStatus) {
        if (flightId <= 0)           return "ERROR:Invalid flight ID.";
        if (isBlank(origin))         return "ERROR:Origin is required.";
        if (isBlank(destination))    return "ERROR:Destination is required.";
        if (isBlank(departureTime))  return "ERROR:Departure time is required.";
        if (isBlank(arrivalTime))    return "ERROR:Arrival time is required.";

        if (origin.trim().equalsIgnoreCase(destination.trim()))
            return "ERROR:Origin and destination cannot be the same city.";

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(totalSeatsStr.trim());
            if (totalSeats < 1) return "ERROR:Total seats must be at least 1.";
        } catch (NumberFormatException e) {
            return "ERROR:Total seats must be a whole number.";
        }

        double pricePerSeat;
        try {
            pricePerSeat = Double.parseDouble(priceStr.trim());
            if (pricePerSeat <= 0) return "ERROR:Price must be greater than zero.";
        } catch (NumberFormatException e) {
            return "ERROR:Price must be a valid decimal number.";
        }

        return dao.updateFlight(flightId, origin, destination,
                                departureTime, arrivalTime,
                                totalSeats, pricePerSeat,
                                flightStatus, getEmployee().getEmployeeId());
    }

    /**
     * Soft-cancels a flight via the stored procedure.
     * Defaults reason to the acting employee's name if blank.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelFlight(int flightId, String reason) {
        if (flightId <= 0) return "ERROR:Invalid flight ID.";
        Employee me = getEmployee();
        if (isBlank(reason)) reason = "Cancelled by " + me.getName();
        return dao.cancelFlight(flightId, me.getEmployeeId(), reason);
    }

    /**
     * Updates flight status only (Scheduled / Delayed / Completed).
     * Blocks direct status → Cancelled to enforce sp_cancel_flight path.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String updateFlightStatus(int flightId, String newStatus) {
        if (flightId <= 0)   return "ERROR:Invalid flight ID.";
        if (isBlank(newStatus)) return "ERROR:New status is required.";
        if (Flight.STATUS_CANCELLED.equals(newStatus))
            return "ERROR:Use the Cancel Flight action to cancel a flight properly.";
        List<String> valid = List.of(
                Flight.STATUS_SCHEDULED, Flight.STATUS_DELAYED,
                Flight.STATUS_COMPLETED, Flight.STATUS_CANCELLED);
        if (!valid.contains(newStatus))
            return "ERROR:Status must be one of: Scheduled, Delayed, Completed.";
        return dao.updateFlightStatus(flightId, newStatus);
    }

    /**
     * Returns all non-cancelled flights.
     */
    public List<Flight> getAllFlights() { return dao.getAllFlights(); }

    /**
     * Searches flights. Falls back to getAllFlights() on blank query.
     */
    public List<Flight> searchFlights(String query) {
        if (isBlank(query)) return dao.getAllFlights();
        return dao.searchFlights(query);
    }

    // ================================================================
    //  BOOKING MANAGEMENT
    // ================================================================

    /**
     * Confirms a pending booking. Records the acting employee.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String confirmBooking(int bookingId) {
        if (bookingId <= 0) return "ERROR:Invalid booking ID.";
        return dao.confirmBooking(bookingId, getEmployee().getEmployeeId());
    }

    /**
     * Cancels a booking. DB trigger T3 restores seats and cancels tickets.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelBooking(int bookingId) {
        if (bookingId <= 0) return "ERROR:Invalid booking ID.";
        return dao.cancelBooking(bookingId);
    }

    /**
     * Returns all bookings.
     */
    public List<Booking> getAllBookings() { return dao.getAllBookings(); }

    /**
     * Searches bookings. Falls back to getAllBookings() on blank query.
     */
    public List<Booking> searchBookings(String query) {
        if (isBlank(query)) return dao.getAllBookings();
        return dao.searchBookings(query);
    }

    // ================================================================
    //  TICKET MANAGEMENT
    // ================================================================

    /**
     * Generates a ticket for a confirmed + paid booking.
     * Seat number format: digits followed by one letter, e.g. "12A".
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String generateTicket(int bookingId, int passengerId,
                                 int flightId,  String seatNumber) {
        if (bookingId <= 0)     return "ERROR:Invalid booking ID.";
        if (passengerId <= 0)   return "ERROR:Invalid passenger ID.";
        if (flightId <= 0)      return "ERROR:Invalid flight ID.";
        if (isBlank(seatNumber)) return "ERROR:Seat number is required.";
        if (!seatNumber.trim().matches("[0-9]{1,3}[A-Za-z]"))
            return "ERROR:Seat number format must be like 12A, 5B, 22C.";
        return dao.generateTicket(bookingId, passengerId, flightId, seatNumber.trim().toUpperCase());
    }

    /**
     * Cancels an active ticket.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelTicket(int ticketId) {
        if (ticketId <= 0) return "ERROR:Invalid ticket ID.";
        return dao.cancelTicket(ticketId);
    }
    /**
 * Returns how many non-cancelled tickets have already been issued
 * for the given booking. Returns -1 on error.
 * Used by the UI to calculate remaining seats needing tickets.
 */
public int countIssuedTickets(int bookingId) {
    if (bookingId <= 0) return -1;
    return dao.countIssuedTickets(bookingId);
}

    /**
     * Returns all tickets.
     */
    public List<Ticket> getAllTickets() { return dao.getAllTickets(); }

    // ================================================================
    //  PAYMENT MANAGEMENT
    // ================================================================

    /**
     * Records (or updates) a payment as Paid, with amount and method.
     * The acting employee is recorded on the payment row.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String recordPayment(int bookingId, String amountStr, String paymentMethod) {
        if (bookingId <= 0)        return "ERROR:Invalid booking ID.";
        if (isBlank(amountStr))    return "ERROR:Amount is required.";
        if (isBlank(paymentMethod)) return "ERROR:Payment method is required.";
        if (!InputValidator.isValidPaymentMethod(paymentMethod))
            return "ERROR:Payment method must be: Credit Card, Debit Card, Cash, or Online.";

        double amount;
        try {
            amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) return "ERROR:Amount must be greater than zero.";
        } catch (NumberFormatException e) {
            return "ERROR:Amount must be a valid number (e.g. 299.99).";
        }

        return dao.recordPayment(bookingId, amount, paymentMethod,
                                 getEmployee().getEmployeeId());
    }


    /**
     * Returns all payments.
     */
    public List<Payment> getAllPayments() { return dao.getAllPayments(); }

    /**
     * Searches payments. Falls back to getAllPayments() on blank query.
     */
    public List<Payment> searchPayments(String query) {
        if (isBlank(query)) return dao.getAllPayments();
        return dao.searchPayments(query);
    }

    // ================================================================
    //  PASSENGER REQUEST MANAGEMENT
    // ================================================================

    /**
     * Returns all Pending registration requests.
     */
    public List<PassengerRequest> getPendingRequests() { return dao.getPendingRequests(); }

    /**
     * Returns all registration requests regardless of status.
     */
    public List<PassengerRequest> getAllRequests() { return dao.getAllRequests(); }

    /**
     * Approves a Pending registration request.
     * DB trigger T6 auto-creates the Passenger account.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String approveRequest(int requestId) {
        if (requestId <= 0) return "ERROR:Invalid request ID.";
        return dao.approveRequest(requestId, getEmployee().getEmployeeId());
    }

    /**
     * Rejects a Pending registration request.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String rejectRequest(int requestId) {
        if (requestId <= 0) return "ERROR:Invalid request ID.";
        return dao.rejectRequest(requestId, getEmployee().getEmployeeId());
    }

    // ================================================================
    //  PASSENGER MANAGEMENT
    // ================================================================

    /**
     * Returns all non-deactivated passengers.
     */
    public List<Passenger> getAllActivePassengers() { return dao.getAllActivePassengers(); }

    /**
     * Searches passengers. Falls back to getAllActivePassengers() on blank query.
     */
    public List<Passenger> searchPassengers(String query) {
        if (isBlank(query)) return dao.getAllActivePassengers();
        return dao.searchPassengers(query);
    }

    /**
     * Suspends an Active passenger account.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String suspendPassenger(int passengerId) {
        if (passengerId <= 0) return "ERROR:Invalid passenger ID.";
        return dao.suspendPassenger(passengerId, getEmployee().getEmployeeId());
    }

    /**
     * Restores a Suspended passenger account to Active.
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String unsuspendPassenger(int passengerId) {
        if (passengerId <= 0) return "ERROR:Invalid passenger ID.";
        return dao.unsuspendPassenger(passengerId);
    }

    /**
     * Permanently deactivates a passenger.
     * Blocked if the passenger has active confirmed bookings (enforced by SP).
     *
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String deactivatePassenger(int passengerId) {
        if (passengerId <= 0) return "ERROR:Invalid passenger ID.";
        return dao.deactivatePassenger(passengerId, getEmployee().getEmployeeId());
    }

    // ================================================================
    //  DASHBOARD STATISTICS
    // ================================================================

    /**
     * Fetches all dashboard statistics via {@code sp_dashboard_stats()}.
     * Check {@link DashboardStats#success} before reading field values.
     */
    public DashboardStats getDashboardStats() {
        return dao.getDashboardStats();
    }

    // ================================================================
    //  PRIVATE HELPERS
    // ================================================================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
