package tampilan.monitoring;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import tampilan.util.UIStyle;
import connection.DatabaseConnection;

public class MonitoringPanel extends JPanel {

    private JTable table;
    private JLabel totalTersediaLabel;
    private JLabel totalDisewaLabel;
    private JLabel totalBookingLabel;
    private DefaultTableModel tableModel;

    public MonitoringPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createSummaryPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadSummaryData();
        loadBookingData();
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);

        totalTersediaLabel = new JLabel("Tersedia: 0");
        totalDisewaLabel = new JLabel("Disewa: 0");
        totalBookingLabel = new JLabel("Booking: 0");

        for (JLabel label : new JLabel[]{totalTersediaLabel, totalDisewaLabel, totalBookingLabel}) {
            label.setFont(UIStyle.fontBold(16));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel card = new UIStyle.RoundedPanel(20);
            card.setBackground(Color.WHITE);
            card.setLayout(new BorderLayout());
            card.add(label, BorderLayout.CENTER);
            panel.add(card);
        }

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"ID Booking", "Nama Pelanggan", "Item", "Tanggal", "Jam", "Durasi", "Status", "Aksi"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Hanya kolom aksi yang bisa di-edit
            }
        };

        table = new JTable(tableModel);
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.setRowHeight(40);

        return new JScrollPane(table);
    }

    private void loadSummaryData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Hitung item tersedia
            String tersediaQuery = "SELECT COUNT(*) FROM aset WHERE status_tersedia = 1 AND status_disewakan = 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(tersediaQuery)) {
                if (rs.next()) {
                    totalTersediaLabel.setText("Tersedia: " + rs.getInt(1));
                }
            }

            // Hitung item disewa (playathome)
            String disewaQuery = "SELECT COUNT(*) FROM playathome_detail pd " +
                               "JOIN playathome p ON pd.id_playhome = p.id_playhome " +
                               "WHERE p.status = 'aktif'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(disewaQuery)) {
                if (rs.next()) {
                    totalDisewaLabel.setText("Disewa: " + rs.getInt(1));
                }
            }

            // Hitung item booking
            String bookingQuery = "SELECT COUNT(*) FROM booking_detail";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(bookingQuery)) {
                if (rs.next()) {
                    totalBookingLabel.setText("Booking: " + rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data summary: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookingData() {
        tableModel.setRowCount(0); // Clear existing data
        
        // Query untuk data booking (hanya yang belum selesai)
        String bookingQuery = "SELECT b.id_booking as id, b.nama, " +
                 "GROUP_CONCAT(a.nama_barang SEPARATOR ', ') as items, " +
                 "b.tanggal, b.jam, b.durasi_menit, " +
                 "'Booking' as jenis, " +
                 "CASE WHEN b.tanggal >= CURDATE() THEN 'Booking' ELSE 'Selesai' END as status " +
                 "FROM booking b " +
                 "JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                 "LEFT JOIN aset a ON bd.id_aset = a.id_aset " +
                 "WHERE b.tanggal >= CURDATE() " + // Hanya booking yang belum lewat tanggal
                 "GROUP BY b.id_booking ";
        
        // Query untuk data playathome (hanya yang aktif)
        String playhomeQuery = "SELECT p.id_playhome as id, p.nama, " +
                 "GROUP_CONCAT(a.nama_barang SEPARATOR ', ') as items, " +
                 "p.tgl_mulai as tanggal, NULL as jam, " +
                 "DATEDIFF(p.tgl_selesai, p.tgl_mulai) as durasi_menit, " +
                 "'PlayAtHome' as jenis, " +
                 "'Disewa' as status " + // Status selalu Disewa karena kita hanya query yang aktif
                 "FROM playathome p " +
                 "JOIN playathome_detail pd ON p.id_playhome = pd.id_playhome " +
                 "LEFT JOIN aset a ON pd.id_aset = a.id_aset " +
                 "WHERE p.status = 'aktif' " + // Hanya yang status aktif
                 "GROUP BY p.id_playhome ";
        
        // Gabungkan kedua query dengan UNION
        String fullQuery = "(" + bookingQuery + ") UNION (" + playhomeQuery + ") " +
                         "ORDER BY tanggal DESC, jam DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fullQuery)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("jenis") + "-" + rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("items"),
                    rs.getDate("tanggal"),
                    rs.getTime("jam"),
                    rs.getString("jenis").equals("Booking") ? 
                        rs.getInt("durasi_menit") + " menit" : 
                        rs.getInt("durasi_menit") + " hari",
                    rs.getString("status"),
                    rs.getString("status").equals("Disewa") ? "Aktif" : "Kembalikan"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void kembalikanItem(String idTransaksi) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            String[] parts = idTransaksi.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Format ID transaksi tidak valid");
            }
            
            String jenis = parts[0];
            int id = Integer.parseInt(parts[1]);
            
            if (jenis.equals("Booking")) {
                // Update status aset yang terkait
                String updateAset = "UPDATE aset a " +
                                  "JOIN booking_detail bd ON a.id_aset = bd.id_aset " +
                                  "SET a.status_tersedia = 1 " +
                                  "WHERE bd.id_booking = ?";
                PreparedStatement pstAset = conn.prepareStatement(updateAset);
                pstAset.setInt(1, id);
                pstAset.executeUpdate();
                
                // Hapus data booking_detail
                String deleteDetail = "DELETE FROM booking_detail WHERE id_booking = ?";
                PreparedStatement pstDetail = conn.prepareStatement(deleteDetail);
                pstDetail.setInt(1, id);
                pstDetail.executeUpdate();
                
                // Hapus data booking
                String deleteBooking = "DELETE FROM booking WHERE id_booking = ?";
                PreparedStatement pstBooking = conn.prepareStatement(deleteBooking);
                pstBooking.setInt(1, id);
                pstBooking.executeUpdate();
                
            } else if (jenis.equals("PlayAtHome")) {
                // Update status playathome menjadi selesai
                String updatePlayhome = "UPDATE playathome SET status = 'selesai' WHERE id_playhome = ?";
                PreparedStatement pstPlayhome = conn.prepareStatement(updatePlayhome);
                pstPlayhome.setInt(1, id);
                pstPlayhome.executeUpdate();
                
                // Update status aset yang terkait
                String updateAset = "UPDATE aset a " +
                                  "JOIN playathome_detail pd ON a.id_aset = pd.id_aset " +
                                  "SET a.status_tersedia = 1 " +
                                  "WHERE pd.id_playhome = ?";
                PreparedStatement pstAset = conn.prepareStatement(updateAset);
                pstAset.setInt(1, id);
                pstAset.executeUpdate();
            } else {
                throw new IllegalArgumentException("Jenis transaksi tidak dikenali: " + jenis);
            }
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, "Item berhasil dikembalikan", 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh data
            loadSummaryData();
            loadBookingData();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengembalikan item: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Format ID transaksi tidak valid", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIStyle.SUCCESS_COLOR);
            setForeground(UIStyle.PRIMARY);
            setFont(UIStyle.fontBold(12));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Kembalikan" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(UIStyle.SUCCESS_COLOR);
            button.setForeground(Color.WHITE);
            button.setFont(UIStyle.fontBold(12));
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected, int row, int column) {
            label = (value == null) ? "Kembalikan" : value.toString();
            button.setText(label);
            this.row = row;
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                // Get the full transaction ID string (like "Booking-1")
                String transactionId = tableModel.getValueAt(row, 0).toString();
                String nama = tableModel.getValueAt(row, 1).toString();
                String items = tableModel.getValueAt(row, 2).toString();
                
                int confirm = JOptionPane.showConfirmDialog(
                    button, 
                    "Apakah Anda yakin ingin mengembalikan transaksi untuk:\n" +
                    "ID: " + transactionId + "\n" +
                    "Nama: " + nama + "\n" +
                    "Items: " + items,
                    "Konfirmasi Pengembalian",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    kembalikanItem(transactionId);
                }
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}