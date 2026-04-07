package com.mssl.form;

import com.mssl.main.Form;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

public class FormPelanggan extends Form {
    
    public FormPelanggan(String nama, String perangkat, String status) {
        setLayout(new MigLayout("insets 20", "[center]", "[center]"));
        
        JLabel lbWelcome = new JLabel("Halo, " + nama + "!");
        lbWelcome.putClientProperty("FlatLaf.style", "font:bold +10");
        
        JLabel lbDetail = new JLabel("Perangkat Anda: " + perangkat);
        JLabel lbStatus = new JLabel("Status Saat Ini: " + status);
        lbStatus.putClientProperty("FlatLaf.style", "foreground:@accentColor; font:bold +5");
        
        add(lbWelcome, "wrap");
        add(lbDetail, "wrap");
        add(lbStatus, "wrap");
    }
}