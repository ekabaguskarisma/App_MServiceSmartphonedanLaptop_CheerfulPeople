package com.mssl.main;

import com.mssl.form.FormDashboard;
import com.mssl.form.FormLogin;
import javax.swing.JFrame;
import raven.modal.demo.utils.UndoRedo;

public class FormManager {
    
    public static final UndoRedo<Form> FORMS = new UndoRedo<>();
    private static MainForm mainForm;
    private static JFrame frame;
    private static FormLogin formLogin;
    
    private static String loggedInUser;
    private static String userRole; 
    
    public static String getLoggedInUser() { return loggedInUser; }
    public static String getUserRole() { return userRole; } 
    
    public static void install(JFrame f){
        frame = f;
        logout();
    }
    
    public static void showForm(Form form){
        if (form != FORMS.getCurrent()){
            FORMS.add(form);
            form.formCheck();
            form.formOpen();
            getMainForm().setForm(form); 
        }
    }
    
    public static void login(String role, String username) { 
        userRole = role; 
        loggedInUser = username; 

        frame.getContentPane().removeAll();
        
        // Membaca role baru dari menu
        mainForm = new MainForm(); 
        frame.getContentPane().add(mainForm);

        showForm(new FormDashboard());
        frame.repaint();
        frame.revalidate();
    }

    public static void logout() {
        frame.getContentPane().removeAll();
        FormLogin login = getLogin();
        login.formCheck();
        frame.getContentPane().add(login);
        FORMS.clear();
        
        userRole = "";
        loggedInUser = "";
        mainForm = null;
        
        frame.repaint();
        frame.revalidate();
    }
    
    public static JFrame getJFrame(){ return frame; }
    
    public static MainForm getMainForm(){
        if(mainForm == null){
            mainForm = new MainForm();
        }
        return mainForm;
    }
    
    private static FormLogin getLogin(){
        if(formLogin == null){
            formLogin = new FormLogin();
        }
        return formLogin;
    }
}