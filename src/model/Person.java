package model;

public abstract class Person {

    // ── Fields ────────────────────────────────────────────────────────
    protected int    id;        // AUTO_INCREMENT PK from DB
    protected String name;
    protected String email;
    protected String phone;
    protected String username;
    private   String password;  // stored as SHA-256 hex; NEVER returned publicly

    // ── Constructor ───────────────────────────────────────────────────
    protected Person(int id, String name, String email,
                     String phone, String username, String password) {
        this.id       = id;
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.username = username;
        this.password = password;
    }

    // ── Getters ───────────────────────────────────────────────────────
    public int    getId()       { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPhone()    { return phone; }
    public String getUsername() { return username; }

    // ── Validated Setters ─────────────────────────────────────────────

    /** Updates name after trimming; rejects blank values. */
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank.");
        this.name = name.trim();
    }

    /** Updates email after basic format check. */
    public void setEmail(String email) {
        if (!InputValidator.isValidEmail(email))
            throw new IllegalArgumentException("Invalid email address: " + email);
        this.email = email.trim().toLowerCase();
    }

    /** Updates phone; must be 10–15 digits with no spaces or dashes. */
    public void setPhone(String phone) {
        if (!InputValidator.isValidPhone(phone))
            throw new IllegalArgumentException("Phone must be 10–15 digits: " + phone);
        this.phone = phone.trim();
    }

   
    String getPassword() { return password; }

     @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person other = (Person) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // ── Abstract ──────────────────────────────────────────────────────
    @Override
    public abstract String toString();
}
