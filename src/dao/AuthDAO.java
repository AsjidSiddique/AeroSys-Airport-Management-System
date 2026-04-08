package dao;

import model.Employee;
import model.Passenger;
import model.Person;
import model.SessionManager;

import java.sql.*;

/**
 * AuthDAO – Database authentication for the login flow.
 * Flow:
 *   AeroSysLoginUI  →  AuthService.login()  →  AuthDAO.authenticate()
 *                                            →  sp_login stored procedure
 *                                            →  SessionManager.startSession()
 *                                            →  LoginResult returned up the chain
  */
public class AuthDAO {

   
    public static final class LoginResult {

        public final boolean success;
        public final Person  user;        // Employee or Passenger instance
        public final String  role;        // "ADMIN" | "EMPLOYEE" | "PASSENGER"
        public final String  userType;    // "EMPLOYEE" | "PASSENGER"
        public final String  errorMessage;

        private LoginResult(boolean success, Person user,
                            String role, String userType, String errorMessage) {
            this.success      = success;
            this.user         = user;
            this.role         = role;
            this.userType     = userType;
            this.errorMessage = errorMessage;
        }

        /** Factory: successful login. */
        public static LoginResult ok(Person user, String role, String userType) {
            return new LoginResult(true, user, role, userType, null);
        }

        /** Factory: failed login with a user-visible message. */
        public static LoginResult fail(String message) {
            return new LoginResult(false, null, null, null, message);
        }

        // ── Convenience helpers ──────────────────────────────────────
        public boolean isEmployee()  { return "EMPLOYEE".equals(userType); }
        public boolean isPassenger() { return "PASSENGER".equals(userType); }

        /** Safe cast — returns null if the logged-in user is a Passenger. */
        public Employee asEmployee() {
            return (user instanceof Employee) ? (Employee) user : null;
        }

        /** Safe cast — returns null if the logged-in user is an Employee. */
        public Passenger asPassenger() {
            return (user instanceof Passenger) ? (Passenger) user : null;
        }

        @Override
        public String toString() {
            return success
                    ? "LoginResult[OK role=" + role + " userType=" + userType + "]"
                    : "LoginResult[FAIL: " + errorMessage + "]";
        }
    }

    // ================================================================
    //  authenticate()  –  Main entry point called by AuthService
    // ================================================================

    /**
     * Authenticates a user against the database via the {@code sp_login} stored procedure.
     *
     * <p>On success the session is started immediately inside this method so the
     * SessionManager is always populated before the result reaches the UI.
     *
     * @param username  plain-text username (trimmed internally)
     * @param password  plain-text password
     * @param roleType  {@code "EMPLOYEE"} or {@code "PASSENGER"}
     * @return          a {@link LoginResult} — never null
     */
    public LoginResult authenticate(String username, String password, String roleType) {

        // ── 1. Input validation (before any DB call) ──────────────────
        if (username == null || username.isBlank())
            return LoginResult.fail("Username cannot be empty.");
        if (password == null || password.isBlank())
            return LoginResult.fail("Password cannot be empty.");
        if (!"EMPLOYEE".equals(roleType) && !"PASSENGER".equals(roleType))
            return LoginResult.fail("Unknown role type: " + roleType);

        // ── 2. Call sp_login stored procedure ─────────────────────────
        String sql = "{CALL sp_login(?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, username.trim());
            cs.setString(2, password);
            cs.setString(3, roleType);

            boolean hasResult = cs.execute();
            if (!hasResult)
                return LoginResult.fail("No response from login procedure.");

            try (ResultSet rs = cs.getResultSet()) {

                // No row → wrong credentials or account doesn't exist
                if (!rs.next())
                    return LoginResult.fail("Invalid username or password.");

                // ── 3. Check account status ───────────────────────────
                String accountStatus = rs.getString("account_status");
                if (!"Active".equalsIgnoreCase(accountStatus))
                    return LoginResult.fail(
                            "Your account has been deactivated.\n"
                          + "Please contact an administrator.");

                // ── 4. Build the correct Person subclass ──────────────
                int    userId   = rs.getInt("user_id");
                String name     = rs.getString("name");
                String userRole = rs.getString("user_role");   // ADMIN | EMPLOYEE | PASSENGER
                String userType = rs.getString("user_type");   // EMPLOYEE | PASSENGER

                Person user;

                if ("EMPLOYEE".equals(userType)) {
                    user = new Employee(
                            userId,
                            name,
                            null,               // email not in sp_login result
                            null,               // phone not in sp_login result
                            userRole,
                            username.trim(),
                            null                // password never held in session
                    );
                } else {
                    user = new Passenger(
                            userId,
                            name,
                            null,               // email not in sp_login result
                            null,               // phone not in sp_login result
                            rs.getString("passport_no"),
                            null,               // nationality not in sp_login result
                            null,               // dob not in sp_login result
                            username.trim(),
                            null                // password never held in session
                    );
                }

                // ── 5. Start the session ──────────────────────────────
                SessionManager.getInstance().startSession(user, userRole);

                return LoginResult.ok(user, userRole, userType);
            }

        } catch (SQLSyntaxErrorException e) {
            System.err.println("[AuthDAO.authenticate] SQL syntax error: " + e.getMessage());
            return LoginResult.fail("Database query error. Contact support.");

        } catch (SQLException e) {
            System.err.println("[AuthDAO.authenticate] SQL error "
                    + e.getErrorCode() + ": " + e.getMessage());
            return LoginResult.fail(
                    "Database error (" + e.getErrorCode() + ").\n"
                  + "Please try again or contact support.");

        } catch (Exception e) {
            System.err.println("[AuthDAO.authenticate] Unexpected: " + e.getMessage());
            return LoginResult.fail("An unexpected error occurred. Please try again.");
        }
    }
}
