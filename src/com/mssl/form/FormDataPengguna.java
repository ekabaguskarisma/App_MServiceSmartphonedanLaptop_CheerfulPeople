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

public class FormDataPengguna extends Form {

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color SUCCESS_GREEN = new Color(46, 204, 113); 
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245);

    private JLabel lblTitleForm, lblTotalAkun;
    private JTextField txtNama, txtUsername, txtSearch;
    private JPasswordField txtPassword;
    private JComboBox<String> cbRole;
    
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JButton btnSimpan, btnHapus, btnBersih, btnRefresh;
    private String selectedId = ""; 

    public FormDataPengguna() {
        init();
        loadDataDariDatabase(); 
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 25 30 25 30, gap 25", "[350!][fill,grow,0]", "[fill,grow,0]"));
        setBackground(APP_BG_COLOR);

        JPanel panelForm = new JPanel(new MigLayout("wrap, fillx, insets 25", "[fill]", "[]25[]5[]15[]5[]15[]5[]15[]5[]25[]"));
        panelForm.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");

        lblTitleForm = new JLabel("Tambah Akun Admin");
        lblTitleForm.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitleForm.setForeground(ACCENT_ORANGE);

        txtNama = new JTextField();
        txtNama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan Nama Lengkap");
        txtNama.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtNama.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");

        txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username Login (Unik)");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password Baru");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10; showRevealButton:true; showCapsLock:true");

        String[] roleList = {"Admin", "Teknisi"};
        cbRole = new JComboBox<>(roleList);
        cbRole.putClientProperty(FlatClientProperties.STYLE, "arc:10"); 

        btnSimpan = new JButton("Simpan Data");
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        // --- TAMBAHAN FITUR ENTER: TEKAN ENTER DI PASSWORD -> SIMPAN ---
        txtPassword.addActionListener(e -> btnSimpan.doClick());
        // ---------------------------------------------------------------
        
        btnHapus = new JButton("Hapus");
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapus.setEnabled(false);
        btnHapus.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ERROR_RED.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnBersih = new JButton("Batal");
        btnBersih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBersih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e0e0e0; foreground:#333333; font:bold +1; borderWidth:0; focusWidth:0");

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        JLabel l1 = new JLabel("Nama Admin / Teknisi"); l1.setFont(fontLabel); l1.setForeground(TEXT_MUTED);
        JLabel l2 = new JLabel("Username Login"); l2.setFont(fontLabel); l2.setForeground(TEXT_MUTED);
        JLabel l3 = new JLabel("Password"); l3.setFont(fontLabel); l3.setForeground(TEXT_MUTED);
        JLabel l4 = new JLabel("Hak Akses (Role)"); l4.setFont(fontLabel); l4.setForeground(TEXT_MUTED);

        panelForm.add(lblTitleForm);
        panelForm.add(l1); panelForm.add(txtNama, "h 38!"); 
        panelForm.add(l2); panelForm.add(txtUsername, "h 38!");
        panelForm.add(l3); panelForm.add(txtPassword, "h 38!");
        panelForm.add(l4); panelForm.add(cbRole, "h 38!");
        
        JPanel panelBtn = new JPanel(new MigLayout("insets 0, gapx 10", "[grow][grow][grow]", "[40!]"));
        panelBtn.setOpaque(false);
        panelBtn.add(btnSimpan, "grow");
        panelBtn.add(btnHapus, "grow");
        panelBtn.add(btnBersih, "grow");
        panelForm.add(panelBtn, "gapy 15");

        JScrollPane scrollKiri = createCustomScroll(panelForm);

        JPanel panelData = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill]", "[][fill,grow]"));
        panelData.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");

        JPanel panelHeaderTabel = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right][]", "[]"));
        panelHeaderTabel.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new MigLayout("wrap, insets 0, gapy 0", "[fill]", "[][]"));
        pnlTitle.setOpaque(false);
        
        JPanel pnlTitleTop = new JPanel(new MigLayout("insets 0, gapx 5", "[][]", "[]"));
        pnlTitleTop.setOpaque(false);
        
        JLabel lblTitleData = new JLabel("Daftar Akun");
        lblTitleData.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitleData.setForeground(SIDEBAR_MAIN_COLOR);
        
        lblTotalAkun = new JLabel("(0)");
        lblTotalAkun.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalAkun.setForeground(TEXT_MUTED);
        
        pnlTitleTop.add(lblTitleData);
        pnlTitleTop.add(lblTotalAkun);
        
        JLabel lblSubtitle = new JLabel("Admin & Teknisi");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(TEXT_MUTED);
        
        pnlTitle.add(pnlTitleTop, "gapbottom -2");
        pnlTitle.add(lblSubtitle);

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Nama / Username...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:15"); 
        try {
            txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16));
        } catch (Exception e) {}

        // --- TAMBAHAN FITUR ENTER: PENCARIAN PENGGUNA ---
        txtSearch.addActionListener(e -> {
            if (table.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0); // Sorot hasil pencarian
                pilihData(); // Langsung isi ke form
            }
        });
        
        btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelHeaderTabel.add(pnlTitle);
        panelHeaderTabel.add(txtSearch, "w 50:220, h 38!, gapright 10");
        panelHeaderTabel.add(btnRefresh, "h 38!"); 

        String[] kolom = {"No.", "Nama Lengkap", "Username", "Hak Akses", "ID_Asli"};
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
        
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // No.
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Nama Lengkap
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Username
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Hak Akses
        
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        WrapTextRenderer textWrapper = new WrapTextRenderer();
        
        table.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(textWrapper); 
        table.getColumnModel().getColumn(2).setCellRenderer(textWrapper); 
        table.getColumnModel().getColumn(3).setCellRenderer(topLeftRenderer);

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(table);
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

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                pilihData();
            }
        });
    }

    private void pilihData() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            
            selectedId = tableModel.getValueAt(modelRow, 4).toString(); 

            txtNama.setText(tableModel.getValueAt(modelRow, 1).toString());
            txtUsername.setText(tableModel.getValueAt(modelRow, 2).toString());
            cbRole.setSelectedItem(tableModel.getValueAt(modelRow, 3).toString());
            
            txtPassword.setText("");
            txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(Kosongkan jika tak diubah)");

            lblTitleForm.setText("Edit Akun");
            lblTitleForm.setForeground(SUCCESS_GREEN); 
            
            btnSimpan.setText("Update Akun");
            btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", SUCCESS_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
            
            btnHapus.setEnabled(true);
        }
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
    
    private boolean tampilkanConfirm(String title, String message) {
        JPanel internalPanel = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        internalPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#e74c3c"); 

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
        int total = 0;
        int nomorUrut = 1;
        
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT * FROM data_pengguna WHERE role != 'Pelanggan' ORDER BY id_user DESC";
            PreparedStatement ps = kon.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    nomorUrut++,
                    rs.getString("nama_lengkap"), 
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getInt("id_user")
                });
                total++;
            }
            lblTotalAkun.setText("(" + total + ")");
        } catch (Exception e) {
            System.err.println("Gagal load data akun: " + e.getMessage());
        }
    }

    private void simpanAtauUpdateData() {
        String nama = txtNama.getText().trim();
        String username = txtUsername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword()).trim();
        String role = cbRole.getSelectedItem().toString();

        if (nama.isEmpty() || username.isEmpty()) {
            tampilkanNotif("Peringatan", "Nama dan Username wajib diisi!", "warning");
            return;
        }

        try {
            Connection kon = DatabaseConnection.getKoneksi();
            
            if (selectedId.isEmpty()) {
                if(password.isEmpty()){
                    tampilkanNotif("Peringatan", "Password wajib diisi untuk akun baru!", "warning");
                    return;
                }
                
                String sql = "INSERT INTO data_pengguna (nama_lengkap, username, password, role) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, nama);
                ps.setString(2, username);
                ps.setString(3, password);
                ps.setString(4, role);
                
                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Sukses", "Akun baru berhasil ditambahkan!", "success");
                }
            } else {
                String sql;
                PreparedStatement ps;
                if(password.isEmpty()){
                    sql = "UPDATE data_pengguna SET nama_lengkap=?, username=?, role=? WHERE id_user=?";
                    ps = kon.prepareStatement(sql);
                    ps.setString(1, nama);
                    ps.setString(2, username);
                    ps.setString(3, role);
                    ps.setString(4, selectedId);
                } else {
                    sql = "UPDATE data_pengguna SET nama_lengkap=?, username=?, password=?, role=? WHERE id_user=?";
                    ps = kon.prepareStatement(sql);
                    ps.setString(1, nama);
                    ps.setString(2, username);
                    ps.setString(3, password);
                    ps.setString(4, role);
                    ps.setString(5, selectedId);
                }

                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Sukses", "Data akun berhasil diperbarui!", "success");
                }
            }
            loadDataDariDatabase();
            bersihkanForm(); 
            
        } catch (Exception e) {
            tampilkanNotif("Error Database", "Gagal memproses data (Mungkin Username sudah digunakan): " + e.getMessage(), "error");
        }
    }

    private void hapusData() {
        if (selectedId.isEmpty()) return;

        if(tampilkanConfirm("Konfirmasi Hapus", "Yakin ingin menghapus akun ini dari sistem?")) {
            try {
                Connection kon = DatabaseConnection.getKoneksi();
                String sql = "DELETE FROM data_pengguna WHERE id_user=?";
                PreparedStatement ps = kon.prepareStatement(sql);
                ps.setString(1, selectedId);

                if (ps.executeUpdate() > 0) {
                    tampilkanNotif("Sukses", "Akun berhasil dihapus!", "success");
                    loadDataDariDatabase();
                    bersihkanForm();
                }
            } catch (Exception e) {
                tampilkanNotif("Error Database", "Gagal menghapus data: " + e.getMessage(), "error");
            }
        }
    }

    private void bersihkanForm() {
        txtNama.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password Baru"); 
        cbRole.setSelectedIndex(0); 
        txtSearch.setText("");
        selectedId = ""; 
        table.clearSelection(); 
        
        lblTitleForm.setText("Tambah Akun Baru");
        lblTitleForm.setForeground(ACCENT_ORANGE); 
        
        btnSimpan.setText("Simpan Data");
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", ACCENT_ORANGE.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnHapus.setEnabled(false);
        txtNama.requestFocus();
    }

    private void liveSearch() {
        String keyword = txtSearch.getText();
        if (keyword.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
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