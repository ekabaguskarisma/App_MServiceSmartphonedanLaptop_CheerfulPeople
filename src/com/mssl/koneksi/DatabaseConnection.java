package com.mssl.koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    private static Connection koneksi;
    private static final String URL = "jdbc:mysql://localhost:3306/database_mssl_cheerfulpeople"; 
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // 1. KONEKSI GENERAL (Untuk Read/Select biasa - Singleton)
    public static Connection getKoneksi() {
        try {
            if (koneksi == null || koneksi.isClosed()) {
                koneksi = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi General ke Database BERHASIL!");
            }
        } catch (SQLException e) {
            System.err.println("Koneksi General GAGAL: " + e.getMessage());
        }
        return koneksi;
    }
    
    // 2. KONEKSI TRANSAKSI (Jalur Khusus/Terisolasi agar tidak tabrakan dengan thread lain)
    public static Connection getKoneksiTransaksi() throws SQLException {
        System.out.println("Membuka Jalur Koneksi Transaksi Khusus...");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}