package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.utils.UIHelper;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class DialogDetailNota extends JDialog {

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

    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color TEXT_MUTED = new Color(110, 115, 130);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color FINISH_GREEN = new Color(39, 174, 96);

    private String idNota;
    private int idServis;

    public DialogDetailNota(JFrame parent, String idNota) {
        super(parent, "Detail & Cetak Nota", true); 
        this.idNota = idNota;
        try { this.idServis = Integer.parseInt(idNota.replaceAll("[^0-9]", "")); } 
        catch (Exception e) { this.idServis = 0; }
        
        initUI();
        loadDataNota();
    }

    private void initUI() {
        setSize(850, 780); // Tinggi sedikit ditambah agar lebih lega
        setLocationRelativeTo(getParent());
        setResizable(true);
        setLayout(new MigLayout("insets 0, fill", "[grow]", "[grow, fill][]")); // MigLayout untuk main content
        getContentPane().setBackground(new Color(245, 245, 248));

        pnlCetak = new JPanel(new MigLayout("wrap, fillx, insets 40", "[fill, grow]", "[]15[]25[]25[]"));
        pnlCetak.setBackground(CARD_BG_COLOR);

        // HEADER
        JPanel pnlHeader = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right]", "[]"));
        pnlHeader.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Detail Nota Servis");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);
        
        lblHeaderNota = new JLabel("Nomor Nota: " + idNota);
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

        // GRID 2 KOLOM
        JPanel panelGrid = new JPanel(new MigLayout("insets 0, fillx", "[fill, 48%]4%[fill, 48%]", "[]"));
        panelGrid.setOpaque(false);

        // KOLOM KIRI
        JPanel pnlKiri = new JPanel(new MigLayout("wrap, fillx, insets 0", "[140!][fill, grow]", "[]15[]12[]12[]12[]12[]"));
        pnlKiri.setOpaque(false);
        
        JLabel lblTitleKiri = new JLabel("INFO PELANGGAN & PERANGKAT");
        lblTitleKiri.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleKiri.setForeground(ACCENT_ORANGE);
        pnlKiri.add(lblTitleKiri, "span 2, gapbottom 10");

        lblNama = createDataLabel("-"); lblTelepon = createDataLabel("-");
        lblPerangkat = createDataLabel("-"); txtKelengkapan = createDataTextArea();
        lblTglMasuk = createDataLabel("-");

        pnlKiri.add(createTitleLabel("Nama Pelanggan")); pnlKiri.add(lblNama);
        pnlKiri.add(createTitleLabel("No. Telp / WA")); pnlKiri.add(lblTelepon);
        pnlKiri.add(createTitleLabel("Tipe Perangkat")); pnlKiri.add(lblPerangkat);
        pnlKiri.add(createTitleLabel("Kelengkapan Bawaan"), "aligny top, gaptop 2"); pnlKiri.add(txtKelengkapan, "growx, wmin 10");
        pnlKiri.add(createTitleLabel("Tanggal Masuk")); pnlKiri.add(lblTglMasuk);

        // KOLOM KANAN
        JPanel pnlKanan = new JPanel(new MigLayout("wrap, fillx, insets 0", "[140!][fill, grow]", "[]15[]12[]12[]12[]12[]"));
        pnlKanan.setOpaque(false);

        JLabel lblTitleKanan = new JLabel("DETAIL KERUSAKAN & TINDAKAN");
        lblTitleKanan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleKanan.setForeground(ACCENT_ORANGE);
        pnlKanan.add(lblTitleKanan, "span 2, gapbottom 10");

        txtKeluhan = createDataTextArea(); txtDiagnosa = createDataTextArea();
        txtTindakan = createDataTextArea(); lblTglSelesai = createDataLabel("-");
        lblTglAmbil = createDataLabel("-");

        pnlKanan.add(createTitleLabel("Keluhan Awal"), "aligny top, gaptop 2"); pnlKanan.add(txtKeluhan, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Hasil Diagnosa"), "aligny top, gaptop 2"); pnlKanan.add(txtDiagnosa, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Tindakan Servis"), "aligny top, gaptop 2"); pnlKanan.add(txtTindakan, "growx, wmin 10");
        pnlKanan.add(createTitleLabel("Tanggal Selesai")); pnlKanan.add(lblTglSelesai);
        pnlKanan.add(createTitleLabel("Tanggal Diambil")); pnlKanan.add(lblTglAmbil);

        panelGrid.add(pnlKiri, "top"); panelGrid.add(pnlKanan, "top");
        pnlCetak.add(panelGrid, "growx");

        // FINANSIAL & GARANSI
        JPanel panelFinansialTitle = new JPanel(new MigLayout("insets 0", "[]")); panelFinansialTitle.setOpaque(false);
        JLabel lblTitleFins = new JLabel("FINANSIAL & GARANSI");
        lblTitleFins.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleFins.setForeground(ACCENT_ORANGE);
        panelFinansialTitle.add(lblTitleFins, "gaptop 15, gapbottom 5");
        pnlCetak.add(panelFinansialTitle, "growx");

        // KOLOM KANAN DIKUNCI AGAR HARGA TIDAK TERPOTONG
        cardFinansial = new JPanel(new MigLayout("wrap 2, fillx, insets 25 30 25 30", "[fill, grow][fill, 250!]", "[]10[]5[]15[]10[]"));
        cardFinansial.setBackground(new Color(248, 250, 252));
        cardFinansial.putClientProperty(FlatClientProperties.STYLE, "border:1,solid,#f1f3f8");
        
        JLabel lblTeksTotal = createTitleLabel("TOTAL");
        lblTeksTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTeksTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 
        
        cardFinansial.add(createTitleLabel("RINCIAN TRANSAKSI")); 
        cardFinansial.add(lblTeksTotal);
        cardFinansial.add(new JSeparator(), "span 2, growx, gapy 5 10");
        
        lblTitleJasa = createTitleLabel("Biaya Jasa Teknisi & Perbaikan"); 
        lblValJasa = createDataLabel("Rp 0");
        lblValJasa.setHorizontalAlignment(SwingConstants.RIGHT);
        lblValJasa.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        cardFinansial.add(lblTitleJasa); cardFinansial.add(lblValJasa);
        
        pnlDaftarKomponen = new JPanel(new MigLayout("wrap 2, fillx, insets 0", "[fill, grow][fill, 200!]", "[]4[]"));
        pnlDaftarKomponen.setOpaque(false);
        cardFinansial.add(pnlDaftarKomponen, "span 2, growx, gapy 5 5");
        
        lblGaransi = createDataLabel("-"); lblStatusBayar = createDataLabel("-");
        lblStatusBayar.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JPanel pnlGaransi = new JPanel(new MigLayout("insets 0", "[][]", "[]")); pnlGaransi.setOpaque(false);
        pnlGaransi.add(createTitleLabel("Masa Garansi Servis:")); pnlGaransi.add(lblGaransi);
        cardFinansial.add(pnlGaransi, "gaptop 15"); 
        
        JPanel pnlBayar = new JPanel(new MigLayout("insets 0, fillx", "[grow, right][]", "[]")); pnlBayar.setOpaque(false);
        pnlBayar.add(createTitleLabel("Status Pembayaran:")); pnlBayar.add(lblStatusBayar);
        pnlBayar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        cardFinansial.add(pnlBayar, "gaptop 15");
        
        lblBiaya = new JLabel("Rp 0"); 
        lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 32)); 
        lblBiaya.setForeground(FINISH_GREEN); 
        lblBiaya.setHorizontalAlignment(SwingConstants.RIGHT);
        lblBiaya.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 
        cardFinansial.add(new JSeparator(), "span 2, growx, gaptop 15, gapbottom 10");
        
        lblTitleTotalBayar = new JLabel("TOTAL PEMBAYARAN");
        lblTitleTotalBayar.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        lblTitleTotalBayar.setForeground(SIDEBAR_MAIN_COLOR);
        cardFinansial.add(lblTitleTotalBayar, "aligny center"); 
        cardFinansial.add(lblBiaya, "aligny center");
        
        pnlCetak.add(cardFinansial, "growx, gaptop 10");

        JLabel lblFooterNB = new JLabel("NB: Nota ini adalah bukti resmi transaksi servis di Cheerful People Service Center.");
        lblFooterNB.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterNB.setForeground(TEXT_MUTED);
        pnlCetak.add(lblFooterNB, "center, gaptop 20");

        // MEMASUKKAN KE SCROLLPANE (KONTEN UTAMA)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        wrapper.setOpaque(false);
        wrapper.add(pnlCetak, BorderLayout.CENTER);
        
        JScrollPane scrollPane = UIHelper.createCustomScroll(wrapper);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // TAMBAHKAN SCROLLPANE KE LAYOUT UTAMA
        add(scrollPane, "grow");

        // --- PANEL TOMBOL BAWAH (STICKY FOOTER) ---
        // Desain baru: Rata kanan-kiri yang rapi dengan padding pas
        JPanel pnlBottom = new JPanel(new MigLayout("insets 15 30 15 30, fillx", "[grow][grow, right]", "[]"));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 235)));

        JButton btnTutup = new JButton("Kembali");
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e2e8f0; foreground:#333333; font:bold +1; padding:12,30,12,30; borderWidth:0");
        btnTutup.addActionListener(e -> dispose());

        JButton btnCetak = new JButton("Cetak / Simpan PDF");
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", FINISH_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; padding:12,35,12,35; borderWidth:0");
        btnCetak.addActionListener(e -> cetakKePDF());

        pnlBottom.add(btnTutup); // Taruh di kiri
        pnlBottom.add(btnCetak); // Taruh di kanan
        
        // TAMBAHKAN FOOTER KE LAYOUT BAWAH
        add(pnlBottom, "dock south");
    }

    private JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lbl.setForeground(TEXT_MUTED); return lbl;
    }

    private JLabel createDataLabel(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); lbl.setForeground(SIDEBAR_MAIN_COLOR); return lbl;
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

    private void cetakKePDF() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Kwitansi_" + idNota);

            PageFormat pf = job.defaultPage();
            Paper paper = pf.getPaper();

            double margin = 15; 
            paper.setSize(595, 842); 
            paper.setImageableArea(margin, margin, 595 - (margin * 2), 842 - (margin * 2));
            pf.setPaper(paper);
            pf.setOrientation(PageFormat.PORTRAIT);
            
            pf = job.validatePage(pf);

            Dimension originalSize = pnlCetak.getSize();
            Dimension prefSize = pnlCetak.getPreferredSize();
            
            int virtualWidth = Math.max(900, prefSize.width);
            pnlCetak.setSize(virtualWidth, prefSize.height + 50);
            pnlCetak.doLayout();

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                double scaleX = pageFormat.getImageableWidth() / pnlCetak.getWidth();
                double scaleY = pageFormat.getImageableHeight() / pnlCetak.getHeight();
                double scale = Math.min(scaleX, scaleY);
                
                if (scale > 1.0) scale = 1.0; 
                
                g2d.scale(scale, scale);
                pnlCetak.printAll(g2d);
                
                return Printable.PAGE_EXISTS;
            }, pf);

            if (job.printDialog()) {
                job.print();
                UIHelper.tampilkanNotif(this, "Sukses", "Nota berhasil dicetak/disimpan sebagai PDF!", "success");
            }

            pnlCetak.setSize(originalSize);
            pnlCetak.revalidate();
            pnlCetak.repaint();

        } catch (Exception ex) {
            UIHelper.tampilkanNotif(this, "Error Print", "Gagal mencetak: \n" + ex.getMessage(), "error");
        }
    }

    private void loadDataNota() {
        lblPesanError.setVisible(false);

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
                    ps.setInt(1, idServis);
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
                            if ("Selesai".equalsIgnoreCase(statusLengkap) || "Diambil".equalsIgnoreCase(statusLengkap)) {
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
                            psParts.setInt(1, idServis);
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
                        pnlDaftarKomponen.add(createTitleLabel("Penggantian Komponen:"), "span 2, gapbottom 5");
                        
                        if (listParts.isEmpty()) {
                            JLabel lblKosong = new JLabel("   - Tidak ada part yang diganti");
                            lblKosong.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                            lblKosong.setForeground(TEXT_MUTED);
                            pnlDaftarKomponen.add(lblKosong, "span 2");
                        } else {
                            for (String[] part : listParts) {
                                String namaPart = "   - " + part[0] + " (x" + part[1] + ")";
                                String hargaPart = formatRp.format(Double.parseDouble(part[2])).replace(",00", "");

                                JLabel lblNm = new JLabel(namaPart);
                                lblNm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                                lblNm.setForeground(SIDEBAR_MAIN_COLOR);

                                JLabel lblHg = new JLabel(hargaPart);
                                lblHg.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                lblHg.setForeground(SIDEBAR_MAIN_COLOR);
                                lblHg.setHorizontalAlignment(SwingConstants.RIGHT);
                                lblHg.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // BANTALAN AMAN

                                pnlDaftarKomponen.add(lblNm); 
                                pnlDaftarKomponen.add(lblHg);
                            }
                        }
                        pnlDaftarKomponen.revalidate(); pnlDaftarKomponen.repaint();
                        
                        lblTglSelesai.setText((dbTglSelesai == null || dbTglSelesai.trim().isEmpty()) ? "Belum Selesai" : dbTglSelesai);
                        lblTglAmbil.setText((dbTglAmbil == null || dbTglAmbil.trim().isEmpty()) ? "Belum Diambil" : dbTglAmbil);
                        
                        if (dbStatusBayar == null || dbStatusBayar.isEmpty()) {
                            lblStatusBayar.setText("Belum Lunas"); lblStatusBayar.setForeground(ERROR_RED);
                        } else if (dbStatusBayar.contains("Lunas") || dbStatusBayar.contains("Diambil") || dbStatusBayar.contains("Tunai") || dbStatusBayar.contains("Transfer")) {
                            lblStatusBayar.setText(dbStatusBayar); lblStatusBayar.setForeground(FINISH_GREEN);
                        } else {
                            lblStatusBayar.setText(dbStatusBayar); lblStatusBayar.setForeground(ERROR_RED);
                        }

                        if (dbGaransi == null || dbGaransi.isEmpty()) {
                            lblGaransi.setText("Belum Ditentukan"); lblGaransi.setForeground(SIDEBAR_MAIN_COLOR);
                        } else if (dbGaransi.equalsIgnoreCase("Tidak Garansi") || dbGaransi.equals("-")) {
                            lblGaransi.setText("Tidak Garansi"); lblGaransi.setForeground(ERROR_RED);
                        } else {
                            lblGaransi.setText(dbGaransi); lblGaransi.setForeground(FINISH_GREEN);
                        }
                        
                        if (dbBiaya == 0 && (dbStatusBayar == null || !dbStatusBayar.contains("Lunas"))) {
                            lblBiaya.setText("Rp 0");
                            lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
                            lblBiaya.setForeground(ACCENT_ORANGE);
                        } else {
                            lblBiaya.setText(formatRp.format(dbBiaya).replace(",00", ""));
                            lblBiaya.setFont(new Font("Segoe UI", Font.BOLD, 32));
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