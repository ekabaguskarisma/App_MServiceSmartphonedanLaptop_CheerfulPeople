package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class DialogDetailNota extends JDialog {

    private final Color APP_BG_COLOR = new Color(248, 249, 252);
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0);
    private final Color FINISH_GREEN = new Color(39, 174, 96);
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    private String idNota;
    private int idServis;

    private JLabel lblTanggalNow, lblStatus, lblTotalBiaya, lblMetode, lblMasaGaransi;
    private JLabel lblNama, lblWa, lblAntrean, lblTipe, lblMerek, lblGaransiBawaan;
    private JLabel lblTglSelesai, lblTglAmbil; 
    private JTextArea txtKeluhan, txtDiagnosa, txtTindakan;
    
    private JLabel lblValJasa, lblTitlePart, lblValPart;
    private JPanel cardRincian;
    
    class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) { super(layout); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle v, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle v, int o, int d) { return 16; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; } 
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public DialogDetailNota(JFrame parent, String idNota) {
        super(parent, "Detail Kelengkapan Nota", true); 
        this.idNota = idNota;
        // Mengamankan parsing ID Servis dari format N00001
        try {
            this.idServis = Integer.parseInt(idNota.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            this.idServis = 0;
        }
        
        initUI();
        loadDataNota();
    }

    private void initUI() {
        setSize(550, 750); 
        setLocationRelativeTo(getParent());
        setResizable(true);

        ScrollablePanel mainPanel = new ScrollablePanel(new MigLayout("wrap, fillx, insets 25 30 25 30", "[fill, grow]", "[]10[]15[]10[]10[]10[]15[]"));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Detail Kelengkapan Nota");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);
        
        lblTanggalNow = new JLabel(new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID")).format(new Date()));
        lblTanggalNow.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTanggalNow.setForeground(TEXT_MUTED);
        
        mainPanel.add(lblTitle);
        mainPanel.add(lblTanggalNow, "gapbottom 15");

        JLabel lblSubInfo = new JLabel("Servis Info");
        lblSubInfo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSubInfo.setForeground(SIDEBAR_MAIN_COLOR);
        
        JPanel pnlInfo = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        pnlInfo.setOpaque(false);
        
        JLabel lblLblNota = new JLabel("Nomor Nota:");
        lblLblNota.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLblNota.setForeground(SIDEBAR_MAIN_COLOR);
        pnlInfo.add(lblLblNota);
        
        JLabel lblValNota = new JLabel(idNota);
        lblValNota.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValNota.setForeground(ACCENT_ORANGE);
        pnlInfo.add(lblValNota);
        
        mainPanel.add(lblSubInfo);
        mainPanel.add(pnlInfo, "gapbottom 10");

        mainPanel.add(createSectionTitle("PELANGGAN"));
        JPanel cardPelanggan = createCardPanel();
        cardPelanggan.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[fill, 50%][fill, 50%]", "[]5[]10[]"));
        
        lblNama = createValLabel(); lblWa = createValLabel(); lblAntrean = createValLabel();
        
        cardPelanggan.add(createTitleLabel("Nama Pelanggan:")); cardPelanggan.add(createTitleLabel("No. WhatsApp:"));
        cardPelanggan.add(lblNama); cardPelanggan.add(lblWa);
        cardPelanggan.add(createTitleLabel("No. Antrean:"), "gaptop 5"); cardPelanggan.add(new JLabel(""));
        cardPelanggan.add(lblAntrean); cardPelanggan.add(new JLabel(""));
        
        mainPanel.add(cardPelanggan);

        mainPanel.add(createSectionTitle("PERANGKAT"));
        JPanel cardPerangkat = createCardPanel();
        cardPerangkat.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[fill, 50%][fill, 50%]", "[]5[]10[]"));
        
        lblTipe = createValLabel(); lblMerek = createValLabel(); lblGaransiBawaan = createValLabel();
        
        cardPerangkat.add(createTitleLabel("Tipe Perangkat:")); cardPerangkat.add(createTitleLabel("Merek / Model:"));
        cardPerangkat.add(lblTipe); cardPerangkat.add(lblMerek);
        
        JPanel pnlGaransi = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        pnlGaransi.setOpaque(false);
        pnlGaransi.add(createTitleLabel("Kelengkapan: ")); pnlGaransi.add(lblGaransiBawaan);
        cardPerangkat.add(pnlGaransi, "span 2, gaptop 5");
        
        mainPanel.add(cardPerangkat);

        mainPanel.add(createSectionTitle("DETAIL KERUSAKAN"));
        JPanel cardKerusakan = createCardPanel();
        cardKerusakan.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[110!][fill, grow]", "[]15[]15[]15[]15[]"));
        
        txtKeluhan = createTextArea(); txtDiagnosa = createTextArea(); txtTindakan = createTextArea();
        lblTglSelesai = createValLabel(); lblTglAmbil = createValLabel();
        
        JLabel lblT1 = new JLabel("Keluhan Awal:"); lblT1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cardKerusakan.add(lblT1); 
        cardKerusakan.add(txtKeluhan, "growx, wmin 10");
        cardKerusakan.add(new JSeparator(), "span 2, growx, gapy 5 5");
        
        JLabel lblT2 = new JLabel("Diagnosa Awal:"); lblT2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cardKerusakan.add(lblT2); 
        cardKerusakan.add(txtDiagnosa, "growx, wmin 10");
        cardKerusakan.add(new JSeparator(), "span 2, growx, gapy 5 5");
        
        JLabel lblT3 = new JLabel("Tindakan Servis:"); lblT3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cardKerusakan.add(lblT3); 
        cardKerusakan.add(txtTindakan, "growx, wmin 10");
        cardKerusakan.add(new JSeparator(), "span 2, growx, gapy 5 5");

        JLabel lblT4 = new JLabel("Tgl. Selesai:"); lblT4.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cardKerusakan.add(lblT4); cardKerusakan.add(lblTglSelesai);
        
        JLabel lblT5 = new JLabel("Tgl. Diambil:"); lblT5.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cardKerusakan.add(lblT5); cardKerusakan.add(lblTglAmbil);
        
        mainPanel.add(cardKerusakan);

        cardRincian = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[fill, grow][right]", "[]8[]"));
        cardRincian.setBackground(new Color(248, 249, 252));
        cardRincian.putClientProperty(FlatClientProperties.STYLE, "arc:15; border:1,solid,#e2e8f0");
        
        JLabel lblRincianTitle = new JLabel("Rincian Biaya Transaksi");
        lblRincianTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRincianTitle.setForeground(ACCENT_ORANGE);
        
        lblValJasa = createValLabel();
        lblTitlePart = createTitleLabel("Penggantian Komponen");
        lblValPart = createValLabel();
        
        cardRincian.add(lblRincianTitle, "span 2, gapbottom 5");
        cardRincian.add(createTitleLabel("Biaya Jasa Teknisi & Perbaikan:")); cardRincian.add(lblValJasa);
        cardRincian.add(lblTitlePart); cardRincian.add(lblValPart);
        
        mainPanel.add(cardRincian);

        mainPanel.add(createSectionTitle("TRANSAKSI"));
        JPanel cardTransaksi = createCardPanel();
        cardTransaksi.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[fill, grow][right]", "[]10[]10[]10[]"));
        
        lblStatus = createValLabel(); lblTotalBiaya = createValLabel(); 
        lblMetode = createValLabel(); lblMasaGaransi = createValLabel();
        
        cardTransaksi.add(new JLabel("Status:")); cardTransaksi.add(lblStatus);
        cardTransaksi.add(new JLabel("Total Biaya:")); cardTransaksi.add(lblTotalBiaya);
        cardTransaksi.add(new JLabel("Metode Pembayaran:")); cardTransaksi.add(lblMetode);
        cardTransaksi.add(new JLabel("Masa Garansi Servis:")); cardTransaksi.add(lblMasaGaransi);
        
        mainPanel.add(cardTransaksi);

        JButton btnCetak = new JButton("Print Kwitansi (PDF)");
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", FINISH_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; padding:10,0,10,0; borderWidth:0");
        btnCetak.addActionListener(e -> cetakPDF());
        
        mainPanel.add(btnCetak, "growx, gaptop 10");

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:8; trackArc:999; thumbArc:999;");
        
        setContentPane(scrollPane);
    }

    private JLabel createSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(SIDEBAR_MAIN_COLOR);
        return lbl;
    }

    private JPanel createCardPanel() {
        JPanel pnl = new JPanel();
        pnl.setBackground(Color.WHITE);
        pnl.putClientProperty(FlatClientProperties.STYLE, "arc:15; border:1,solid," + String.format("#%06x", BORDER_COLOR.getRGB() & 0xFFFFFF));
        return pnl;
    }

    private JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }
    
    private JLabel createValLabel() {
        JLabel lbl = new JLabel("-");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(SIDEBAR_MAIN_COLOR);
        return lbl;
    }

    private JTextArea createTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false); ta.setFocusable(false); 
        ta.setLineWrap(true); ta.setWrapStyleWord(true); 
        ta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ta.setForeground(SIDEBAR_MAIN_COLOR); ta.setBackground(Color.WHITE); ta.setBorder(null);
        return ta;
    }
    
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
    
    private void cetakPDF() {
        tampilkanNotif("Info Cetak", "Membuka Print Dialog... (Fitur PDF sedang disiapkan)", "success");
    }

    private void loadDataNota() {
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            NumberFormat formatRp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            // 1. QUERY UTAMA MENGGUNAKAN VIEW (Sangat Aman & Cepat)
            String sqlView = "SELECT * FROM vw_detail_nota WHERE id_servis = ?";
            PreparedStatement psView = kon.prepareStatement(sqlView);
            psView.setInt(1, idServis);
            ResultSet rs = psView.executeQuery();

            if (rs.next()) {
                // Isi Data Pelanggan
                lblNama.setText(rs.getString("nama_pelanggan"));
                lblWa.setText(rs.getString("no_whatsapp"));
                lblAntrean.setText(String.format("%03d", idServis)); 
                
                // Isi Data Perangkat
                lblTipe.setText(rs.getString("merek")); // Jenis (HP/Laptop)
                lblMerek.setText(rs.getString("perangkat")); // Gabungan Merek Tipe
                lblGaransiBawaan.setText(rs.getString("kelengkapan")); 
                
                // Isi Detail Kerusakan
                txtKeluhan.setText(rs.getString("keluhan_awal"));
                txtDiagnosa.setText(rs.getString("hasil_diagnosa") != null ? rs.getString("hasil_diagnosa") : "-");
                txtTindakan.setText(rs.getString("tindakan_perbaikan") != null ? rs.getString("tindakan_perbaikan") : "-");
                
                lblTglSelesai.setText(rs.getString("tanggal_selesai") != null ? rs.getString("tanggal_selesai") : "-");
                lblTglAmbil.setText(rs.getString("tgl_ambil") != null ? rs.getString("tgl_ambil") : "-");

                // Isi Data Transaksi
                String status = rs.getString("status");
                lblStatus.setText(status);
                lblStatus.setForeground(status.equalsIgnoreCase("Diambil") ? FINISH_GREEN : ACCENT_ORANGE);

                double totalBiaya = rs.getDouble("total_biaya");
                lblTotalBiaya.setText(formatRp.format(totalBiaya).replace(",00", ""));
                lblMetode.setText(rs.getString("status_pembayaran") != null ? rs.getString("status_pembayaran") : "-");
                lblMasaGaransi.setText(rs.getString("masa_garansi") != null ? rs.getString("masa_garansi") : "-");
                
                // 2. QUERY KEDUA: AMBIL DAFTAR SPAREPART (Karena bisa lebih dari satu)
                double totalHargaSp = 0;
                StringBuilder daftarSp = new StringBuilder();
                
                String sqlSp = "SELECT s.nama_sparepart, d.qty, d.subtotal " +
                               "FROM detail_pengambilan_sparepart d " +
                               "JOIN data_sparepart s ON d.id_sparepart = s.id_sparepart " +
                               "JOIN data_pengambilan p ON d.id_pengambilan = p.id_pengambilan " +
                               "WHERE p.id_servis = ?";
                PreparedStatement psSp = kon.prepareStatement(sqlSp);
                psSp.setInt(1, idServis);
                ResultSet rsSp = psSp.executeQuery();
                
                while(rsSp.next()) {
                    if (daftarSp.length() > 0) daftarSp.append(", ");
                    daftarSp.append(rsSp.getString("nama_sparepart")).append(" (").append(rsSp.getInt("qty")).append("x)");
                    totalHargaSp += rsSp.getDouble("subtotal");
                }
                
                // Update Tampilan Rincian
                double biayaJasa = rs.getDouble("biaya_jasa");
                lblValJasa.setText(formatRp.format(biayaJasa).replace(",00", ""));
                
                if (daftarSp.length() > 0) {
                    lblTitlePart.setText("Ganti Komponen: " + daftarSp.toString());
                    lblValPart.setText(formatRp.format(totalHargaSp).replace(",00", ""));
                    lblTitlePart.setVisible(true); lblValPart.setVisible(true);
                    cardRincian.setVisible(true);
                } else if (biayaJasa > 0) {
                    lblTitlePart.setVisible(false); lblValPart.setVisible(false);
                    cardRincian.setVisible(true);
                } else {
                    cardRincian.setVisible(false);
                }
            }
        } catch (Exception e) {
            tampilkanNotif("Error Database", "Gagal memuat detail nota: " + e.getMessage(), "error");
        }
    }
}