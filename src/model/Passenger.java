package model;

import java.sql.Date;
import java.sql.Timestamp;


public class Passenger extends Person {

    // ── Allowed status values ──────────────────────────────────────────
    public static final String STATUS_ACTIVE      = "Active";
    public static final String STATUS_SUSPENDED   = "Suspended";
    public static final String STATUS_DEACTIVATED = "Deactivated";

    // ── Fields ────────────────────────────────────────────────────────
    private String    passportNo;
    private String    nationality;
    private Date      dateOfBirth;
    private String    accountStatus;
    private Timestamp createdAt;

    // ── Full constructor — used by DAO mappers ────────────────────────
    public Passenger(int       passengerId,
                     String    name,
                     String    email,
                     String    phone,
                     String    passportNo,
                     String    nationality,
                     Date      dateOfBirth,
                     String    username,
                     String    password,
                     String    accountStatus,
                     Timestamp createdAt) {
        super(passengerId, name, email, phone, username, password);
        this.passportNo    = passportNo;
        this.nationality   = nationality;
        this.dateOfBirth   = dateOfBirth;
        this.accountStatus = isValidStatus(accountStatus) ? accountStatus : STATUS_ACTIVE;
        this.createdAt     = createdAt;
    }

    /**
     * Convenience constructor — used after login or new registration
     * when createdAt is not yet known.
     * accountStatus defaults to "Active".
     */
    public Passenger(int    passengerId,
                     String name,
                     String email,
                     String phone,
                     String passportNo,
                     String nationality,
                     Date   dateOfBirth,
                     String username,
                     String password) {
        this(passengerId, name, email, phone, passportNo, nationality,
             dateOfBirth, username, password, STATUS_ACTIVE, null);
    }

    // ── Getters ───────────────────────────────────────────────────────
    /** Alias for Person.getId() — matches the DB column name. */
    public int       getPassengerId()   { return id; }
    public String    getPassportNo()    { return passportNo; }
    public String    getNationality()   { return nationality; }
    public Date      getDateOfBirth()   { return dateOfBirth; }
    public String    getAccountStatus() { return accountStatus; }
    public Timestamp getCreatedAt()     { return createdAt; }

    // ── Validated Setter ──────────────────────────────────────────────

    /**
     * Changes account status.
     * Only accepts the three defined lifecycle values.
     * Note: deactivation is permanent by policy — enforce this in the service layer.
     */
    public void setAccountStatus(String status) {
        if (!isValidStatus(status))
            throw new IllegalArgumentException(
                "Status must be Active, Suspended, or Deactivated, got: " + status);
        this.accountStatus = status;
    }

    // ── Status Helpers ────────────────────────────────────────────────
    public boolean isActive()      { return STATUS_ACTIVE.equals(accountStatus); }
    public boolean isSuspended()   { return STATUS_SUSPENDED.equals(accountStatus); }
    public boolean isDeactivated() { return STATUS_DEACTIVATED.equals(accountStatus); }

    /** True if the passenger can log in and make bookings. */
    public boolean canBook()       { return isActive(); }

    // ── toString ─────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Passenger[id=%d, passport='%s', name='%s', status=%s]",
            id, passportNo, name, accountStatus);
    }

    // ── Private validation helper ─────────────────────────────────────
    private static boolean isValidStatus(String s) {
        return STATUS_ACTIVE.equals(s)
            || STATUS_SUSPENDED.equals(s)
            || STATUS_DEACTIVATED.equals(s);
    }
}
