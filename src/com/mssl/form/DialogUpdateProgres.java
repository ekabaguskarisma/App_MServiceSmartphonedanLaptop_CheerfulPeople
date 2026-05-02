package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class DialogUpdateProgres extends JDialog {

    private final Color APP_BG_COLOR = new Color(245, 246, 250);
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0);
    private final Color TEXT_MUTED = new Color(130, 135, 150);

    private String idNota;
    private String namaPelanggan;
    
    private JComboBox<String> cbStatus;
    private JSlider sliderPersen;
    private JLabel lblAngkaPersen;
    private JTextArea txtLog;
    private JButton btnSimpan, btnBatal;
    
    // Variabel pengaman agar event listener tidak looping saat diubah sistem
    private boolean isAdjustingSlider = false;

    public DialogUpdateProgres(JFrame parent, String idNota, String namaPelanggan) {
        super(parent, "Update Progres Servis", true);
        this.idNota = idNota;
        this.namaPelanggan = namaPelanggan;
        
        initUI();
        loadCurrentData();
    }

    private void initUI() {
        setSize(550, 650);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new MigLayout("wrap, fillx, insets 20 30 50 30", "[fill, grow]", "[]15[]15[]20[]"));
        mainPanel.setBackground(APP_BG_COLOR);

        // HEADER
        JPanel pnlHeader = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Update Progres ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);
        JLabel lblNota = new JLabel(idNota);
        lblNota.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblNota.setForeground(ACCENT_ORANGE);
        pnlHeader.add(lblTitle); pnlHeader.add(lblNota);
        mainPanel.add(pnlHeader, "center");

        // CARD 1: INFO PELANGGAN
        JPanel pnlInfo = new JPanel(new MigLayout("wrap, fillx, insets 15 20 15 20", "[fill, grow]", "[]5[]"));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        
        JLabel lblNamaTitle = new JLabel("Pelanggan:");
        lblNamaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNamaTitle.setForeground(TEXT_MUTED);
        
        JLabel lblNamaVal = new JLabel(namaPelanggan);
        lblNamaVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNamaVal.setForeground(SIDEBAR_MAIN_COLOR);
        
        pnlInfo.add(lblNamaTitle);
        pnlInfo.add(lblNamaVal);
        mainPanel.add(pnlInfo, "growx");

        // CARD 2: FORM UPDATE
        JPanel pnlForm = new JPanel(new MigLayout("wrap, fillx, insets 20", "[fill, grow]", "[]5[]15[]5[][]15[]5[]"));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        pnlForm.add(createTitleLabel("Status Pengerjaan Terbaru"));
        cbStatus = new JComboBox<>(new String[]{
            "Antrean / Pengecekan", 
            "Menunggu Sparepart", 
            "Sedang Dikerjakan", 
            "Selesai (Siap Diambil)"
        });
        cbStatus.putClientProperty(FlatClientProperties.STYLE, "arc:10; font:14");
        pnlForm.add(cbStatus, "h 40!");

        pnlForm.add(createTitleLabel("Persentase Progres"), "gapy 10 0");
        JPanel pnlSlider = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
        pnlSlider.setOpaque(false);
        sliderPersen = new JSlider(0, 100, 0);
        sliderPersen.setMajorTickSpacing(20);
        sliderPersen.setPaintTicks(true);
        sliderPersen.setPaintLabels(true);
        sliderPersen.setOpaque(false);
        lblAngkaPersen = new JLabel("0%");
        lblAngkaPersen.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAngkaPersen.setForeground(ACCENT_ORANGE);
        
        pnlSlider.add(sliderPersen, "growx");
        pnlSlider.add(lblAngkaPersen, "w 65!, right");
        pnlForm.add(pnlSlider, "growx");

        pnlForm.add(createTitleLabel("Log Keterangan / Catatan Teknisi"), "gapy 10 0");
        txtLog = new JTextArea(4, 20);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        txtLog.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLog.setMargin(new Insets(10, 10, 10, 10));
        txtLog.putClientProperty(FlatClientProperties.STYLE, "background:#f5f6fa; borderWidth:0");
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)));
        pnlForm.add(scrollLog, "growx, h 100!");

        mainPanel.add(pnlForm, "growx");

        // BUTTONS
        JPanel pnlBtn = new JPanel(new MigLayout("insets 0, fillx, gapx 15", "[grow][grow]", "[]"));
        pnlBtn.setOpaque(false);

        btnSimpan = new JButton("Simpan Progres");
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnBatal = new JButton("Batal");
        btnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatal.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e0e0e0; foreground:#333333; font:bold +1; borderWidth:0; focusWidth:0");

        // EVENT LISTENERS UNTUK MENGUNCI LOGIKA STATUS DAN PERSENTASE
        cbStatus.addActionListener(e -> validasiSliderDanStatus());
        sliderPersen.addChangeListener(e -> {
            if (!isAdjustingSlider) {
                validasiSliderDanStatus();
            }
            lblAngkaPersen.setText(sliderPersen.getValue() + "%");
        });

        btnSimpan.addActionListener(e -> simpanProgres());
        btnBatal.addActionListener(e -> dispose());

        pnlBtn.add(btnBatal, "growx, h 45!");
        pnlBtn.add(btnSimpan, "growx, h 45!");
        
        mainPanel.add(pnlBtn, "growx");

        setContentPane(mainPanel);
    }

    // FUNGSI PENGAMAN LOGIKA PERSENTASE (MENCEGAH BUG BUG)
    private void validasiSliderDanStatus() {
        if (isAdjustingSlider) return;
        isAdjustingSlider = true;

        String status = cbStatus.getSelectedItem().toString();
        int val = sliderPersen.getValue();

        switch (status) {
            case "Antrean / Pengecekan":
                sliderPersen.setEnabled(true);
                if (val > 20) sliderPersen.setValue(20);
                break;
            case "Menunggu Sparepart":
                sliderPersen.setEnabled(false); // Kunci Slider
                sliderPersen.setValue(25);      // Patok di 25%
                break;
            case "Sedang Dikerjakan":
                sliderPersen.setEnabled(true);
                if (val < 30) sliderPersen.setValue(30);
                if (val >= 100) sliderPersen.setValue(99); // Dilarang 100% kalau belum Selesai
                break;
            case "Selesai (Siap Diambil)":
                sliderPersen.setEnabled(false); // Kunci Slider
                sliderPersen.setValue(100);     // Patok 100% wajib
                break;
        }
        
        lblAngkaPersen.setText(sliderPersen.getValue() + "%");
        isAdjustingSlider = false;
    }

    private JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private void loadCurrentData() {
        try {
            Connection kon = DatabaseConnection.getKoneksi(); // Read-only tidak butuh koneksi transaksi
            String sql = "SELECT status_servis, persentase, keterangan FROM tb_progres_servis WHERE id_nota = ?";
            PreparedStatement ps = kon.prepareStatement(sql);
            ps.setString(1, idNota);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String stat = rs.getString("status_servis");
                if(stat != null) cbStatus.setSelectedItem(stat);
                
                int persen = rs.getInt("persentase");
                sliderPersen.setValue(persen);
                lblAngkaPersen.setText(persen + "%");
                
                String log = rs.getString("keterangan");
                if(log != null) txtLog.setText(log);
            }
            
            // Panggil validasi setelah memuat data agar UI langsung terkunci dengan benar
            validasiSliderDanStatus();
            
        } catch (Exception e) {
            System.err.println("Load Progres: " + e.getMessage());
        }
    }

    private void simpanProgres() {
        String status = cbStatus.getSelectedItem().toString();
        int persen = sliderPersen.getValue(); 
        String log = txtLog.getText().trim();
        int idServisInt = Integer.parseInt(idNota.replace("N", ""));

        try {
            // MENGGUNAKAN JALUR KHUSUS UNTUK TRANSAKSI
            Connection kon = DatabaseConnection.getKoneksiTransaksi();
            kon.setAutoCommit(false); 

            try {
                String sqlCek = "SELECT id_nota FROM tb_progres_servis WHERE id_nota = ?";
                PreparedStatement psCek = kon.prepareStatement(sqlCek);
                psCek.setString(1, idNota);
                ResultSet rs = psCek.executeQuery();
                
                if (rs.next()) {
                    String sqlUpd = "UPDATE tb_progres_servis SET status_servis=?, persentase=?, keterangan=? WHERE id_nota=?";
                    PreparedStatement psUpd = kon.prepareStatement(sqlUpd);
                    psUpd.setString(1, status); psUpd.setInt(2, persen); psUpd.setString(3, log); psUpd.setString(4, idNota);
                    psUpd.executeUpdate();
                } else {
                    String sqlIns = "INSERT INTO tb_progres_servis (id_nota, status_servis, persentase, keterangan) VALUES (?, ?, ?, ?)";
                    PreparedStatement psIns = kon.prepareStatement(sqlIns);
                    psIns.setString(1, idNota); psIns.setString(2, status); psIns.setInt(3, persen); psIns.setString(4, log);
                    psIns.executeUpdate();
                }

                String mainStatus = "Proses"; 
                if (status.equals("Menunggu Sparepart")) mainStatus = "Menunggu Sparepart";
                if (status.equals("Selesai (Siap Diambil)") || persen == 100) mainStatus = "Selesai";

                String sqlSync = "UPDATE data_servis_lengkap SET status=? WHERE id_servis=?";
                PreparedStatement psSync = kon.prepareStatement(sqlSync);
                psSync.setString(1, mainStatus);
                psSync.setInt(2, idServisInt);
                psSync.executeUpdate();

                kon.commit();
                tampilkanNotif("Berhasil!", "Progres servis " + idNota + " telah diperbarui.", "success");
                dispose();
            } catch (Exception ex) {
                kon.rollback();
                throw ex; // Melempar error agar ditangkap oleh catch utama
            } finally {
                kon.close(); // MENUTUP KONEKSI AGAR MEMORI TIDAK BOCOR
            }

        } catch (Exception e) {
            tampilkanNotif("Gagal Update", e.getMessage(), "error");
        }
    }

    private void tampilkanNotif(String title, String message, String type) {
        String bgColor = (type.equals("success")) ? "#27ae60" : "#e74c3c";
        String iconName = (type.equals("success")) ? "success.svg" : "error.svg";
        
        JPanel p = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor); 
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        
        JPanel tp = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]")); tp.setOpaque(false); 
        tp.add(new JLabel(title) {{ putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#ffffff"); }});
        tp.add(new JLabel(message) {{ putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(240,240,240)"); }});
        
        p.add(new JLabel(icon), "top"); p.add(tp);
        JButton b = new JButton("Tutup") {{ 
            setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:"+bgColor+"; font:bold; arc:10; borderWidth:0; margin:5,15,5,15; focusWidth:0"); 
        }};
        b.addActionListener(e -> { Window w = SwingUtilities.getWindowAncestor(b); if(w!=null) w.dispose(); });
        JOptionPane.showOptionDialog(this, p, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{b}, b);
    }
}