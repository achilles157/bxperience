package tampilan.aset;

import connection.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tampilan.util.UIStyle;

public class AsetManajemenPanel extends JPanel {

    private JTable asetTable;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private TableRowSorter<TableModel> sorter;
    private boolean isEditing = false;
    private JButton editSaveButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public AsetManajemenPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title Panel
        UIStyle.RoundedPanel titlePanel = new UIStyle.RoundedPanel(15, false);
        titlePanel.setLayout(new BorderLayout(15, 15));
        titlePanel.setBackground(UIStyle.CARD_BG);
        titlePanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("Manajemen Data Aset");
        title.setFont(UIStyle.fontBold(24));
        title.setForeground(UIStyle.PRIMARY);
        titlePanel.add(title, BorderLayout.NORTH);

        // Search and Filter Panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 38));
        UIStyle.styleTextField(searchField);
        
        filterCombo = new JComboBox<>(new String[]{"Semua", "PC GAMING", "RACING SIMULATOR", 
            "MOTION RACING SIMULATOR", "FLIGHT SIMULATOR", "VIP DUOS", "VIP SQUAD", 
            "PS5 OPEN SPACE", "PLAYSTATION 5", "PSVR 2", "VR OPULUS META SQUES 2", 
            "VR OPULUS META SQUES 3", "XBOX S SERIES", "NINTENDO SWITCH CADANGAN"});
        UIStyle.styleComboBox(filterCombo);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        searchPanel.add(createLabel("Cari:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.4;
        searchPanel.add(searchField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        searchPanel.add(createLabel("Filter Kategori:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.3;
        searchPanel.add(filterCombo, gbc);
        
        titlePanel.add(searchPanel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        // Table setup
        model = new DefaultTableModel(new String[]{
            "ID Aset", "Nama Barang", "Kode Barang", "Kategori",
            "Deskripsi", "Harga/menit", "Harga/hari", "Tersedia", "Disewakan"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 7 || columnIndex == 8) { // Tersedia and Disewakan columns
                    return Boolean.class;
                }
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing when in edit mode and not the ID column
                return isEditing && column != 0;
            }
        };
        
        asetTable = new JTable(model);
        UIStyle.styleTable(asetTable);
        asetTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        asetTable.setAutoCreateRowSorter(true);
        asetTable.getTableHeader().setReorderingAllowed(false);
        
        // Set custom editor for boolean columns
        asetTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        asetTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        
        // Enable sorting
        sorter = new TableRowSorter<>(model);
        asetTable.setRowSorter(sorter);
        
        // Add mouse listener for header clicks to show sort indicator
        asetTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = asetTable.columnAtPoint(e.getPoint());
                if (column >= 0) {
                    SortOrder order = asetTable.getRowSorter().getSortKeys().isEmpty() || 
                                     asetTable.getRowSorter().getSortKeys().get(0).getSortOrder() == SortOrder.DESCENDING ?
                                     SortOrder.ASCENDING : SortOrder.DESCENDING;
                    sorter.setSortKeys(List.of(new RowSorter.SortKey(column, order)));
                }
            }
        });

        // Add search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        filterCombo.addActionListener(e -> filter());

        JScrollPane scrollPane = new JScrollPane(asetTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        
        refreshButton = UIStyle.modernButton("Refresh");
        refreshButton.setBackground(UIStyle.SECONDARY);
        refreshButton.addActionListener(e -> refreshData());
        
        editSaveButton = UIStyle.modernButton("Edit");
        editSaveButton.setBackground(UIStyle.PRIMARY);
        editSaveButton.addActionListener(e -> toggleEditMode());
        
        deleteButton = UIStyle.modernButton("Hapus");
        deleteButton.setBackground(UIStyle.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteSelectedRows());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(editSaveButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAsetData();
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.fontMedium(14));
        label.setForeground(UIStyle.TEXT);
        return label;
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        
        if (isEditing) {
            editSaveButton.setText("Simpan");
            editSaveButton.setBackground(UIStyle.SUCCESS_COLOR);
            deleteButton.setEnabled(false);
            refreshButton.setEnabled(false);
            asetTable.setRowSelectionAllowed(false); // Disable row selection during edit
        } else {
            editSaveButton.setText("Edit");
            editSaveButton.setBackground(UIStyle.PRIMARY);
            deleteButton.setEnabled(true);
            refreshButton.setEnabled(true);
            asetTable.setRowSelectionAllowed(true);
            saveChanges();
        }
        
        model.fireTableStructureChanged(); // Refresh table to reflect edit mode changes
    }

    private void saveChanges() {
        int[] selectedRows = asetTable.getSelectedRows();
        if (selectedRows.length == 0) {
            UIStyle.showSuccessMessage(this, "Tidak ada perubahan yang disimpan.");
            return;
        }

        List<Integer> modelRows = new ArrayList<>();
        for (int viewRow : selectedRows) {
            modelRows.add(asetTable.convertRowIndexToModel(viewRow));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            String updateSql = "UPDATE aset SET nama_barang=?, kode_barang=?, kategori=?, deskripsi=?, " +
                             "harga_sewa_menit=?, harga_sewa_hari=?, status_tersedia=?, status_disewakan=? " +
                             "WHERE id_aset=?";
            
            try (PreparedStatement pst = conn.prepareStatement(updateSql)) {
                for (int modelRow : modelRows) {
                    String id = model.getValueAt(modelRow, 0).toString();
                    String nama = model.getValueAt(modelRow, 1).toString();
                    String kode = model.getValueAt(modelRow, 2).toString();
                    String kategori = model.getValueAt(modelRow, 3).toString();
                    String deskripsi = model.getValueAt(modelRow, 4).toString();
                    double hargaMenit = Double.parseDouble(model.getValueAt(modelRow, 5).toString());
                    double hargaHari = Double.parseDouble(model.getValueAt(modelRow, 6).toString());
                    boolean tersedia = (boolean) model.getValueAt(modelRow, 7);
                    boolean disewakan = (boolean) model.getValueAt(modelRow, 8);

                    pst.setString(1, nama);
                    pst.setString(2, kode);
                    pst.setString(3, kategori);
                    pst.setString(4, deskripsi);
                    pst.setDouble(5, hargaMenit);
                    pst.setDouble(6, hargaHari);
                    pst.setBoolean(7, tersedia);
                    pst.setBoolean(8, disewakan);
                    pst.setString(9, id);
                    
                    pst.addBatch();
                }
                
                int[] results = pst.executeBatch();
                conn.commit();
                
                int successCount = 0;
                for (int result : results) {
                    if (result >= 0) successCount++;
                }
                
                UIStyle.showSuccessMessage(this, 
                    "Berhasil menyimpan " + successCount + " dari " + results.length + " perubahan.");
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            UIStyle.showErrorMessage(null, "Gagal menyimpan perubahan: ");
        }
    }

    private void loadAsetData() {
        setInteractions(false);
        model.setRowCount(0);

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                // Kolom didefinisikan di sini untuk model sementara
                String[] columnNames = {"ID Aset", "Nama Barang", "Kode Barang", "Kategori", "Deskripsi", "Harga/menit", "Harga/hari", "Tersedia", "Disewakan"};
                
                DefaultTableModel tempModel = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        if (columnIndex == 7 || columnIndex == 8) { // Kolom Tersedia dan Disewakan
                            return Boolean.class;
                        }
                        return String.class;
                    }
                    
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        // Hanya izinkan edit saat mode edit aktif dan bukan kolom ID
                        return isEditing && column != 0;
                    }
                };

                String query = "SELECT * FROM aset";
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pst = conn.prepareStatement(query);
                    ResultSet rs = pst.executeQuery()) {

                    while (rs.next()) {
                        tempModel.addRow(new Object[]{
                            rs.getString("id_aset"),
                            rs.getString("nama_barang"),
                            rs.getString("kode_barang"),
                            rs.getString("kategori"),
                            rs.getString("deskripsi"),
                            rs.getDouble("harga_sewa_menit"),
                            rs.getDouble("harga_sewa_hari"),
                            rs.getBoolean("status_tersedia"),
                            rs.getBoolean("status_disewakan")
                        });
                    }
                }
                return tempModel;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel resultModel = get();
                    
                    // --- PERBAIKAN DI SINI ---
                    // Konversi array nama kolom menjadi Vector
                    java.util.Vector<String> columnIdentifiers = new java.util.Vector<>();
                    String[] columns = {"ID Aset", "Nama Barang", "Kode Barang", "Kategori", "Deskripsi", "Harga/menit", "Harga/hari", "Tersedia", "Disewakan"};
                    for (String column : columns) {
                        columnIdentifiers.add(column);
                    }
                    
                    // Gunakan setDataVector dengan dua argumen Vector
                    model.setDataVector(resultModel.getDataVector(), columnIdentifiers);
                    
                    // Set ulang editor untuk kolom boolean setelah model diperbarui
                    asetTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JCheckBox()));
                    asetTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JCheckBox()));

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AsetManajemenPanel.this, "Gagal memuat data aset: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setInteractions(true); // Aktifkan kembali interaksi
                }
            }
        }.execute();
    }   

    private void setInteractions(boolean enabled) {
        searchField.setEnabled(enabled);
        filterCombo.setEnabled(enabled);
        asetTable.setEnabled(enabled);
        editSaveButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        // Tampilkan kursor tunggu saat memuat
        setCursor(enabled ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void filter() {
        String text = searchField.getText().toLowerCase();
        String category = filterCombo.getSelectedItem().toString();
        
        RowFilter<TableModel, Object> rf = RowFilter.andFilter(List.of(
            RowFilter.regexFilter("(?i)" + text),
            RowFilter.regexFilter(category.equals("Semua") ? ".*" : "(?i)" + category, 3) // Filter by category column
        ));
        
        sorter.setRowFilter(rf);
    }

    private void deleteSelectedRows() {
        int[] selectedRows = asetTable.getSelectedRows();
        if (selectedRows.length == 0) {
            UIStyle.showSuccessMessage(this, "Pilih baris yang ingin dihapus.");
            return;
        }

        List<String> idsToDelete = new ArrayList<>();
        List<String> namesToDelete = new ArrayList<>();
        
        for (int viewRow : selectedRows) {
            int modelRow = asetTable.convertRowIndexToModel(viewRow);
            idsToDelete.add(model.getValueAt(modelRow, 0).toString());
            namesToDelete.add(model.getValueAt(modelRow, 1).toString());
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus " + selectedRows.length + " aset berikut?\n" + 
            String.join(", ", namesToDelete) + 
            "\n\nPERINGATAN: Semua riwayat booking dan penyewaan terkait aset ini juga akan dihapus.", 
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            // Menggunakan try-with-resources untuk memastikan koneksi ditutup
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Memulai transaksi

                // Query untuk menghapus dari tabel anak terlebih dahulu
                String deletePlayAtHomeDetailSql = "DELETE FROM playathome_detail WHERE id_aset=?";
                String deleteBookingDetailSql = "DELETE FROM booking_detail WHERE id_aset=?";
                String deleteAsetSql = "DELETE FROM aset WHERE id_aset=?";
                
                // Gunakan PreparedStatement untuk setiap query di dalam transaksi
                try (PreparedStatement pstPlayAtHome = conn.prepareStatement(deletePlayAtHomeDetailSql);
                     PreparedStatement pstBooking = conn.prepareStatement(deleteBookingDetailSql);
                     PreparedStatement pstAset = conn.prepareStatement(deleteAsetSql)) {

                    for (String id : idsToDelete) {
                        // Tambahkan perintah ke batch untuk setiap tabel
                        pstPlayAtHome.setString(1, id);
                        pstPlayAtHome.addBatch();

                        pstBooking.setString(1, id);
                        pstBooking.addBatch();
                        
                        pstAset.setString(1, id);
                        pstAset.addBatch();
                    }
                    
                    // Eksekusi semua batch
                    pstPlayAtHome.executeBatch(); // Hapus dari playathome_detail
                    pstBooking.executeBatch();    // Hapus dari booking_detail
                    int[] results = pstAset.executeBatch(); // Hapus dari aset
                    
                    conn.commit(); // Jika semua berhasil, commit transaksi
                    
                    int successCount = 0;
                    for (int result : results) {
                        // Di beberapa JDBC driver, SUCCESS_NO_INFO (-2) juga berarti berhasil
                        if (result > 0 || result == Statement.SUCCESS_NO_INFO) {
                            successCount++;
                        }
                    }
                    
                    UIStyle.showSuccessMessage(this, 
                        "Berhasil menghapus " + successCount + " dari " + results.length + " aset beserta data terkaitnya.");
                        
                    refreshData(); // Muat ulang data tabel
                } catch (SQLException ex) {
                    conn.rollback(); // Jika terjadi error, batalkan semua perubahan dalam transaksi
                    ex.printStackTrace();
                    UIStyle.showErrorMessage(null, "Gagal menghapus data (transaksi dibatalkan): " + ex.getMessage());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                UIStyle.showErrorMessage(null, "Gagal mendapatkan koneksi ke database: " + ex.getMessage());
            }
        }
    }

    public void refreshData() {
        loadAsetData();
    }
}