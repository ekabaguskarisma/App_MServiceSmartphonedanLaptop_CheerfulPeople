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

public class FormPengerjaanStatus extends Form {

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color SUCCESS_GREEN = new Color(39, 174, 96); 
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245);

    private JTextField txtID, txtNama, txtPerangkat, txtKeluhan, txtSearch;
    private JTextArea txtDiagnosa, txtTindakan;
    private JComboBox<String> cbStatus;
    private JLabel lblTotalTugas;
    
    private JTable tablePekerjaan;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JButton btnUpdate, btnBersih, btnRefresh;
    private String selectedIdServis = ""; 

    public FormPengerjaanStatus() {
        init();
        loadPekerjaanAktif(); 
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 25 30 25 30, gap 25", "[400!][fill,grow,0]", "[fill,grow,0]"));
        setBackground(APP_BG_COLOR);
        
        // UPDATE PROGRES
        JPanel panelForm = new JPanel(new MigLayout("wrap, fillx, insets 25", "[fill]", "[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]20[]"));
        panelForm.setBackground(CARD_BG_COLOR);
        panelForm.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        JLabel lblTitle = new JLabel("Pengerjaan & Status");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ACCENT_ORANGE);

        txtID = createReadOnlyField("ID Nota");
        txtNama = createReadOnlyField("Nama Pemilik");
        txtPerangkat = createReadOnlyField("Unit Perangkat");
        txtKeluhan = createReadOnlyField("Keluhan awal dari CS");

        txtDiagnosa = new JTextArea(4, 20);
        txtDiagnosa.setLineWrap(true); txtDiagnosa.setWrapStyleWord(true);
        txtDiagnosa.putClientProperty(FlatClientProperties.STYLE, "margin:5,10,5,10");
        txtDiagnosa.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tulis hasil analisa kerusakan...");
        JScrollPane scrollDiagnosa = new JScrollPane(txtDiagnosa);
        scrollDiagnosa.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 225)));

        txtTindakan = new JTextArea(4, 20);
        txtTindakan.setLineWrap(true); txtTindakan.setWrapStyleWord(true);
        txtTindakan.putClientProperty(FlatClientProperties.STYLE, "margin:5,10,5,10");
        txtTindakan.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Solusi / Sparepart yang diganti...");
        JScrollPane scrollTindakan = new JScrollPane(txtTindakan);
        scrollTindakan.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 225)));

        cbStatus = new JComboBox<>(new String[]{"Antrean", "Proses", "Menunggu Sparepart", "Selesai", "Batal"});
        cbStatus.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        btnUpdate = new JButton("Update Progres Servis");
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:" + String.format("#%06x", SUCCESS_GREEN.getRGB() & 0xFFFFFF) + "; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnBersih = new JButton("Reset");
        btnBersih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBersih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e0e0e0; font:bold; borderWidth:0; focusWidth:0");

        Font fLabel = new Font("Segoe UI", Font.BOLD, 12);
        panelForm.add(lblTitle, "gapbottom 10");
        panelForm.add(new JLabel("ID NOTA") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtID, "h 38!");
        panelForm.add(new JLabel("PELANGGAN") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtNama, "h 38!");
        panelForm.add(new JLabel("PERANGKAT") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtPerangkat, "h 38!");
        panelForm.add(new JLabel("KELUHAN AWAL") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}); panelForm.add(txtKeluhan, "h 38!");
        
        panelForm.add(new JLabel("HASIL DIAGNOSA TEKNISI") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}, "gapy 10");
        panelForm.add(scrollDiagnosa, "h 90!");
        
        panelForm.add(new JLabel("TINDAKAN / SOLUSI") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}, "gapy 10");
        panelForm.add(scrollTindakan, "h 90!");
        
        panelForm.add(new JLabel("UBAH STATUS SERVIS") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}, "gapy 10");
        panelForm.add(cbStatus, "h 38!");

        JPanel pBtn = new JPanel(new MigLayout("insets 0, gapx 10", "[grow][100!]", "[45!]"));
        pBtn.setOpaque(false);
        pBtn.add(btnUpdate, "grow"); pBtn.add(btnBersih, "grow");
        panelForm.add(pBtn, "gapy 15");

        JScrollPane scrollKiri = createCustomScroll(panelForm);
        
        // TABEL PEKERJAAN
        JPanel panelData = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill]", "[][fill,grow]"));
        panelData.setBackground(CARD_BG_COLOR);
        panelData.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        JPanel pHeaderTable = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right][]", "[]"));
        pHeaderTable.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        pnlTitle.setOpaque(false);
        JLabel lblT = new JLabel("Pekerjaan Aktif");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblT.setForeground(SIDEBAR_MAIN_COLOR);
        lblTotalTugas = new JLabel("(0)");
        lblTotalTugas.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalTugas.setForeground(TEXT_MUTED);
        pnlTitle.add(lblT); pnlTitle.add(lblTotalTugas);

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Pekerjaan...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:15"); 
        try { txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16)); } catch (Exception e) {}

        btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(SIDEBAR_MAIN_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; margin:0,15,0,15");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pHeaderTable.add(pnlTitle);
        pHeaderTable.add(txtSearch, "w 50:220, h 38!, gapright 10");
        pHeaderTable.add(btnRefresh, "h 38!"); 

        // PERBAIKAN: Menambahkan kolom No. Urut visual di Index 0
        String[] kolom = {"No.", "ID Nota", "Pelanggan", "Perangkat", "Status", "ID_Asli"};
        tableModel = new DefaultTableModel(kolom, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablePekerjaan = new JTable(tableModel);
        styleTable(tablePekerjaan);
        
        tablePekerjaan.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablePekerjaan.getColumnModel().getColumn(1).setPreferredWidth(90);
        tablePekerjaan.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablePekerjaan.getColumnModel().getColumn(3).setPreferredWidth(180);
        tablePekerjaan.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        // Sembunyikan ID Database Asli
        tablePekerjaan.getColumnModel().getColumn(5).setMinWidth(0);
        tablePekerjaan.getColumnModel().getColumn(5).setMaxWidth(0);
        tablePekerjaan.getColumnModel().getColumn(5).setWidth(0);

        rowSorter = new TableRowSorter<>(tableModel);
        tablePekerjaan.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(tablePekerjaan);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollTable.getViewport().setBackground(CARD_BG_COLOR);

        panelData.add(pHeaderTable, "gapbottom 15");
        panelData.add(scrollTable, "grow");

        add(scrollKiri, "grow");
        add(panelData, "grow");

        // LISTENERS
        btnUpdate.addActionListener(e -> updateProgresServis());
        btnBersih.addActionListener(e -> resetForm());
        btnRefresh.addActionListener(e -> { 
            txtSearch.setText(""); 
            resetForm();
            loadPekerjaanAktif(); 
        });
        
        // --- TAMBAHAN FITUR ENTER: PENCARIAN ---
        txtSearch.addActionListener(e -> {
            if (tablePekerjaan.getRowCount() > 0) {
                tablePekerjaan.setRowSelectionInterval(0, 0); // Sorot baris pertama
                int modelRow = tablePekerjaan.convertRowIndexToModel(0);
                selectedIdServis = tableModel.getValueAt(modelRow, 5).toString();
                txtID.setText(tableModel.getValueAt(modelRow, 1).toString());
                txtNama.setText(tableModel.getValueAt(modelRow, 2).toString());
                txtPerangkat.setText(tableModel.getValueAt(modelRow, 3).toString());
                loadDetailPengerjaan(selectedIdServis);
            }
        });
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText())); }
            public void removeUpdate(DocumentEvent e) { rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText())); }
            public void changedUpdate(DocumentEvent e) { rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText())); }
        });

        tablePekerjaan.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = tablePekerjaan.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tablePekerjaan.convertRowIndexToModel(row);
                    selectedIdServis = tableModel.getValueAt(modelRow, 5).toString();
                    txtID.setText(tableModel.getValueAt(modelRow, 1).toString());
                    txtNama.setText(tableModel.getValueAt(modelRow, 2).toString());
                    txtPerangkat.setText(tableModel.getValueAt(modelRow, 3).toString());
                    loadDetailPengerjaan(selectedIdServis);
                }
            }
        });
    }

    private JTextField createReadOnlyField(String p) {
        JTextField tf = new JTextField(); tf.setEditable(false); tf.setBackground(new Color(245, 246, 250));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, p);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc:10; foreground:#666666");
        return tf;
    }

    private JScrollPane createCustomScroll(JPanel p) {
        JScrollPane s = new JScrollPane(p); s.setBorder(null); s.setOpaque(false); s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(15);
        s.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");
        return s;
    }

    private void styleTable(JTable tb) {
        tb.setRowHeight(60); tb.setShowGrid(false); tb.setShowHorizontalLines(true);
        tb.setGridColor(new Color(230, 230, 235));
        tb.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 80%); selectionForeground:#000000; selectionArc:10");
        JTableHeader h = tb.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13)); h.setBackground(TABLE_HEADER_BG); h.setForeground(SIDEBAR_MAIN_COLOR);
        ((DefaultTableCellRenderer)h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        
        DefaultTableCellRenderer top = new DefaultTableCellRenderer();
        top.setVerticalAlignment(SwingConstants.TOP); top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        WrapTextRenderer wrap = new WrapTextRenderer();
        tb.getColumnModel().getColumn(0).setCellRenderer(top);
        tb.getColumnModel().getColumn(1).setCellRenderer(top);
        tb.getColumnModel().getColumn(2).setCellRenderer(wrap);
        tb.getColumnModel().getColumn(3).setCellRenderer(wrap);
        tb.getColumnModel().getColumn(4).setCellRenderer(top);
    }

    private void tampilkanNotif(String title, String message, String type) {
        final String bgColor = type.equals("success") ? "#27ae60" : (type.equals("warning") ? "#ff8200" : "#e74c3c");
        String iconName = type.equals("success") ? "success.svg" : "error.svg";
        
        JPanel p = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor); 
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        
        JPanel tp = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]")); tp.setOpaque(false); 
        tp.add(new JLabel(title) {{ putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#ffffff"); }});
        tp.add(new JLabel(message) {{ putClientProperty(FlatClientProperties.STYLE, "font:13; foreground:rgb(240,240,240)"); }});
        
        p.add(new JLabel(icon), "top"); p.add(tp);
        
        JButton b = new JButton("Tutup") {{ 
            setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:" + bgColor + "; font:bold; arc:10; borderWidth:0; margin:5,15,5,15; focusWidth:0"); 
        }};
        b.addActionListener(e -> { Window w = SwingUtilities.getWindowAncestor(b); if(w!=null) w.dispose(); });
        JOptionPane.showOptionDialog(this, p, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{b}, b);
    }

    private void loadPekerjaanAktif() {
        tableModel.setRowCount(0);
        int total = 0, no = 1;
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT s.id_servis, p.nama_pelanggan, pr.merek, pr.tipe_model, s.status " +
                         "FROM data_servis_lengkap s " +
                         "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                         "WHERE s.status NOT IN ('Selesai', 'Batal') ORDER BY s.id_servis ASC";
            ResultSet rs = kon.createStatement().executeQuery(sql);
            while(rs.next()){
                tableModel.addRow(new Object[]{
                    no++, "N" + String.format("%05d", rs.getInt("id_servis")),
                    rs.getString("nama_pelanggan"), rs.getString("merek") + " " + rs.getString("tipe_model"),
                    rs.getString("status"), rs.getInt("id_servis")
                });
                total++;
            }
            lblTotalTugas.setText("(" + total + ")");
        } catch (Exception e) {}
    }

    private void loadDetailPengerjaan(String id) {
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT keluhan_awal, hasil_diagnosa, tindakan_perbaikan, status FROM data_servis_lengkap WHERE id_servis = ?";
            PreparedStatement ps = kon.prepareStatement(sql); ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                txtKeluhan.setText(rs.getString("keluhan_awal"));
                txtDiagnosa.setText(rs.getString("hasil_diagnosa"));
                txtTindakan.setText(rs.getString("tindakan_perbaikan"));
                cbStatus.setSelectedItem(rs.getString("status"));
            }
        } catch (Exception e) {}
    }

    private void updateProgresServis() {
        if (selectedIdServis.isEmpty()) { tampilkanNotif("Peringatan", "Pilih pekerjaan dari tabel terlebih dahulu!", "warning"); return; }
        
        String diagnosa = txtDiagnosa.getText().trim();
        String tindakan = txtTindakan.getText().trim();
        String status = cbStatus.getSelectedItem().toString();
        String idNota = txtID.getText();
        
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            // Update Tabel Utama Servis
            String sql = "UPDATE data_servis_lengkap SET hasil_diagnosa=?, tindakan_perbaikan=?, status=? WHERE id_servis=?";
            PreparedStatement ps = kon.prepareStatement(sql);
            ps.setString(1, diagnosa); ps.setString(2, tindakan); ps.setString(3, status); ps.setString(4, selectedIdServis);
            
            if (ps.executeUpdate() > 0) {
                // Sinkronisasi ke Tabel Progres (Untuk Lacak Real-Time Pelanggan)
                int persen = 10; // Default Antrean
                if(status.equalsIgnoreCase("Proses")) persen = 50;
                else if(status.equalsIgnoreCase("Menunggu Sparepart")) persen = 25;
                else if(status.equalsIgnoreCase("Selesai")) persen = 100;
                else if(status.equalsIgnoreCase("Batal")) persen = 0;

                String sqlProgres = "INSERT INTO tb_progres_servis (id_nota, status_servis, persentase, keterangan) " +
                                    "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status_servis=?, persentase=?, keterangan=?";
                PreparedStatement psP = kon.prepareStatement(sqlProgres);
                psP.setString(1, idNota); psP.setString(2, status); psP.setInt(3, persen); psP.setString(4, tindakan);
                psP.setString(5, status); psP.setInt(6, persen); psP.setString(7, tindakan);
                psP.executeUpdate();
                
                tampilkanNotif("Berhasil", "Progres servis " + idNota + " telah diperbarui!", "success");
                loadPekerjaanAktif(); resetForm();
            }
        } catch (Exception e) { tampilkanNotif("Error", e.getMessage(), "warning"); }
    }

    private void resetForm() {
        selectedIdServis = ""; txtID.setText(""); txtNama.setText(""); txtPerangkat.setText("");
        txtKeluhan.setText(""); txtDiagnosa.setText(""); txtTindakan.setText("");
        cbStatus.setSelectedIndex(0); tablePekerjaan.clearSelection();
    }

    class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() { setLineWrap(true); setWrapStyleWord(true); setFont(new Font("Segoe UI", Font.PLAIN, 14)); setMargin(new java.awt.Insets(10, 10, 10, 10)); setOpaque(true); }
        @Override public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean h, int r, int c) {
            setText(v != null ? v.toString() : ""); if (s) { setBackground(t.getSelectionBackground()); setForeground(t.getSelectionForeground()); } else { setBackground(t.getBackground()); setForeground(t.getForeground()); } return this;
        }
    }
}