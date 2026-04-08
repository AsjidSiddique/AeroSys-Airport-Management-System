package service;

import dao.TicketDAO;
import model.Booking;
import model.SessionManager;
import java.util.List;


public class TicketService {

    private final TicketDAO ticketDAO = new TicketDAO();

    
    public List<Object[]> getMyTickets() {
        int passengerId;
        try {
            passengerId = SessionManager.getInstance().requirePassenger().getPassengerId();
        } catch (IllegalStateException e) {
            return List.of();
        }
        return ticketDAO.getPassengerTicketsWithDetails(passengerId);
    }

    //  TICKET GENERATION  
    public String generateTicketsForBooking(Booking booking) {
        if (booking == null)
            return "ERROR:Cannot generate tickets — booking is null.";

        int seatCount   = booking.getSeatCount();
        int passengerId = booking.getPassengerId();
        int flightId    = booking.getFlightId();
        int bookingId   = booking.getBookingId();

        if (seatCount < 1)
            return "ERROR:Booking has no seats to generate tickets for.";

        for (int i = 0; i < seatCount; i++) {
            String seatNumber = ticketDAO.generateSeatNumber(flightId, i);

            var ticket = ticketDAO.generateTicket(bookingId, passengerId, flightId, seatNumber);

            if (ticket == null) {
                return "ERROR:Failed to generate ticket " + (i + 1) + " of " + seatCount
                        + " (seat " + seatNumber + "). "
                        + "Tickets already issued may have been created — contact support.";
            }
        }

        return "SUCCESS:" + seatCount + " ticket(s) issued successfully.";
    }
}
