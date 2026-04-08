package Gui;

import service.AuthService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AeroSysRegisterUI extends JFrame {

    private int currentStep = 1;

    // ── AeroComponents widgets ──────────────────────────────────────
    private AeroComponents.AnimatedBackground bgPanel;
    private AeroComponents.StepIndicator      stepIndicator;
    private AeroComponents.StrengthBar        strengthBar;

    // ── Form cards ──────────────────────────────────────────────────
    private CardLayout formCards;
    private JPanel     formPanel;

    // Step 1
    private AeroComponents.AeroTextField txtFullName, txtEmail, txtPhone;
    private AeroComponents.AeroTextField txtPassport, txtNationality, txtDOB;

    // Step 2
    private AeroComponents.AeroTextField      txtUsername;
    private AeroComponents.AeroPasswordField  txtPassword, txtConfirmPass;
    private JCheckBox chkTerms, chkShowPass;

    // Nav
    private JButton btnNext, btnBack, btnSubmit;
    private JLabel  lblTime, lblDate;

    private static final Color COL_BLUE  = new Color(59,  130, 246);
    private static final Color COL_GREEN = new Color(16,  185, 129);
    private static final Color COL_RED   = new Color(239, 68,  68);

    public AeroSysRegisterUI() {
        setTitle("AeroSys – Create Account");
        setIconImage(new ImageIcon(getClass().getResource("/icons/logos.png")).getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1300, 840);
        setMinimumSize(new Dimension(1000, 680));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch(Exception ignored){}
        buildUI(); startClock();
    }

    private ImageIcon icon(String n, int w, int h) { return AeroComponents.icon(n, w, h); }

    // ── Build UI ─────────────────────────────────────────────────────
    private void buildUI() {
        JLayeredPane layered = new JLayeredPane(); layered.setLayout(null);
        setContentPane(layered);
        bgPanel = new AeroComponents.AnimatedBackground();
        layered.add(bgPanel, JLayeredPane.DEFAULT_LAYER);
        JPanel content = new JPanel(null); content.setOpaque(false);
        layered.add(content, JLayeredPane.PALETTE_LAYER);
        addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){
                bgPanel.setBounds(0,0,getWidth(),getHeight());
                content.setBounds(0,0,getWidth(),getHeight()); layoutAll(content);
            }
        });
        bgPanel.setBounds(0,0,1300,840); content.setBounds(0,0,1300,840);
        buildContent(content);
    }

    private void buildContent(JPanel root) {
        // Top bar
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.setBounds(0,0,1300,64);
        JPanel clk = new JPanel(new FlowLayout(FlowLayout.LEFT,14,0)); clk.setOpaque(false);
        clk.setBorder(BorderFactory.createEmptyBorder(14,22,0,0));
        lblTime=new JLabel("00:00:00"); lblTime.setFont(new Font("Segoe UI",Font.BOLD,20));
        lblTime.setForeground(Color.WHITE); lblTime.setIcon(icon("clock.png",22,22));
        lblDate=new JLabel(); lblDate.setFont(new Font("Segoe UI",Font.PLAIN,14));
        lblDate.setForeground(new Color(148,163,200)); lblDate.setIcon(icon("calendar.png",18,18));
        clk.add(lblTime); clk.add(lblDate); top.add(clk,BorderLayout.WEST);
        JButton btnToLogin = makeGlassBtn("Back to Login","back.png");
        btnToLogin.addActionListener(e->onBackToLogin());
        JPanel tr=new JPanel(new FlowLayout(FlowLayout.RIGHT,22,12)); tr.setOpaque(false);
        tr.add(btnToLogin); top.add(tr,BorderLayout.EAST);
        root.add(top);

        // Left
        JPanel left = buildLeftPanel(); left.setBounds(0,0,520,840); root.add(left);
        root.putClientProperty("left",left);

        // Form card
        JPanel card = buildFormCard(); card.setBounds(570,60,680,700); root.add(card);
        root.putClientProperty("card",card); root.putClientProperty("top",top);
    }

    private void layoutAll(JPanel root){
        int w=root.getWidth(), h=root.getHeight();
        JPanel card=(JPanel)root.getClientProperty("card");
        JPanel left=(JPanel)root.getClientProperty("left");
        JPanel top =(JPanel)root.getClientProperty("top");
        if(top!=null)  top.setBounds(0,0,w,64);
        if(left!=null) left.setBounds(0,0,w/2-30,h);
        if(card!=null) card.setBounds(w/2+10,(h-660)/2,660,660);
        root.revalidate();
    }

    // ── Left panel ────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.RadialGradientPaint(new Point(getWidth()/2,getHeight()/2),getHeight()*0.7f,
                        new float[]{0f,1f},new Color[]{new Color(16,185,129,35),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(16,185,129,20)); g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawOval(getWidth()/2-180,getHeight()/2-180,360,360);
                g2.drawOval(getWidth()/2-260,getHeight()/2-260,520,520); g2.dispose();
            }
        };
        p.setOpaque(false);
        JPanel c=new JPanel(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));
        c.setOpaque(false); c.setBounds(55,180,420,460);
        JLabel pl=new JLabel(icon("logo.png",80,80)); pl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel h2=new JLabel("Join AeroSys");
        h2.setFont(new Font("Segoe UI",Font.BOLD,56)); h2.setForeground(Color.WHITE); h2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s2=new JLabel("Create your passenger account");
        s2.setFont(new Font("Segoe UI",Font.PLAIN,20)); s2.setForeground(new Color(148,190,230)); s2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel sep=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new java.awt.GradientPaint(0,0,COL_GREEN,220,0,new Color(0,0,0,0)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        sep.setOpaque(false); sep.setPreferredSize(new Dimension(220,3));
        sep.setMaximumSize(new Dimension(220,3)); sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[][]benefits={
            {"security.png","Secure passport-verified account"},
            {"ticket.png","Book flights instantly"},
            {"history.png","Track all your bookings"},
            {"boarding-pass.png","Download e-tickets anytime"}
        };
        JPanel bl=new JPanel(); bl.setLayout(new BoxLayout(bl,BoxLayout.Y_AXIS));
        bl.setOpaque(false); bl.setAlignmentX(Component.LEFT_ALIGNMENT);
        for(String[]b:benefits){
            JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,12,5)); row.setOpaque(false);
            JPanel dot=new JPanel(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COL_GREEN); g2.fillOval(0,6,10,10); g2.dispose();
                }
                @Override public Dimension getPreferredSize(){return new Dimension(10,22);}
            };
            dot.setOpaque(false);
            JLabel ic=new JLabel(icon(b[0],18,18));
            JLabel tx=new JLabel(b[1]); tx.setFont(new Font("Segoe UI",Font.PLAIN,14));
            tx.setForeground(new Color(160,200,240));
            row.add(dot); row.add(ic); row.add(tx); bl.add(row);
        }
        c.add(pl); c.add(Box.createRigidArea(new Dimension(0,12)));
        c.add(h2); c.add(s2); c.add(Box.createRigidArea(new Dimension(0,18)));
        c.add(sep); c.add(Box.createRigidArea(new Dimension(0,18))); c.add(bl);
        p.add(c); return p;
    }

    // ── Form card ─────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card=new JPanel(new BorderLayout(0,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,60)); g2.fillRoundRect(8,10,getWidth()-8,getHeight()-8,28,28);
                g2.setPaint(new java.awt.GradientPaint(0,0,new Color(255,255,255,22),0,getHeight(),new Color(255,255,255,10)));
                g2.fillRoundRect(0,0,getWidth()-8,getHeight()-8,28,28);
                g2.setColor(new Color(255,255,255,35)); g2.fillRoundRect(0,0,getWidth()-8,(getHeight()-8)/3,28,28);
                g2.setColor(new Color(255,255,255,45)); g2.setStroke(new java.awt.BasicStroke(1.3f));
                g2.drawRoundRect(0,0,getWidth()-9,getHeight()-9,28,28); g2.dispose();
            }
        };
        card.setOpaque(false);
        JPanel header=new JPanel(new BorderLayout(0,8)); header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(26,32,16,32));
        JLabel cardTitle=new JLabel("Create Account");
        cardTitle.setFont(new Font("Segoe UI",Font.BOLD,28)); cardTitle.setForeground(Color.WHITE);
        cardTitle.setIcon(icon("add-user.png",32,32)); cardTitle.setIconTextGap(12);
        stepIndicator = new AeroComponents.StepIndicator();
        header.add(cardTitle,BorderLayout.NORTH); header.add(stepIndicator,BorderLayout.CENTER);
        formCards=new CardLayout(); formPanel=new JPanel(formCards); formPanel.setOpaque(false);
        formPanel.add(buildStep1(),"STEP1"); formPanel.add(buildStep2(),"STEP2");
        JPanel nav=buildNav();
        card.add(header,BorderLayout.NORTH); card.add(formPanel,BorderLayout.CENTER); card.add(nav,BorderLayout.SOUTH);
        return card;
    }

    // ── Step 1 ────────────────────────────────────────────────────────
    private JPanel buildStep1() {
        JPanel p=new JPanel(new GridBagLayout()); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8,32,8,32));
        GridBagConstraints g=new GridBagConstraints();
        g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(5,6,5,6);
        g.gridx=0; g.gridy=0; g.gridwidth=2;
        p.add(sectionLabel("Step 1 of 2  —  Personal Information"),g);
        g.gridwidth=1; g.weightx=0.5;

        g.gridx=0; g.gridy=1; p.add(fieldLabel("Full Name","user.png"),g);
        g.gridx=1;             p.add(fieldLabel("Date of Birth","calendar.png"),g);
        g.gridx=0; g.gridy=2; txtFullName=new AeroComponents.AeroTextField("e.g. Mohammed Al-Ghamdi",true); p.add(txtFullName,g);
        g.gridx=1;             txtDOB=new AeroComponents.AeroTextField("YYYY-MM-DD",true); p.add(txtDOB,g);

        g.gridx=0; g.gridy=3; p.add(fieldLabel("Email Address","email.png"),g);
        g.gridx=1;             p.add(fieldLabel("Phone Number","phone.png"),g);
        g.gridx=0; g.gridy=4; txtEmail=new AeroComponents.AeroTextField("e.g. name@gmail.com",true); p.add(txtEmail,g);
        g.gridx=1;             txtPhone=new AeroComponents.AeroTextField("e.g. 0551234567",true); p.add(txtPhone,g);

        g.gridx=0; g.gridy=5; p.add(fieldLabel("Passport Number","passport.png"),g);
        g.gridx=1;             p.add(fieldLabel("Nationality","nationality.png"),g);
        g.gridx=0; g.gridy=6; txtPassport=new AeroComponents.AeroTextField("e.g. SA1234567",true); p.add(txtPassport,g);
        g.gridx=1;             txtNationality=new AeroComponents.AeroTextField("e.g. Saudi",true); p.add(txtNationality,g);

        g.gridx=0; g.gridy=7; g.gridwidth=2; g.insets=new Insets(12,6,2,6);
        JPanel note=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); note.setOpaque(false);
        note.add(new JLabel(icon("info.png",16,16)));
        JLabel nt=new JLabel("All fields are required. Passport number must be unique.");
        nt.setFont(new Font("Segoe UI",Font.PLAIN,12)); nt.setForeground(new Color(120,160,220));
        note.add(nt); p.add(note,g); return p;
    }

    // ── Step 2 ────────────────────────────────────────────────────────
    private JPanel buildStep2() {
        JPanel p=new JPanel(new GridBagLayout()); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8,32,8,32));
        GridBagConstraints g=new GridBagConstraints();
        g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(5,6,5,6);
        g.weightx=1.0; g.gridwidth=2;
        g.gridx=0; g.gridy=0; p.add(sectionLabel("Step 2 of 2  —  Account Setup"),g);
        g.gridy=1; p.add(fieldLabel("Username","username.png"),g);
        g.gridy=2; txtUsername=new AeroComponents.AeroTextField("Choose a unique username",true);
        txtUsername.setPreferredSize(new Dimension(560,46)); p.add(txtUsername,g);

        g.gridwidth=1; g.weightx=0.5;
        g.gridx=0; g.gridy=3; p.add(fieldLabel("Password","password.png"),g);
        g.gridx=1;             p.add(fieldLabel("Confirm Password","password.png"),g);

        g.gridx=0; g.gridy=4;
        txtPassword=new AeroComponents.AeroPasswordField(true);
        txtPassword.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){ strengthBar.update(new String(txtPassword.getPassword())); }
        });
        p.add(txtPassword,g);

        g.gridx=1;
        txtConfirmPass=new AeroComponents.AeroPasswordField(true);
        JLabel matchLbl=new JLabel("");
        matchLbl.setFont(new Font("Segoe UI",Font.PLAIN,11)); matchLbl.setForeground(new Color(120,160,220));
        txtConfirmPass.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                String p1=new String(txtPassword.getPassword()), p2=new String(txtConfirmPass.getPassword());
                if(p2.isEmpty()){matchLbl.setText("");return;}
                if(p1.equals(p2)){matchLbl.setForeground(new Color(16,185,129));matchLbl.setText("  Passwords match");}
                else{matchLbl.setForeground(new Color(239,68,68));matchLbl.setText("  Passwords do not match");}
            }
        });
        p.add(txtConfirmPass,g);

        g.gridx=0; g.gridy=5; g.insets=new Insets(2,6,8,6);
        strengthBar=new AeroComponents.StrengthBar(); p.add(strengthBar,g);
        g.gridx=1; p.add(matchLbl,g);

        g.gridx=0; g.gridy=6; g.gridwidth=2; g.insets=new Insets(0,6,6,6);
        chkShowPass=new JCheckBox("Show passwords");
        chkShowPass.setFont(new Font("Segoe UI",Font.PLAIN,13));
        chkShowPass.setForeground(new Color(148,170,220)); chkShowPass.setOpaque(false); chkShowPass.setFocusPainted(false);
        chkShowPass.addActionListener(e->{ char ec=chkShowPass.isSelected()?(char)0:'*'; txtPassword.setEchoChar(ec); txtConfirmPass.setEchoChar(ec); });
        p.add(chkShowPass,g);

        g.gridy=7; g.insets=new Insets(8,6,4,6);
        JPanel terms=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); terms.setOpaque(false);
        chkTerms=new JCheckBox(); chkTerms.setOpaque(false); chkTerms.setFocusPainted(false);
        JLabel tl=new JLabel("<html><font color='#94A3B8'>I agree to the </font><font color='#60A5FA'><u>Terms of Service</u></font><font color='#94A3B8'> and </font><font color='#60A5FA'><u>Privacy Policy</u></font></html>");
        tl.setFont(new Font("Segoe UI",Font.PLAIN,13));
        terms.add(chkTerms); terms.add(tl); p.add(terms,g);

        g.gridy=8; g.insets=new Insets(6,6,2,6);
        JPanel note=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); note.setOpaque(false);
        note.add(new JLabel(icon("info.png",16,16)));
        JLabel nt=new JLabel("Your account will be reviewed before activation.");
        nt.setFont(new Font("Segoe UI",Font.PLAIN,12)); nt.setForeground(new Color(120,160,220));
        note.add(nt); p.add(note,g); return p;
    }

    // ── Navigation ─────────────────────────────────────────────────
    private JPanel buildNav() {
        JPanel nav=new JPanel(new BorderLayout()); nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(8,32,26,32));
        btnBack   = makeGlassBtn("Back","back.png"); btnBack.setVisible(false);
        btnNext   = makeGradBtn("Next Step","forward.png",COL_BLUE);
        btnSubmit = makeGradBtn("Create Account","checkmark.png",COL_GREEN); btnSubmit.setVisible(false);
        btnBack  .addActionListener(e->onBack());
        btnNext  .addActionListener(e->onNext());
        btnSubmit.addActionListener(e->onSubmit());
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0)); right.setOpaque(false);
        right.add(btnBack); right.add(btnNext); right.add(btnSubmit);
        nav.add(right,BorderLayout.CENTER); return nav;
    }

    private JButton makeGradBtn(String text, String ic, Color color) {
        JButton b=new JButton(text){
            boolean hov=false;
            {addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){hov=true;repaint();}public void mouseExited(MouseEvent e){hov=false;repaint();}});}
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color c=hov?color.brighter():color;
                g2.setPaint(new java.awt.GradientPaint(0,0,c,0,getHeight(),c.darker()));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(255,255,255,35)); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,14,14);
                Icon ico=getIcon(); FontMetrics fm=g2.getFontMetrics();
                int iw=ico!=null?ico.getIconWidth()+8:0, tw=fm.stringWidth(getText());
                int sx=(getWidth()-iw-tw)/2;
                if(ico!=null)ico.paintIcon(this,g2,sx,(getHeight()-ico.getIconHeight())/2);
                g2.setFont(new Font("Segoe UI",Font.BOLD,14)); g2.setColor(Color.WHITE);
                g2.drawString(getText(),sx+iw,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setIcon(icon(ic,18,18)); b.setFont(new Font("Segoe UI",Font.BOLD,14));
        b.setForeground(Color.WHITE); b.setPreferredSize(new Dimension(200,48));
        b.setBorderPainted(false);b.setFocusPainted(false);b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JButton makeGlassBtn(String text, String ic) {
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,35)); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(255,255,255,60)); g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                Icon ico=getIcon(); FontMetrics fm=g2.getFontMetrics();
                int iw=ico!=null?ico.getIconWidth()+8:0, tw=fm.stringWidth(getText());
                int sx=(getWidth()-iw-tw)/2;
                if(ico!=null)ico.paintIcon(this,g2,sx,(getHeight()-ico.getIconHeight())/2);
                g2.setFont(new Font("Segoe UI",Font.BOLD,14)); g2.setColor(new Color(200,220,255));
                g2.drawString(getText(),sx+iw,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setIcon(icon(ic,18,18)); b.setFont(new Font("Segoe UI",Font.BOLD,14));
        b.setForeground(new Color(200,220,255)); b.setPreferredSize(new Dimension(150,48));
        b.setBorderPainted(false);b.setFocusPainted(false);b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    // ── Helpers ────────────────────────────────────────────────────
    private JLabel fieldLabel(String t, String ic){
        JLabel l=new JLabel("  "+t); l.setIcon(icon(ic,16,16));
        l.setFont(new Font("Segoe UI",Font.BOLD,13)); l.setForeground(new Color(180,205,240));
        l.setBorder(BorderFactory.createEmptyBorder(0,0,2,0)); return l;
    }
    private JLabel sectionLabel(String t){
        JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,13));
        l.setForeground(COL_GREEN); l.setBorder(BorderFactory.createEmptyBorder(4,0,8,0)); return l;
    }

    private void startClock(){
        new javax.swing.Timer(1000,e->{
            LocalDateTime n=LocalDateTime.now();
            lblTime.setText(n.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(n.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }).start();
    }

    // ── Validation ─────────────────────────────────────────────────
    private boolean validateStep1(){
        if(txtFullName.getText().trim().isEmpty()||txtEmail.getText().trim().isEmpty()||
           txtPhone.getText().trim().isEmpty()||txtPassport.getText().trim().isEmpty()||
           txtNationality.getText().trim().isEmpty()||txtDOB.getText().trim().isEmpty()){
            AeroComponents.AeroToast.show(this,"All fields are required.",false); return false;
        }
        if(!txtEmail.getText().trim().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")){
            AeroComponents.AeroToast.show(this,"Please enter a valid email address.",false); return false;
        }
        if(!txtPhone.getText().trim().matches("\\d{10,15}")){
            AeroComponents.AeroToast.show(this,"Phone must be 10-15 digits only.",false); return false;
        }
        if(!txtDOB.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")){
            AeroComponents.AeroToast.show(this,"Date of Birth must be YYYY-MM-DD format.",false); return false;
        }
        return true;
    }

    private boolean validateStep2(){
        String uname=txtUsername.getText().trim();
        String pass=new String(txtPassword.getPassword());
        String conf=new String(txtConfirmPass.getPassword());
        if(uname.isEmpty()||pass.isEmpty()||conf.isEmpty()){
            AeroComponents.AeroToast.show(this,"Please fill in all fields.",false); return false;
        }
        if(uname.length()<4){
            AeroComponents.AeroToast.show(this,"Username must be at least 4 characters.",false); return false;
        }
        if(!uname.matches("[a-zA-Z0-9_]+")){
            AeroComponents.AeroToast.show(this,"Username: letters, digits, underscores only.",false); return false;
        }
        if(pass.length()<8){
            AeroComponents.AeroToast.show(this,"Password must be at least 8 characters.",false); return false;
        }
        if(!pass.equals(conf)){
            AeroComponents.AeroToast.show(this,"Passwords do not match.",false); return false;
        }
        if(!chkTerms.isSelected()){
            AeroComponents.AeroToast.show(this,"You must agree to the Terms of Service.",false); return false;
        }
        return true;
    }

    // ── Event Handlers ────────────────────────────────────────────
    private void onNext(){
        if(!validateStep1()) return;
        currentStep=2; stepIndicator.setStep(2); formCards.show(formPanel,"STEP2");
        btnBack.setVisible(true); btnNext.setVisible(false); btnSubmit.setVisible(true);
    }

    private void onBack(){
        currentStep=1; stepIndicator.setStep(1); formCards.show(formPanel,"STEP1");
        btnBack.setVisible(false); btnNext.setVisible(true); btnSubmit.setVisible(false);
    }

    /**
     * Submit — calls AuthService.registerPassengerRequest() which returns
     * "SUCCESS:..." or "ERROR:..." string (NOT boolean).
     */
    private void onSubmit(){
        if(!validateStep2()) return;

        String fullName    = txtFullName.getText().trim();
        String email       = txtEmail.getText().trim();
        String phone       = txtPhone.getText().trim();
        String passport    = txtPassport.getText().trim();
        String nationality = txtNationality.getText().trim();
        String dob         = txtDOB.getText().trim();
        String username    = txtUsername.getText().trim();
        String password    = new String(txtPassword.getPassword());
        String confirmPass = new String(txtConfirmPass.getPassword());

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                java.sql.Date sqlDob = null;
                try { sqlDob = java.sql.Date.valueOf(dob); } catch (Exception ignored) {}
                // registerPassengerRequest returns "SUCCESS:..." or "ERROR:..."
                return new AuthService().registerPassengerRequest(
                        fullName, email, phone, passport,
                        nationality, sqlDob,
                        username, password, confirmPass);
            }
            @Override protected void done() {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Create Account");
                try {
                    String result = get();
                    if (result.startsWith("SUCCESS")) {
                        String msg = result.replaceFirst("^SUCCESS:", "").trim();
                        AeroComponents.AeroToast.show(AeroSysRegisterUI.this,
                                "Welcome " + fullName + "! " + msg, true);
                        new javax.swing.Timer(3500, e -> {
                            bgPanel.stopAnimation(); dispose();
                            new AeroSysLoginUI().setVisible(true);
                        }) {{ setRepeats(false); start(); }};
                    } else {
                        String msg = result.replaceFirst("^ERROR:", "").trim();
                        AeroComponents.AeroToast.show(AeroSysRegisterUI.this, msg, false);
                    }
                } catch (Exception ex) {
                    AeroComponents.AeroToast.show(AeroSysRegisterUI.this,
                            "System error: " + ex.getMessage(), false);
                }
            }
        };
        worker.execute();
    }

    private void onBackToLogin(){
        int c=JOptionPane.showConfirmDialog(this,"Go back to login? Entered data will be lost.","Confirm",JOptionPane.YES_NO_OPTION);
        if(c==JOptionPane.YES_OPTION){bgPanel.stopAnimation();dispose();new AeroSysLoginUI().setVisible(true);}
    }
}
