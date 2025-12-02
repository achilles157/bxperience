package tampilan.laporan;

import javax.swing.*;
import java.awt.*;
import tampilan.util.UIStyle;

public class LaporanContainerPanel extends JPanel {

    private JTabbedPane tabbedPane;

    public LaporanContainerPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        // Buat komponen TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.fontMedium(16));
        tabbedPane.setBackground(UIStyle.CARD_BG);
        tabbedPane.setForeground(UIStyle.PRIMARY);

        LaporanPendapatanPanel laporanPendapatan = new LaporanPendapatanPanel();
        tabbedPane.addTab("  Pendapatan  ", laporanPendapatan);

        LaporanAsetTerlarisPanel laporanAset = new LaporanAsetTerlarisPanel();
        tabbedPane.addTab("  Aset Terlaris  ", laporanAset);

        LaporanPelangganPanel laporanPelanggan = new LaporanPelangganPanel();
        tabbedPane.addTab("  Aktivitas Pelanggan  ", laporanPelanggan);

        LaporanKategoriPanel laporanKategori = new LaporanKategoriPanel();
        tabbedPane.addTab("  Kategori  ", laporanKategori);

        LaporanStatusAsetPanel laporanStatusAset = new LaporanStatusAsetPanel();
        tabbedPane.addTab("  Status Aset  ", laporanStatusAset);

        add(tabbedPane, BorderLayout.CENTER);
    }
}