package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.mssl.main.Form;
import com.mssl.main.FormManager;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.utils.UIHelper;
import java.awt.*;
import java.awt.print.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class FormNotaServis extends Form {

    private JPanel pnlCetak; 
    private JLabel lblHeaderNota, lblPesanError;
    private JLabel lblTglMasuk, lblTglSelesai, lblTglAmbil; 
    private JLabel lblNama, lblTelepon, lblPerangkat;
    private JLabel lblBiaya, lblStatusBayar, lblGaransi;
    
    private JLabel lblTitleJasa, lblValJasa;
    private JLabel lblTitleTotalBayar; 
    
    private JPanel pnlDaftarKomponen; 
    private JPanel cardFinansial; 
    
    private JTextArea txtKelengkapan, txtKeluhan, txtDiagnosa, txtTindakan;
    private String idNotaUntukDicetak = "";

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
        
        pnlCetak = new JPanel(new MigLayout("wrap, fillx, insets 35 40 35 40", "[fill, grow]", "[]15[]15[]15[]"));
        pnlCetak.setBackground(CARD_BG_COLOR);
        pnlCetak.putClientProperty(FlatClientProperties.STYLE, "arc:20"); 

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
        pnlCetak.add(new JSeparator(), "growx, gapy 5 10");

        lblPesanError = new JLabel("");
        lblPesanError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPesanError.setForeground(ERROR_RED);
        lblPesanError.setVisible(false);
        pnlCetak.add(lblPesanError);

        JPanel panelGrid = new JPanel(new MigLayout("insets 0, fillx", "[0:0, grow, fill]20[0:0, grow, fill]", "[]"));
        panelGrid.setOpaque(false);

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

        JPanel pnlKanan = new JPanel(new MigLayout("wrap, fillx, insets 0", "[130!][0:0, grow, fill]", "[]15[][][][][]"));
        pnlKanan.setOpaque(false);

        JLabel lblTitleKanan = new JLabel("DETAIL KERUSAKAN & TINDAKAN");
        lblTitleKanan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitleKanan.setForeground(ACCENT_ORANGE);
        pnlKanan.add(lblTitleKanan, "span 2, gapbottom 10");

        txtKeluhan = createDataTextArea(); 
        txtDiagnosa = createDataTextArea();
        txtTindakan = createDataTextArea(); 
        lblTglSelesai = createDataLabel("-");
        lblTglAmbil = createDataLabel("-");

        pnlKanan.add(createTitleLabel("Keluhan Awal"), "top, gaptop 3"); pnlKanan.add(txtKeluhan, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Hasil Diagnosa"), "top, gaptop 3"); pnlKanan.add(txtDiagnosa, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Tindakan Servis"), "top, gaptop 3"); pnlKanan.add(txtTindakan, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Tanggal Selesai")); pnlKanan.add(lblTglSelesai);
        pnlKanan.add(createTitleLabel("Tanggal Diambil")); pnlKanan.add(lblTglAmbil);

        panelGrid.add(pnlKiri, "top");
        panelGrid.add(pnlKanan, "top");
        pnlCetak.add(panelGrid, "growx");

        JPanel panelFinansialTitle = new JPanel(new MigLayout("insets 0", "[]")); panelFinansialTitle.setOpaque(false);
        JLabel lblTitleFins = new JLabel("FINANSIAL & GARANSI");
        lblTitleFins.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitleFins.setForeground(ACCENT_ORANGE);
        panelFinansialTitle.add(lblTitleFins, "gaptop 10, gapbottom 5");
        pnlCetak.add(panelFinansialTitle, "growx");

        cardFinansial = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[fill][right]", "[]8[]5[]12[]10[]"));
        cardFinansial.setBackground(Color.WHITE);
        cardFinansial.putClientProperty(FlatClientProperties.STYLE, "arc:15; border:1,solid,#f1f3f8");
        
        cardFinansial.add(createTitleLabel("RINCIAN TRANSAKSI")); cardFinansial.add(createTitleLabel("TOTAL"), "right");
        cardFinansial.add(new JSeparator(), "span 2, growx, gapy 2 5");
        
        lblTitleJasa = createTitleLabel("Biaya Jasa Teknisi & Perbaikan"); lblValJasa = createDataLabel("Rp 0");
        cardFinansial.add(lblTitleJasa); cardFinansial.add(lblValJasa);
        
        pnlDaftarKomponen = new JPanel(new MigLayout("wrap 2, fillx, insets 0", "[fill, grow][right]", "[]2[]"));
        pnlDaftarKomponen.setOpaque(false);
        cardFinansial.add(pnlDaftarKomponen, "span 2, growx, gapy 5 5");
        
        lblGaransi = createDataLabel("-");
        lblStatusBayar = createDataLabel("-");
        
        JPanel pnlGaransi = new JPanel(new MigLayout("insets 0", "[][]", "[]")); pnlGaransi.setOpaque(false);
        pnlGaransi.add(createTitleLabel("Masa Garansi Servis:")); pnlGaransi.add(lblGaransi);
        cardFinansial.add(pnlGaransi, "gaptop 10"); 
        
        JPanel pnlBayar = new JPanel(new MigLayout("insets 0", "[][]", "[]")); pnlBayar.setOpaque(false);
        pnlBayar.add(createTitleLabel("Status Pembayaran:")); pnlBayar.add(lblStatusBayar);
        cardFinansial.add(pnlBayar, "right");
        
        lblBiaya = new JLabel("Rp 0"); lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 28)); lblBiaya.setForeground(FINISH_GREEN); 
        cardFinansial.add(new JSeparator(), "span 2, growx, gaptop 10");
        
        lblTitleTotalBayar = new JLabel("TOTAL PEMBAYARAN / ESTIMASI");
        lblTitleTotalBayar.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        lblTitleTotalBayar.setForeground(SIDEBAR_MAIN_COLOR);
        cardFinansial.add(lblTitleTotalBayar, "gaptop 5"); 
        cardFinansial.add(lblBiaya, "right, gaptop 5");
        
        pnlCetak.add(cardFinansial, "growx, gaptop 10");

        JLabel lblFooterNB = new JLabel("NB: Nota ini adalah bukti resmi transaksi servis di Cheerful People Service Center.");
        lblFooterNB.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterNB.setForeground(TEXT_MUTED);
        pnlCetak.add(lblFooterNB, "center, gaptop 10");

        JScrollPane scrollCetak = UIHelper.createCustomScroll(pnlCetak);
        scrollCetak.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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

        JButton btnCetak = new JButton("Cetak / Simpan PDF");
        btnCetak.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.setBackground(SIDEBAR_MAIN_COLOR);
        btnCetak.setForeground(Color.WHITE);
        btnCetak.putClientProperty(FlatClientProperties.STYLE, "arc:999; borderWidth:0; focusWidth:0; margin:8,30,8,30"); 

        btnRefresh.addActionListener(e -> loadDataNota());
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
        ta.setEditable(false); ta.setFocusable(false);
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ta.setForeground(SIDEBAR_MAIN_COLOR);
        ta.setBackground(CARD_BG_COLOR); ta.setBorder(null);
        return ta;
    }

    // --- BLOK KODE YANG DIPERBARUI MULAI DI SINI ---
    private void cetakKePDF() {
        if (idNotaUntukDicetak.isEmpty()) { 
            UIHelper.tampilkanNotif(this, "Peringatan", "Data nota belum termuat!", "warning"); 
            return; 
        }

        try {
            // 1. TRIK DUMMY FRAME: Lepas pnlCetak dari UI sementara agar ukurannya bisa di-render utuh
            JFrame dummyFrame = new JFrame();
            dummyFrame.setUndecorated(true);
            
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(Color.WHITE);
            wrapper.add(pnlCetak, BorderLayout.CENTER);
            dummyFrame.add(wrapper);
            
            // Set ukuran panjang agar semua teks (keluhan, diagnosa, part) muat sebelum dihitung
            wrapper.setSize(595, 2000); 
            dummyFrame.pack(); 

            // Hitung tinggi aslinya, minimal setinggi kertas A4 (842)
            int actualHeight = wrapper.getPreferredSize().height;
            wrapper.setSize(595, Math.max(842, actualHeight));
            dummyFrame.pack();

            // 2. PROSES CETAK PDF
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Nota_Servis_" + idNotaUntukDicetak);

            PageFormat pf = job.defaultPage();
            Paper paper = pf.getPaper();
            // Margin aman agar tidak error di driver Windows
            double margin = 15;
            paper.setImageableArea(margin, margin, paper.getWidth() - (margin * 2), paper.getHeight() - (margin * 2));
            pf.setPaper(paper);
            pf.setOrientation(PageFormat.PORTRAIT);
            
            // MANTRA WAJIB
            pf = job.validatePage(pf);

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                // SKALA OTOMATIS: Memastikan panel muat di PDF
                double scaleX = pageFormat.getImageableWidth() / wrapper.getWidth();
                double scaleY = pageFormat.getImageableHeight() / wrapper.getHeight();
                double scale = Math.min(scaleX, scaleY);
                
                g2d.scale(scale, scale);
                wrapper.printAll(g2d);
                
                return Printable.PAGE_EXISTS;
            }, pf);

            // BUKA DIALOG PRINT WINDOWS
            if (job.printDialog()) {
                try {
                    job.print();
                    UIHelper.tampilkanNotif(this, "Sukses", "Nota berhasil dicetak/disimpan sebagai PDF!", "success");
                } catch (PrinterException ex) {
                    UIHelper.tampilkanNotif(this, "Error Cetak", "Gagal: " + ex.getMessage(), "error");
                }
            }

            // 3. KEMBALIKAN TAMPILAN KE NORMAL
            dummyFrame.dispose();
            
            // Rekonstruksi ulang panel ke dalam FormNotaServis agar tidak hilang dari layar
            this.removeAll();
            JScrollPane scrollCetak = UIHelper.createCustomScroll(pnlCetak);
            scrollCetak.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            this.add(scrollCetak, "grow, wmin 0");
            this.add(createFooterPanel(), "left, gaptop 15");
            
            this.revalidate();
            this.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memproses cetak PDF: \n" + ex.getMessage(), "Error Print", JOptionPane.ERROR_MESSAGE);
        }
    }
    // --- BLOK KODE YANG DIPERBARUI SELESAI DI SINI ---

    private void loadDataNota() {
        String idNota = FormManager.getLoggedInUser();
        if (idNota == null) idNota = "N00007"; 
        
        idNotaUntukDicetak = idNota;
        lblHeaderNota.setText("Nomor Nota: " + idNota);
        lblPesanError.setVisible(false);
        final int idServisAngka = Integer.parseInt(idNota.replace("N", ""));

        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            private String dbTglMasuk = "-", dbTglSelesai = "-", dbTglAmbil = "-"; 
            private String dbNama = "-", dbTelepon = "-", dbPerangkat = "-", dbKelengkapan = "-";
            private String dbKeluhan = "-", dbDiagnosa = "-", dbTindakan = "-";
            private String dbStatusBayar = "-", dbGaransi = "-";
            private double dbBiaya = 0, dbJasa = 0;
            private boolean isFound = false;
            private String errorMsg = "";
            private java.util.List<String[]> listParts = new java.util.ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                String sql = "SELECT * FROM vw_detail_nota WHERE id_servis = ?";
                
                try (Connection conn = DatabaseConnection.getKoneksi();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, idServisAngka);
                    try (ResultSet res = ps.executeQuery()) {
                        if (res.next()) {
                            dbTglMasuk = res.getString("tgl_masuk"); dbNama = res.getString("nama_pelanggan");
                            dbTelepon = res.getString("no_whatsapp"); dbPerangkat = res.getString("merek") + " " + res.getString("tipe_model");
                            dbKelengkapan = res.getString("kelengkapan"); dbKeluhan = res.getString("keluhan_awal");
                            dbDiagnosa = res.getString("hasil_diagnosa"); 
                            dbJasa = res.getDouble("biaya_jasa");
                            
                            String tindakanDb = res.getString("tindakan_perbaikan");
                            dbTindakan = (tindakanDb != null && !tindakanDb.trim().isEmpty()) ? tindakanDb : "Pengecekan / Servis Unit";
                            
                            dbTglSelesai = res.getString("tanggal_selesai"); dbTglAmbil = res.getString("tgl_ambil"); 
                            dbBiaya = res.getDouble("total_biaya"); dbStatusBayar = res.getString("status_pembayaran");
                            dbGaransi = res.getString("masa_garansi");
                            
                            String statusLengkap = res.getString("status");
                            if ("Selesai".equalsIgnoreCase(statusLengkap)) {
                                if (dbTglAmbil != null && !dbTglAmbil.trim().isEmpty()) {
                                    dbStatusBayar = "Telah Diambil / " + (dbStatusBayar != null ? dbStatusBayar : "Lunas");
                                } else {
                                    dbStatusBayar = "Siap Diambil / " + (dbStatusBayar != null ? dbStatusBayar : "Lunas");
                                }
                            } else if ("Batal".equalsIgnoreCase(statusLengkap)) {
                                dbStatusBayar = "Dibatalkan";
                            }
                            isFound = true;
                        }
                    }
                    
                    if (isFound) {
                        String sqlParts = "SELECT sp.kategori, sp.nama_sparepart, det.qty, det.subtotal " +
                                          "FROM detail_pengambilan_sparepart det " +
                                          "JOIN data_sparepart sp ON det.id_sparepart = sp.id_sparepart " +
                                          "JOIN data_pengambilan pg ON det.id_pengambilan = pg.id_pengambilan " +
                                          "WHERE pg.id_servis = ?";
                        try (PreparedStatement psParts = conn.prepareStatement(sqlParts)) {
                            psParts.setInt(1, idServisAngka);
                            try (ResultSet rsParts = psParts.executeQuery()) {
                                while(rsParts.next()) {
                                    String namaLengkapPart = rsParts.getString("kategori") + " " + rsParts.getString("nama_sparepart");
                                    listParts.add(new String[]{
                                        namaLengkapPart, 
                                        String.valueOf(rsParts.getInt("qty")),
                                        String.valueOf(rsParts.getDouble("subtotal"))
                                    });
                                }
                            }
                        }
                    }
                } catch (Exception e) { errorMsg = e.getMessage(); throw e;  }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); 
                    if (isFound) {
                        lblNama.setText(dbNama); lblTelepon.setText(dbTelepon);
                        lblPerangkat.setText(dbPerangkat); lblTglMasuk.setText(dbTglMasuk != null ? dbTglMasuk : "-");
                        txtKelengkapan.setText(dbKelengkapan != null ? dbKelengkapan : "-");
                        txtKeluhan.setText(dbKeluhan != null ? dbKeluhan : "-");
                        txtDiagnosa.setText((dbDiagnosa != null && !dbDiagnosa.trim().isEmpty()) ? dbDiagnosa : "Belum dianalisa teknisi");
                        txtTindakan.setText(dbTindakan);
                        
                        NumberFormat formatRp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        lblValJasa.setText(formatRp.format(dbJasa).replace(",00", ""));
                        
                        pnlDaftarKomponen.removeAll();
                        pnlDaftarKomponen.add(createTitleLabel("Penggantian Komponen:"), "span 2");
                        
                        if (listParts.isEmpty()) {
                            JLabel lblKosong = new JLabel("   - Tidak ada part yang diganti");
                            lblKosong.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                            lblKosong.setForeground(TEXT_MUTED);
                            pnlDaftarKomponen.add(lblKosong, "span 2");
                        } else {
                            for (String[] part : listParts) {
                                String namaPart = "   - " + part[0] + " (x" + part[1] + ")";
                                String hargaPart = formatRp.format(Double.parseDouble(part[2])).replace(",00", "");

                                JLabel lblNm = new JLabel(namaPart);
                                lblNm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                                lblNm.setForeground(new Color(100, 100, 110));

                                JLabel lblHg = new JLabel(hargaPart);
                                lblHg.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                lblHg.setForeground(SIDEBAR_MAIN_COLOR);

                                pnlDaftarKomponen.add(lblNm); pnlDaftarKomponen.add(lblHg);
                            }
                        }
                        pnlDaftarKomponen.revalidate(); pnlDaftarKomponen.repaint();
                        
                        lblTglSelesai.setText((dbTglSelesai == null || dbTglSelesai.trim().isEmpty()) ? "Belum Selesai" : dbTglSelesai);
                        lblTglAmbil.setText((dbTglAmbil == null || dbTglAmbil.trim().isEmpty()) ? "Belum Diambil" : dbTglAmbil);
                        
                        if (dbStatusBayar == null || dbStatusBayar.isEmpty()) {
                            lblStatusBayar.setText("Belum Lunas"); lblStatusBayar.setForeground(ERROR_RED);
                            lblTitleTotalBayar.setText("TOTAL PEMBAYARAN / ESTIMASI");
                        } else if (dbStatusBayar.contains("Lunas") || dbStatusBayar.contains("Diambil") || dbStatusBayar.contains("Tunai") || dbStatusBayar.contains("Transfer")) {
                            lblStatusBayar.setText(dbStatusBayar); lblStatusBayar.setForeground(FINISH_GREEN);
                            lblTitleTotalBayar.setText("TOTAL PEMBAYARAN");
                        } else {
                            lblStatusBayar.setText(dbStatusBayar); lblStatusBayar.setForeground(ERROR_RED);
                            lblTitleTotalBayar.setText("TOTAL PEMBAYARAN / ESTIMASI");
                        }

                        if (dbGaransi == null || dbGaransi.isEmpty()) {
                            lblGaransi.setText("Belum Ditentukan"); lblGaransi.setForeground(SIDEBAR_MAIN_COLOR);
                        } else if (dbGaransi.equalsIgnoreCase("Tidak Garansi")) {
                            lblGaransi.setText(dbGaransi); lblGaransi.setForeground(ERROR_RED);
                        } else {
                            lblGaransi.setText(dbGaransi); lblGaransi.setForeground(FINISH_GREEN);
                        }
                        
                        if (dbBiaya == 0 && (dbStatusBayar == null || !dbStatusBayar.contains("Lunas"))) {
                            lblBiaya.setText("Menunggu Kalkulasi");
                            lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
                            lblBiaya.setForeground(ACCENT_ORANGE);
                        } else {
                            lblBiaya.setText(formatRp.format(dbBiaya).replace(",00", ""));
                            lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 28));
                            lblBiaya.setForeground(FINISH_GREEN);
                        }
                    } else {
                        lblPesanError.setText("Data Nota Tidak Ditemukan di Database."); lblPesanError.setVisible(true);
                    }
                } catch (Exception e) {
                    lblPesanError.setText("Error Query SQL: " + errorMsg); lblPesanError.setVisible(true);
                }
            }
        };
        worker.execute();
    }
}