package service;

import dao.AuthDAO;
import dao.AuthDAO.LoginResult;
import dao.PassengerDAO;
import model.InputValidator;
import model.SessionManager;


public class AuthService {

    private final AuthDAO     authDAO     = new AuthDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();

    //Authenticates a user (employee OR passenger) against the database.
   
    public LoginResult login(String username, String password, String roleType) {
        // Input guard — fast-fail before any DB call
        if (isBlank(username)) return LoginResult.fail("Username cannot be empty.");
        if (isBlank(password)) return LoginResult.fail("Password cannot be empty.");
        if (!"EMPLOYEE".equals(roleType) && !"PASSENGER".equals(roleType))
            return LoginResult.fail("Unknown role type: " + roleType);

        return authDAO.authenticate(username.trim(), password, roleType);
    }

    //  LOGOUT
    // ================================================================

    public void logout() {
        SessionManager.getInstance().endSession();
    }

    // ================================================================
    //  REGISTRATION
    // ================================================================

    /**
     * Submits a new passenger registration request.
     */
    public String registerPassengerRequest(String name,       String email,
                                           String phone,      String passportNo,
                                           String nationality, java.sql.Date dob,
                                           String username,   String password,
                                           String confirmPassword) {
        // ── Field presence checks ─────────────────────────────────────
        if (isBlank(name))        return "ERROR:Full name is required.";
        if (isBlank(email))       return "ERROR:Email address is required.";
        if (isBlank(phone))       return "ERROR:Phone number is required.";
        if (isBlank(passportNo))  return "ERROR:Passport number is required.";
        if (isBlank(nationality)) return "ERROR:Nationality is required.";
        if (dob == null)          return "ERROR:Date of birth is required.";
        if (isBlank(username))    return "ERROR:Username is required.";
        if (isBlank(password))    return "ERROR:Password is required.";

        // ── Format / business rule checks ────────────────────────────
        if (name.trim().length() < 3)
            return "ERROR:Name must be at least 3 characters.";
        if (!InputValidator.isValidEmail(email))
            return "ERROR:Please enter a valid email address.";
        if (!InputValidator.isValidPhone(phone))
            return "ERROR:Phone must be 10–15 digits only.";
        if (!InputValidator.isValidPassportNo(passportNo))
            return "ERROR:Passport number format is invalid.";
        if (!InputValidator.isValidUsername(username))
            return "ERROR:Username must be 4–20 characters: letters, numbers, underscore only.";
        if (password.length() < 6)
            return "ERROR:Password must be at least 6 characters.";
        if (!password.equals(confirmPassword))
            return "ERROR:Passwords do not match.";

        // ── Delegate to DAO ───────────────────────────────────────────
        boolean saved = passengerDAO.registerRequest(
                name.trim(), email.trim().toLowerCase(),
                phone.trim(), passportNo.trim().toUpperCase(),
                nationality.trim(), dob,
                username.trim(), password);

        return saved
                ? "SUCCESS:Registration request submitted. Please wait for admin approval."
                : "ERROR:Username, email, or passport number is already registered. "
                + "Please use different details or contact support.";
    }

    // ================================================================
    //  PRIVATE HELPERS
    // ================================================================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
