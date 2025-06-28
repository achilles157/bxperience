package tampilan.booking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import connection.DatabaseConnection;
import tampilan.util.UIStyle;

public class PlayAtHomeRentalPanel extends JPanel {
    private JTable rentalTable;
    private DefaultTableModel tableModel;
    private JButton backButton, selesaiButton;
    private JLabel titleLabel; // Declare titleLabel as a class-level field

    public PlayAtHomeRentalPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Hapus duplikasi deklarasi titleLabel
        titleLabel = new JLabel("DATA PENYEWAAN PLAY AT HOME", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.fontBold(22));
        titleLabel.setForeground(UIStyle.PRIMARY);
        add(titleLabel, BorderLayout.NORTH);
    
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        if (testDatabaseConnection()) {
            loadRentalData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal terhubung ke database",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JScrollPane createTablePanel() {
        String[] columns = {
            "ID", "Nama", "Item Sewa", "Dari Tanggal", "Sampai Tanggal",
            "Metode", "Alamat Antar", "Alamat Kembali",
            "Keperluan", "Harga", "Ongkir", "Total", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rentalTable = new JTable(tableModel);
        rentalTable.setFillsViewportHeight(true);
        rentalTable.setRowHeight(24);
        rentalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(rentalTable);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setBackground(UIStyle.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
        // Tombol Selesai
        selesaiButton = new JButton("Tandai Selesai");
        styleButton(selesaiButton, UIStyle.SUCCESS_COLOR); // Warna hijau untuk aksi positif
        selesaiButton.addActionListener(e -> tandaiSelesai());
        selesaiButton.setEnabled(false);
        
        // Tooltip untuk tombol
        selesaiButton.setToolTipText("Tandai penyewaan yang dipilih sebagai selesai");
    
        // Tombol Kembali
        backButton = new JButton("Kembali");
        styleButton(backButton, UIStyle.SECONDARY); // Warna abu-abu
        backButton.addActionListener(e -> {
            javaapplication1.MainFrame.setPage(
                new tampilan.utama.SidebarShell(new PlayAtHomeManual()), 
                "playathome"
            );
        });
    
        rentalTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = rentalTable.getSelectedRow() != -1;
            selesaiButton.setEnabled(rowSelected);
            
            // Ubah tooltip berdasarkan seleksi
            if (rowSelected) {
                int row = rentalTable.getSelectedRow();
                String status = (String) tableModel.getValueAt(row, 12);
                if ("selesai".equalsIgnoreCase(status)) {
                    selesaiButton.setToolTipText("Penyewaan ini sudah selesai");
                    selesaiButton.setEnabled(false);
                }
            }
        });
    
        panel.add(selesaiButton);
        panel.add(backButton);
        return panel;
    }
    
    // Helper method untuk styling tombol
    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(UIStyle.fontBold(14));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Efek hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void loadRentalData() {
        // Gunakan komponen loading yang sudah ada daripada removeAll()
        titleLabel.setText("Memuat data...");
        rentalTable.setVisible(false);
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
    
                tableModel.setRowCount(0); // Clear existing data
                
                // Debug: Cetak query untuk pengecekan
                System.out.println("Executing query...");
                
                String query = "SELECT p.id_playhome, p.nama, " +
                             "GROUP_CONCAT(DISTINCT a.nama_barang SEPARATOR ', ') as items, " +
                             "p.tgl_mulai, p.tgl_selesai, p.metode_pengambilan, " +
                             "p.alamat_antar, p.alamat_kembali, p.keperluan, " +
                             "p.total_harga - IFNULL(p.ongkir, 0) as harga, " +
                             "IFNULL(p.ongkir, 0) as ongkir, " +
                             "p.total_harga, " +
                             "p.status " +
                             "FROM playathome p " +
                             "LEFT JOIN playathome_detail pd ON p.id_playhome = pd.id_playhome " +
                             "LEFT JOIN aset a ON pd.id_aset = a.id_aset " +
                             "WHERE p.status = 'aktif' " +
                             "GROUP BY p.id_playhome " +
                             "ORDER BY p.tgl_mulai DESC";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                        Object[] row = {
                            rs.getInt("id_playhome"),
                            rs.getString("nama"),
                            rs.getString("items"),
                            rs.getDate("tgl_mulai"),
                            rs.getDate("tgl_selesai"),
                            rs.getString("metode_pengambilan"),
                            rs.getString("alamat_antar"),
                            rs.getString("alamat_kembali"),
                            rs.getString("keperluan"),
                            rs.getDouble("harga"),
                            rs.getDouble("ongkir"),
                            rs.getDouble("total_harga"),
                            rs.getString("status")
                        };
                        tableModel.addRow(row);
                    }
                    
                    // Debug: Cetak jumlah baris yang ditemukan
                    System.out.println("Jumlah data ditemukan: " + rowCount);
                    
                } catch (SQLException e) {
                    System.err.println("Error saat memuat data:");
                    e.printStackTrace();
                    throw e;
                }
                return null;
            }
            
            @Override
        protected void done() {
            try {
                get();
                titleLabel.setText("DATA PENYEWAAN PLAY AT HOME");
                rentalTable.setVisible(true);
                
                if (tableModel.getRowCount() == 0) {

                        // Tampilkan pesan lebih informatif
                        JOptionPane.showMessageDialog(PlayAtHomeRentalPanel.this,
                            "<html><b>Tidak ada data penyewaan aktif yang ditemukan</b><br>" +
                            "Silakan periksa:<br>" +
                            "1. Koneksi database<br>" +
                            "2. Data di tabel playathome dengan status 'aktif'<br>" +
                            "3. Data relasi di playathome_detail</html>",
                            "Informasi", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PlayAtHomeRentalPanel.this, 
                        "<html><b>Gagal memuat data penyewaan</b><br>" +
                        "Error: " + e.getMessage() + "</html>", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    
                    // Fallback view
                    removeAll();
                    add(titleLabel, BorderLayout.NORTH);
                    add(createTablePanel(), BorderLayout.CENTER);
                    add(createButtonPanel(), BorderLayout.SOUTH);
                }
                revalidate();
                repaint();
            }
        }.execute();
    }

    private boolean testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Koneksi database berhasil");
            return true;
        } catch (SQLException e) {
            System.err.println("Gagal terhubung ke database:");
            e.printStackTrace();
            return false;
        }
    }

    private void tandaiSelesai() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) return;
    
        int idPlayhome = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 12);
        String namaPenyewa = (String) tableModel.getValueAt(selectedRow, 1);
    
        if ("selesai".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, 
                "<html><b>Penyewaan ini sudah selesai</b><br>" +
                "Penyewaan oleh " + namaPenyewa + " (ID: " + idPlayhome + ") " +
                "sudah ditandai selesai sebelumnya.</html>", 
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        // Custom dialog dengan ikon dan format lebih baik
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel iconLabel = new JLabel(new ImageIcon("path/to/icon.png")); // Ganti dengan path ikon
        JLabel messageLabel = new JLabel("<html><div style='width: 300px;'>" +
            "<b>Konfirmasi Penyelesaian Penyewaan</b><br><br>" +
            "Anda akan menandai penyewaan berikut sebagai selesai:<br>" +
            "ID: " + idPlayhome + "<br>" +
            "Nama: " + namaPenyewa + "<br><br>" +
            "Apakah Anda yakin?</div></html>");
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(messageLabel, BorderLayout.CENTER);
        
        int confirm = JOptionPane.showOptionDialog(this,
            panel,
            "Konfirmasi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new Object[]{"Ya, Tandai Selesai", "Batal"},
            "Batal");
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Show progress dialog
            final JDialog progressDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Memproses...", true);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            progressDialog.setLayout(new BorderLayout());
            
            JLabel progressLabel = new JLabel("Sedang menandai penyewaan sebagai selesai...", JLabel.CENTER);
            progressDialog.add(progressLabel, BorderLayout.CENTER);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            // Run in background
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        conn.setAutoCommit(false);
                        
                        // Update status penyewaan
                        String updatePlayhome = "UPDATE playathome SET status = 'selesai' WHERE id_playhome = ?";
                        PreparedStatement pstPlayhome = conn.prepareStatement(updatePlayhome);
                        pstPlayhome.setInt(1, idPlayhome);
                        pstPlayhome.executeUpdate();
                        
                        // Update status aset
                        String updateAset = "UPDATE aset a " +
                                           "JOIN playathome_detail pd ON a.id_aset = pd.id_aset " +
                                           "SET a.status_tersedia = 1 " +
                                           "WHERE pd.id_playhome = ?";
                        
                        PreparedStatement pstAset = conn.prepareStatement(updateAset);
                        pstAset.setInt(1, idPlayhome);
                        pstAset.executeUpdate();
                        
                        conn.commit();
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        get(); // Check for exceptions
                        
                        JOptionPane.showMessageDialog(PlayAtHomeRentalPanel.this, 
                            "<html><b>Penyewaan berhasil ditandai selesai</b><br>" +
                            "Penyewaan oleh " + namaPenyewa + " (ID: " + idPlayhome + ") " +
                            "telah berhasil ditandai sebagai selesai.</html>", 
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        
                        loadRentalData();
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(PlayAtHomeRentalPanel.this, 
                            "<html><b>Gagal menandai selesai</b><br>" +
                            "Terjadi kesalahan: " + e.getMessage() + "</html>", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
            
            progressDialog.setVisible(true);
        }
    }
}