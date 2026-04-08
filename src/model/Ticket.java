package model;

import java.sql.Timestamp;


public class Ticket {

    // ── Status constants ───────────────────────────────────────────────
    public static final String STATUS_ACTIVE    = "Active";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_USED      = "Used";

    // ── Fields ────────────────────────────────────────────────────────
    private int       ticketId;
    private int       bookingId;    // FK — must match a Confirmed Booking
    private int       passengerId;
    private int       flightId;
    private String    seatNumber;   // format: row+letter, e.g. "12A", "5B"
    private Timestamp issuedDate;
    private String    status;       // see STATUS_* constants

    // ── Full constructor — used by DAO mappers ────────────────────────
    public Ticket(int ticketId, int bookingId, int passengerId,
                  int flightId, String seatNumber,
                  Timestamp issuedDate, String status) {
        this.ticketId    = ticketId;
        this.bookingId   = bookingId;
        this.passengerId = passengerId;
        this.flightId    = flightId;
        this.seatNumber  = seatNumber;
        this.issuedDate  = issuedDate;
        this.status      = (status != null) ? status : STATUS_ACTIVE;
    }

    /**
     * Constructor for generating a new ticket before it is saved.
     * ticketId and issuedDate are assigned by the DB on INSERT.
     */
    public Ticket(int bookingId, int passengerId, int flightId, String seatNumber) {
        this(0, bookingId, passengerId, flightId, seatNumber, null, STATUS_ACTIVE);
    }

    // ── Getters ───────────────────────────────────────────────────────
    public int       getTicketId()    { return ticketId; }
    public int       getBookingId()   { return bookingId; }
    public int       getPassengerId() { return passengerId; }
    public int       getFlightId()    { return flightId; }
    public String    getSeatNumber()  { return seatNumber; }
    public Timestamp getIssuedDate()  { return issuedDate; }
    public String    getStatus()      { return status; }

    // ── Setters ───────────────────────────────────────────────────────
    public void setTicketId(int id)       { this.ticketId = id; }
    public void setIssuedDate(Timestamp t){ this.issuedDate = t; }

    // ── Status Helpers ─────────────────────────────────────────────────
    public boolean isActive()    { return STATUS_ACTIVE.equals(status); }
    public boolean isCancelled() { return STATUS_CANCELLED.equals(status); }
    public boolean isUsed()      { return STATUS_USED.equals(status); }

    /** True when the ticket can still be cancelled (only Active tickets). */
    public boolean isCancellable() { return isActive(); }

    // ── Business Methods ───────────────────────────────────────────────

    /**
     * Cancels this ticket in memory (Active → Cancelled).
     *
     * ⚠ Does NOT write to the DB. Call EmployeeService.cancelTicket() first.
     */
    public void cancel() {
        if (!isCancellable())
            throw new IllegalStateException(
                "Only Active tickets can be cancelled. Current: " + status);
        this.status = STATUS_CANCELLED;
    }

    /**
     * Marks this ticket as used after the passenger boards (Active → Used).
     * Future feature — DB update still needed via DAO.
     *
     * ⚠ Does NOT write to the DB.
     */
    public void markAsUsed() {
        if (!isActive())
            throw new IllegalStateException(
                "Only Active tickets can be marked as used. Current: " + status);
        this.status = STATUS_USED;
    }

    
    public String generateTicketSummary() {
        return String.format(
            "╔══════════════════════╗%n" +
            "║   AEROSYS BOARDING   ║%n" +
            "╠══════════════════════╣%n" +
            "║ Ticket   : %-10d║%n" +
            "║ Booking  : %-10d║%n" +
            "║ Seat     : %-10s║%n" +
            "║ Issued   : %-10s║%n" +
            "║ Status   : %-10s║%n" +
            "╚══════════════════════╝",
            ticketId, bookingId, seatNumber,
            issuedDate != null ? issuedDate.toString().substring(0, 10) : "pending",
            status);
    }

    // ── toString ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Ticket[id=%d, booking=%d, passenger=%d, flight=%d, seat=%s, status=%s]",
            ticketId, bookingId, passengerId, flightId, seatNumber, status);
    }
}
