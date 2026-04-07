package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mssl.koneksi.DatabaseConnection;
import com.mssl.main.FormManager;
import com.mssl.main.Form;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;

public class FormPenerimaanServis extends Form {

    private final Color APP_BG_COLOR = new Color(245, 246, 250); 
    private final Color CARD_BG_COLOR = Color.WHITE;
    private final Color SIDEBAR_MAIN_COLOR = new Color(40, 45, 60); 
    private final Color TEXT_MUTED = new Color(130, 135, 150);
    private final Color ACCENT_ORANGE = new Color(255, 130, 0); 
    private final Color TABLE_HEADER_BG = new Color(235, 238, 245); 

    private JTextField txtPelanggan, txtMerek, txtTipeModel, txtKetPerangkat, txtKelengkapan, txtSearchAntrean;
    private JButton btnCariPelanggan, btnSimpan, btnBersih, btnRefresh;
    private JComboBox<String> cbJenisPerangkat;
    private JTextArea txtKeluhan;
    private int idPelangganTerpilih = -1;
    
    private JTable tableAntrean;
    private DefaultTableModel modelAntrean;
    private TableRowSorter<DefaultTableModel> sorterAntrean;

    public FormPenerimaanServis() {
        init();
        loadAntreanHariIni(); 
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 25 30 25 30, gap 25", "[400!][fill,grow,0]", "[fill,grow,0]"));
        setBackground(APP_BG_COLOR);

        JPanel panelForm = new JPanel(new MigLayout("wrap, fillx, insets 20", "[fill]", "[]15[]5[]15[]5[]5[]5[]5[]15[]5[]15[]5[]20[]"));
        panelForm.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");

        JLabel lblTitle = new JLabel("Penerimaan Servis Baru");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ACCENT_ORANGE);

        txtPelanggan = new JTextField();
        txtPelanggan.setEditable(false);
        txtPelanggan.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Klik tombol cari...");
        txtPelanggan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#f5f6fa");
        
        FlatSVGIcon iconCari = new FlatSVGIcon("com/mssl/icon/search.svg", 16, 16);
        iconCari.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        btnCariPelanggan = new JButton(iconCari);
        btnCariPelanggan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariPelanggan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; borderWidth:0");
        btnCariPelanggan.addActionListener(e -> showModalPelanggan());

        cbJenisPerangkat = new JComboBox<>(new String[]{"Handphone (HP)", "Laptop", "Tablet", "PC / Desktop", "Lainnya"});
        cbJenisPerangkat.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        txtMerek = createInput("Merek (Samsung, Oppo, dll)");
        txtTipeModel = createInput("Tipe (Galaxy S22, A54, dll)");
        txtKetPerangkat = createInput("Warna / IMEI / SN");
        txtKelengkapan = createInput("Unit, Charger, Dus, dll");

        txtKeluhan = new JTextArea(4, 20);
        txtKeluhan.setLineWrap(true);
        txtKeluhan.setWrapStyleWord(true);
        txtKeluhan.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        btnSimpan = new JButton("Simpan Antrean");
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#ff8200; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0");
        
        btnBersih = new JButton("Batal");
        btnBersih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBersih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#e0e0e0; font:bold; borderWidth:0; focusWidth:0");

        Font fLabel = new Font("Segoe UI", Font.BOLD, 12);
        panelForm.add(lblTitle, "gapbottom 10");
        panelForm.add(new JLabel("PEMILIK UNIT") {{ setFont(fLabel); setForeground(TEXT_MUTED); }});
        panelForm.add(txtPelanggan, "split 2, growx, h 38!"); panelForm.add(btnCariPelanggan, "w 40!, h 38!");
        
        panelForm.add(new JLabel("DETAIL PERANGKAT") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}, "gapy 10");
        panelForm.add(cbJenisPerangkat, "h 38!");
        panelForm.add(txtMerek, "h 38!"); panelForm.add(txtTipeModel, "h 38!"); panelForm.add(txtKetPerangkat, "h 38!");
        
        panelForm.add(new JLabel("KELENGKAPAN & KELUHAN") {{ setFont(fLabel); setForeground(TEXT_MUTED); }}, "gapy 10");
        panelForm.add(txtKelengkapan, "h 38!");
        panelForm.add(new JScrollPane(txtKeluhan), "h 100!");
        
        JPanel pBtn = new JPanel(new MigLayout("insets 0, gapx 10", "[grow][grow]", "[45!]"));
        pBtn.setOpaque(false);
        pBtn.add(btnSimpan, "grow"); pBtn.add(btnBersih, "grow");
        panelForm.add(pBtn, "gapy 15");

        JScrollPane scrollKiri = createCustomScroll(panelForm);

        JPanel panelData = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill]", "[][fill,grow]"));
        panelData.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");

        JPanel pHeaderTable = new JPanel(new MigLayout("insets 0, fillx", "[][grow, right][]", "[]"));
        pHeaderTable.setOpaque(false);
        
        JPanel pnlTitleData = new JPanel(new MigLayout("wrap, insets 0, gapy 0", "[fill]", "[][]"));
        pnlTitleData.setOpaque(false);
        JLabel lblTitleData = new JLabel("Antrean Masuk");
        lblTitleData.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitleData.setForeground(SIDEBAR_MAIN_COLOR);
        JLabel lblSubTitleData = new JLabel("(Data Terdaftar Hari Ini)");
        lblSubTitleData.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubTitleData.setForeground(TEXT_MUTED);
        pnlTitleData.add(lblTitleData, "gapbottom -2");
        pnlTitleData.add(lblSubTitleData);

        txtSearchAntrean = createInput("Cari No. Nota/Pelanggan...");
        btnRefresh = new JButton("Refresh Data");
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold; margin:0,15,0,15; borderWidth:0");

        pHeaderTable.add(pnlTitleData);
        pHeaderTable.add(txtSearchAntrean, "wmin 100, wmax 200, h 38!");
        pHeaderTable.add(btnRefresh, "h 38!");

        modelAntrean = new DefaultTableModel(new String[]{"No. Nota", "Pelanggan", "Perangkat", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAntrean = new JTable(modelAntrean);
        styleTable(tableAntrean);
        tableAntrean.getColumnModel().getColumn(0).setMaxWidth(90);
        
        WrapTextRenderer textWrapper = new WrapTextRenderer();
        tableAntrean.getColumnModel().getColumn(1).setCellRenderer(textWrapper);
        tableAntrean.getColumnModel().getColumn(2).setCellRenderer(textWrapper);
        
        DefaultTableCellRenderer topLeftRenderer = new DefaultTableCellRenderer();
        topLeftRenderer.setVerticalAlignment(SwingConstants.TOP);
        topLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        topLeftRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tableAntrean.getColumnModel().getColumn(0).setCellRenderer(topLeftRenderer);
        tableAntrean.getColumnModel().getColumn(3).setCellRenderer(topLeftRenderer);

        sorterAntrean = new TableRowSorter<>(modelAntrean);
        tableAntrean.setRowSorter(sorterAntrean);

        panelData.add(pHeaderTable, "gapbottom 15");
        panelData.add(new JScrollPane(tableAntrean), "grow, w 0:100%, h 0:100%");

        add(scrollKiri, "grow, w 0:100%, h 0:100%");
        add(panelData, "grow, w 0:100%, h 0:100%");
        
        btnSimpan.addActionListener(e -> prosesSimpanTerpadu());
        btnBersih.addActionListener(e -> resetForm());
        btnRefresh.addActionListener(e -> loadAntreanHariIni());
        txtSearchAntrean.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });
    }

    private JTextField createInput(String p) {
        JTextField tf = new JTextField();
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, p);
        tf.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc:10");
        return tf;
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

    private void styleTable(JTable tb) {
        tb.setBackground(CARD_BG_COLOR);
        tb.setRowHeight(60); 
        tb.setShowGrid(false);
        tb.setShowHorizontalLines(true);
        tb.setGridColor(new Color(230, 230, 235));
        tb.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 85%); selectionForeground:#000000; selectionArc:10");
        JTableHeader h = tb.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setOpaque(false);
        h.setBackground(TABLE_HEADER_BG); 
        h.setForeground(SIDEBAR_MAIN_COLOR);
        ((DefaultTableCellRenderer)h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private void showModalPelanggan() {
        JDialog d = new JDialog(FormManager.getJFrame(), "Pilih Pelanggan", true);
        d.setSize(650, 500); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new MigLayout("wrap, fill, insets 25", "[fill, grow]", "[]15[fill, grow][]"));
        p.setBackground(CARD_BG_COLOR);
        
        JLabel lblHeader = new JLabel("Pilih Pelanggan");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(SIDEBAR_MAIN_COLOR);
        
        JTextField tCari = createInput("Cari Nama atau No. WhatsApp...");
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Nama Pelanggan", "No. WhatsApp"}, 0) { @Override public boolean isCellEditable(int r, int cl) { return false; } };
        JTable t = new JTable(m); styleTable(t);
        t.setRowHeight(45);
        t.getColumnModel().getColumn(0).setMaxWidth(80);
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<>(m); t.setRowSorter(s);
        
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            ResultSet rs = kon.createStatement().executeQuery("SELECT id_pelanggan, nama_pelanggan, no_whatsapp FROM data_pelanggan ORDER BY id_pelanggan DESC");
            while(rs.next()){ m.addRow(new Object[]{"PLG-" + rs.getInt("id_pelanggan"), rs.getString("nama_pelanggan"), rs.getString("no_whatsapp")}); }
        } catch (Exception e) {}
        
        tCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { s.setRowFilter(RowFilter.regexFilter("(?i)" + tCari.getText())); }
            public void removeUpdate(DocumentEvent e) { s.setRowFilter(RowFilter.regexFilter("(?i)" + tCari.getText())); }
            public void changedUpdate(DocumentEvent e) { s.setRowFilter(RowFilter.regexFilter("(?i)" + tCari.getText())); }
        });
        
        JButton bPilih = new JButton("Gunakan Pelanggan Ini");
        bPilih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bPilih.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold; borderWidth:0");
        
        Runnable aPilih = () -> {
            int row = t.getSelectedRow();
            if(row >= 0) {
                int mRow = t.convertRowIndexToModel(row);
                idPelangganTerpilih = Integer.parseInt(m.getValueAt(mRow, 0).toString().replace("PLG-", ""));
                txtPelanggan.setText(m.getValueAt(mRow, 1).toString());
                d.dispose();
            }
        };
        bPilih.addActionListener(e -> aPilih.run());
        t.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if(e.getClickCount() == 2) aPilih.run(); } });
        
        p.add(lblHeader); p.add(tCari, "h 38!"); p.add(new JScrollPane(t)); p.add(bPilih, "h 45!");
        d.add(p); d.setVisible(true);
    }

    private void prosesSimpanTerpadu() {
        if (idPelangganTerpilih == -1 || txtMerek.getText().isEmpty() || txtTipeModel.getText().isEmpty() || txtKeluhan.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lengkapi Pemilik Unit, Detail Perangkat, dan Keluhan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String namaPelanggan = txtPelanggan.getText().trim();
        String perangkatLengkap = txtMerek.getText().trim() + " " + txtTipeModel.getText().trim();
        
        Connection kon = null;
        try {
            kon = DatabaseConnection.getKoneksi();
            kon.setAutoCommit(false);
            
            // Simpan ke data_perangkat 
            PreparedStatement psUnit = kon.prepareStatement("INSERT INTO data_perangkat (jenis_perangkat, merek, tipe_model, warna_imei_sn, kelengkapan) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            psUnit.setString(1, cbJenisPerangkat.getSelectedItem().toString());
            psUnit.setString(2, txtMerek.getText().trim());
            psUnit.setString(3, txtTipeModel.getText().trim());
            psUnit.setString(4, txtKetPerangkat.getText().trim());
            psUnit.setString(5, txtKelengkapan.getText().trim());
            psUnit.executeUpdate();
            
            ResultSet rsKeys = psUnit.getGeneratedKeys();
            int idPrg = 0; if (rsKeys.next()) idPrg = rsKeys.getInt(1);
            
            // Simpan ke data_servis_lengkap
            PreparedStatement psServis = kon.prepareStatement("INSERT INTO data_servis_lengkap (id_pelanggan, id_perangkat, keluhan_awal, status, tgl_masuk) VALUES (?, ?, ?, 'Antrean', NOW())", Statement.RETURN_GENERATED_KEYS);
            psServis.setInt(1, idPelangganTerpilih); 
            psServis.setInt(2, idPrg);
            psServis.setString(3, txtKeluhan.getText().trim());
            psServis.executeUpdate();
            
            // Generate Nomor Nota Baru dari ID Servis
            ResultSet rsServis = psServis.getGeneratedKeys();
            int idServis = 0; if (rsServis.next()) idServis = rsServis.getInt(1);
            String noNotaBaru = "N" + String.format("%05d", idServis);
            
            // Ambil Nomor HP Pelanggan
            String noHp = "";
            PreparedStatement psHp = kon.prepareStatement("SELECT no_whatsapp FROM data_pelanggan WHERE id_pelanggan = ?");
            psHp.setInt(1, idPelangganTerpilih);
            ResultSet rsHp = psHp.executeQuery();
            if(rsHp.next()) noHp = rsHp.getString("no_whatsapp");
            
            // OTOMATIS BUAT AKUN PELANGGAN
            PreparedStatement psAkun = kon.prepareStatement("INSERT INTO data_pengguna (username, password, nama_lengkap, role, id_pelanggan) VALUES (?, ?, ?, 'Pelanggan', ?)");
            psAkun.setString(1, noNotaBaru);
            psAkun.setString(2, noHp);
            psAkun.setString(3, namaPelanggan);
            psAkun.setInt(4, idPelangganTerpilih);
            psAkun.executeUpdate();
            
            kon.commit();
            
            // Memanggil Pop-Up Custom dengan Opsi Cetak
            tampilkanTiketPendaftaran(noNotaBaru, noHp, namaPelanggan, perangkatLengkap);
            
            loadAntreanHariIni(); 
            resetForm();
            
        } catch (Exception e) {
            try { if(kon != null) kon.rollback(); } catch (Exception ex) {}
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }
    
    // FITUR CETAK POP-UP NOTA TIKET YANG ELEGAN
    private void tampilkanTiketPendaftaran(String noNota, String noHp, String nama, String perangkat) {
        JPanel pnl = new JPanel(new MigLayout("wrap, fillx, insets 20 30 20 30", "[center]", "[]10[]5[]20[fill]20[]"));
        pnl.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:#ffffff");
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/success.svg", 50, 50);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> new Color(39, 174, 96))); 
        JLabel lblIcon = new JLabel(icon);

        JLabel lblTitle = new JLabel("Pendaftaran Berhasil!");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(SIDEBAR_MAIN_COLOR); 

        JLabel lblDesc = new JLabel("<html><div style='text-align:center;'>Berikan informasi akun ini kepada pelanggan<br>untuk melacak progres secara real-time.</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(TEXT_MUTED);
        
        JPanel pnlTicket = new JPanel(new MigLayout("wrap 2, fillx, insets 15 25 15 25", "[][grow, right]", "[]10[]15[]15[]10[]"));
        pnlTicket.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:#f5f6fa; border:1,solid,#e2e8f0");

        Font fLabel = new Font("Segoe UI", Font.BOLD, 12);
        Font fValue = new Font("Segoe UI", Font.BOLD, 15);
        Font fValueHighlight = new Font("Segoe UI", Font.BOLD, 18);
        Color cLabel = TEXT_MUTED;
        Color cValue = SIDEBAR_MAIN_COLOR;
        Color cHighlight = ACCENT_ORANGE; 

        pnlTicket.add(new JLabel("Nama Pelanggan") {{setFont(fLabel); setForeground(cLabel);}});
        pnlTicket.add(new JLabel(nama) {{setFont(fValue); setForeground(cValue);}});
        
        pnlTicket.add(new JLabel("Unit Perangkat") {{setFont(fLabel); setForeground(cLabel);}});
        pnlTicket.add(new JLabel(perangkat) {{setFont(fValue); setForeground(cValue);}});
        
        pnlTicket.add(new JSeparator(), "span 2, growx");

        pnlTicket.add(new JLabel("USERNAME LOGIN") {{setFont(fLabel); setForeground(cLabel);}});
        pnlTicket.add(new JLabel(noNota) {{setFont(fValueHighlight); setForeground(cHighlight);}});
        
        pnlTicket.add(new JLabel("PASSWORD LOGIN") {{setFont(fLabel); setForeground(cLabel);}});
        pnlTicket.add(new JLabel(noHp) {{setFont(fValueHighlight); setForeground(cHighlight);}});

        pnl.add(lblIcon);
        pnl.add(lblTitle);
        pnl.add(lblDesc);
        pnl.add(pnlTicket, "growx");
        
        JButton btnCetak = new JButton("Cetak Tiket (PDF)");
        btnCetak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCetak.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#282d3c; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0; margin:8,20,8,20");
        
        JButton btnTutup = new JButton("Tutup & Lanjutkan");
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:#ff8200; foreground:#ffffff; font:bold +1; borderWidth:0; focusWidth:0; margin:8,20,8,20");
        
        btnCetak.addActionListener(e -> cetakTiketKePDF(pnl, noNota));

        btnTutup.addActionListener(e -> {
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(btnTutup);
            if (window != null) window.dispose();
        });

        javax.swing.JOptionPane.showOptionDialog(
            this, 
            pnl, 
            "Tiket Pendaftaran Servis", 
            javax.swing.JOptionPane.DEFAULT_OPTION, 
            javax.swing.JOptionPane.PLAIN_MESSAGE, 
            null, 
            new Object[]{btnCetak, btnTutup}, // Menyisipkan 2 Tombol
            btnTutup
        );
    }
    
    // LOGIKA MENCETAK PANEL TIKET KE PDF
    private void cetakTiketKePDF(JPanel panelToPrint, String noNota) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Tiket_Pendaftaran_" + noNota);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                double pageWidth = pageFormat.getImageableWidth();
                double panelWidth = panelToPrint.getWidth();
                double scale = 1.0;

                if (panelWidth > pageWidth) {
                    scale = pageWidth / panelWidth;
                }

                g2d.translate(10, 20);
                g2d.scale(scale * 0.95, scale * 0.95);
                
                RepaintManager currentManager = RepaintManager.currentManager(panelToPrint);
                currentManager.setDoubleBufferingEnabled(false);
                panelToPrint.print(g2d);
                currentManager.setDoubleBufferingEnabled(true);

                return Printable.PAGE_EXISTS;
            }
        });
        
        boolean isPrintAccepted = job.printDialog();
        if (isPrintAccepted) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Tiket berhasil dikirim untuk dicetak!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadAntreanHariIni() {
        modelAntrean.setRowCount(0);
        try {
            Connection kon = DatabaseConnection.getKoneksi();
            String sql = "SELECT s.id_servis, p.nama_pelanggan, pr.merek, pr.tipe_model, s.status " +
                         "FROM data_servis_lengkap s " +
                         "JOIN data_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "JOIN data_perangkat pr ON s.id_perangkat = pr.id_perangkat " +
                         "WHERE DATE(s.tgl_masuk) = CURDATE() AND s.status != 'Batal' " +
                         "ORDER BY s.id_servis DESC";
            ResultSet rs = kon.createStatement().executeQuery(sql);
            while(rs.next()){ modelAntrean.addRow(new Object[]{ String.format("N%05d", rs.getInt("id_servis")), rs.getString("nama_pelanggan"), rs.getString("merek") + " " + rs.getString("tipe_model"), rs.getString("status") }); }
        } catch (Exception e) {}
    }

    private void resetForm() {
        idPelangganTerpilih = -1; txtPelanggan.setText(""); txtMerek.setText(""); txtTipeModel.setText("");
        txtKetPerangkat.setText(""); txtKelengkapan.setText(""); txtKeluhan.setText("");
        cbJenisPerangkat.setSelectedIndex(0); tableAntrean.clearSelection();
    }

    private void liveSearch() {
        String k = txtSearchAntrean.getText();
        if (k.trim().isEmpty()) sorterAntrean.setRowFilter(null);
        else sorterAntrean.setRowFilter(RowFilter.regexFilter("(?i)" + k));
    }

    class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() {
            setLineWrap(true); setWrapStyleWord(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
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