package tampilan.aset;

import connection.DatabaseConnection;
import javax.swing.*;
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

    public AsetManajemenPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel title = new JLabel("Manajemen Data Aset");
        title.setFont(UIStyle.fontBold(24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(UIStyle.PRIMARY);
        titlePanel.add(title, BorderLayout.NORTH);

        // Search and Filter Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            searchField.getBorder(), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        filterCombo = new JComboBox<>(new String[]{"Semua", "PC GAMING", "RACING SIMULATOR", 
            "MOTION RACING SIMULATOR", "FLIGHT SIMULATOR", "VIP DUOS", "VIP SQUAD", 
            "PS5 OPEN SPACE", "PLAYSTATION 5", "PSVR 2", "VR OPULUS META SQUES 2", 
            "VR OPULUS META SQUES 3", "XBOX S SERIES", "NINTENDO SWITCH CADANGAN"});
        
        searchPanel.add(new JLabel("Cari:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Filter Kategori:"));
        searchPanel.add(filterCombo);
        
        titlePanel.add(searchPanel, BorderLayout.SOUTH);
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

        add(new JScrollPane(asetTable), BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        
        editSaveButton = new JButton("Edit");
        editSaveButton.setBackground(UIStyle.PRIMARY);
        editSaveButton.setForeground(Color.BLACK);
        editSaveButton.addActionListener(e -> toggleEditMode());
        
        deleteButton = new JButton("Hapus");
        deleteButton.setBackground(Color.RED);
        deleteButton.setForeground(Color.BLACK);
        deleteButton.addActionListener(e -> deleteSelectedRows());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(editSaveButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAsetData();
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        
        if (isEditing) {
            editSaveButton.setText("Simpan");
            editSaveButton.setBackground(new Color(0, 150, 0)); // Green for save
            deleteButton.setEnabled(false);
            asetTable.setRowSelectionAllowed(false); // Disable row selection during edit
        } else {
            editSaveButton.setText("Edit");
            editSaveButton.setBackground(Color.BLACK);
            deleteButton.setEnabled(true);
            asetTable.setRowSelectionAllowed(true);
            saveChanges();
        }
        
        model.fireTableStructureChanged(); // Refresh table to reflect edit mode changes
    }

    private void saveChanges() {
        int[] selectedRows = asetTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada perubahan yang disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
                
                JOptionPane.showMessageDialog(this, 
                    "Berhasil menyimpan " + successCount + " dari " + results.length + " perubahan.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Gagal menyimpan perubahan: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAsetData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM aset")) {

            while (rs.next()) {
                model.addRow(new Object[]{
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
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
                        if (result > 0) successCount++;
                    }
                    
                    JOptionPane.showMessageDialog(this, 
                        "Berhasil menghapus " + successCount + " dari " + results.length + " aset.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                        
                    refreshData();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Gagal menghapus data: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadAsetData();
    }
}