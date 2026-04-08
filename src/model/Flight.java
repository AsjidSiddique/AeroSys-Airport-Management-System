package model;

import java.sql.Timestamp;

public class Flight {

    // ── Status constants ───────────────────────────────────────────────
    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_DELAYED   = "Delayed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    // ── Fields ────────────────────────────────────────────────────────
    private int       flightId;
    private String    flightNumber;
    private String    origin;
    private String    destination;
    private Timestamp departureTime;
    private Timestamp arrivalTime;
    private int       totalSeats;
    private int       availableSeats;
    private double    pricePerSeat;
    private String    status;        // see STATUS_* constants
    private int       managedBy;     // employee_id FK

    // ── Full constructor — used by DAO mappers ────────────────────────
    public Flight(int flightId, String flightNumber, String origin,
                  String destination, Timestamp departureTime, Timestamp arrivalTime,
                  int totalSeats, int availableSeats, double pricePerSeat,
                  String status, int managedBy) {
        this.flightId       = flightId;
        this.flightNumber   = flightNumber;
        this.origin         = origin;
        this.destination    = destination;
        this.departureTime  = departureTime;
        this.arrivalTime    = arrivalTime;
        this.totalSeats     = totalSeats;
        this.availableSeats = availableSeats;
        this.pricePerSeat   = pricePerSeat;
        this.status         = (status != null) ? status : STATUS_SCHEDULED;
        this.managedBy      = managedBy;
    }

    /**
     * Constructor for a brand-new flight before it is saved to the DB.
     * flightId defaults to 0; availableSeats starts equal to totalSeats.
     * status defaults to "Scheduled".
     */
    public Flight(String flightNumber, String origin, String destination,
                  Timestamp departureTime, Timestamp arrivalTime,
                  int totalSeats, double pricePerSeat, int managedBy) {
        this(0, flightNumber, origin, destination, departureTime, arrivalTime,
             totalSeats, totalSeats, pricePerSeat, STATUS_SCHEDULED, managedBy);
    }

    // ── Getters ───────────────────────────────────────────────────────
    public int       getFlightId()       { return flightId; }
    public String    getFlightNumber()   { return flightNumber; }
    public String    getOrigin()         { return origin; }
    public String    getDestination()    { return destination; }
    public Timestamp getDepartureTime()  { return departureTime; }
    public Timestamp getArrivalTime()    { return arrivalTime; }
    public int       getTotalSeats()     { return totalSeats; }
    public int       getAvailableSeats() { return availableSeats; }
    public double    getPrice()          { return pricePerSeat; }
    public String    getStatus()         { return status; }
    public int       getManagedBy()      { return managedBy; }

    // ── Setters ───────────────────────────────────────────────────────
    public void setFlightId(int id)          { this.flightId = id; }
    public void setOrigin(String origin)     { this.origin = origin; }
    public void setDestination(String dest)  { this.destination = dest; }
    public void setDepartureTime(Timestamp t){ this.departureTime = t; }
    public void setArrivalTime(Timestamp t)  { this.arrivalTime = t; }
    public void setManagedBy(int empId)      { this.managedBy = empId; }

    /** Price must be non-negative. */
    public void setPrice(double price) {
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        this.pricePerSeat = price;
    }

    /**
     * Available seats must be between 0 and totalSeats.
     * Called by DAO after a booking reduces the count.
     */
    public void setAvailableSeats(int seats) {
        if (seats < 0 || seats > totalSeats)
            throw new IllegalArgumentException(
                "Available seats must be 0–" + totalSeats + ", got: " + seats);
        this.availableSeats = seats;
    }

    /** Status must be one of the four defined values. */
    public void setStatus(String status) {
        if (!STATUS_SCHEDULED.equals(status) && !STATUS_DELAYED.equals(status)
         && !STATUS_COMPLETED.equals(status) && !STATUS_CANCELLED.equals(status))
            throw new IllegalArgumentException("Invalid flight status: " + status);
        this.status = status;
    }

    // ── Business Helpers ──────────────────────────────────────────────

    /** True when at least one seat is free. */
    public boolean hasAvailableSeats(int count) {
        return availableSeats >= count;
    }

    /**
     * A flight is bookable when it is Scheduled or Delayed
     * AND has at least one available seat.
     */
    public boolean isBookable() {
        return (STATUS_SCHEDULED.equals(status) || STATUS_DELAYED.equals(status))
            && availableSeats > 0;
    }

    /** True for Completed or Cancelled — no further state changes allowed. */
    public boolean isTerminal() {
        return STATUS_COMPLETED.equals(status) || STATUS_CANCELLED.equals(status);
    }

    /**
     * Calculates the total price for the given number of seats.
     *
     * @param seatCount number of seats to book (must be > 0)
     * @return pricePerSeat × seatCount
     */
    public double calculateTotalPrice(int seatCount) {
        if (seatCount < 1)
            throw new IllegalArgumentException("Seat count must be at least 1.");
        return pricePerSeat * seatCount;
    }

    // ── toString ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "Flight[id=%d, number=%s, %s→%s, dep=%s, seats=%d/%d, price=%.2f, status=%s]",
            flightId, flightNumber, origin, destination,
            departureTime, availableSeats, totalSeats, pricePerSeat, status);
    }
}
