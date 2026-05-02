package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import com.mssl.main.FormManager;
import com.mssl.utils.UIHelper;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        txtUsername.addActionListener(e -> btnLogin.doClick());
        txtPassword.addActionListener(e -> btnLogin.doClick());

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
                UIHelper.tampilkanNotif(this, "Peringatan", "Username dan Password tidak boleh kosong!", "warning");
                return;
            }

            try (Connection kon = DatabaseConnection.getKoneksi()) {
                
                // 1. CEK LOGIN ADMIN / TEKNISI (Cek berdasarkan Username dulu)
                String sqlCekAdmin = "SELECT * FROM data_pengguna WHERE username = ?";
                try (PreparedStatement ps = kon.prepareStatement(sqlCekAdmin)) {
                    ps.setString(1, user);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { // Jika Username Ditemukan
                            String dbPassword = rs.getString("password");
                            
                            // Cocokkan Password
                            if (dbPassword.equals(pass)) {
                                String role = rs.getString("role");
                                String namaLengkap = rs.getString("nama_lengkap");
                                
                                FormManager.login(role, user);
                                UIHelper.tampilkanNotif(FormManager.getJFrame(), "Login Berhasil!", "Selamat datang, " + namaLengkap + "!", "success");
                            } else {
                                // Username benar, tapi Password salah
                                UIHelper.tampilkanNotif(this, "Gagal Masuk", "Password yang Anda masukkan salah!", "error");
                                txtPassword.setText("");
                                txtPassword.requestFocus();
                            }
                            return; // Hentikan eksekusi karena akun admin sudah dicek
                        }
                    }
                }

                // 2. CEK LOGIN PELANGGAN (Jika Username Admin tidak ditemukan)
                String sqlCekPelanggan = "SELECT s.id_servis, p.nama_pelanggan, p.no_whatsapp " +
                                         "FROM data_servis_lengkap s " +
                                         "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                                         "WHERE CONCAT('N', LPAD(s.id_servis, 5, '0')) = ?";
                try (PreparedStatement psPel = kon.prepareStatement(sqlCekPelanggan)) {
                    psPel.setString(1, user);
                    try (ResultSet rsPel = psPel.executeQuery()) {
                        if (rsPel.next()) { // Jika Nomor Nota Ditemukan
                            String dbWa = rsPel.getString("no_whatsapp");
                            
                            // Cocokkan No WA sebagai Password Pelanggan
                            if (dbWa.equals(pass)) {
                                String namaPelanggan = rsPel.getString("nama_pelanggan");
                                FormManager.login("Pelanggan", user); 
                                UIHelper.tampilkanNotif(FormManager.getJFrame(), "Akses Lacak Diberikan", "Halo Kak " + namaPelanggan + "!", "success");
                            } else {
                                // Nota benar, tapi No WA (Password) salah
                                UIHelper.tampilkanNotif(this, "Gagal Masuk", "Password (No. WhatsApp) yang dimasukkan salah!", "error");
                                txtPassword.setText("");
                                txtPassword.requestFocus();
                            }
                            return;
                        }
                    }
                }
                
                UIHelper.tampilkanNotif(this, "Gagal Masuk", "Username atau Nomor Nota tidak ditemukan / belum terdaftar!", "error");
                txtUsername.requestFocus();

            } catch (Exception ex) {
                ex.printStackTrace();
                UIHelper.tampilkanNotif(this, "Error Sistem", "Terjadi kesalahan database: " + ex.getMessage(), "error");
            }
        });
    }
}