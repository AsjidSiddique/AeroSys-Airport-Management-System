package model;

import java.sql.Timestamp;

public class Employee extends Person {

    // ── Allowed values (use these constants everywhere, not raw strings) ─
    public static final String ROLE_ADMIN    = "ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";
    public static final String STATUS_ACTIVE      = "Active";
    public static final String STATUS_DEACTIVATED = "Deactivated";

    // ── Fields ────────────────────────────────────────────────────────
    private String    role;           // ROLE_ADMIN | ROLE_EMPLOYEE
    private String    accountStatus;  // STATUS_ACTIVE | STATUS_DEACTIVATED
    private Timestamp createdAt;

    // ── Full constructor — used by DAO mappers when loading from DB ───
    public Employee(int       employeeId,
                    String    name,
                    String    email,
                    String    phone,
                    String    role,
                    String    username,
                    String    password,
                    String    accountStatus,
                    Timestamp createdAt) {
        super(employeeId, name, email, phone, username, password);
        this.role          = isValidRole(role)          ? role          : ROLE_EMPLOYEE;
        this.accountStatus = isValidStatus(accountStatus) ? accountStatus : STATUS_ACTIVE;
        this.createdAt     = createdAt;
    }

    /**
     * Convenience constructor — used right after login when createdAt
     * and accountStatus are not needed by the session.
     * accountStatus defaults to "Active".
     */
    public Employee(int    employeeId,
                    String name,
                    String email,
                    String phone,
                    String role,
                    String username,
                    String password) {
        this(employeeId, name, email, phone, role,
             username, password, STATUS_ACTIVE, null);
    }

    // ── Getters ───────────────────────────────────────────────────────
    /** Alias for Person.getId() — matches the DB column name. */
    public int       getEmployeeId()    { return id; }
    public String    getRole()          { return role; }
    public String    getAccountStatus() { return accountStatus; }
    public Timestamp getCreatedAt()     { return createdAt; }

    // ── Validated Setters ─────────────────────────────────────────────

    /** Changes role; only accepts "ADMIN" or "EMPLOYEE". */
    public void setRole(String role) {
        if (!isValidRole(role))
            throw new IllegalArgumentException(
                "Role must be ADMIN or EMPLOYEE, got: " + role);
        this.role = role;
    }

    /** Changes account status; only accepts "Active" or "Deactivated". */
    public void setAccountStatus(String status) {
        if (!isValidStatus(status))
            throw new IllegalArgumentException(
                "Status must be Active or Deactivated, got: " + status);
        this.accountStatus = status;
    }

    // ── Role / Status Helpers ─────────────────────────────────────────

    /** True when this employee holds the ADMIN role. */
    public boolean isAdmin() { return ROLE_ADMIN.equals(role); }

    /**
     * True for both ADMIN and EMPLOYEE roles.
     * Use this for checks that apply to any logged-in staff member.
     */
    public boolean isStaff() { return isAdmin() || ROLE_EMPLOYEE.equals(role); }

    /** True when the account is active and can log in. */
    public boolean isActive()      { return STATUS_ACTIVE.equals(accountStatus); }

    /** True when the account has been deactivated by an admin. */
    public boolean isDeactivated() { return STATUS_DEACTIVATED.equals(accountStatus); }

    // ── toString ─────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Employee[id=%d, name='%s', role=%s, status=%s]",
            id, name, role, accountStatus);
    }

    // ── Private validation helpers ────────────────────────────────────
    private static boolean isValidRole(String r) {
        return ROLE_ADMIN.equals(r) || ROLE_EMPLOYEE.equals(r);
    }

    private static boolean isValidStatus(String s) {
        return STATUS_ACTIVE.equals(s) || STATUS_DEACTIVATED.equals(s);
    }
}
