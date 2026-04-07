package com.mssl.koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    private static Connection koneksi;
    
    public static Connection getKoneksi() {
        if (koneksi == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/database_mssl_cheerfulpeople"; 
                String user = "root";
                String password = "";
                
                koneksi = DriverManager.getConnection(url, user, password);
                System.out.println("Koneksi ke Database BERHASIL!");
                
            } catch (SQLException e) {
                System.out.println("Koneksi ke Database GAGAL!");
                System.out.println("Error: " + e.getMessage());
            }
        }
        return koneksi;
    }
}