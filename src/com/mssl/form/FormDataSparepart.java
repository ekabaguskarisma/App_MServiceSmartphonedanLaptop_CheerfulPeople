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

public class FormDataSparepart extends Form {

    private JTextField txtSearch;
    private JTextField txtNama, txtStok, txtModal, txtJual;
    private JComboBox<String> cbKategori;
    private JLabel lblTitleForm;
    private JButton btnSimpan, btnHapus, btnBersih;
    
    private JTable tableSparepart;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private String selectedId = ""; 

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color SUCCESS_GREEN = new Color(39, 174, 96);
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245); 

    public FormDataSparepart() {
        init();
        loadDataDariDatabase(); 
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 25 30 25 30, gap 25", "[350!][fill,grow,0]", "[fill,grow,0]"));
        setBackground(APP_BG_COLOR);
        
        // 1. FORM INPUT
        JPanel panelForm = new JPanel(new MigLayout("wrap, fillx, insets 25", "[fill]", "[]20[]5[]10[]5[]10[]5[]10[]5[]10[]5[]20[]"));
        panelForm.setBackground(CARD_BG_COLOR);
        panelForm.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        lblTitleForm = new JLabel("Input Sparepart Baru");
        lblTitleForm.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitleForm.setForeground(ACCENT_ORANGE);

        txtNama = new JTextField();
        txtNama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Contoh: LCD Samsung A51");
        txtNama.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtNama.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");

        String[] kategoriList = {"LCD / Layar", "Baterai", "Konektor & Fleksibel", "IC & Mesin", "Kamera", "Housing/Backdoor", "Lainnya"};
        cbKategori = new JComboBox<>(kategoriList);
        cbKategori.putClientProperty(FlatClientProperties.STYLE, "arc:10"); 

        txtStok = new JTextField();
        txtStok.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Jumlah Stok");
        txtStok.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtStok.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        
        txtModal = new JTextField();
        txtModal.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Harga Beli/Modal (Rp)");
        txtModal.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtModal.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        
        txtJual = new JTextField();
        txtJual.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Harga Jual (Rp)");
        txtJual.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtJual.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");

        KeyAdapter numOnly = new KeyAdapter() { public void keyTyped(KeyEvent evt) { if (!Character.isDigit(evt.getKeyChar())) evt.consume(); }};
        txtStok.addKeyListener(numOnly); txtModal.addKeyListener(numOnly); txtJual.addKeyListener(numOnly);

        btnSimpan = new JButton("Simpan Data");
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnHapus = new JButton("Hapus");
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapus.setEnabled(false);
        btnHapus.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ERROR_RED.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnBersih = new JButton("Batal");
        btnBersih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBersih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e0e0e0; foreground:#333333; font:bold +1; borderWidth:0; focusWidth:0");

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        panelForm.add(lblTitleForm);
        panelForm.add(new JLabel("Nama Sparepart") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtNama, "h 38!"); 
        panelForm.add(new JLabel("Kategori") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }}); panelForm.add(cbKategori, "h 38!");
        panelForm.add(new JLabel("Stok Barang") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtStok, "h 38!");
        panelForm.add(new JLabel("Harga Modal (Rp)") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtModal, "h 38!");
        panelForm.add(new JLabel("Harga Jual (Rp)") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtJual, "h 38!");
        
        JPanel panelBtn = new JPanel(new MigLayout("insets 0, gapx 10", "[grow][grow][grow]", "[40!]"));
        panelBtn.setOpaque(false);
        panelBtn.add(btnSimpan, "grow");
        panelBtn.add(btnHapus, "grow");
        panelBtn.add(btnBersih, "grow");
        panelForm.add(panelBtn, "gapy 10");

        JScrollPane scrollKiri = createCustomScroll(panelForm);
        
        // 2. TABEL DATA
        JPanel panelData = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill]", "[][fill,grow]"));
        panelData.setBackground(CARD_BG_COLOR);
        panelData.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        JPanel panelHeaderTabel = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right][]", "[]"));
        panelHeaderTabel.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        pnlTitle.setOpaque(false);
        JLabel lblTitleData = new JLabel("Data Inventaris Sparepart");
        lblTitleData.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitleData.setForeground(SIDEBAR_MAIN_COLOR);
        pnlTitle.add(lblTitleData);

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Nama Sparepart...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:10"); 
        try { txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16)); } catch (Exception e) {}

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:0,15,0,15");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelHeaderTabel.add(pnlTitle);
        panelHeaderTabel.add(txtSearch, "wmin 100, wmax 250, h 38!");
        panelHeaderTabel.add(btnRefresh, "h 38!");

        String[] kolom = {"ID", "Nama Sparepart", "Kategori", "Stok", "H. Modal", "H. Jual"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableSparepart = new JTable(tableModel);
        
        tableSparepart.setBackground(CARD_BG_COLOR);
        tableSparepart.setForeground(new Color(60, 60, 60));
        tableSparepart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableSparepart.setRowHeight(60); 
        tableSparepart.setShowGrid(false);
        tableSparepart.setShowHorizontalLines(true);
        tableSparepart.setGridColor(new Color(230, 230, 235));
        tableSparepart.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 80%); selectionForeground:#000000; selectionArc:10");

        JTableHeader tableHeader = tableSparepart.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setOpaque(false);
        tableHeader.setBackground(TABLE_HEADER_BG); 
        tableHeader.setForeground(SIDEBAR_MAIN_COLOR);
        
        tableSparepart.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableSparepart.getColumnModel().getColumn(1).setPreferredWidth(200); 
        tableSparepart.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableSparepart.getColumnModel().getColumn(3).setPreferredWidth(70);
        tableSparepart.getColumnModel().getColumn(4).setPreferredWidth(110);
        tableSparepart.getColumnModel().getColumn(5).setPreferredWidth(110);

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        tableSparepart.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(2).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(3).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(4).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(5).setCellRenderer(topLeftRenderer);

        WrapTextRenderer textWrapper = new WrapTextRenderer();
        tableSparepart.getColumnModel().getColumn(1).setCellRenderer(textWrapper); 

        ((DefaultTableCellRenderer) tableSparepart.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        
        rowSorter = new TableRowSorter<>(tableModel);
        tableSparepart.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(tableSparepart);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollTable.getViewport().setBackground(CARD_BG_COLOR);
        scrollTable.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:8; trackArc:999; thumbArc:999;");

        panelData.add(panelHeaderTabel, "gapbottom 15, growx, w 0:100%");
        panelData.add(scrollTable, "grow, w 0:100%, h 0:100%");

        add(scrollKiri, "grow, w 0:100%, h 0:100%");
        add(panelData, "grow, w 0:100%, h 0:100%");
        
        btnSimpan.addActionListener(e -> simpanAtauUpdateData());
        btnHapus.addActionListener(e -> hapusData());
        btnBersih.addActionListener(e -> bersihkanForm());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadDataDariDatabase(); });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });

        tableSparepart.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = tableSparepart.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tableSparepart.convertRowIndexToModel(row);
                    selectedId = tableModel.getValueAt(modelRow, 0).toString().replace("SPR-", ""); 
                    
                    txtNama.setText(tableModel.getValueAt(modelRow, 1).toString());
                    cbKategori.setSelectedItem(tableModel.getValueAt(modelRow, 2).toString());
                    txtStok.setText(tableModel.getValueAt(modelRow, 3).toString());
                    
                    String modal = tableModel.getValueAt(modelRow, 4).toString().replaceAll("[^0-9]", "");
                    String jual = tableModel.getValueAt(modelRow, 5).toString().replaceAll("[^0-9]", "");
                    txtModal.setText(modal);
                    txtJual.setText(jual);

                    lblTitleForm.setText("Edit Data Sparepart");
                    lblTitleForm.setForeground(SUCCESS_GREEN); 
                    
                    btnSimpan.setText("Update Data");
                    btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", SUCCESS_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
                    
                    btnHapus.setEnabled(true);
                }
            }
        });
    }

    private JScrollPane createCustomScroll(JPanel p) {
        JScrollPane s = new JScrollPane(p);
        s.setBorder(null); s.setOpaque(false); s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(15);
        s.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");
        return s;
    }
    
    // FUNGSI NOTIFIKASI CUSTOM
    private void tampilkanNotif(String title, String message, String type) {
        String bgColor = "#e74c3c";
        String iconName = "error.svg";
        
        if (type.equals("success")) {
            bgColor = "#27ae60";
            iconName = "success.svg";
        } else if (type.equals("warning")) {
            bgColor = "#ff8200";
            iconName = "error.svg";
        }

        JPanel internalPanel = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        internalPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor); 

        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        JLabel lbIcon = new JLabel(icon);
        
        JPanel textPanel = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]"));
        textPanel.setOpaque(false); 
        
        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#ffffff");
        JLabel lbMessage = new JLabel(message);
        lbMessage.putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(240,240,240)");
        
        textPanel.add(lbTitle);
        textPanel.add(lbMessage);
        
        internalPanel.add(lbIcon, "top, gapy 2");
        internalPanel.add(textPanel);
        
        JButton btnTutup = new JButton("Tutup");
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:" + bgColor + "; font:bold; arc:10; borderWidth:0; focusWidth:0; margin:5,15,5,15");
        
        btnTutup.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(btnTutup);
            if (window != null) window.dispose(); 
        });

        JOptionPane.showOptionDialog(this, internalPanel, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{btnTutup}, btnTutup);
    }
    
    // FUNGSI KONFIRMASI (YES / NO)
    private boolean tampilkanConfirm(String title, String message) {
        JPanel internalPanel = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        internalPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#e74c3c"); // Merah untuk Hapus

        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/error.svg", 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        JLabel lbIcon = new JLabel(icon);
        
        JPanel textPanel = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]"));
        textPanel.setOpaque(false); 
        
        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#ffffff");
        JLabel lbMessage = new JLabel(message);
        lbMessage.putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(240,240,240)");
        
        textPanel.add(lbTitle);
        textPanel.add(lbMessage);
        
        internalPanel.add(lbIcon, "top, gapy 2");
        internalPanel.add(textPanel);
        
        JButton btnBatal = new JButton("Batal");
        btnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatal.putClientProperty(FlatClientProperties.STYLE, "background:rgba(255,255,255,0.2); foreground:#ffffff; font:bold; arc:10; borderWidth:0; focusWidth:0; margin:5,15,5,15");

        JButton btnYa = new JButton("Ya, Lanjutkan");
        btnYa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYa.putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:#e74c3c; font:bold; arc:10; borderWidth:0; focusWidth:0; margin:5,15,5,15");
        
        final boolean[] result = {false};

        btnYa.addActionListener(e -> {
            result[0] = true;
            Window window = SwingUtilities.getWindowAncestor(btnYa);
            if (window != null) window.dispose();
        });

        btnBatal.addActionListener(e -> {
            result[0] = false;
            Window window = SwingUtilities.getWindowAncestor(btnBatal);
            if (window != null) window.dispose();
        });

        JOptionPane.showOptionDialog(this, internalPanel, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{btnBatal, btnYa}, btnBatal);

        return result[0];
    }

    private void loadDataDariDatabase() {
        tableModel.setRowCount(0); 
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        try {
            java.sql.Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT * FROM data_sparepart ORDER BY id_sparepart DESC";
            PreparedStatement ps = kon.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String hargaModal = formatRupiah.format(rs.getDouble("harga_modal")).replace(",00", "");
                String hargaJual = formatRupiah.format(rs.getDouble("harga_jual")).replace(",00", "");
                
                tableModel.addRow(new Object[]{
                    "SPR-" + rs.getInt("id_sparepart"),
                    rs.getString("nama_sparepart"), 
                    rs.getString("kategori"),
                    rs.getInt("stok"),
                    hargaModal,
                    hargaJual
                });
            }
        } catch (Exception e) {
            System.err.println("Gagal load data sparepart: " + e.getMessage());
        }
    }

    private void simpanAtauUpdateData() {
        String nama = txtNama.getText().trim();
        String kategori = cbKategori.getSelectedItem().toString();
        String stokStr = txtStok.getText().trim();
        String modalStr = txtModal.getText().trim();
        String jualStr = txtJual.getText().trim();

        if (nama.isEmpty() || stokStr.isEmpty() || modalStr.isEmpty() || jualStr.isEmpty()) {
            tampilkanNotif("Peringatan", "Semua kolom input wajib diisi!", "warning");
            return;
        }

        try {
            int stok = Integer.parseInt(stokStr);
            double modal = Double.parseDouble(modalStr);
            double jual = Double.parseDouble(jualStr);
            
            java.sql.Connection kon = DatabaseConnection.getKoneksi();
            
            if (selectedId.isEmpty()) {
                String sql = "INSERT INTO data_sparepart (nama_sparepart, kategori, stok, harga_modal, harga_jual) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, nama); ps.setString(2, kategori); ps.setInt(3, stok); ps.setDouble(4, modal); ps.setDouble(5, jual);
                
                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Berhasil", "Data sparepart baru berhasil ditambahkan!", "success");
                }
            } else {
                String sql = "UPDATE data_sparepart SET nama_sparepart=?, kategori=?, stok=?, harga_modal=?, harga_jual=? WHERE id_sparepart=?";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, nama); ps.setString(2, kategori); ps.setInt(3, stok); ps.setDouble(4, modal); ps.setDouble(5, jual); ps.setString(6, selectedId);
                
                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Diperbarui", "Data sparepart berhasil diupdate!", "success");
                }
            }
            loadDataDariDatabase();
            bersihkanForm(); 
            
        } catch (Exception e) {
            tampilkanNotif("Error Sistem", "Terjadi kesalahan database: " + e.getMessage(), "error");
        }
    }

    private void hapusData() {
        if (selectedId.isEmpty()) return;

        if (tampilkanConfirm("Konfirmasi Hapus", "Yakin ingin menghapus sparepart ini dari inventaris?")) {
            try {
                java.sql.Connection kon = DatabaseConnection.getKoneksi();
                String sql = "DELETE FROM data_sparepart WHERE id_sparepart=?";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, selectedId);
                
                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Terhapus", "Data sparepart berhasil dihapus!", "success");
                    loadDataDariDatabase();
                    bersihkanForm();
                }
            } catch (Exception e) {
                tampilkanNotif("Error Sistem", "Gagal menghapus data: " + e.getMessage(), "error");
            }
        }
    }

    private void bersihkanForm() {
        txtNama.setText(""); txtStok.setText(""); txtModal.setText(""); txtJual.setText("");
        cbKategori.setSelectedIndex(0); txtSearch.setText("");
        selectedId = ""; tableSparepart.clearSelection(); 
        
        lblTitleForm.setText("Input Sparepart Baru");
        lblTitleForm.setForeground(ACCENT_ORANGE); 
        btnSimpan.setText("Simpan Data");
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        btnHapus.setEnabled(false);
        txtNama.requestFocus();
    }

    private void liveSearch() {
        String keyword = txtSearch.getText();
        if (keyword.trim().isEmpty()) rowSorter.setRowFilter(null);
        else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() {
            setLineWrap(true); setWrapStyleWord(true); setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setMargin(new java.awt.Insets(10, 10, 10, 10)); setOpaque(true);
        }
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            if (isSelected) { setBackground(table.getSelectionBackground()); setForeground(table.getSelectionForeground()); }
            else { setBackground(table.getBackground()); setForeground(table.getForeground()); }
            return this;
        }
    }
}