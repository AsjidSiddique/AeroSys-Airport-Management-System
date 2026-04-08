package model;

import java.sql.Timestamp;


public class Payment {

    // ── Status constants ───────────────────────────────────────────────
    public static final String STATUS_PENDING  = "Pending";
    public static final String STATUS_PAID     = "Paid";
    public static final String STATUS_REFUNDED = "Refunded";

    // ── Fields ─────────────────────────────────────────────────────────
    private int       paymentId;
    private int       bookingId;       // FK – one-to-one with Booking
    private double    amount;
    private Timestamp paymentDate;
    private String    paymentMethod;   // Credit Card | Debit Card | Cash | Online
    private String    status;          // see STATUS_* constants
    private int       processedBy;     // employee_id (0 = self-service / not yet set)

    // ── Full constructor — used by DAO mappers ────────────────────────
    public Payment(int paymentId, int bookingId, double amount,
                   Timestamp paymentDate, String paymentMethod,
                   String status, int processedBy) {
        this.paymentId     = paymentId;
        this.bookingId     = bookingId;
        this.amount        = amount;
        this.paymentDate   = paymentDate;
        this.paymentMethod = paymentMethod;
        this.status        = (status != null) ? status : STATUS_PENDING;
        this.processedBy   = processedBy;
    }

    /**
     * Constructor for creating a new Pending payment before it is saved.
     * paymentId and paymentDate are assigned by the DB on INSERT.
     */
    public Payment(int bookingId, double amount, String paymentMethod) {
        this(0, bookingId, amount, null, paymentMethod, STATUS_PENDING, 0);
    }

    // ── Getters ────────────────────────────────────────────────────────
    public int       getPaymentId()     { return paymentId; }
    public int       getBookingId()     { return bookingId; }
    public double    getAmount()        { return amount; }
    public Timestamp getPaymentDate()   { return paymentDate; }
    public String    getPaymentMethod() { return paymentMethod; }
    public String    getStatus()        { return status; }
    public int       getProcessedBy()   { return processedBy; }

    // ── Setters ────────────────────────────────────────────────────────
    public void setPaymentId(int id)         { this.paymentId = id; }
    public void setPaymentDate(Timestamp ts) { this.paymentDate = ts; }
    public void setProcessedBy(int empId)    { this.processedBy = empId; }

    // ── Status Helpers ─────────────────────────────────────────────────
    public boolean isPending()    { return STATUS_PENDING.equals(status); }
    public boolean isPaid()       { return STATUS_PAID.equals(status); }
    public boolean isRefunded()   { return STATUS_REFUNDED.equals(status); }

    /** A payment can only be refunded if it has already been paid. */
    public boolean isRefundable() { return isPaid(); }

    // ── Business Methods ───────────────────────────────────────────────

    /**
     * Records the payment in memory (Pending → Paid).
     *
     * ⚠ Does NOT write to the DB. Call PaymentService.processPayment() first.
     *
     * @param method  the payment method used (must be a valid method string)
     * @param empId   the employee who processed it (0 for online self-service)
     */
    public void pay(String method, int empId) {
        if (!isPending())
            throw new IllegalStateException(
                "Only Pending payments can be marked as Paid. Current: " + status);
        if (!InputValidator.isValidPaymentMethod(method))
            throw new IllegalArgumentException("Invalid payment method: " + method);
        this.paymentMethod = method;
        this.processedBy   = empId;
        this.status        = STATUS_PAID;
    }

    /**
     * Records the refund in memory (Paid → Refunded).
     *
     * ⚠ Does NOT write to the DB. Call EmployeeService.refundPayment() first.
     */
    public void refund() {
        if (!isRefundable())
            throw new IllegalStateException(
                "Only Paid payments can be refunded. Current: " + status);
        this.status = STATUS_REFUNDED;
    }

    // ── toString ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Payment[id=%d, booking=%d, amount=%.2f, method=%s, status=%s]",
            paymentId, bookingId, amount, paymentMethod, status);
    }
}
