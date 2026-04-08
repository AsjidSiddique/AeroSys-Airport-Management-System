package model;

import java.sql.Timestamp;

public class Booking {

    // ── Status constants ───────────────────────────────────────────────
    public static final String STATUS_PENDING   = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CANCELLED = "Cancelled";

    // ── Fields ─────────────────────────────────────────────────────────
    private int       bookingId;
    private int       passengerId;
    private int       flightId;
    private Timestamp bookingDate;
    private int       seatCount;
    private double    totalPrice;
    private String    status;        // see STATUS_* constants above
    private int       processedBy;   // employee_id (0 = not yet processed by staff)

    // ── Full constructor — used by DAO mappers ────────────────────────
    public Booking(int bookingId, int passengerId, int flightId,
                   Timestamp bookingDate, int seatCount, double totalPrice,
                   String status, int processedBy) {
        this.bookingId   = bookingId;
        this.passengerId = passengerId;
        this.flightId    = flightId;
        this.bookingDate = bookingDate;
        this.seatCount   = seatCount;
        this.totalPrice  = totalPrice;
        this.status      = (status != null) ? status : STATUS_PENDING;
        this.processedBy = processedBy;
    }

    /**
     * Constructor for creating a brand-new booking before it is saved.
     * bookingId and bookingDate are assigned by the DB on INSERT.
     */
    public Booking(int passengerId, int flightId, int seatCount, double totalPrice) {
        this(0, passengerId, flightId, null, seatCount, totalPrice, STATUS_PENDING, 0);
    }

    // ── Getters ────────────────────────────────────────────────────────
    public int       getBookingId()   { return bookingId; }
    public int       getPassengerId() { return passengerId; }
    public int       getFlightId()    { return flightId; }
    public Timestamp getBookingDate() { return bookingDate; }
    public int       getSeatCount()   { return seatCount; }
    public double    getTotalPrice()  { return totalPrice; }
    public String    getStatus()      { return status; }
    public int       getProcessedBy() { return processedBy; }

    // ── Setters ────────────────────────────────────────────────────────
    public void setBookingId(int id)         { this.bookingId = id; }
    public void setBookingDate(Timestamp ts) { this.bookingDate = ts; }
    public void setProcessedBy(int empId)    { this.processedBy = empId; }

    /** Updates status; accepts only the three defined lifecycle values. */
    public void setStatus(String status) {
        if (!STATUS_PENDING.equals(status)
         && !STATUS_CONFIRMED.equals(status)
         && !STATUS_CANCELLED.equals(status))
            throw new IllegalArgumentException(
                "Invalid booking status: " + status);
        this.status = status;
    }

    // ── Status Helpers ─────────────────────────────────────────────────
    public boolean isPending()     { return STATUS_PENDING.equals(status); }
    public boolean isConfirmed()   { return STATUS_CONFIRMED.equals(status); }
    public boolean isCancelled()   { return STATUS_CANCELLED.equals(status); }

    /** A booking can be cancelled as long as it is not already cancelled. */
    public boolean isCancellable() { return !isCancelled(); }

    // ── Business Methods ───────────────────────────────────────────────

    /**
     * Confirms this booking in memory (Pending → Confirmed).
     *
     * ⚠ Does NOT write to the DB — call EmployeeService.confirmBooking() first,
     *   then call this to keep the object consistent with the DB state.
     */
    public void confirm(int employeeId) {
        if (!isPending())
            throw new IllegalStateException(
                "Only Pending bookings can be confirmed. Current status: " + status);
        this.status      = STATUS_CONFIRMED;
        this.processedBy = employeeId;
    }

    /**
     * Cancels this booking in memory (Pending|Confirmed → Cancelled).
     *
     * ⚠ Does NOT write to the DB — call BookingDAO.cancelBooking() or
     *   EmployeeService.cancelBooking() first, then call this method.
     */
    public void cancel() {
        if (!isCancellable())
            throw new IllegalStateException(
                "Booking is already cancelled (id=" + bookingId + ").");
        this.status = STATUS_CANCELLED;
    }

    // ── toString ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Booking[id=%d, passenger=%d, flight=%d, seats=%d, total=%.2f, status=%s]",
            bookingId, passengerId, flightId, seatCount, totalPrice, status);
    }
}
