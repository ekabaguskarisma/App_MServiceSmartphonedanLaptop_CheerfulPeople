package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import com.mssl.main.FormManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

public class FormLacakProgres extends Form {
    
    private JLabel lblHeaderNota;
    private JLabel lblStatusTeks;
    private JProgressBar progressBar;
    private JTextArea txtLogArea;
    private final Color APP_BG_COLOR = new Color(245, 245, 248); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color FINISH_GREEN = new Color(39, 174, 96); 
    private final Color ERROR_RED = new Color(231, 76, 60); 

    public FormLacakProgres() {
        init();
        loadDataFromDatabase(); 
    }

    private void init() {
        setLayout(new MigLayout("wrap, fill, insets 25", "[fill, grow]", "[][grow, fill][]"));
        setBackground(APP_BG_COLOR);

        add(createHeaderPanel(), "gapy n 20"); 
        add(createMainContentArea(), "grow");
        add(createFooterPanel(), "left, gaptop 20"); 
    }

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15, fill", "[][grow, right]", "[]"));
        
        p.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:rgb(255,255,255);"
                + "arc:20"); 

        JLabel lblTitle = new JLabel("Live Tracking Servis Perangkat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);

        lblHeaderNota = new JLabel("Nomor Nota: Memuat...");
        lblHeaderNota.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderNota.setForeground(SIDEBAR_MAIN_COLOR);

        try {
            lblTitle.setIcon(new FlatSVGIcon("com/mssl/icon/device.svg", 28, 28));
            lblHeaderNota.setIcon(new FlatSVGIcon("com/mssl/icon/users.svg", 24, 24));
        } catch (Exception e) {
            System.err.println("Icon Header tidak ditemukan");
        }

        p.add(lblTitle);
        p.add(lblHeaderNota);
        return p;
    }

    private JPanel createMainContentArea() {
        JPanel p = new JPanel(new MigLayout("wrap, fill, insets 20 0 0 0", "[fill, grow]", "[][grow, fill]"));
        p.setOpaque(false);

        p.add(createStatusCard(), "growx, gapy n 15");
        p.add(createLogPanel(), "grow, pushy");

        return p;
    }

    private JPanel createStatusCard() {
        JPanel card = new JPanel(new MigLayout("wrap, fill, insets 20", "[fill, grow]", "[]5[]"));
        
        card.putClientProperty(FlatClientProperties.STYLE, "background:$Panel.background; arc:15");

        JLabel lblStatusHeader = new JLabel("STATUS SAAT INI");
        lblStatusHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusHeader.setForeground(new Color(150, 150, 160));
        
        lblStatusTeks = new JLabel("Sedang Menunggu Konfirmasi Teknisi...");
        lblStatusTeks.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblStatusTeks.setForeground(SIDEBAR_MAIN_COLOR);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressBar.setForeground(ACCENT_ORANGE); 
        progressBar.setBackground(new Color(230, 230, 235));

        card.add(lblStatusHeader);
        card.add(lblStatusTeks);
        card.add(progressBar, "growx, height 24!, gaptop 10");

        return card;
    }

    private JPanel createLogPanel() {
        JPanel p = new JPanel(new MigLayout("wrap, fill, insets 0", "[fill, grow]", "[][grow, fill]"));
        p.setOpaque(false);

        JLabel lblLogTitle = new JLabel("PROSES SERVIS");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLogTitle.setForeground(ACCENT_ORANGE); 

        txtLogArea = new JTextArea();
        txtLogArea.setEditable(false);
        txtLogArea.setFont(new Font("Fira Code", Font.PLAIN, 14)); 
        txtLogArea.setForeground(new Color(70, 70, 80));
        txtLogArea.setBackground(CARD_BG_COLOR);
        
        txtLogArea.setMargin(new java.awt.Insets(10, 15, 10, 15));
        txtLogArea.setLineWrap(true);
        txtLogArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(txtLogArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "background:$Panel.background; arc:15"); 

        p.add(lblLogTitle, "gaptop 10, gapy n 5");
        p.add(scrollPane, "grow, pushy");

        return p;
    }

    private JPanel createFooterPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[][]", "[]"));
        p.setOpaque(false);

        JButton btnRefresh = new JButton("Refresh Status");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            FlatSVGIcon refreshIcon = new FlatSVGIcon("com/mssl/icon/recive.svg", 18, 18);
            refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
            btnRefresh.setIcon(refreshIcon);
        } catch (Exception e) {}

        btnRefresh.setBackground(ACCENT_ORANGE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:999;" 
                + "borderWidth:0;" 
                + "focusWidth:0;" 
                + "margin:8,30,8,30"); 

        btnRefresh.addActionListener(e -> loadDataFromDatabase());

        p.add(btnRefresh, "height 40!");
        return p;
    }

    // =========================================================
    // FUNGSI NOTIFIKASI TIKET CUSTOM (PREMIUM STYLE)
    // =========================================================
    private void tampilkanNotif(String title, String message, String type) {
        final String bgColor = type.equals("success") ? "#27ae60" : (type.equals("warning") ? "#ff8200" : "#e74c3c");
        String iconName = type.equals("success") ? "success.svg" : "error.svg";
        
        JPanel p = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor); 
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        
        JPanel tp = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]")); tp.setOpaque(false); 
        tp.add(new JLabel(title) {{ setFont(new Font("Segoe UI", Font.BOLD, 18)); setForeground(Color.WHITE); }});
        tp.add(new JLabel(message) {{ setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(new Color(240,240,240)); }});
        
        p.add(new JLabel(icon), "top, gapy 2"); p.add(tp);
        
        JButton b = new JButton("Tutup") {{ 
            setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:" + bgColor + "; font:bold; arc:10; borderWidth:0; margin:5,15,5,15; focusWidth:0"); 
        }};
        b.addActionListener(e -> { Window w = SwingUtilities.getWindowAncestor(b); if(w!=null) w.dispose(); });

        JOptionPane.showOptionDialog(this, p, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{b}, b);
    }

    private void loadDataFromDatabase() {
        String idNota = FormManager.getLoggedInUser();
        if (idNota == null) idNota = "N00002"; // Fallback
        
        lblHeaderNota.setText("Nomor Nota: " + idNota);
        lblStatusTeks.setText("Memuat data...");
        progressBar.setForeground(new Color(170, 170, 180));

        final String finalIdNota = idNota;

        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            private String dbStatus = "Antrean / Pengecekan";
            private int dbProgres = 0;
            private String dbLog = "Perangkat Anda sedang dalam antrean. Teknisi akan segera melakukan pengecekan.";
            private boolean isFound = false;
            private String errorMsg = "";

            @Override
            protected Void doInBackground() throws Exception {
                // UPDATE SQL: Tambahkan JOIN ke tabel pengambilan untuk mengecek tgl_ambil
                String sql = "SELECT s.status AS status_global, p.status_servis, p.persentase, p.keterangan, pg.tgl_ambil " +
                             "FROM data_servis_lengkap s " +
                             "LEFT JOIN tb_progres_servis p ON p.id_nota = CONCAT('N', LPAD(s.id_servis, 5, '0')) " +
                             "LEFT JOIN data_pengambilan pg ON s.id_servis = s.id_servis " +
                             "WHERE CONCAT('N', LPAD(s.id_servis, 5, '0')) = ?";
                
                java.sql.Connection conn = DatabaseConnection.getKoneksi();
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, finalIdNota);
                    try (ResultSet res = ps.executeQuery()) {
                        if (res.next()) {
                            String statusGlobal = res.getString("status_global");
                            String statServis = res.getString("status_servis");
                            int persen = res.getInt("persentase");
                            String ket = res.getString("keterangan");
                            String tglAmbil = res.getString("tgl_ambil"); // Cek apakah sudah diambil
                            
                            if (statServis != null) {
                                dbStatus = statServis;
                                dbProgres = persen;
                                if (ket != null && !ket.trim().isEmpty()) {
                                    dbLog = ket;
                                }
                            } else {
                                dbStatus = "Antrean / Pengecekan";
                                dbProgres = 5;
                                dbLog = "Perangkat telah diterima dan sedang menunggu giliran pengecekan awal oleh teknisi kami.";
                            }
                            
                            // LOGIKA BARU: Pemisahan "Siap Diambil" dan "Telah Diambil"
                            if ("Selesai".equalsIgnoreCase(statusGlobal)) {
                                if (tglAmbil != null && !tglAmbil.isEmpty()) {
                                    dbStatus = "Telah Diambil";
                                    dbProgres = 100;
                                    if (ket == null || ket.isEmpty()) dbLog = "Perangkat telah diambil oleh pelanggan. Terima kasih telah mempercayakan servis di Cheerful People!";
                                } else {
                                    dbStatus = "Selesai (Siap Diambil)";
                                    dbProgres = 100;
                                    if (ket == null || ket.isEmpty()) dbLog = "Perangkat telah selesai diperbaiki dan siap untuk diambil. Silakan menuju kasir.";
                                }
                            } else if ("Batal".equalsIgnoreCase(statusGlobal)) {
                                dbStatus = "Servis Dibatalkan";
                                dbProgres = 0;
                                dbLog = "Proses servis dibatalkan.";
                            }
                            
                            isFound = true;
                        }
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    throw e;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); 
                    
                    lblStatusTeks.setText(dbStatus);
                    progressBar.setValue(dbProgres);
                    txtLogArea.setText(dbLog);
                    txtLogArea.setCaretPosition(txtLogArea.getDocument().getLength());

                    if (isFound) {
                        if (dbProgres >= 100) {
                            progressBar.setForeground(FINISH_GREEN); 
                            lblStatusTeks.setForeground(FINISH_GREEN); 
                        } else if (dbStatus.equals("Servis Dibatalkan")) {
                            progressBar.setForeground(ERROR_RED); 
                            lblStatusTeks.setForeground(ERROR_RED); 
                        } else {
                            progressBar.setForeground(ACCENT_ORANGE); 
                            lblStatusTeks.setForeground(SIDEBAR_MAIN_COLOR); 
                        }
                    } else {
                        lblStatusTeks.setText("Data Tidak Ditemukan");
                        txtLogArea.setText("Maaf, kami tidak dapat menemukan riwayat perbaikan untuk nomor nota tersebut.");
                        lblStatusTeks.setForeground(new Color(130, 130, 140)); 
                    }

                } catch (Exception e) {
                    lblStatusTeks.setText("Gagal Memuat Data");
                    txtLogArea.setText("Pastikan koneksi database XAMPP berjalan lancar.");
                    tampilkanNotif("Error Database", "Gagal memuat status: " + errorMsg, "error");
                }
            }
        };
        worker.execute();
    }
}