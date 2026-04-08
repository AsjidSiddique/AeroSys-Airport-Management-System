package Gui;

import Main.AirportSystem;
import dao.PassengerDAO;
import model.*;
import service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AeroSysPassengerUI extends JFrame {

    // ── Services ──────────────────────────────────────────────────────
    private Passenger loggedInPassenger;
    private final SessionManager session = SessionManager.getInstance();
    private final FlightService flightSvc = new FlightService();
    private final BookingService bookingSvc = new BookingService();
    private final PaymentService paymentSvc = new PaymentService();
    private final TicketService ticketSvc = new TicketService();
    private final PassengerDAO passengerDAO = new PassengerDAO();

    // ── Layout ────────────────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel contentPanel;

    // ── Header ────────────────────────────────────────────────────────
    private JLabel lblTime, lblDate, lblWelcome;

    // ── Sidebar ───────────────────────────────────────────────────────
    private AeroComponents.SidebarButton btnDashboard, btnSearch,
            btnMyBookings, btnMyTickets, btnMyProfile;
    private JLabel sidebarNameLbl;

    // ── Dashboard stat cards ──────────────────────────────────────────
    private AeroComponents.StatCard cardAvailFlights, cardMyBookings,
            cardMyTickets, cardSpent;

    // ── Search panel ──────────────────────────────────────────────────
    private AeroComponents.AeroTextField txtSearchOrigin, txtSearchDest;
    private AeroComponents.AeroDatePicker dpSearchDate;
    private JPanel flightResultsPanel;

    // ── My Bookings ───────────────────────────────────────────────────
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private AeroComponents.AeroTextField txtFilterBooking;

    // ── My Tickets ────────────────────────────────────────────────────
    private JTable tblTickets;
    private DefaultTableModel modelTickets;
    private AeroComponents.AeroTextField txtFilterTicket;

    // ── Profile ───────────────────────────────────────────────────────
    private JLabel lblProfName, lblProfEmail, lblProfPhone,
            lblProfPassport, lblProfNat, lblProfStatus,
            lblProfJoined, lblProfUsername;
    private AeroComponents.AeroTextField fldName, fldEmail, fldPhone;
    private JPanel profileViewPanel, profileEditPanel, profileSwitchPanel;
    private CardLayout profileCardLayout;

    // ── Table row caches for client-side filter ───────────────────────
    private List<Object[]> allBookingRows = new java.util.ArrayList<>();
    private List<Object[]> allTicketRows = new java.util.ArrayList<>();

    // ── Colors ────────────────────────────────────────────────────────
    private static final Color BG_CONTENT = new Color(238, 242, 252);
    private static final Color BG_SIDEBAR = new Color(14, 22, 52);
    private static final Color COL_BLUE = new Color(59, 130, 246);
    private static final Color COL_GREEN = new Color(16, 185, 129);
    private static final Color COL_AMBER = new Color(245, 158, 11);
    private static final Color COL_PURPLE = new Color(139, 92, 246);
    private static final Color COL_TEAL = new Color(20, 184, 166);
    private static final Color COL_PINK = new Color(236, 72, 153);

    // ═════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═════════════════════════════════════════════════════════════════
    public AeroSysPassengerUI(Passenger passenger) {
        this.loggedInPassenger = passenger;
        setIconImage(new ImageIcon(getClass().getResource("/icons/logos.png")).getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("AeroSys – Passenger Portal  |  " + passenger.getName());
        setSize(1350, 880);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        buildUI();
        startClock();
        setPassenger(passenger);
        SwingUtilities.invokeLater(() -> {
            showCard("DASHBOARD");
            setActive(btnDashboard);
        });
    }

    // ── Icon helper ───────────────────────────────────────────────────
    private ImageIcon icon(String name, int w, int h) {
        return AeroComponents.icon(name, w, h);
    }

    // ── Toast helper — AeroToast centered over this window ────────────
    private void toast(String msg, boolean ok) {
        AeroComponents.AeroToast.show(this, ok ? "SUCCESS:" + msg : "ERROR:" + msg);
    }

    private void toast(String resultStr) {
        AeroComponents.AeroToast.show(this, resultStr);
    }

    // ═════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ═════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════
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
        hdr.setPreferredSize(new Dimension(0, 76));
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel(icon("flight-takeoff.png", 42, 42));
        JLabel title = new JLabel("AeroSys");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Passenger Portal");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));
        JPanel ts = new JPanel();
        ts.setLayout(new BoxLayout(ts, BoxLayout.Y_AXIS));
        ts.setOpaque(false);
        ts.add(title);
        ts.add(sub);
        left.add(logo);
        left.add(ts);
        hdr.add(left, BorderLayout.WEST);

        lblWelcome = new JLabel("Welcome, Passenger");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblWelcome.setForeground(new Color(148, 163, 184));
        ImageIcon pIco = icon("passenger.png", 22, 22);
        if (pIco == null) {
            pIco = icon("user.png", 22, 22);
        }
        lblWelcome.setIcon(pIco);
        lblWelcome.setIconTextGap(8);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        hdr.add(lblWelcome, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 22, 15));
        right.setOpaque(false);
        JPanel clock = new JPanel();
        clock.setLayout(new BoxLayout(clock, BoxLayout.Y_AXIS));
        clock.setOpaque(false);
        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTime.setForeground(Color.WHITE);
        lblTime.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblDate = new JLabel("Loading...");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(148, 163, 184));
        lblDate.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clock.add(lblTime);
        clock.add(lblDate);

        JButton btnLogout = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);
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
        btnLogout.setIcon(icon("logout.png", 22, 22));
        btnLogout.setPreferredSize(new Dimension(120, 40));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> onLogout());
        right.add(clock);
        right.add(btnLogout);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    // ═════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BG_SIDEBAR, 0, getHeight(), new Color(8, 14, 36)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sb.setPreferredSize(new Dimension(235, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(BorderFactory.createEmptyBorder(28, 0, 24, 0));

        // Avatar area
        sb.add(buildAvatarArea());
        sb.add(Box.createRigidArea(new Dimension(0, 22)));
        sb.add(sidebarDivider());
        sb.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel navLabel = secLabel("NAVIGATION");
        navLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(navLabel);
        btnDashboard = sideBtn(sb, "Dashboard", "dashboard.png", COL_BLUE, "DASHBOARD");
        btnSearch = sideBtn(sb, "Search Flights", "flight-takeoff.png", COL_TEAL, "SEARCH");
        btnMyBookings = sideBtn(sb, "My Bookings", "booking.png", COL_PURPLE, "MY_BOOKINGS");
        btnMyTickets = sideBtn(sb, "My Tickets", "ticket.png", COL_PINK, "MY_TICKETS");

        sb.add(Box.createRigidArea(new Dimension(0, 22)));
        sb.add(sidebarDivider());
        sb.add(Box.createRigidArea(new Dimension(0, 14)));
        JLabel accLabel = secLabel("ACCOUNT");
        accLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(accLabel);
        btnMyProfile = sideBtn(sb, "My Profile", "account.png", COL_AMBER, "MY_PROFILE");

        sb.add(Box.createVerticalGlue());
        // Tip card
        sb.add(buildTipCard());
        sb.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel ver = new JLabel("AeroSys v1.0  •  CSC236");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(new Color(80, 100, 140));
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(ver);
        return sb;
    }

    private JPanel buildAvatarArea() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), getHeight(), COL_PURPLE));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(68, 68);
            }
        };
        circle.setOpaque(false);
        circle.setLayout(new GridBagLayout());
        ImageIcon logoIco = icon("logo.png", 60, 60);
        circle.add(new JLabel(logoIco));
        JPanel cWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cWrap.setOpaque(false);
        cWrap.add(circle);
        sidebarNameLbl = new JLabel("Passenger Name");
        sidebarNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sidebarNameLbl.setForeground(Color.WHITE);
        sidebarNameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarNameLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel roleLbl = new JLabel("Registered Passenger");
        roleLbl.setIcon(icon("flight-takeoff.png", 14, 14));
        roleLbl.setIconTextGap(5);
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLbl.setForeground(new Color(148, 163, 184));
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(cWrap);
        p.add(Box.createRigidArea(new Dimension(0, 8)));
        p.add(sidebarNameLbl);
        p.add(Box.createRigidArea(new Dimension(0, 3)));
        p.add(roleLbl);
        return p;
    }

    private JPanel buildTipCard() {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(59, 130, 246, 80));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(240, 80));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel t1 = new JLabel("Book early for best prices!");
        t1.setIcon(icon("info.png", 14, 14));
        t1.setIconTextGap(6);
        t1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t1.setForeground(new Color(148, 163, 220));
        JLabel t2 = new JLabel("Pay first to receive your ticket.");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t2.setForeground(new Color(100, 120, 170));
        card.add(t1, BorderLayout.NORTH);
        card.add(t2, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        wrap.add(card);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return wrap;
    }

    private JLabel sidebarDivider() {
        JLabel d = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 18));
                g.drawLine(18, getHeight() / 2, getWidth() - 18, getHeight() / 2);
            }
        };
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        d.setPreferredSize(new Dimension(0, 2));
        return d;
    }

    private JLabel secLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(70, 95, 140));
        l.setBorder(BorderFactory.createEmptyBorder(6, 24, 6, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private AeroComponents.SidebarButton sideBtn(JPanel parent, String text, String iconFile, Color accent, String card) {
        AeroComponents.SidebarButton btn = new AeroComponents.SidebarButton(text, accent);
        ImageIcon ic = icon(iconFile, 22, 22);
        if (ic != null) {
            btn.setIcon(ic);
        }
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            showCard(card);
            setActive(btn);
        });
        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
        return btn;
    }

    private void setActive(AeroComponents.SidebarButton btn) {
        for (AeroComponents.SidebarButton b : new AeroComponents.SidebarButton[]{
            btnDashboard, btnSearch, btnMyBookings, btnMyTickets, btnMyProfile}) {
            if (b != null) {
                b.setActive(false);
            }
        }
        btn.setActive(true);
    }

    // ═════════════════════════════════════════════════════════════════
    //  CONTENT CARDS
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_CONTENT);
        contentPanel.add(buildDashboard(), "DASHBOARD");
        contentPanel.add(buildSearchPanel(), "SEARCH");
        contentPanel.add(buildMyBookings(), "MY_BOOKINGS");
        contentPanel.add(buildMyTickets(), "MY_TICKETS");
        contentPanel.add(buildMyProfile(), "MY_PROFILE");
        return contentPanel;
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
        switch (name) {
            case "DASHBOARD" ->
                loadDashboardStats();
            case "MY_BOOKINGS" ->
                loadMyBookings();
            case "MY_TICKETS" ->
                loadMyTickets();
            case "MY_PROFILE" ->
                loadMyProfile();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONTENT);

        // Banner
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(30, 58, 138), getWidth(), getHeight(), new Color(59, 130, 246)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 12));
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 6}, 0));
                g2.drawArc(-40, 20, getWidth() + 80, 120, -20, 60);
                g2.dispose();
            }
        };
        banner.setPreferredSize(new Dimension(0, 140));
        banner.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 36));
        JLabel greet = new JLabel("  Your Travel Dashboard");
        greet.setFont(new Font("Segoe UI", Font.BOLD, 32));
        greet.setForeground(Color.WHITE);
        ImageIcon wavIco = icon("waving-hand.png", 36, 36);
        if (wavIco == null) {
            wavIco = icon("flight.png", 36, 36);
        }
        if (wavIco != null) {
            greet.setIcon(wavIco);
        }
        JLabel bsub = new JLabel("Manage your flights, bookings and tickets all in one place");
        bsub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bsub.setForeground(new Color(186, 210, 255));
        JButton btnSearchNow = new JButton("Search Flights") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(COL_BLUE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                Icon ic = getIcon();
                int iw = ic != null ? ic.getIconWidth() + 8 : 0;
                int tx = (getWidth() - iw - fm.stringWidth(getText())) / 2;
                if (ic != null) {
                    ic.paintIcon(this, g2, tx, (getHeight() - ic.getIconHeight()) / 2);
                }
                g2.drawString(getText(), tx + iw, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        ImageIcon sIco = icon("search.png", 18, 18);
        if (sIco != null) {
            btnSearchNow.setIcon(sIco);
        }
        btnSearchNow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearchNow.setForeground(COL_BLUE);
        btnSearchNow.setPreferredSize(new Dimension(190, 44));
        btnSearchNow.setBorderPainted(false);
        btnSearchNow.setFocusPainted(false);
        btnSearchNow.setContentAreaFilled(false);
        btnSearchNow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearchNow.addActionListener(e -> {
            showCard("SEARCH");
            setActive(btnSearch);
        });
        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);
        bannerText.add(Box.createVerticalGlue());
        bannerText.add(greet);
        bannerText.add(Box.createRigidArea(new Dimension(0, 6)));
        bannerText.add(bsub);
        bannerText.add(Box.createVerticalGlue());
        JPanel bRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 48));
        bRight.setOpaque(false);
        bRight.add(btnSearchNow);
        banner.add(bannerText, BorderLayout.CENTER);
        banner.add(bRight, BorderLayout.EAST);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 0));
        stats.setOpaque(false);
        stats.setBorder(BorderFactory.createEmptyBorder(22, 28, 0, 28));
        cardAvailFlights = new AeroComponents.StatCard("Available Flights", COL_TEAL, false);
        cardMyBookings = new AeroComponents.StatCard("My Bookings", COL_PURPLE, false);
        cardMyTickets = new AeroComponents.StatCard("My Active Tickets", COL_PINK, false);
        cardSpent = new AeroComponents.StatCard("Total Spent", COL_AMBER, true);
        cardAvailFlights.setCardIcon(icon("flight.png", 36, 36));
        cardMyBookings.setCardIcon(icon("booking.png", 36, 36));
        cardMyTickets.setCardIcon(icon("ticket.png", 36, 36));
        cardSpent.setCardIcon(icon("money.png", 36, 36));
        stats.add(cardAvailFlights);
        stats.add(cardMyBookings);
        stats.add(cardMyTickets);
        stats.add(cardSpent);

        // Quick Actions
        JPanel qaRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qaRow.setOpaque(false);
        qaRow.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 0));
        JLabel qaLbl = new JLabel("  Quick Actions");
        qaLbl.setIcon(icon("dashboard.png", 22, 22));
        qaLbl.setIconTextGap(6);
        qaLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        qaLbl.setForeground(new Color(15, 23, 42));
        qaRow.add(qaLbl);

        JPanel qaBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        qaBtns.setOpaque(false);
        qaBtns.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(8, 22, 8, 22)));
        qaBtns.add(quickCard("Search Flights", "flight-takeoff.png", COL_TEAL, e -> {
            showCard("SEARCH");
            setActive(btnSearch);
        }));
        qaBtns.add(quickCard("My Bookings", "booking.png", COL_PURPLE, e -> {
            showCard("MY_BOOKINGS");
            setActive(btnMyBookings);
        }));
        qaBtns.add(quickCard("My Tickets", "ticket.png", COL_PINK, e -> {
            showCard("MY_TICKETS");
            setActive(btnMyTickets);
        }));
        qaBtns.add(quickCard("My Profile", "user.png", COL_AMBER, e -> {
            showCard("MY_PROFILE");
            setActive(btnMyProfile);
        }));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_CONTENT);
        JPanel lower = new JPanel(new BorderLayout());
        lower.setBackground(BG_CONTENT);
        lower.add(qaBtns, BorderLayout.CENTER);
        center.add(stats, BorderLayout.NORTH);
        center.add(qaRow, BorderLayout.CENTER);
        center.add(lower, BorderLayout.SOUTH);
        panel.add(banner, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel quickCard(String text, String iconFile, Color accent, ActionListener al) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            private boolean hover = false;

            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }

                    public void mouseClicked(MouseEvent e) {
                        al.actionPerformed(null);
                    }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(hover ? accent : new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(hover ? 2f : 1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(170, 110));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel ic = new JLabel(icon(iconFile, 36, 36));
        ic.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(accent);
        card.add(ic, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════
    //  SEARCH FLIGHTS
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_CONTENT);
        panel.add(pageHeader("Search Flights", "Find available flights and book your seat",
                icon("flight-takeoff.png", 36, 36)), BorderLayout.NORTH);

        JPanel searchCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COL_BLUE.darker().darker(), getWidth(), getHeight(), new Color(99, 102, 241)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        searchCard.setOpaque(false);
        searchCard.setPreferredSize(new Dimension(0, 110));
        searchCard.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        fields.setOpaque(false);
        txtSearchOrigin = new AeroComponents.AeroTextField("From (e.g. Riyadh)");
        txtSearchOrigin.setPreferredSize(new Dimension(200, 44));
        txtSearchDest = new AeroComponents.AeroTextField("To (e.g. Dubai)");
        txtSearchDest.setPreferredSize(new Dimension(200, 44));
        dpSearchDate = new AeroComponents.AeroDatePicker("Departure Date");
        dpSearchDate.setPreferredSize(new Dimension(200, 44));

        AeroComponents.AeroButton btnGo = new AeroComponents.AeroButton("Search", COL_BLUE);
        AeroComponents.AeroButton btnAll = new AeroComponents.AeroButton("Show All", new Color(99, 102, 241));
        btnGo.setIcon(icon("search.png", 18, 18));
        btnGo.setPreferredSize(new Dimension(140, 44));
        btnAll.setIcon(icon("flight.png", 18, 18));
        btnAll.setPreferredSize(new Dimension(130, 44));
        btnGo.addActionListener(e -> onSearchFlights());
        btnAll.addActionListener(e -> onLoadAllFlights());

        fields.add(new JLabel(icon("location.png", 20, 20)));
        fields.add(txtSearchOrigin);
        fields.add(new JLabel(icon("destination.png", 20, 20)));
        fields.add(txtSearchDest);
        fields.add(new JLabel(icon("calendar.png", 20, 20)));
        fields.add(dpSearchDate);
        fields.add(btnGo);
        fields.add(btnAll);
        searchCard.add(fields, BorderLayout.CENTER);

        flightResultsPanel = new JPanel();
        flightResultsPanel.setLayout(new BoxLayout(flightResultsPanel, BoxLayout.Y_AXIS));
        flightResultsPanel.setBackground(BG_CONTENT);
        flightResultsPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 20, 20));
        JLabel hint = new JLabel("  Use the search above or click 'Show All' to browse flights", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        hint.setForeground(new Color(150, 170, 210));
        hint.setIcon(icon("flight.png", 28, 28));
        flightResultsPanel.add(hint);

        JScrollPane flightScroll = new JScrollPane(flightResultsPanel);
        flightScroll.setBorder(null);
        flightScroll.setOpaque(false);
        flightScroll.getViewport().setOpaque(false);
        flightScroll.getViewport().setBackground(BG_CONTENT);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG_CONTENT);
        center.add(searchCard, BorderLayout.NORTH);
        center.add(flightScroll, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════
    //  MY BOOKINGS
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildMyBookings() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONTENT);
        panel.add(pageHeader("My Bookings", "View and manage all your flight bookings",
                icon("booking.png", 36, 36)), BorderLayout.NORTH);
        String[] cols = {"Booking ID", "Flight No", "From", "To", "Departure", "Seats", "Total ($)", "Status", "Booked On"};
        modelBookings = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblBookings = styledTable(modelBookings);
        txtFilterBooking = new AeroComponents.AeroTextField("Filter bookings...");
        txtFilterBooking.setPreferredSize(new Dimension(240, 33));
        txtFilterBooking.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterBookings(txtFilterBooking.getText());
            }
        });
        AeroComponents.AeroButton btnPay = new AeroComponents.AeroButton("Pay Now", COL_GREEN);
        AeroComponents.AeroButton btnCancel = new AeroComponents.AeroButton("Cancel Booking", Color.RED);
        AeroComponents.AeroButton btnPrintReceipt = new AeroComponents.AeroButton("Print Receipt", COL_PURPLE);
        btnPrintReceipt.setIcon(icon("print.png", 18, 18));
        btnPrintReceipt.addActionListener(e -> onPrintBookingReceipt());

        AeroComponents.AeroButton btnRefresh = new AeroComponents.AeroButton("Refresh", COL_BLUE);
        btnPay.setIcon(icon("paid.png", 18, 18));
        btnCancel.setIcon(icon("cancel-ticket.png", 18, 18));
        btnRefresh.setIcon(icon("refresh.png", 18, 18));
        btnPay.addActionListener(e -> onPayBooking());
        btnCancel.addActionListener(e -> onCancelBooking());
        btnRefresh.addActionListener(e -> loadMyBookings());
        panel.add(tableCard(tblBookings, new Component[]{txtFilterBooking, btnPay, btnCancel,btnPrintReceipt, btnRefresh}), BorderLayout.CENTER);
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════
    //  MY TICKETS
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildMyTickets() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONTENT);
        panel.add(pageHeader("My Tickets", "All issued tickets for your confirmed bookings",
                icon("ticket.png", 36, 36)), BorderLayout.NORTH);
        String[] cols = {"Ticket ID", "Seat", "Flight No", "From", "To", "Departure", "Issued", "Status"};
        modelTickets = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblTickets = styledTable(modelTickets);
        txtFilterTicket = new AeroComponents.AeroTextField("Filter tickets...");
        txtFilterTicket.setPreferredSize(new Dimension(280, 40));
        txtFilterTicket.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterTickets(txtFilterTicket.getText());
            }
        });
        AeroComponents.AeroButton btnPrint = new AeroComponents.AeroButton("Print Ticket", COL_BLUE);
        AeroComponents.AeroButton btnRefresh = new AeroComponents.AeroButton("Refresh", new Color(99, 102, 241));
        btnPrint.setIcon(icon("print.png", 18, 18));
        btnRefresh.setIcon(icon("refresh.png", 18, 18));
        btnPrint.addActionListener(e -> onPrintTicket());
        btnRefresh.addActionListener(e -> loadMyTickets());
        panel.add(tableCard(tblTickets, new Component[]{txtFilterTicket, btnPrint, btnRefresh}), BorderLayout.CENTER);
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════
    //  MY PROFILE
    // ═════════════════════════════════════════════════════════════════
    private JPanel buildMyProfile() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONTENT);
        panel.add(pageHeader("My Profile", "Your personal information and account details",
                icon("account.png", 36, 36)), BorderLayout.NORTH);
        profileCardLayout = new CardLayout();
        profileSwitchPanel = new JPanel(profileCardLayout);
        profileSwitchPanel.setBackground(BG_CONTENT);
        profileViewPanel = buildProfileView();
        profileEditPanel = buildProfileEdit();
        profileSwitchPanel.add(profileViewPanel, "VIEW");
        profileSwitchPanel.add(profileEditPanel, "EDIT");
        panel.add(profileSwitchPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildProfileView() {
        String[][] rows = {
            {"user.png", "Full Name"}, {"account.png", "Username"}, {"email.png", "Email"},
            {"phone.png", "Phone"}, {"passport.png", "Passport No"}, {"nationality.png", "Nationality"},
            {"status.png", "Status"}, {"calendar.png", "Member Since"}
        };
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblProfName = profileVal();
        lblProfUsername = profileVal();
        lblProfEmail = profileVal();
        lblProfPhone = profileVal();
        lblProfPassport = profileVal();
        lblProfNat = profileVal();
        lblProfStatus = profileVal();
        lblProfJoined = profileVal();
        JLabel[] vals = {lblProfName, lblProfUsername, lblProfEmail, lblProfPhone, lblProfPassport, lblProfNat, lblProfStatus, lblProfJoined};
        gbc.gridwidth = 1;
        for (int i = 0; i < rows.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.weightx = 0.05;
            JLabel ri = new JLabel(icon(rows[i][0], 20, 20));
            ri.setPreferredSize(new Dimension(28, 28));
            card.add(ri, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.40;
            JLabel k = new JLabel(rows[i][1]);
            k.setFont(new Font("Segoe UI", Font.BOLD, 14));
            k.setForeground(new Color(71, 85, 105));
            card.add(k, gbc);
            gbc.gridx = 2;
            gbc.weightx = 0.55;
            card.add(vals[i], gbc);
        }
        gbc.gridy = rows.length;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.insets = new Insets(20, 10, 8, 10);
        AeroComponents.AeroButton btnEdit = new AeroComponents.AeroButton("Edit Profile", COL_BLUE);
        btnEdit.setIcon(icon("edit.png", 18, 18));
        btnEdit.setPreferredSize(new Dimension(180, 44));
        btnEdit.addActionListener(e -> {
            populateEditFields();
            profileCardLayout.show(profileSwitchPanel, "EDIT");
        });
        JPanel bw = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bw.setOpaque(false);
        bw.add(btnEdit);
        card.add(bw, gbc);
        JPanel outer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        outer.setBackground(BG_CONTENT);
        card.setPreferredSize(new Dimension(620, 510));
        outer.add(card);
        JScrollPane sp = new JScrollPane(outer);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBackground(BG_CONTENT);
        JPanel pv = new JPanel(new BorderLayout());
        pv.setBackground(BG_CONTENT);
        pv.add(sp, BorderLayout.CENTER);
        return pv;
    }

    private JPanel buildProfileEdit() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("  Edit Profile");
        title.setIcon(icon("edit.png", 24, 24));
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));
        card.add(title, gbc);
        String[][] editRows = {{"user.png", "Full Name"}, {"email.png", "Email"}, {"phone.png", "Phone"}};
        fldName = new AeroComponents.AeroTextField("Enter full name");
        fldName.setPreferredSize(new Dimension(320, 42));
        fldEmail = new AeroComponents.AeroTextField("Enter email");
        fldEmail.setPreferredSize(new Dimension(320, 42));
        fldPhone = new AeroComponents.AeroTextField("Enter phone");
        fldPhone.setPreferredSize(new Dimension(320, 42));
        AeroComponents.AeroTextField[] flds = {fldName, fldEmail, fldPhone};
        gbc.gridwidth = 1;
        for (int i = 0; i < editRows.length; i++) {
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            gbc.weightx = 0.35;
            JPanel lp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            lp.setOpaque(false);
            lp.add(new JLabel(icon(editRows[i][0], 18, 18)));
            JLabel k = new JLabel(editRows[i][1]);
            k.setFont(new Font("Segoe UI", Font.BOLD, 14));
            k.setForeground(new Color(71, 85, 105));
            lp.add(k);
            card.add(lp, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.65;
            card.add(flds[i], gbc);
        }
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        JLabel note = new JLabel("  Passport No, Nationality and Username cannot be changed.");
        note.setIcon(icon("info.png", 16, 16));
        note.setIconTextGap(6);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(new Color(100, 116, 139));
        card.add(note, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 10, 8, 10);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        AeroComponents.AeroButton btnSave = new AeroComponents.AeroButton("Save Changes", COL_GREEN);
        AeroComponents.AeroButton btnCancel2 = new AeroComponents.AeroButton("Cancel", new Color(100, 116, 139));
        btnSave.setIcon(icon("checkmark.png", 18, 18));
        btnSave.setPreferredSize(new Dimension(180, 44));
        btnCancel2.setPreferredSize(new Dimension(140, 44));
        btnSave.addActionListener(e -> onSaveProfile());
        btnCancel2.addActionListener(e -> profileCardLayout.show(profileSwitchPanel, "VIEW"));
        btnRow.add(btnSave);
        btnRow.add(btnCancel2);
        card.add(btnRow, gbc);
        JPanel outer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        outer.setBackground(BG_CONTENT);
        card.setPreferredSize(new Dimension(620, 440));
        outer.add(card);
        JScrollPane sp = new JScrollPane(outer);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBackground(BG_CONTENT);
        JPanel pe = new JPanel(new BorderLayout());
        pe.setBackground(BG_CONTENT);
        pe.add(sp, BorderLayout.CENTER);
        return pe;
    }

    // ═════════════════════════════════════════════════════════════════
    //  SHARED UI HELPERS
    // ═════════════════════════════════════════════════════════════════
    private JPanel pageHeader(String title, String sub, Icon ic) {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(226, 232, 240));
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createEmptyBorder(20, 30, 18, 30));
        JLabel t = new JLabel("  " + title);
        t.setIcon(ic);
        t.setIconTextGap(10);
        t.setFont(new Font("Segoe UI", Font.BOLD, 27));
        t.setForeground(new Color(15, 23, 42));
        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(new Color(100, 116, 139));
        s.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));
        JPanel tp = new JPanel();
        tp.setOpaque(false);
        tp.setLayout(new BoxLayout(tp, BoxLayout.Y_AXIS));
        tp.add(t);
        tp.add(s);
        h.add(tp, BorderLayout.WEST);
        return h;
    }

    private JPanel tableCard(JTable table, Component[] toolbar) {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 24, 24));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        for (Component c : toolbar) {
            bar.add(c);
        }
        card.add(bar, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_CONTENT);
        wrap.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(44);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(new Color(30, 64, 175));
        t.setGridColor(new Color(241, 245, 249));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setBackground(Color.WHITE);
        JTableHeader hdr = t.getTableHeader();
        hdr.setBackground(new Color(248, 250, 252));
        hdr.setForeground(new Color(71, 85, 105));
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdr.setPreferredSize(new Dimension(0, 46));
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
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(CENTER);
                if (v != null && !sel) {
                    String s = v.toString();
                    if (s.equalsIgnoreCase("Confirmed") || s.equalsIgnoreCase("Paid") || s.equalsIgnoreCase("Active") || s.equalsIgnoreCase("Scheduled")) {
                        c.setForeground(new Color(5, 150, 105));
                    } else if (s.equalsIgnoreCase("Pending") || s.equalsIgnoreCase("Delayed")) {
                        c.setForeground(new Color(217, 119, 6));
                    } else if (s.equalsIgnoreCase("Cancelled") || s.equalsIgnoreCase("Suspended")) {
                        c.setForeground(new Color(220, 38, 38));
                    }
                }
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(cr);
        }
        return t;
    }

    private JLabel profileVal() {
        JLabel l = new JLabel("—");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(new Color(15, 23, 42));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                BorderFactory.createEmptyBorder(4, 0, 4, 0)));
        return l;
    }

    private void filterBookings(String q) {
        modelBookings.setRowCount(0);
        if (q == null || q.isBlank()) {
            for (Object[] r : allBookingRows) {
                modelBookings.addRow(r);
            }
            return;
        }
        String lq = q.toLowerCase();
        for (Object[] r : allBookingRows) {
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(lq)) {
                    modelBookings.addRow(r);
                    break;
                }
            }
        }
    }

    private void filterTickets(String q) {
        modelTickets.setRowCount(0);
        if (q == null || q.isBlank()) {
            for (Object[] r : allTicketRows) {
                modelTickets.addRow(r);
            }
            return;
        }
        String lq = q.toLowerCase();
        for (Object[] r : allTicketRows) {
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(lq)) {
                    modelTickets.addRow(r);
                    break;
                }
            }
        }
    }

    private void startClock() {
        new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy")));
        }).start();
    }

    public void setPassenger(Passenger p) {
        this.loggedInPassenger = p;
        if (lblWelcome != null) {
            lblWelcome.setText("  Welcome, " + p.getName());
        }
        if (sidebarNameLbl != null) {
            sidebarNameLbl.setText(p.getName());
        }
    }

    private String safe(String s) {
        return (s != null && !s.isBlank()) ? s : "—";
    }

    // ═════════════════════════════════════════════════════════════════
    //  EVENT HANDLERS
    // ═════════════════════════════════════════════════════════════════
    private void onLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            // Use AirportSystem facade — clears service cache + session
            AirportSystem.getInstance().onLogout();
            dispose();
            SwingUtilities.invokeLater(() -> new AeroSysLoginUI().setVisible(true));
        }
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────
    private void loadDashboardStats() {
        int paxId = session.getCurrentUserId();
        if (paxId <= 0) {
            return;
        }
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                return passengerDAO.getPassengerStats(paxId);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    if (stats != null && !stats.isEmpty()) {
                        int avail = stats.containsKey("available_flights_count") ? ((Number) stats.get("available_flights_count")).intValue() : 0;
                        int bkgs = stats.containsKey("total_bookings") ? ((Number) stats.get("total_bookings")).intValue() : 0;
                        int tkts = stats.containsKey("active_tickets") ? ((Number) stats.get("active_tickets")).intValue() : 0;
                        int spent = stats.containsKey("total_spent") ? ((Number) stats.get("total_spent")).intValue() : 0;
                        cardAvailFlights.animateTo(avail);
                        cardMyBookings.animateTo(bkgs);
                        cardMyTickets.animateTo(tkts);
                        cardSpent.animateTo(spent);
                    }
                } catch (Exception ex) {
                    cardAvailFlights.animateTo(0);
                    cardMyBookings.animateTo(0);
                    cardMyTickets.animateTo(0);
                    cardSpent.animateTo(0);
                }
            }
        }.execute();
    }

    // ── SEARCH FLIGHTS ─────────────────────────────────────────────────
    private void onSearchFlights() {
        String origin = txtSearchOrigin.getText().trim();
        String dest = txtSearchDest.getText().trim();
        String date = dpSearchDate.getDateText();
        if (origin.isEmpty() && dest.isEmpty()) {
            toast("Enter at least an origin or destination!", false);
            return;
        }
        new SwingWorker<List<Flight>, Void>() {
            @Override
            protected List<Flight> doInBackground() {
                Date sqlDate = null;
                if (!date.isEmpty()) {
                    try {
                        sqlDate = Date.valueOf(date);
                    } catch (Exception ignored) {
                    }
                }
                // FlightService.searchFlights requires all three params — if no date, use getAllAvailable
                if (sqlDate == null) {
                    return flightSvc.getAllAvailableFlights();
                }
                return flightSvc.searchFlights(origin.isEmpty() ? null : origin, dest.isEmpty() ? null : dest, sqlDate);
            }

            @Override
            protected void done() {
                try {
                    displayFlightCards(get());
                } catch (Exception ex) {
                    toast("Error searching flights: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void onLoadAllFlights() {
        new SwingWorker<List<Flight>, Void>() {
            @Override
            protected List<Flight> doInBackground() {
                return flightSvc.getAllAvailableFlights();
            }

            @Override
            protected void done() {
                try {
                    displayFlightCards(get());
                } catch (Exception ex) {
                    toast("Error loading flights: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void displayFlightCards(List<Flight> flights) {
        flightResultsPanel.removeAll();
        if (flights == null || flights.isEmpty()) {
            JLabel empty = new JLabel("  No flights found matching your search.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            empty.setForeground(new Color(150, 170, 210));
            empty.setIcon(icon("info.png", 24, 24));
            flightResultsPanel.add(empty);
        } else {
            for (Flight f : flights) {
                String dep = f.getDepartureTime() != null ? f.getDepartureTime().toString() : "N/A";
                String arr = f.getArrivalTime() != null ? f.getArrivalTime().toString() : "N/A";
                JPanel fc = buildFlightCard(f, dep, arr);
                flightResultsPanel.add(fc);
                flightResultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        flightResultsPanel.revalidate();
        flightResultsPanel.repaint();
    }

    private JPanel buildFlightCard(Flight f, String dep, String arr) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 18, 18);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setPreferredSize(new Dimension(100, 130));
        card.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Route
        JPanel route = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        route.setOpaque(false);
        JLabel orig = new JLabel(f.getOrigin());
        orig.setFont(new Font("Segoe UI", Font.BOLD, 20));
        orig.setForeground(new Color(15, 23, 42));
        JLabel arrow = new JLabel(icon("flight-takeoff.png", 26, 26));
        JLabel dest2 = new JLabel(f.getDestination());
        dest2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dest2.setForeground(new Color(15, 23, 42));
        route.add(orig);
        route.add(arrow);
        route.add(dest2);

        // Detail grid
        JPanel detail = new JPanel(new GridLayout(2, 2, 8, 2));
        detail.setOpaque(false);
        detail.add(detailCell("Departure", dep));
        detail.add(detailCell("Arrival", arr));
        detail.add(detailCell("Seats", f.getAvailableSeats() + " left"));
        detail.add(detailCell("Flight No", f.getFlightNumber()));

        // Right panel: price + status + book
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        JLabel priceLabel = new JLabel(String.format("$%.2f", f.getPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        priceLabel.setForeground(new Color(16, 185, 129));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Color sc = f.getStatus().equalsIgnoreCase("Scheduled") ? new Color(16, 185, 129) : f.getStatus().equalsIgnoreCase("Delayed") ? new Color(245, 158, 11) : new Color(239, 68, 68);
        JLabel badge = new JLabel(f.getStatus(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(sc);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setOpaque(false);

        AeroComponents.AeroButton btnBook = new AeroComponents.AeroButton("Book Now", COL_BLUE);
        btnBook.setPreferredSize(new Dimension(110, 36));
        btnBook.setMaximumSize(new Dimension(120, 38));
        btnBook.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBook.setEnabled(f.getAvailableSeats() > 0 && f.getStatus().equalsIgnoreCase("Scheduled"));
        btnBook.addActionListener(e -> onBookFlight(f.getFlightId(), f.getPrice()));

        right.add(Box.createVerticalGlue());
        right.add(priceLabel);
        right.add(Box.createRigidArea(new Dimension(0, 4)));
        right.add(badge);
        right.add(Box.createRigidArea(new Dimension(0, 8)));
        right.add(btnBook);
        right.add(Box.createVerticalGlue());

        JPanel left2 = new JPanel(new BorderLayout(0, 6));
        left2.setOpaque(false);
        left2.add(route, BorderLayout.NORTH);
        left2.add(detail, BorderLayout.CENTER);
        card.add(left2, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel detailCell(String lbl, String val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel k = new JLabel(lbl);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        k.setForeground(new Color(100, 116, 139));
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(new Color(30, 41, 59));
        p.add(k, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void onBookFlight(int flightId, double price) {
        if (!session.isLoggedIn()) {
            toast("Not logged in!", false);
            return;
        }
        String[] opts = {"1", "2", "3", "4", "5"};
        String chosen = (String) JOptionPane.showInputDialog(this,
                "How many seats?\nPrice per seat: $" + String.format("%.2f", price),
                "Book Flight", JOptionPane.PLAIN_MESSAGE, null, opts, "1");
        if (chosen == null) {
            return;
        }
        int seats = Integer.parseInt(chosen);
        double total = seats * price;
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("<html><b>Confirm Booking</b><br>Seats : %d<br>Total : $%.2f<br><br>A payment record will be created. Pay from <b>My Bookings</b>.</html>", seats, total),
                "Confirm Booking", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            // BookingService.createBooking(flightId, seatCount) — passengerId read from session
            @Override
            protected String doInBackground() {
                return bookingSvc.createBooking(flightId, seats);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        showCard("MY_BOOKINGS");
                        setActive(btnMyBookings);
                    }
                } catch (Exception ex) {
                    toast("Booking error: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    // ── MY BOOKINGS ───────────────────────────────────────────────────
    private void loadMyBookings() {
        modelBookings.setRowCount(0);
        allBookingRows.clear();
        new SwingWorker<List<Object[]>, Void>() {
            // BookingService.getMyBookings() — passengerId read from session
            @Override
            protected List<Object[]> doInBackground() {
                return bookingSvc.getMyBookings();
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> list = get();
                    if (list != null) {
                        for (Object[] row : list) {
                            // row: [booking_id, flight_number, origin, destination, departure_time,
                            //       seat_count, total_price, booking_status, booked_at]
                            Object[] tr = new Object[]{row[0], row[1], row[2], row[3], row[4], row[5],
                                String.format("%.2f", row[6]), row[7], row[8]};
                            allBookingRows.add(tr);
                            modelBookings.addRow(tr);
                        }
                    }
                } catch (Exception ex) {
                    toast("Error loading bookings: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void onCancelBooking() {
        int row = tblBookings.getSelectedRow();
        if (row == -1) {
            toast("Select a booking to cancel!", false);
            return;
        }
        String status = modelBookings.getValueAt(row, 7).toString();
        if ("Cancelled".equalsIgnoreCase(status)) {
            toast("This booking is already cancelled.", false);
            return;
        }
        if ("Confirmed".equalsIgnoreCase(status)) {
            toast("Confirmed bookings cannot be cancelled.", false);
            return;
        }
        int bookingId = Integer.parseInt(modelBookings.getValueAt(row, 0).toString());
        int c = JOptionPane.showConfirmDialog(this,
                "<html>Cancel booking <b>#" + bookingId + "</b>?<br>This will free your reserved seats.</html>",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<String, Void>() {
            // BookingService.cancelBooking(int) returns "SUCCESS:..." / "ERROR:..."
            @Override
            protected String doInBackground() {
                return bookingSvc.cancelBooking(bookingId);
            }

            @Override
            protected void done() {
                try {
                    String r = get();
                    toast(r);
                    if (r.startsWith("SUCCESS")) {
                        loadMyBookings();
                    }
                } catch (Exception ex) {
                    toast("Error: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void onPayBooking() {
        int row = tblBookings.getSelectedRow();
        if (row == -1) {
            toast("Select a booking to pay!", false);
            return;
        }

        String status = modelBookings.getValueAt(row, 7).toString();
        if (!"Pending".equalsIgnoreCase(status)) {
            toast("Only Pending bookings can be paid!", false);
            return;
        }

        int bookingId = Integer.parseInt(modelBookings.getValueAt(row, 0).toString());
        String totalStr = modelBookings.getValueAt(row, 6).toString();

        String[] methods = {"Credit Card", "Debit Card", "Cash", "Online"};
        String method = (String) JOptionPane.showInputDialog(this,
                "<html>Select payment method<br><b>Total: $" + totalStr + "</b></html>",
                "Pay Booking #" + bookingId, JOptionPane.PLAIN_MESSAGE, null, methods, methods[0]);
        if (method == null) {
            return;
        }

        // processedBy = 0 means passenger self-service (not an employee transaction)
        // PaymentService.processPayment returns a String: "SUCCESS:..." or "ERROR:..."
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return paymentSvc.processPayment(bookingId, method, 0);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result != null && result.startsWith("SUCCESS:")) {
                        toast("Payment of $" + totalStr + " via " + method
                                + " confirmed! Your tickets have been generated.", true);
                        loadMyBookings();
                        loadMyTickets();
                    } else {
                        String msg = result != null ? result.replaceFirst("^ERROR:", "") : "Payment failed. Please try again.";
                        toast(msg, false);
                    }
                } catch (Exception ex) {
                    toast("Payment error: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void onPrintBookingReceipt() {
        int row = tblBookings.getSelectedRow();
        if (row == -1) {
            toast("Select a booking first!", false);
            return;
        }
        BookingPaymentPrinter.fromPassengerTable(this, modelBookings, row,
                loggedInPassenger != null ? loggedInPassenger.getName() : "—");
        toast("Receipt for booking #" + modelBookings.getValueAt(row, 0) + " opened.", true);
    }

    // ── MY TICKETS ────────────────────────────────────────────────────
    private void loadMyTickets() {
        modelTickets.setRowCount(0);
        allTicketRows.clear();
        new SwingWorker<List<Object[]>, Void>() {
            // TicketService.getMyTickets() — passengerId read from session
            @Override
            protected List<Object[]> doInBackground() {
                return ticketSvc.getMyTickets();
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> list = get();
                    if (list != null) {
                        for (Object[] row : list) {
                            // row: [ticket_id, seat_number, flight_number, origin, destination, departure_time, issued_at, ticket_status]
                            allTicketRows.add(row);
                            modelTickets.addRow(row);
                        }
                    }
                } catch (Exception ex) {
                    toast("Error loading tickets: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }

    private void onPrintTicket() {
        int row = tblTickets.getSelectedRow();
        if (row == -1) {
            toast("Select a ticket to print!", false);
            return;
        }

        String passengerName = loggedInPassenger != null
                ? loggedInPassenger.getName() : "—";

        TicketPrinter.fromPassengerTable(this, modelTickets, row, passengerName);
        toast("Ticket #" + modelTickets.getValueAt(row, 0) + " opened for printing.", true);
    }

    // ── MY PROFILE ────────────────────────────────────────────────────
    private void loadMyProfile() {
        int paxId = session.getCurrentUserId();
        if (paxId > 0) {
            new SwingWorker<Passenger, Void>() {
                @Override
                protected Passenger doInBackground() {
                    return passengerDAO.getPassengerById(paxId);
                }

                @Override
                protected void done() {
                    try {
                        Passenger fresh = get();
                        if (fresh != null) {
                            loggedInPassenger = fresh;
                            session.startSession(fresh, "PASSENGER");
                        }
                        populateProfileLabels();
                    } catch (Exception ex) {
                        populateProfileLabels();
                    }
                }
            }.execute();
        } else {
            populateProfileLabels();
        }
        profileCardLayout.show(profileSwitchPanel, "VIEW");
    }

    private void populateProfileLabels() {
        Passenger p = loggedInPassenger != null ? loggedInPassenger : session.asPassenger();
        if (p == null) {
            return;
        }
        lblProfName.setText(safe(p.getName()));
        lblProfUsername.setText(safe(p.getUsername()));
        lblProfEmail.setText(safe(p.getEmail()));
        lblProfPhone.setText(safe(p.getPhone()));
        lblProfPassport.setText(safe(p.getPassportNo()));
        lblProfNat.setText(safe(p.getNationality()));
        String st = safe(p.getAccountStatus());
        lblProfStatus.setText(st);
        lblProfStatus.setForeground("Active".equalsIgnoreCase(st) ? new Color(5, 150, 105) : new Color(220, 38, 38));
        lblProfJoined.setText(p.getCreatedAt() != null ? p.getCreatedAt().toString() : "—");
    }

    private void populateEditFields() {
        Passenger p = loggedInPassenger != null ? loggedInPassenger : session.asPassenger();
        if (p == null) {
            return;
        }
        fldName.setText(p.getName() != null ? p.getName() : "");
        fldEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        fldPhone.setText(p.getPhone() != null ? p.getPhone() : "");
    }

    private void onSaveProfile() {
        String name = fldName.getText().trim();
        String email = fldEmail.getText().trim();
        String phone = fldPhone.getText().trim();
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            toast("All fields are required!", false);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            toast("Invalid email address!", false);
            return;
        }
        if (!phone.matches("^[0-9]{10,15}$")) {
            toast("Phone must be 10-15 digits!", false);
            return;
        }
        Passenger p = loggedInPassenger != null ? loggedInPassenger : session.asPassenger();
        if (p == null) {
            toast("Session expired. Please log in again.", false);
            return;
        }
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return passengerDAO.updatePassenger(p);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loggedInPassenger = p;
                        session.startSession(p, "PASSENGER");
                        if (sidebarNameLbl != null) {
                            sidebarNameLbl.setText(p.getName());
                        }
                        if (lblWelcome != null) {
                            lblWelcome.setText("  Welcome, " + p.getName());
                        }
                        toast("Profile updated successfully!", true);
                        profileCardLayout.show(profileSwitchPanel, "VIEW");
                        populateProfileLabels();
                    } else {
                        toast("Profile update failed. Please try again.", false);
                    }
                } catch (Exception ex) {
                    toast("Update error: " + ex.getMessage(), false);
                }
            }
        }.execute();
    }
}
//buildMyBookings
