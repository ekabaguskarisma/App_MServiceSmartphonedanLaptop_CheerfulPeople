package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import com.mssl.utils.UIHelper;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement; 
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;
import java.awt.event.*;

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

        // MEMANGGIL SCROLL DARI UIHELPER
        JScrollPane scrollKiri = UIHelper.createCustomScroll(panelForm);
        
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

        String[] kolom = {"No.", "ID Nota", "Pelanggan", "Perangkat", "Status", "ID_Asli"};
        tableModel = new DefaultTableModel(kolom, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablePekerjaan = new JTable(tableModel);
        
        // MEMANGGIL STYLING TABEL DARI UIHELPER
        UIHelper.styleTable(tablePekerjaan, TABLE_HEADER_BG, SIDEBAR_MAIN_COLOR);
        
        tablePekerjaan.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablePekerjaan.getColumnModel().getColumn(1).setPreferredWidth(90);
        tablePekerjaan.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablePekerjaan.getColumnModel().getColumn(3).setPreferredWidth(180);
        tablePekerjaan.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        tablePekerjaan.getColumnModel().getColumn(5).setMinWidth(0);
        tablePekerjaan.getColumnModel().getColumn(5).setMaxWidth(0);
        tablePekerjaan.getColumnModel().getColumn(5).setWidth(0);

        DefaultTableCellRenderer top = new DefaultTableCellRenderer();
        top.setVerticalAlignment(SwingConstants.TOP); top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // MEMANGGIL TEXT WRAPPER DARI UIHELPER
        UIHelper.WrapTextRenderer wrap = new UIHelper.WrapTextRenderer();
        tablePekerjaan.getColumnModel().getColumn(0).setCellRenderer(top);
        tablePekerjaan.getColumnModel().getColumn(1).setCellRenderer(top);
        tablePekerjaan.getColumnModel().getColumn(2).setCellRenderer(wrap);
        tablePekerjaan.getColumnModel().getColumn(3).setCellRenderer(wrap);
        tablePekerjaan.getColumnModel().getColumn(4).setCellRenderer(top);

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
        
        txtSearch.addActionListener(e -> {
            if (tablePekerjaan.getRowCount() > 0) {
                tablePekerjaan.setRowSelectionInterval(0, 0);
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

    private void loadPekerjaanAktif() {
        tableModel.setRowCount(0);
        int total = 0, no = 1;
        try (Connection kon = DatabaseConnection.getKoneksi();
             ResultSet rs = kon.createStatement().executeQuery("SELECT s.id_servis, p.nama_pelanggan, pr.merek, pr.tipe_model, s.status FROM data_servis_lengkap s JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat WHERE s.status NOT IN ('Selesai', 'Diambil', 'Batal') ORDER BY s.id_servis ASC")) {
             
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
        try (Connection kon = DatabaseConnection.getKoneksi();
             PreparedStatement ps = kon.prepareStatement("SELECT keluhan_awal, hasil_diagnosa, tindakan_perbaikan, status FROM data_servis_lengkap WHERE id_servis = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    txtKeluhan.setText(rs.getString("keluhan_awal"));
                    txtDiagnosa.setText(rs.getString("hasil_diagnosa"));
                    txtTindakan.setText(rs.getString("tindakan_perbaikan"));
                    cbStatus.setSelectedItem(rs.getString("status"));
                }
            }
        } catch (Exception e) {}
    }

    private void updateProgresServis() {
        if (selectedIdServis.isEmpty()) { UIHelper.tampilkanNotif(this, "Peringatan", "Pilih pekerjaan dari tabel terlebih dahulu!", "warning"); return; }
        
        String diagnosa = txtDiagnosa.getText().trim();
        String tindakan = txtTindakan.getText().trim();
        String status = cbStatus.getSelectedItem().toString();
        String idNota = txtID.getText();
        
        try (Connection kon = DatabaseConnection.getKoneksi()) {
            
            int persen = 10; 
            if(status.equalsIgnoreCase("Proses")) persen = 50;
            else if(status.equalsIgnoreCase("Menunggu Sparepart")) persen = 25;
            else if(status.equalsIgnoreCase("Selesai")) persen = 100;
            else if(status.equalsIgnoreCase("Batal")) persen = 0;

            String sqlCall = "{CALL sp_simpan_progres_teknisi(?, ?, ?, ?, ?)}";
            try (CallableStatement cs = kon.prepareCall(sqlCall)) {
                cs.setInt(1, Integer.parseInt(selectedIdServis));
                cs.setString(2, diagnosa);
                cs.setString(3, tindakan);
                cs.setString(4, status);
                cs.setInt(5, persen);
                
                cs.executeUpdate();
            }
            
            UIHelper.tampilkanNotif(this, "Berhasil", "Progres servis " + idNota + " telah diperbarui!", "success");
            loadPekerjaanAktif(); resetForm();
            
        } catch (Exception e) { 
            UIHelper.tampilkanNotif(this, "Error", "Gagal memanggil Stored Procedure: " + e.getMessage(), "error"); 
        }
    }

    private void resetForm() {
        selectedIdServis = ""; txtID.setText(""); txtNama.setText(""); txtPerangkat.setText("");
        txtKeluhan.setText(""); txtDiagnosa.setText(""); txtTindakan.setText("");
        cbStatus.setSelectedIndex(0); tablePekerjaan.clearSelection();
    }
}