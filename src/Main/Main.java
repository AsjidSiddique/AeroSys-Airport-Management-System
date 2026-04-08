package Main;

/**
 * Main – Application entry point. One class, one method, one line of logic.
 *  Call flow
 * ═══════════════════════════════════════════════════════
 *  main()
 *    └─► AirportSystem.getInstance()   — Singleton created
 *          └─► launch()               — AeroSysLoginUI opened on EDT
 */
public class Main {

    public static void main(String[] args) {
        AirportSystem.getInstance().launch();
    }
}
