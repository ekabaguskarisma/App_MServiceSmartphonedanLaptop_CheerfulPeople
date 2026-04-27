package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import com.mssl.main.FormManager; 
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import net.miginfocom.swing.MigLayout;

public class FormDashboard extends Form {

    // Palet Warna
    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);

    // Warna Aksen Cheerful People
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color SUCCESS_GREEN = new Color(46, 204, 113); 
    private final Color INFO_BLUE = new Color(52, 152, 219); 
    private final Color ERROR_RED = new Color(231, 76, 60);

    // Variabel Label untuk Data Database
    private JLabel lblTitleBulan, lblValPendapatanHari, lblSubPendapatanHari;
    private JLabel lblValPendapatanBulan, lblSubPendapatanBulan;
    private JLabel lblValLabaHari, lblSubLabaHari; 
    private JLabel lblValLabaBulan, lblSubLabaBulan; 

    private JLabel lblValServisMasuk, lblValSedangDikerjakan, lblValMenungguSp, lblValSiapDiambil, lblValServisSelesai;
    
    private JComboBox<String> cbBulan;
    private Map<String, String> mapBulan = new LinkedHashMap<>(); 
    
    private DefaultTableModel tableModel;
    private JTable table;

    public FormDashboard() {
        init();
    }

    private void init() {
        String role = FormManager.getUserRole();
        if (role == null) {
            role = "Pelanggan"; 
        }

        setBackground(APP_BG_COLOR);

        if (role.equalsIgnoreCase("Pelanggan")) {
            initPelangganDashboard(); 
        } else {
            initAdminDashboard();     
        }
    }
    
    // DESAIN KHUSUS PELANGGAN 
    private void initPelangganDashboard() {
        setLayout(new MigLayout("wrap, fillx, insets 20 30 20 30", "[fill]", "[][][]"));

        JPanel banner = new JPanel(new MigLayout("wrap, insets 35 40 35 40", "[fill]", "[]10[]"));
        banner.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:rgb(40, 45, 60)");

        JLabel lbWelcome = new JLabel("Selamat Datang di Cheerful People!");
        lbWelcome.putClientProperty(FlatClientProperties.STYLE, "font:bold +14; foreground:#ffffff");

        JLabel lbDesc = new JLabel("Pusat perbaikan Smartphone dan Laptop terpercaya dengan fitur monitoring Real-Time.");
        lbDesc.putClientProperty(FlatClientProperties.STYLE, "font:14; foreground:rgb(200,200,200)");

        banner.add(lbWelcome);
        banner.add(lbDesc);

        JLabel lbInfo = new JLabel("Keunggulan Layanan Kami");
        lbInfo.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:@accentColor");

        JPanel panelCards = new JPanel(new MigLayout("insets 0, gapx 20", "[fill, 33%][fill, 33%][fill, 33%]", "[fill]"));
        panelCards.setOpaque(false);

        panelCards.add(createCardPelanggan("Lacak Real-Time", "Pantau status servis perangkat Anda kapan saja dan di mana saja dari rumah.", "rgb(235, 245, 255)", "rgb(0, 102, 204)"));
        panelCards.add(createCardPelanggan("Teknisi Ahli", "Ditangani langsung oleh teknisi berpengalaman dan profesional di bidangnya.", "rgb(255, 245, 235)", "rgb(204, 102, 0)"));
        panelCards.add(createCardPelanggan("Transparan", "Rincian biaya, suku cadang, dan kerusakan dijelaskan secara detail dan jujur.", "rgb(240, 255, 240)", "rgb(0, 153, 51)"));

        add(banner, "gapbottom 30"); 
        add(lbInfo, "gapbottom 15");
        add(panelCards);
    }

    private JPanel createCardPelanggan(String title, String desc, String bgColor, String titleColor) {
        JPanel card = new JPanel(new MigLayout("wrap, insets 30 20 30 20", "[center]", "[]15[]"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor);

        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:" + titleColor);

        JLabel lbDesc = new JLabel("<html><div style='text-align: center; color: rgb(100,100,100); font-family: sans-serif; font-size: 11px;'>" + desc + "</div></html>");

        card.add(lbTitle);
        card.add(lbDesc);
        return card;
    }
    
    // 2. DESAIN KHUSUS ADMIN & TEKNISI
    private void initAdminDashboard() {
        setLayout(new MigLayout("wrap, fillx, insets 20 35 25 35", "[fill, grow]", "[][][][][grow, fill]"));

        lblValPendapatanHari = new JLabel("Rp 0");
        lblSubPendapatanHari = new JLabel("Memuat data...");
        lblValLabaHari = new JLabel("Rp 0");
        lblSubLabaHari = new JLabel("Keuntungan Bersih");
        
        lblTitleBulan = new JLabel("Finansial Bulan ..."); 
        lblValPendapatanBulan = new JLabel("Rp 0");
        lblSubPendapatanBulan = new JLabel("Memuat data...");
        lblValLabaBulan = new JLabel("Rp 0");
        lblSubLabaBulan = new JLabel("Keuntungan Bersih");
        
        lblValServisMasuk = new JLabel("0");
        lblValSedangDikerjakan = new JLabel("0");
        lblValMenungguSp = new JLabel("0");
        lblValSiapDiambil = new JLabel("0");
        lblValServisSelesai = new JLabel("0");

        // BAGIAN HEADER
        JPanel headerPanel = new JPanel(new MigLayout("insets 0, fillx", "[][][grow, right][][]", "[]"));
        headerPanel.setOpaque(false);
        
        JLabel lblLogo = new JLabel();
        try {
            ImageIcon iconLogo = new ImageIcon(getClass().getResource("/com/mssl/img/Logo.png"));
            Image scaledImage = iconLogo.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {}

        JPanel titlePanel = new JPanel(new MigLayout("insets 0, wrap, gapy 0", "[]", "[][]"));
        titlePanel.setOpaque(false);

        JLabel lblGreeting = new JLabel("Cheerful People");
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 28)); 
        lblGreeting.setForeground(SIDEBAR_MAIN_COLOR); 
        
        JLabel lblSubGreeting = new JLabel("Service Center");
        lblSubGreeting.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        lblSubGreeting.setForeground(TEXT_MUTED); 
        
        titlePanel.add(lblGreeting, "gapbottom -2");
        titlePanel.add(lblSubGreeting); 
        
        cbBulan = new JComboBox<>();
        cbBulan.putClientProperty(FlatClientProperties.STYLE, "arc:10; font:bold 13");
        loadBulanTersedia(); 
        
        cbBulan.addActionListener(e -> {
            if(cbBulan.getSelectedItem() != null) {
                lblTitleBulan.setText("Finansial " + cbBulan.getSelectedItem().toString());
                loadDashboardData();
            }
        });
        
        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold; margin:6,18,6,18; borderWidth:0; focusWidth:0");
        btnRefresh.addActionListener(e -> loadDashboardData());
        
        headerPanel.add(lblLogo, "gapright 10");
        headerPanel.add(titlePanel);
        headerPanel.add(new JLabel("Pilih Bulan:"), "gapright 5");
        headerPanel.add(cbBulan, "h 38!, gapright 15");
        headerPanel.add(btnRefresh, "h 38!");
        add(headerPanel, "gapbottom 20");

        // BAGIAN KARTU FINANSIAL
        JPanel pnlFinancial = new JPanel(new MigLayout("insets 0, gapx 15", "[fill, 25%][fill, 25%][fill, 25%][fill, 25%]", "[fill, 130!]"));
        pnlFinancial.setOpaque(false);
        
        pnlFinancial.add(createFinanceCard("Omzet Hari Ini", lblValPendapatanHari, lblSubPendapatanHari, ACCENT_ORANGE));
        pnlFinancial.add(createFinanceCard("Laba Hari Ini", lblValLabaHari, lblSubLabaHari, SUCCESS_GREEN));
        pnlFinancial.add(createFinanceCard("Omzet Bulanan", lblValPendapatanBulan, lblSubPendapatanBulan, SIDEBAR_MAIN_COLOR));
        pnlFinancial.add(createFinanceCard("Laba Bulanan", lblValLabaBulan, lblSubLabaBulan, INFO_BLUE));
        add(pnlFinancial, "gapbottom 25");

        // BAGIAN 3 KARTU STATUS OPERASIONAL SERVIS
        JLabel lblOpTitle = new JLabel("Status Operasional Servis");
        lblOpTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblOpTitle.setForeground(SIDEBAR_MAIN_COLOR);
        add(lblOpTitle, "gapbottom 10");

        JPanel pnlMetrics = new JPanel(new MigLayout("insets 0, gapx 15", "[fill, grow][fill, grow][fill, grow][fill, grow][fill, grow]", "[fill, 95!]"));
        pnlMetrics.setOpaque(false);
        pnlMetrics.add(createAdminMetricCard("Servis Masuk", lblValServisMasuk, INFO_BLUE));
        pnlMetrics.add(createAdminMetricCard("Dikerjakan", lblValSedangDikerjakan, ACCENT_ORANGE));
        pnlMetrics.add(createAdminMetricCard("Menunggu Part", lblValMenungguSp, ERROR_RED));
        pnlMetrics.add(createAdminMetricCard("Siap Diambil", lblValSiapDiambil, SUCCESS_GREEN));
        pnlMetrics.add(createAdminMetricCard("Servis Selesai", lblValServisSelesai, SIDEBAR_MAIN_COLOR)); 
        add(pnlMetrics, "gapbottom 25");

        // BAGIAN TABEL ANTREAN PRIORITAS
        JPanel pnlTable = new JPanel(new MigLayout("wrap, fill, insets 0", "[fill, grow]", "[][grow, fill]"));
        pnlTable.setOpaque(false);
        
        JLabel lblTableTitle = new JLabel("Antrean Servis Aktif (Belum Selesai)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTableTitle.setForeground(SIDEBAR_MAIN_COLOR);
        
        String[] columns = {"No. Nota", "Pelanggan", "Perangkat", "Keluhan", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        
        // TEMA DAN RENDERER TABEL
        table.setBackground(CARD_BG_COLOR);
        table.setForeground(new Color(60, 60, 60));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(60); // DITINGGIKAN AGAR WRAP TEXT BISA TURUN KE BAWAH
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 235));
        table.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 80%); selectionForeground:#000000; selectionArc:10");

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setOpaque(false);
        tableHeader.setBackground(new Color(235, 238, 245)); 
        tableHeader.setForeground(SIDEBAR_MAIN_COLOR);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        
        // Atur lebar kolom
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

        // Pasang Renderer Rata Kiri & Wrap Text
        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        WrapTextRenderer textWrapper = new WrapTextRenderer();

        table.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(textWrapper); 
        table.getColumnModel().getColumn(2).setCellRenderer(textWrapper);
        table.getColumnModel().getColumn(3).setCellRenderer(textWrapper);
        table.getColumnModel().getColumn(4).setCellRenderer(topLeftRenderer);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG_COLOR);
        scroll.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:#ffffff");
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");
        
        pnlTable.add(lblTableTitle, "gapbottom 10");
        pnlTable.add(scroll);
        add(pnlTable, "grow");

        if(cbBulan.getItemCount() > 0) {
            lblTitleBulan.setText("Finansial " + cbBulan.getSelectedItem().toString());
        }
        loadDashboardData();
    }

    private JPanel createFinanceCard(String title, JLabel val, JLabel sub, Color color) {
        JPanel card = new JPanel(new MigLayout("wrap, insets 15 20 15 20", "[fill]", "[]push[]0[]"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + String.format("#%06x", color.getRGB() & 0xFFFFFF));
        
        JLabel lbT = new JLabel(title); 
        lbT.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        lbT.setForeground(new Color(255,255,255,200));
        
        val.setFont(new Font("Segoe UI", Font.BOLD, 24)); 
        val.setForeground(Color.WHITE);
        
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 
        sub.setForeground(new Color(255,255,255,160));
        
        card.add(lbT); 
        card.add(val); 
        card.add(sub); 
        return card;
    }

    private JPanel createAdminMetricCard(String title, JLabel lblValue, Color accentColor) {
        JPanel card = new JPanel(new MigLayout("wrap, insets 15 15 15 15", "[fill]", "[]push[]"));
        card.setBackground(CARD_BG_COLOR);
        card.putClientProperty(FlatClientProperties.STYLE, "arc:15");
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor)); 

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_MUTED);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32)); 
        lblValue.setForeground(SIDEBAR_MAIN_COLOR); 

        card.add(lblTitle);
        card.add(lblValue);
        
        return card;
    }

    private void loadBulanTersedia() {
        cbBulan.removeAllItems();
        mapBulan.clear();
        
        LocalDate now = LocalDate.now();
        String currentMonthValue = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String currentMonthDisplay = formatNamaBulan(currentMonthValue);
        
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT DISTINCT DATE_FORMAT(tgl_ambil, '%Y-%m') as bulan_tahun FROM data_pengambilan ORDER BY bulan_tahun DESC";
            ResultSet rs = kon.createStatement().executeQuery(sql);
            
            boolean hasCurrentMonth = false;
            while (rs.next()) {
                String val = rs.getString("bulan_tahun");
                String display = formatNamaBulan(val);
                cbBulan.addItem(display);
                mapBulan.put(display, val);
                if (val.equals(currentMonthValue)) hasCurrentMonth = true;
            }
            
            if (!hasCurrentMonth) {
                cbBulan.insertItemAt(currentMonthDisplay, 0);
                mapBulan.put(currentMonthDisplay, currentMonthValue);
            }
            
            cbBulan.setSelectedIndex(0);
        } catch (Exception e) {
            System.err.println("Gagal memuat daftar bulan: " + e.getMessage());
        }
    }
    
    private String formatNamaBulan(String yyyyMM) {
        try {
            YearMonth ym = YearMonth.parse(yyyyMM);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"));
            return ym.format(formatter);
        } catch (Exception e) {
            return yyyyMM;
        }
    }

    private void loadDashboardData() {
        String selectedMonthDisplay = cbBulan.getSelectedItem() != null ? cbBulan.getSelectedItem().toString() : "";
        String selectedMonthVal = mapBulan.getOrDefault(selectedMonthDisplay, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));

        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            
            String pendapatanHari = "Rp 0"; String labaHari = "Rp 0"; String subHari = "Dari 0 transaksi hari ini";
            String pendapatanBulan = "Rp 0"; String labaBulan = "Rp 0"; String subBulan = "Total 0 transaksi di " + selectedMonthDisplay;
            int masuk = 0, proses = 0, sparepart = 0, siap = 0, selesai = 0;

            @Override
            protected Void doInBackground() throws Exception {
                Connection kon = DatabaseConnection.getKoneksi();

                // 1. HITUNG OMZET & LABA HARI INI (SUDAH DIPERBAIKI UNTUK STRUKTUR MANY-TO-MANY)
                String sqlHari = "SELECT IFNULL(SUM(dp.total_bayar), 0) AS omzet, " +
                                 "IFNULL(SUM(dp.biaya_jasa + COALESCE(sub.profit, 0)), 0) AS laba, " +
                                 "COUNT(dp.id_pengambilan) AS jml " +
                                 "FROM data_pengambilan dp " +
                                 "LEFT JOIN (SELECT det.id_pengambilan, SUM(det.qty * (sp.harga_jual - sp.harga_modal)) as profit " +
                                 "FROM detail_pengambilan_sparepart det JOIN data_sparepart sp ON det.id_sparepart = sp.id_sparepart " +
                                 "GROUP BY det.id_pengambilan) sub ON dp.id_pengambilan = sub.id_pengambilan " +
                                 "WHERE DATE(dp.tgl_ambil) = CURDATE()";
                ResultSet rsHari = kon.createStatement().executeQuery(sqlHari);
                if(rsHari.next()) {
                    pendapatanHari = formatRupiah.format(rsHari.getDouble("omzet"));
                    labaHari = formatRupiah.format(rsHari.getDouble("laba"));
                    subHari = "Dari " + rsHari.getInt("jml") + " transaksi hari ini";
                }

                // 2. HITUNG OMZET & LABA BULANAN (SUDAH DIPERBAIKI UNTUK STRUKTUR MANY-TO-MANY)
                String sqlBulan = "SELECT IFNULL(SUM(dp.total_bayar), 0) AS omzet, " +
                                  "IFNULL(SUM(dp.biaya_jasa + COALESCE(sub.profit, 0)), 0) AS laba, " +
                                  "COUNT(dp.id_pengambilan) AS jml " +
                                  "FROM data_pengambilan dp " +
                                  "LEFT JOIN (SELECT det.id_pengambilan, SUM(det.qty * (sp.harga_jual - sp.harga_modal)) as profit " +
                                  "FROM detail_pengambilan_sparepart det JOIN data_sparepart sp ON det.id_sparepart = sp.id_sparepart " +
                                  "GROUP BY det.id_pengambilan) sub ON dp.id_pengambilan = sub.id_pengambilan " +
                                  "WHERE DATE_FORMAT(dp.tgl_ambil, '%Y-%m') = ?";
                PreparedStatement psBulan = kon.prepareStatement(sqlBulan);
                psBulan.setString(1, selectedMonthVal);
                ResultSet rsBulan = psBulan.executeQuery();
                if(rsBulan.next()) {
                    pendapatanBulan = formatRupiah.format(rsBulan.getDouble("omzet"));
                    labaBulan = formatRupiah.format(rsBulan.getDouble("laba"));
                    subBulan = "Total " + rsBulan.getInt("jml") + " transaksi di " + selectedMonthDisplay;
                }

                // 3. METRIK OPERASIONAL (Penyesuaian nama status agar akurat)
                ResultSet rsMasuk = kon.createStatement().executeQuery("SELECT COUNT(id_servis) AS c FROM data_servis_lengkap WHERE status = 'Antrean'");
                if(rsMasuk.next()) masuk = rsMasuk.getInt("c");

                ResultSet rsProses = kon.createStatement().executeQuery("SELECT COUNT(id_servis) AS c FROM data_servis_lengkap WHERE status NOT IN ('Selesai', 'Diambil', 'Batal', 'Antrean', 'Menunggu Sparepart')");
                if(rsProses.next()) proses = rsProses.getInt("c");

                ResultSet rsSparepart = kon.createStatement().executeQuery("SELECT COUNT(id_servis) AS c FROM data_servis_lengkap WHERE status = 'Menunggu Sparepart'");
                if(rsSparepart.next()) sparepart = rsSparepart.getInt("c");

                ResultSet rsSiap = kon.createStatement().executeQuery("SELECT COUNT(id_servis) AS c FROM data_servis_lengkap WHERE status = 'Selesai'");
                if(rsSiap.next()) siap = rsSiap.getInt("c");
                
                ResultSet rsSelesai = kon.createStatement().executeQuery("SELECT COUNT(id_servis) AS c FROM data_servis_lengkap WHERE status = 'Diambil'");
                if(rsSelesai.next()) selesai = rsSelesai.getInt("c");

                // 4. TABEL ANTREAN PRIORITAS
                tableModel.setRowCount(0); 
                String sqlTable = "SELECT s.id_servis, p.nama_pelanggan, " +
                        "CONCAT(pr.merek, ' ', pr.tipe_model) AS perangkat, " + 
                        "s.keluhan_awal, s.status " +
                        "FROM data_servis_lengkap s " +
                        "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                        "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                        "WHERE s.status NOT IN ('Diambil', 'Batal') " +
                        "ORDER BY s.tgl_masuk DESC";
                ResultSet rsTable = kon.createStatement().executeQuery(sqlTable);
                while (rsTable.next()) {
                    publish(new Object[]{
                        String.format("N%05d", rsTable.getInt("id_servis")),
                        rsTable.getString("nama_pelanggan"),
                        rsTable.getString("perangkat"), // TYPO SUDAH DIPERBAIKI DISINI
                        rsTable.getString("keluhan_awal"),
                        rsTable.getString("status")
                    });
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Object[]> chunks) {
                for (Object[] rowData : chunks) {
                    tableModel.addRow(rowData);
                }
            }

            @Override
            protected void done() {
                try {
                    get(); 
                    lblValPendapatanHari.setText(pendapatanHari.replace(",00", ""));
                    lblSubPendapatanHari.setText(subHari);
                    
                    lblValLabaHari.setText(labaHari.replace(",00", ""));
                    
                    lblValPendapatanBulan.setText(pendapatanBulan.replace(",00", ""));
                    lblSubPendapatanBulan.setText(subBulan);
                    
                    lblValLabaBulan.setText(labaBulan.replace(",00", ""));
                    
                    lblValServisMasuk.setText(String.valueOf(masuk));
                    lblValSedangDikerjakan.setText(String.valueOf(proses));
                    lblValMenungguSp.setText(String.valueOf(sparepart));
                    lblValSiapDiambil.setText(String.valueOf(siap));
                    lblValServisSelesai.setText(String.valueOf(selesai));
                    
                } catch (Exception e) {
                    System.err.println("Gagal memuat dashboard: " + e.getMessage());
                    e.printStackTrace(); 
                }
            }
        };
        worker.execute();
    }
    
    // KELAS RENDERER
    class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setMargin(new java.awt.Insets(10, 10, 10, 10)); // Padding Atas, Kiri, Bawah, Kanan
            setOpaque(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }
    }
}