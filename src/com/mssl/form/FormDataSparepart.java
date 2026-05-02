package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import com.mssl.utils.UIHelper; // IMPORT SAKTI KITA MASUK DI SINI
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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
        
        txtJual.addActionListener(e -> btnSimpan.doClick());
        
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

        // MEMANGGIL FUNGSI SCROLL DARI UIHELPER
        JScrollPane scrollKiri = UIHelper.createCustomScroll(panelForm);
        
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

        String[] kolom = {"No.", "Nama Sparepart", "Kategori", "Stok", "H. Modal", "H. Jual", "ID_Asli", "Modal_Asli", "Jual_Asli"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableSparepart = new JTable(tableModel);
        
        // MEMANGGIL STYLING TABEL DARI UIHELPER
        UIHelper.styleTable(tableSparepart, TABLE_HEADER_BG, SIDEBAR_MAIN_COLOR);
        
        tableSparepart.getColumnModel().getColumn(0).setPreferredWidth(50); // No.
        tableSparepart.getColumnModel().getColumn(1).setPreferredWidth(200); // Nama
        tableSparepart.getColumnModel().getColumn(2).setPreferredWidth(120); // Kategori
        tableSparepart.getColumnModel().getColumn(3).setPreferredWidth(60);  // Stok
        tableSparepart.getColumnModel().getColumn(4).setPreferredWidth(120); // Modal (Rp)
        tableSparepart.getColumnModel().getColumn(5).setPreferredWidth(120); // Jual (Rp)
        
        // Sembunyikan Data Asli (Tanpa Rp)
        for (int i = 6; i <= 8; i++) {
            tableSparepart.getColumnModel().getColumn(i).setMinWidth(0);
            tableSparepart.getColumnModel().getColumn(i).setMaxWidth(0);
            tableSparepart.getColumnModel().getColumn(i).setWidth(0);
        }

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        tableSparepart.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(2).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(3).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(4).setCellRenderer(topLeftRenderer);
        tableSparepart.getColumnModel().getColumn(5).setCellRenderer(topLeftRenderer);

        // MEMANGGIL TEXT WRAPPER DARI UIHELPER
        UIHelper.WrapTextRenderer textWrapper = new UIHelper.WrapTextRenderer();
        tableSparepart.getColumnModel().getColumn(1).setCellRenderer(textWrapper); 

        StockRenderer stockRenderer = new StockRenderer();
        tableSparepart.getColumnModel().getColumn(4).setCellRenderer(stockRenderer);

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
        
        btnRefresh.addActionListener(e -> { 
            txtSearch.setText(""); 
            bersihkanForm(); 
            loadDataDariDatabase(); 
        });

        txtSearch.addActionListener(e -> {
            if (tableSparepart.getRowCount() > 0) {
                tableSparepart.setRowSelectionInterval(0, 0); 
                pilihData(); 
            }
        });
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });

        tableSparepart.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                pilihData();
            }
        });
    }

    private void pilihData() {
        int row = tableSparepart.getSelectedRow();
        if (row >= 0) {
            int modelRow = tableSparepart.convertRowIndexToModel(row);
            selectedId = tableModel.getValueAt(modelRow, 6).toString(); 
            
            txtNama.setText(tableModel.getValueAt(modelRow, 1).toString());
            cbKategori.setSelectedItem(tableModel.getValueAt(modelRow, 2).toString());
            txtStok.setText(tableModel.getValueAt(modelRow, 3).toString());
            
            txtModal.setText(tableModel.getValueAt(modelRow, 7).toString());
            txtJual.setText(tableModel.getValueAt(modelRow, 8).toString());

            lblTitleForm.setText("Edit Data Sparepart");
            lblTitleForm.setForeground(SUCCESS_GREEN); 
            
            btnSimpan.setText("Update Data");
            btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", SUCCESS_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
            
            btnHapus.setEnabled(true);
        }
    }

    private void loadDataDariDatabase() {
        tableModel.setRowCount(0); 
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        int no = 1;
        
        try (Connection kon = DatabaseConnection.getKoneksi();
             PreparedStatement ps = kon.prepareStatement("SELECT * FROM data_sparepart ORDER BY id_sparepart DESC");
             ResultSet rs = ps.executeQuery()) { // TRY-WITH-RESOURCES

            while (rs.next()) {
                double modal = rs.getDouble("harga_modal");
                double jual = rs.getDouble("harga_jual");
                
                String rpModal = formatRupiah.format(modal).replace(",00", "");
                String rpJual = formatRupiah.format(jual).replace(",00", "");
                
                tableModel.addRow(new Object[]{
                    no++, // Visual Nomor Urut
                    rs.getString("nama_sparepart"), 
                    rs.getString("kategori"),
                    rs.getInt("stok"),
                    rpModal, // Visual Rupiah
                    rpJual,  // Visual Rupiah
                    rs.getInt("id_sparepart"), // Hidden ID Asli
                    String.format("%.0f", modal), // Hidden Angka Mentah Modal
                    String.format("%.0f", jual)   // Hidden Angka Mentah Jual
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
            UIHelper.tampilkanNotif(this, "Peringatan", "Semua kolom input wajib diisi!", "warning");
            return;
        }

        try {
            int stok = Integer.parseInt(stokStr);
            double modal = Double.parseDouble(modalStr);
            double jual = Double.parseDouble(jualStr);
            
            Connection kon = DatabaseConnection.getKoneksiTransaksi();
            
            if (selectedId.isEmpty()) {
                String sqlCek = "SELECT id_sparepart, stok FROM data_sparepart WHERE nama_sparepart = ? AND kategori = ?";
                try (PreparedStatement psCek = kon.prepareStatement(sqlCek)) {
                    psCek.setString(1, nama);
                    psCek.setString(2, kategori);
                    try (ResultSet rs = psCek.executeQuery()) {
                        if (rs.next()) {
                            int idExisting = rs.getInt("id_sparepart");
                            int stokLama = rs.getInt("stok");
                            int totalStokSekarang = stokLama + stok;

                            String sqlUpdate = "UPDATE data_sparepart SET stok = ?, harga_modal = ?, harga_jual = ? WHERE id_sparepart = ?";
                            try (PreparedStatement psUpdate = kon.prepareStatement(sqlUpdate)) {
                                psUpdate.setInt(1, totalStokSekarang); psUpdate.setDouble(2, modal); psUpdate.setDouble(3, jual); psUpdate.setInt(4, idExisting);
                                if (psUpdate.executeUpdate() > 0) {
                                    UIHelper.tampilkanNotif(this, "Berhasil", "Barang sudah ada. Stok ditambahkan menjadi: " + totalStokSekarang, "success");
                                }
                            }
                        } else {
                            String sqlInsert = "INSERT INTO data_sparepart (nama_sparepart, kategori, stok, harga_modal, harga_jual) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement psInsert = kon.prepareStatement(sqlInsert)) {
                                psInsert.setString(1, nama); psInsert.setString(2, kategori); psInsert.setInt(3, stok); psInsert.setDouble(4, modal); psInsert.setDouble(5, jual);
                                if (psInsert.executeUpdate() > 0) {
                                    UIHelper.tampilkanNotif(this, "Berhasil", "Data sparepart baru berhasil ditambahkan!", "success");
                                }
                            }
                        }
                    }
                }
            } else {
                String sql = "UPDATE data_sparepart SET nama_sparepart=?, kategori=?, stok=?, harga_modal=?, harga_jual=? WHERE id_sparepart=?";
                try (PreparedStatement ps = kon.prepareStatement(sql)) {
                    ps.setString(1, nama); ps.setString(2, kategori); ps.setInt(3, stok); ps.setDouble(4, modal); ps.setDouble(5, jual); ps.setString(6, selectedId);
                    if (ps.executeUpdate() > 0) {
                        UIHelper.tampilkanNotif(this, "Diperbarui", "Data sparepart berhasil diupdate!", "success");
                    }
                }
            }
            
            loadDataDariDatabase();
            bersihkanForm(); 
            
        } catch (NumberFormatException e) {
            UIHelper.tampilkanNotif(this, "Error", "Input stok atau harga harus berupa angka!", "error");
        } catch (Exception e) {
            UIHelper.tampilkanNotif(this, "Error Sistem", "Terjadi kesalahan database: " + e.getMessage(), "error");
        }
    }

    private void hapusData() {
        if (selectedId.isEmpty()) return;

        if (UIHelper.tampilkanConfirm(this, "Konfirmasi Hapus", "Yakin ingin menghapus sparepart ini dari inventaris?")) {
            try {
                Connection kon = DatabaseConnection.getKoneksiTransaksi();
                try (PreparedStatement ps = kon.prepareStatement("DELETE FROM data_sparepart WHERE id_sparepart=?")) {
                    ps.setString(1, selectedId);
                    if (ps.executeUpdate() > 0) {
                        UIHelper.tampilkanNotif(this, "Terhapus", "Data sparepart berhasil dihapus!", "success");
                        loadDataDariDatabase();
                        bersihkanForm();
                    }
                }
            } catch (Exception e) {
                UIHelper.tampilkanNotif(this, "Gagal", "Data tidak bisa dihapus karena masih terkait di keranjang.", "error");
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

    // KHUSUS FORM SPAREPART, CLASS INI DIPERTAHANKAN KARENA ADA WARNA CUSTOM JIKA STOK <= 5
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