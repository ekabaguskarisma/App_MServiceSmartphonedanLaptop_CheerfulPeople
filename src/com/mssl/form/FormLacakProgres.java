package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.main.Form;
import com.mssl.main.FormManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JProgressBar;
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
        JPanel p = new JPanel(new MigLayout("wrap, fillx, insets 10", "[fill, grow]", "[][]"));
        p.setOpaque(false); 

        p.add(createStatusCard(), "growx, gapy n 15");
        p.add(createLogPanel(), "grow");

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
        
        progressBar.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 24));
        
        progressBar.setForeground(ACCENT_ORANGE); 
        progressBar.setBackground(new Color(230, 230, 235));

        card.add(lblStatusHeader);
        card.add(lblStatusTeks);
        card.add(progressBar, "gaptop 10");

        return card;
    }

    private JPanel createLogPanel() {
        JPanel p = new JPanel(new MigLayout("wrap, fill, insets 15", "[fill, grow]", "[][]"));
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

        p.add(lblLogTitle, "gapy n 5");
        p.add(scrollPane, "grow");

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

            @Override
            protected Void doInBackground() throws Exception {
                String sql = "SELECT s.status AS status_global, p.status_servis, p.persentase, p.keterangan " +
                             "FROM data_servis_lengkap s " +
                             "LEFT JOIN tb_progres_servis p ON p.id_nota = CONCAT('N', LPAD(s.id_servis, 5, '0')) " +
                             "WHERE CONCAT('N', LPAD(s.id_servis, 5, '0')) = ?";
                
                java.sql.Connection conn = com.mssl.koneksi.DatabaseConnection.getKoneksi();
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, finalIdNota);
                    try (ResultSet res = ps.executeQuery()) {
                        if (res.next()) {
                            String statusGlobal = res.getString("status_global");
                            String statServis = res.getString("status_servis");
                            int persen = res.getInt("persentase");
                            String ket = res.getString("keterangan");
                            
                            if (statServis != null) {
                                dbStatus = statServis;
                                dbProgres = persen;
                                if (ket != null && !ket.trim().isEmpty()) {
                                    dbLog = ket;
                                }
                            } else {
                                // Default jika teknisi belum update
                                dbStatus = "Antrean / Pengecekan";
                                dbProgres = 5;
                                dbLog = "Perangkat telah diterima dan sedang menunggu giliran pengecekan awal oleh teknisi kami.";
                            }
                            
                            if ("Selesai".equalsIgnoreCase(statusGlobal)) {
                                dbStatus = "Selesai (Siap Diambil / Diambil)";
                                dbProgres = 100;
                                if (ket == null || ket.isEmpty()) dbLog = "Perangkat telah selesai diperbaiki dan siap untuk diambil. Silakan menuju kasir.";
                            } else if ("Batal".equalsIgnoreCase(statusGlobal)) {
                                dbStatus = "Servis Dibatalkan";
                                dbProgres = 0;
                                dbLog = "Proses servis dibatalkan.";
                            }
                            
                            isFound = true;
                        }
                    }
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
                    txtLogArea.setText("Pastikan koneksi database lancar.");
                    System.err.println("Error Lacak Progres: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}