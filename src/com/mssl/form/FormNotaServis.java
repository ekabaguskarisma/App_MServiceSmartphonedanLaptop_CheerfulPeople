package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import com.mssl.main.FormManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class FormNotaServis extends Form {

    // Komponen UI
    private JPanel pnlCetak; 
    private JLabel lblHeaderNota, lblPesanError;
    private JLabel lblTglMasuk, lblTglSelesai;
    private JLabel lblNama, lblTelepon, lblPerangkat;
    private JLabel lblBiaya, lblStatusBayar, lblGaransi;
    private JTextArea txtKelengkapan, txtKeluhan, txtDiagnosa, txtTindakan;
    private String idNotaUntukDicetak = "";

    // Palet Warna
    private final Color APP_BG_COLOR = new Color(245, 245, 248); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color TEXT_MUTED = new Color(130, 130, 140);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color FINISH_GREEN = new Color(39, 174, 96);

    public FormNotaServis() {
        init();
        loadDataNota();
    }

    private void init() {
        setLayout(new MigLayout("wrap, fill, insets 25 30 25 30", "[fill, grow]", "[grow, fill][]"));
        setBackground(APP_BG_COLOR);
        
        // CETAK KERTAS
        pnlCetak = new JPanel(new MigLayout("wrap, fillx, insets 35 40 35 40", "[fill, grow]", "[]15[]20[]"));
        pnlCetak.setBackground(CARD_BG_COLOR);
        pnlCetak.putClientProperty(FlatClientProperties.STYLE, "arc:20"); 

        // HEADER NOTA
        JPanel pnlHeader = new JPanel(new MigLayout("insets 0, fillx", "[][0:0, grow, right]", "[]"));
        pnlHeader.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Detail Nota Servis");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);
        
        lblHeaderNota = new JLabel("Nomor Nota: Memuat...");
        lblHeaderNota.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderNota.setForeground(ACCENT_ORANGE); 
        
        pnlHeader.add(lblTitle); 
        pnlHeader.add(lblHeaderNota);
        pnlCetak.add(pnlHeader, "growx");
        pnlCetak.add(new JSeparator(), "growx, gapy 5 15");

        lblPesanError = new JLabel("");
        lblPesanError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPesanError.setForeground(ERROR_RED);
        lblPesanError.setVisible(false);
        pnlCetak.add(lblPesanError);

        // GRID KIRI KANAN
        JPanel panelGrid = new JPanel(new MigLayout("insets 0, fillx", "[0:0, grow, fill]20[0:0, grow, fill]", "[]"));
        panelGrid.setOpaque(false);

        // KOLOM Pelanggan & Perangkat
        JPanel pnlKiri = new JPanel(new MigLayout("wrap, fillx, insets 0", "[130!][0:0, grow, fill]", "[]15[][][][]"));
        pnlKiri.setOpaque(false);
        
        JLabel lblTitleKiri = new JLabel("INFO PELANGGAN & PERANGKAT");
        lblTitleKiri.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitleKiri.setForeground(ACCENT_ORANGE);
        pnlKiri.add(lblTitleKiri, "span 2, gapbottom 10");

        lblNama = createDataLabel("-"); 
        lblTelepon = createDataLabel("-");
        lblPerangkat = createDataLabel("-"); 
        txtKelengkapan = createDataTextArea();
        lblTglMasuk = createDataLabel("-");

        pnlKiri.add(createTitleLabel("Nama Pelanggan")); pnlKiri.add(lblNama);
        pnlKiri.add(createTitleLabel("No. Telp / WA")); pnlKiri.add(lblTelepon);
        pnlKiri.add(createTitleLabel("Tipe Perangkat")); pnlKiri.add(lblPerangkat);
        pnlKiri.add(createTitleLabel("Kelengkapan Bawaan"), "top, gaptop 3"); pnlKiri.add(txtKelengkapan, "growx, wmin 10");
        pnlKiri.add(createTitleLabel("Tanggal Masuk")); pnlKiri.add(lblTglMasuk);

        // KOLOM Detail Kerusakan
        JPanel pnlKanan = new JPanel(new MigLayout("wrap, fillx, insets 0", "[130!][0:0, grow, fill]", "[]15[][][][]"));
        pnlKanan.setOpaque(false);

        JLabel lblTitleKanan = new JLabel("DETAIL KERUSAKAN & TINDAKAN");
        lblTitleKanan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitleKanan.setForeground(ACCENT_ORANGE);
        pnlKanan.add(lblTitleKanan, "span 2, gapbottom 10");

        txtKeluhan = createDataTextArea(); 
        txtDiagnosa = createDataTextArea();
        txtTindakan = createDataTextArea(); 
        lblTglSelesai = createDataLabel("-");

        pnlKanan.add(createTitleLabel("Keluhan Awal"), "top, gaptop 3"); pnlKanan.add(txtKeluhan, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Hasil Diagnosa"), "top, gaptop 3"); pnlKanan.add(txtDiagnosa, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Tindakan Servis"), "top, gaptop 3"); pnlKanan.add(txtTindakan, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Tanggal Selesai")); pnlKanan.add(lblTglSelesai);

        panelGrid.add(pnlKiri, "top");
        panelGrid.add(pnlKanan, "top");
        pnlCetak.add(panelGrid, "growx");

        pnlCetak.add(new JSeparator(), "growx, gapy 20 20");

        // SUB-PANEL FINANSIAL & GARANSI
        JPanel panelBawah = new JPanel(new MigLayout("insets 0, fillx", "[][][0:0, grow, right]", "[]5[]"));
        panelBawah.setOpaque(false);

        lblGaransi = new JLabel("-"); lblGaransi.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStatusBayar = new JLabel("-"); lblStatusBayar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBiaya = new JLabel("Rp 0"); lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 28)); lblBiaya.setForeground(FINISH_GREEN); 

        JPanel pnlGaransi = new JPanel(new MigLayout("wrap, insets 0", "[]", "[][]"));
        pnlGaransi.setOpaque(false);
        pnlGaransi.add(createTitleLabel("Masa Garansi")); pnlGaransi.add(lblGaransi);

        JPanel pnlBayar = new JPanel(new MigLayout("wrap, insets 0", "[]", "[][]"));
        pnlBayar.setOpaque(false);
        pnlBayar.add(createTitleLabel("Status Pembayaran")); pnlBayar.add(lblStatusBayar);

        JPanel pnlTotal = new JPanel(new MigLayout("wrap, insets 0", "[right]", "[][]"));
        pnlTotal.setOpaque(false);
        pnlTotal.add(createTitleLabel("Total Biaya / Estimasi")); pnlTotal.add(lblBiaya);

        panelBawah.add(pnlGaransi, "gapright 40");
        panelBawah.add(pnlBayar);
        panelBawah.add(pnlTotal);

        pnlCetak.add(panelBawah, "growx");

        JScrollPane scrollCetak = new JScrollPane(pnlCetak);
        scrollCetak.setBorder(null);
        scrollCetak.setOpaque(false);
        scrollCetak.getViewport().setOpaque(false);
        scrollCetak.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCetak.getVerticalScrollBar().setUnitIncrement(15);
        scrollCetak.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");

        add(scrollCetak, "grow, wmin 0");
        add(createFooterPanel(), "left, gaptop 15"); 
    }

    private JPanel createFooterPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[][grow, right]", "[]"));
        p.setOpaque(false);

        JButton btnRefresh = new JButton("Muat Ulang Data");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBackground(ACCENT_ORANGE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:999; borderWidth:0; focusWidth:0; margin:8,30,8,30"); 

        // TOMBOL CETAK PDF
        JButton btnCetak = new JButton("Cetak / Simpan PDF");
        btnCetak.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.setBackground(SIDEBAR_MAIN_COLOR);
        btnCetak.setForeground(Color.WHITE);
        btnCetak.putClientProperty(FlatClientProperties.STYLE, "arc:999; borderWidth:0; focusWidth:0; margin:8,30,8,30"); 

        try {
            FlatSVGIcon refreshIcon = new FlatSVGIcon("com/mssl/icon/recive.svg", 18, 18);
            refreshIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
            btnRefresh.setIcon(refreshIcon);
        } catch (Exception e) {}

        btnRefresh.addActionListener(e -> loadDataNota());
        
        // MENGHUBUNGKAN TOMBOL CETAK KE FUNGSI CETAK
        btnCetak.addActionListener(e -> cetakKePDF());

        p.add(btnRefresh, "height 40!, gapright 10");
        p.add(btnCetak, "height 40!");
        return p;
    }

    private JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JLabel createDataLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(SIDEBAR_MAIN_COLOR);
        return lbl;
    }
    
    private JTextArea createDataTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ta.setForeground(SIDEBAR_MAIN_COLOR);
        ta.setBackground(CARD_BG_COLOR);
        ta.setBorder(null);
        return ta;
    }

    // FUNGSI CETAK KE PDF YANG AKTIF
    private void cetakKePDF() {
        if (idNotaUntukDicetak.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data nota belum termuat sepenuhnya!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Nota Servis - " + idNotaUntukDicetak);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double pageWidth = pageFormat.getImageableWidth();
                double panelWidth = pnlCetak.getWidth();
                double scale = 1.0;

                if (panelWidth > pageWidth) {
                    scale = pageWidth / panelWidth;
                }

                g2d.translate(10, 20); // Margin
                g2d.scale(scale * 0.95, scale * 0.95);
                
                RepaintManager currentManager = RepaintManager.currentManager(pnlCetak);
                currentManager.setDoubleBufferingEnabled(false);
                pnlCetak.print(g2d);
                currentManager.setDoubleBufferingEnabled(true);

                return Printable.PAGE_EXISTS;
            }
        });
        
        boolean isPrintAccepted = job.printDialog();
        if (isPrintAccepted) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Nota berhasil dicetak/disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDataNota() {
        String idNota = FormManager.getLoggedInUser();
        if (idNota == null) idNota = "N00007"; // Fallback untuk testing
        
        idNotaUntukDicetak = idNota;
        lblHeaderNota.setText("Nomor Nota: " + idNota);
        lblPesanError.setVisible(false);
        
        final int idServisAngka = Integer.parseInt(idNota.replace("N", ""));

        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            private String dbTglMasuk = "-", dbTglSelesai = "-";
            private String dbNama = "-", dbTelepon = "-", dbPerangkat = "-", dbKelengkapan = "-";
            private String dbKeluhan = "-", dbDiagnosa = "-", dbTindakan = "-";
            private String dbStatusBayar = "-", dbGaransi = "-";
            private double dbBiaya = 0;
            
            private boolean isFound = false;
            private String errorMsg = "";

            @Override
            protected Void doInBackground() throws Exception {
                String sql = "SELECT s.tgl_masuk, n.tanggal_selesai, p.nama_pelanggan, p.no_whatsapp, " +
                             "pr.merek, pr.tipe_model, pr.kelengkapan, s.keluhan_awal, s.hasil_diagnosa, " +
                             "s.tindakan_perbaikan, n.total_biaya, n.status_pembayaran, n.masa_garansi, s.status " +
                             "FROM data_servis_lengkap s " +
                             "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                             "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                             "LEFT JOIN tb_nota n ON n.id_servis = s.id_servis " +
                             "WHERE s.id_servis = ?";
                
                java.sql.Connection conn = com.mssl.koneksi.DatabaseConnection.getKoneksi();
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, idServisAngka);
                    try (ResultSet res = ps.executeQuery()) {
                        if (res.next()) {
                            dbTglMasuk = res.getString("tgl_masuk");
                            dbNama = res.getString("nama_pelanggan");
                            dbTelepon = res.getString("no_whatsapp");
                            dbPerangkat = res.getString("merek") + " " + res.getString("tipe_model");
                            dbKelengkapan = res.getString("kelengkapan");
                            dbKeluhan = res.getString("keluhan_awal");
                            dbDiagnosa = res.getString("hasil_diagnosa");
                            dbTindakan = res.getString("tindakan_perbaikan");
                            
                            // Data dari tabel NOTA (Hanya ada jika sudah bayar di kasir)
                            dbTglSelesai = res.getString("tanggal_selesai");
                            dbBiaya = res.getDouble("total_biaya");
                            dbStatusBayar = res.getString("status_pembayaran");
                            dbGaransi = res.getString("masa_garansi");
                            
                            String statusLengkap = res.getString("status");
                            if ("Batal".equalsIgnoreCase(statusLengkap)) {
                                dbStatusBayar = "Dibatalkan";
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
                    
                    if (isFound) {
                        lblNama.setText(dbNama);
                        lblTelepon.setText(dbTelepon);
                        lblPerangkat.setText(dbPerangkat);
                        lblTglMasuk.setText(dbTglMasuk != null ? dbTglMasuk : "-");
                        
                        txtKelengkapan.setText(dbKelengkapan != null ? dbKelengkapan : "-");
                        txtKeluhan.setText(dbKeluhan != null ? dbKeluhan : "-");
                        txtDiagnosa.setText((dbDiagnosa != null && !dbDiagnosa.trim().isEmpty()) ? dbDiagnosa : "Belum dianalisa teknisi");
                        txtTindakan.setText((dbTindakan != null && !dbTindakan.trim().isEmpty()) ? dbTindakan : "Belum ada tindakan tercatat");
                        
                        // Logika Jika Nota Belum Lunas / Masih Proses
                        lblTglSelesai.setText((dbTglSelesai == null || dbTglSelesai.isEmpty()) ? "Belum Selesai" : dbTglSelesai);
                        
                        if (dbStatusBayar == null || dbStatusBayar.isEmpty()) {
                            lblStatusBayar.setText("Belum Lunas");
                            lblStatusBayar.setForeground(ERROR_RED);
                        } else if (dbStatusBayar.equalsIgnoreCase("Lunas")) {
                            lblStatusBayar.setText(dbStatusBayar);
                            lblStatusBayar.setForeground(FINISH_GREEN);
                        } else {
                            lblStatusBayar.setText(dbStatusBayar); // Dibatalkan
                            lblStatusBayar.setForeground(ERROR_RED);
                        }

                        if (dbGaransi == null || dbGaransi.isEmpty()) {
                            lblGaransi.setText("Belum Ditentukan");
                            lblGaransi.setForeground(SIDEBAR_MAIN_COLOR);
                        } else if (dbGaransi.equalsIgnoreCase("Tidak Garansi")) {
                            lblGaransi.setText(dbGaransi);
                            lblGaransi.setForeground(ERROR_RED);
                        } else {
                            lblGaransi.setText(dbGaransi);
                            lblGaransi.setForeground(FINISH_GREEN);
                        }
                        
                        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        if (dbBiaya == 0 && (dbStatusBayar == null || !dbStatusBayar.equalsIgnoreCase("Lunas"))) {
                            lblBiaya.setText("Menunggu Kalkulasi");
                            lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Agak dikecilkan agar muat teksnya
                            lblBiaya.setForeground(ACCENT_ORANGE);
                        } else {
                            lblBiaya.setText(formatRupiah.format(dbBiaya).replace(",00", ""));
                            lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 28));
                            lblBiaya.setForeground(FINISH_GREEN);
                        }
                        
                    } else {
                        lblPesanError.setText("Data Nota Tidak Ditemukan di Database.");
                        lblPesanError.setVisible(true);
                    }

                } catch (Exception e) {
                    lblPesanError.setText("Error Query SQL: " + errorMsg);
                    lblPesanError.setVisible(true);
                }
            }
        };
        worker.execute();
    }
}