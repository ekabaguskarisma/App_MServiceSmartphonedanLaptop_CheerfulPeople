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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;

public class FormRiwayatServis extends Form {

    private JTextField txtSearch;
    private JComboBox<String> cbFilterBayar;
    private JComboBox<String> cbFilterGaransi;
    private JTable tableRiwayat;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JButton btnRefresh;

    private final Color APP_BG_COLOR = new Color(245, 245, 248);
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0);
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245);

    public FormRiwayatServis() {
        init();
        loadDataTable(); 
    }

    private void init() {
        setLayout(new MigLayout("wrap, fill, insets 25 30 25 30, gap 20", "[fill,grow]", "[][][fill,grow][]"));
        setBackground(APP_BG_COLOR);

        add(createHeaderPanel());
        add(createFilterPanel());
        add(createTablePanel(), "grow, w 0:100%, h 0:100%");
        add(createFooterPanel());
    }

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right]", "[]"));
        p.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan Riwayat Servis");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);

        JLabel lblDesc = new JLabel("Rekapitulasi data perangkat yang telah selesai diperbaiki atau dibatalkan.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(130, 135, 150));

        JPanel pnlLeft = new JPanel(new MigLayout("wrap, insets 0, gapy 0", "[]", "[][]"));
        pnlLeft.setOpaque(false);
        pnlLeft.add(lblTitle, "gapbottom 2");
        pnlLeft.add(lblDesc);

        p.add(pnlLeft);
        return p;
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new MigLayout("insets 15 25 15 25, fillx", "[350!, fill]15[150!, fill]15[150!, fill][grow, right]", "[]"));
        p.setBackground(CARD_BG_COLOR);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20"); 

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Nomor Nota atau Pelanggan...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        try {
            txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16));
        } catch (Exception e) {}

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        cbFilterBayar = new JComboBox<>(new String[]{"Semua Pembayaran", "Lunas", "Belum Lunas"});
        cbFilterBayar.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        cbFilterBayar.addActionListener(e -> applyFilters());

        cbFilterGaransi = new JComboBox<>(new String[]{"Semua Garansi", "Bergaransi", "Tidak Garansi"});
        cbFilterGaransi.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        cbFilterGaransi.addActionListener(e -> applyFilters());

        btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterBayar.setSelectedIndex(0);
            cbFilterGaransi.setSelectedIndex(0);
            loadDataTable(); 
        });

        p.add(txtSearch, "h 38!");
        p.add(cbFilterBayar, "h 38!");
        p.add(cbFilterGaransi, "h 38!"); 
        p.add(btnRefresh, "w 130!, h 38!"); 

        return p;
    }

    private JPanel createTablePanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[fill, grow]", "[fill, grow]"));
        p.setBackground(CARD_BG_COLOR);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        String[] columns = {"ID Nota", "Tgl Selesai", "Pelanggan", "Perangkat", "Status Bayar", "Garansi", "Total Biaya"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableRiwayat = new JTable(tableModel);
        tableRiwayat.setBackground(Color.WHITE);
        tableRiwayat.setForeground(new Color(60, 60, 60));
        tableRiwayat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableRiwayat.setRowHeight(60); 
        tableRiwayat.setShowGrid(false);
        tableRiwayat.setShowHorizontalLines(true);
        tableRiwayat.setGridColor(new Color(230, 230, 235));
        tableRiwayat.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 85%); selectionForeground:#000000; selectionArc:10");

        JTableHeader header = tableRiwayat.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(false);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(SIDEBAR_MAIN_COLOR);
        
        tableRiwayat.getColumnModel().getColumn(0).setPreferredWidth(90);  
        tableRiwayat.getColumnModel().getColumn(1).setPreferredWidth(110); 
        tableRiwayat.getColumnModel().getColumn(2).setPreferredWidth(200); 
        tableRiwayat.getColumnModel().getColumn(3).setPreferredWidth(180); 
        tableRiwayat.getColumnModel().getColumn(4).setPreferredWidth(110); 
        tableRiwayat.getColumnModel().getColumn(5).setPreferredWidth(110); 
        tableRiwayat.getColumnModel().getColumn(6).setPreferredWidth(140); 

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        tableRiwayat.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        tableRiwayat.getColumnModel().getColumn(1).setCellRenderer(topLeftRenderer);
        tableRiwayat.getColumnModel().getColumn(4).setCellRenderer(topLeftRenderer);
        tableRiwayat.getColumnModel().getColumn(5).setCellRenderer(topLeftRenderer);
        tableRiwayat.getColumnModel().getColumn(6).setCellRenderer(topLeftRenderer);

        WrapTextRenderer textWrapper = new WrapTextRenderer();
        tableRiwayat.getColumnModel().getColumn(2).setCellRenderer(textWrapper);
        tableRiwayat.getColumnModel().getColumn(3).setCellRenderer(textWrapper);

        ((DefaultTableCellRenderer) tableRiwayat.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        tableRiwayat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bukaDetailNota();
                }
            }
        });
        
        rowSorter = new TableRowSorter<>(tableModel);
        tableRiwayat.setRowSorter(rowSorter);

        JScrollPane scroll = new JScrollPane(tableRiwayat);
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

        JButton btnDetail = new JButton("Lihat Detail Nota");
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDetail.setBackground(ACCENT_ORANGE);
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:10,25,10,25");

        JButton btnExport = new JButton("Ekspor Data");
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExport.setBackground(new Color(39, 174, 96)); 
        btnExport.setForeground(Color.WHITE);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:10,25,10,25");
        
        try {
            FlatSVGIcon exportIcon = new FlatSVGIcon("com/mssl/icon/chart.svg", 18, 18);
            exportIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnExport.setIcon(exportIcon);
        } catch(Exception e){}

        btnDetail.addActionListener(e -> bukaDetailNota());
        btnExport.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fitur Ekspor (Excel/PDF) sedang dalam pengembangan.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        p.add(btnDetail, "height 45!");
        p.add(btnExport, "height 45!");
        return p;
    }

    private void bukaDetailNota() {
        try {
            int row = tableRiwayat.getSelectedRow();
            if(row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih salah satu baris di tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = tableRiwayat.convertRowIndexToModel(row);
            String idNota = tableModel.getValueAt(modelRow, 0).toString();
            
            Window window = SwingUtilities.getWindowAncestor(this);
            JFrame mainFrame = null;
            if (window instanceof JFrame) {
                mainFrame = (JFrame) window;
            }
            
            DialogDetailNota dialogDetail = new DialogDetailNota(mainFrame, idNota);
            dialogDetail.setVisible(true);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal membuka detail nota: " + ex.getMessage(), "Error UI", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilters() {
        List<RowFilter<Object,Object>> filters = new ArrayList<>();

        String keyword = txtSearch.getText().trim();
        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 0, 2));
        }

        String bayar = cbFilterBayar.getSelectedItem() != null ? cbFilterBayar.getSelectedItem().toString() : "Semua Pembayaran";
        if (!bayar.equals("Semua Pembayaran")) {
            filters.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(bayar) + "$", 4));
        }

        String garansi = cbFilterGaransi.getSelectedItem() != null ? cbFilterGaransi.getSelectedItem().toString() : "Semua Garansi";
        if (!garansi.equals("Semua Garansi")) {
            if (garansi.equals("Tidak Garansi")) {
                filters.add(RowFilter.regexFilter("(?i)^Tidak Garansi$", 5));
            } else if (garansi.equals("Bergaransi")) {
                filters.add(RowFilter.notFilter(RowFilter.regexFilter("(?i)^Tidak Garansi$", 5)));
            }
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void loadDataTable() {
        tableModel.setRowCount(0); 

        javax.swing.SwingWorker<Void, Object[]> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // UPDATE: Mengambil data gabungan (Termasuk yang Batal)
                String sql = "SELECT s.id_servis, n.tanggal_selesai, p.nama_pelanggan, " +
                             "pr.merek, pr.tipe_model, s.status AS status_servis, " +
                             "n.status_pembayaran, n.masa_garansi, n.total_biaya " +
                             "FROM data_servis_lengkap s " +
                             "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                             "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                             "LEFT JOIN tb_nota n ON n.id_servis = s.id_servis " +
                             "WHERE s.status IN ('Selesai', 'Batal') " +
                             "ORDER BY s.id_servis DESC";

                java.sql.Connection conn = DatabaseConnection.getKoneksi();
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    try (ResultSet res = ps.executeQuery()) {
                        while (res.next()) {
                            String id = "N" + String.format("%05d", res.getInt("id_servis"));
                            String statusServis = res.getString("status_servis");
                            
                            // TANGGAL SELESAI
                            String tgl = res.getString("tanggal_selesai");
                            if (statusServis.equalsIgnoreCase("Batal")) {
                                tgl = "Dibatalkan";
                            } else if(tgl == null || tgl.isEmpty()) {
                                tgl = "Belum Selesai";
                            }
                            
                            String nama = res.getString("nama_pelanggan");
                            String hp = res.getString("merek") + " " + res.getString("tipe_model");
                            
                            // STATUS PEMBAYARAN
                            String statBayar = res.getString("status_pembayaran");
                            if(statusServis.equalsIgnoreCase("Batal")) {
                                statBayar = "Dibatalkan";
                            } else if(statBayar == null || statBayar.trim().isEmpty()) {
                                statBayar = "Belum Lunas";
                            }
                            
                            // MASA GARANSI
                            String garansi = res.getString("masa_garansi");
                            if(statusServis.equalsIgnoreCase("Batal")) {
                                garansi = "-";
                            } else if(garansi == null || garansi.trim().isEmpty() || garansi.equals("-")) {
                                garansi = "Tidak Garansi";
                            }
                            
                            // TOTAL BIAYA
                            double biayaDb = res.getDouble("total_biaya");
                            String biaya = statusServis.equalsIgnoreCase("Batal") ? "Rp 0" : formatRupiah.format(biayaDb).replace(",00", "");

                            publish(new Object[]{id, tgl, nama, hp, statBayar, garansi, biaya});
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
                    applyFilters(); 
                } catch (Exception e) {
                    System.err.println("Error Load Data Tabel Riwayat: " + e.getMessage());
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
}