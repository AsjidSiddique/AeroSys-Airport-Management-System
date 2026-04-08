package service;

import dao.BookingDAO;
import dao.PaymentDAO;
import model.Booking;
import model.InputValidator;
import model.Payment;
import model.SessionManager;


public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final TicketService ticketService = new TicketService();

    
    public String processPayment(int bookingId, String paymentMethod, int processedBy) {
        if (bookingId <= 0)       return "ERROR:Invalid booking ID.";
        if (isBlank(paymentMethod)) return "ERROR:Payment method is required.";
        if (!InputValidator.isValidPaymentMethod(paymentMethod))
            return "ERROR:Payment method must be: Credit Card, Debit Card, Cash, or Online.";

        // ── Booking state check ───────────────────────────────────────
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null)
            return "ERROR:Booking #" + bookingId + " not found.";
        if (booking.isCancelled())
            return "ERROR:Cannot pay for a cancelled booking.";

        // ── Ownership check ───────────────────────────────────────────────
        // processedBy == 0 means passenger self-service (from passenger portal).
        // processedBy  > 0 means an employee is processing it at the counter.
        // In both cases validate the booking — ownership guard only for passengers.
        if (processedBy <= 0) {
            try {
                int passengerId = SessionManager.getInstance().requirePassenger().getPassengerId();
                if (booking.getPassengerId() != passengerId)
                    return "ERROR:You can only pay for your own bookings.";
            } catch (IllegalStateException e) {
                // No passenger session — check if employee session is active instead
                if (SessionManager.getInstance().asEmployee() == null)
                    return "ERROR:No active session. Please log in again.";
            }
        }

        // ── Mark payment as Paid ──────────────────────────────────────
        boolean paid = paymentDAO.markAsPaid(bookingId, paymentMethod,
                processedBy > 0 ? processedBy : 0);
        if (!paid)
            return "ERROR:Payment could not be processed. Please try again.";

        // ── Auto-generate tickets ─────────────────────────────────────
        String ticketResult = ticketService.generateTicketsForBooking(booking);
        if (ticketResult.startsWith("ERROR:")) {
            // Payment succeeded but tickets failed — log for admin investigation
            System.err.println("[PaymentService] Tickets not generated after payment for booking #"
                    + bookingId + ": " + ticketResult);
            return "SUCCESS:Payment processed for booking #" + bookingId
                    + ". Note: Ticket generation encountered an issue — "
                    + "please contact support to issue your tickets.";
        }

        return "SUCCESS:Payment processed for booking #" + bookingId
                + ". " + ticketResult;
    }

    // ================================================================
    //  READ
    // ================================================================

    /**
     * Returns the payment record for a booking.
     *
     * @return the {@link Payment}, or {@code null} if no payment exists yet
     */
    public Payment getPaymentByBookingId(int bookingId) {
        if (bookingId <= 0) return null;
        return paymentDAO.getPaymentByBookingId(bookingId);
    }

    // ================================================================
    //  PRIVATE HELPERS
    // ================================================================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
