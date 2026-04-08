package Gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public final class FlightReportPrinter {
    private FlightReportPrinter() {}

    static final Color NAVY       = new Color(10,  18,  50);
    static final Color NAVY_LIGHT = new Color(20,  45, 110);
    static final Color BLUE       = new Color(59, 130, 246);
    static final Color GREEN      = new Color(5,  150, 105);
    static final Color AMBER      = new Color(217,119,   6);
    static final Color RED        = new Color(220,  38,  38);
    static final Color GREY_LINE  = new Color(226,232,240);
    static final Color ROW_EVEN   = Color.WHITE;
    static final Color ROW_ODD    = new Color(248,250,252);
    static final Color HDR_BG     = new Color(241,245,249);

    private static final int ROWS_PER_PAGE = 15;

        public static void fromFlightsTable(Window parent,
            DefaultTableModel model, String employeeName) {
        List<String[]> rows = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            rows.add(new String[]{
                cell(model,r,1), cell(model,r,2), cell(model,r,3),
                cell(model,r,4), cell(model,r,5), cell(model,r,6),
                cell(model,r,7), cell(model,r,8), cell(model,r,9)
            });
        }
        long scheduled = rows.stream().filter(r -> r[8].equalsIgnoreCase("Scheduled")).count();
        long delayed   = rows.stream().filter(r -> r[8].equalsIgnoreCase("Delayed")).count();
        long cancelled = rows.stream().filter(r -> r[8].equalsIgnoreCase("Cancelled")).count();
        long completed = rows.stream().filter(r -> r[8].equalsIgnoreCase("Completed")).count();
        showPreview(parent, rows, employeeName, scheduled, delayed, cancelled, completed);
    }

    private static void showPreview(Window parent, List<String[]> rows,
            String employeeName, long scheduled, long delayed,
            long cancelled, long completed) {
        ReportPreviewPanel preview = new ReportPreviewPanel(rows, employeeName,
                scheduled, delayed, cancelled, completed);
        JDialog dlg = new JDialog(parent,
                "Flight Schedule Report  \u2013  " + rows.size() + " flights",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(true); dlg.setPreferredSize(new Dimension(820,700));
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(228,232,243));
        JScrollPane scroll = new JScrollPane(preview);
        scroll.setBorder(BorderFactory.createEmptyBorder(20,20,0,20));
        scroll.setBackground(new Color(228,232,243));
        scroll.getViewport().setBackground(new Color(228,232,243));
        root.add(scroll, BorderLayout.CENTER);
        root.add(buildBar(dlg, rows, employeeName, scheduled, delayed, cancelled, completed),
                BorderLayout.SOUTH);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "print");
        root.getActionMap().put("print", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                startPrintJob(rows, employeeName, scheduled, delayed, cancelled, completed, dlg);
            }
        });
        dlg.setContentPane(root); dlg.pack();
        dlg.setLocationRelativeTo(parent); dlg.setVisible(true);
    }

    private static JPanel buildBar(JDialog dlg, List<String[]> rows,
            String employeeName, long scheduled, long delayed,
            long cancelled, long completed) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER,14,13));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,GREY_LINE));
        JButton btnPrint = styledBtn("Print / Save as PDF", BLUE);
        btnPrint.setToolTipText("Ctrl+P \u2014 choose 'Save as PDF' to download.");
        btnPrint.addActionListener(e ->
                startPrintJob(rows, employeeName, scheduled, delayed, cancelled, completed, dlg));
        JButton btnClose = styledBtn("Close", new Color(100,116,139));
        btnClose.addActionListener(e -> dlg.dispose());
        JLabel info = new JLabel(rows.size() + " flight(s)  \u2022  "
                + scheduled + " Scheduled  \u2022  " + delayed + " Delayed  \u2022  "
                + cancelled + " Cancelled  \u2022  " + completed + " Completed");
        info.setFont(new Font("Segoe UI",Font.ITALIC,11));
        info.setForeground(new Color(100,116,139));
        bar.add(btnPrint); bar.add(btnClose); bar.add(info);
        return bar;
    }

    private static void startPrintJob(List<String[]> rows, String employeeName,
            long scheduled, long delayed, long cancelled, long completed,
            Component parent) {
        Thread t = new Thread(() -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pf = job.defaultPage(); Paper paper = pf.getPaper();
            double pageW=595, pageH=842, margin=36;
            paper.setSize(pageW,pageH);
            paper.setImageableArea(margin,margin,pageW-margin*2,pageH-margin*2);
            pf.setPaper(paper); pf.setOrientation(PageFormat.PORTRAIT);
            int totalPages = Math.max(1,(int)Math.ceil((double)rows.size()/ROWS_PER_PAGE));
            job.setPrintable((graphics,pageFormat,pageIndex) -> {
                if(pageIndex>=totalPages) return Printable.NO_SUCH_PAGE;
                Graphics2D g2=(Graphics2D)graphics;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);
                g2.translate(pageFormat.getImageableX(),pageFormat.getImageableY());
                drawReportPage(g2,(int)pageFormat.getImageableWidth(),(int)pageFormat.getImageableHeight(),
                        rows,employeeName,scheduled,delayed,cancelled,completed,pageIndex,totalPages);
                return Printable.PAGE_EXISTS;
            },pf);
            job.setJobName("AeroSys Flight Schedule Report");
            if(job.printDialog()){
                try{job.print();}catch(PrinterException ex){
                    SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(parent,
                            "Print failed: "+ex.getMessage(),"Print Error",JOptionPane.ERROR_MESSAGE));
                }
            }
        },"AeroSys-FlightReportPrintThread");
        t.setDaemon(true); t.start();
    }

    static void drawReportPage(Graphics2D g2, int W, int H,
            List<String[]> rows, String employeeName,
            long scheduled, long delayed, long cancelled, long completed,
            int pageIndex, int totalPages) {
        int y = drawReportHeader(g2, W, employeeName, pageIndex, totalPages, 0);
        if (pageIndex==0) y = drawStatsBar(g2, W, rows.size(), scheduled, delayed, cancelled, completed, y);
        drawTableSection(g2, W, H, rows, pageIndex, y);
        drawPageFooter(g2, W, H, pageIndex, totalPages);
    }

    private static int drawReportHeader(Graphics2D g2, int W,
            String employeeName, int pageIndex, int totalPages, int y) {
        int hdrH=62;
        g2.setPaint(new GradientPaint(0,y,NAVY,W,y,NAVY_LIGHT)); g2.fillRect(0,y,W,hdrH);
        g2.setColor(BLUE); g2.fillRect(0,y,W,4);
        ImageIcon ico=AeroComponents.icon("logo.png",42,42);
        if(ico!=null) g2.drawImage(ico.getImage(),10,y+10,42,42,null);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,20));
        g2.drawString("AeroSys",62,y+28);
        g2.setFont(new Font("Segoe UI",Font.PLAIN,11)); g2.setColor(new Color(148,163,200));
        g2.drawString("FLIGHT SCHEDULE REPORT",62,y+44);
        String ts=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm"));
        g2.setFont(new Font("Segoe UI",Font.PLAIN,10)); g2.setColor(new Color(148,163,200));
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString("Generated: "+ts,W-fm.stringWidth("Generated: "+ts)-8,y+24);
        g2.drawString("By: "+employeeName,W-fm.stringWidth("By: "+employeeName)-8,y+38);
        String pg="Page "+(pageIndex+1)+" of "+totalPages;
        g2.setFont(new Font("Segoe UI",Font.BOLD,10)); g2.setColor(new Color(100,120,160));
        fm=g2.getFontMetrics(); g2.drawString(pg,W-fm.stringWidth(pg)-8,y+52);
        return y+hdrH+8;
    }

    private static int drawStatsBar(Graphics2D g2, int W, int total,
            long scheduled, long delayed, long cancelled, long completed, int y) {
        int barH=48;
        g2.setColor(new Color(248,250,252)); g2.fillRect(0,y,W,barH);
        g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(0.8f)); g2.drawRect(0,y,W,barH);
        int cellW=W/4;
        String[]nums={String.valueOf(total),String.valueOf(scheduled),String.valueOf(delayed),String.valueOf(cancelled+completed)};
        String[]lbls={"Total Flights","Scheduled","Delayed","Cancelled / Done"};
        Color[]colors={BLUE,GREEN,AMBER,RED};
        for(int i=0;i<4;i++){
            int cx=i*cellW;
            if(i>0){g2.setColor(GREY_LINE);g2.drawLine(cx,y+6,cx,y+barH-6);}
            g2.setFont(new Font("Segoe UI",Font.BOLD,22)); g2.setColor(colors[i]);
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(nums[i],cx+(cellW-fm.stringWidth(nums[i]))/2,y+28);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,9)); g2.setColor(new Color(100,116,139));
            fm=g2.getFontMetrics(); g2.drawString(lbls[i],cx+(cellW-fm.stringWidth(lbls[i]))/2,y+42);
        }
        return y+barH+10;
    }

    private static int drawTableSection(Graphics2D g2, int W, int H,
            List<String[]> rows, int pageIndex, int startY) {
        String[]headers={"Flight No","From","To","Departure","Arrival","Seats","Avail","Price ($)","Status"};
        float[]weights={0.11f,0.10f,0.10f,0.16f,0.14f,0.07f,0.07f,0.10f,0.10f};
        // last weight absorbs rounding
        float last=1f; for(float w:weights) last-=w; weights[weights.length-1]+=last;
        int[]colX=new int[headers.length], colW=new int[headers.length]; int curX=0;
        for(int i=0;i<headers.length;i++){colW[i]=(int)(W*weights[i]);colX[i]=curX;curX+=colW[i];}
        int rowH=22, hdrH=26, y=startY;
        g2.setColor(HDR_BG); g2.fillRect(0,y,W,hdrH);
        g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(0.8f)); g2.drawRect(0,y,W,hdrH);
        g2.setFont(new Font("Segoe UI",Font.BOLD,9)); g2.setColor(new Color(71,85,105));
        for(int i=0;i<headers.length;i++){
            FontMetrics fm=g2.getFontMetrics(); int tx=colX[i]+(colW[i]-fm.stringWidth(headers[i]))/2;
            g2.drawString(headers[i],tx,y+17);
            if(i>0){g2.setColor(GREY_LINE);g2.drawLine(colX[i],y+4,colX[i],y+hdrH-4);g2.setColor(new Color(71,85,105));}
        }
        y+=hdrH;
        int startRow=pageIndex*ROWS_PER_PAGE, endRow=Math.min(startRow+ROWS_PER_PAGE,rows.size());
        for(int r=startRow;r<endRow;r++){
            String[]row=rows.get(r); Color bg=(r%2==0)?ROW_EVEN:ROW_ODD;
            g2.setColor(bg); g2.fillRect(0,y,W,rowH);
            g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(0.5f)); g2.drawLine(0,y+rowH-1,W,y+rowH-1);
            for(int c=0;c<headers.length;c++){
                String val=row[c]; Color fg=cellColor(val,c);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,9)); g2.setColor(fg);
                FontMetrics fm=g2.getFontMetrics();
                String display=trunc(val,Math.max(4,colW[c]/6+1));
                int tx=colX[c]+(colW[c]-fm.stringWidth(display))/2;
                g2.drawString(display,tx,y+15);
                if(c>0){g2.setColor(GREY_LINE);g2.drawLine(colX[c],y+3,colX[c],y+rowH-3);g2.setColor(fg);}
            }
            y+=rowH;
        }
        g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(0.8f)); g2.drawRect(0,startY,W,y-startY);
        return y;
    }

    private static Color cellColor(String val,int col){
        if(col!=8) return new Color(30,41,59);
        if(val==null) return new Color(100,116,139);
        if(val.equalsIgnoreCase("Scheduled")) return GREEN;
        if(val.equalsIgnoreCase("Delayed"))   return AMBER;
        if(val.equalsIgnoreCase("Cancelled")) return RED;
        if(val.equalsIgnoreCase("Completed")) return BLUE;
        return new Color(100,116,139);
    }

    private static void drawPageFooter(Graphics2D g2,int W,int H,int pageIndex,int totalPages){
        g2.setColor(GREY_LINE); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(0,H-18,W,H-18);
        g2.setFont(new Font("Segoe UI",Font.PLAIN,9)); g2.setColor(new Color(148,163,184));
        g2.drawString("AeroSys  \u2022  CSC236  \u2022  Confidential",0,H-5);
        String pg="Page "+(pageIndex+1)+" / "+totalPages;
        FontMetrics fm=g2.getFontMetrics(); g2.drawString(pg,W-fm.stringWidth(pg),H-5);
    }

    private static String trunc(String s,int max){if(s==null)return"\u2014";return s.length()>max?s.substring(0,max-1)+"\u2026":s;}

    static class ReportPreviewPanel extends JPanel {
        private final List<String[]> rows;
        private final String employeeName;
        private final long scheduled, delayed, cancelled, completed;
        private static final int PREVIEW_W=720;
        private static final int PREVIEW_H=(int)(PREVIEW_W*842.0/595.0);

        ReportPreviewPanel(List<String[]> rows, String employeeName,
                long scheduled, long delayed, long cancelled, long completed) {
            this.rows=rows; this.employeeName=employeeName;
            this.scheduled=scheduled; this.delayed=delayed;
            this.cancelled=cancelled; this.completed=completed;
            setPreferredSize(new Dimension(PREVIEW_W,PREVIEW_H));
            setBackground(new Color(228,232,243));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);
            for(int i=5;i>=1;i--){g2.setColor(new Color(0,0,0,6));g2.fillRoundRect(i+2,i+2,PREVIEW_W-4,PREVIEW_H-4,4,4);}
            g2.setColor(Color.WHITE); g2.fillRect(0,0,PREVIEW_W,PREVIEW_H);
            double scaleX=PREVIEW_W/595.0, scaleY=PREVIEW_H/842.0;
            g2.scale(scaleX,scaleY);
            int totalPages=Math.max(1,(int)Math.ceil((double)rows.size()/ROWS_PER_PAGE));
            drawReportPage(g2,595,842,rows,employeeName,scheduled,delayed,cancelled,completed,0,totalPages);
            if(totalPages>1){
                g2.scale(1.0/scaleX,1.0/scaleY);
                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(PREVIEW_W/2-110,PREVIEW_H-40,220,30,10,10);
                g2.setFont(new Font("Segoe UI",Font.BOLD,12)); g2.setColor(Color.WHITE);
                FontMetrics fm=g2.getFontMetrics();
                String msg="Preview: Page 1 of "+totalPages+"  (all pages print)";
                g2.drawString(msg,PREVIEW_W/2-fm.stringWidth(msg)/2,PREVIEW_H-20);
            }
            g2.dispose();
        }
    }

    private static String cell(DefaultTableModel m,int row,int col){if(col>=m.getColumnCount())return"\u2014";Object v=m.getValueAt(row,col);return v!=null?v.toString():"\u2014";}
    private static JButton styledBtn(String text,Color bg){JButton b=new JButton(text){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(getModel().isPressed()?bg.darker():getModel().isRollover()?bg.brighter():bg);g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);g2.setColor(Color.WHITE);g2.setFont(new Font("Segoe UI",Font.BOLD,13));FontMetrics fm=g2.getFontMetrics();g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);g2.dispose();}};b.setPreferredSize(new Dimension(210,40));b.setFocusPainted(false);b.setBorderPainted(false);b.setContentAreaFilled(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
}
