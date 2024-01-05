/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author user
 */
public class Koneksi {
    private final String URL = "jdbc:mysql://localhost:3306/rent_car";
    private final String USER = "root";
    private final String PASS = "";
    
    public Connection getConnection(){
        Connection conn = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL,USER,PASS);
            System.out.println("Koneksi Berhasil");
            return conn;
        }catch(ClassNotFoundException | SQLException ex){
            System.err.println("Koneksi Gagaal");
            return conn = null;
        }
    }
    
      // Method untuk mendapatkan jumlah entri dari tabel tertentu
    public int getNumberOfEntries(String tabel_car) {
        int count = 0;
        String query = "SELECT COUNT(*) AS count FROM tabel_car" ;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
    
      public int getTotalRentals() {
        int count = 0;
        String query = "SELECT COUNT(*) AS count FROM rental";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
    
    public double getTotalRentalAmount() {
    double totalAmount = 0;
    String query = "SELECT SUM(total) AS total_amount FROM rental";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            totalAmount = rs.getDouble("total_amount");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return totalAmount;
}
    public static void main(String[] args) {
        Koneksi k = new Koneksi();
        k.getConnection();
    }
    
}
