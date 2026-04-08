package Gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class BookingPaymentPrinter {
    private BookingPaymentPrinter() {}

    static final Color NAVY       = new Color(10,  18,  50);
    static final Color NAVY_LIGHT = new Color(20,  45, 110);
    static final Color BLUE       = new Color(59, 130, 246);
    static final Color GREEN      = new Color(5,  150, 105);
    static final Color AMBER      = new Color(217,119,   6);
    static final Color RED        = new Color(220,  38,  38);
    static final Color GREY_LINE  = new Color(226,232,240);

    public static void fromAdminTable(Window parent,
            javax.swing.table.DefaultTableModel model, int row) {
        showAndPrint(parent,
                cell(model,row,0), cell(model,row,1), cell(model,row,2),
                cell(model,row,3), cell(model,row,4), cell(model,row,5),
                cell(model,row,6), cell(model,row,7), cell(model,row,8),
                cell(model,row,9), cell(model,row,10), cell(model,row,11),
                cell(model,row,12), cell(model,row,13));
    }

    // Entry point: PassengerUI My Bookings panel (9 cols)
    // [0]id [1]flightNo [2]origin [3]dest [4]departure
    // [5]seats [6]total [7]status [8]bookedAt
    public static void fromPassengerTable(Window parent,
            javax.swing.table.DefaultTableModel model,
            int row, String passengerName) {
        showAndPrint(parent,
                cell(model,row,0), passengerName, "—",
                cell(model,row,1), cell(model,row,2), cell(model,row,3),
                cell(model,row,4), cell(model,row,5), cell(model,row,6),
                cell(model,row,7), "—", "—", "—", cell(model,row,8));
    }

    private static void showAndPrint(Window parent,
            String bookingId, String passenger, String passport,
            String flightNo, String origin, String destination,
            String departure, String seats, String total,
            String bookingStatus, String payStatus, String payMethod,
            String paidAt, String bookedAt) {
        ReceiptPanel receipt = new ReceiptPanel(bookingId, passenger, passport,
                flightNo, origin, destination, departure, seats, total,
                bookingStatus, payStatus, payMethod, paidAt, bookedAt);
        showDialog(parent, receipt, bookingId);
    }

    private static void showDialog(Window parent, ReceiptPanel receipt, String bookingId) {
        JDialog dlg = new JDialog(parent,
                "Booking Receipt  \u2013  #" + bookingId,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(228,232,243));
        JPanel padded = new JPanel(new FlowLayout(FlowLayout.CENTER,30,26));
        padded.setOpaque(false); padded.add(receipt);
        root.add(padded, BorderLayout.CENTER);
        root.add(buildBar(dlg, receipt), BorderLayout.SOUTH);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "print");
        root.getActionMap().put("print", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { startPrintJob(receipt, dlg); }
        });
        dlg.setContentPane(root); dlg.pack();
        dlg.setLocationRelativeTo(parent); dlg.setVisible(true);
    }

    private static JPanel buildBar(JDialog dlg, ReceiptPanel receipt) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER,14,13));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,GREY_LINE));
        JButton btnPrint = styledBtn("Print / Save as PDF", BLUE);
        btnPrint.setToolTipText("Ctrl+P \u2014 choose 'Save as PDF' in the print dialog");
        btnPrint.addActionListener(e -> startPrintJob(receipt, dlg));
        JButton btnClose = styledBtn("Close", new Color(100,116,139));
        btnClose.addActionListener(e -> dlg.dispose());
        JLabel hint = new JLabel("Tip: choose 'Save as PDF' in the print dialog to download.");
        hint.setFont(new Font("Segoe UI",Font.ITALIC,11));
        hint.setForeground(new Color(148,163,184));
        bar.add(btnPrint); bar.add(btnClose); bar.add(hint);
        return bar;
    }

    private static void startPrintJob(ReceiptPanel receipt, Component dialogParent) {
        int pw = receipt.getPreferredSize().width;
        int ph = receipt.getPreferredSize().height;
        java.awt.image.BufferedImage snapshot =
                new java.awt.image.BufferedImage(pw*2, ph*2,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = snapshot.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        sg.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        sg.scale(2,2); receipt.paint(sg); sg.dispose();

        Thread t = new Thread(() -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pf = job.defaultPage(); Paper paper = pf.getPaper();
            double pageW=595, pageH=842, margin=36;
            paper.setSize(pageW,pageH); paper.setImageableArea(margin,margin,pageW-margin*2,pageH-margin*2);
            pf.setPaper(paper); pf.setOrientation(PageFormat.PORTRAIT);
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex>0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2=(Graphics2D)graphics;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double iw=pageFormat.getImageableWidth(), ih=pageFormat.getImageableHeight();
                double scale=Math.min(iw/pw,ih/ph);
                g2.scale(scale,scale); g2.translate((iw/scale-pw)/2.0,0);
                g2.drawImage(snapshot,0,0,pw,ph,null);
                return Printable.PAGE_EXISTS;
            }, pf);
            job.setJobName("AeroSys Booking Receipt");
            if (job.printDialog()) {
                try { job.print(); } catch (PrinterException ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(dialogParent,
                                    "Print failed: "+ex.getMessage(),"Print Error",JOptionPane.ERROR_MESSAGE));
                }
            }
        }, "AeroSys-BookingPrintThread");
        t.setDaemon(true); t.start();
    }

    public static class ReceiptPanel extends JPanel {
        private final String bookingId, passenger, passport, flightNo;
        private final String origin, destination, departure, seats, total;
        private final String bookingStatus, payStatus, payMethod, paidAt, bookedAt;
        static final int CARD_W=600, CARD_H=400;

        public ReceiptPanel(String bookingId, String passenger, String passport,
                String flightNo, String origin, String destination, String departure,
                String seats, String total, String bookingStatus, String payStatus,
                String payMethod, String paidAt, String bookedAt) {
            this.bookingId=bookingId; this.passenger=passenger; this.passport=passport;
            this.flightNo=flightNo; this.origin=origin; this.destination=destination;
            this.departure=departure; this.seats=seats; this.total=total;
            this.bookingStatus=bookingStatus; this.payStatus=payStatus;
            this.payMethod=payMethod; this.paidAt=paidAt; this.bookedAt=bookedAt;
            setPreferredSize(new Dimension(CARD_W,CARD_H)); setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);
            drawReceipt(g2); g2.dispose();
        }

        private void drawReceipt(Graphics2D g2) {
            int w=CARD_W, h=CARD_H, hdrH=72;
            for(int i=6;i>=1;i--){g2.setColor(new Color(0,0,0,8));g2.fillRoundRect(i,i,w,h,20,20);}
            g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,w,h,18,18);
            Shape clip=g2.getClip();
            g2.setClip(new RoundRectangle2D.Double(0,0,w,h,18,18));
            g2.setPaint(new GradientPaint(0,0,NAVY,w,0,NAVY_LIGHT)); g2.fillRect(0,0,w,hdrH);
            g2.setColor(GREEN); g2.fillRect(0,0,w,5); g2.setClip(clip);
            int lsz=40,lx=20,ly=(hdrH-lsz)/2;
            ImageIcon ico=AeroComponents.icon("logo.png",lsz,lsz);
            if(ico!=null) g2.drawImage(ico.getImage(),lx,ly,lsz,lsz,null);
            else{g2.setColor(new Color(255,255,255,35));g2.fillOval(lx,ly,lsz,lsz);}
            int tx=lx+lsz+12;
            g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,22));
            g2.drawString("AeroSys",tx,ly+26);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10)); g2.setColor(new Color(148,163,200));
            g2.drawString("BOOKING RECEIPT",tx,ly+42);
            FontMetrics fm; String label="BOOKING #"+bookingId;
            g2.setFont(new Font("Segoe UI",Font.BOLD,13)); g2.setColor(new Color(180,200,230));
            fm=g2.getFontMetrics(); g2.drawString(label,w-fm.stringWidth(label)-20,ly+24);
            String ts="Printed: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm"));
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10)); g2.setColor(new Color(120,145,185));
            fm=g2.getFontMetrics(); g2.drawString(ts,w-fm.stringWidth(ts)-20,ly+42);
            g2.setColor(GREY_LINE);
            g2.setStroke(new BasicStroke(1.2f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{6,4},0));
            g2.drawLine(16,hdrH+8,w-16,hdrH+8);
            g2.setStroke(new BasicStroke(1f)); g2.setColor(new Color(228,232,243));
            g2.fillOval(-11,hdrH-1,22,22); g2.fillOval(w-11,hdrH-1,22,22);
            g2.setColor(GREY_LINE); g2.drawOval(-11,hdrH-1,22,22); g2.drawOval(w-11,hdrH-1,22,22);
            g2.setStroke(new BasicStroke(1f));
            int bodyY=hdrH+22, col1=24, col2=w/2+10, rowH=44;
            drawField(g2,"PASSENGER",trunc(passenger,24),col1,bodyY);
            drawField(g2,"ROUTE",trunc(origin,9)+"\u2192"+trunc(destination,9),col2,bodyY);
            drawField(g2,"FLIGHT NO",flightNo,col1,bodyY+rowH);
            drawField(g2,"DEPARTURE",trunc(departure,22),col2,bodyY+rowH);
            drawField(g2,"SEATS",seats,col1,bodyY+rowH*2);
            drawField(g2,"BOOKED AT",trunc(bookedAt,16),col2,bodyY+rowH*2);
            drawField(g2,"PASSPORT",trunc(passport,14),col1,bodyY+rowH*3);
            drawField(g2,"PAY METHOD",trunc(payMethod,16),col2,bodyY+rowH*3);
            int divY=bodyY+rowH*4+4;
            g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(0.8f));
            g2.drawLine(16,divY,w-16,divY);
            int footY=divY+14;
            drawStatusPill(g2,"BOOKING: "+bookingStatus,col1,footY,bookingStatusColor(bookingStatus),bookingStatusBg(bookingStatus));
            if(!"—".equals(payStatus))
                drawStatusPill(g2,"PAYMENT: "+payStatus,col1+170,footY,payStatusColor(payStatus),payStatusBg(payStatus));
            String totalLabel="$"+total;
            g2.setFont(new Font("Segoe UI",Font.BOLD,28)); g2.setColor(new Color(15,23,42));
            fm=g2.getFontMetrics(); g2.drawString(totalLabel,w-fm.stringWidth(totalLabel)-24,footY+22);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10)); g2.setColor(new Color(100,116,139));
            g2.drawString("TOTAL AMOUNT",w-106,footY+36);
            if(!"—".equals(paidAt)&&!paidAt.isBlank()){
                g2.setFont(new Font("Segoe UI",Font.PLAIN,10)); g2.setColor(new Color(100,116,139));
                g2.drawString("Paid at: "+trunc(paidAt,16),col1,footY+36);
            }
            drawBarcode(g2,col1,h-28,w-48,14);
            g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0,0,w-1,h-1,18,18);
        }

        private void drawField(Graphics2D g2,String label,String val,int x,int y){
            g2.setFont(new Font("Segoe UI",Font.PLAIN,9)); g2.setColor(new Color(148,163,184));
            g2.drawString(label,x,y);
            g2.setFont(new Font("Segoe UI",Font.BOLD,13)); g2.setColor(new Color(15,23,42));
            g2.drawString(val!=null?val:"—",x,y+16);
        }
        private void drawStatusPill(Graphics2D g2,String text,int x,int y,Color fg,Color bg){
            int pw2=155,ph=24; g2.setColor(bg); g2.fillRoundRect(x,y,pw2,ph,10,10);
            g2.setFont(new Font("Segoe UI",Font.BOLD,10)); g2.setColor(fg);
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(text,x+(pw2-fm.stringWidth(text))/2,y+16);
        }
        private void drawBarcode(Graphics2D g2,int x,int y,int totalW,int barH){
            int[]widths={2,1,3,1,2,1,1,2,1,3,2,1,1,2,3,1,2,1,1,2,1,3,1,1,2,1,2,3,1,2,1,1,2,1,2,1,3,2};
            int cx=x; boolean dark=true;
            for(int bw:widths){if(cx>=x+totalW)break;if(dark){g2.setColor(new Color(15,23,42));g2.fillRect(cx,y,bw,barH);}cx+=bw+1;dark=!dark;}
            g2.setFont(new Font("Courier New",Font.PLAIN,8)); g2.setColor(new Color(148,163,184));
            g2.drawString("* A E R O S Y S  R E C E I P T *",x,y+barH+10);
        }
        private String trunc(String s,int max){if(s==null||s.isBlank())return"—";return s.length()>max?s.substring(0,max-1)+"\u2026":s;}
        Color bookingStatusColor(String s){if(s==null)return new Color(100,116,139);if(s.equalsIgnoreCase("Confirmed"))return GREEN;if(s.equalsIgnoreCase("Cancelled"))return RED;return AMBER;}
        Color bookingStatusBg(String s){if(s==null)return new Color(241,245,249);if(s.equalsIgnoreCase("Confirmed"))return new Color(209,250,229);if(s.equalsIgnoreCase("Cancelled"))return new Color(254,226,226);return new Color(254,243,199);}
        Color payStatusColor(String s){if(s==null)return new Color(100,116,139);if(s.equalsIgnoreCase("Paid"))return GREEN;if(s.equalsIgnoreCase("Refunded"))return RED;return AMBER;}
        Color payStatusBg(String s){if(s==null)return new Color(241,245,249);if(s.equalsIgnoreCase("Paid"))return new Color(209,250,229);if(s.equalsIgnoreCase("Refunded"))return new Color(254,226,226);return new Color(254,243,199);}
    }

    private static String cell(javax.swing.table.DefaultTableModel m,int row,int col){if(col>=m.getColumnCount())return"—";Object v=m.getValueAt(row,col);return v!=null?v.toString():"—";}
    private static JButton styledBtn(String text,Color bg){JButton b=new JButton(text){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(getModel().isPressed()?bg.darker():getModel().isRollover()?bg.brighter():bg);g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);g2.setColor(Color.WHITE);g2.setFont(new Font("Segoe UI",Font.BOLD,13));FontMetrics fm=g2.getFontMetrics();g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);g2.dispose();}};b.setPreferredSize(new Dimension(210,40));b.setFocusPainted(false);b.setBorderPainted(false);b.setContentAreaFilled(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
}
