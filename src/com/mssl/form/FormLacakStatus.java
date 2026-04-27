package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.main.Form;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;

public class FormLacakStatus extends Form {

    private JTextField txtSearch;
    private JComboBox<String> cbFilterTahapan;
    private JTable tableMonitoring;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JButton btnRefresh, btnUpdate;
    
    private boolean isRefreshing = false;

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0);
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245);

    public FormLacakStatus() {
        init();
        loadDataMonitoring(); 
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

        JLabel lblTitle = new JLabel("Monitoring Servis Aktif");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR);

        JLabel lblDesc = new JLabel("Pantau progres perangkat yang sedang berada di bengkel dan update status pengerjaan.");
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
        JPanel p = new JPanel(new MigLayout("insets 15 25 15 25, fillx", "[350!, fill]15[200!, fill][grow, right]", "[]"));
        p.setBackground(CARD_BG_COLOR);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Nomor Nota atau Pelanggan...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        try { txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16)); } catch (Exception e) {}

        // --- TAMBAHAN FITUR ENTER PADA PENCARIAN LACAK STATUS ---
        txtSearch.addActionListener(e -> {
            if (tableMonitoring.getRowCount() > 0) {
                tableMonitoring.setRowSelectionInterval(0, 0); // Sorot hasil
                bukaFormUpdate(); // Langsung buka pop-up edit progres
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });

        cbFilterTahapan = new JComboBox<>(new String[]{
            "Semua Tahapan", 
            "Antrean / Pengecekan", 
            "Sedang Dikerjakan", 
            "Menunggu Sparepart", 
            "Selesai (Siap Diambil)"
        });
        cbFilterTahapan.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        cbFilterTahapan.addActionListener(e -> {
            if (!isRefreshing) loadDataMonitoring();
        });

        btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR); 
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRefresh.addActionListener(e -> {
            isRefreshing = true; 
            txtSearch.setText("");
            cbFilterTahapan.setSelectedIndex(0);
            isRefreshing = false; 
            loadDataMonitoring(); 
        });

        p.add(txtSearch, "h 38!");
        p.add(cbFilterTahapan, "h 38!");
        p.add(btnRefresh, "w 130!, h 38!"); 

        return p;
    }

    private JPanel createTablePanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, fill", "[fill, grow]", "[fill, grow]"));
        p.setBackground(CARD_BG_COLOR);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        // PERBAIKAN: Menambahkan kolom No. Urut Visual
        String[] columns = {"No.", "ID Nota", "Pelanggan", "Perangkat", "Status Terakhir", "Progres"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableMonitoring = new JTable(tableModel);
        tableMonitoring.setBackground(Color.WHITE);
        tableMonitoring.setForeground(new Color(60, 60, 60));
        tableMonitoring.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableMonitoring.setRowHeight(60);
        tableMonitoring.setShowGrid(false);
        tableMonitoring.setShowHorizontalLines(true);
        tableMonitoring.setGridColor(new Color(230, 230, 235));
        tableMonitoring.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 85%); selectionForeground:#000000; selectionArc:10");

        JTableHeader header = tableMonitoring.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(false);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(SIDEBAR_MAIN_COLOR);
        
        tableMonitoring.getColumnModel().getColumn(0).setPreferredWidth(50); // No.
        tableMonitoring.getColumnModel().getColumn(1).setPreferredWidth(100); 
        tableMonitoring.getColumnModel().getColumn(2).setPreferredWidth(200); 
        tableMonitoring.getColumnModel().getColumn(3).setPreferredWidth(180); 
        tableMonitoring.getColumnModel().getColumn(4).setPreferredWidth(250); 
        tableMonitoring.getColumnModel().getColumn(5).setPreferredWidth(100); 

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        tableMonitoring.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        tableMonitoring.getColumnModel().getColumn(1).setCellRenderer(topLeftRenderer);
        tableMonitoring.getColumnModel().getColumn(5).setCellRenderer(topLeftRenderer);

        WrapTextRenderer textWrapper = new WrapTextRenderer();
        tableMonitoring.getColumnModel().getColumn(2).setCellRenderer(textWrapper);
        tableMonitoring.getColumnModel().getColumn(3).setCellRenderer(textWrapper);
        tableMonitoring.getColumnModel().getColumn(4).setCellRenderer(textWrapper);

        ((DefaultTableCellRenderer) tableMonitoring.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        tableMonitoring.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) bukaFormUpdate();
            }
        });
        
        rowSorter = new TableRowSorter<>(tableModel);
        tableMonitoring.setRowSorter(rowSorter);

        JScrollPane scroll = new JScrollPane(tableMonitoring);
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

        btnUpdate = new JButton("Update Progres & Log Servis");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUpdate.setBackground(ACCENT_ORANGE);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:10,30,10,30");
        btnUpdate.addActionListener(e -> bukaFormUpdate());

        p.add(btnUpdate, "height 45!");
        return p;
    }

    private void bukaFormUpdate() {
        int row = tableMonitoring.getSelectedRow();
        if(row == -1) {
            tampilkanNotif("Peringatan", "Pilih salah satu nota di tabel terlebih dahulu!", "warning");
            return;
        }
        int modelRow = tableMonitoring.convertRowIndexToModel(row);
        // Karena ada penambahan kolom "No", maka index ID bergeser dari 0 ke 1
        String idNota = tableModel.getValueAt(modelRow, 1).toString();
        String pelanggan = tableModel.getValueAt(modelRow, 2).toString();
        
        Window window = SwingUtilities.getWindowAncestor(this);
        JFrame mainFrame = null;
        if (window instanceof JFrame) mainFrame = (JFrame) window;

        DialogUpdateProgres dialog = new DialogUpdateProgres(mainFrame, idNota, pelanggan);
        dialog.setVisible(true);
        loadDataMonitoring(); 
    }

    private void loadDataMonitoring() {
        String filterTahapan = cbFilterTahapan.getSelectedItem() != null ? cbFilterTahapan.getSelectedItem().toString() : "Semua Tahapan";
        tableModel.setRowCount(0); 

        javax.swing.SwingWorker<Void, Object[]> worker = new javax.swing.SwingWorker<>() {
            private String errorMsg = "";

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    StringBuilder sql = new StringBuilder(
                        "SELECT s.id_servis, pel.nama_pelanggan, per.merek, per.tipe_model, p.status_servis, p.persentase " +
                        "FROM data_servis_lengkap s " +
                        "JOIN data_pelanggan pel ON s.id_pelanggan = pel.id_pelanggan " +
                        "JOIN data_perangkat per ON s.id_perangkat = per.id_perangkat " +
                        "LEFT JOIN tb_progres_servis p ON p.id_nota = CONCAT('N', LPAD(s.id_servis, 5, '0')) " +
                        "WHERE s.status NOT IN ('Selesai', 'Batal')" 
                    );
                    
                    if (!filterTahapan.equals("Semua Tahapan")) {
                        if (filterTahapan.equals("Antrean / Pengecekan")) {
                            sql.append(" AND (p.persentase <= 20 OR p.persentase IS NULL)");
                        } else if (filterTahapan.equals("Sedang Dikerjakan")) {
                            sql.append(" AND p.persentase > 20 AND p.persentase < 90");
                        } else if (filterTahapan.equals("Selesai (Siap Diambil)")) {
                            sql.append(" AND p.persentase >= 100");
                        }
                    }
                    
                    sql.append(" ORDER BY s.id_servis DESC"); 

                    java.sql.Connection conn = com.mssl.koneksi.DatabaseConnection.getKoneksi();

                    try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                        try (ResultSet res = ps.executeQuery()) {
                            int no = 1;
                            while (res.next()) {
                                String id = "N" + String.format("%05d", res.getInt("id_servis"));
                                String nama = res.getString("nama_pelanggan");
                                String hp = res.getString("merek") + " " + res.getString("tipe_model");
                                String status = res.getString("status_servis");
                                
                                if(status == null) status = "Belum Ada Status";
                                
                                int persen = res.getInt("persentase");
                                String strPersen = persen + "%";

                                publish(new Object[]{no++, id, nama, hp, status, strPersen});
                            }
                        }
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    throw e;
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
                    liveSearch(); 
                } catch (Exception e) {
                    tampilkanNotif("Database Error", "Gagal memuat dari Database: " + errorMsg, "error");
                }
            }
        };
        worker.execute();
    }

    private void liveSearch() {
        String keyword = txtSearch.getText();
        if (keyword.trim().isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    // =========================================================
    // FUNGSI NOTIFIKASI TIKET CUSTOM (PREMIUM STYLE)
    // =========================================================
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

    class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() {
            setLineWrap(true); setWrapStyleWord(true); setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setMargin(new java.awt.Insets(10, 10, 10, 10)); setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            if (isSelected) { setBackground(table.getSelectionBackground()); setForeground(table.getSelectionForeground()); } 
            else { setBackground(table.getBackground()); setForeground(table.getForeground()); }
            return this;
        }
    }
}