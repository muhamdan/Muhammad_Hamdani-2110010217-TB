/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package gui;

import db.Koneksi;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author user
 */
public class Rental extends javax.swing.JFrame {
    private Koneksi koneksi;
   
    
    Map<String, Integer> dataMap;
    String username;
    
    BufferedImage bImage;
    
    private final int IMG_WIDTH = 187;
    private final int IMG_HEIGHT = 94;
    
    private TableRowSorter<TableModel> rowSorter;

    private BufferedImage selectedImage;
    
    public Rental(String username) {
        initComponents();
        
        lblAdmin.setText(username);
        this.username = username;
        
        this.setLocationRelativeTo(null);
         showDataFromDashboard(); 
         
         dataMap = new HashMap<>();
         koneksi = new Koneksi();
         
         getCbMobil();
         getCbModel("Avanza");

          
        
         
    }
    

     public BufferedImage getBufferedImage(Blob imageBlob) {
        InputStream binaryStream = null;
        BufferedImage b = null;
        try {
            binaryStream = imageBlob.getBinaryStream();
            b = ImageIO.read(binaryStream);
        } catch (SQLException | IOException ex) {
            System.err.println("Error getBufferedImage : "+ex);
        }
        return b;
    }
    
    public Blob getBlobImage(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Blob blFile = null;
        try {
            ImageIO.write(bi, "png", baos);
            blFile = new javax.sql.rowset.serial.SerialBlob(baos.toByteArray());
        } catch (SQLException | IOException ex) {
            Logger.getLogger(Rental.class.getName()).log(Level.SEVERE, null, ex);
        }
        return blFile;
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int type) {
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        return resizedImage;
    }

    
    
    private void showDataFromDashboard() {
    Dashboard dashboard = new Dashboard(username);
    DefaultTableModel dashboardData = dashboard.getDataFromDashboard();
    tabelCar.setModel(dashboardData); // tabelRental adalah JTable di kelas Rental
    
    // Buat RowSorter untuk tabelCar
    rowSorter = new TableRowSorter<>(tabelCar.getModel());
    tabelCar.setRowSorter(rowSorter);

    // Tambahkan ActionListener untuk JTextField pencarian
    tfCariCar.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            search(tfCariCar.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            search(tfCariCar.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            search(tfCariCar.getText());
        }

        private void search(String text) {
            if (text.trim().length() == 0) {
                rowSorter.setRowFilter(null); // Jika teks kosong, hapus penyaringan
            } else {
                // Gunakan RowFilter.regexFilter untuk pencarian berbasis teks
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); // (?i) untuk pencarian case-insensitive
            }
        }
    });
}    
    
    
    
    public void getCbMobil(){
        try {
            // Membuat koneksi ke database
            Connection conn = koneksi.getConnection();

            if (conn != null) {
                // Query untuk mengambil data mobil yang tersedia berdasarkan merek
                String selectQuery = "SELECT * FROM tabel_car WHERE status = 'Tersedia' GROUP BY brand";

                // Persiapan statement SQL
                PreparedStatement ps = conn.prepareStatement(selectQuery);

                // Menjalankan query dan menyimpan hasilnya
                ResultSet rs = ps.executeQuery();

                // Menghapus semua item dalam combo box merek mobil
                comboBrand.removeAllItems();
                
                // Looping hasil query
                while (rs.next()) {
                    // Mengambil nilai ID mobil dan merek mobil
                    int id = rs.getInt("car_id");
                    String item = rs.getString("brand");
                    
                    // Menambahkan merek mobil ke dalam combo box
                    comboBrand.addItem(item);
                }

                // Menutup objek ResultSet, PreparedStatement, dan koneksi
                rs.close();
                ps.close();
                conn.close(); 
            }
        } catch (SQLException e) {
            // Menangani exception jika terjadi kesalahan dalam eksekusi query atau koneksi
            System.err.println("SQL Exception: " + e);
        }
    }
    
     public void getCbModel(String brand){
        try {
            // Membuat koneksi ke database
            Connection conn = koneksi.getConnection();

            if (conn != null) {
                // Query untuk mengambil model mobil berdasarkan merek yang dipilih
                String selectQuery = "SELECT * FROM tabel_car WHERE brand = ? AND status = 'Tersedia' GROUP BY model";
                
                // Persiapan statement SQL dengan parameter
                PreparedStatement ps = conn.prepareStatement(selectQuery);

                // Menetapkan nilai parameter brand untuk statement prepared statement
                ps.setString(1, brand);

                // Menjalankan query dan menyimpan hasilnya
                ResultSet rs = ps.executeQuery();

                // Menghapus semua item dalam combo box model mobil
                comboModel.removeAllItems();
                
                // Looping hasil query
                while (rs.next()) {
                    // Mengambil nilai ID mobil dan model mobil
                    int id = rs.getInt("car_id");
                    String item = rs.getString("model");
                    
                    // Menambahkan model mobil ke dalam combo box
                    comboModel.addItem(item);
                    
                    // Memetakan model dengan ID mobil ke dalam dataMap
                    dataMap.put(item, id); 
                }

                // Menutup objek ResultSet, PreparedStatement, dan koneksi
                rs.close();
                ps.close();
                conn.close(); 
            }
        } catch (SQLException e) {
            // Menangani exception jika terjadi kesalahan dalam eksekusi query atau koneksi
            System.err.println("SQL Exception: " + e);
        }
    }
    
    
    public void getCbCar(String brand, String model) {
    try {
        Connection conn = koneksi.getConnection();
        if (conn != null) {
            String selectQuery = "SELECT car_id FROM tabel_car WHERE brand = ? AND model = ? AND status = 'Tersedia'";
            
            PreparedStatement ps = conn.prepareStatement(selectQuery);
            ps.setString(1, brand);
            ps.setString(2, model);
            ResultSet rs = ps.executeQuery();

            ComboID.removeAllItems(); // Menghapus semua item dalam combo box mobil
            
            while (rs.next()) {
                int id = rs.getInt("car_id");
                 ComboID.addItem(String.valueOf(id));
            }

            rs.close();
            ps.close();
            conn.close(); 
        }
    } catch (SQLException e) {
        System.err.println("SQL Exception: " + e);
    }
}
    
    
public long hitungSelisihTanggal(JXDatePicker startDatePicker, JXDatePicker endDatePicker) {
        LocalDate startDate = startDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return ChronoUnit.DAYS.between(startDate, endDate);
    }


// Metode untuk mengambil harga mobil berdasarkan ID mobil dari database
public int getHargaMobil(int carID) {
    int harga = 0;
    try {
        Connection conn = koneksi.getConnection();
        if (conn != null) {
            String selectQuery = "SELECT harga FROM tabel_car WHERE car_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(selectQuery);
            ps.setInt(1, carID);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                harga = rs.getInt("harga");
            }
            
            rs.close();
            ps.close();
            conn.close();
        }
    } catch (SQLException e) {
        System.err.println("SQL Exception: " + e);
    }
    return harga;
}

// Method untuk mengambil harga mobil dari combobox yang dipilih
public int getHargaMobilDariComboBox() {
    int carID = Integer.parseInt(ComboID.getSelectedItem().toString()); // Ambil ID mobil dari combobox
    
    // Panggil method getHargaMobil untuk mendapatkan harga mobil berdasarkan ID mobil
    int harga = getHargaMobil(carID);
    
    return harga;
}

private void hitungPengurangan() {
    String nilaiLabelStr = lblTotal.getText();
    String nilaiTextFieldStr = tfBayar.getText();

    if (!nilaiLabelStr.isEmpty() && !nilaiTextFieldStr.isEmpty()) {
        int nilaiLabel = Integer.parseInt(nilaiLabelStr);
        int nilaiTextField = Integer.parseInt(nilaiTextFieldStr);
        
        // Lakukan pengurangan
        int hasilPengurangan = nilaiTextField - nilaiLabel ;
        
        // Tampilkan hasil pengurangan pada label lain
        lblBalance.setText(String.valueOf(hasilPengurangan));
    } else {
       
        lblBalance.setText("Nilai kosong");
    }
}

// Method to retrieve KTP photo from GUI
// Method to retrieve KTP photo from GUI
private BufferedImage ambilFotoKTPDariGUI() {
    JFileChooser fChooser = new JFileChooser();
    FileFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");
    fChooser.setFileFilter(filter);

    BufferedImage ktpImage = null;

    int result = fChooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
        try {
            File file = fChooser.getSelectedFile();
            ktpImage = ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Error saat membaca foto KTP: " + e);
        }
    }

    return ktpImage;
}

// Method to save rental data into the database
private void simpanRental() {
    String carId = ComboID.getSelectedItem().toString();
    String nama = tfNama.getText();
    String gender = comboGender.getSelectedItem().toString();
    String telpon = tfTelpon.getText();
    String total = lblTotal.getText();

    try {
        BufferedImage ktpImage = selectedImage;

        // Ambil tanggal dari JXDatePicker 'awal' dan 'akhir'
        Date tanggalAwal = new Date(StartDatePicker.getDate().getTime());
        Date tanggalAkhir = new Date(EndDatePicker.getDate().getTime());

        // Checking if KTP image exists before proceeding
        if (ktpImage != null) {
            // Converting KTP image to Blob
            Blob ktpBlob = getBlobImage(ktpImage);
            
              // Get the admin ID from AdminSession
            AdminSession adminSession = AdminSession.getInstance();
            int adminId = adminSession.getAdminId();

            // Opening database connection
            Connection conn = koneksi.getConnection();

            // Checking if the connection was successful
            if (conn != null) {
                // SQL query to insert data into the rental table using a parameterized query to prevent SQL injection
                String insertQuery = "INSERT INTO rental (admin_id, car_id, nama, gender, telpon, ktp, awal, akhir, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(insertQuery);

                // Setting parameter values on the prepared statement for data taken from GUI components
                ps.setInt(1, adminId);
                ps.setString(2, carId);
                ps.setString(3, nama);
                ps.setString(4, gender);
                ps.setString(5, telpon);
                ps.setBlob(6, ktpBlob); // Setting KTP photo to parameter number 5
                ps.setDate(7, tanggalAwal); // Setting 'awal' date to parameter number 6
                ps.setDate(8, tanggalAkhir); // Setting 'akhir' date to parameter number 7
                ps.setString(9, total);

                // Executing the SQL command to insert data into the database
                ps.executeUpdate();

                // Displaying a message indicating successful data storage
                System.out.println("Data Rental berhasil disimpan ke dalam database.");

                // Closing the connection and related resources
                ps.close();
                conn.close();
            }
        } else {
            System.err.println("Tidak ada foto KTP yang dipilih.");
        }
    } catch (Exception e) {
        // Handling errors if they occur
        System.err.println("Error saat menyimpan data Rental: " + e);
    }
}

public int getLastRentIdFromDatabase(Connection conn) {
    int lastRentId = 0;
    try {
        // Query untuk mendapatkan rent_id terakhir dari tabel rental
        String query = "SELECT MAX(rent_id) as last_id FROM rental";

        // Membuat statement
        Statement stmt = conn.createStatement();

        // Eksekusi query
        ResultSet rs = stmt.executeQuery(query);

        // Mendapatkan rent_id terakhir
        if (rs.next()) {
            lastRentId = rs.getInt("last_id");
        }

        // Tutup koneksi dan statement
        rs.close();
        stmt.close();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return lastRentId;
}


    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fChooser = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblAdmin = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tfNama = new javax.swing.JTextField();
        comboBrand = new javax.swing.JComboBox<>();
        comboModel = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        tfTelpon = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        comboGender = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        lbGambar = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        labelbalen = new javax.swing.JLabel();
        tfBayar = new javax.swing.JTextField();
        lblTotal = new javax.swing.JLabel();
        btnKTP = new javax.swing.JButton();
        btnRent = new javax.swing.JButton();
        StartDatePicker = new org.jdesktop.swingx.JXDatePicker();
        EndDatePicker = new org.jdesktop.swingx.JXDatePicker();
        jLabel3 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        ComboID = new javax.swing.JComboBox<>();
        btnHitung = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        lblBalance = new javax.swing.JLabel();
        btnCetak_struk = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        scrollpane = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelCar = new javax.swing.JTable();
        tfCariCar = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 0));

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/orang_icon.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("welcome");

        lblAdmin.setFont(new java.awt.Font("Microsoft YaHei", 1, 24)); // NOI18N
        lblAdmin.setForeground(new java.awt.Color(255, 255, 255));
        lblAdmin.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAdmin.setText("ADMIN");

        jButton1.setBackground(new java.awt.Color(0, 51, 51));
        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Home");
        jButton1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(0, 51, 51));
        jButton4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Availabe Car");
        jButton4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 2, true));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(0, 51, 51));
        jButton5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("Rent Car");
        jButton5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 255, 0), 3));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton9.setBackground(new java.awt.Color(255, 0, 0));
        jButton9.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jButton9.setForeground(new java.awt.Color(255, 255, 255));
        jButton9.setText("Sign Out");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(11, 11, 11))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAdmin)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton9)
                .addGap(34, 34, 34))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel4.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel4.setText("Car ID :");

        jLabel6.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel6.setText("Model :");

        jLabel8.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel8.setText("Nama :");

        tfNama.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        comboBrand.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBrand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBrandActionPerformed(evt);
            }
        });

        comboModel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboModelActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel9.setText("Telepon :");

        tfTelpon.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel10.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel10.setText("Gender :");

        comboGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pria", "Wanita" }));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Upload KTP", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbGambar, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbGambar, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
        );

        jLabel7.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel7.setText("Brand :");

        jLabel11.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        jLabel11.setText("Amount : Rp.");

        labelbalen.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        labelbalen.setText("Balance : Rp.");

        tfBayar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tfBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBayarActionPerformed(evt);
            }
        });

        lblTotal.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        lblTotal.setText("total");

        btnKTP.setBackground(new java.awt.Color(51, 153, 0));
        btnKTP.setForeground(new java.awt.Color(255, 255, 255));
        btnKTP.setText("KTP");
        btnKTP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKTPActionPerformed(evt);
            }
        });

        btnRent.setBackground(new java.awt.Color(0, 51, 51));
        btnRent.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        btnRent.setForeground(new java.awt.Color(255, 255, 255));
        btnRent.setText("Rent");
        btnRent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRentActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel3.setText("Date Rental");

        jLabel13.setFont(new java.awt.Font("Microsoft YaHei", 1, 14)); // NOI18N
        jLabel13.setText("Date Returned");

        ComboID.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ComboID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComboIDActionPerformed(evt);
            }
        });

        btnHitung.setText("Hitung");
        btnHitung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHitungActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        jLabel12.setText("Total     : Rp.");

        lblBalance.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 12)); // NOI18N
        lblBalance.setText(" balance");

        btnCetak_struk.setBackground(new java.awt.Color(0, 51, 51));
        btnCetak_struk.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        btnCetak_struk.setForeground(new java.awt.Color(255, 255, 255));
        btnCetak_struk.setText("Cetak");
        btnCetak_struk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetak_strukActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(comboBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ComboID, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(comboModel, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfNama, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfTelpon, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(btnKTP, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(38, 38, 38))))
                            .addComponent(comboGender, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(EndDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(StartDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel12)
                                    .addComponent(labelbalen))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblBalance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 17, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnRent, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCetak_struk, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHitung)
                        .addGap(98, 98, 98))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tfTelpon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotal)
                    .addComponent(jLabel7)
                    .addComponent(comboBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(comboModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(ComboID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(btnKTP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8)
                                    .addComponent(tfNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(comboGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(19, 19, 19)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(StartDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(EndDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(tfBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelbalen)
                            .addComponent(lblBalance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHitung)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRent, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCetak_struk, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Verdana", 3, 18)); // NOI18N
        jLabel15.setText("Available Car");

        tabelCar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tabelCar.setEnabled(false);
        jScrollPane1.setViewportView(tabelCar);

        scrollpane.setViewportView(jScrollPane1);

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cari_icon.png"))); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollpane))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel15))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfCariCar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfCariCar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(7, 7, 7)
                .addComponent(scrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnKTPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKTPActionPerformed
        // TODO add your handling code here:
        FileFilter filter = new FileNameExtensionFilter("Image Files",
                                                 "jpg", "png", "gif", "jpeg");
        fChooser.setFileFilter(filter);
        BufferedImage img = null;
        try {
            int result = fChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fChooser.getSelectedFile();
                img = ImageIO.read(file);
                int type = img.getType() == 0? BufferedImage.TYPE_INT_ARGB : img.getType();
                selectedImage = resizeImage(img, type);
                
              if (lbGambar != null) {
                lbGambar.setIcon(new ImageIcon(selectedImage)); // Tampilkan gambar di lbGambar
            } else {
                System.err.println("lbGambar memiliki nilai null!");
            }
              }
        } catch (IOException e) {
            System.err.println("Error bPilih : "+e);
        }
    }//GEN-LAST:event_btnKTPActionPerformed

    private void btnRentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRentActionPerformed
        hitungPengurangan();
        simpanRental();
    }//GEN-LAST:event_btnRentActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new Home(username).setVisible(true);
        dispose();
        dispose();        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        new Dashboard(username).setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
    
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void comboBrandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBrandActionPerformed
        getCbModel((String)comboBrand.getSelectedItem());
    }//GEN-LAST:event_comboBrandActionPerformed

    private void comboModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboModelActionPerformed
        getCbCar((String)comboBrand.getSelectedItem(), (String)comboModel.getSelectedItem());
    }//GEN-LAST:event_comboModelActionPerformed

    private void tfBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBayarActionPerformed

    }//GEN-LAST:event_tfBayarActionPerformed

    private void ComboIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboIDActionPerformed

    }//GEN-LAST:event_ComboIDActionPerformed

    private void btnHitungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHitungActionPerformed
    long selisihHari = hitungSelisihTanggal(StartDatePicker, EndDatePicker); // Ganti jXDatePicker1 dan jXDatePicker2 sesuai kebutuhan
    // Mendapatkan harga mobil dari combobox yang dipilih
    int hargaMobil = getHargaMobilDariComboBox();

    // Menghitung total harga sewa mobil
    int totalHarga = (int) (selisihHari * hargaMobil);

    // Menampilkan total harga
     // Menampilkan total harga di labelTotal
   lblTotal.setText(String.valueOf(totalHarga));
    }//GEN-LAST:event_btnHitungActionPerformed

    private void btnCetak_strukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetak_strukActionPerformed
        try {
    String reportPath = "src/report/Transaksi.jasper";
    Connection conn = koneksi.getConnection();
    
    int lastRentId = getLastRentIdFromDatabase(conn); // Mendapatkan rent_id terakhir dari database
    
    HashMap<String, Object> parameters = new HashMap<>();
    parameters.put("rent_id", lastRentId); // Menggunakan rent_id terakhir sebagai parameter

    JasperPrint print = JasperFillManager.fillReport(reportPath, parameters, conn);
    JasperViewer viewer = new JasperViewer(print, false);
    viewer.setVisible(true);
} catch (Exception e) {
    JOptionPane.showMessageDialog(this, "Error displaying report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}
    }//GEN-LAST:event_btnCetak_strukActionPerformed

    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Rental.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Rental.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Rental.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rental.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Rental("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> ComboID;
    private org.jdesktop.swingx.JXDatePicker EndDatePicker;
    private org.jdesktop.swingx.JXDatePicker StartDatePicker;
    private javax.swing.JButton btnCetak_struk;
    private javax.swing.JButton btnHitung;
    private javax.swing.JButton btnKTP;
    private javax.swing.JButton btnRent;
    private javax.swing.JComboBox<String> comboBrand;
    private javax.swing.JComboBox<String> comboGender;
    private javax.swing.JComboBox<String> comboModel;
    private javax.swing.JFileChooser fChooser;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelbalen;
    private javax.swing.JLabel lbGambar;
    private javax.swing.JLabel lblAdmin;
    private javax.swing.JLabel lblBalance;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JScrollPane scrollpane;
    private javax.swing.JTable tabelCar;
    private javax.swing.JTextField tfBayar;
    private javax.swing.JTextField tfCariCar;
    private javax.swing.JTextField tfNama;
    private javax.swing.JTextField tfTelpon;
    // End of variables declaration//GEN-END:variables
}
