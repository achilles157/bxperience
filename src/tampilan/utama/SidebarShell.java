package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import tampilan.util.UIStyle;
import tampilan.util.UIStyle.HoverLabel;
import tampilan.booking.BookingManual;
import tampilan.playathome.PlayAtHomeManual;
import tampilan.components.RoundedPanel;
import tampilan.aset.AsetControl;
import tampilan.aset.AsetManajemenPanel;
import tampilan.monitoring.MonitoringPanel;
import tampilan.laporan.LaporanContainerPanel;

public class SidebarShell extends JPanel {

    private JPanel contentArea;

    public SidebarShell(JComponent content) {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        // Sidebar panel dengan efek shadow
        RoundedPanel sidebar = new RoundedPanel(0, false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.PRIMARY);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // Logo/Branding
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        JLabel logo = new JLabel("Consolerent ID");
        logo.setFont(UIStyle.fontBold(20));
        logo.setForeground(Color.WHITE);
        logoPanel.add(logo);

        sidebar.add(logoPanel);

        // Menu items
        String[] menu = { "Home", "Booking", "Play At Home", "Aset Control", "Manajemen Aset", "Monitoring", "Laporan",
                "Logout" };

        for (String item : menu) {
            HoverLabel label = new HoverLabel(item);
            label.setFont(UIStyle.fontMedium(14));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            label.setMaximumSize(new Dimension(180, 40));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setOpaque(false);
            label.setBackground(new Color(0, 0, 0, 0));
            label.setForeground(Color.WHITE);

            label.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    switch (item) {
                        case "Home":
                            javaapplication1.MainFrame.showPage("home");
                            break;
                        case "Booking":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new BookingManual()), "booking");
                            break;
                        case "Play At Home":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new PlayAtHomeManual()), "playathome");
                            break;
                        case "Aset Control":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new AsetControl()), "aset");
                            break;
                        case "Manajemen Aset":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new AsetManajemenPanel()),
                                    "manajemenaset");
                            break;
                        case "Monitoring":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new MonitoringPanel()), "monitoring");
                            break;
                        case "Laporan":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new LaporanContainerPanel()),
                                    "laporan");
                            break;
                        case "Logout":
                            javaapplication1.MainFrame.showPage("login");
                            break;
                    }
                }
            });

            sidebar.add(Box.createVerticalStrut(5));
            sidebar.add(label);
        }

        // Add some space at the bottom
        sidebar.add(Box.createVerticalGlue());

        // Content area
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UIStyle.BACKGROUND);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentArea.add(content, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }
}