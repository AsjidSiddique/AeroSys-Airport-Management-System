package service;

import dao.FlightDAO;
import model.Flight;

import java.sql.Date;
import java.util.List;


public class FlightService {

    private final FlightDAO flightDAO = new FlightDAO();

    public List<Flight> getAllAvailableFlights() {
        return flightDAO.getAllAvailableFlights();
    }

 
    public List<Flight> searchFlights(String origin, String destination, Date travelDate) {
        if (isBlank(origin))       return List.of();
        if (isBlank(destination))  return List.of();
        if (travelDate == null)    return List.of();
        if (origin.trim().equalsIgnoreCase(destination.trim())) return List.of();

        return flightDAO.searchFlights(origin.trim(), destination.trim(), travelDate);
    }

    /**
     * Returns a single non-cancelled flight by its primary key.
     *
     * @return the {@link Flight}, or {@code null} if not found or cancelled
     */
    public Flight getFlightById(int flightId) {
        if (flightId <= 0) return null;
        return flightDAO.getFlightById(flightId);
    }

    // ================================================================
    //  AVAILABILITY CHECK  (used by BookingService)
    // ================================================================

    /**
     * Checks whether a flight can be booked for the given seat count.
     *
     * <p>Used by {@link BookingService} as a pre-booking guard,
     * and can also be called by the GUI to enable/disable the Book button.
     *
     * @param flightId       flight to check
     * @param requestedSeats number of seats the passenger wants
     * @return {@code true} only if the flight is bookable AND has enough seats
     */
    public boolean isFlightBookable(int flightId, int requestedSeats) {
        if (flightId <= 0 || requestedSeats < 1) return false;
        Flight flight = flightDAO.getFlightById(flightId);
        if (flight == null) return false;
        return flight.isBookable() && flight.hasAvailableSeats(requestedSeats);
    }

    // ================================================================
    //  PRIVATE HELPERS
    // ================================================================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
