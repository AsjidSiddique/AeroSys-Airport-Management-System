package Gui;

import dao.AuthDAO;
import dao.AuthDAO.LoginResult;
import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AeroSysLoginUI – Main login screen.
 * Uses AeroComponents for all shared widgets.
 */
public class AeroSysLoginUI extends JFrame {

    private AeroComponents.AnimatedBackground bgPanel;
    private AeroComponents.RoleToggle         roleToggle;
    private AeroComponents.AeroTextField      txtUsername;
    private AeroComponents.AeroPasswordField  txtPassword;
    private JCheckBox   chkShowPass;
    private JLabel      lblError;
    private JLabel      lblTime, lblDate;
    private JButton     btnLogin, btnRegister, btnForgot;
    private JLabel      lblLoginTitle;

    private static final Color COL_BLUE  = new Color(59,  130, 246);
    private static final Color COL_GREEN = new Color(16,  185, 129);
    private static final Color COL_RED   = new Color(239, 68,  68);

    public AeroSysLoginUI() {
       setIconImage(new ImageIcon(getClass().getResource("/icons/logos.png")).getImage());
        setTitle("AeroSys – Login");
        setSize(1300, 820);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        buildUI();
        startClock();
    }

    private ImageIcon icon(String name, int w, int h) {
        return AeroComponents.icon(name, w, h);
    }

    private void buildUI() {
        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(null);
        setContentPane(layered);
        bgPanel = new AeroComponents.AnimatedBackground();
        layered.add(bgPanel, JLayeredPane.DEFAULT_LAYER);
        JPanel content = new JPanel(null);
        content.setOpaque(false);
        layered.add(content, JLayeredPane.PALETTE_LAYER);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                bgPanel.setBounds(0, 0, getWidth(), getHeight());
                content.setBounds(0, 0, getWidth(), getHeight());
                layoutContent(content);
            }
        });
        bgPanel.setBounds(0, 0, 1300, 820);
        content.setBounds(0, 0, 1300, 820);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildContent(content);
    }

    private void buildContent(JPanel root) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false); topBar.setBounds(0, 0, 1300, 64);
        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        clockPanel.setOpaque(false); clockPanel.setBorder(BorderFactory.createEmptyBorder(14, 22, 0, 0));
        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTime.setForeground(Color.WHITE);
        lblTime.setIcon(icon("clock.png", 22, 22));
        lblDate = new JLabel("Loading...");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDate.setForeground(new Color(148, 163, 200));
        lblDate.setIcon(icon("calendar.png", 18, 18));
        clockPanel.add(lblTime); clockPanel.add(lblDate);
        topBar.add(clockPanel, BorderLayout.WEST);
        JLabel verBadge = new JLabel("AeroSys v1.0  \u2022  CSC236") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,18)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        verBadge.setFont(new Font("Segoe UI",Font.PLAIN,12));
        verBadge.setForeground(new Color(120,150,200));
        verBadge.setBorder(BorderFactory.createEmptyBorder(8,14,8,14)); verBadge.setOpaque(false);
        JPanel verWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT,22,10)); verWrap.setOpaque(false);
        verWrap.add(verBadge); topBar.add(verWrap, BorderLayout.EAST);
        root.add(topBar);
        JPanel leftPanel = buildLeftDecor(); leftPanel.setBounds(0, 0, 580, 820); root.add(leftPanel);
        AeroComponents.GlassCard card = buildLoginCard(); card.setBounds(640, 100, 520, 590); root.add(card);
        root.putClientProperty("card", card);
        root.putClientProperty("left", leftPanel);
        root.putClientProperty("top",  topBar);
    }

    private void layoutContent(JPanel root) {
        int w = root.getWidth(), h = root.getHeight();
        AeroComponents.GlassCard card = (AeroComponents.GlassCard) root.getClientProperty("card");
        JPanel left = (JPanel) root.getClientProperty("left");
        JPanel top  = (JPanel) root.getClientProperty("top");
        if (top  != null) top.setBounds(0, 0, w, 64);
        if (card != null) card.setBounds(w/2+30, (h-610)/2, 520, 610);
        if (left != null) left.setBounds(0, 0, w/2, h);
        root.revalidate();
    }

    private JPanel buildLeftDecor() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new RadialGradientPaint(new Point(getWidth()/2,getHeight()/2),getHeight()*0.7f,
                        new float[]{0f,1f},new Color[]{new Color(30,80,200,45),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,12));
                int cx=getWidth()/2-20,cy=getHeight()/2-40;
                int[]wx={cx-220,cx+60,cx+30,cx-180},wy={cy+60,cy-20,cy-60,cy+10};
                g2.fillPolygon(wx,wy,4);
                g2.setColor(new Color(255,255,255,16));
                g2.fillRoundRect(cx-30,cy-80,200,40,20,20); g2.dispose();
            }
        };
        p.setOpaque(false);
        JPanel tb = new JPanel(); tb.setLayout(new BoxLayout(tb,BoxLayout.Y_AXIS));
        tb.setOpaque(false); tb.setBounds(60,200,460,380);
        JLabel logo = new JLabel(icon("logo.png",120,120)); logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel brand = new JLabel("AeroSys");
        brand.setFont(new Font("Segoe UI",Font.BOLD,64)); brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tag = new JLabel("Airport Management System");
        tag.setFont(new Font("Segoe UI",Font.PLAIN,22)); tag.setForeground(new Color(148,180,240));
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel sep = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,COL_BLUE,getWidth(),0,new Color(0,0,0,0)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        sep.setOpaque(false); sep.setPreferredSize(new Dimension(200,3));
        sep.setMaximumSize(new Dimension(200,3)); sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel desc = new JLabel("<html><body style='width:380px;color:rgb(140,165,210);font-size:14px;line-height:1.6'>" +
                "Manage passengers, flights, bookings, tickets<br>and payments — all in one place.</body></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[][]feats={{"flight-takeoff.png","Real-time flight management"},{"ticket.png","Instant ticket generation"},{"paid.png","Secure payment processing"}};
        JPanel fl=new JPanel(); fl.setLayout(new BoxLayout(fl,BoxLayout.Y_AXIS)); fl.setOpaque(false); fl.setAlignmentX(Component.LEFT_ALIGNMENT);
        for(String[]f:feats){
            JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,10,4)); row.setOpaque(false);
            JLabel ic=new JLabel(icon(f[0],20,20));
            JLabel tx=new JLabel(f[1]); tx.setFont(new Font("Segoe UI",Font.PLAIN,14)); tx.setForeground(new Color(160,195,240));
            row.add(ic); row.add(tx); fl.add(row);
        }
        tb.add(logo); tb.add(Box.createRigidArea(new Dimension(0,14)));
        tb.add(brand); tb.add(tag); tb.add(Box.createRigidArea(new Dimension(0,16)));
        tb.add(sep);   tb.add(Box.createRigidArea(new Dimension(0,16)));
        tb.add(desc);  tb.add(Box.createRigidArea(new Dimension(0,20))); tb.add(fl);
        p.add(tb); return p;
    }

    private AeroComponents.GlassCard buildLoginCard() {
        AeroComponents.GlassCard card = new AeroComponents.GlassCard(new Color(255,255,255,40));
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,24,8,24); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1; gbc.weightx = 1;

        gbc.gridy=0; gbc.insets=new Insets(28,28,4,28);
        JLabel cardTitle = new JLabel("Welcome Back");
        cardTitle.setFont(new Font("Segoe UI",Font.BOLD,30)); cardTitle.setForeground(Color.WHITE);
        cardTitle.setIcon(icon("waving-hand.png",32,32)); cardTitle.setIconTextGap(12);
        card.add(cardTitle, gbc);

        gbc.gridy=1; gbc.insets=new Insets(0,28,20,28);
        JLabel sub = new JLabel("Sign in to your AeroSys account");
        sub.setFont(new Font("Segoe UI",Font.PLAIN,14)); sub.setForeground(new Color(148,170,220));
        card.add(sub, gbc);

        gbc.gridy=2; gbc.insets=new Insets(0,28,20,28);
        roleToggle = new AeroComponents.RoleToggle();
        roleToggle.addPassengerListener(e -> { roleToggle.setPassengerActive(); lblLoginTitle.setText("Passenger Login"); btnRegister.setVisible(true); });
        roleToggle.addEmployeeListener(e  -> { roleToggle.setEmployeeActive();  lblLoginTitle.setText("Employee / Admin Login"); btnRegister.setVisible(false); });
        card.add(roleToggle, gbc);

        gbc.gridy=3; gbc.insets=new Insets(0,28,8,28);
        lblLoginTitle = new JLabel("Passenger Login");
        lblLoginTitle.setFont(new Font("Segoe UI",Font.BOLD,15)); lblLoginTitle.setForeground(new Color(100,160,255));
        card.add(lblLoginTitle, gbc);

        gbc.gridy=4; gbc.insets=new Insets(0,28,0,28);
        JLabel lblUser = new JLabel("  Username");
        lblUser.setFont(new Font("Segoe UI",Font.BOLD,13)); lblUser.setForeground(new Color(180,200,240));
        lblUser.setIcon(icon("username.png",18,18)); card.add(lblUser, gbc);

        gbc.gridy=5; gbc.insets=new Insets(4,28,12,28);
        txtUsername = new AeroComponents.AeroTextField("Enter your username", true);
        card.add(txtUsername, gbc);

        gbc.gridy=6; gbc.insets=new Insets(0,28,0,28);
        JLabel lblPass = new JLabel("  Password");
        lblPass.setFont(new Font("Segoe UI",Font.BOLD,13)); lblPass.setForeground(new Color(180,200,240));
        lblPass.setIcon(icon("password.png",18,18)); card.add(lblPass, gbc);

        gbc.gridy=7; gbc.insets=new Insets(4,28,8,28);
        txtPassword = new AeroComponents.AeroPasswordField(true);
        card.add(txtPassword, gbc);

        gbc.gridy=8; gbc.insets=new Insets(0,24,8,24);
        JPanel optRow = new JPanel(new BorderLayout()); optRow.setOpaque(false);
        chkShowPass = new JCheckBox("Show Password");
        chkShowPass.setFont(new Font("Segoe UI",Font.PLAIN,13));
        chkShowPass.setForeground(new Color(148,170,220)); chkShowPass.setOpaque(false);
        chkShowPass.setFocusPainted(false); chkShowPass.addActionListener(e -> onTogglePassword());
        btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(new Font("Segoe UI",Font.PLAIN,13)); btnForgot.setForeground(new Color(100,160,255));
        btnForgot.setBorderPainted(false); btnForgot.setFocusPainted(false);
        btnForgot.setContentAreaFilled(false); btnForgot.setOpaque(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> onForgotPassword());
        optRow.add(chkShowPass,BorderLayout.WEST); optRow.add(btnForgot,BorderLayout.EAST);
        card.add(optRow, gbc);

        gbc.gridy=9; gbc.insets=new Insets(0,28,4,28);
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI",Font.BOLD,13)); lblError.setForeground(COL_RED);
        lblError.setIcon(icon("error.png",16,16)); card.add(lblError, gbc);

        gbc.gridy=10; gbc.insets=new Insets(4,28,10,28);
        btnLogin = buildLoginBtn(); btnLogin.addActionListener(e -> onLogin());
        card.add(btnLogin, gbc);

        gbc.gridy=11; gbc.insets=new Insets(0,28,28,28);
        btnRegister = new JButton("Don't have an account?  Register Now") {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,18)); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(255,255,255,35)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,13)); g2.setColor(new Color(160,195,255));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btnRegister.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btnRegister.setPreferredSize(new Dimension(340,42));
        btnRegister.setBorderPainted(false); btnRegister.setFocusPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> onRegister());
        card.add(btnRegister, gbc);

        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if(e.getKeyCode()==KeyEvent.VK_ENTER) onLogin(); }
        };
        txtUsername.addKeyListener(enter); txtPassword.addKeyListener(enter);
        return card;
    }

    private JButton buildLoginBtn() {
        JButton b = new JButton("Sign In") {
            private float glow=0f; private boolean hov=false; private javax.swing.Timer t;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;  startAnim();}
                public void mouseExited (MouseEvent e){hov=false; startAnim();}
            }); }
            void startAnim(){
                if(t!=null)t.stop();
                t=new javax.swing.Timer(14,ev->{
                    glow+=hov?0.1f:-0.1f; glow=Math.max(0f,Math.min(1f,glow)); repaint();
                    if((hov&&glow>=1f)||(!hov&&glow<=0f))t.stop();
                }); t.start();
            }
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(glow>0){g2.setColor(new Color(59,130,246,(int)(glow*70)));g2.fillRoundRect(-4,4,getWidth()+8,getHeight()+4,18,18);}
                Color c1=hov?new Color(79,150,255):COL_BLUE,c2=hov?new Color(50,100,240):new Color(37,99,235);
                g2.setPaint(new GradientPaint(0,0,c1,0,getHeight(),c2)); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(255,255,255,35)); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,14,14);
                Icon ic=getIcon(); FontMetrics fm=g2.getFontMetrics();
                int iw=ic!=null?ic.getIconWidth()+10:0,tw=fm.stringWidth(getText());
                int sx=(getWidth()-iw-tw)/2;
                if(ic!=null)ic.paintIcon(this,g2,sx,(getHeight()-ic.getIconHeight())/2);
                g2.setFont(new Font("Segoe UI",Font.BOLD,16)); g2.setColor(Color.WHITE);
                g2.drawString(getText(),sx+iw,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,16)); b.setIcon(icon("login.png",22,22));
        b.setForeground(Color.WHITE); b.setPreferredSize(new Dimension(340,54));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private void startClock() {
        new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }).start();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        Component card = null;
        for(Component c:((JLayeredPane)getContentPane()).getComponents())
            if(c instanceof JPanel)
                for(Component cc:((JPanel)c).getComponents())
                    if(cc instanceof AeroComponents.GlassCard){card=cc;break;}
        if(card==null) return;
        final Component fc=card; final int ox=fc.getX();
        int[]deltas={-8,8,-6,6,-4,4,-2,2,0};
        javax.swing.Timer shake=new javax.swing.Timer(30,null);
        final int[]idx={0};
        shake.addActionListener(e->{
            if(idx[0]<deltas.length){fc.setLocation(ox+deltas[idx[0]],fc.getY());idx[0]++;}
            else{fc.setLocation(ox,fc.getY());shake.stop();}
        }); shake.start();
    }
    private void clearError() { lblError.setText(" "); }

    private void onLogin() {
        clearError();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String roleType = roleToggle.isEmployeeMode() ? "EMPLOYEE" : "PASSENGER";
        if(username.isEmpty()){showError("Username is required!");txtUsername.requestFocus();return;}
        if(password.isEmpty()){showError("Password is required!");txtPassword.requestFocus();return;}
        if(password.length()<3){showError("Password must be at least 3 characters!");txtPassword.requestFocus();return;}

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnLogin.setEnabled(false); btnLogin.setText("Signing in...");

        SwingWorker<LoginResult,Void> worker = new SwingWorker<>() {
            @Override protected LoginResult doInBackground() {
                return new AuthDAO().authenticate(username, password, roleType);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnLogin.setEnabled(true); btnLogin.setText("Sign In");
                LoginResult result;
                try { result = get(); }
                catch(Exception ex){ showError("Unexpected error. Please try again."); return; }
                if(!result.success){
                    showError(result.errorMessage); txtPassword.setText(""); txtPassword.requestFocus(); return;
                }
                bgPanel.stopAnimation(); dispose();
                if(result.isEmployee()){
                    new AeroSysAdminUI(result.asEmployee(), result.role).setVisible(true);
                } else {
                    new AeroSysPassengerUI(result.asPassenger()).setVisible(true);
                }
            }
        };
        worker.execute();
    }

    private void onTogglePassword() {
        txtPassword.setEchoChar(chkShowPass.isSelected() ? (char)0 : '*');
    }

    private void onRegister() {
        dispose();
        new AeroSysRegisterUI().setVisible(true);
    }

    private void onForgotPassword() {
        String email = JOptionPane.showInputDialog(this,
                "Enter your registered email address:", "Forgot Password", JOptionPane.PLAIN_MESSAGE);
        if(email!=null&&!email.trim().isEmpty())
            JOptionPane.showMessageDialog(this,
                    "If this email exists, a reset link has been sent.",
                    "Password Reset", JOptionPane.INFORMATION_MESSAGE);
    }
}
