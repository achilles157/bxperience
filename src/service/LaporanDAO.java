package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import connection.DatabaseConnection;

/**
 * Data Access Object for generating reports.
 */
public class LaporanDAO {

    /**
     * Mengambil laporan aset terlaris berdasarkan filter waktu dan jenis transaksi.
     *
     * @param from        Tanggal awal periode.
     * @param to          Tanggal akhir periode.
     * @param jenisFilter Filter jenis transaksi ("Booking", "PlayAtHome", atau
     *                    "Semua").
     * @return List data aset terlaris (peringkat, nama, kategori, total disewa).
     * @throws SQLException jika terjadi kesalahan database.
     */
    public List<Object[]> getAsetTerlaris(Date from, Date to, String jenisFilter) throws SQLException {
        List<Object[]> data = new ArrayList<>();

        // Query untuk mengambil data booking
        String bookingSql = "SELECT bd.id_aset, COUNT(bd.id_aset) as jumlah " +
                "FROM booking_detail bd " +
                "JOIN booking b ON bd.id_booking = b.id_booking " +
                "WHERE b.status = 'completed' AND b.tanggal BETWEEN ? AND ? " +
                "GROUP BY bd.id_aset";

        // Query untuk mengambil data playathome
        String playAtHomeSql = "SELECT pd.id_aset, COUNT(pd.id_aset) as jumlah " +
                "FROM playathome_detail pd " +
                "JOIN playathome p ON pd.id_playhome = p.id_playhome " +
                "WHERE p.status = 'selesai' AND p.tgl_selesai BETWEEN ? AND ? " +
                "GROUP BY pd.id_aset";

        String combinedSql;
        if ("Booking".equals(jenisFilter)) {
            combinedSql = bookingSql;
        } else if ("PlayAtHome".equals(jenisFilter)) {
            combinedSql = playAtHomeSql;
        } else {
            combinedSql = "(" + bookingSql + ") UNION ALL (" + playAtHomeSql + ")";
        }

        String finalQuery = "SELECT a.nama_barang, a.kategori, SUM(t.jumlah) as total_disewa " +
                "FROM (" + combinedSql + ") as t " +
                "JOIN aset a ON t.id_aset = a.id_aset " +
                "GROUP BY a.id_aset, a.nama_barang, a.kategori " +
                "ORDER BY total_disewa DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(finalQuery)) {

            pst.setDate(1, new java.sql.Date(from.getTime()));
            pst.setDate(2, new java.sql.Date(to.getTime()));

            if ("Semua".equals(jenisFilter)) {
                pst.setDate(3, new java.sql.Date(from.getTime()));
                pst.setDate(4, new java.sql.Date(to.getTime()));
            }

            try (ResultSet rs = pst.executeQuery()) {
                int peringkat = 1;
                while (rs.next()) {
                    data.add(new Object[] {
                            peringkat++,
                            rs.getString("nama_barang"),
                            rs.getString("kategori"),
                            rs.getInt("total_disewa")
                    });
                }
            }
        }
        return data;
    }

    /**
     * Mengambil laporan pendapatan harian.
     * Menggabungkan data dari Booking dan PlayAtHome.
     *
     * @param from Tanggal awal periode.
     * @param to   Tanggal akhir periode.
     * @return List data pendapatan (tanggal, jenis, jumlah transaksi, total
     *         nominal).
     * @throws SQLException jika terjadi kesalahan database.
     */
    public List<Object[]> getPendapatan(Date from, Date to) throws SQLException {
        List<Object[]> data = new ArrayList<>();
        String sqlBooking = "SELECT DATE(created_at) as tanggal, 'Booking' as jenis, " +
                "COUNT(id_booking) as jumlah, SUM(total_harga) as total " +
                "FROM booking " +
                "WHERE status IN ('completed', 'confirmed') AND DATE(created_at) BETWEEN ? AND ? " +
                "GROUP BY DATE(created_at)";

        String sqlPlayAtHome = "SELECT tgl_selesai as tanggal, 'PlayAtHome' as jenis, " +
                "COUNT(id_playhome) as jumlah, SUM(total_harga) as total " +
                "FROM playathome " +
                "WHERE status = 'selesai' AND tgl_selesai BETWEEN ? AND ? " +
                "GROUP BY tgl_selesai";

        String finalQuery = "(" + sqlBooking + ") UNION ALL (" + sqlPlayAtHome + ") ORDER BY tanggal ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(finalQuery)) {

            pst.setDate(1, new java.sql.Date(from.getTime()));
            pst.setDate(2, new java.sql.Date(to.getTime()));
            pst.setDate(3, new java.sql.Date(from.getTime()));
            pst.setDate(4, new java.sql.Date(to.getTime()));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data.add(new Object[] {
                            rs.getDate("tanggal"),
                            rs.getString("jenis"),
                            rs.getInt("jumlah"),
                            rs.getDouble("total")
                    });
                }
            }
        }
        return data;
    }

    /**
     * Mengambil laporan pelanggan paling aktif.
     *
     * @param from   Tanggal awal periode.
     * @param to     Tanggal akhir periode.
     * @param sortBy Kriteria pengurutan ("Total Belanja" atau "Jumlah Transaksi").
     * @return List data pelanggan (peringkat, nama, instagram, total transaksi,
     *         total belanja).
     * @throws SQLException jika terjadi kesalahan database.
     */
    public List<Object[]> getPelangganAktif(Date from, Date to, String sortBy) throws SQLException {
        List<Object[]> data = new ArrayList<>();
        String sortColumn = "total_belanja"; // Default
        if ("Jumlah Transaksi".equals(sortBy)) {
            sortColumn = "total_transaksi";
        }

        String finalQuery = "SELECT " +
                "    t.nama, " +
                "    t.instagram, " +
                "    COUNT(*) as total_transaksi, " +
                "    SUM(t.total_harga) as total_belanja " +
                "FROM ( " +
                "    (SELECT nama, instagram, total_harga " +
                "     FROM booking " +
                "     WHERE status = 'completed' AND DATE(created_at) BETWEEN ? AND ?) " +
                "    UNION ALL " +
                "    (SELECT nama, instagram, total_harga " +
                "     FROM playathome " +
                "     WHERE status = 'selesai' AND tgl_selesai BETWEEN ? AND ?) " +
                ") as t " +
                "WHERE t.nama IS NOT NULL AND t.nama != '' " +
                "GROUP BY t.nama, t.instagram " +
                "ORDER BY " + sortColumn + " DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(finalQuery)) {

            pst.setDate(1, new java.sql.Date(from.getTime()));
            pst.setDate(2, new java.sql.Date(to.getTime()));
            pst.setDate(3, new java.sql.Date(from.getTime()));
            pst.setDate(4, new java.sql.Date(to.getTime()));

            try (ResultSet rs = pst.executeQuery()) {
                int peringkat = 1;
                while (rs.next()) {
                    data.add(new Object[] {
                            peringkat++,
                            rs.getString("nama"),
                            rs.getString("instagram"),
                            rs.getInt("total_transaksi"),
                            rs.getDouble("total_belanja")
                    });
                }
            }
        }
        return data;
    }

    /**
     * Mengambil ringkasan status aset saat ini.
     * Menghitung jumlah total, tersedia, dan sedang disewa per kategori.
     *
     * @return List data status aset (kategori, total, tersedia, disewakan).
     * @throws SQLException jika terjadi kesalahan database.
     */
    public List<Object[]> getStatusAset() throws SQLException {
        List<Object[]> data = new ArrayList<>();
        String query = "SELECT kategori, COUNT(*) as total, " +
                "SUM(CASE WHEN status_tersedia = 1 THEN 1 ELSE 0 END) as tersedia, " +
                "SUM(CASE WHEN status_tersedia = 0 THEN 1 ELSE 0 END) as disewakan " +
                "FROM aset GROUP BY kategori";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                data.add(new Object[] {
                        rs.getString("kategori"),
                        rs.getInt("total"),
                        rs.getInt("tersedia"),
                        rs.getInt("disewakan")
                });
            }
        }
        return data;
    }

    /**
     * Mengambil laporan kategori aset dengan pendapatan tertinggi.
     *
     * @return List data kategori terlaris (kategori, jumlah transaksi, total
     *         pendapatan).
     * @throws SQLException jika terjadi kesalahan database.
     */
    public List<Object[]> getKategoriTerlaris() throws SQLException {
        List<Object[]> data = new ArrayList<>();
        String query = "SELECT a.kategori, " +
                "COUNT(t.id_transaksi) as jumlah_transaksi, " +
                "SUM(t.total_harga) as total_pendapatan " +
                "FROM (" +
                "  SELECT bd.id_booking as id_transaksi, bd.id_aset, bd.subtotal as total_harga " +
                "  FROM booking_detail bd " +
                "  UNION ALL " +
                "  SELECT pd.id_playhome, pd.id_aset, pd.subtotal " +
                "  FROM playathome_detail pd" +
                ") t " +
                "JOIN aset a ON t.id_aset = a.id_aset " +
                "GROUP BY a.kategori " +
                "ORDER BY total_pendapatan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                data.add(new Object[] {
                        rs.getString("kategori"),
                        rs.getInt("jumlah_transaksi"),
                        rs.getDouble("total_pendapatan")
                });
            }
        }
        return data;
    }
}
