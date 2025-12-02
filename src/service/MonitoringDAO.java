package service;

import connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class MonitoringDAO {

    public int[] getSummaryData() throws SQLException {
        int[] summary = new int[3]; // 0: tersedia, 1: disewa, 2: booking

        String tersediaQuery = "SELECT COUNT(*) FROM aset WHERE status_tersedia = 1 AND status_disewakan = 1";
        String disewaQuery = "SELECT COUNT(DISTINCT pd.id_aset) FROM playathome_detail pd " +
                "JOIN playathome p ON pd.id_playhome = p.id_playhome " +
                "WHERE p.status = 'aktif'";
        String bookingQuery = "SELECT COUNT(DISTINCT bd.id_aset) FROM booking_detail bd " +
                "JOIN booking b ON bd.id_booking = b.id_booking " +
                "WHERE b.status IS NULL OR b.status NOT IN ('completed', 'cancelled')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pst = conn.prepareStatement(tersediaQuery);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    summary[0] = rs.getInt(1);
            }

            try (PreparedStatement pst = conn.prepareStatement(disewaQuery);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    summary[1] = rs.getInt(1);
            }

            try (PreparedStatement pst = conn.prepareStatement(bookingQuery);
                    ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    summary[2] = rs.getInt(1);
            }
        }
        return summary;
    }

    public DefaultTableModel getBookingData() throws SQLException {
        String[] columns = { "ID Booking", "Nama Pelanggan", "Item", "Tanggal", "Jam", "Durasi", "Status", "Aksi" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        String bookingQuery = "SELECT b.id_booking as id, b.nama, " +
                "GROUP_CONCAT(a.nama_barang SEPARATOR ', ') as items, " +
                "b.tanggal, b.jam, b.durasi_menit, " +
                "'Booking' as jenis, " +
                "COALESCE(b.status, 'Booking') as status " +
                "FROM booking b " +
                "JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                "LEFT JOIN aset a ON bd.id_aset = a.id_aset " +
                "WHERE (b.status IS NULL OR b.status NOT IN ('completed', 'cancelled')) " +
                "AND b.tanggal >= CURDATE() " +
                "GROUP BY b.id_booking ";

        String playhomeQuery = "SELECT p.id_playhome as id, p.nama, " +
                "GROUP_CONCAT(a.nama_barang SEPARATOR ', ') as items, " +
                "p.tgl_mulai as tanggal, NULL as jam, " +
                "DATEDIFF(p.tgl_selesai, p.tgl_mulai) as durasi_menit, " +
                "'PlayAtHome' as jenis, " +
                "'Disewa' as status " +
                "FROM playathome p " +
                "JOIN playathome_detail pd ON p.id_playhome = pd.id_playhome " +
                "LEFT JOIN aset a ON pd.id_aset = a.id_aset " +
                "WHERE p.status = 'aktif' " +
                "GROUP BY p.id_playhome ";

        String fullQuery = "(" + bookingQuery + ") UNION (" + playhomeQuery + ") " +
                "ORDER BY tanggal DESC, jam DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(fullQuery);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("jenis") + "-" + rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("items"),
                        rs.getDate("tanggal"),
                        rs.getTime("jam"),
                        rs.getString("jenis").equals("Booking") ? rs.getInt("durasi_menit") + " menit"
                                : rs.getInt("durasi_menit") + " hari",
                        rs.getString("status"),
                        rs.getString("status").equals("Disewa") ? "Aktif" : "Kembalikan"
                });
            }
        }
        return model;
    }

    public void kembalikanItem(String idTransaksi) throws SQLException, IllegalArgumentException {
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
                try (PreparedStatement pstAset = conn.prepareStatement(updateAset)) {
                    pstAset.setInt(1, id);
                    pstAset.executeUpdate();
                }

                // Update status booking menjadi completed
                String updateBooking = "UPDATE booking SET status = 'completed' WHERE id_booking = ?";
                try (PreparedStatement pstBooking = conn.prepareStatement(updateBooking)) {
                    pstBooking.setInt(1, id);
                    pstBooking.executeUpdate();
                }

            } else if (jenis.equals("PlayAtHome")) {
                // Update status playathome menjadi selesai
                String updatePlayhome = "UPDATE playathome SET status = 'selesai' WHERE id_playhome = ?";
                try (PreparedStatement pstPlayhome = conn.prepareStatement(updatePlayhome)) {
                    pstPlayhome.setInt(1, id);
                    pstPlayhome.executeUpdate();
                }

                // Update status aset yang terkait
                String updateAset = "UPDATE aset a " +
                        "JOIN playathome_detail pd ON a.id_aset = pd.id_aset " +
                        "SET a.status_tersedia = 1 " +
                        "WHERE pd.id_playhome = ?";
                try (PreparedStatement pstAset = conn.prepareStatement(updateAset)) {
                    pstAset.setInt(1, id);
                    pstAset.executeUpdate();
                }
            } else {
                throw new IllegalArgumentException("Jenis transaksi tidak dikenali: " + jenis);
            }

            conn.commit();
        }
    }
}
