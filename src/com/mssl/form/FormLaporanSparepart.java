package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;

public class FormLaporanSparepart extends Form {

    private JTextField txtSearch;
    private JComboBox<String> cbKategori;
    private JComboBox<String> cbStatusStok;
    private JTable tableSparepart;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel lblTotalItem, lblStokMenipis, lblTotalAset;
    private boolean isRefreshing = false;
    private final Color APP_BG_COLOR = new Color(245, 245, 248);
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color FINISH_GREEN = new Color(39, 174, 96);
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245);

    public FormLaporanSparepart() {
        init();
        loadDataSparepart(); 
    }

    private void init() {
        setLayout(new MigLayout("wrap, fill, insets 25 30 25 30, gap 20", "[fill,grow]", "[][][][fill,grow][]"));
        setBackground(APP_BG_COLOR);

        add(createHeaderPanel());
        add(createSummaryPanel());
        add(createFilterPanel());
        add(createTablePanel(), "grow, w 0:100%, h 0:100%");
        add(createFooterPanel());
    }

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right]", "[]"));
        p.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan Stok Sparepart");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);

        JLabel lblDesc = new JLabel("Pantau ketersediaan suku cadang dan nilai aset inventaris bengkel secara Real-Time.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(130, 135, 150));

        JPanel pnlLeft = new JPanel(new MigLayout("wrap, insets 0, gapy 0", "[]", "[][]"));
        pnlLeft.setOpaque(false);
        pnlLeft.add(lblTitle, "gapbottom 2");
        pnlLeft.add(lblDesc);

        p.add(pnlLeft);
        return p;
    }

    private JPanel createSummaryPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, gapx 20", "[fill, 33%][fill, 33%][fill, 33%]", "[fill]"));
        p.setOpaque(false);

        lblTotalItem = new JLabel("0 Jenis");
        lblStokMenipis = new JLabel("0 Item");
        lblTotalAset = new JLabel("Rp 0");

        p.add(createCard("Total Jenis Sparepart", lblTotalItem, new Color(235, 245, 255), new Color(0, 102, 204)));
        p.add(createCard("Peringatan Stok Menipis (<5)", lblStokMenipis, new Color(255, 235, 235), ERROR_RED));
        p.add(createCard("Estimasi Nilai Aset (Modal)", lblTotalAset, new Color(240, 255, 240), FINISH_GREEN));

        return p;
    }

    private JPanel createCard(String title, JLabel lblNilai, Color bgColor, Color titleColor) {
        JPanel card = new JPanel(new MigLayout("wrap, insets 20", "[fill]", "[]10[]"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + String.format("#%06x", bgColor.getRGB() & 0xFFFFFF));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(titleColor);

        lblNilai.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblNilai.setForeground(SIDEBAR_MAIN_COLOR);

        card.add(lblTitle);
        card.add(lblNilai);
        return card;
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15 25 15 25, fillx", "[350!, fill]15[150!, fill]15[150!, fill][grow, right]", "[]"));
        p.setBackground(CARD_BG_COLOR);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Kode atau Nama Sparepart...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        try { 
            txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16)); 
        } catch (Exception e) {}
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { if (!isRefreshing) loadDataSparepart(); }
            public void removeUpdate(DocumentEvent e) { if (!isRefreshing) loadDataSparepart(); }
            public void changedUpdate(DocumentEvent e) { if (!isRefreshing) loadDataSparepart(); }
        });

        cbKategori = new JComboBox<>(new String[]{"Semua Kategori", "LCD / Layar", "Baterai", "IC & Mesin", "Konektor & Fleksibel", "Lainnya"});
        cbKategori.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        cbKategori.addActionListener(e -> {
            if (!isRefreshing) loadDataSparepart();
        });

        cbStatusStok = new JComboBox<>(new String[]{"Semua Stok", "Tersedia (>5)", "Menipis (1-5)", "Habis (0)"});
        cbStatusStok.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        cbStatusStok.addActionListener(e -> {
            if (!isRefreshing) loadDataSparepart();
        });

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            isRefreshing = true;
            txtSearch.setText("");
            cbKategori.setSelectedIndex(0);
            cbStatusStok.setSelectedIndex(0);
            isRefreshing = false;
            loadDataSparepart();
        });

        p.add(txtSearch, "h 38!");
        p.add(cbKategori, "h 38!");
        p.add(cbStatusStok, "h 38!");
        p.add(btnRefresh, "w 130!, h 38!"); 

        return p;
    }

    private JPanel createTablePanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[fill, grow]", "[fill, grow]"));
        p.setBackground(CARD_BG_COLOR);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        String[] columns = {"Kode", "Nama Sparepart", "Kategori", "Stok", "Harga Modal", "Harga Jual"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableSparepart = new JTable(tableModel);
        
        tableSparepart.setBackground(Color.WHITE);
        tableSparepart.setForeground(new Color(60, 60, 60));
        tableSparepart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableSparepart.setRowHeight(60); 
        tableSparepart.setShowGrid(false);
        tableSparepart.setShowHorizontalLines(true);
        tableSparepart.setGridColor(new Color(230, 230, 235));
        tableSparepart.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 85%); selectionForeground:#000000; selectionArc:10");

        JTableHeader header = tableSparepart.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(false);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(SIDEBAR_MAIN_COLOR);

        tableSparepart.getColumnModel().getColumn(0).setPreferredWidth(100); 
        tableSparepart.getColumnModel().getColumn(1).setPreferredWidth(250); 
        tableSparepart.getColumnModel().getColumn(2).setPreferredWidth(150); 
        tableSparepart.getColumnModel().getColumn(3).setPreferredWidth(80);  
        tableSparepart.getColumnModel().getColumn(4).setPreferredWidth(150); 
        tableSparepart.getColumnModel().getColumn(5).setPreferredWidth(150); 

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableSparepart.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(2).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(4).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(5).setCellRenderer(topLeftRenderer);

        WrapTextRenderer textWrapper = new WrapTextRenderer();
        tableSparepart.getColumnModel().getColumn(1).setCellRenderer(textWrapper);

        StockRenderer stockRenderer = new StockRenderer();
        tableSparepart.getColumnModel().getColumn(3).setCellRenderer(stockRenderer);

        ((DefaultTableCellRenderer) tableSparepart.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        rowSorter = new TableRowSorter<>(tableModel);
        tableSparepart.setRowSorter(rowSorter);

        JScrollPane scroll = new JScrollPane(tableSparepart);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setBackground(CARD_BG_COLOR);
        
        scroll.getVerticalScrollBar().setUnitIncrement(15);
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");

        p.add(scroll, "grow, w 0:100%, h 0:100%");
        return p;
    }

    private JPanel createFooterPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[][grow, right]", "[]"));
        p.setOpaque(false);

        JButton btnEkspor = new JButton("Ekspor Data");
        btnEkspor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEkspor.setBackground(FINISH_GREEN);
        btnEkspor.setForeground(Color.WHITE);
        btnEkspor.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEkspor.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:10,30,10,30");
        
        try {
            FlatSVGIcon exportIcon = new FlatSVGIcon("com/mssl/icon/chart.svg", 18, 18);
            exportIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnEkspor.setIcon(exportIcon);
        } catch(Exception e){}

        btnEkspor.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fitur Ekspor Laporan (Excel/PDF) sedang dalam pengembangan.");
        });

        p.add(btnEkspor, "height 45!");
        return p;
    }

    private void loadDataSparepart() {
        String keyword = txtSearch.getText().trim();
        String kategori = cbKategori.getSelectedItem() != null ? cbKategori.getSelectedItem().toString() : "Semua Kategori";
        String statusStok = cbStatusStok.getSelectedItem() != null ? cbStatusStok.getSelectedItem().toString() : "Semua Stok";

        tableModel.setRowCount(0); 

        javax.swing.SwingWorker<Void, Object[]> worker = new javax.swing.SwingWorker<>() {
            
            int totalJenis = 0;
            int totalMenipis = 0;
            double totalAset = 0;

            @Override
            protected Void doInBackground() throws Exception {
                StringBuilder sql = new StringBuilder("SELECT id_sparepart, nama_sparepart, kategori, stok, harga_modal, harga_jual FROM data_sparepart WHERE 1=1");
                
                if (!keyword.isEmpty()) {
                    sql.append(" AND (id_sparepart LIKE ? OR nama_sparepart LIKE ?)");
                }
                if (!kategori.equals("Semua Kategori")) {
                    sql.append(" AND kategori = ?");
                }
                if (statusStok.equals("Tersedia (>5)")) {
                    sql.append(" AND stok > 5");
                } else if (statusStok.equals("Menipis (1-5)")) {
                    sql.append(" AND stok > 0 AND stok <= 5");
                } else if (statusStok.equals("Habis (0)")) {
                    sql.append(" AND stok = 0");
                }
                
                sql.append(" ORDER BY nama_sparepart ASC"); 

                java.sql.Connection conn = DatabaseConnection.getKoneksi();
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    int paramIndex = 1;
                    
                    if (!keyword.isEmpty()) {
                        ps.setString(paramIndex++, "%" + keyword + "%");
                        ps.setString(paramIndex++, "%" + keyword + "%");
                    }
                    if (!kategori.equals("Semua Kategori")) {
                        ps.setString(paramIndex++, kategori);
                    }
                    
                    try (ResultSet res = ps.executeQuery()) {
                        while (res.next()) {
                            String kode = "SPR-" + res.getInt("id_sparepart");
                            String nama = res.getString("nama_sparepart");
                            String kat = res.getString("kategori");
                            int stok = res.getInt("stok");
                            double modal = res.getDouble("harga_modal");
                            double jual = res.getDouble("harga_jual");
                            
                            String strModal = formatRupiah.format(modal).replace(",00", "");
                            String strJual = formatRupiah.format(jual).replace(",00", "");
                            String strStok = stok + " Pcs";

                            totalJenis++;
                            if (stok <= 5) totalMenipis++;
                            totalAset += (stok * modal); 

                            publish(new Object[]{kode, nama, kat, strStok, strModal, strJual});
                        }
                    }
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
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    lblTotalItem.setText(totalJenis + " Jenis");
                    lblStokMenipis.setText(totalMenipis + " Item");
                    lblTotalAset.setText(formatRupiah.format(totalAset).replace(",00", ""));
                    
                } catch (Exception e) {
                    System.err.println("Error Load Data Sparepart: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setMargin(new java.awt.Insets(10, 10, 10, 10)); 
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

    class StockRenderer extends DefaultTableCellRenderer {
        public StockRenderer() {
            setVerticalAlignment(SwingConstants.TOP);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
                int stok = Integer.parseInt(value.toString().replaceAll("[^0-9]", ""));
                
                if (!isSelected) {
                    if (stok == 0) {
                        c.setForeground(new Color(180, 180, 180)); 
                    } else if (stok <= 5) {
                        c.setForeground(ERROR_RED); 
                    } else {
                        c.setForeground(SIDEBAR_MAIN_COLOR); 
                    }
                }
            } catch(Exception e){}
            return c;
        }
    }
}