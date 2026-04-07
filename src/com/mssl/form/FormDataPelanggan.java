package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
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

public class FormDataPelanggan extends Form {

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color SUCCESS_GREEN = new Color(39, 174, 96); 
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245); 

    private JLabel lblTitleForm, lblTotalPelanggan;
    private JTextField txtNama, txtWa, txtSearch;
    private JTextArea txtAlamat;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JButton btnSimpan, btnHapus, btnBersih, btnRefresh;
    private String selectedId = ""; 

    public FormDataPelanggan() {
        init();
        loadDataDariDatabase(); 
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 25 30 25 30, gap 25", "[350!][fill,grow,0]", "[fill,grow,0]"));
        setBackground(APP_BG_COLOR);
        
        // FORM INPUT
        JPanel panelForm = new JPanel(new MigLayout("wrap, fillx, insets 25", "[fill]", "[]25[]5[]15[]5[]15[]5[]25[]"));
        panelForm.setBackground(CARD_BG_COLOR);
        panelForm.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        lblTitleForm = new JLabel("Input Pelanggan Baru");
        lblTitleForm.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitleForm.setForeground(ACCENT_ORANGE);

        txtNama = new JTextField();
        txtNama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan Nama Lengkap");
        txtNama.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtNama.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");

        txtWa = new JTextField();
        txtWa.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Contoh: 081234567890");
        txtWa.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtWa.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        
        txtWa.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                if (!Character.isDigit(evt.getKeyChar())) evt.consume(); 
            }
        });

        txtAlamat = new JTextArea(5, 20);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        txtAlamat.putClientProperty(FlatClientProperties.STYLE, "margin:5,10,5,10");
        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);
        scrollAlamat.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 225)));

        btnSimpan = new JButton("Simpan Data");
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnHapus = new JButton("Hapus");
        btnHapus.setEnabled(false);
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapus.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ERROR_RED.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnBersih = new JButton("Batal");
        btnBersih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBersih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e0e0e0; font:bold; borderWidth:0; focusWidth:0");

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        panelForm.add(lblTitleForm);
        panelForm.add(new JLabel("Nama Lengkap") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }});
        panelForm.add(txtNama, "h 38!"); 
        panelForm.add(new JLabel("No. WhatsApp") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }});
        panelForm.add(txtWa, "h 38!");
        panelForm.add(new JLabel("Alamat Lengkap") {{ setFont(fontLabel); setForeground(TEXT_MUTED); }});
        panelForm.add(scrollAlamat, "h 120!");
        
        JPanel panelBtn = new JPanel(new MigLayout("insets 0, gapx 10", "[grow][grow][grow]", "[45!]"));
        panelBtn.setOpaque(false);
        panelBtn.add(btnSimpan, "grow"); panelBtn.add(btnHapus, "grow"); panelBtn.add(btnBersih, "grow");
        panelForm.add(panelBtn, "gapy 15");

        JScrollPane scrollKiri = createCustomScroll(panelForm);
        
        // TABEL DATA
        JPanel panelData = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill]", "[][fill,grow]"));
        panelData.setBackground(CARD_BG_COLOR);
        panelData.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        JPanel headerTabel = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right][]", "[]"));
        headerTabel.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new MigLayout("insets 0, gapx 5", "[][]", "[]"));
        pnlTitle.setOpaque(false);
        JLabel lblTitleData = new JLabel("Daftar Pelanggan");
        lblTitleData.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitleData.setForeground(SIDEBAR_MAIN_COLOR);
        lblTotalPelanggan = new JLabel("(0)");
        lblTotalPelanggan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalPelanggan.setForeground(TEXT_MUTED);
        pnlTitle.add(lblTitleData);
        pnlTitle.add(lblTotalPelanggan);

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Nama / WA...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        try {
            txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16));
        } catch (Exception e) {}

        btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:0,15,0,15");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        headerTabel.add(pnlTitle);
        headerTabel.add(txtSearch, "wmin 100, wmax 250, h 38!");
        headerTabel.add(btnRefresh, "h 38!");

        // KOLOM No, Nama, WA, Alamat, ID_ASLI
        String[] kolom = {"No.", "Nama Pelanggan", "No. WhatsApp", "Alamat", "ID_Asli"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        
        table.setBackground(CARD_BG_COLOR);
        table.setForeground(new Color(60, 60, 60));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(60); 
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 235));
        table.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 80%); selectionForeground:#000000; selectionArc:10");

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setOpaque(false);
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(SIDEBAR_MAIN_COLOR);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        
        // Sembunyikan ID Database Asli
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        table.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(topLeftRenderer);

        WrapTextRenderer textWrapper = new WrapTextRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(textWrapper); 
        table.getColumnModel().getColumn(3).setCellRenderer(textWrapper); 

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollTable.getViewport().setBackground(CARD_BG_COLOR);
        scrollTable.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:8; trackArc:999; thumbArc:999;");

        panelData.add(headerTabel, "gapbottom 15, growx, w 0:100%");
        panelData.add(scrollTable, "grow, w 0:100%, h 0:100%");

        add(scrollKiri, "grow, w 0:100%, h 0:100%");
        add(panelData, "grow, w 0:100%, h 0:100%");
        
        btnSimpan.addActionListener(e -> simpanAtauUpdateData());
        btnHapus.addActionListener(e -> hapusData());
        btnBersih.addActionListener(e -> bersihkanForm());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadDataDariDatabase();
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    // Ambil ID Asli dari kolom rahasia (index 4)
                    selectedId = tableModel.getValueAt(modelRow, 4).toString();
                    
                    txtNama.setText(tableModel.getValueAt(modelRow, 1).toString());
                    txtWa.setText(tableModel.getValueAt(modelRow, 2).toString());
                    txtAlamat.setText(tableModel.getValueAt(modelRow, 3).toString());

                    lblTitleForm.setText("Edit Data Pelanggan");
                    lblTitleForm.setForeground(SUCCESS_GREEN);
                    btnSimpan.setText("Update Data");
                    btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", SUCCESS_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1");
                    btnHapus.setEnabled(true);
                }
            }
        });
    }

    private JScrollPane createCustomScroll(JPanel p) {
        JScrollPane s = new JScrollPane(p);
        s.setBorder(null);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(15);
        s.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");
        return s;
    }
    
    // CUSTOM NOTIFICATIONS
    private void tampilkanNotif(String title, String message, String type) {
        String bgColor = (type.equals("success")) ? "#27ae60" : (type.equals("warning") ? "#ff8200" : "#e74c3c");
        String iconName = (type.equals("success")) ? "success.svg" : "error.svg";
        
        JPanel p = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor); 
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        
        JPanel tp = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]")); tp.setOpaque(false); 
        tp.add(new JLabel(title) {{ putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#ffffff"); }});
        tp.add(new JLabel(message) {{ putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(240,240,240)"); }});
        
        p.add(new JLabel(icon), "top"); p.add(tp);
        JButton b = new JButton("Tutup") {{ setCursor(new Cursor(Cursor.HAND_CURSOR)); putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:"+bgColor+"; font:bold; arc:10; borderWidth:0; margin:5,15,5,15"); }};
        b.addActionListener(e -> { Window w = SwingUtilities.getWindowAncestor(b); if(w!=null) w.dispose(); });
        JOptionPane.showOptionDialog(this, p, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{b}, b);
    }

    private boolean tampilkanConfirm(String title, String message) {
        final boolean[] res = {false};
        JPanel p = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#e74c3c");
        JPanel tp = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]")); tp.setOpaque(false);
        tp.add(new JLabel(title) {{ putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#ffffff"); }});
        tp.add(new JLabel(message) {{ putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(240,240,240)"); }});
        p.add(new JLabel(new FlatSVGIcon("com/mssl/icon/error.svg", 45, 45) {{ setColorFilter(new FlatSVGIcon.ColorFilter(c->Color.WHITE)); }}), "top"); p.add(tp);
        JButton bY = new JButton("Ya, Hapus") {{ setCursor(new Cursor(Cursor.HAND_CURSOR)); putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:#e74c3c; font:bold; arc:10; borderWidth:0; margin:5,15,5,15"); }};
        JButton bB = new JButton("Batal") {{ setCursor(new Cursor(Cursor.HAND_CURSOR)); putClientProperty(FlatClientProperties.STYLE, "background:rgba(255,255,255,0.2); foreground:#ffffff; font:bold; arc:10; borderWidth:0; margin:5,15,5,15"); }};
        bY.addActionListener(e -> { res[0]=true; Window w=SwingUtilities.getWindowAncestor(bY); if(w!=null) w.dispose(); });
        bB.addActionListener(e -> { Window w=SwingUtilities.getWindowAncestor(bB); if(w!=null) w.dispose(); });
        JOptionPane.showOptionDialog(this, p, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{bB, bY}, bB);
        return res[0];
    }

    private void loadDataDariDatabase() {
        tableModel.setRowCount(0); 
        int total = 0;
        int nomorUrut = 1;
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT * FROM data_pelanggan ORDER BY id_pelanggan DESC";
            ResultSet rs = kon.createStatement().executeQuery(sql);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    nomorUrut++, // No. Visual
                    rs.getString("nama_pelanggan"), 
                    rs.getString("no_whatsapp"), 
                    rs.getString("alamat"),
                    rs.getInt("id_pelanggan") // ID Asli (Hidden)
                });
                total++;
            }
            lblTotalPelanggan.setText("(" + total + " Orang)");
        } catch (Exception e) {
            System.err.println("Gagal load data pelanggan: " + e.getMessage());
        }
    }

    private void simpanAtauUpdateData() {
        String nama = txtNama.getText().trim();
        String wa = txtWa.getText().trim();
        String alamat = txtAlamat.getText().trim();

        if (nama.isEmpty() || wa.isEmpty()) {
            tampilkanNotif("Peringatan", "Nama dan Nomor WhatsApp wajib diisi!", "warning");
            return;
        }

        try {
            Connection kon = DatabaseConnection.getKoneksi();
            if (selectedId.isEmpty()) {
                String sql = "INSERT INTO data_pelanggan (nama_pelanggan, no_whatsapp, alamat) VALUES (?, ?, ?)";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, nama); ps.setString(2, wa); ps.setString(3, alamat);
                if (ps.executeUpdate() > 0) tampilkanNotif("Berhasil", "Data pelanggan baru berhasil ditambahkan!", "success");
            } else {
                String sql = "UPDATE data_pelanggan SET nama_pelanggan=?, no_whatsapp=?, alamat=? WHERE id_pelanggan=?";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, nama); ps.setString(2, wa); ps.setString(3, alamat); ps.setString(4, selectedId);
                if (ps.executeUpdate() > 0) tampilkanNotif("Diperbarui", "Data pelanggan berhasil diupdate!", "success");
            }
            loadDataDariDatabase(); bersihkanForm();
        } catch (Exception e) {
            tampilkanNotif("Error Database", e.getMessage(), "error");
        }
    }

    private void hapusData() {
        if (selectedId.isEmpty()) return;
        if (tampilkanConfirm("Konfirmasi Hapus", "Yakin ingin menghapus data pelanggan ini dari sistem?")) {
            try {
                Connection kon = DatabaseConnection.getKoneksi();
                PreparedStatement ps = kon.prepareStatement("DELETE FROM data_pelanggan WHERE id_pelanggan=?");
                ps.setString(1, selectedId);
                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Terhapus", "Data pelanggan berhasil dihapus.", "success");
                    loadDataDariDatabase(); bersihkanForm();
                }
            } catch (Exception e) {
                tampilkanNotif("Gagal Hapus", "Data ini tidak bisa dihapus karena masih terkait dengan transaksi servis.", "error");
            }
        }
    }

    private void bersihkanForm() {
        txtNama.setText(""); txtWa.setText(""); txtAlamat.setText(""); txtSearch.setText("");
        selectedId = ""; table.clearSelection();
        lblTitleForm.setText("Input Pelanggan Baru");
        lblTitleForm.setForeground(ACCENT_ORANGE);
        btnSimpan.setText("Simpan Data");
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1");
        btnHapus.setEnabled(false);
        txtNama.requestFocus();
    }

    private void liveSearch() {
        String k = txtSearch.getText();
        if (k.trim().isEmpty()) rowSorter.setRowFilter(null);
        else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + k));
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