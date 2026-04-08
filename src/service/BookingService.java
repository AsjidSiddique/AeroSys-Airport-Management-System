package service;

import dao.BookingDAO;
import dao.FlightDAO;
import dao.PaymentDAO;
import model.Booking;
import model.Flight;
import model.Payment;
import model.SessionManager;

import java.util.List;


public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final FlightDAO  flightDAO  = new FlightDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

       public String createBooking(int flightId, int seatCount) {
        // ── Session check ─────────────────────────────────────────────
        int passengerId;
        try {
            passengerId = SessionManager.getInstance().requirePassenger().getPassengerId();
        } catch (IllegalStateException e) {
            return "ERROR:No active passenger session. Please log in again.";
        }

        // ── Basic input guards ────────────────────────────────────────
        if (flightId <= 0)  return "ERROR:Invalid flight selected.";
        if (seatCount < 1)  return "ERROR:You must book at least 1 seat.";
        if (seatCount > 10) return "ERROR:Maximum 10 seats per booking.";

        // ── Flight availability check ─────────────────────────────────
        Flight flight = flightDAO.getFlightById(flightId);
        if (flight == null)
            return "ERROR:Flight not found. It may have been cancelled.";
        if (!flight.isBookable())
            return "ERROR:This flight is not available for booking (status: "
                    + flight.getStatus() + ").";
        if (!flight.hasAvailableSeats(seatCount))
            return "ERROR:Not enough seats available. Only "
                    + flight.getAvailableSeats() + " seat(s) remaining.";

        // ── Create booking ────────────────────────────────────────────
        double totalPrice = flight.calculateTotalPrice(seatCount);
        Booking booking   = bookingDAO.createBooking(passengerId, flightId, seatCount, totalPrice);

        if (booking == null)
            return "ERROR:Booking could not be created. Please try again.";

        // ── Auto-create Pending payment record ────────────────────────
        // paymentMethod is null here — the method is chosen when the passenger/employee pays.
        // PaymentDAO.createPayment() handles null by storing a placeholder that markAsPaid() overwrites.
        Payment payment = paymentDAO.createPayment(booking.getBookingId(), totalPrice, null);
        if (payment == null) {
            // Non-fatal: booking exists, payment row will be created when employee processes it
            System.err.println("[BookingService] Warning: payment record not created for booking #"
                    + booking.getBookingId());
        }

        return "SUCCESS:Booking #" + booking.getBookingId() + " created successfully. "
                + "Total: $" + String.format("%.2f", totalPrice)
                + ". Please visit the counter for payment and ticket issuance.";
    }

    // ================================================================
    //  READ
    // ================================================================

    /**
     * Returns all bookings for the currently logged-in passenger,
     * joined with their flight details.
     *
     * @return list of Object[] rows — see {@link dao.BookingDAO#getPassengerBookingsWithDetails}
     */
    public List<Object[]> getMyBookings() {
        int passengerId;
        try {
            passengerId = SessionManager.getInstance().requirePassenger().getPassengerId();
        } catch (IllegalStateException e) {
            return List.of();
        }
        return bookingDAO.getPassengerBookingsWithDetails(passengerId);
    }

    /**
     * Returns a single booking by ID — used before cancel to verify ownership.
     *
     * @return the {@link Booking}, or {@code null} if not found
     */
    public Booking getBookingById(int bookingId) {
        return bookingDAO.getBookingById(bookingId);
    }

    // ================================================================
    //  CANCEL
    // ================================================================

    /**
     * Cancels a booking owned by the currently logged-in passenger.
     *
     * <p>Ownership is verified before cancellation — a passenger cannot
     * cancel another passenger's booking.
     *
     * @param bookingId booking to cancel
     * @return {@code "SUCCESS:..."} or {@code "ERROR:..."}
     */
    public String cancelBooking(int bookingId) {
        if (bookingId <= 0) return "ERROR:Invalid booking ID.";

        // ── Session check ─────────────────────────────────────────────
        int passengerId;
        try {
            passengerId = SessionManager.getInstance().requirePassenger().getPassengerId();
        } catch (IllegalStateException e) {
            return "ERROR:No active passenger session. Please log in again.";
        }

        // ── Ownership & state guard ───────────────────────────────────
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null)
            return "ERROR:Booking #" + bookingId + " not found.";
        if (booking.getPassengerId() != passengerId)
            return "ERROR:You can only cancel your own bookings.";
        if (booking.isCancelled())
            return "ERROR:Booking #" + bookingId + " is already cancelled.";

        boolean ok = bookingDAO.cancelBooking(bookingId);
        return ok
                ? "SUCCESS:Booking #" + bookingId + " cancelled. Seats have been released."
                : "ERROR:Booking could not be cancelled. Please try again.";
    }
}
