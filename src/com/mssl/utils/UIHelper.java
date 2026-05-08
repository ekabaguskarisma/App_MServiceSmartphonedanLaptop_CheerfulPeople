package com.mssl.utils;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import net.miginfocom.swing.MigLayout;

public class UIHelper {
    
    // 1. FUNGSI NOTIFIKASI 
    public static void tampilkanNotif(Component parent, String title, String message, String type) {
        final String bgColor = type.equals("success") ? "#27ae60" : (type.equals("warning") ? "#ff8200" : "#e74c3c");
        String iconName = type.equals("success") ? "success.svg" : "error.svg";
        
        JPanel p = new JPanel(new MigLayout("insets 20, gapx 20", "[][grow]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:" + bgColor); 
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName, 45, 45);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE)); 
        
        JPanel tp = new JPanel(new MigLayout("wrap, insets 0", "[fill]", "[]5[]")); 
        tp.setOpaque(false); 
        tp.add(new JLabel(title) {{ setFont(new Font("Segoe UI", Font.BOLD, 18)); setForeground(Color.WHITE); }});
        tp.add(new JLabel(message) {{ setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(new Color(240,240,240)); }});
        
        p.add(new JLabel(icon), "top, gapy 2"); 
        p.add(tp);
        
        JButton b = new JButton("Tutup") {{ 
            setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            putClientProperty(FlatClientProperties.STYLE, "background:#ffffff; foreground:" + bgColor + "; font:bold; arc:10; borderWidth:0; margin:5,15,5,15; focusWidth:0"); 
        }};
        b.addActionListener(e -> { Window w = SwingUtilities.getWindowAncestor(b); if(w != null) w.dispose(); });

        JOptionPane.showOptionDialog(parent, p, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{b}, b);
    }
    
    // 2. FUNGSI KONFIRMASI
    public static boolean tampilkanConfirm(Component parent, String title, String message) {
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

        JOptionPane.showOptionDialog(parent, internalPanel, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{btnBatal, btnYa}, btnBatal);

        return result[0];
    }
    
    // 3. FUNGSI RENDER CUSTOM SCROLLBAR
    public static JScrollPane createCustomScroll(JPanel p) {
        JScrollPane s = new JScrollPane(p);
        s.setBorder(null);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(15);
        s.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "width:7; trackArc:999; thumbArc:999;");
        return s;
    }
    
    // 4. FUNGSI STYLING TABEL GLOBAL
    public static void styleTable(JTable tb, Color headerBg, Color headerFg) {
        tb.setBackground(Color.WHITE);
        tb.setRowHeight(60); 
        tb.setShowGrid(false);
        tb.setShowHorizontalLines(true);
        tb.setGridColor(new Color(230, 230, 235));
        tb.putClientProperty(FlatClientProperties.STYLE, "selectionBackground:tint(@accentColor, 85%); selectionForeground:#000000; selectionArc:10");

        JTableHeader header = tb.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(false);
        header.setBackground(headerBg); 
        header.setForeground(headerFg); 
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }
    
    // 5. CLASS RENDERER TEXT WRAP (AUTO ENTER DI TABEL)
    public static class WrapTextRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public WrapTextRenderer() {
            setLineWrap(true); 
            setWrapStyleWord(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setMargin(new java.awt.Insets(10, 10, 10, 10)); 
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
    
    // 6. FUNGSI EKSPOR TABEL KE EXCEL
    public static void exportToCSV(Component parent, JTable table, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan sebagai Excel (.xls)");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName + ".xls"));
        
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Excel Document (*.xls)", "xls");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xls")) {
                fileToSave = new java.io.File(filePath + ".xls");
            }

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(fileToSave))) {
                
                bw.write("<html><head><meta charset=\"UTF-8\">");
                bw.write("<style>");
                bw.write("table { border-collapse: collapse; width: 100%; font-family: 'Segoe UI', Arial, sans-serif; }");
                bw.write("th { background-color: #282D3C; color: #FFFFFF; font-weight: bold; padding: 10px; border: 1px solid #B0B0B0; text-align: left; }");
                bw.write("td { padding: 8px; border: 1px solid #D0D0D0; white-space: nowrap; vertical-align: top; }");
                bw.write("tr:nth-child(even) { background-color: #F8F9FC; }");
                bw.write("</style></head><body>");
                
                bw.write("<h2>" + defaultFileName.replace("_", " ") + "</h2>");
                bw.write("<table>");

                // 1. TULIS HEADER
                bw.write("<tr>");
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (table.getColumnModel().getColumn(i).getWidth() > 0) {
                        bw.write("<th>" + table.getColumnName(i) + "</th>");
                    }
                }
                bw.write("</tr>");

                // 2. TULIS DATA BARIS PER BARIS
                for (int row = 0; row < table.getRowCount(); row++) {
                    bw.write("<tr>");
                    for (int col = 0; col < table.getColumnCount(); col++) {
                        if (table.getColumnModel().getColumn(col).getWidth() > 0) {
                            Object value = table.getValueAt(row, col);
                            String strValue = (value != null) ? value.toString() : "";
                            
                            // Amankan karakter HTML
                            strValue = strValue.replace("<", "&lt;").replace(">", "&gt;");
                            strValue = strValue.replace("\n", "<br>"); // Agar teks yang dienter tetap rapi di Excel
                            
                            bw.write("<td>" + strValue + "</td>");
                        }
                    }
                    bw.write("</tr>");
                }
                
                bw.write("</table></body></html>");
                
                tampilkanNotif(parent, "Ekspor Berhasil!", "Data berhasil disimpan dan dirapikan di:\n" + fileToSave.getName(), "success");
            } catch (Exception ex) {
                tampilkanNotif(parent, "Gagal Ekspor", "Terjadi kesalahan: " + ex.getMessage(), "error");
            }
        }
    }
    // 7. FUNGSI PEMBUKA TAUTAN WEB (BROWSER)
    public static void bukaLinkWeb(Component parent, String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                tampilkanNotif(parent, "Gagal", "Sistem operasi Anda tidak mendukung fitur buka tautan otomatis.", "error");
            }
        } catch (Exception ex) {
            tampilkanNotif(parent, "Error Buka Tautan", "Gagal membuka tautan: " + ex.getMessage(), "error");
        }
    }
}