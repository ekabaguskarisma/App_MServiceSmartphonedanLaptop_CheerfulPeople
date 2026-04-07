package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.Form;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import net.miginfocom.swing.MigLayout;

public class FormPengambilan extends Form {

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245); 
    private final Color FINISH_GREEN = new Color(39, 174, 96);

    private JTextField txtIdServis, txtNama, txtPerangkat, txtJasa, txtTotal, txtSearch;
    private JComboBox<String> cbMetode, cbGaransi;
    
    private JTextField txtSpTerpilih;
    private JButton btnCariSp;
    private JSpinner spinQty;
    private int idSpTerpilih = -1;
    private double hargaSpTerpilih = 0.0;
    private int stokSpTerpilih = 0;
    
    private DefaultListModel<String> listModelSp;
    private JList<String> listSparepart;
    private JButton btnTambahSp, btnHapusSp, btnResetSp; 
    
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JButton btnBayar, btnRefresh;
    private String selectedId = "";
    
    class CartItem {
        int idSp; String namaSp; int qty; double hargaSatuan; double subtotal;
        public CartItem(int id, String n, int q, double h, double s) {
            idSp = id; namaSp = n; qty = q; hargaSatuan = h; subtotal = s;
        }
    }
    
    private List<CartItem> keranjang = new ArrayList<>();
    private NumberFormat formatRp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public FormPengambilan() { 
        try {
            init(); 
            loadDataTabel(); 
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat Form: " + e.getMessage(), "Error UI", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void init() {
        // PERBAIKAN: Lebar panel kiri diperbesar menjadi 460! agar isinya tidak terpotong
        setLayout(new MigLayout("fill, insets 25 30 25 30, gap 25", "[460!][fill,grow]", "[fill,grow]"));
        setBackground(APP_BG_COLOR);

        JPanel panelBayar = new JPanel(new MigLayout("wrap, fillx, insets 20 25 20 25", "[fill]", "[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]15[]5[]25[]"));
        panelBayar.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");

        JLabel lblTitle = new JLabel("Pengambilan & Pembayaran"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ACCENT_ORANGE);

        txtIdServis = createReadOnly("ID Servis Otomatis"); 
        txtNama = createReadOnly("Nama Pelanggan"); 
        txtPerangkat = createReadOnly("Unit Perangkat");

        txtSpTerpilih = createReadOnly("Belum ada sparepart dipilih...");
        btnCariSp = new JButton();
        try {
            FlatSVGIcon iconCari = new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16);
            iconCari.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnCariSp.setIcon(iconCari);
        } catch (Exception e) {}
        
        btnCariSp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariSp.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; borderWidth:0");
        btnCariSp.addActionListener(e -> showModalPilihSparepart());

        spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spinQty.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        btnTambahSp = new JButton("+ Tambah");
        btnTambahSp.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold; borderWidth:0");
        btnTambahSp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnHapusSp = new JButton("Hapus"); 
        btnHapusSp.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e74c3c; foreground:#ffffff; font:bold; borderWidth:0");
        btnHapusSp.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnResetSp = new JButton("Reset"); 
        btnResetSp.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#95a5a6; foreground:#ffffff; font:bold; borderWidth:0");
        btnResetSp.setCursor(new Cursor(Cursor.HAND_CURSOR));

        listModelSp = new DefaultListModel<>();
        listSparepart = new JList<>(listModelSp);
        listSparepart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listSparepart.putClientProperty(FlatClientProperties.STYLE, "background:#f5f6fa; foreground:#666666; selectionBackground:tint(@accentColor, 80%); selectionForeground:#000000");
        
        JScrollPane scrollListSp = new JScrollPane(listSparepart);
        scrollListSp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); 
        scrollListSp.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        scrollListSp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        txtJasa = new JTextField("0"); 
        txtJasa.putClientProperty(FlatClientProperties.STYLE, "arc:10"); 
        txtJasa.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtJasa.addKeyListener(new KeyAdapter() { 
            public void keyTyped(KeyEvent evt) { if (!Character.isDigit(evt.getKeyChar())) evt.consume(); } 
            public void keyReleased(KeyEvent evt) { hitungTotal(); }
        });

        cbGaransi = new JComboBox<>(new String[]{"Tidak Garansi", "1 Minggu", "2 Minggu", "1 Bulan", "3 Bulan", "6 Bulan"}); 
        cbGaransi.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        cbMetode = new JComboBox<>(new String[]{"Tunai", "Transfer Bank", "QRIS", "E-Wallet"}); 
        cbMetode.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        txtTotal = new JTextField("Rp 0"); 
        txtTotal.setEditable(false); 
        txtTotal.setHorizontalAlignment(JTextField.RIGHT); 
        txtTotal.setFont(new Font("Segoe UI", Font.BOLD, 32)); 
        txtTotal.setForeground(FINISH_GREEN); 
        txtTotal.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:10,15,10,15; borderWidth:0; background:#E6FFE6");

        btnBayar = new JButton("Selesaikan Transaksi"); 
        btnBayar.setBackground(FINISH_GREEN); 
        btnBayar.setForeground(Color.WHITE); 
        btnBayar.setEnabled(false); 
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBayar.putClientProperty(FlatClientProperties.STYLE, "arc:10; font:bold +2; borderWidth:0");

        Font fL = new Font("Segoe UI", Font.BOLD, 12);
        
        panelBayar.add(lblTitle, "gapbottom 10");
        
        JLabel lbl1 = new JLabel("ID SERVIS"); lbl1.setFont(fL); lbl1.setForeground(TEXT_MUTED);
        panelBayar.add(lbl1); panelBayar.add(txtIdServis, "h 38!");
        
        JLabel lbl2 = new JLabel("NAMA PELANGGAN"); lbl2.setFont(fL); lbl2.setForeground(TEXT_MUTED);
        panelBayar.add(lbl2); panelBayar.add(txtNama, "h 38!");
        
        JLabel lbl3 = new JLabel("PERANGKAT"); lbl3.setFont(fL); lbl3.setForeground(TEXT_MUTED);
        panelBayar.add(lbl3); panelBayar.add(txtPerangkat, "h 38!");
        
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 235));
        panelBayar.add(sep, "growx, gapy 15 15"); // Diberi growx agar full lebar
        
        JLabel lbl4 = new JLabel("TAMBAH SPAREPART (CARI & ISI JUMLAH)"); lbl4.setFont(fL); lbl4.setForeground(TEXT_MUTED);
        panelBayar.add(lbl4); 
        
        JPanel pnlPilihSp = new JPanel(new MigLayout("insets 0", "[grow, fill][]", "[]"));
        pnlPilihSp.setOpaque(false);
        pnlPilihSp.add(txtSpTerpilih, "h 38!");
        pnlPilihSp.add(btnCariSp, "w 45!, h 38!");
        panelBayar.add(pnlPilihSp);
        
        JPanel pnlAksiSp = new JPanel(new MigLayout("insets 0, gapx 8", "[][70!][grow, fill][fill][fill]", "[]"));
        pnlAksiSp.setOpaque(false);
        pnlAksiSp.add(new JLabel("Jumlah:"));
        pnlAksiSp.add(spinQty, "h 38!");
        pnlAksiSp.add(btnTambahSp, "h 38!");
        pnlAksiSp.add(btnHapusSp, "h 38!");
        pnlAksiSp.add(btnResetSp, "h 38!");
        panelBayar.add(pnlAksiSp);
        
        panelBayar.add(scrollListSp, "h 90!");

        JLabel lbl5 = new JLabel("BIAYA JASA (Rp)"); lbl5.setFont(fL); lbl5.setForeground(TEXT_MUTED);
        panelBayar.add(lbl5); panelBayar.add(txtJasa, "h 38!");
        
        JLabel lbl6 = new JLabel("MASA GARANSI"); lbl6.setFont(fL); lbl6.setForeground(TEXT_MUTED);
        panelBayar.add(lbl6); panelBayar.add(cbGaransi, "h 38!");

        JLabel lbl7 = new JLabel("METODE PEMBAYARAN"); lbl7.setFont(fL); lbl7.setForeground(TEXT_MUTED);
        panelBayar.add(lbl7); panelBayar.add(cbMetode, "h 38!");
        
        JLabel lbl8 = new JLabel("TOTAL BAYAR"); lbl8.setFont(new Font("Segoe UI", Font.BOLD, 14)); lbl8.setForeground(SIDEBAR_MAIN_COLOR);
        panelBayar.add(lbl8, "gapy 15"); 
        panelBayar.add(txtTotal, "h 70!"); 
        panelBayar.add(btnBayar, "h 45!, gapy 15");

        JScrollPane scrollKiri = createCustomScroll(panelBayar);
        
        // TABEL DATA NOTA SELESAI
        JPanel panelData = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill]", "[][fill,grow]"));
        panelData.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");

        JPanel pHeaderTable = new JPanel(new MigLayout("insets 0, fillx", "[][grow][][]", "[]")); 
        pHeaderTable.setOpaque(false);

        JPanel pnlTitleData = new JPanel(new MigLayout("wrap, insets 0, gapy 0", "[fill]", "[][]"));
        pnlTitleData.setOpaque(false);
        
        JLabel lblTitleData = new JLabel("Unit Siap Diambil");
        lblTitleData.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitleData.setForeground(SIDEBAR_MAIN_COLOR);
        
        JLabel lblSubTitleData = new JLabel("(Status Telah Selesai)");
        lblSubTitleData.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubTitleData.setForeground(TEXT_MUTED);
        
        pnlTitleData.add(lblTitleData, "gapbottom -2");
        pnlTitleData.add(lblSubTitleData);

        txtSearch = new JTextField(); 
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Pelanggan/Nota..."); 
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true); 
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:15"); 
        
        btnRefresh = new JButton("Refresh Data"); 
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold; margin:0,15,0,15; borderWidth:0; focusWidth:0");

        pHeaderTable.add(pnlTitleData); 
        pHeaderTable.add(new JLabel(""), "grow"); 
        pHeaderTable.add(txtSearch, "wmin 100, wmax 200, growx, h 38!"); 
        pHeaderTable.add(btnRefresh, "h 38!");

        tableModel = new DefaultTableModel(new String[]{"ID Servis", "Pelanggan", "Perangkat", "Status"}, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        table = new JTable(tableModel); 
        styleTable(table);
        
        table.getColumnModel().getColumn(0).setMaxWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        table.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(topLeftRenderer);

        WrapTextRenderer textWrapper = new WrapTextRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(textWrapper); 
        table.getColumnModel().getColumn(2).setCellRenderer(textWrapper); 

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        
        rowSorter = new TableRowSorter<>(tableModel); 
        table.setRowSorter(rowSorter);

        panelData.add(pHeaderTable, "gapbottom 15");
        
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        scrollTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelData.add(scrollTable, "grow");

        add(scrollKiri, "grow"); 
        add(panelData, "grow");

        btnRefresh.addActionListener(e -> { loadDataTabel(); txtSearch.setText(""); });
        table.getSelectionModel().addListSelectionListener(e -> { if (table.getSelectedRow() != -1) pilihData(); });
        
        btnTambahSp.addActionListener(e -> tambahKeKeranjang());
        btnHapusSp.addActionListener(e -> hapusDariKeranjang());
        btnResetSp.addActionListener(e -> resetKeranjang());
        btnBayar.addActionListener(e -> aksiBayar());
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() { 
            public void insertUpdate(DocumentEvent e) { liveSearch(); } 
            public void removeUpdate(DocumentEvent e) { liveSearch(); } 
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });
    }

    private JTextField createReadOnly(String p) { 
        JTextField tf = new JTextField(); 
        tf.setEditable(false); 
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, p);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#f5f6fa; foreground:#666666"); 
        return tf; 
    }
    
    private JScrollPane createCustomScroll(JPanel p) { 
        JScrollPane s = new JScrollPane(p); 
        s.setBorder(null); 
        s.setOpaque(false); 
        s.getViewport().setOpaque(false); 
        s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        s.getVerticalScrollBar().setUnitIncrement(15); 
        s.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;"); 
        return s; 
    }

    private void styleTable(JTable tb) {
        tb.setBackground(CARD_BG_COLOR);
        tb.setForeground(new Color(60, 60, 60));
        tb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tb.setRowHeight(60); 
        tb.setShowGrid(false);
        tb.setShowHorizontalLines(true);
        tb.setGridColor(new Color(230, 230, 235));
        tb.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 85%); selectionForeground:#000000; selectionArc:10");

        JTableHeader header = tb.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(false);
        header.setBackground(TABLE_HEADER_BG); 
        header.setForeground(SIDEBAR_MAIN_COLOR); 
    }

    private void loadDataTabel() {
        tableModel.setRowCount(0);
        try {
            Connection kon = DatabaseConnection.getKoneksi(); 
            String sql = "SELECT s.id_servis, p.nama_pelanggan, pr.tipe_model, s.status FROM data_servis_lengkap s JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat WHERE s.status = 'Selesai' AND s.id_servis NOT IN (SELECT id_servis FROM data_pengambilan)";
            ResultSet rs = kon.createStatement().executeQuery(sql);
            while (rs.next()) { 
                tableModel.addRow(new Object[]{String.format("N%05d", rs.getInt("id_servis")), rs.getString("nama_pelanggan"), rs.getString("tipe_model"), rs.getString("status")}); 
            }
        } catch (Exception e) {}
    }

    private void pilihData() {
        int row = table.getSelectedRow();
        if(row != -1) {
            int mR = table.convertRowIndexToModel(row); 
            selectedId = tableModel.getValueAt(mR, 0).toString().replace("N", "");
            txtIdServis.setText(tableModel.getValueAt(mR, 0).toString()); 
            txtNama.setText(tableModel.getValueAt(mR, 1).toString()); 
            txtPerangkat.setText(tableModel.getValueAt(mR, 2).toString()); 
            btnBayar.setEnabled(true); 
            hitungTotal();
        }
    }

    private void showModalPilihSparepart() {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog d = new JDialog(window instanceof JFrame ? (JFrame) window : null, "Pilih Sparepart", Dialog.ModalityType.APPLICATION_MODAL);
        
        d.setSize(650, 500); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill, grow]", "[]15[fill, grow][]"));
        p.setBackground(CARD_BG_COLOR);
        
        JLabel lblHeader = new JLabel("Pilih Sparepart Tersedia");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20)); lblHeader.setForeground(SIDEBAR_MAIN_COLOR);
        
        JTextField tCari = new JTextField(); tCari.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari Nama Sparepart...");
        tCari.putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:5,10,5,10");
        
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Nama Sparepart", "Stok", "Harga Jual"}, 0) { 
            @Override public boolean isCellEditable(int r, int cl) { return false; } 
        };
        JTable t = new JTable(m); 
        t.setRowHeight(40); t.setShowGrid(false); t.setShowHorizontalLines(true); t.setGridColor(new Color(230, 230, 235));
        t.getTableHeader().setBackground(TABLE_HEADER_BG); t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        t.getColumnModel().getColumn(0).setMaxWidth(60); 
        t.getColumnModel().getColumn(2).setMaxWidth(80);
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<>(m); t.setRowSorter(s);
        
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT id_sparepart, nama_sparepart, stok, harga_jual FROM data_sparepart WHERE stok > 0 ORDER BY nama_sparepart ASC";
            ResultSet rs = kon.createStatement().executeQuery(sql);
            while(rs.next()){ 
                m.addRow(new Object[]{rs.getInt("id_sparepart"), rs.getString("nama_sparepart"), rs.getInt("stok"), rs.getDouble("harga_jual")}); 
            }
        } catch (Exception e) {}
        
        tCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { s.setRowFilter(RowFilter.regexFilter("(?i)" + tCari.getText())); }
            public void removeUpdate(DocumentEvent e) { s.setRowFilter(RowFilter.regexFilter("(?i)" + tCari.getText())); }
            public void changedUpdate(DocumentEvent e) { s.setRowFilter(RowFilter.regexFilter("(?i)" + tCari.getText())); }
        });
        
        JButton bPilih = new JButton("Gunakan Sparepart Ini");
        bPilih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bPilih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold; borderWidth:0; h:40");
        
        Runnable actPilih = () -> {
            int row = t.getSelectedRow();
            if(row >= 0) {
                int mRow = t.convertRowIndexToModel(row);
                idSpTerpilih = (int) m.getValueAt(mRow, 0);
                String nama = m.getValueAt(mRow, 1).toString();
                stokSpTerpilih = (int) m.getValueAt(mRow, 2);
                hargaSpTerpilih = (double) m.getValueAt(mRow, 3);
                
                txtSpTerpilih.setText(nama);
                spinQty.setModel(new SpinnerNumberModel(1, 1, stokSpTerpilih, 1));
                d.dispose();
            }
        };
        bPilih.addActionListener(e -> actPilih.run());
        t.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if(e.getClickCount() == 2) actPilih.run(); } });
        
        p.add(lblHeader); p.add(tCari, "h 38!"); p.add(new JScrollPane(t)); p.add(bPilih);
        d.add(p); d.setVisible(true);
    }

    private void tambahKeKeranjang() {
        if(idSpTerpilih == -1) {
            JOptionPane.showMessageDialog(this, "Silakan klik tombol Cari dan pilih sparepart terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int qty = (int) spinQty.getValue();
        double subtotal = qty * hargaSpTerpilih;
        String nama = txtSpTerpilih.getText();
        
        keranjang.add(new CartItem(idSpTerpilih, nama, qty, hargaSpTerpilih, subtotal));
        
        String txtList = qty + "x " + nama + " (Rp" + formatRp.format(subtotal).replace("Rp", "").replace(",00", "") + ")";
        listModelSp.addElement(txtList); 
        
        idSpTerpilih = -1;
        hargaSpTerpilih = 0;
        stokSpTerpilih = 0;
        txtSpTerpilih.setText("Belum ada sparepart dipilih...");
        spinQty.setModel(new SpinnerNumberModel(1, 1, 999, 1));
        
        hitungTotal();
    }
    
    private void hapusDariKeranjang() {
        int selectedIndex = listSparepart.getSelectedIndex();
        if (selectedIndex != -1) {
            keranjang.remove(selectedIndex);
            listModelSp.remove(selectedIndex); 
            hitungTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Klik daftar sparepart yang ingin dihapus terlebih dahulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetKeranjang() {
        keranjang.clear();
        listModelSp.clear();
        
        idSpTerpilih = -1;
        txtSpTerpilih.setText("Belum ada sparepart dipilih...");
        spinQty.setModel(new SpinnerNumberModel(1, 1, 999, 1));
        
        hitungTotal();
    }

    private void hitungTotal() {
        try {
            double jasa = txtJasa.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtJasa.getText().trim()); 
            
            double totalSp = 0;
            for(CartItem item : keranjang) { totalSp += item.subtotal; }
            
            double totalAkhir = jasa + totalSp;
            txtTotal.setText(formatRp.format(totalAkhir));
        } catch (Exception e) { 
            txtTotal.setText("Rp 0"); 
        }
    }

    private void aksiBayar() {
        try {
            Connection kon = DatabaseConnection.getKoneksi(); 
            kon.setAutoCommit(false); 
            
            Integer firstIdSp = keranjang.isEmpty() ? 0 : keranjang.get(0).idSp;
            
            String totalStr = txtTotal.getText().replaceAll("[^0-9]", ""); 
            double totalAngka = totalStr.isEmpty() ? 0 : Double.parseDouble(totalStr) / 100;
            String idNotaBaru = "N" + String.format("%05d", Integer.parseInt(selectedId));
            
            // MASUKKAN KE TRANSAKSI KASIR
            PreparedStatement ps = kon.prepareStatement("INSERT INTO data_pengambilan (id_servis, tgl_ambil, id_sparepart, biaya_jasa, total_bayar, metode_bayar) VALUES (?, CURDATE(), ?, ?, ?, ?)");
            ps.setString(1, selectedId); 
            if (firstIdSp == 0) { ps.setNull(2, java.sql.Types.INTEGER); } else { ps.setInt(2, firstIdSp); }
            ps.setDouble(3, Double.parseDouble(txtJasa.getText())); 
            ps.setDouble(4, totalAngka); 
            ps.setString(5, cbMetode.getSelectedItem().toString()); 
            ps.executeUpdate();
            
            // MENGAMBIL SELURUH DATA LENGKAP UNTUK DIBUATKAN NOTA
            String sqlAmbil = "SELECT s.tgl_masuk, p.nama_pelanggan, p.no_whatsapp, pr.merek, pr.tipe_model, pr.kelengkapan, s.keluhan_awal, s.hasil_diagnosa, s.tindakan_perbaikan " +
                              "FROM data_servis_lengkap s " +
                              "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                              "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                              "WHERE s.id_servis = ?";
            PreparedStatement psAmbil = kon.prepareStatement(sqlAmbil);
            psAmbil.setString(1, selectedId);
            ResultSet rsAmbil = psAmbil.executeQuery();
            
            if (rsAmbil.next()) {
                // CETAK KE tb_nota AGAR BISA DILIHAT PELANGGAN
                String sqlNota = "INSERT INTO tb_nota (id_nota, id_servis, tanggal_masuk, tanggal_selesai, nama_pelanggan, no_telepon, tipe_perangkat, kelengkapan, keluhan, diagnosa, tindakan, total_biaya, status_pembayaran, masa_garansi) VALUES (?, ?, ?, CURDATE(), ?, ?, ?, ?, ?, ?, ?, ?, 'Lunas', ?)";
                PreparedStatement psNota = kon.prepareStatement(sqlNota);
                psNota.setString(1, idNotaBaru);
                psNota.setInt(2, Integer.parseInt(selectedId));
                psNota.setString(3, rsAmbil.getString("tgl_masuk"));
                psNota.setString(4, rsAmbil.getString("nama_pelanggan"));
                psNota.setString(5, rsAmbil.getString("no_whatsapp"));
                psNota.setString(6, rsAmbil.getString("merek") + " " + rsAmbil.getString("tipe_model"));
                psNota.setString(7, rsAmbil.getString("kelengkapan"));
                psNota.setString(8, rsAmbil.getString("keluhan_awal"));
                psNota.setString(9, rsAmbil.getString("hasil_diagnosa"));
                psNota.setString(10, rsAmbil.getString("tindakan_perbaikan"));
                psNota.setDouble(11, totalAngka);
                psNota.setString(12, cbGaransi.getSelectedItem().toString());
                psNota.executeUpdate();
            }
            
            // POTONG STOK SPAREPART
            for (CartItem item : keranjang) {
                PreparedStatement psStok = kon.prepareStatement("UPDATE data_sparepart SET stok = stok - ? WHERE id_sparepart = ?"); 
                psStok.setInt(1, item.qty); 
                psStok.setInt(2, item.idSp); 
                psStok.executeUpdate(); 
            }
            
            // UPDATE STATUS JADI SELESAI
            PreparedStatement psUpdStatus = kon.prepareStatement("UPDATE data_servis_lengkap SET status = 'Selesai' WHERE id_servis = ?");
            psUpdStatus.setString(1, selectedId);
            psUpdStatus.executeUpdate();
            
            kon.commit(); 
            JOptionPane.showMessageDialog(this, "Pembayaran Berhasil! Nota Pelanggan diterbitkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            loadDataTabel(); 
            resetKeranjang();
            
            txtIdServis.setText(""); txtNama.setText(""); txtPerangkat.setText(""); 
            txtJasa.setText("0"); txtTotal.setText("Rp 0"); 
            cbMetode.setSelectedIndex(0); cbGaransi.setSelectedIndex(0);
            btnBayar.setEnabled(false);
            
        } catch (Exception e) {
            try { DatabaseConnection.getKoneksi().rollback(); } catch (Exception ex) {}
            JOptionPane.showMessageDialog(this, "Gagal memproses pembayaran: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE); 
        }
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