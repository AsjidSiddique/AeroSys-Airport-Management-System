package Gui;

import dao.DatabaseConnection;
import dao.EmployeeDAO.DashboardStats;
import model.*;
import service.EmployeeService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class AeroSysAdminUI extends JFrame {

    private final Employee loggedInEmployee;
    private final EmployeeService svc;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel lblTime, lblDate, lblWelcome;

    private AeroComponents.SidebarButton btnDashboard, btnFlights, btnAddFlight;
    private AeroComponents.SidebarButton btnPassengers, btnRequests;
    private AeroComponents.SidebarButton btnBookings, btnTickets;
    private AeroComponents.SidebarButton btnEmployees, btnAddEmployee;

    private AeroComponents.StatCard cardFlights, cardPassengers,
            cardBookings, cardRevenue, cardTickets, cardPending,
            cardScheduled, cardDelayed;

    private DefaultTableModel dashFlightsModel;

    private static final int FLT_ID_COL = 0;
    private JTable tblFlights;
    private DefaultTableModel modelFlights;
    private AeroComponents.AeroTextField txtSearchFlight;
    private AeroComponents.AeroTextField txtFlightNo, txtOrigin, txtDestination;
    private AeroComponents.AeroTextField txtDepTime, txtArrTime, txtSeats, txtPrice;
    private JComboBox<String> cmbFlightStatus;

    private static final int PAX_ID_COL = 0;
    private static final int PAX_STATUS_COL = 7;
    private JTable tblPassengers;
    private DefaultTableModel modelPassengers;
    private AeroComponents.AeroTextField txtSearchPassenger;

    private static final int REQ_ID_COL = 0;
    private static final int REQ_STATUS_COL = 8;
    private JTable tblRequests;
    private DefaultTableModel modelRequests;
    private AeroComponents.AeroTextField txtSearchRequest;

    private static final int BKG_ID_COL = 0;
    private static final int BKG_STATUS_COL = 9;
    private static final int BKG_PAY_STATUS_COL = 10;
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private AeroComponents.AeroTextField txtSearchBooking;
    private int[] bkgPassengerIds = new int[0];
    private int[] bkgFlightIds = new int[0];
    private int[] bkgPaymentIds = new int[0];

    private static final int TKT_ID_COL = 0;
    private JTable tblTickets;
    private DefaultTableModel modelTickets;
    private AeroComponents.AeroTextField txtSearchTicket;

    private static final int EMP_ID_COL = 0;
    private static final int EMP_STATUS_COL = 6;
    private JTable tblEmployees;
    private DefaultTableModel modelEmployees;
    private AeroComponents.AeroTextField txtSearchEmployee;
    private AeroComponents.AeroTextField txtEmpName, txtEmpEmail, txtEmpPhone, txtEmpUsername;
    private AeroComponents.AeroPasswordField txtEmpPassword, txtEmpConfirmPass;
    private JComboBox<String> cmbEmpRole;

    private static final Color BG_SIDEBAR = new Color(18, 25, 50);
    private static final Color BG_CONTENT = new Color(240, 244, 252);
    private static final Color COL_BLUE = new Color(59, 130, 246);
    private static final Color COL_GREEN = new Color(16, 185, 129);
    private static final Color COL_AMBER = new Color(245, 158, 11);
    private static final Color COL_RED = new Color(239, 68, 68);
    private static final Color COL_PURPLE = new Color(139, 92, 246);
    private static final Color COL_TEAL = new Color(20, 184, 166);
    private static final Color COL_PINK = new Color(236, 72, 153);
    private static final Color COL_INDIGO = new Color(99, 102, 241);

    public AeroSysAdminUI(Employee employee, String role) {
        this.loggedInEmployee = employee;
        this.svc = new EmployeeService();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            setIconImage(new ImageIcon(getClass().getResource("/icons/logos.png")).getImage());
        } catch (Exception ignored) {
        }
        setTitle("AeroSys – " + (employee.isAdmin() ? "Admin" : "Employee")
                + " Portal  |  " + employee.getName());
        setSize(1400, 900);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        buildUI();
        startClock();
        SwingUtilities.invokeLater(() -> {
            lblWelcome.setText("  Welcome, " + employee.getName() + "  [" + employee.getRole() + "]");
            showCard("DASHBOARD");
            setActiveBtn(btnDashboard);
        });
    }

    public AeroSysAdminUI(Employee employee) {
        this(employee, employee.getRole());
    }

    private ImageIcon icon(String name, int w, int h) {
        return AeroComponents.icon(name, w, h);
    }

    private int safeInt(Object v) {
        if (v == null) {
            return 0;
        }
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String safeTs(java.sql.Timestamp ts, int maxLen) {
        if (ts == null) {
            return "";
        }
        String s = ts.toString();
        return s.substring(0, Math.min(maxLen, s.length()));
    }

    private void toast(String r) {
        AeroComponents.AeroToast.show(this, r);
    }

    private void toast(String msg, boolean ok) {
        AeroComponents.AeroToast.show(this, ok ? "SUCCESS:" + msg : "ERROR:" + msg);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ═══════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(10, 18, 40), getWidth(), 0, new Color(20, 45, 110)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(59, 130, 246, 90));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        hdr.setPreferredSize(new Dimension(0, 74));
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 26));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel(icon("flight-takeoff.png", 40, 40));
        JLabel appName = new JLabel("AeroSys");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel(loggedInEmployee.isAdmin() ? "Admin Portal" : "Employee Portal");
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appSub.setForeground(new Color(148, 163, 184));
        JPanel ts = new JPanel();
        ts.setLayout(new BoxLayout(ts, BoxLayout.Y_AXIS));
        ts.setOpaque(false);
        ts.add(appName);
        ts.add(appSub);
        left.add(logo);
        left.add(ts);
        hdr.add(left, BorderLayout.WEST);
        lblWelcome = new JLabel("Welcome");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWelcome.setForeground(new Color(148, 163, 184));
        lblWelcome.setIcon(icon("employee.png", 20, 20));
        lblWelcome.setIconTextGap(8);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        hdr.add(lblWelcome, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        right.setOpaque(false);
        JPanel clk = new JPanel();
        clk.setLayout(new BoxLayout(clk, BoxLayout.Y_AXIS));
        clk.setOpaque(false);
        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTime.setForeground(Color.WHITE);
        lblTime.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblDate = new JLabel("Loading...");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(148, 163, 184));
        lblDate.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clk.add(lblTime);
        clk.add(lblDate);
        JButton btnLogout = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Icon ic = getIcon();
                int iw = 0;
                if (ic != null) {
                    iw = ic.getIconWidth() + 6;
                    ic.paintIcon(this, g2, 10, (getHeight() - ic.getIconHeight()) / 2);
                }
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 10 + iw, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnLogout.setIcon(icon("logout.png", 20, 20));
        btnLogout.setPreferredSize(new Dimension(110, 38));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> onLogout());
        right.add(clk);
        right.add(btnLogout);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BG_SIDEBAR, 0, getHeight(), new Color(10, 16, 36)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sb.setPreferredSize(new Dimension(240, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        sb.add(secLbl("MAIN"));
        btnDashboard = sideBtn(sb, "Dashboard", "dashboard.png", COL_BLUE, "DASHBOARD");
        btnFlights = sideBtn(sb, "Flights", "flight.png", COL_TEAL, "FLIGHTS");
        btnAddFlight = sideBtn(sb, "Add Flight", "add.png", COL_GREEN, "ADD_FLIGHT");
        sb.add(Box.createRigidArea(new Dimension(0, 6)));
        sb.add(secLbl("PASSENGERS"));
        btnPassengers = sideBtn(sb, "Passengers", "passenger.png", COL_PURPLE, "PASSENGERS");
        btnRequests = sideBtn(sb, "Requests", "request.png", COL_AMBER, "REQUESTS");
        sb.add(Box.createRigidArea(new Dimension(0, 6)));
        sb.add(secLbl("OPERATIONS"));
        btnBookings = sideBtn(sb, "Bookings & Payments", "booking.png", COL_INDIGO, "BOOKINGS");
        btnTickets = sideBtn(sb, "Tickets", "ticket.png", COL_PINK, "TICKETS");
        if (loggedInEmployee.isAdmin()) {
            sb.add(Box.createRigidArea(new Dimension(0, 6)));
            sb.add(secLbl("ADMIN ONLY"));
            btnEmployees = sideBtn(sb, "Employees", "employee.png", COL_RED, "EMPLOYEES");
            btnAddEmployee = sideBtn(sb, "Add Employee", "add-user.png", COL_PURPLE, "ADD_EMPLOYEE");
        }
        sb.add(Box.createVerticalGlue());
        JLabel ver = new JLabel("AeroSys v1.0  •  CSC236");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(new Color(80, 100, 140));
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(ver);
        return sb;
    }

    private JLabel secLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(70, 95, 140));
        l.setBorder(BorderFactory.createEmptyBorder(8, 20, 3, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private AeroComponents.SidebarButton sideBtn(JPanel parent, String text, String ico, Color accent, String card) {
        AeroComponents.SidebarButton btn = new AeroComponents.SidebarButton(text, accent);
        btn.setIcon(icon(ico, 18, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            showCard(card);
            setActiveBtn(btn);
        });
        parent.add(btn);
        return btn;
    }

    private void setActiveBtn(AeroComponents.SidebarButton active) {
        AeroComponents.SidebarButton[] all = {btnDashboard, btnFlights, btnAddFlight,
            btnPassengers, btnRequests, btnBookings, btnTickets, btnEmployees, btnAddEmployee};
        for (AeroComponents.SidebarButton b : all) {
            if (b != null) {
                b.setActive(false);
            }
        }
        if (active != null) {
            active.setActive(true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CONTENT
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_CONTENT);
        contentPanel.add(buildDashboard(), "DASHBOARD");
        contentPanel.add(buildFlightsPanel(), "FLIGHTS");
        contentPanel.add(buildAddFlightPanel(), "ADD_FLIGHT");
        contentPanel.add(buildPassengersPanel(), "PASSENGERS");
        contentPanel.add(buildRequestsPanel(), "REQUESTS");
        contentPanel.add(buildBookingsPanel(), "BOOKINGS");
        contentPanel.add(buildTicketsPanel(), "TICKETS");
        if (loggedInEmployee.isAdmin()) {
            contentPanel.add(buildEmployeesPanel(), "EMPLOYEES");
            contentPanel.add(buildAddEmployeePanel(), "ADD_EMPLOYEE");
        }
        return contentPanel;
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
        switch (name) {
            case "DASHBOARD" ->
                loadDashboard();
            case "FLIGHTS" ->
                loadFlights();
            case "PASSENGERS" ->
                loadPassengers();
            case "REQUESTS" ->
                loadRequests();
            case "BOOKINGS" ->
                loadBookings();
            case "TICKETS" ->
                loadTickets();
            case "EMPLOYEES" ->
                loadEmployees();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Dashboard", "System Overview & Statistics", icon("dashboard.png", 34, 34)), BorderLayout.NORTH);
        JPanel r1 = new JPanel(new GridLayout(1, 4, 14, 0));
        r1.setOpaque(false);
        r1.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        cardFlights = new AeroComponents.StatCard("Total Flights", COL_BLUE);
        cardPassengers = new AeroComponents.StatCard("Passengers", COL_PURPLE);
        cardBookings = new AeroComponents.StatCard("Bookings", COL_INDIGO);
        cardRevenue = new AeroComponents.StatCard("Revenue ($)", COL_GREEN, true);
        cardFlights.setCardIcon(icon("flight.png", 34, 34));
        cardPassengers.setCardIcon(icon("passenger.png", 34, 34));
        cardBookings.setCardIcon(icon("booking.png", 34, 34));
        cardRevenue.setCardIcon(icon("paid.png", 34, 34));
        r1.add(cardFlights);
        r1.add(cardPassengers);
        r1.add(cardBookings);
        r1.add(cardRevenue);
        JPanel r2 = new JPanel(new GridLayout(1, 4, 14, 0));
        r2.setOpaque(false);
        r2.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        cardTickets = new AeroComponents.StatCard("Active Tickets", COL_TEAL);
        cardPending = new AeroComponents.StatCard("Pending Requests", COL_AMBER);
        cardScheduled = new AeroComponents.StatCard("Scheduled", COL_PINK);
        cardDelayed = new AeroComponents.StatCard("Delayed Flights", COL_RED);
        cardTickets.setCardIcon(icon("ticket.png", 34, 34));
        cardPending.setCardIcon(icon("request.png", 34, 34));
        cardScheduled.setCardIcon(icon("departure.png", 34, 34));
        cardDelayed.setCardIcon(icon("warning.png", 34, 34));
        r2.add(cardTickets);
        r2.add(cardPending);
        r2.add(cardScheduled);
        r2.add(cardDelayed);
        JPanel cards = new JPanel();
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(18, 26, 0, 26));
        cards.add(r1);
        cards.add(r2);
        String[] dc = {"Flight No", "From", "To", "Departure", "Available", "Price ($)", "Status"};
        dashFlightsModel = new DefaultTableModel(dc, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JScrollPane dsp = styledScroll(styledTable(dashFlightsModel));
        dsp.setPreferredSize(new Dimension(0, 190));
        JLabel rl = new JLabel("  Recent Flights");
        rl.setIcon(icon("flight.png", 20, 20));
        rl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rl.setForeground(new Color(30, 40, 80));
        rl.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        JPanel bot = new JPanel(new BorderLayout(0, 6));
        bot.setOpaque(false);
        bot.setBorder(BorderFactory.createEmptyBorder(2, 26, 22, 26));
        bot.add(rl, BorderLayout.NORTH);
        bot.add(dsp, BorderLayout.CENTER);
        JPanel ctr = new JPanel(new BorderLayout());
        ctr.setOpaque(false);
        ctr.add(cards, BorderLayout.NORTH);
        ctr.add(bot, BorderLayout.CENTER);
        p.add(ctr, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  FLIGHTS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildFlightsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Flight Management", "View, search, update status and cancel flights",
                icon("flight.png", 34, 34)), BorderLayout.NORTH);
        String[] cols = {"ID", "Flight No", "From", "To", "Departure", "Arrival", "Total Seats", "Available", "Price ($)", "Status"};
        modelFlights = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblFlights = styledTable(modelFlights);
        hideCol(tblFlights, FLT_ID_COL);
        txtSearchFlight = new AeroComponents.AeroTextField("Search flights...");
        txtSearchFlight.setPreferredSize(new Dimension(230, 35));
        txtSearchFlight.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                onSearchFlights(txtSearchFlight.getText());
            }
        });
        AeroComponents.AeroButton bStatus = new AeroComponents.AeroButton("Update Status", COL_AMBER);
        bStatus.setIcon(icon("edit.png", 17, 17));
        bStatus.addActionListener(e -> onUpdateFlightStatus());
        AeroComponents.AeroButton bCancel = new AeroComponents.AeroButton("Cancel Flight", COL_RED);
        bCancel.setIcon(icon("cancel-ticket.png", 17, 17));
        bCancel.addActionListener(e -> onCancelFlight());
        AeroComponents.AeroButton bFlightReport = new AeroComponents.AeroButton("Flight Report PDF", COL_PURPLE);
        bFlightReport.setPreferredSize(new Dimension(180, 40));
        bFlightReport.addActionListener(e -> onPrintFlightReport());   
        AeroComponents.AeroButton bRefresh = new AeroComponents.AeroButton("Refresh", COL_BLUE);
        bRefresh.setIcon(icon("refresh.png", 17, 17));
        bRefresh.addActionListener(e -> loadFlights());
        p.add(tblCard(tblFlights, new Component[]{txtSearchFlight, bStatus, bCancel, bFlightReport, bRefresh}),
                BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ADD FLIGHT
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildAddFlightPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Add New Flight", "Schedule a new flight in the system", icon("add.png", 34, 34)), BorderLayout.NORTH);
        JPanel form = formCard();
        GridBagConstraints gbc = gbc();
        txtFlightNo = new AeroComponents.AeroTextField("e.g. AE201");
        txtOrigin = new AeroComponents.AeroTextField("e.g. Riyadh");
        txtDestination = new AeroComponents.AeroTextField("e.g. Dubai");
        txtDepTime = new AeroComponents.AeroTextField("YYYY-MM-DD HH:MM:SS");
        txtArrTime = new AeroComponents.AeroTextField("YYYY-MM-DD HH:MM:SS");
        txtSeats = new AeroComponents.AeroTextField("e.g. 150");
        txtPrice = new AeroComponents.AeroTextField("e.g. 499.99");
        cmbFlightStatus = combo(new String[]{"Scheduled", "Delayed"});
        frow(form, gbc, 0, "Flight Number", icon("flight.png", 17, 17), txtFlightNo);
        frow(form, gbc, 1, "Origin", icon("location.png", 17, 17), txtOrigin);
        frow(form, gbc, 2, "Destination", icon("destination.png", 17, 17), txtDestination);
        frow(form, gbc, 3, "Departure Time", icon("departure.png", 17, 17), txtDepTime);
        frow(form, gbc, 4, "Arrival Time", icon("arrival.png", 17, 17), txtArrTime);
        frow(form, gbc, 5, "Total Seats", icon("seat.png", 17, 17), txtSeats);
        frow(form, gbc, 6, "Price per Seat", icon("price.png", 17, 17), txtPrice);
        frow(form, gbc, 7, "Status", icon("info.png", 17, 17), cmbFlightStatus);
        AeroComponents.AeroButton bSave = new AeroComponents.AeroButton("Add Flight", COL_GREEN);
        bSave.setIcon(icon("checkmark.png", 18, 18));
        bSave.setPreferredSize(new Dimension(180, 42));
        bSave.addActionListener(e -> onAddFlight());
        AeroComponents.AeroButton bClear = new AeroComponents.AeroButton("Clear", new Color(100, 120, 160));
        bClear.setPreferredSize(new Dimension(120, 42));
        bClear.addActionListener(e -> clearFlight());
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        form.add(bSave, gbc);
        gbc.gridx = 1;
        form.add(bClear, gbc);
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrap.setOpaque(false);
        wrap.add(form);
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PASSENGERS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildPassengersPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Passenger Management", "View, search and manage registered passengers", icon("passenger.png", 34, 34)), BorderLayout.NORTH);
        String[] cols = {"ID", "Name", "Email", "Phone", "Passport No", "Nationality", "Username", "Status", "Registered"};
        modelPassengers = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblPassengers = styledTable(modelPassengers);
        hideCol(tblPassengers, PAX_ID_COL);
        txtSearchPassenger = new AeroComponents.AeroTextField("Search passengers...");
        txtSearchPassenger.setPreferredSize(new Dimension(260, 40));
        txtSearchPassenger.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                onSearchPassengers(txtSearchPassenger.getText());
            }
        });
        AeroComponents.AeroButton bView = new AeroComponents.AeroButton("View Details", COL_BLUE);
        bView.setIcon(icon("view-details.png", 17, 17));
        bView.addActionListener(e -> onViewPassenger());
        AeroComponents.AeroButton bSusp = new AeroComponents.AeroButton("Suspend / Restore", COL_AMBER);
        bSusp.setIcon(icon("lock.png", 17, 17));
        bSusp.addActionListener(e -> onSuspendPassenger());
        p.add(tblCard(tblPassengers, new Component[]{txtSearchPassenger, bView, bSusp}), BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  REQUESTS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildRequestsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Passenger Requests", "Approve or reject new passenger registration requests", icon("request.png", 34, 34)), BorderLayout.NORTH);
        String[] cols = {"ID", "Name", "Email", "Phone", "Passport No", "Nationality", "Username", "Submitted", "Status"};
        modelRequests = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblRequests = styledTable(modelRequests);
        hideCol(tblRequests, REQ_ID_COL);
        txtSearchRequest = new AeroComponents.AeroTextField("Search requests...");
        txtSearchRequest.setPreferredSize(new Dimension(240, 40));
        txtSearchRequest.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                onSearchRequests(txtSearchRequest.getText());
            }
        });
        AeroComponents.AeroButton bApprove = new AeroComponents.AeroButton("Approve", COL_GREEN);
        bApprove.setIcon(icon("checkmark.png", 17, 17));
        bApprove.addActionListener(e -> onApproveRequest());
        AeroComponents.AeroButton bReject = new AeroComponents.AeroButton("Reject", COL_RED);
        bReject.setIcon(icon("close.png", 17, 17));
        bReject.addActionListener(e -> onRejectRequest());
        AeroComponents.AeroButton bRefresh = new AeroComponents.AeroButton("Refresh", COL_BLUE);
        bRefresh.setIcon(icon("refresh.png", 17, 17));
        bRefresh.addActionListener(e -> loadRequests());
        p.add(tblCard(tblRequests, new Component[]{txtSearchRequest, bApprove, bReject, bRefresh}), BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BOOKINGS & PAYMENTS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildBookingsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Bookings & Payments", "Manage all bookings and payments in one place", icon("booking.png", 24, 24)), BorderLayout.NORTH);
        String[] cols = {"Bkg ID", "Passenger", "Passport", "Flight No", "From", "To", "Departure",
            "Seats", "Total ($)", "Booking Status", "Pay Status", "Pay Method", "Paid At", "Booked At"};
        modelBookings = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblBookings = styledTable(modelBookings);
        hideCol(tblBookings, BKG_ID_COL);
        txtSearchBooking = new AeroComponents.AeroTextField("Search");
        txtSearchBooking.setPreferredSize(new Dimension(100, 40));
        txtSearchBooking.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                onSearchBookings(txtSearchBooking.getText());
            }
        });
        AeroComponents.AeroButton bConfirmPay = new AeroComponents.AeroButton("Confirm & Pay", COL_GREEN);
        bConfirmPay.setIcon(icon("paid.png", 17, 17));
        bConfirmPay.addActionListener(e -> onConfirmAndPay());
        AeroComponents.AeroButton bCancel = new AeroComponents.AeroButton("Cancel Booking", COL_RED);
        bCancel.setIcon(icon("cancel-ticket.png", 17, 17));
        bCancel.addActionListener(e -> onCancelBooking());
        AeroComponents.AeroButton bTicket = new AeroComponents.AeroButton("Generate Ticket", COL_TEAL);
        bTicket.setIcon(icon("new-ticket.png", 17, 17));
        bTicket.addActionListener(e -> onGenerateTicket());
        AeroComponents.AeroButton bPrintReceipt = new AeroComponents.AeroButton("Print Receipt", COL_PURPLE);
        bPrintReceipt.setIcon(icon("print.png", 17, 17));
        bPrintReceipt.addActionListener(e -> onPrintBookingReceipt());   // ← calls class-level method
        AeroComponents.AeroButton bRefresh = new AeroComponents.AeroButton("Refresh", COL_BLUE);
        bRefresh.setIcon(icon("refresh.png", 17, 17));
        bRefresh.addActionListener(e -> loadBookings());
        JPanel legendStrip = buildBookingLegend();
        p.add(tblCardWithBottom(tblBookings,
                new Component[]{txtSearchBooking, bConfirmPay, bCancel, bTicket, bPrintReceipt, bRefresh},
                legendStrip), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBookingLegend() {
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        l.setBackground(new Color(248, 250, 252));
        l.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        l.add(legendDot(new Color(5, 150, 105), "Confirmed / Paid / Active"));
        l.add(legendDot(new Color(217, 119, 6), "Pending / Delayed"));
        l.add(legendDot(new Color(220, 38, 38), "Cancelled / Refunded"));
        return l;
    }

    private JLabel legendDot(Color c, String text) {
        JLabel lbl = new JLabel("● " + text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(c);
        return lbl;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TICKETS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildTicketsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Ticket Management", "View, print and cancel issued tickets", icon("ticket.png", 34, 34)), BorderLayout.NORTH);
        String[] cols = {"Ticket ID", "Seat", "Passenger", "Passport", "Flight No", "From", "To", "Departure", "Issued At", "Status"};
        modelTickets = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblTickets = styledTable(modelTickets);
        txtSearchTicket = new AeroComponents.AeroTextField("Search tickets...");
        txtSearchTicket.setPreferredSize(new Dimension(260, 40));
        txtSearchTicket.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterTicketsLocally(txtSearchTicket.getText());
            }
        });
        AeroComponents.AeroButton bPrint = new AeroComponents.AeroButton("View / Print", COL_BLUE);
        bPrint.setIcon(icon("print.png", 17, 17));
        bPrint.addActionListener(e -> onPrintTicket());
        AeroComponents.AeroButton bCancel = new AeroComponents.AeroButton("Cancel Ticket", COL_RED);
        bCancel.setIcon(icon("cancel-ticket.png", 17, 17));
        bCancel.addActionListener(e -> onCancelTicket());
        AeroComponents.AeroButton bRefresh = new AeroComponents.AeroButton("Refresh", COL_INDIGO);
        bRefresh.setIcon(icon("refresh.png", 17, 17));
        bRefresh.addActionListener(e -> loadTickets());
        p.add(tblCard(tblTickets, new Component[]{txtSearchTicket, bPrint, bCancel, bRefresh}), BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  EMPLOYEES
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildEmployeesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Employees", "Manage system employee accounts (Admin only)", icon("employee.png", 34, 34)), BorderLayout.NORTH);
        String[] cols = {"ID", "Name", "Email", "Phone", "Role", "Username", "Status", "Joined"};
        modelEmployees = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblEmployees = styledTable(modelEmployees);
        hideCol(tblEmployees, EMP_ID_COL);
        txtSearchEmployee = new AeroComponents.AeroTextField("Search employees...");
        txtSearchEmployee.setPreferredSize(new Dimension(260, 40));
        txtSearchEmployee.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                onSearchEmployees(txtSearchEmployee.getText());
            }
        });
        AeroComponents.AeroButton bToggle = new AeroComponents.AeroButton("Deactivate / Restore", COL_AMBER);
        bToggle.setIcon(icon("lock.png", 17, 17));
        bToggle.addActionListener(e -> onToggleEmployee());
        AeroComponents.AeroButton bRefresh = new AeroComponents.AeroButton("Refresh", COL_BLUE);
        bRefresh.setIcon(icon("refresh.png", 17, 17));
        bRefresh.addActionListener(e -> loadEmployees());
        AeroComponents.AeroButton bAdd = new AeroComponents.AeroButton("Add Employee", COL_GREEN);
        bAdd.setIcon(icon("add-user.png", 17, 17));
        bAdd.addActionListener(e -> {
            showCard("ADD_EMPLOYEE");
            setActiveBtn(btnAddEmployee);
        });
        p.add(tblCard(tblEmployees, new Component[]{txtSearchEmployee, bToggle, bRefresh, bAdd}), BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ADD EMPLOYEE
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildAddEmployeePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CONTENT);
        p.add(pageHdr("Add New Employee", "Register a new staff account (Admin only)", icon("add-user.png", 34, 34)), BorderLayout.NORTH);
        JPanel form = formCard();
        form.setPreferredSize(new Dimension(520, 500));
        GridBagConstraints gbc = gbc();
        txtEmpName = new AeroComponents.AeroTextField("Full name");
        txtEmpEmail = new AeroComponents.AeroTextField("email@aerosys.com");
        txtEmpPhone = new AeroComponents.AeroTextField("05XXXXXXXXX");
        txtEmpUsername = new AeroComponents.AeroTextField("username");
        txtEmpPassword = new AeroComponents.AeroPasswordField();
        txtEmpConfirmPass = new AeroComponents.AeroPasswordField();
        cmbEmpRole = combo(new String[]{"EMPLOYEE", "ADMIN"});
        frow(form, gbc, 0, "Full Name", icon("user.png", 17, 17), txtEmpName);
        frow(form, gbc, 1, "Email", icon("email.png", 17, 17), txtEmpEmail);
        frow(form, gbc, 2, "Phone", icon("phone.png", 17, 17), txtEmpPhone);
        frow(form, gbc, 3, "Username", icon("username.png", 17, 17), txtEmpUsername);
        frow(form, gbc, 4, "Password", icon("password.png", 17, 17), txtEmpPassword);
        frow(form, gbc, 5, "Confirm Password", icon("password.png", 17, 17), txtEmpConfirmPass);
        frow(form, gbc, 6, "Role", icon("badge.png", 17, 17), cmbEmpRole);
        AeroComponents.AeroButton bAdd = new AeroComponents.AeroButton("Add Employee", COL_GREEN);
        bAdd.setIcon(icon("add-user.png", 18, 18));
        bAdd.setPreferredSize(new Dimension(180, 42));
        bAdd.addActionListener(e -> onAddEmployee());
        AeroComponents.AeroButton bClear = new AeroComponents.AeroButton("Clear", new Color(100, 120, 160));
        bClear.setPreferredSize(new Dimension(120, 42));
        bClear.addActionListener(e -> clearEmp());
        AeroComponents.AeroButton bView = new AeroComponents.AeroButton("View All Employees", COL_BLUE);
        bView.setPreferredSize(new Dimension(200, 42));
        bView.addActionListener(e -> {
            showCard("EMPLOYEES");
            setActiveBtn(btnEmployees);
        });
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        form.add(bAdd, gbc);
        gbc.gridx = 1;
        form.add(bClear, gbc);
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel lr = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lr.setOpaque(false);
        lr.add(bView);
        form.add(lr, gbc);
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 26));
        wrap.setOpaque(false);
        wrap.add(form);
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel pageHdr(String title, String sub, Icon ic) {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(226, 232, 240));
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createEmptyBorder(18, 26, 16, 26));
        JLabel t = new JLabel("  " + title);
        t.setIcon(ic);
        t.setFont(new Font("Segoe UI", Font.BOLD, 24));
        t.setForeground(new Color(15, 23, 42));
        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(new Color(100, 116, 139));
        s.setBorder(BorderFactory.createEmptyBorder(3, 4, 0, 0));
        JPanel tp = new JPanel();
        tp.setOpaque(false);
        tp.setLayout(new BoxLayout(tp, BoxLayout.Y_AXIS));
        tp.add(t);
        tp.add(s);
        h.add(tp, BorderLayout.WEST);
        return h;
    }

    private JPanel tblCard(JTable tbl, Component[] items) {
        return tblCardWithBottom(tbl, items, null);
    }

    private JPanel tblCardWithBottom(JTable tbl, Component[] items, JPanel bottom) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tb.setOpaque(false);
        tb.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        for (Component c : items) {
            tb.add(c);
        }
        card.add(tb, BorderLayout.NORTH);
        card.add(styledScroll(tbl), BorderLayout.CENTER);
        if (bottom != null) {
            card.add(bottom, BorderLayout.SOUTH);
        }
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_CONTENT);
        wrap.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel formCard() {
        JPanel c = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));
        c.setPreferredSize(new Dimension(500, 540));
        return c;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 7, 7, 7);
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private void frow(JPanel form, GridBagConstraints gbc, int row, String label, Icon ic, Component field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel l = new JLabel("  " + label);
        l.setIcon(ic);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(44, 62, 80));
        l.setPreferredSize(new Dimension(170, 40));
        form.add(l, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(42);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(new Color(30, 64, 175));
        t.setGridColor(new Color(241, 245, 249));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setBackground(Color.WHITE);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JTableHeader hdr = t.getTableHeader();
        hdr.setBackground(new Color(248, 250, 252));
        hdr.setForeground(new Color(71, 85, 105));
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdr.setPreferredSize(new Dimension(0, 44));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)));
        hdr.setReorderingAllowed(false);
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(new Color(30, 41, 59));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(CENTER);
                if (v != null && !sel) {
                    String s = v.toString();
                    if (s.equalsIgnoreCase("Confirmed") || s.equalsIgnoreCase("Paid") || s.equalsIgnoreCase("Active") || s.equalsIgnoreCase("Scheduled")) {
                        c.setForeground(new Color(5, 150, 105));
                    } else if (s.equalsIgnoreCase("Pending") || s.equalsIgnoreCase("Delayed") || s.equalsIgnoreCase("Not Set")) {
                        c.setForeground(new Color(217, 119, 6));
                    } else if (s.equalsIgnoreCase("Cancelled") || s.equalsIgnoreCase("Refunded") || s.equalsIgnoreCase("Suspended") || s.equalsIgnoreCase("Deactivated")) {
                        c.setForeground(new Color(220, 38, 38));
                    }
                }
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(cr);
        }
        t.setFillsViewportHeight(true);
        return t;
    }

    private JScrollPane styledScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(200, 40));
        return cb;
    }

    private void hideCol(JTable t, int col) {
        TableColumn c = t.getColumnModel().getColumn(col);
        c.setMinWidth(0);
        c.setMaxWidth(0);
        c.setWidth(0);
        c.setPreferredWidth(0);
    }

    private void startClock() {
        new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy")));
        }).start();
    }

    private void clearFlight() {
        txtFlightNo.setText("");
        txtOrigin.setText("");
        txtDestination.setText("");
        txtDepTime.setText("");
        txtArrTime.setText("");
        txtSeats.setText("");
        txtPrice.setText("");
        cmbFlightStatus.setSelectedIndex(0);
    }

    private void clearEmp() {
        if (txtEmpName != null) {
            txtEmpName.setText("");
        }
        if (txtEmpEmail != null) {
            txtEmpEmail.setText("");
        }
        if (txtEmpPhone != null) {
            txtEmpPhone.setText("");
        }
        if (txtEmpUsername != null) {
            txtEmpUsername.setText("");
        }
        if (txtEmpPassword != null) {
            txtEmpPassword.setText("");
        }
        if (txtEmpConfirmPass != null) {
            txtEmpConfirmPass.setText("");
        }
        if (cmbEmpRole != null) {
            cmbEmpRole.setSelectedIndex(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LOGOUT
    // ═══════════════════════════════════════════════════════════════════
    private void onLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            Main.AirportSystem.getInstance().onLogout();
            new AeroSysLoginUI().setVisible(true);
            dispose();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LOAD METHODS
    // ═══════════════════════════════════════════════════════════════════
    private void loadDashboard() {
        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() {
                return svc.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    DashboardStats s = get();
                    if (s == null || !s.success) {
                        toast(s != null && s.errorMessage != null ? s.errorMessage : "Cannot load dashboard.", false);
                        return;
                    }
                    cardFlights.animateTo(s.totalFlights);
                    cardPassengers.animateTo(s.totalPassengers);
                    cardBookings.animateTo(s.totalBookings);
                    cardRevenue.animateTo((int) s.totalRevenue);
                    cardTickets.animateTo(s.activeTickets);
                    cardPending.animateTo(s.pendingRequests);
                    cardScheduled.animateTo(s.scheduledFlights);
                    cardDelayed.animateTo(s.delayedFlights);
                    dashFlightsModel.setRowCount(0);
                    int n = 0;
                    for (Flight f : svc.getAllFlights()) {
                        dashFlightsModel.addRow(new Object[]{f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                            f.getDepartureTime(), f.getAvailableSeats(), String.format("$%.2f", f.getPrice()), f.getStatus()});
                        if (++n >= 7) {
                            break;
                        }
                    }
                } catch (Exception ex) {
                    toast("Dashboard error: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void loadFlights() {
        new SwingWorker<List<Flight>, Void>() {
            @Override
            protected List<Flight> doInBackground() {
                return svc.getAllFlights();
            }

            @Override
            protected void done() {
                try {
                    modelFlights.setRowCount(0);
                    for (Flight f : get()) {
                        modelFlights.addRow(new Object[]{
                            f.getFlightId(), f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                            f.getDepartureTime(), f.getArrivalTime(), f.getTotalSeats(), f.getAvailableSeats(),
                            String.format("%.2f", f.getPrice()), f.getStatus(),
                            f.getManagedBy() == 0 ? "System" : String.valueOf(f.getManagedBy())});
                    }
                } catch (Exception ex) {
                    toast("Cannot load flights: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void loadPassengers() {
        new SwingWorker<List<Passenger>, Void>() {
            @Override
            protected List<Passenger> doInBackground() {
                return svc.getAllActivePassengers();
            }

            @Override
            protected void done() {
                try {
                    modelPassengers.setRowCount(0);
                    for (Passenger px : get()) {
                        modelPassengers.addRow(new Object[]{
                            px.getPassengerId(), px.getName(), px.getEmail(), px.getPhone(),
                            px.getPassportNo(), px.getNationality(), px.getUsername(),
                            px.getAccountStatus(), safeTs(px.getCreatedAt(), 10)});
                    }
                } catch (Exception ex) {
                    toast("Cannot load passengers: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void loadRequests() {
        new SwingWorker<List<PassengerRequest>, Void>() {
            @Override
            protected List<PassengerRequest> doInBackground() {
                return svc.getPendingRequests();
            }

            @Override
            protected void done() {
                try {
                    modelRequests.setRowCount(0);
                    for (PassengerRequest r : get()) {
                        String d = r.getRequestDate() != null ? r.getRequestDate().toString().substring(0, Math.min(16, r.getRequestDate().toString().length())) : "";
                        modelRequests.addRow(new Object[]{r.getRequestId(), r.getName(), r.getEmail(), r.getPhone(), r.getPassportNo(), r.getNationality(), r.getUsername(), d, r.getStatus()});
                    }
                } catch (Exception ex) {
                    toast("Cannot load requests: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void loadBookings() {
        new SwingWorker<Void, Void>() {
            Object[][] rows = new Object[0][0];
            int[] paxIds = new int[0];
            int[] fltIds = new int[0];
            int[] payIds = new int[0];

            @Override
            protected Void doInBackground() {
                String sql = "SELECT booking_id, passenger_id, passenger_name, passport_no, "
                        + "flight_id, flight_number, origin, destination, departure_time, "
                        + "seat_count, total_price, booking_status, pay_status, pay_method, "
                        + "paid_at, booked_at, payment_id FROM vw_bookings_full";
                try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    java.util.ArrayList<Object[]> list = new java.util.ArrayList<>();
                    java.util.ArrayList<Integer> pList = new java.util.ArrayList<>();
                    java.util.ArrayList<Integer> fList = new java.util.ArrayList<>();
                    java.util.ArrayList<Integer> yList = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Timestamp dep = rs.getTimestamp("departure_time");
                        Timestamp paid = rs.getTimestamp("paid_at");
                        Timestamp bkAt = rs.getTimestamp("booked_at");
                        list.add(new Object[]{rs.getInt("booking_id"), rs.getString("passenger_name"), rs.getString("passport_no"),
                            rs.getString("flight_number"), rs.getString("origin"), rs.getString("destination"),
                            dep != null ? dep.toString().substring(0, Math.min(16, dep.toString().length())) : "",
                            rs.getInt("seat_count"), String.format("%.2f", rs.getDouble("total_price")),
                            rs.getString("booking_status"), rs.getString("pay_status"), rs.getString("pay_method"),
                            paid != null ? paid.toString().substring(0, Math.min(16, paid.toString().length())) : "—",
                            bkAt != null ? bkAt.toString().substring(0, Math.min(10, bkAt.toString().length())) : ""});
                        pList.add(rs.getInt("passenger_id"));
                        fList.add(rs.getInt("flight_id"));
                        yList.add(rs.getInt("payment_id"));
                    }
                    rows = list.toArray(new Object[0][0]);
                    paxIds = pList.stream().mapToInt(i -> i).toArray();
                    fltIds = fList.stream().mapToInt(i -> i).toArray();
                    payIds = yList.stream().mapToInt(i -> i).toArray();
                } catch (SQLException e) {
                    System.err.println("[AdminUI.loadBookings] " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                modelBookings.setRowCount(0);
                bkgPassengerIds = paxIds;
                bkgFlightIds = fltIds;
                bkgPaymentIds = payIds;
                for (Object[] row : rows) {
                    modelBookings.addRow(row);
                }
            }
        }.execute();
    }

    private void loadTickets() {
        new SwingWorker<Void, Void>() {
            Object[][] rows = new Object[0][0];

            @Override
            protected Void doInBackground() {
                String sql = "SELECT ticket_id, seat_number, passenger_name, passport_no, "
                        + "flight_number, origin, destination, departure_time, issued_at, ticket_status "
                        + "FROM vw_all_tickets";
                try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    java.util.ArrayList<Object[]> list = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Timestamp dep = rs.getTimestamp("departure_time");
                        Timestamp iss = rs.getTimestamp("issued_at");
                        list.add(new Object[]{rs.getInt("ticket_id"), rs.getString("seat_number"),
                            rs.getString("passenger_name"), rs.getString("passport_no"), rs.getString("flight_number"),
                            rs.getString("origin"), rs.getString("destination"),
                            dep != null ? dep.toString().substring(0, Math.min(16, dep.toString().length())) : "",
                            iss != null ? iss.toString().substring(0, Math.min(16, iss.toString().length())) : "",
                            rs.getString("ticket_status")});
                    }
                    rows = list.toArray(new Object[0][0]);
                } catch (SQLException e) {
                    System.err.println("[AdminUI.loadTickets] " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                modelTickets.setRowCount(0);
                for (Object[] row : rows) {
                    modelTickets.addRow(row);
                }
            }
        }.execute();
    }

    private void loadEmployees() {
        new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() {
                return svc.getAllEmployees();
            }

            @Override
            protected void done() {
                try {
                    modelEmployees.setRowCount(0);
                    for (Employee e : get()) {
                        modelEmployees.addRow(new Object[]{
                            e.getEmployeeId(), e.getName(), e.getEmail(), e.getPhone(), e.getRole(), e.getUsername(),
                            e.getAccountStatus(), safeTs(e.getCreatedAt(), 10)});
                    }
                } catch (Exception ex) {
                    toast("Cannot load employees: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  FLIGHTS EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════
    private void onSearchFlights(String q) {
        new SwingWorker<List<Flight>, Void>() {
            @Override
            protected List<Flight> doInBackground() {
                return svc.searchFlights(q);
            }

            @Override
            protected void done() {
                try {
                    modelFlights.setRowCount(0);
                    for (Flight f : get()) {
                        modelFlights.addRow(new Object[]{
                            f.getFlightId(), f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                            f.getDepartureTime(), f.getArrivalTime(), f.getTotalSeats(), f.getAvailableSeats(),
                            String.format("%.2f", f.getPrice()), f.getStatus(),
                            f.getManagedBy() == 0 ? "System" : String.valueOf(f.getManagedBy())});
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }.execute();
    }

    private void onAddFlight() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.addFlight(txtFlightNo.getText(), txtOrigin.getText(), txtDestination.getText(),
                        txtDepTime.getText(), txtArrTime.getText(), txtSeats.getText(), txtPrice.getText(),
                        (String) cmbFlightStatus.getSelectedItem());
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        clearFlight();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onUpdateFlightStatus() {
        int row = tblFlights.getSelectedRow();
        if (row == -1) {
            toast("Select a flight first.", false);
            return;
        }
        int fid = safeInt(modelFlights.getValueAt(row, FLT_ID_COL));
        String fno = modelFlights.getValueAt(row, 1).toString();
        String[] opts = {"Scheduled", "Delayed", "Completed"};
        String ch = (String) JOptionPane.showInputDialog(this, "New status for flight " + fno + ":", "Update Status",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (ch == null) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.updateFlightStatus(fid, ch);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadFlights();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onCancelFlight() {
        int row = tblFlights.getSelectedRow();
        if (row == -1) {
            toast("Select a flight to cancel.", false);
            return;
        }
        int fid = safeInt(modelFlights.getValueAt(row, FLT_ID_COL));
        String fno = modelFlights.getValueAt(row, 1).toString();
        String reason = JOptionPane.showInputDialog(this, "Reason for cancelling flight " + fno + ":", "Cancel Flight", JOptionPane.PLAIN_MESSAGE);
        if (reason == null) {
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Cancel flight " + fno + "?\nReason: " + reason, "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.cancelFlight(fid, reason);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadFlights();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    // ─── PRINT FLIGHT REPORT ─────────────────────────────────────────
    // This is a proper class-level private method — NOT nested inside any SwingWorker
    private void onPrintFlightReport() {
        if (modelFlights == null || modelFlights.getRowCount() == 0) {
            toast("No flights loaded.", false);
            return;
        }
        FlightReportPrinter.fromFlightsTable(this, modelFlights, loggedInEmployee.getName());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PASSENGERS EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════
    private void onSearchPassengers(String q) {
        new SwingWorker<List<Passenger>, Void>() {
            @Override
            protected List<Passenger> doInBackground() {
                return svc.searchPassengers(q);
            }

            @Override
            protected void done() {
                try {
                    modelPassengers.setRowCount(0);
                    for (Passenger px : get()) {
                        modelPassengers.addRow(new Object[]{
                            px.getPassengerId(), px.getName(), px.getEmail(), px.getPhone(),
                            px.getPassportNo(), px.getNationality(), px.getUsername(),
                            px.getAccountStatus(), safeTs(px.getCreatedAt(), 10)});
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }.execute();
    }

    private void onViewPassenger() {
        int row = tblPassengers.getSelectedRow();
        if (row == -1) {
            toast("Select a passenger to view.", false);
            return;
        }
        String[] lb = {"Name", "Email", "Phone", "Passport No", "Nationality", "Username", "Status", "Registered"};
        StringBuilder sb = new StringBuilder("<html><table cellpadding='7'>");
        for (int i = 0; i < lb.length; i++) {
            sb.append("<tr><td><b>").append(lb[i]).append("</b></td><td>").append(modelPassengers.getValueAt(row, i + 1)).append("</td></tr>");
        }
        sb.append("</table></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Passenger Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSuspendPassenger() {
        int row = tblPassengers.getSelectedRow();
        if (row == -1) {
            toast("Select a passenger first.", false);
            return;
        }
        int pid = safeInt(modelPassengers.getValueAt(row, PAX_ID_COL));
        String name = modelPassengers.getValueAt(row, 1).toString();
        String status = modelPassengers.getValueAt(row, PAX_STATUS_COL).toString();
        String action = "Suspended".equalsIgnoreCase(status) ? "Restore" : "Suspend";
        int c = JOptionPane.showConfirmDialog(this, action + " account of " + name + "?", "Confirm " + action, JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return "Suspended".equalsIgnoreCase(status) ? svc.unsuspendPassenger(pid) : svc.suspendPassenger(pid);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadPassengers();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  REQUESTS EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════
    private void onSearchRequests(String q) {
        if (q == null || q.isBlank()) {
            loadRequests();
            return;
        }
        new SwingWorker<List<PassengerRequest>, Void>() {
            @Override
            protected List<PassengerRequest> doInBackground() {
                return svc.getAllRequests();
            }

            @Override
            protected void done() {
                try {
                    modelRequests.setRowCount(0);
                    String lq = q.toLowerCase();
                    for (PassengerRequest r : get()) {
                        if (r.getName().toLowerCase().contains(lq) || r.getEmail().toLowerCase().contains(lq)
                                || r.getPassportNo().toLowerCase().contains(lq) || r.getStatus().toLowerCase().contains(lq)) {
                            String d = r.getRequestDate() != null ? r.getRequestDate().toString().substring(0, Math.min(16, r.getRequestDate().toString().length())) : "";
                            modelRequests.addRow(new Object[]{r.getRequestId(), r.getName(), r.getEmail(), r.getPhone(), r.getPassportNo(), r.getNationality(), r.getUsername(), d, r.getStatus()});
                        }
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }.execute();
    }

    private void onApproveRequest() {
        int row = tblRequests.getSelectedRow();
        if (row == -1) {
            toast("Select a request first.", false);
            return;
        }
        int rid = safeInt(modelRequests.getValueAt(row, REQ_ID_COL));
        String name = modelRequests.getValueAt(row, 1).toString();
        String status = modelRequests.getValueAt(row, REQ_STATUS_COL).toString();
        if (!"Pending".equalsIgnoreCase(status)) {
            toast("Only Pending requests can be approved.", false);
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Approve registration for " + name + "?\nThis creates their passenger account.", "Confirm Approval", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.approveRequest(rid);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadRequests();
                        loadPassengers();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onRejectRequest() {
        int row = tblRequests.getSelectedRow();
        if (row == -1) {
            toast("Select a request first.", false);
            return;
        }
        int rid = safeInt(modelRequests.getValueAt(row, REQ_ID_COL));
        String name = modelRequests.getValueAt(row, 1).toString();
        String status = modelRequests.getValueAt(row, REQ_STATUS_COL).toString();
        if (!"Pending".equalsIgnoreCase(status)) {
            toast("Only Pending requests can be rejected.", false);
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Reject request from " + name + "?", "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.rejectRequest(rid);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadRequests();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BOOKINGS EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════
    private void onSearchBookings(String q) {
        if (q == null || q.isBlank()) {
            loadBookings();
            return;
        }
        String lq = q.toLowerCase();
        for (int i = modelBookings.getRowCount() - 1; i >= 0; i--) {
            boolean match = false;
            for (int j = 1; j < modelBookings.getColumnCount(); j++) {
                Object v = modelBookings.getValueAt(i, j);
                if (v != null && v.toString().toLowerCase().contains(lq)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                modelBookings.removeRow(i);
            }
        }
    }

    private void onConfirmAndPay() {
        int viewRow = tblBookings.getSelectedRow();
        if (viewRow == -1) {
            toast("Select a booking row first.", false);
            return;
        }
        int modelRow = tblBookings.convertRowIndexToModel(viewRow);
        int bid = safeInt(modelBookings.getValueAt(modelRow, BKG_ID_COL));
        String bStatus = modelBookings.getValueAt(modelRow, BKG_STATUS_COL).toString();
        String pStatus = modelBookings.getValueAt(modelRow, BKG_PAY_STATUS_COL).toString();
        String passenger = modelBookings.getValueAt(modelRow, 1).toString();
        String amount = modelBookings.getValueAt(modelRow, 8).toString();
        if ("Cancelled".equalsIgnoreCase(bStatus)) {
            toast("Cannot pay for a cancelled booking.", false);
            return;
        }
        if ("Paid".equalsIgnoreCase(pStatus)) {
            toast("Payment for booking #" + bid + " is already completed.", false);
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Confirm and pay booking #" + bid + " for " + passenger + "?\nAmount: $" + amount + " — Method: Online", "Confirm & Pay", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                if ("Pending".equalsIgnoreCase(bStatus)) {
                    String cr = svc.confirmBooking(bid);
                    if (cr.startsWith("ERROR:")) {
                        return cr;
                    }
                }
                return svc.recordPayment(bid, amount, "Online");
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadBookings();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onCancelBooking() {
        int viewRow = tblBookings.getSelectedRow();
        if (viewRow == -1) {
            toast("Select a booking first.", false);
            return;
        }
        int modelRow = tblBookings.convertRowIndexToModel(viewRow);
        int bid = safeInt(modelBookings.getValueAt(modelRow, BKG_ID_COL));
        String bStatus = modelBookings.getValueAt(modelRow, BKG_STATUS_COL).toString();
        if ("Cancelled".equalsIgnoreCase(bStatus)) {
            toast("Booking #" + bid + " is already cancelled.", false);
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Cancel booking #" + bid + "?\nSeats will be restored and tickets cancelled.", "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.cancelBooking(bid);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadBookings();
                        loadTickets();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onGenerateTicket() {
    // ── 1. Selection & basic validation ───────────────────────────
    int viewRow = tblBookings.getSelectedRow();
    if (viewRow == -1) {
        toast("Select a booking first.", false);
        return;
    }
    int modelRow = tblBookings.convertRowIndexToModel(viewRow);

    int    bid       = safeInt(modelBookings.getValueAt(modelRow, BKG_ID_COL));
    int    pid       = bkgPassengerIds.length > modelRow ? bkgPassengerIds[modelRow] : 0;
    int    fid       = bkgFlightIds.length    > modelRow ? bkgFlightIds[modelRow]    : 0;
    int    seatCount = safeInt(modelBookings.getValueAt(modelRow, 7)); // col 7 = Seats
    String bStatus   = modelBookings.getValueAt(modelRow, BKG_STATUS_COL).toString();
    String pStatus   = modelBookings.getValueAt(modelRow, BKG_PAY_STATUS_COL).toString();

    if (!"Confirmed".equalsIgnoreCase(bStatus)) {
        toast("Booking must be Confirmed before generating tickets.", false);
        return;
    }
    if (!"Paid".equalsIgnoreCase(pStatus)) {
        toast("Payment must be completed before generating tickets.", false);
        return;
    }
    if (pid == 0 || fid == 0) {
        toast("Cannot resolve passenger/flight. Refresh and try again.", false);
        return;
    }
    if (seatCount < 1) {
        toast("Booking has invalid seat count. Refresh and try again.", false);
        return;
    }

    // ── 2. Check how many tickets are already issued ───────────────
    // Done on EDT before spawning worker — count is fast (PK lookup).
    int alreadyIssued = svc.countIssuedTickets(bid);
    if (alreadyIssued < 0) {
        toast("Could not verify existing tickets. Refresh and try again.", false);
        return;
    }
    int remaining = seatCount - alreadyIssued;
    if (remaining <= 0) {
        toast("All " + seatCount + " ticket(s) for booking #" + bid
                + " are already issued.", false);
        return;
    }

    // ── 3. Collect one seat number per remaining ticket ────────────
    // All prompts happen on the EDT before any DB work starts.
    java.util.List<String> seats = new java.util.ArrayList<>();
    for (int i = 1; i <= remaining; i++) {
        String prompt = "<html>"
                + "<b>Booking #" + bid + " — Passenger: "
                + modelBookings.getValueAt(modelRow, 1) + "</b><br><br>"
                + "Total seats booked: <b>" + seatCount + "</b><br>"
                + "Already issued:     <b>" + alreadyIssued + "</b><br>"
                + "Remaining:          <b>" + remaining + "</b><br><br>"
                + "Enter seat number for ticket <b>" + i + " of " + remaining + "</b>"
                + " (e.g. 12A, 5B, 22C):"
                + "</html>";

        String seat = JOptionPane.showInputDialog(
                this, prompt,
                "Generate Ticket " + i + " of " + remaining,
                JOptionPane.PLAIN_MESSAGE);

        // User cancelled mid-way
        if (seat == null || seat.trim().isEmpty()) {
            if (i > 1) {
                toast("Cancelled after " + (i - 1) + " seat(s) entered. "
                        + "Those will NOT be saved.", false);
            }
            return;
        }

        String trimmed = seat.trim().toUpperCase();

        // Format check: 1–3 digits followed by exactly one letter (e.g. 12A)
        if (!trimmed.matches("[0-9]{1,3}[A-Za-z]")) {
            toast("ERROR:Invalid seat format '" + trimmed
                    + "'. Use format like 12A, 5B, 22C. Generation cancelled.", false);
            return;
        }

        // Duplicate check within this batch
        if (seats.contains(trimmed)) {
            toast("ERROR:Seat " + trimmed + " entered twice in this batch. "
                    + "Generation cancelled.", false);
            return;
        }

        seats.add(trimmed);
    }

    // ── 4. Insert all tickets on background thread ─────────────────
    final int finalPid = pid;
    final int finalFid = fid;

    new SwingWorker<java.util.List<String>, Void>() {

        @Override
        protected java.util.List<String> doInBackground() {
            java.util.List<String> results = new java.util.ArrayList<>();
            for (String s : seats) {
                String r = svc.generateTicket(bid, finalPid, finalFid, s);
                results.add("Seat " + s + ":  " + r);
                // Stop immediately on first DB-level failure
                if (r.startsWith("ERROR:")) break;
            }
            return results;
        }

        @Override
        protected void done() {
            try {
                java.util.List<String> results = get();

                boolean anyError = results.stream()
                        .anyMatch(r -> r.contains("ERROR:"));

                // Build a clear per-seat summary dialog
                StringBuilder sb = new StringBuilder(
                        "<html><b>Ticket Generation Summary — Booking #" + bid
                        + "</b><br><br>");
                for (String line : results) {
                    boolean ok = !line.contains("ERROR:");
                    sb.append("<font color='")
                      .append(ok ? "#059669" : "#dc2626")
                      .append("'>")
                      .append(line.replace("ERROR:", "✗ ").replace("SUCCESS:", "✓ "))
                      .append("</font><br>");
                }
                sb.append("</html>");

                JOptionPane.showMessageDialog(
                        AeroSysAdminUI.this,
                        sb.toString(),
                        "Ticket Generation Results",
                        anyError
                            ? JOptionPane.ERROR_MESSAGE
                            : JOptionPane.INFORMATION_MESSAGE);

                // Always refresh both views so state is consistent
                loadTickets();
                loadBookings();

            } catch (Exception ex) {
                toast("ERROR:Unexpected error: " + ex.getMessage());
            }
        }
    }.execute();
}
    // ─── PRINT BOOKING RECEIPT ────────────────────────────────────────
    // This is a proper class-level private method — NOT nested inside any SwingWorker
    private void onPrintBookingReceipt() {
        int viewRow = tblBookings.getSelectedRow();
        if (viewRow == -1) {
            toast("Select a booking row to print.", false);
            return;
        }
        int modelRow = tblBookings.convertRowIndexToModel(viewRow);
        BookingPaymentPrinter.fromAdminTable(this, modelBookings, modelRow);
        toast("Receipt for booking #" + modelBookings.getValueAt(modelRow, BKG_ID_COL) + " opened.", true);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TICKETS EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════
    private void filterTicketsLocally(String q) {
        if (q == null || q.isBlank()) {
            loadTickets();
            return;
        }
        String lq = q.toLowerCase();
        for (int i = modelTickets.getRowCount() - 1; i >= 0; i--) {
            boolean match = false;
            for (int j = 0; j < modelTickets.getColumnCount(); j++) {
                Object v = modelTickets.getValueAt(i, j);
                if (v != null && v.toString().toLowerCase().contains(lq)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                modelTickets.removeRow(i);
            }
        }
    }

    private void onPrintTicket() {
        int row = tblTickets.getSelectedRow();
        if (row == -1) {
            toast("Select a ticket to view.", false);
            return;
        }
        TicketPrinter.fromAdminTable(this, modelTickets, row);
        toast("Ticket #" + modelTickets.getValueAt(row, 0) + " opened for printing.", true);
    }

    private void onCancelTicket() {
        int row = tblTickets.getSelectedRow();
        if (row == -1) {
            toast("Select a ticket.", false);
            return;
        }
        int tid = safeInt(modelTickets.getValueAt(row, TKT_ID_COL));
        int c = JOptionPane.showConfirmDialog(this, "Cancel ticket #" + tid + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.cancelTicket(tid);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadTickets();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  EMPLOYEES EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════
    private void onSearchEmployees(String q) {
        new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() {
                return svc.searchEmployees(q);
            }

            @Override
            protected void done() {
                try {
                    modelEmployees.setRowCount(0);
                    for (Employee e : get()) {
                        modelEmployees.addRow(new Object[]{
                            e.getEmployeeId(), e.getName(), e.getEmail(), e.getPhone(), e.getRole(), e.getUsername(),
                            e.getAccountStatus(), safeTs(e.getCreatedAt(), 10)});
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }.execute();
    }

    private void onToggleEmployee() {
        if (tblEmployees == null) {
            return;
        }
        int row = tblEmployees.getSelectedRow();
        if (row == -1) {
            toast("Select an employee.", false);
            return;
        }
        int eid = safeInt(modelEmployees.getValueAt(row, EMP_ID_COL));
        String name = modelEmployees.getValueAt(row, 1).toString();
        String status = modelEmployees.getValueAt(row, EMP_STATUS_COL).toString();
        String action = "Deactivated".equalsIgnoreCase(status) ? "Reactivate" : "Deactivate";
        int c = JOptionPane.showConfirmDialog(this, action + " account of " + name + "?", "Confirm " + action, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return "Deactivated".equalsIgnoreCase(status) ? svc.reactivateEmployee(eid) : svc.deactivateEmployee(eid);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadEmployees();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onAddEmployee() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return svc.addEmployee(txtEmpName.getText(), txtEmpEmail.getText(), txtEmpPhone.getText(),
                        (String) cmbEmpRole.getSelectedItem(), txtEmpUsername.getText(),
                        new String(txtEmpPassword.getPassword()), new String(txtEmpConfirmPass.getPassword()));
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        clearEmp();
                        loadEmployees();
                    }
                } catch (Exception ex) {
                    toast("ERROR:" + ex.getMessage());
                }
            }
        }.execute();
    }
}