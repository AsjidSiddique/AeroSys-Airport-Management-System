package Main;

import model.Employee;
import model.SessionManager;
import service.AuthService;
import service.BookingService;
import service.EmployeeService;
import service.FlightService;
import service.PaymentService;
import service.TicketService;

/**
 * AirportSystem – Central Controller and Facade for the entire application.
 
 
 *  Typical call flow
 * ═══════════════════════════════════════════════════════
 *  1. Main.main()
 *       └─► AirportSystem.getInstance().launch()
 *             └─► SwingUtilities.invokeLater → AeroSysLoginUI.setVisible(true)
 *
 *  2. User logs in (employee)
 *       └─► Login.authenticate() → sets SessionManager session
 *       └─► AeroSysAdminUI calls AirportSystem.getInstance().getEmployeeService()
 *             └─► EmployeeService created lazily, bound to current session
 *
 *  3. User logs out
 *       └─► AeroSysAdminUI calls AirportSystem.getInstance().onLogout()
 *             └─► employeeService = null, session cleared
 *             └─► AeroSysLoginUI re-opens
 */
public class AirportSystem {

    // ── Singleton instance ─────────────────────────────────────────────
    private static AirportSystem instance;

    // ── Stateless services — created once at startup ───────────────────
    private final AuthService    authService;
    private final BookingService bookingService;
    private final FlightService  flightService;
    private final PaymentService paymentService;
    private final TicketService  ticketService;

    // ── Session-dependent service — created lazily after employee login ─
    private EmployeeService employeeService;
    // Tracks which employee the cached service was built for.
    // When a different employee logs in, the cache is rebuilt.
    private int cachedEmployeeId = -1;

    // ── Private constructor ────────────────────────────────────────────
    private AirportSystem() {
        this.authService    = new AuthService();
        this.bookingService = new BookingService();
        this.flightService  = new FlightService();
        this.paymentService = new PaymentService();
        this.ticketService  = new TicketService();
        // employeeService intentionally null — built lazily after login
    }

    // ── Singleton accessor ─────────────────────────────────────────────

    public static synchronized AirportSystem getInstance() {
        if (instance == null) {
            instance = new AirportSystem();
        }
        return instance;
    }

    /** Passenger authentication and registration. */
    public AuthService    getAuthService()    { return authService; }

    /** Passenger-side booking creation and cancellation. */
    public BookingService getBookingService() { return bookingService; }

    /** Passenger-side flight search and availability checks. */
    public FlightService  getFlightService()  { return flightService; }

    /** Passenger-side payment processing and ticket generation. */
    public PaymentService getPaymentService() { return paymentService; }

    /** Passenger-side ticket retrieval. */
    public TicketService  getTicketService()  { return ticketService; }

   
    public EmployeeService getEmployeeService() {
        // Validate session before creating or returning the service.
        // requireEmployee() throws a clear IllegalStateException if no
        // employee is logged in — no silent null returns.
        Employee currentEmployee = SessionManager.getInstance().requireEmployee();

        // Rebuild the service whenever the session belongs to a different
        // employee (e.g. admin logs out and a different admin logs in).
        if (employeeService == null
                || cachedEmployeeId != currentEmployee.getEmployeeId()) {
            employeeService    = new EmployeeService();
            cachedEmployeeId   = currentEmployee.getEmployeeId();
        }

        return employeeService;
    }

      public void onLogout() {
        employeeService  = null;                   // discard session-bound service
        cachedEmployeeId = -1;                     // reset cache key
        SessionManager.getInstance().endSession(); // clear user + role
    }

     public void launch() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new Gui.AeroSysLoginUI().setVisible(true);
        });
    }
}
