package tampilan.monitoring;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        // Header Panel
        JPanel headerPanel = new UIStyle.RoundedPanel(20, false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(1024, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel titleLabel = new JLabel("MONITORING PENYEWAAN", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.fontBold(24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Panel utama dengan scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        mainPanel.add(createSummaryPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createTablePanel());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadSummaryData();
        loadBookingData();
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Inisialisasi label dengan teks default
        totalTersediaLabel = new JLabel("<html><div style='text-align:center'>Tersedia<br><span style='font-size:24px'>0</span></div></html>", SwingConstants.CENTER);
        totalDisewaLabel = new JLabel("<html><div style='text-align:center'>Disewa<br><span style='font-size:24px'>0</span></div></html>", SwingConstants.CENTER);
        totalBookingLabel = new JLabel("<html><div style='text-align:center'>Booking<br><span style='font-size:24px'>0</span></div></html>", SwingConstants.CENTER);

        JLabel[] labels = {totalTersediaLabel, totalDisewaLabel, totalBookingLabel};
        Color[] colors = {UIStyle.SUCCESS_COLOR, UIStyle.WARNING_COLOR, UIStyle.PRIMARY};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = labels[i];
            label.setFont(UIStyle.fontBold(16));
            label.setForeground(Color.WHITE);
            label.setOpaque(false);

            // Panel untuk kartu menggunakan RoundedPanel dari UIStyle
            UIStyle.RoundedPanel card = new UIStyle.RoundedPanel(15, true);
            card.setLayout(new BorderLayout());
            card.setBackground(colors[i]);
            card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
            card.setPreferredSize(new Dimension(200, 100));
            card.setMaximumSize(new Dimension(300, 120));
            card.add(label, BorderLayout.CENTER);
            
            panel.add(card);
            if (i < labels.length - 1) {
                panel.add(Box.createRigidArea(new Dimension(15, 0)));
            }
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
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 7 ? JButton.class : Object.class;
            }
        };

        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.setRowHeight(45);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(0, 0, 0, 20), 1, true),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        return scrollPane;
    }

    // Ganti metode loadSummaryData() yang lama
    private void loadSummaryData() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int[] summary = new int[3]; // 0: tersedia, 1: disewa, 2: booking
                String tersediaQuery = "SELECT COUNT(*) FROM aset WHERE status_tersedia = 1 AND status_disewakan = 1";
                String disewaQuery = "SELECT COUNT(DISTINCT pd.id_aset) FROM playathome_detail pd JOIN playathome p ON pd.id_playhome = p.id_playhome WHERE p.status = 'aktif'";
                String bookingQuery = "SELECT COUNT(DISTINCT bd.id_aset) FROM booking_detail bd JOIN booking b ON bd.id_booking = b.id_booking WHERE b.status IS NULL OR b.status NOT IN ('completed', 'cancelled')";

                try (Connection conn = DatabaseConnection.getConnection()) {
                    try (PreparedStatement pst = conn.prepareStatement(tersediaQuery); ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) summary[0] = rs.getInt(1);
                    }
                    try (PreparedStatement pst = conn.prepareStatement(disewaQuery); ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) summary[1] = rs.getInt(1);
                    }
                    try (PreparedStatement pst = conn.prepareStatement(bookingQuery); ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) summary[2] = rs.getInt(1);
                    }
                }
                return summary;
            }

            @Override
            protected void done() {
                try {
                    int[] summary = get();
                    totalTersediaLabel.setText("<html><div style='text-align:center'>Tersedia<br><span style='font-size:24px'>" + summary[0] + "</span></div></html>");
                    totalDisewaLabel.setText("<html><div style='text-align:center'>Disewa<br><span style='font-size:24px'>" + summary[1] + "</span></div></html>");
                    totalBookingLabel.setText("<html><div style='text-align:center'>Booking<br><span style='font-size:24px'>" + summary[2] + "</span></div></html>");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MonitoringPanel.this, "Gagal memuat data summary: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }


// Ganti metode loadBookingData() yang lama
    private void loadBookingData() {
        tableModel.setRowCount(0); // Kosongkan tabel

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                DefaultTableModel tempModel = (DefaultTableModel) table.getModel();
                
                String bookingQuery = "SELECT b.id_booking as id, b.nama, GROUP_CONCAT(a.nama_barang SEPARATOR ', ') as items, b.tanggal, b.jam, b.durasi_menit, 'Booking' as jenis, COALESCE(b.status, 'Booking') as status FROM booking b JOIN booking_detail bd ON b.id_booking = bd.id_booking LEFT JOIN aset a ON bd.id_aset = a.id_aset WHERE (b.status IS NULL OR b.status NOT IN ('completed', 'cancelled')) AND b.tanggal >= CURDATE() GROUP BY b.id_booking ";
                String playhomeQuery = "SELECT p.id_playhome as id, p.nama, GROUP_CONCAT(a.nama_barang SEPARATOR ', ') as items, p.tgl_mulai as tanggal, NULL as jam, DATEDIFF(p.tgl_selesai, p.tgl_mulai) as durasi_menit, 'PlayAtHome' as jenis, 'Disewa' as status FROM playathome p JOIN playathome_detail pd ON p.id_playhome = pd.id_playhome LEFT JOIN aset a ON pd.id_aset = a.id_aset WHERE p.status = 'aktif' GROUP BY p.id_playhome ";
                String fullQuery = "(" + bookingQuery + ") UNION (" + playhomeQuery + ") ORDER BY tanggal DESC, jam DESC";

                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pst = conn.prepareStatement(fullQuery);
                    ResultSet rs = pst.executeQuery()) {
                    
                    while (rs.next()) {
                        tempModel.addRow(new Object[]{
                            rs.getString("jenis") + "-" + rs.getInt("id"),
                            rs.getString("nama"),
                            rs.getString("items"),
                            rs.getDate("tanggal"),
                            rs.getTime("jam"),
                            rs.getString("jenis").equals("Booking") ? rs.getInt("durasi_menit") + " menit" : rs.getInt("durasi_menit") + " hari",
                            rs.getString("status"),
                            rs.getString("status").equals("Disewa") ? "Aktif" : "Kembalikan"
                        });
                    }
                }
                return tempModel;
            }

            @Override
            protected void done() {
                try {
                    // Proses ini tidak memerlukan update model karena sudah dimanipulasi di doInBackground
                    get(); // Cukup panggil get() untuk menangkap exception jika ada
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MonitoringPanel.this, "Gagal memuat data transaksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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
            
            UIStyle.showSuccessMessage(this, "Item berhasil dikembalikan" + 
                "Sukses" + JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh data
            loadSummaryData();
            loadBookingData();
            
        } catch (SQLException e) {
            e.printStackTrace();
            UIStyle.showErrorMessage(this, "Gagal mengembalikan item: " + e.getMessage() + 
                "Error" + JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            UIStyle.showErrorMessage(this, "Format ID transaksi tidak valid" + 
                "Error" + JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            UIStyle.showErrorMessage(this, e.getMessage() + 
                "Error" + JOptionPane.ERROR_MESSAGE);
        }
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIStyle.SUCCESS_COLOR);
            setForeground(Color.WHITE);
            setFont(UIStyle.fontBold(12));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Kembalikan" : value.toString());
            
            // Custom color based on status
            String status = tableModel.getValueAt(row, 6).toString();
            if ("Aktif".equals(status)) {
                setBackground(UIStyle.WARNING_COLOR);
            } else {
                setBackground(UIStyle.SUCCESS_COLOR);
            }
            
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
            button = new UIStyle.RoundedButton("Kembalikan", 6);
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
            
            // Custom color based on status
            String status = tableModel.getValueAt(row, 6).toString();
            if ("Aktif".equals(status)) {
                button.setBackground(UIStyle.WARNING_COLOR);
            } else {
                button.setBackground(UIStyle.SUCCESS_COLOR);
            }
            
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
                
                // Create custom confirmation dialog
                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                // Create icon
                JLabel iconLabel = new JLabel();
                iconLabel.setPreferredSize(new Dimension(48, 48));
                iconLabel.setOpaque(true);
                iconLabel.setBackground(UIStyle.WARNING_COLOR);
                iconLabel.setBorder(BorderFactory.createLineBorder(UIStyle.WARNING_COLOR.darker(), 2));
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconLabel.setForeground(Color.WHITE);
                iconLabel.setFont(UIStyle.fontBold(24));
                iconLabel.setText("!");
                
                JLabel messageLabel = new JLabel("<html><div style='width: 300px;'>" +
                    "<b>Konfirmasi Pengembalian</b><br><br>" +
                    "Anda akan mengembalikan transaksi berikut:<br>" +
                    "ID: " + transactionId + "<br>" +
                    "Nama: " + nama + "<br>" +
                    "Items: " + items + "<br><br>" +
                    "Apakah Anda yakin?</div></html>");
                messageLabel.setFont(UIStyle.fontRegular(14));
                
                panel.add(iconLabel, BorderLayout.WEST);
                panel.add(messageLabel, BorderLayout.CENTER);
                
                int confirm = JOptionPane.showOptionDialog(
                    button, 
                    panel,
                    "Konfirmasi Pengembalian",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Ya, Kembalikan", "Batal"},
                    "Batal");
                
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