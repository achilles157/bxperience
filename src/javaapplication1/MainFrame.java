package javaapplication1;

import javax.swing.*;
import tampilan.utama.TampilanLoginManual;
import tampilan.utama.TampilanHomeManual;
import tampilan.booking.BookingManual;
import tampilan.booking.PlayAtHomeManual;
import tampilan.aset.AsetControl;
import tampilan.aset.AsetManajemenPanel;
import tampilan.utama.SidebarShell;
import tampilan.monitoring.MonitoringPanel;
import java.awt.*;

public class MainFrame extends JFrame {

    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainPanel = new JPanel(cardLayout);

    public static void setPage(JComponent component, String name) {
        mainPanel.add(component, name);
        showPage(name);
    }

    public MainFrame() {
        setTitle("Byebeli Experience");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        // Tambahkan semua tampilan sebagai panel
        mainPanel.add(new TampilanLoginManual(), "login");
        mainPanel.add(new TampilanHomeManual(), "home");
        mainPanel.add(new SidebarShell(new TampilanHomeManual()), "home");
        mainPanel.add(new SidebarShell(new BookingManual()), "booking");
        mainPanel.add(new SidebarShell(new PlayAtHomeManual()), "playathome");
        mainPanel.add(new SidebarShell(new AsetControl()), "aset");
        mainPanel.add(new SidebarShell(new AsetManajemenPanel()), "manajemenaset");
        mainPanel.add(new SidebarShell(new MonitoringPanel()), "monitoringpanel");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    public static void showPage(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
} 
