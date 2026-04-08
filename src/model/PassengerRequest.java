package model;

import java.sql.Date;
import java.sql.Timestamp;


public class PassengerRequest {

    // ── Status constants ───────────────────────────────────────────────
    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    // ── Fields ─────────────────────────────────────────────────────────
    private int       requestId;
    private String    name;
    private String    email;
    private String    phone;
    private String    passportNo;
    private String    nationality;
    private Date      dateOfBirth;
    private String    username;
    private String    password;     // SHA-256 hash stored by DAO
    private Timestamp requestDate;
    private String    status;       // see STATUS_* constants
    private int       reviewedBy;   // employee_id; 0 = not yet reviewed

    // ── Full constructor — used by DAO mappers ────────────────────────
    public PassengerRequest(int requestId, String name, String email,
                            String phone, String passportNo, String nationality,
                            Date dateOfBirth, String username, String password,
                            Timestamp requestDate, String status, int reviewedBy) {
        this.requestId   = requestId;
        this.name        = name;
        this.email       = email;
        this.phone       = phone;
        this.passportNo  = passportNo;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.username    = username;
        this.password    = password;
        this.requestDate = requestDate;
        this.status      = (status != null) ? status : STATUS_PENDING;
        this.reviewedBy  = reviewedBy;
    }

    /**
     * Constructor for submitting a new registration request.
     * requestId, requestDate, status, and reviewedBy are set by the DB on INSERT.
     */
    public PassengerRequest(String name, String email, String phone,
                            String passportNo, String nationality,
                            Date dateOfBirth, String username, String password) {
        this(0, name, email, phone, passportNo, nationality,
             dateOfBirth, username, password, null, STATUS_PENDING, 0);
    }

    // ── Getters ────────────────────────────────────────────────────────
    public int       getRequestId()   { return requestId; }
    public String    getName()        { return name; }
    public String    getEmail()       { return email; }
    public String    getPhone()       { return phone; }
    public String    getPassportNo()  { return passportNo; }
    public String    getNationality() { return nationality; }
    public Date      getDateOfBirth() { return dateOfBirth; }
    public String    getUsername()    { return username; }
    public String    getPassword()    { return password; }  // returns the hash
    public Timestamp getRequestDate() { return requestDate; }
    public String    getStatus()      { return status; }
    public int       getReviewedBy()  { return reviewedBy; }

    // ── Setters ────────────────────────────────────────────────────────
    public void setRequestId(int id) { this.requestId = id; }

    /**
     * Updates the review decision.
     * Only APPROVED or REJECTED are valid — PENDING is the initial state set
     * by the constructor and should not be set externally.
     *
     * @param status      STATUS_APPROVED or STATUS_REJECTED
     * @param reviewerId  the employee_id who made the decision
     */
    public void review(String status, int reviewerId) {
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status))
            throw new IllegalArgumentException(
                "Review status must be APPROVED or REJECTED, got: " + status);
        if (!isPending())
            throw new IllegalStateException(
                "Only PENDING requests can be reviewed. Current: " + this.status);
        this.status     = status;
        this.reviewedBy = reviewerId;
    }

    // ── Status Helpers ─────────────────────────────────────────────────
    public boolean isPending()  { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }

    /** True for APPROVED and REJECTED — no further changes allowed. */
    public boolean isReviewed() { return isApproved() || isRejected(); }

    // ── toString ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "PassengerRequest[id=%d, name='%s', email='%s', username='%s', status=%s]",
            requestId, name, email, username, status);
    }
}
