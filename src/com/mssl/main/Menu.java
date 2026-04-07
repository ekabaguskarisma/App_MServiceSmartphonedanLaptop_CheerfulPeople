package com.mssl.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon; 
import com.mssl.form.FormDataPelanggan;
import com.mssl.form.FormDataPengguna;
import com.mssl.form.FormPengerjaanStatus;
import com.mssl.form.FormDashboard;
import com.mssl.form.FormLacakProgres;
import com.mssl.form.FormPengambilan;
import com.mssl.form.FormPenerimaanServis;
import com.mssl.form.FormLacakStatus; 
import com.mssl.form.FormDataSparepart;
import com.mssl.form.FormLaporanSparepart;
import com.mssl.form.FormNotaServis; 
import com.mssl.form.FormRiwayatServis; 
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane; 
import javax.swing.border.MatteBorder;
import net.miginfocom.swing.MigLayout;

public class Menu extends JPanel {

    // PALET WARNA TEMA GELAP
    private final Color SIDEBAR_COLOR = new Color(40, 45, 60);     // Warna dasar sidebar
    private final Color HOVER_COLOR = new Color(55, 65, 85);       // Warna saat mouse lewat
    private final Color TEXT_COLOR = Color.WHITE;                  // Warna teks menu
    private final Color ACCENT_ORANGE = new Color(255, 130, 0);    // Oranye khas Cheerful People
    private final Color ACTIVE_BG_COLOR = new Color(255, 130, 0, 30); // Oranye transparan untuk efek glow

    private JPanel panelUtama;
    private String userRole = "";
    private MainForm mainForm; 
    private JPanel currentSelectedPanel;

    public Menu(String role, MainForm mainForm) {
        this.userRole = role; 
        this.mainForm = mainForm;
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill, insets 0", "[280!]", "[fill]")); 
        setBackground(SIDEBAR_COLOR);

        panelUtama = new JPanel(new MigLayout("wrap, fillx, insets 15 0 10 0", "[fill]")); 
        panelUtama.setBackground(SIDEBAR_COLOR);
        
        // HEADER SIDEBAR (LOGO & JUDUL)
        JLabel lbLogo = new JLabel();
        try {
            FlatSVGIcon logoIcon = new FlatSVGIcon("com/mssl/icon/Logo.svg", 45, 45);
            lbLogo.setIcon(logoIcon);
        } catch (Exception e) {
        }

        JLabel lbTitle = new JLabel("Cheerful People");
        lbTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbTitle.setForeground(TEXT_COLOR);

        JLabel lbSubTitle = new JLabel("Service Center");
        lbSubTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbSubTitle.setForeground(new Color(180, 180, 180));

        JPanel headerContainer = new JPanel(new MigLayout("insets 0 20 0 12, fillx", "[][grow]", "[][]"));
        headerContainer.setOpaque(false);
        headerContainer.add(lbLogo, "cell 0 0 1 2, gapright 12"); 
        headerContainer.add(lbTitle, "cell 1 0"); 
        headerContainer.add(lbSubTitle, "cell 1 1"); 

        panelUtama.add(headerContainer, "gapy 10 20, wrap");
        
        // MENU DASHBOARD
        JPanel pnlDashboard = createMenuItem("Dashboard", "dashboard", () -> {
            FormManager.showForm(new FormDashboard());
        });
        panelUtama.add(pnlDashboard, "gaptop 10");
        
        setSelectedMenu(pnlDashboard);

        // MENU KHUSUS ADMIN & TEKNISI
        if (userRole.equalsIgnoreCase("Admin") || userRole.equalsIgnoreCase("Teknisi")) {
            
            // SEKSI DATA MASTER
            panelUtama.add(createSectionLabel("DATA MASTER"), "gapleft 25, gaptop 15, gapbottom 5");
            
            panelUtama.add(createMenuItem("Data Pelanggan", "box", () -> {
                FormManager.showForm(new FormDataPelanggan());
            }));
            
            panelUtama.add(createMenuItem("Data Sparepart", "DATAPELANGGAN", () -> {
                FormManager.showForm(new FormDataSparepart()); 
            }));

            // SEKSI TRANSAKSI
            panelUtama.add(createSectionLabel("TRANSAKSI"), "gapleft 25, gaptop 15, gapbottom 5");
            
            panelUtama.add(createMenuItem("Penerimaan Servis", "PENERIMAANSERVIS", () -> {
                 FormManager.showForm(new FormPenerimaanServis());
             })); 
             
             panelUtama.add(createMenuItem("Pengerjaan & Status", "settings", () -> {
                 FormManager.showForm(new FormPengerjaanStatus());
             }));
             
             // PENGAMBILAN DAN PEMBAYARAN
             panelUtama.add(createMenuItem("Pengambilan & Pembayaran", "cart", () -> {
                 FormManager.showForm(new FormPengambilan());
             }));

            // MONITORING & LAPORAN
            panelUtama.add(createSectionLabel("MONITORING & LAPORAN"), "gapleft 25, gaptop 15, gapbottom 5");
            
            panelUtama.add(createMenuItem("Lacak Status", "search", () -> {
                FormManager.showForm(new FormLacakStatus());
            }));
            
            panelUtama.add(createMenuItem("Riwayat Servis", "report", () -> {
                FormManager.showForm(new FormRiwayatServis());
            }));
            
            panelUtama.add(createMenuItem("Laporan Sparepart", "chart", () -> {
                FormManager.showForm(new FormLaporanSparepart());
            }));
            
            // SEKSI PENGATURAN
            panelUtama.add(createSectionLabel("PENGATURAN"), "gapleft 25, gaptop 15, gapbottom 5");
            
            panelUtama.add(createMenuItem("Account Setting", "user_admin", () -> {
                FormManager.showForm(new FormDataPengguna());
            }));
        } 
        
        // MENU KHUSUS PELANGGAN
        else if (userRole.equalsIgnoreCase("Pelanggan")) {
            
            panelUtama.add(createSectionLabel("LAYANAN"), "gapleft 25, gaptop 15, gapbottom 5");
            
            panelUtama.add(createMenuItem("Lacak Progres Real-Time", "search", () -> {
                FormManager.showForm(new FormLacakProgres());
            }));
            
            panelUtama.add(createMenuItem("Nota Servis Saya", "report", () -> {
                FormManager.showForm(new FormNotaServis());
            }));
            
            panelUtama.add(createSectionLabel("BANTUAN"), "gapleft 25, gaptop 15, gapbottom 5");
            
            panelUtama.add(createMenuItem("Hubungi CS", "user_admin", () -> {
                try {
                    FormManager.showForm(new com.mssl.form.FormCS()); 
                } catch (Exception ex) {
                    // Penanganan jika FormCS belum dibuat
                }
            }));
        }
        
        // FOOTER (TOMBOL KELUAR)
        panelUtama.add(new JLabel(), "push"); // Mendorong tombol logout ke bawah
        
        JPanel pnlLogout = createMenuItem_Simple("KELUAR", "logout", () -> {
            FormManager.logout();
        });
        panelUtama.add(pnlLogout, "bottom, gapbottom 15");
        
        // 4. KONFIGURASI SCROLLBAR SIDEBAR
        JScrollPane scroll = new JScrollPane(panelUtama);
        scroll.setBorder(null); 
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
        scroll.getVerticalScrollBar().setUnitIncrement(12); 
        scroll.getViewport().setBackground(SIDEBAR_COLOR); 
        
        // Styling Scrollbar
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, ""
                + "width:6;"
                + "trackArc:999;"
                + "thumbArc:999;"
                + "trackInsets:0,2,0,2;"
                + "thumbInsets:0,2,0,2;"
                + "background:$Navigation.background");
                
        add(scroll, "grow");
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(150, 155, 170)); // Warna abu-abu muted
        return label;
    }
    
    private void setSelectedMenu(JPanel selectedPanel) {
        if (currentSelectedPanel != null) {
            currentSelectedPanel.setBackground(SIDEBAR_COLOR);
            currentSelectedPanel.setBorder(new MatteBorder(0, 5, 0, 0, SIDEBAR_COLOR));             
        }
        
        currentSelectedPanel = selectedPanel;
        currentSelectedPanel.setBackground(ACTIVE_BG_COLOR);
        currentSelectedPanel.setBorder(new MatteBorder(0, 5, 0, 0, ACCENT_ORANGE));
        
        panelUtama.revalidate();
        panelUtama.repaint();
    }

    private JPanel createMenuItem(String name, String iconName, Runnable formAction) {
        JPanel panel = new JPanel(new MigLayout("insets 10 30 10 20", "[][fill]", "[]"));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(new MatteBorder(0, 5, 0, 0, SIDEBAR_COLOR)); 
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName + ".svg", 18, 18);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_COLOR));

        JLabel lbIcon = new JLabel(icon);
        JLabel lbText = new JLabel(name);
        lbText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbText.setForeground(TEXT_COLOR);
        
        panel.add(lbIcon, "gapright 10");
        panel.add(lbText);
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (panel != currentSelectedPanel) {
                    panel.setBackground(HOVER_COLOR);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (panel != currentSelectedPanel) {
                    panel.setBackground(SIDEBAR_COLOR);
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedMenu(panel);
                if (formAction != null) {
                    formAction.run();
                }
            }
        });

        return panel;
    }
    
    private JPanel createMenuItem_Simple(String name, String iconName, Runnable action) {
        JPanel panel = new JPanel(new MigLayout("insets 10 25 10 20", "[][fill]", "[]"));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(new MatteBorder(0, 5, 0, 0, SIDEBAR_COLOR)); 
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        FlatSVGIcon icon = new FlatSVGIcon("com/mssl/icon/" + iconName + ".svg", 18, 18);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> ACCENT_ORANGE)); // Beri warna oranye

        JLabel lbIcon = new JLabel(icon);
        JLabel lbText = new JLabel(name);
        lbText.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbText.setForeground(ACCENT_ORANGE);
        
        panel.add(lbIcon, "gapright 10");
        panel.add(lbText);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { panel.setBackground(HOVER_COLOR); }
            @Override
            public void mouseExited(MouseEvent e) { panel.setBackground(SIDEBAR_COLOR); }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.run();
                }
            }
        });

        return panel;
    }
}