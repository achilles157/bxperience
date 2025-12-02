package tampilan.playathome;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import connection.DatabaseConnection;
import tampilan.util.UIStyle;
import tampilan.components.RoundedButton;
import tampilan.components.RoundedPanel;

public class PlayAtHomeRentalPanel extends JPanel {
    private JTable rentalTable;
    private DefaultTableModel tableModel;
    private JButton backButton, selesaiButton;
    private JLabel titleLabel;

    public PlayAtHomeRentalPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new RoundedPanel(20, false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        titleLabel = new JLabel("DATA PENYEWAAN PLAY AT HOME", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.fontBold(24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        if (testDatabaseConnection()) {
            loadRentalData();
        } else {
            UIStyle.showErrorMessage(PlayAtHomeRentalPanel.this,
                    "Gagal terhubung ke database" +
                            "Error" + JOptionPane.ERROR_MESSAGE);
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
        UIStyle.styleTable(rentalTable);
        rentalTable.setFillsViewportHeight(true);
        rentalTable.setRowHeight(30);
        rentalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        rentalTable.getColumnModel().getColumn(12).setCellRenderer(new StatusRenderer());

        JScrollPane scrollPane = new JScrollPane(rentalTable);
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(5, 5, 5, 5)));

        return scrollPane;
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        public StatusRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String status = value.toString().toLowerCase();
                if (status.equals("aktif")) {
                    c.setBackground(UIStyle.SUCCESS_COLOR);
                    c.setForeground(Color.WHITE);
                } else if (status.equals("selesai")) {
                    c.setBackground(UIStyle.SECONDARY);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(UIStyle.WARNING_COLOR);
                    c.setForeground(Color.WHITE);
                }
            }

            if (isSelected) {
                c.setBackground(c.getBackground().darker());
            }

            return c;
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setBackground(UIStyle.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        selesaiButton = new RoundedButton("Tandai Selesai", 8);
        selesaiButton.setBackground(UIStyle.SUCCESS_COLOR);
        selesaiButton.setForeground(Color.WHITE);
        selesaiButton.setFont(UIStyle.fontBold(14));
        selesaiButton.addActionListener(e -> tandaiSelesai());
        selesaiButton.setEnabled(false);

        selesaiButton.setToolTipText("Tandai penyewaan yang dipilih sebagai selesai");

        backButton = new RoundedButton("Kembali", 8);
        backButton.setBackground(UIStyle.SECONDARY);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(UIStyle.fontBold(14));
        backButton.addActionListener(e -> {
            javaapplication1.MainFrame.setPage(
                    new tampilan.utama.SidebarShell(new tampilan.monitoring.MonitoringPanel()),
                    "monitoring");
        });

        rentalTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = rentalTable.getSelectedRow() != -1;
            selesaiButton.setEnabled(rowSelected);

            if (rowSelected) {
                int row = rentalTable.getSelectedRow();
                String status = (String) tableModel.getValueAt(row, 12);
                if ("selesai".equalsIgnoreCase(status)) {
                    selesaiButton.setToolTipText("Penyewaan ini sudah selesai");
                    selesaiButton.setEnabled(false);
                } else {
                    selesaiButton.setToolTipText("Tandai penyewaan yang dipilih sebagai selesai");
                }
            }
        });

        panel.add(selesaiButton);
        panel.add(backButton);
        return panel;
    }

    private void loadRentalData() {

        titleLabel.setText("Memuat data...");
        rentalTable.setVisible(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                tableModel.setRowCount(0);

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
                        PreparedStatement pst = conn.prepareStatement(query);
                        ResultSet rs = pst.executeQuery()) {

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
        if (selectedRow == -1)
            return;

        int idPlayhome = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 12);
        String namaPenyewa = (String) tableModel.getValueAt(selectedRow, 1);

        if ("selesai".equalsIgnoreCase(status)) {
            UIStyle.showSuccessMessage(this,
                    "<html><b>Penyewaan ini sudah selesai</b><br>" +
                            "Penyewaan oleh " + namaPenyewa + " (ID: " + idPlayhome + ") " +
                            "sudah ditandai selesai sebelumnya.</html>" +
                            "Informasi" + JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(48, 48));
        iconLabel.setOpaque(true);
        iconLabel.setBackground(UIStyle.SUCCESS_COLOR);
        iconLabel.setBorder(BorderFactory.createLineBorder(UIStyle.SUCCESS_COLOR.darker(), 2));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(UIStyle.fontBold(24));
        iconLabel.setText("✓");

        JLabel messageLabel = new JLabel("<html><div style='width: 300px;'>" +
                "<b>Konfirmasi Penyelesaian Penyewaan</b><br><br>" +
                "Anda akan menandai penyewaan berikut sebagai selesai:<br>" +
                "ID: " + idPlayhome + "<br>" +
                "Nama: " + namaPenyewa + "<br><br>" +
                "Apakah Anda yakin?</div></html>");
        messageLabel.setFont(UIStyle.fontRegular(14));

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(messageLabel, BorderLayout.CENTER);

        int confirm = JOptionPane.showOptionDialog(this,
                panel,
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[] { "Ya, Tandai Selesai", "Batal" },
                "Batal");

        if (confirm == JOptionPane.YES_OPTION) {

            final JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Memproses...",
                    true);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            progressDialog.setLayout(new BorderLayout());
            progressDialog.getContentPane().setBackground(UIStyle.BACKGROUND);

            JPanel progressContent = new JPanel(new BorderLayout(10, 10));
            progressContent.setBackground(UIStyle.BACKGROUND);
            progressContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel progressLabel = new JLabel("Sedang menandai penyewaan sebagai selesai...", JLabel.CENTER);
            progressLabel.setFont(UIStyle.fontRegular(14));
            progressContent.add(progressLabel, BorderLayout.CENTER);

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressContent.add(progressBar, BorderLayout.SOUTH);

            progressDialog.add(progressContent);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        conn.setAutoCommit(false);

                        String updatePlayhome = "UPDATE playathome SET status = 'selesai', tgl_selesai = CURRENT_DATE WHERE id_playhome = ?";
                        PreparedStatement pstPlayhome = conn.prepareStatement(updatePlayhome);
                        pstPlayhome.setInt(1, idPlayhome);
                        pstPlayhome.executeUpdate();

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
                        get();

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