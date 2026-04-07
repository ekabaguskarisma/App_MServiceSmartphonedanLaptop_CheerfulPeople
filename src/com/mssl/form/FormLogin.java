package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.main.Form;
import com.mssl.main.FormManager;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public class FormLogin extends Form {

    private JLabel imageLogo;
    private JPanel mainPanel;
    private JPanel panelForm;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public FormLogin() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 20", "[center]", "[center]"));
        this.putClientProperty(FlatClientProperties.STYLE, "background:rgb(255,255,255)");

        mainPanel = new JPanel(new MigLayout("insets 50", "[] []", "[fill][grow]"));
        mainPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;background:@accentColor");

        JPanel panelLogo = new JPanel(new MigLayout("wrap", "300", "[] 0 []"));
        panelLogo.putClientProperty(FlatClientProperties.STYLE, "background:@accentColor");

        imageLogo = new JLabel();
        imageLogo.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/com/mssl/img/Logo.png"))
                .getImage()
                .getScaledInstance(100, 100, Image.SCALE_SMOOTH)));

        JLabel lbTitleLogo = new JLabel("Sistem Aplikasi Manajemen Service");
        lbTitleLogo.putClientProperty(FlatClientProperties.STYLE, "foreground:rgb(255,255,255);font:bold italic +14 Roboto");

        JLabel lbDetail = new JLabel("Smartphone dan Laptop, berbasis OOP dan Database");
        lbDetail.putClientProperty(FlatClientProperties.STYLE, "foreground:rgb(255,255,255);font:bold 16");

        JLabel lbDetail2 = new JLabel("Monitoring Progres Secara Real Time");
        lbDetail2.putClientProperty(FlatClientProperties.STYLE, "foreground:rgb(255,255,255);font:bold 16");

        JLabel lbCreated = new JLabel("created by Cheerful People");
        lbCreated.putClientProperty(FlatClientProperties.STYLE, "foreground:rgb(140,140,140);font:12");

        panelLogo.add(imageLogo, "align center, gapy 30, gap 25px 0px");
        panelLogo.add(lbTitleLogo, "align center, gapy 5, gap 25px 0px");
        panelLogo.add(lbDetail, "align center, gapy 5, gap 25px 0px");
        panelLogo.add(lbDetail2, "align center, gapy 5, gap 25px 0px");
        panelLogo.add(lbCreated, "align center, gapy 5, gap 25px 0px");

        panelForm = new JPanel(new MigLayout("wrap, insets 20", "fill, 200:250"));
        panelForm.putClientProperty(FlatClientProperties.STYLE, "arc:20;background:rgb(255,255,255)");

        JLabel lbTitleForm = new JLabel("Login", JLabel.CENTER);
        lbTitleForm.putClientProperty(FlatClientProperties.STYLE, "foreground:@accentColor;font:bold +10");

        JLabel lbDescription = new JLabel("Please sign in to access your dashboard", JLabel.CENTER);
        lbDescription.putClientProperty(FlatClientProperties.STYLE, "foreground:@accentColor");

        JLabel lbUsername = new JLabel("Username");
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "foreground:@accentColor");

        txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username / No. Nota");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, 
                    new FlatSVGIcon("com/mssl/icon/username.svg", 20, 20));
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        JLabel lbPassword = new JLabel("Password");
        lbPassword.putClientProperty(FlatClientProperties.STYLE, "foreground:@accentColor");

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password / No. HP");
        txtPassword.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtPassword.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, 
                    new FlatSVGIcon("com/mssl/icon/password.svg", 20, 20));
        txtPassword.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;"
                + "showRevealButton:true;"
                + "showCapsLock:true");

        btnLogin = new JButton("Login");
        btnLogin.putClientProperty(FlatClientProperties.STYLE, ""
                + "foreground:rgb(255,255,255);"
                + "background:@accentColor;"
                + "arc:10;"
                + "borderWidth:0;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "font:bold 16");

        panelForm.add(lbTitleForm);
        panelForm.add(lbDescription);
        panelForm.add(lbUsername, "gapy 8");
        panelForm.add(txtUsername, "hmin 30");
        panelForm.add(lbPassword, "gapy 8");
        panelForm.add(txtPassword, "hmin 30");
        panelForm.add(btnLogin, "hmin 30, gapy 15 15");

        mainPanel.add(panelForm);
        mainPanel.add(panelLogo);

        add(mainPanel);
        
        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = String.valueOf(txtPassword.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                tampilkanNotif("Peringatan", "Username dan Password tidak boleh kosong!", false);
                return;
            }

            try {
                java.sql.Connection kon = com.mssl.koneksi.DatabaseConnection.getKoneksi();
                
                String sql = "SELECT * FROM data_pengguna WHERE username=? AND password=?";
                java.sql.PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                java.sql.ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String role = rs.getString("role");
                    String namaLengkap = rs.getString("nama_lengkap");
                    
                    FormManager.login(role, user);
                    
                    tampilkanNotif("Login Berhasil!", "Selamat datang, " + namaLengkap + "!", true);
                    
                } else {
                    tampilkanNotif("Login Gagal", "Username atau Password salah/tidak ditemukan.", false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                tampilkanNotif("Error Sistem", "Terjadi kesalahan: " + ex.getMessage(), false);
            }
        });
    }

    private void tampilkanNotif(String title, String message, boolean isSuccess) {
        String backgroundColor = isSuccess ? "@accentColor" : "#e04f5f"; 
        String iconPath = isSuccess ? "com/mssl/icon/success.svg" : "com/mssl/icon/error.svg";
        
        JPanel internalPanel = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        internalPanel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"  
                + "background:" + backgroundColor); 

        FlatSVGIcon icon = new FlatSVGIcon(iconPath, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        JLabel lbIcon = new JLabel(icon);
        
        JPanel textPanel = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]"));
        textPanel.setOpaque(false); 
        
        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:rgb(255,255,255)");
        
        JLabel lbMessage = new JLabel(message);
        lbMessage.putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(235,235,235)");
        
        textPanel.add(lbTitle);
        textPanel.add(lbMessage);
        
        internalPanel.add(lbIcon, "top, gapy 2");
        internalPanel.add(textPanel);
        
        JButton btnTutup = new JButton("Tutup");
        btnTutup.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:rgb(255,255,255);" 
                + "foreground:" + (isSuccess ? "@accentColor" : "#e04f5f") + ";" 
                + "font:bold;"
                + "arc:10;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "borderWidth:0");
        
        btnTutup.addActionListener(e -> {
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(btnTutup);
            if (window != null) {
                window.dispose(); 
            }
        });

        javax.swing.JOptionPane.showOptionDialog(
            this, 
            internalPanel, 
            "", 
            javax.swing.JOptionPane.DEFAULT_OPTION, 
            javax.swing.JOptionPane.PLAIN_MESSAGE, 
            null, 
            new Object[]{btnTutup}, 
            btnTutup
        );
    }
}