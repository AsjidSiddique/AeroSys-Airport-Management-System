package Gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * AeroComponents – Shared UI widget library for ALL AeroSys GUI windows.
 *
 * OOP Concepts Applied:
 *   Utility class / Nested static classes – widgets are public static inner classes
 *   Encapsulation – each widget manages its own paint and state internally
 *   Inheritance   – all widgets extend standard Swing components
 
 */
public final class AeroComponents {
    private AeroComponents() {}

    // ─────────────────────────────────────────────────────────────────
    //  SHARED ICON LOADER
    // ─────────────────────────────────────────────────────────────────
    public static ImageIcon icon(String name, int w, int h) {
        try {
            java.net.URL url = AeroComponents.class.getResource("/icons/" + name);
            if (url == null) return null;
            return new ImageIcon(new ImageIcon(url).getImage()
                    .getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    // ═════════════════════════════════════════════════════════════════
    //  SIDEBAR BUTTON
    //  Replaces: SidebarButton (AdminUI) + PassengerSidebarBtn (PassengerUI)
    // ═════════════════════════════════════════════════════════════════
    public static class SidebarButton extends JButton {
        private boolean active = false;
        private float   hoverAlpha = 0f;
        private Color   accent;
        private javax.swing.Timer hoverTimer;

        public SidebarButton(String text, Color accent) {
            super(text);
            this.accent = accent;
            setFocusPainted(false); setBorderPainted(false);
            setContentAreaFilled(false); setOpaque(false);
            setForeground(new Color(180, 190, 210));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
            setPreferredSize(new Dimension(220, 46));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if (!active) animateHover(true);  }
                public void mouseExited (MouseEvent e) { if (!active) animateHover(false); }
            });
        }

        public SidebarButton(String text, Color accent, String iconFile) {
            this(text, accent);
            ImageIcon ic = icon(iconFile, 18, 18);
            if (ic != null) setIcon(ic);
        }

        private void animateHover(boolean in) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new javax.swing.Timer(16, null);
            hoverTimer.addActionListener(e -> {
                hoverAlpha += in ? 0.1f : -0.1f;
                hoverAlpha = Math.max(0f, Math.min(1f, hoverAlpha));
                repaint();
                if ((in && hoverAlpha >= 1f) || (!in && hoverAlpha <= 0f)) hoverTimer.stop();
            });
            hoverTimer.start();
        }

        public void setActive(boolean a) {
            this.active = a;
            hoverAlpha = a ? 1f : 0f;
            setForeground(a ? Color.WHITE : new Color(180, 190, 210));
            setFont(new Font("Segoe UI", a ? Font.BOLD : Font.PLAIN, 14));
            repaint();
        }
        public boolean isActive() { return active; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-8, 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 8, 4, getHeight()-16, 4, 4);
            } else if (hoverAlpha > 0f) {
                g2.setColor(new Color(255, 255, 255, (int)(hoverAlpha * 15)));
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-8, 12, 12);
            }
            Icon ico = getIcon();
            int ix = 20, iw = 0;
            if (ico != null) { iw = ico.getIconWidth(); ico.paintIcon(this, g2, ix, (getHeight()-ico.getIconHeight())/2); }
            g2.setFont(getFont()); g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), ix + iw + (iw > 0 ? 10 : 0), (getHeight()+fm.getAscent()-fm.getDescent())/2);
            g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  STAT CARD
    //  Replaces: StatCard (AdminUI) + PassengerStatCard (PassengerUI)
    //  isMoney=true  → formats value as "$1,234" instead of "1,234"
    // ═════════════════════════════════════════════════════════════════
    public static class StatCard extends JPanel {
        private JLabel valueLabel, titleLabel, iconLabel;
        private Color  accentColor;
        private int    currentValue = 0, targetValue = 0;
        private final boolean isMoney;

        /** Standard card — integer count display */
        public StatCard(String title, Color accent) {
            this(title, accent, false);
        }

        /** isMoney=true formats value as "$N,NNN" */
        public StatCard(String title, Color accent, boolean isMoney) {
            this.accentColor = accent;
            this.isMoney = isMoney;
            setOpaque(false); setLayout(new BorderLayout());
            setPreferredSize(new Dimension(200, 140));
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            iconLabel = new JLabel();
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));
            valueLabel = new JLabel("0");
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            valueLabel.setForeground(Color.WHITE);
            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            valueLabel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 20));
            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLabel.setForeground(new Color(255, 255, 255, 180));
            titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 20));
            JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
            top.add(iconLabel, BorderLayout.WEST); top.add(valueLabel, BorderLayout.CENTER);
            add(top, BorderLayout.CENTER); add(titleLabel, BorderLayout.SOUTH);
        }

        public void setCardIcon(ImageIcon ic) { iconLabel.setIcon(ic); }

        public void animateTo(int target) {
            this.targetValue = target; currentValue = 0;
            javax.swing.Timer t = new javax.swing.Timer(20, null);
            t.addActionListener(e -> {
                int step = Math.max(1, (targetValue - currentValue) / 8);
                currentValue = Math.min(currentValue + step, targetValue);
                valueLabel.setText(isMoney
                        ? String.format("$%,d", currentValue)
                        : String.format("%,d", currentValue));
                if (currentValue >= targetValue) t.stop();
            });
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth()-8, h = getHeight()-8;
            g2.setColor(new Color(0,0,0,40)); g2.fillRoundRect(6,6,w,h,20,20);
            g2.setPaint(new GradientPaint(0,0,accentColor,w,h,accentColor.darker().darker()));
            g2.fillRoundRect(2,2,w,h,20,20);
            g2.setColor(new Color(255,255,255,30)); g2.fillRoundRect(2,2,w,h/2,20,20);
            g2.setColor(new Color(255,255,255,50)); g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(2,2,w,h,20,20); g2.dispose(); super.paintComponent(g);
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  AERO TEXT FIELD
    //  dark=false → light mode (Admin/Passenger panels, white background)
    //  dark=true  → dark mode (Login/Register, glass background)
    // ═════════════════════════════════════════════════════════════════
    public static class AeroTextField extends JTextField {
        private final String placeholder;
        private boolean focused = false;
        private final boolean dark;

        public AeroTextField(String placeholder)               { this(placeholder, false); }
        public AeroTextField(String placeholder, boolean dark) {
            this.placeholder = placeholder; this.dark = dark;
            setFont(new Font("Segoe UI", Font.PLAIN, 14)); setOpaque(false);
            setForeground(dark ? Color.WHITE : new Color(15,23,42));
            if (dark) setCaretColor(new Color(100,180,255));
            setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
            setPreferredSize(new Dimension(200, 46));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { focused=true;  repaint(); }
                public void focusLost (FocusEvent e)  { focused=false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (dark) {
                g2.setColor(new Color(255,255,255,focused?28:16)); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(focused?new Color(59,130,246):new Color(255,255,255,50));
            } else {
                g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(focused?new Color(59,130,246):new Color(203,213,225));
            }
            g2.setStroke(new BasicStroke(focused?2f:1.5f));
            g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,12,12); g2.dispose();
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D gh = (Graphics2D) g.create();
                gh.setFont(new Font("Segoe UI",Font.PLAIN,13));
                gh.setColor(dark?new Color(150,180,220,160):new Color(148,163,184));
                gh.drawString(placeholder, 14, getHeight()/2+5); gh.dispose();
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  AERO PASSWORD FIELD
    // ═════════════════════════════════════════════════════════════════
    public static class AeroPasswordField extends JPasswordField {
        private boolean focused = false;
        private final boolean dark;

        public AeroPasswordField()               { this(false); }
        public AeroPasswordField(boolean dark) {
            this.dark = dark;
            setFont(new Font("Segoe UI",Font.PLAIN,14)); setOpaque(false);
            setForeground(dark?Color.WHITE:new Color(15,23,42));
            if (dark) setCaretColor(new Color(100,180,255));
            setEchoChar('●');
            setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
            setPreferredSize(new Dimension(200,46));
            addFocusListener(new FocusAdapter(){
                public void focusGained(FocusEvent e){focused=true; repaint();}
                public void focusLost (FocusEvent e){focused=false;repaint();}
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (dark) {
                g2.setColor(new Color(255,255,255,focused?28:16)); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(focused?new Color(59,130,246):new Color(255,255,255,50));
            } else {
                g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(focused?new Color(59,130,246):new Color(203,213,225));
            }
            g2.setStroke(new BasicStroke(focused?2f:1.5f));
            g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,12,12);
            g2.dispose(); super.paintComponent(g);
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  AERO BUTTON — gradient button with hover animation
    // ═════════════════════════════════════════════════════════════════
    public static class AeroButton extends JButton {
        private final Color base;
        private float hoverAlpha = 0f;
        private javax.swing.Timer hoverTimer;

        public AeroButton(String text, Color color) {
            super(text); this.base = color;
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
            setForeground(Color.WHITE); setFont(new Font("Segoe UI",Font.BOLD,13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160,40));
            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){animateHover(true);}
                public void mouseExited (MouseEvent e){animateHover(false);}
            });
        }

        private void animateHover(boolean in) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new javax.swing.Timer(16, null);
            hoverTimer.addActionListener(ev -> {
                hoverAlpha += in ? 0.08f : -0.08f;
                hoverAlpha = Math.max(0f, Math.min(1f, hoverAlpha));
                repaint();
                if ((in && hoverAlpha>=1f)||(!in && hoverAlpha<=0f)) hoverTimer.stop();
            });
            hoverTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            Color top = hoverAlpha>0f ? base.brighter() : base;
            Color bot = hoverAlpha>0f ? base : base.darker();
            g2.setColor(new Color(0,0,0,25)); g2.fillRoundRect(2,3,w-4,h-1,12,12);
            g2.setPaint(new GradientPaint(0,0,top,0,h,bot)); g2.fillRoundRect(0,0,w,h-2,12,12);
            g2.setColor(new Color(255,255,255,30)); g2.fillRoundRect(0,0,w,h/2,12,12);
            Icon ico = getIcon(); FontMetrics fm = g2.getFontMetrics(getFont());
            int iw = ico!=null?ico.getIconWidth()+6:0, tw=fm.stringWidth(getText());
            int sx=(w-iw-tw)/2;
            if(ico!=null) ico.paintIcon(this,g2,sx,(h-ico.getIconHeight())/2-1);
            g2.setFont(getFont()); g2.setColor(Color.WHITE);
            g2.drawString(getText(),sx+iw,(h+fm.getAscent()-fm.getDescent())/2-1);
            g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  AERO TOAST — centered notification over parent window
   
    //  POSITION: Always centered over the parent window
    //  PREFIX:   Strips "SUCCESS:" / "ERROR:" from message automatically
    // ═════════════════════════════════════════════════════════════════
    public static class AeroToast extends JWindow {
        private static final Color C_OK  = new Color(16, 185, 129);
        private static final Color C_ERR = new Color(239, 68,  68);

        public AeroToast(Window parent, String rawMsg, boolean ok) {
            super(parent);
            Color accent = ok ? C_OK : C_ERR;
            String msg = rawMsg == null ? "" : rawMsg
                    .replaceFirst("^SUCCESS:", "").replaceFirst("^ERROR:", "").trim();

            // Load PNG icon — success.png (green) or false.png (red)
            ImageIcon ico = icon(ok ? "success.png" : "false.png", 32, 32);

            JPanel panel = new JPanel(new BorderLayout(16, 0)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    // Shadow
                    g2.setColor(new Color(0,0,0,55));
                    g2.fillRoundRect(5,7,getWidth()-5,getHeight()-5,20,20);
                    // Body — near-black with slight transparency
                    g2.setColor(new Color(15,23,42,240));
                    g2.fillRoundRect(0,0,getWidth()-5,getHeight()-5,20,20);
                    // Left accent stripe
                    g2.setColor(accent);
                    g2.fillRoundRect(0,14,5,getHeight()-33,4,4);
                    // Border
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),120));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0,0,getWidth()-6,getHeight()-6,20,20);
                    g2.dispose();
                }
            };
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createEmptyBorder(18,20,22,26));

            // Icon label — PNG if loaded, fallback to colored circle
            JLabel iconLbl;
            if (ico != null) {
                iconLbl = new JLabel(ico);
            } else {
                // Fallback: colored circle with + or ! text
                iconLbl = new JLabel() {
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2=(Graphics2D)g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(accent); g2.fillOval(0,0,32,32);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI",Font.BOLD,18));
                        FontMetrics fm=g2.getFontMetrics();
                        String sym = ok ? "+" : "!";
                        g2.drawString(sym,(32-fm.stringWidth(sym))/2,(32+fm.getAscent()-fm.getDescent())/2);
                        g2.dispose();
                    }
                    @Override public Dimension getPreferredSize(){return new Dimension(32,32);}
                };
            }
            panel.add(iconLbl, BorderLayout.WEST);

            // Text area
            JPanel txt = new JPanel(); txt.setOpaque(false);
            txt.setLayout(new BoxLayout(txt,BoxLayout.Y_AXIS));
            JLabel titleLbl = new JLabel(ok ? "Success" : "Error");
            titleLbl.setFont(new Font("Segoe UI",Font.BOLD,14));
            titleLbl.setForeground(accent);
            JLabel bodyLbl = new JLabel("<html><body style='width:280px;color:white'>" + msg + "</body></html>");
            bodyLbl.setFont(new Font("Segoe UI",Font.PLAIN,13));
            bodyLbl.setForeground(new Color(210,220,240));
            txt.add(titleLbl);
            txt.add(Box.createRigidArea(new Dimension(0,4)));
            txt.add(bodyLbl);
            panel.add(txt, BorderLayout.CENTER);

            // Progress countdown bar at bottom
            JPanel prog = new JPanel() {
                float pct = 1f;
                javax.swing.Timer pt = new javax.swing.Timer(35, e -> {
                    pct -= 0.011f;
                    if (pct <= 0) { pct = 0; ((javax.swing.Timer)e.getSource()).stop(); }
                    repaint();
                });
                { pt.start(); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setColor(new Color(255,255,255,25)); g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);
                    g2.setColor(accent); g2.fillRoundRect(0,0,(int)(getWidth()*pct),getHeight(),4,4);
                    g2.dispose();
                }
                @Override public Dimension getPreferredSize(){return new Dimension(0,4);}
            };
            prog.setOpaque(false);

            JPanel outer = new JPanel(new BorderLayout()); outer.setOpaque(false);
            outer.add(panel,BorderLayout.CENTER); outer.add(prog,BorderLayout.SOUTH);
            setContentPane(outer);
            pack();
            setMinimumSize(new Dimension(360, 80));

            // CENTER over parent window (not screen edge)
            if (parent != null) {
                Rectangle pb = parent.getBounds();
                int cx = pb.x + (pb.width  - getWidth())  / 2;
                int cy = pb.y + (pb.height - getHeight()) / 2;
                setLocation(cx, cy);
            } else {
                Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
                setLocation((scr.width-getWidth())/2, (scr.height-getHeight())/2 - 40);
            }

            setAlwaysOnTop(true);
            setVisible(true);
            // Auto-dismiss after 0.8 seconds
            new javax.swing.Timer(1300, e -> fadeOut()) {{ setRepeats(false); start(); }};
        }

        private void fadeOut() {
            javax.swing.Timer fo = new javax.swing.Timer(13, null);
            fo.addActionListener(e -> {
                try {
                    float op = getOpacity() - 0.07f;
                    if (op <= 0f) { fo.stop(); dispose(); return; }
                    setOpacity(op);
                } catch (Exception ex) { fo.stop(); dispose(); }
            });
            fo.start();
        }

        /** Auto-detects SUCCESS:/ERROR: prefix from result string. */
        public static void show(Window parent, String msg) {
            boolean ok = msg != null && msg.startsWith("SUCCESS:");
            SwingUtilities.invokeLater(() -> new AeroToast(parent, msg, ok));
        }

        /** Explicit ok/fail — wraps message automatically. */
        public static void show(Window parent, String msg, boolean ok) {
            SwingUtilities.invokeLater(() -> new AeroToast(parent, msg, ok));
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  ANIMATED BACKGROUND  (LoginUI + RegisterUI)
    // ═════════════════════════════════════════════════════════════════
    public static class AnimatedBackground extends JPanel {
        private static final int COUNT = 55;
        private final float[][] particles;
        private javax.swing.Timer animTimer;

        public AnimatedBackground() {
            setOpaque(false);
            particles = new float[COUNT][5];
            for (int i = 0; i < COUNT; i++) initParticle(i, true);
            animTimer = new javax.swing.Timer(30, e -> {
                for (int i = 0; i < COUNT; i++) {
                    particles[i][1] -= particles[i][2];
                    particles[i][4] -= 0.003f;
                    if (particles[i][1] < -10 || particles[i][4] <= 0) initParticle(i, false);
                }
                repaint();
            });
            animTimer.start();
        }

        private void initParticle(int i, boolean ry) {
            particles[i][0] = (float)(Math.random()*1400);
            particles[i][1] = ry ? (float)(Math.random()*900) : 920f;
            particles[i][2] = 0.3f + (float)(Math.random()*1.2f);
            particles[i][3] = 1.5f + (float)(Math.random()*3.5f);
            particles[i][4] = 0.15f + (float)(Math.random()*0.55f);
        }

        public void stopAnimation() { if (animTimer != null) animTimer.stop(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0,0,new Color(5,8,25),0,getHeight(),new Color(10,22,65)));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.setPaint(new RadialGradientPaint(new Point(getWidth()/2,getHeight()),getHeight()*0.85f,
                    new float[]{0f,0.45f,1f},
                    new Color[]{new Color(30,90,200,90),new Color(15,40,120,35),new Color(0,0,0,0)}));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.setPaint(new RadialGradientPaint(
                    new Point((int)(getWidth()*0.82f),(int)(getHeight()*0.18f)), getHeight()*0.45f,
                    new float[]{0f,1f}, new Color[]{new Color(80,40,200,50),new Color(0,0,0,0)}));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(59,130,246,18)); g2.setStroke(new BasicStroke(0.8f));
            for (int x=0; x<getWidth(); x+=60) g2.drawLine(x,0,x,getHeight());
            for (int y=0; y<getHeight(); y+=60) g2.drawLine(0,y,getWidth(),y);
            g2.setColor(new Color(59,130,246,8));
            for (int d=-getHeight(); d<getWidth(); d+=140) g2.drawLine(d,0,d+getHeight(),getHeight());
            for (float[] p : particles) {
                g2.setColor(new Color(180,210,255,(int)(p[4]*255)));
                g2.fillOval((int)p[0],(int)p[1],(int)p[3],(int)p[3]);
            }
            g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  GLASS CARD  (LoginUI / RegisterUI form container)
    // ═════════════════════════════════════════════════════════════════
    public static class GlassCard extends JPanel {
        private final Color borderColor;
        public GlassCard(Color border) {
            this.borderColor = border; setOpaque(false); setLayout(new BorderLayout());
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,18)); g2.fillRoundRect(0,0,getWidth(),getHeight(),28,28);
            g2.setPaint(new GradientPaint(0,0,new Color(255,255,255,35),0,getHeight()/3,new Color(255,255,255,5)));
            g2.fillRoundRect(0,0,getWidth(),getHeight()/2,28,28);
            g2.setColor(borderColor); g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,28,28); g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  ROLE TOGGLE  (LoginUI — Passenger / Employee switch)
    // ═════════════════════════════════════════════════════════════════
    public static class RoleToggle extends JPanel {
        private boolean employeeActive = false;
        private final JButton btnPassenger, btnEmployee;
        private static final Color ACTIVE = new Color(59,130,246);

        public RoleToggle() {
            setOpaque(false); setLayout(new GridLayout(1,2,0,0));
            setPreferredSize(new Dimension(340,48));
            btnPassenger = makeBtn("  Passenger",        false);
            btnEmployee  = makeBtn("  Employee / Admin", true);
            add(btnPassenger); add(btnEmployee);
            setPassengerActive();
        }

        private JButton makeBtn(String text, boolean isRight) {
            JButton b = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean active = isRight ? employeeActive : !employeeActive;
                    g2.setColor(active ? ACTIVE : new Color(255,255,255,25));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setFont(new Font("Segoe UI",active?Font.BOLD:Font.PLAIN,14));
                    g2.setColor(active ? Color.WHITE : new Color(180,200,240));
                    FontMetrics fm=g2.getFontMetrics(); Icon ic=getIcon();
                    int iw=ic!=null?ic.getIconWidth()+8:0;
                    int sx=(getWidth()-iw-fm.stringWidth(getText().trim()))/2;
                    if(ic!=null) ic.paintIcon(this,g2,sx,(getHeight()-ic.getIconHeight())/2);
                    g2.drawString(getText().trim(),sx+iw,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            b.setFocusPainted(false); b.setBorderPainted(false);
            b.setContentAreaFilled(false); b.setOpaque(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        public void setPassengerActive() { employeeActive=false; repaint(); }
        public void setEmployeeActive()  { employeeActive=true;  repaint(); }
        public boolean isEmployeeMode()  { return employeeActive; }
        public void addPassengerListener(ActionListener l) { btnPassenger.addActionListener(l); }
        public void addEmployeeListener (ActionListener l) { btnEmployee .addActionListener(l); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
            g2.setColor(new Color(255,255,255,40)); g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16); g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  STEP INDICATOR  (RegisterUI — 2-step wizard progress)
    // ═════════════════════════════════════════════════════════════════
    public static class StepIndicator extends JPanel {
        private int currentStep = 1;
        private static final String[] NAMES = {"Personal Info","Account Setup"};

        public StepIndicator() { setOpaque(false); setPreferredSize(new Dimension(460,60)); }
        public void setStep(int s) { currentStep=s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int cX=getWidth()/2, sp=120;
            int[] xs = {cX-sp, cX+sp};
            int lx1=xs[0]+22, lx2=xs[1]-22, ly=20;
            if (currentStep==2)
                g2.setPaint(new GradientPaint(lx1,ly,new Color(59,130,246),lx2,ly,new Color(16,185,129)));
            else
                g2.setColor(new Color(255,255,255,25));
            g2.setStroke(new BasicStroke(2.5f)); g2.drawLine(lx1,ly,lx2,ly);
            for (int i=0; i<2; i++) {
                int x=xs[i]; boolean done=i+1<currentStep, active=i+1==currentStep;
                g2.setColor(done?new Color(16,185,129):active?new Color(59,130,246):new Color(255,255,255,30));
                g2.fillOval(x-2,8,28,28);
                if (active) {
                    g2.setColor(new Color(59,130,246,60));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(x-6,4,36,36);
                }
                g2.setFont(new Font("Segoe UI",Font.BOLD,13)); g2.setColor(Color.WHITE);
                // Use plain text — no emoji
                String sym = done ? "v" : String.valueOf(i+1);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(sym, x+12-fm.stringWidth(sym)/2, 27);
                g2.setFont(new Font("Segoe UI",active?Font.BOLD:Font.PLAIN,12));
                g2.setColor(active?Color.WHITE:new Color(148,170,220));
                fm=g2.getFontMetrics();
                g2.drawString(NAMES[i], x+12-fm.stringWidth(NAMES[i])/2, 54);
            }
            g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  STRENGTH BAR  (RegisterUI — password strength indicator)
    // ═════════════════════════════════════════════════════════════════
    public static class StrengthBar extends JPanel {
        private int strength = 0;
        private static final Color[] COLORS = {
            new Color(239,68,68), new Color(249,115,22),
            new Color(234,179,8), new Color(16,185,129)
        };
        private static final String[] LABELS = {"Weak","Fair","Good","Strong"};

        public StrengthBar() { setOpaque(false); setPreferredSize(new Dimension(220,24)); }

        public void update(String pass) {
            int s=0;
            if(pass.length()>=8)s++;
            if(pass.matches(".*[A-Z].*"))s++;
            if(pass.matches(".*[0-9].*"))s++;
            if(pass.matches(".*[!@#$%^&*()_+].*"))s++;
            strength=s; repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int segW=(getWidth()-30)/4, gap=4;
            for (int i=0; i<4; i++) {
                int x=i*(segW+gap);
                g2.setColor(i<strength ? COLORS[strength-1] : new Color(255,255,255,25));
                g2.fillRoundRect(x,0,segW,8,4,4);
            }
            if (strength>0) {
                g2.setFont(new Font("Segoe UI",Font.BOLD,11));
                g2.setColor(COLORS[strength-1]);
                g2.drawString(LABELS[strength-1], getWidth()-42, 9);
            }
            g2.dispose();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  DATE PICKER — popup calendar for date selection (PassengerUI)
    // ═════════════════════════════════════════════════════════════════
    public static class AeroDatePicker extends JPanel {
        private final JTextField display;
        private JWindow   popup;
        private java.time.LocalDate  selected;
        private java.time.YearMonth  viewing;
        private static final Color ACCENT = new Color(59,130,246);

        public AeroDatePicker(String placeholder) {
            setLayout(new BorderLayout()); setOpaque(false);
            viewing = java.time.YearMonth.now();
            selected = null;
            display = new JTextField(placeholder) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            display.setOpaque(true); display.setEditable(false);
            display.setFont(new Font("Segoe UI",Font.PLAIN,13));
            display.setForeground(new Color(30,41,59));
            display.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
            display.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            display.setBackground(Color.WHITE);
            display.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){ togglePopup(); }
            });
            add(display, BorderLayout.CENTER);
        }

        private void togglePopup() {
            if (popup!=null && popup.isVisible()) { popup.dispose(); popup=null; return; }
            buildPopup();
        }

        private void buildPopup() {
            Window w = SwingUtilities.getWindowAncestor(this);
            popup = new JWindow(w);
            popup.setBackground(new Color(0,0,0,0));
            JPanel cal = new JPanel(new BorderLayout(0,0)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(10,18,40)); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setColor(ACCENT); g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14); g2.dispose();
                }
            };
            cal.setOpaque(false); cal.setPreferredSize(new Dimension(280,290));
            cal.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
            JButton prev = navBtn("<"), next = navBtn(">");
            JLabel mLbl = new JLabel("", SwingConstants.CENTER);
            mLbl.setFont(new Font("Segoe UI",Font.BOLD,14)); mLbl.setForeground(Color.WHITE);
            header.add(prev,BorderLayout.WEST); header.add(mLbl,BorderLayout.CENTER); header.add(next,BorderLayout.EAST);
            JPanel grid = new JPanel(new GridLayout(7,7,3,3)); grid.setOpaque(false);
            Runnable refresh = () -> {
                mLbl.setText(viewing.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")));
                grid.removeAll();
                String[] days = {"Su","Mo","Tu","We","Th","Fr","Sa"};
                for (String d:days) {
                    JLabel l=new JLabel(d,SwingConstants.CENTER);
                    l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(ACCENT); grid.add(l);
                }
                int first = viewing.atDay(1).getDayOfWeek().getValue() % 7;
                for (int i=0;i<first;i++) grid.add(new JLabel());
                for (int d=1; d<=viewing.lengthOfMonth(); d++) {
                    java.time.LocalDate date = viewing.atDay(d);
                    JButton btn = new JButton(String.valueOf(d)){
                        @Override protected void paintComponent(Graphics g){
                            Graphics2D g2=(Graphics2D)g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                            boolean isSel=date.equals(selected), isToday=date.equals(java.time.LocalDate.now());
                            if(isSel){g2.setColor(ACCENT);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);}
                            else if(isToday){g2.setColor(new Color(59,130,246,50));g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);}
                            g2.setColor(isSel?Color.WHITE:(isToday?ACCENT:Color.WHITE));
                            g2.setFont(new Font("Segoe UI",isSel?Font.BOLD:Font.PLAIN,12));
                            FontMetrics fm=g2.getFontMetrics();
                            g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                            g2.dispose();
                        }
                    };
                    btn.setOpaque(false);btn.setContentAreaFilled(false);btn.setBorderPainted(false);btn.setFocusPainted(false);
                    btn.setFont(new Font("Segoe UI",Font.PLAIN,12)); btn.setForeground(Color.WHITE);
                    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    btn.addActionListener(e->{
                        selected=date;
                        display.setText(date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        display.setForeground(new Color(15,23,42));
                        popup.dispose(); popup=null;
                    });
                    grid.add(btn);
                }
                grid.revalidate(); grid.repaint();
            };
            prev.addActionListener(e->{ viewing=viewing.minusMonths(1); refresh.run(); });
            next.addActionListener(e->{ viewing=viewing.plusMonths(1);  refresh.run(); });
            cal.add(header,BorderLayout.NORTH);
            cal.add(Box.createRigidArea(new Dimension(0,6)),BorderLayout.CENTER);
            cal.add(grid,BorderLayout.SOUTH);
            popup.setContentPane(cal); popup.pack();
            Point loc = display.getLocationOnScreen();
            popup.setLocation(loc.x, loc.y+display.getHeight()+2);
            popup.setVisible(true); refresh.run();
            popup.addWindowFocusListener(new WindowAdapter(){
                public void windowLostFocus(WindowEvent e){ if(popup!=null){popup.dispose();popup=null;} }
            });
        }

        private JButton navBtn(String t) {
            JButton b=new JButton(t);
            b.setFont(new Font("Segoe UI",Font.BOLD,18)); b.setForeground(ACCENT);
            b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(36,28)); return b;
        }

        public String getDateText() { return display.getText().trim(); }
        public java.time.LocalDate getDate() { return selected; }
        public void clear() { selected=null; display.setText(""); }
    }
}
