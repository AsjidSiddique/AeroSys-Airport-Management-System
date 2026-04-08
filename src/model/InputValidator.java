package model;

import java.util.Set;


public final class InputValidator {

    // ── Valid values ───────────────────────────────────────────────────
    /** All accepted payment method strings. */
    public static final Set<String> VALID_PAYMENT_METHODS =
        Set.of("Credit Card", "Debit Card", "Cash", "Online");

    /** All accepted employee role strings. */
    public static final Set<String> VALID_ROLES =
        Set.of(Employee.ROLE_ADMIN, Employee.ROLE_EMPLOYEE);

    /** Maximum number of seats a passenger can book in one transaction. */
    public static final int MAX_SEAT_COUNT = 10;

    // ── Private constructor ────────────────────────────────────────────
    private InputValidator() {}   // utility class — never instantiated

    // ══════════════════════════════════════════════════════════════════
    //  TEXT VALIDATORS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns true if the value is non-null and contains at least one
     * non-whitespace character.
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    // ══════════════════════════════════════════════════════════════════
    //  FORMAT VALIDATORS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Valid email: local-part @ domain . TLD (minimum 2-char TLD).
     * Examples: user@example.com, first.last+tag@mail.co.uk
     */
    public static boolean isValidEmail(String email) {
        return email != null &&
               email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Valid phone: 10 to 15 consecutive digits.
     * No spaces, dashes, or country-code prefixes.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10,15}$");
    }

    /**
     * Valid passport number: 5 to 20 uppercase letters or digits.
     * Examples: A12345678, AB1234567
     */
    public static boolean isValidPassportNo(String passport) {
        return passport != null && passport.matches("^[A-Z0-9]{5,20}$");
    }

    /**
     * Valid username: 4 to 30 characters; letters, digits, and underscores only.
     * No spaces, no special characters.
     */
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[A-Za-z0-9_]{4,30}$");
    }

    // Valid password: minimum 6 characters, at least one non-whitespace character.
          public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6 && !password.isBlank();
    }

    // ══════════════════════════════════════════════════════════════════
    //  BUSINESS RULE VALIDATORS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Seat count must be between 1 and MAX_SEAT_COUNT (inclusive).
     */
    public static boolean isValidSeatCount(int count) {
        return count >= 1 && count <= MAX_SEAT_COUNT;
    }

    /**
     * Payment method must be one of the four values in VALID_PAYMENT_METHODS.
     */
    public static boolean isValidPaymentMethod(String method) {
        return method != null && VALID_PAYMENT_METHODS.contains(method);
    }

    /**
     * Employee role must be "ADMIN" or "EMPLOYEE".
     */
    public static boolean isValidRole(String role) {
        return role != null && VALID_ROLES.contains(role);
    }
}
