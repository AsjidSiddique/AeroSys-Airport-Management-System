package model;


public class SessionManager {

    // ── Singleton ─────────────────────────────────────────────────────
    private static SessionManager instance;

    private Person currentUser;  // Employee or Passenger, never null while logged in
    private String currentRole;  // "ADMIN" | "EMPLOYEE" | "PASSENGER"

    private SessionManager() {}

    /** Thread-safe Singleton accessor. */
    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // ── Session Lifecycle ─────────────────────────────────────────────

    /**
     * Starts a new session after a successful login.
     *
     * @param user  fully-populated Employee or Passenger object from DB
     * @param role  "ADMIN", "EMPLOYEE", or "PASSENGER"
     * @throws IllegalArgumentException if user or role is null
     */
    public void startSession(Person user, String role) {
        if (user == null || role == null)
            throw new IllegalArgumentException("User and role cannot be null.");
        this.currentUser = user;
        this.currentRole = role;
    }

    /** Clears the session. Called on logout or before a new login. */
    public void endSession() {
        this.currentUser = null;
        this.currentRole = null;
    }

    // ── State Checks ──────────────────────────────────────────────────
    public boolean isLoggedIn()  { return currentUser != null; }
    public String  getRole()     { return currentRole; }
    public Person  getUser()     { return currentUser; }

    /** True only for the ADMIN role. */
    public boolean isAdmin()     { return Employee.ROLE_ADMIN.equals(currentRole); }

    /** True for both ADMIN and EMPLOYEE roles. */
    public boolean isEmployee()  { return isAdmin()
                                       || Employee.ROLE_EMPLOYEE.equals(currentRole); }

    /** True for the PASSENGER role. */
    public boolean isPassenger() { return "PASSENGER".equals(currentRole); }

    // ── Typed Accessors ───────────────────────────────────────────────

    /**
     * Returns the logged-in user cast as Employee, or null if a passenger
     * is currently logged in.
     */
    public Employee asEmployee() {
        return (currentUser instanceof Employee) ? (Employee) currentUser : null;
    }

    /**
     * Returns the logged-in user cast as Passenger, or null if an employee
     * is currently logged in.
     */
    public Passenger asPassenger() {
        return (currentUser instanceof Passenger) ? (Passenger) currentUser : null;
    }

    /**
     * Returns the logged-in Employee or throws if no employee is in session.
     * Use this in EmployeeService to assert the caller is authenticated.
     *
     * @throws IllegalStateException if no employee is logged in
     */
    public Employee requireEmployee() {
        Employee emp = asEmployee();
        if (emp == null)
            throw new IllegalStateException(
                "An employee session is required but none is active.");
        return emp;
    }

    /**
     * Returns the logged-in Passenger or throws if no passenger is in session.
     *
     * @throws IllegalStateException if no passenger is logged in
     */
    public Passenger requirePassenger() {
        Passenger pax = asPassenger();
        if (pax == null)
            throw new IllegalStateException(
                "A passenger session is required but none is active.");
        return pax;
    }

    /** Returns the DB primary key of the current user, or -1 if not logged in. */
    public int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : -1;
    }

    @Override
    public String toString() {
        if (!isLoggedIn()) return "SessionManager[no session]";
        return String.format("SessionManager[user=%s, role=%s]",
                             currentUser.getUsername(), currentRole);
    }
}
