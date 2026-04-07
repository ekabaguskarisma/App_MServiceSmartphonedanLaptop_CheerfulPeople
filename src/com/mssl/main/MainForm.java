package com.mssl.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import raven.modal.demo.icons.SVGIconUIColor;

public class MainForm extends JPanel {

    private JPanel mainPanel;
    private Menu sidebarMenu; 
    private boolean isSidebarOpen = true; 

    public MainForm() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        sidebarMenu = new Menu(FormManager.getUserRole(), this);
        
        JPanel contentPanel = new JPanel(new MigLayout("fillx, wrap, insets 0, gap 0", "[fill]", "[][fill, grow][]"));
        contentPanel.add(createHeader());
        contentPanel.add(createMain());
        contentPanel.add(new JSeparator(), "height 2!");
        contentPanel.add(createFooter());

        add(sidebarMenu, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setForm(Component form) {
        mainPanel.removeAll();
        mainPanel.add(form);
        mainPanel.repaint();
        mainPanel.revalidate();
    }
    
    public void toggleSidebar() {
        isSidebarOpen = !isSidebarOpen;
        sidebarMenu.setVisible(isSidebarOpen);
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 0 15 0 15, fill, height 50!", ""));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background");

        JButton btnMenu = new JButton(); 
        try {
            btnMenu.setIcon(new FlatSVGIcon("com/mssl/icon/menu.svg", 22, 22));
        } catch (Exception e) {}

        btnMenu.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0; focusWidth:0; background:null");
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnMenu.addActionListener((e) -> {
            toggleSidebar();
        });

        panel.add(btnMenu, "width 40!, height 40!");
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new MigLayout("insets 1 n 1 n, al trailing center, gapx 10, height 30!", "[]push[][]", "fill"));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:tint($Panel.background, 20%)");

        JLabel lbAppVersion = new JLabel("Sistem Manajemen Service " + Main.APP_VERSION);
        lbAppVersion.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        lbAppVersion.setIcon(new SVGIconUIColor("raven/modal/demo/icons/git.svg", 1f, "Label.disabledForeground"));
        panel.add(lbAppVersion);

        String javaVendor = System.getProperty("java.vendor");
        if (javaVendor.equals("Oracle Corporation")) {
            javaVendor = "";
        }

        String java = javaVendor + " v" + System.getProperty("java.version").trim();
        String st = "Running on: Java %s";
        JLabel lbJava = new JLabel(String.format(st, java));
        lbJava.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        lbJava.setIcon(new SVGIconUIColor("raven/modal/demo/icons/java.svg", 1f, "Label.disabledForeground"));
        panel.add(lbJava);

        JLabel lbDate = new JLabel();
        lbDate.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        Timer timer = new Timer(1000, (e) -> {
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            lbDate.setText(df.format(new Date()));
        });
        panel.add(lbDate);

        timer.start();
        return panel;
    }

    private Component createMain() {
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.setOpaque(false);
    return mainPanel;
}
}