package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
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

    // Komponen Data
    private JLabel lblTanggalNow, lblStatus, lblTotalBiaya, lblMetode, lblMasaGaransi;
    private JLabel lblNama, lblWa, lblAntrean, lblTipe, lblMerek, lblGaransiBawaan;
    private JTextArea txtKeluhan, txtDiagnosa, txtTindakan;
    
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
        this.idServis = Integer.parseInt(idNota.replace("N", ""));
        
        initUI();
        loadDataNota();
    }

    private void initUI() {
        setSize(550, 750); 
        setLocationRelativeTo(getParent());
        setResizable(true);

        // Custom ScrollablePanel
        ScrollablePanel mainPanel = new ScrollablePanel(new MigLayout("wrap, fillx, insets 25 30 25 30", "[fill, grow]", "[]10[]15[]10[]10[]10[]15[]"));
        mainPanel.setBackground(Color.WHITE);

        // TITLE HEADER
        JLabel lblTitle = new JLabel("Detail Kelengkapan Nota");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);
        
        lblTanggalNow = new JLabel(new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID")).format(new Date()));
        lblTanggalNow.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTanggalNow.setForeground(TEXT_MUTED);
        
        mainPanel.add(lblTitle);
        mainPanel.add(lblTanggalNow, "gapbottom 15");

        // SERVIS INFO
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

        // PELANGGAN CARD
        mainPanel.add(createSectionTitle("PELANGGAN"));
        JPanel cardPelanggan = createCardPanel();
        cardPelanggan.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[fill, 50%][fill, 50%]", "[]5[]10[]"));
        
        lblNama = createValLabel(); lblWa = createValLabel(); lblAntrean = createValLabel();
        
        cardPelanggan.add(createTitleLabel("Nama Pelanggan:")); cardPelanggan.add(createTitleLabel("No. WhatsApp:"));
        cardPelanggan.add(lblNama); cardPelanggan.add(lblWa);
        cardPelanggan.add(createTitleLabel("No. Antrean:"), "gaptop 5"); cardPelanggan.add(new JLabel(""));
        cardPelanggan.add(lblAntrean); cardPelanggan.add(new JLabel(""));
        
        mainPanel.add(cardPelanggan);

        // PERANGKAT CARD
        mainPanel.add(createSectionTitle("PERANGKAT"));
        JPanel cardPerangkat = createCardPanel();
        cardPerangkat.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[fill, 50%][fill, 50%]", "[]5[]10[]"));
        
        lblTipe = createValLabel(); lblMerek = createValLabel(); lblGaransiBawaan = createValLabel();
        
        cardPerangkat.add(createTitleLabel("Tipe Perangkat:")); cardPerangkat.add(createTitleLabel("Merek / Model:"));
        cardPerangkat.add(lblTipe); cardPerangkat.add(lblMerek);
        
        JPanel pnlGaransi = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        pnlGaransi.setOpaque(false);
        pnlGaransi.add(createTitleLabel("Garansi Bawaan:")); pnlGaransi.add(lblGaransiBawaan);
        cardPerangkat.add(pnlGaransi, "span 2, gaptop 5");
        
        mainPanel.add(cardPerangkat);

        // DETAIL KERUSAKAN CARD
        mainPanel.add(createSectionTitle("DETAIL KERUSAKAN"));
        JPanel cardKerusakan = createCardPanel();
        cardKerusakan.setLayout(new MigLayout("wrap 2, fillx, insets 15", "[110!][fill, grow]", "[]15[]15[]"));
        
        txtKeluhan = createTextArea(); txtDiagnosa = createTextArea(); txtTindakan = createTextArea();
        
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
        
        mainPanel.add(cardKerusakan);

        // TRANSAKSI CARD
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

        // BUTTON CETAK
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
        ta.setEditable(false);
        ta.setFocusable(false); 
        ta.setLineWrap(true);      
        ta.setWrapStyleWord(false); // Diatur false agar kata panjang tanpa spasi (jjjj) tetap dipaksa turun
        ta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ta.setForeground(SIDEBAR_MAIN_COLOR);
        ta.setBackground(Color.WHITE); 
        ta.setBorder(null);
        return ta;
    }
    
    private void cetakPDF() {
        JOptionPane.showMessageDialog(this, "Membuka Print Dialog...", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadDataNota() {
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT s.tgl_masuk, s.keluhan_awal, s.status AS status_servis, " +
                         "p.nama_pelanggan, p.no_whatsapp, " +
                         "pr.jenis_perangkat, pr.merek, pr.tipe_model, " +
                         "pg.tgl_ambil, pg.total_bayar, pg.metode_bayar, " +
                         "n.masa_garansi, n.diagnosa, n.tindakan " + 
                         "FROM data_servis_lengkap s " +
                         "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                         "LEFT JOIN data_pengambilan pg ON s.id_servis = pg.id_servis " +
                         "LEFT JOIN tb_nota n ON n.id_nota = ? " +
                         "WHERE s.id_servis = ?";
            
            PreparedStatement ps = kon.prepareStatement(sql);
            ps.setString(1, idNota);
            ps.setInt(2, idServis);
            
            ResultSet rs = ps.executeQuery();
            NumberFormat formatRp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            if (rs.next()) {
                lblNama.setText(rs.getString("nama_pelanggan"));
                lblWa.setText(rs.getString("no_whatsapp"));
                lblAntrean.setText(String.format("%03d", idServis)); 
                
                lblTipe.setText(rs.getString("jenis_perangkat"));
                lblMerek.setText(rs.getString("merek") + " " + rs.getString("tipe_model"));
                lblGaransiBawaan.setText("Tidak Ada"); 
                
                txtKeluhan.setText(rs.getString("keluhan_awal"));
                txtDiagnosa.setText(rs.getString("diagnosa") != null ? rs.getString("diagnosa") : "Belum di-diagnosa");
                txtTindakan.setText(rs.getString("tindakan") != null ? rs.getString("tindakan") : "Belum ada tindakan tercatat");
                
                String status = rs.getString("status_servis");
                lblStatus.setText(status);
                if(status != null && status.equalsIgnoreCase("Selesai")) {
                    lblStatus.setForeground(FINISH_GREEN);
                } else {
                    lblStatus.setForeground(ACCENT_ORANGE);
                }

                double totalBayar = rs.getDouble("total_bayar");
                if (totalBayar > 0) {
                    lblTotalBiaya.setText(formatRp.format(totalBayar).replace(",00", ""));
                } else {
                    lblTotalBiaya.setText("Menunggu Kalkulasi");
                }
                
                lblMetode.setText(rs.getString("metode_bayar") != null ? rs.getString("metode_bayar") : "Belum Lunas");
                lblMasaGaransi.setText(rs.getString("masa_garansi") != null ? rs.getString("masa_garansi") : "Belum Ditentukan");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat detail: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}