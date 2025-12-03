package service;

import connection.DatabaseConnection;
import java.sql.*;
import java.util.List;

import javax.swing.table.DefaultTableModel;

/**
 * Data Access Object for Asset management.
 * Handles CRUD operations for assets, including ID generation and
 * category-specific logic.
 */
public class AsetDAO {

    /**
     * Inserts a new asset into the database.
     * Automatically generates ID and Code based on category and existing data.
     *
     * @param nama       Name of the asset
     * @param kategori   Category of the asset
     * @param deskripsi  Description of the asset
     * @param jumlah     Number of items to add
     * @param hargaMenit Price per minute
     * @param hargaHari  Price per day
     * @param tersedia   Availability status
     * @param disewakan  Rentable status
     * @throws SQLException if a database error occurs
     */
    public void insertAset(String nama, String kategori, String deskripsi, int jumlah, double hargaMenit,
            double hargaHari, boolean tersedia, boolean disewakan) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO aset (id_aset, nama_barang, kode_barang, kategori, deskripsi, harga_sewa_menit, harga_sewa_hari, status_tersedia, status_disewakan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                int lastIdNumber = getLastNumberFromDb(conn, "id_aset", "AST");
                String kodePrefix = generateKodeBarang(kategori);
                int lastKodeNumber = getLastNumberFromDb(conn, "kode_barang", kodePrefix);

                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    for (int i = 1; i <= jumlah; i++) {
                        String finalId = "AST" + String.format("%03d", lastIdNumber + i);
                        String finalKode = kodePrefix + String.format("%03d", lastKodeNumber + i);
                        String finalNama = nama;
                        if (jumlah > 1) {
                            finalNama = nama + " " + String.format("%02d", lastKodeNumber + i);
                        }

                        pst.setString(1, finalId);
                        pst.setString(2, finalNama);
                        pst.setString(3, finalKode);
                        pst.setString(4, kategori);
                        pst.setString(5, deskripsi);
                        pst.setDouble(6, hargaMenit);
                        pst.setDouble(7, hargaHari);
                        pst.setInt(8, tersedia ? 1 : 0);
                        pst.setInt(9, disewakan ? 1 : 0);

                        pst.addBatch();
                    }
                    pst.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Updates multiple assets in a batch.
     *
     * @param updates List of object arrays containing update data.
     *                Format: [id, nama, kode, kategori, deskripsi, hargaMenit,
     *                hargaHari, tersedia, disewakan]
     * @throws SQLException if a database error occurs
     */
    public void updateAset(List<Object[]> updates) throws SQLException {
        String updateSql = "UPDATE aset SET nama_barang=?, kode_barang=?, kategori=?, deskripsi=?, " +
                "harga_sewa_menit=?, harga_sewa_hari=?, status_tersedia=?, status_disewakan=? " +
                "WHERE id_aset=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(updateSql)) {

            conn.setAutoCommit(false);
            try {
                for (Object[] row : updates) {
                    // row: [id, nama, kode, kategori, deskripsi, hargaMenit, hargaHari, tersedia,
                    // disewakan]
                    pst.setString(1, (String) row[1]);
                    pst.setString(2, (String) row[2]);
                    pst.setString(3, (String) row[3]);
                    pst.setString(4, (String) row[4]);
                    pst.setDouble(5, (Double) row[5]);
                    pst.setDouble(6, (Double) row[6]);
                    pst.setBoolean(7, (Boolean) row[7]);
                    pst.setBoolean(8, (Boolean) row[8]);
                    pst.setString(9, (String) row[0]);

                    pst.addBatch();
                }
                pst.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Deletes assets and their associated details (Booking and PlayAtHome) by IDs.
     *
     * @param ids List of Asset IDs to delete
     * @return Number of assets successfully deleted
     * @throws SQLException if a database error occurs
     */
    public int deleteAset(List<String> ids) throws SQLException {
        int successCount = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String deletePlayAtHomeDetailSql = "DELETE FROM playathome_detail WHERE id_aset=?";
            String deleteBookingDetailSql = "DELETE FROM booking_detail WHERE id_aset=?";
            String deleteAsetSql = "DELETE FROM aset WHERE id_aset=?";

            try (PreparedStatement pstPlayAtHome = conn.prepareStatement(deletePlayAtHomeDetailSql);
                    PreparedStatement pstBooking = conn.prepareStatement(deleteBookingDetailSql);
                    PreparedStatement pstAset = conn.prepareStatement(deleteAsetSql)) {

                for (String id : ids) {
                    pstPlayAtHome.setString(1, id);
                    pstPlayAtHome.addBatch();

                    pstBooking.setString(1, id);
                    pstBooking.addBatch();

                    pstAset.setString(1, id);
                    pstAset.addBatch();
                }

                pstPlayAtHome.executeBatch();
                pstBooking.executeBatch();
                int[] results = pstAset.executeBatch();

                conn.commit();

                for (int result : results) {
                    if (result > 0 || result == Statement.SUCCESS_NO_INFO) {
                        successCount++;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return successCount;
    }

    /**
     * Retrieves all assets from the database.
     *
     * @return DefaultTableModel containing all asset data
     * @throws SQLException if a database error occurs
     */
    public DefaultTableModel getAllAset() throws SQLException {
        String[] columnNames = { "ID Aset", "Nama Barang", "Kode Barang", "Kategori", "Deskripsi", "Harga/menit",
                "Harga/hari", "Tersedia", "Disewakan" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 7 || columnIndex == 8) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        String query = "SELECT * FROM aset";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[] {
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
        return model;
    }

    /**
     * Menghasilkan ID Aset berikutnya secara otomatis.
     * Format ID: ASTxxx (contoh: AST001, AST002).
     *
     * @return String ID Aset baru.
     */
    public String generateNextIdAset() {
        String prefix = "AST";
        int counter = 1;
        String query = "SELECT id_aset FROM aset ORDER BY id_aset DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id_aset");
                if (lastId != null && lastId.startsWith(prefix)) {
                    String numberPart = lastId.substring(prefix.length());
                    counter = Integer.parseInt(numberPart) + 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefix + String.format("%03d", counter);
    }

    /**
     * Menghasilkan Kode Barang berikutnya berdasarkan kategori.
     * Format Kode: [PREFIX]xxx (contoh: PS3001, TV40005).
     *
     * @param kategori Kategori aset untuk menentukan prefix.
     * @return String Kode Barang baru.
     */
    public String generateNextKodeBarang(String kategori) {
        String prefix = generateKodeBarang(kategori);
        int nextNumber = 1;
        String query = "SELECT kode_barang FROM aset WHERE kode_barang LIKE ? ORDER BY kode_barang DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, prefix + "%");
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String lastKode = rs.getString("kode_barang");
                if (lastKode != null && lastKode.startsWith(prefix)) {
                    String numberPart = lastKode.substring(prefix.length());
                    nextNumber = Integer.parseInt(numberPart) + 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefix + String.format("%03d", nextNumber);
    }

    /**
     * Mengambil nomor urut terakhir dari database untuk kolom tertentu.
     * Digunakan untuk generate ID atau Kode otomatis.
     *
     * @param conn       Koneksi database.
     * @param columnName Nama kolom yang akan dicek (misal: id_aset atau
     *                   kode_barang).
     * @param prefix     Prefix yang digunakan untuk filter (misal: AST atau PS3).
     * @return Integer nomor urut terakhir yang ditemukan, atau 0 jika belum ada.
     * @throws SQLException jika terjadi kesalahan database.
     */
    private int getLastNumberFromDb(Connection conn, String columnName, String prefix) throws SQLException {
        int lastNumber = 0;
        String query = "SELECT " + columnName + " FROM aset WHERE " + columnName + " LIKE ? ORDER BY " + columnName
                + " DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastValue = rs.getString(columnName);
                if (lastValue != null && lastValue.startsWith(prefix)) {
                    String numberPart = lastValue.substring(prefix.length());
                    if (numberPart.matches("\\d+")) {
                        lastNumber = Integer.parseInt(numberPart);
                    }
                }
            }
        }
        return lastNumber;
    }

    /**
     * Menentukan prefix kode barang berdasarkan kategori.
     *
     * @param kategori Nama kategori aset.
     * @return String prefix kode barang (misal: PS3, PS4, TV29).
     */
    private String generateKodeBarang(String kategori) {
        String prefix;
        switch (kategori.toUpperCase()) {
            case "PLAYSTATION 3":
                prefix = "PS3";
                break;
            case "PLAYSTATION 4":
                prefix = "PS4";
                break;
            case "PLAYSTATION 5":
                prefix = "PS5";
                break;
            case "TV 29INCH":
                prefix = "TV29";
                break;
            case "TV 40INCH":
                prefix = "TV40";
                break;
            case "STICK PS4":
                prefix = "SPS4";
                break;
            case "STICK PS5":
                prefix = "SPS5";
                break;
            case "NINTENDO":
                prefix = "NT";
                break;
            case "VIP ROOM":
                prefix = "VIP";
                break;
            default:
                prefix = "KD";
                break;
        }
        return prefix;
    }
}
