package com.mssl.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.mssl.main.Form;
import com.mssl.utils.UIHelper; // Import sakti kita
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class FormCS extends Form {

    public FormCS() {
        init();
    }

    private void init() {
        // Layout luar
        setLayout(new MigLayout("fillx, insets 20", "[center]", "[center]"));

        // KARTU/CARD UTAMA
        JPanel cardUtama = new JPanel(new MigLayout("wrap, insets 40 50 40 50", "[center]", "[]10[]30[]10[]30[]"));
        cardUtama.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:25;" // Sudut membulat modern
                + "background:rgb(255,255,255)"); // Warna putih bersih

        // JUDUL
        JLabel lbTitle = new JLabel("Pusat Bantuan & CS");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +16; foreground:@accentColor");

        JLabel lbKelompok = new JLabel("Kelompok 7");
        lbKelompok.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:rgb(150,150,150)");

        // SECTION WHATSAPP
        JLabel lbWa = new JLabel("Contact Person (WhatsApp)");
        lbWa.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:rgb(37,211,102)"); // Warna Hijau WA

        // --- MASUKKAN TAUTAN WA DI SINI (0 diganti 62) ---
        JPanel panelWa = new JPanel(new MigLayout("wrap 3, insets 0, gapx 40, gapy 15", "[][][]", "[]"));
        panelWa.setOpaque(false);
        panelWa.add(createContactCard("Gusht", "0822-3454-8092", "https://wa.me/6282234548092"));
        panelWa.add(createContactCard("Panjie", "0856-4639-4081", "https://wa.me/6285646394081"));
        panelWa.add(createContactCard("Bryan", "0821-4062-4202", "https://wa.me/6282140624202"));

        // SECTION INSTAGRAM
        JLabel lbIg = new JLabel("Instagram");
        lbIg.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:rgb(225,48,108)"); // Warna Pink IG

        // --- MASUKKAN TAUTAN INSTAGRAM DI SINI ---
        JPanel panelIg = new JPanel(new MigLayout("wrap 3, insets 0, gapx 40", "[][][]", "[]"));
        panelIg.setOpaque(false);
        panelIg.add(createIgCard("@ex.gusht", "https://www.instagram.com/ex.gusht01/"));
        panelIg.add(createIgCard("@jie_", "https://www.instagram.com/panjiprataama_/"));
        panelIg.add(createIgCard("@bynadsp", "https://www.instagram.com/bynadsp/"));

        // FOOTER DEVELOPER
        JLabel lbDev = new JLabel("Developed By Cheerful People");
        lbDev.putClientProperty(FlatClientProperties.STYLE, "font:bold italic 13; foreground:rgb(180,180,180)");

        // SUSUN KE DALAM CARD
        cardUtama.add(lbTitle);
        cardUtama.add(lbKelompok);
        cardUtama.add(lbWa, "gapy 20");
        cardUtama.add(panelWa);
        cardUtama.add(lbIg, "gapy 20");
        cardUtama.add(panelIg);
        cardUtama.add(lbDev, "gapy 40"); // Jarak agak jauh ke bawah

        add(cardUtama);
    }
    
    // --- METHOD HELPER YANG SUDAH DIUPGRADE ---
    
    private JPanel createContactCard(String nama, String nomor, String url) {
        JPanel p = new JPanel(new MigLayout("wrap, insets 10 20 10 20", "[center]", "[][]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:rgb(245,248,255)"); 

        JLabel lbNama = new JLabel(nama);
        lbNama.putClientProperty(FlatClientProperties.STYLE, "font:bold 16; foreground:@accentColor");

        JLabel lbNomor = new JLabel(nomor);
        lbNomor.putClientProperty(FlatClientProperties.STYLE, "font:14; foreground:rgb(100,100,100)");

        p.add(lbNama);
        p.add(lbNomor);
        
        // Menjadikan panel bisa diklik dan merubah kursor jadi tangan
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UIHelper.bukaLinkWeb(FormCS.this, url); // Panggil fungsi buka link
            }
        });

        return p;
    }
    
    private JPanel createIgCard(String username, String url) {
        JPanel p = new JPanel(new MigLayout("insets 8 20 8 20", "[center]", "[]"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:rgb(255,245,248)"); 

        JLabel lbUsername = new JLabel(username);
        lbUsername.putClientProperty(FlatClientProperties.STYLE, "font:bold 15; foreground:rgb(200,60,100)");

        p.add(lbUsername);
        
        // Menjadikan panel bisa diklik dan merubah kursor jadi tangan
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UIHelper.bukaLinkWeb(FormCS.this, url); // Panggil fungsi buka link
            }
        });

        return p;
    }
}