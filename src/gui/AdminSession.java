/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

/**
 *
 * @author user
 */
public class AdminSession {
    private static AdminSession instance;
    private int adminId;
    
     private AdminSession() {
        // Private constructor
    }

    public static AdminSession getInstance() {
        if (instance == null) {
            instance = new AdminSession();
        }
        return instance;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public int getAdminId() {
        return adminId;
    }
    
    
}

