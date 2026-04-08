package Gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TicketPrinter {

    private TicketPrinter() {}

    // ── Theme colors ──────────────────────────────────────────────────
    static final Color NAVY       = new Color(10,  18,  50);
    static final Color NAVY_LIGHT = new Color(20,  45, 110);
    static final Color BLUE       = new Color(59, 130, 246);
    static final Color GREEN      = new Color(5,  150, 105);
    static final Color AMBER      = new Color(217,119,   6);
    static final Color RED        = new Color(220,  38,  38);
    static final Color GREY_LINE  = new Color(226,232,240);

    public static void fromPassengerTable(Window parent,
            javax.swing.table.DefaultTableModel model,
            int row, String passengerName) {

        showAndPrint(parent,
                cell(model, row, 0),   // ticketId
                passengerName,         // passenger (from session, not table)
                cell(model, row, 1),   // seat
                cell(model, row, 2),   // flightNo
                cell(model, row, 3),   // from
                cell(model, row, 4),   // to
                cell(model, row, 5),   // departure
                cell(model, row, 6),   // issued
                cell(model, row, 7));  // status
    }

   
    public static void fromAdminTable(Window parent,
            javax.swing.table.DefaultTableModel model, int row) {

        showAndPrint(parent,
                cell(model, row, 0),   // ticketId
                cell(model, row, 2),   // passenger_name
                cell(model, row, 1),   // seat_number
                cell(model, row, 4),   // flight_number
                cell(model, row, 5),   // origin
                cell(model, row, 6),   // destination
                cell(model, row, 7),   // departure_time
                cell(model, row, 8),   // issued_at
                cell(model, row, 9));  // ticket_status
        // col [3] = passport_no — available but not shown on boarding pass
    }

    // ── Internal unified path ─────────────────────────────────────────
    private static void showAndPrint(Window parent,
            String ticketId, String passenger, String seat,
            String flightNo, String from, String to,
            String departure, String issued, String status) {

        BoardingPassPanel pass = new BoardingPassPanel(
                ticketId, passenger, seat, flightNo,
                from, to, departure, issued, status);
        showDialog(parent, pass, ticketId);
    }

    // ══════════════════════════════════════════════════════════════════
    //  DIALOG
    // ══════════════════════════════════════════════════════════════════
    private static void showDialog(Window parent,
            BoardingPassPanel pass, String ticketId) {

        JDialog dlg = new JDialog(parent,
                "Boarding Pass  –  Ticket #" + ticketId,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(228, 232, 243));

        JPanel padded = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 26));
        padded.setOpaque(false);
        padded.add(pass);
        root.add(padded, BorderLayout.CENTER);
        root.add(buildBar(dlg, pass), BorderLayout.SOUTH);

        // Ctrl+P / Cmd+P shortcut
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "print");
        root.getActionMap().put("print",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        startPrintJob(pass, dlg);
                    }
                });

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private static JPanel buildBar(JDialog dlg, BoardingPassPanel pass) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 13));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GREY_LINE));

        JButton btnPrint = styledBtn("Print / Save as PDF", BLUE);
        btnPrint.setToolTipText("Ctrl+P  —  choose 'Save as PDF' in the print dialog");
        btnPrint.addActionListener(e -> startPrintJob(pass, dlg));

        JButton btnClose = styledBtn("Close", new Color(100, 116, 139));
        btnClose.addActionListener(e -> dlg.dispose());

        JLabel hint = new JLabel(
                "Tip: choose 'Microsoft Print to PDF' or 'Save as PDF' to save a copy.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(148, 163, 184));

        bar.add(btnPrint);
        bar.add(btnClose);
        bar.add(hint);
        return bar;
    }

        private static void startPrintJob(BoardingPassPanel pass, Component dialogParent) {

        // Step 1 — snapshot on EDT (safe)
        int pw = pass.getPreferredSize().width;
        int ph = pass.getPreferredSize().height;
        java.awt.image.BufferedImage snapshot =
                new java.awt.image.BufferedImage(
                        pw * 2, ph * 2,   // 2x for print sharpness
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = snapshot.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
        sg.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);
        sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,      RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        sg.scale(2, 2);    // paint at 2x resolution
        pass.paint(sg);
        sg.dispose();

        // Step 2 — background thread handles everything from here
        Thread t = new Thread(() -> {

            PrinterJob job = PrinterJob.getPrinterJob();

            // A4 portrait page
            PageFormat pf  = job.defaultPage();
            Paper paper    = pf.getPaper();
            double pageW   = 595, pageH = 842;  // A4 in 72-dpi points
            double margin  = 36;
            paper.setSize(pageW, pageH);
            paper.setImageableArea(margin, margin, pageW - margin * 2, pageH - margin * 2);
            pf.setPaper(paper);
            pf.setOrientation(PageFormat.PORTRAIT);

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) graphics;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,      RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                double iw    = pageFormat.getImageableWidth();
                double ih    = pageFormat.getImageableHeight();
                // snapshot is 2x, divide back to logical size for scaling math
                double scale = Math.min(iw / pw, ih / ph);
                g2.scale(scale, scale);
                g2.translate((iw / scale - pw) / 2.0, 0);

                // Draw snapshot — no Swing calls, fully thread-safe
                g2.drawImage(snapshot, 0, 0, pw, ph, null);
                return Printable.PAGE_EXISTS;
            }, pf);

            job.setJobName("AeroSys Boarding Pass");

            // Step 3 — printDialog() blocks here; EDT is free
            if (job.printDialog()) {
                try {
                    job.print();
                } catch (PrinterException ex) {
                    // Error message back on EDT
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(dialogParent,
                                    "Print failed: " + ex.getMessage(),
                                    "Print Error", JOptionPane.ERROR_MESSAGE));
                }
            }

        }, "AeroSys-PrintThread");

        t.setDaemon(true);   // won't block JVM shutdown
        t.start();
    }

    public static class BoardingPassPanel extends JPanel {

        private final String ticketId, passenger, seat, flightNo;
        private final String from, to, departure, issued, status;

        static final int CARD_W = 640;
        static final int CARD_H = 340;

        public BoardingPassPanel(
                String ticketId, String passenger, String seat, String flightNo,
                String from, String to, String departure, String issued, String status) {
            this.ticketId  = ticketId;
            this.passenger = passenger;
            this.seat      = seat;
            this.flightNo  = flightNo;
            this.from      = from;
            this.to        = to;
            this.departure = departure;
            this.issued    = issued;
            this.status    = status;
            setPreferredSize(new Dimension(CARD_W, CARD_H));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
            drawCard(g2);
            g2.dispose();
        }

        private void drawCard(Graphics2D g2) {
            int w = CARD_W, h = CARD_H;
            int hdrH    = 82;
            int seatW   = 142;
            int bodyTop = hdrH + 20;

            // ── Shadow ────────────────────────────────────────────────
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(i, i, w, h, 20, 20);
            }

            // ── White card base ───────────────────────────────────────
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            // ── Header gradient ───────────────────────────────────────
            Shape savedClip = g2.getClip();
            g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, 18, 18));
            g2.setPaint(new GradientPaint(0, 0, NAVY, w, 0, NAVY_LIGHT));
            g2.fillRect(0, 0, w, hdrH);
            g2.setColor(BLUE);   // top accent stripe
            g2.fillRect(0, 0, w, 5);
            g2.setClip(savedClip);

            // ── Logo ──────────────────────────────────────────────────
            int lsz = 46, lx = 22, ly = (hdrH - lsz) / 2;
            ImageIcon ico = AeroComponents.icon("logo.png", lsz, lsz);
            if (ico != null) {
                g2.drawImage(ico.getImage(), lx, ly, lsz, lsz, null);
            } else {
                // Fallback: subtle circle + plane glyph
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillOval(lx, ly, lsz, lsz);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, lsz - 8));
                g2.drawString("✈", lx + 6, ly + lsz - 6);
            }

            // ── "AeroSys" + subtitle ──────────────────────────────────
            int tx = lx + lsz + 12;
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 27));
            g2.drawString("AeroSys", tx, ly + 30);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(148, 163, 200));
            g2.drawString("BOARDING PASS", tx, ly + 48);

            // ── Ticket ID + print timestamp (top-right) ───────────────
            FontMetrics fm;
            String tidLabel = "TICKET #" + ticketId;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(new Color(180, 200, 230));
            fm = g2.getFontMetrics();
            g2.drawString(tidLabel, w - fm.stringWidth(tidLabel) - 22, ly + 28);

            String ts = "Printed: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm"));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(120, 145, 185));
            fm = g2.getFontMetrics();
            g2.drawString(ts, w - fm.stringWidth(ts) - 22, ly + 46);

            // ── Tear / perforation line ───────────────────────────────
            g2.setColor(GREY_LINE);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{6, 4}, 0));
            g2.drawLine(18, hdrH + 10, w - 18, hdrH + 10);
            // Notch circles on left and right edges
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(228, 232, 243));
            g2.fillOval(-11, hdrH - 1, 22, 22);
            g2.fillOval(w - 11, hdrH - 1, 22, 22);
            g2.setColor(GREY_LINE);
            g2.drawOval(-11, hdrH - 1, 22, 22);
            g2.drawOval(w - 11, hdrH - 1, 22, 22);

            // ── Route: FROM ──→ TO ────────────────────────────────────
            int ry = bodyTop + 12;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
            g2.setColor(new Color(15, 23, 42));
            g2.drawString(trunc(from, 10), 24, ry + 32);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("ORIGIN", 24, ry + 48);

            drawPlaneArrow(g2, 150, ry + 18, 238, ry + 18);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
            g2.setColor(new Color(15, 23, 42));
            g2.drawString(trunc(to, 10), 248, ry + 32);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("DESTINATION", 248, ry + 48);

            // Flight number badge
            g2.setColor(new Color(239, 246, 255));
            g2.fillRoundRect(408, ry, 110, 32, 8, 8);
            g2.setColor(BLUE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            fm = g2.getFontMetrics();
            g2.drawString(flightNo, 408 + (110 - fm.stringWidth(flightNo)) / 2, ry + 22);

            // Thin divider below route
            g2.setColor(GREY_LINE);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawLine(24, bodyTop + 64, w - seatW - 20, bodyTop + 64);

            // ── Info fields (2 × 2 grid) ──────────────────────────────
            int fy = bodyTop + 78;
            drawField(g2, "PASSENGER", passenger, 24,  fy);
            drawField(g2, "ISSUED AT", issued,    310, fy);
            drawField(g2, "DEPARTURE", departure, 24,  fy + 48);
            drawField(g2, "STATUS",    status,    310, fy + 48);

            // ── Vertical divider before seat column ───────────────────
            int divX = w - seatW - 10;
            g2.setColor(GREY_LINE);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{5, 3}, 0));
            g2.drawLine(divX, hdrH + 20, divX, h - 28);

            // ── Seat column ───────────────────────────────────────────
            int scx = w - seatW + 2;
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            fm = g2.getFontMetrics();
            g2.drawString("SEAT", scx + (seatW - fm.stringWidth("SEAT")) / 2, bodyTop + 20);

            int r = 46, cx = scx + (seatW - r * 2) / 2, cy = bodyTop + 28;
            g2.setColor(new Color(239, 246, 255));
            g2.fillOval(cx, cy, r * 2, r * 2);
            g2.setColor(BLUE);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawOval(cx, cy, r * 2, r * 2);
            int fontSize = seat.length() <= 3 ? 28 : 22;
            g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            g2.setColor(NAVY);
            fm = g2.getFontMetrics();
            g2.drawString(seat, cx + r - fm.stringWidth(seat) / 2,
                    cy + r + fm.getAscent() / 2 - 2);

            drawStatusPill(g2, status, scx + (seatW - 90) / 2, cy + r * 2 + 12);

            // ── Barcode strip ─────────────────────────────────────────
            drawBarcode(g2, 24, h - 32, w - seatW - 44, 17);

            // ── Card outline ──────────────────────────────────────────
            g2.setColor(GREY_LINE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
        }

        private void drawField(Graphics2D g2, String label, String val, int x, int y) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(148, 163, 184));
            g2.drawString(label, x, y);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor("STATUS".equals(label) ? statusColor(val) : new Color(15, 23, 42));
            g2.drawString(trunc(val != null ? val : "—", 28), x, y + 16);
        }

        private void drawPlaneArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            g2.setColor(BLUE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x1, y1, x2 - 8, y1);
            int[] ax = {x2 - 14, x2, x2 - 14};
            int[] ay = {y1 - 6, y1, y1 + 6};
            g2.fillPolygon(ax, ay, 3);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            g2.drawString("to", x1 + (x2 - x1) / 2 - 8, y1 - 3);
        }

        private void drawStatusPill(Graphics2D g2, String s, int x, int y) {
            g2.setColor(statusBg(s));
            g2.fillRoundRect(x, y, 90, 22, 12, 12);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(statusColor(s));
            String lbl = s != null ? s.toUpperCase() : "—";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, x + (90 - fm.stringWidth(lbl)) / 2, y + 14);
        }

        private void drawBarcode(Graphics2D g2, int x, int y, int totalW, int barH) {
            int[] widths = {2,1,3,1,2,1,1,2,1,3,2,1,1,2,3,1,2,1,1,
                            2,1,3,1,1,2,1,2,3,1,2,1,1,2,1,2,1,3,2};
            int cx = x;
            boolean dark = true;
            for (int bw : widths) {
                if (cx >= x + totalW) break;
                if (dark) {
                    g2.setColor(new Color(15, 23, 42));
                    g2.fillRect(cx, y, bw, barH);
                }
                cx += bw + 1;
                dark = !dark;
            }
            g2.setFont(new Font("Courier New", Font.PLAIN, 8));
            g2.setColor(new Color(148, 163, 184));
            g2.drawString("* A E R O S Y S *", x, y + barH + 10);
        }

        private String trunc(String s, int max) {
            if (s == null) return "—";
            return s.length() > max ? s.substring(0, max - 1) + "…" : s;
        }

        Color statusColor(String s) {
            if (s == null) return new Color(100, 116, 139);
            if (s.equalsIgnoreCase("Active"))    return GREEN;
            if (s.equalsIgnoreCase("Used"))      return BLUE;
            if (s.equalsIgnoreCase("Cancelled")) return RED;
            return AMBER;
        }

        Color statusBg(String s) {
            if (s == null) return new Color(241, 245, 249);
            if (s.equalsIgnoreCase("Active"))    return new Color(209, 250, 229);
            if (s.equalsIgnoreCase("Used"))      return new Color(219, 234, 254);
            if (s.equalsIgnoreCase("Cancelled")) return new Color(254, 226, 226);
            return new Color(254, 243, 199);
        }
    }

    // ── Shared cell reader ────────────────────────────────────────────
    private static String cell(javax.swing.table.DefaultTableModel m, int row, int col) {
        Object v = m.getValueAt(row, col);
        return v != null ? v.toString() : "—";
    }

    // ── Styled button factory ─────────────────────────────────────────
    private static JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker()
                        : getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(210, 40));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}