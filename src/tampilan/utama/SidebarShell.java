package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import tampilan.util.UIStyle;
import tampilan.util.UIStyle.HoverLabel;
import tampilan.booking.BookingManual;
import tampilan.booking.PlayAtHomeManual;
import tampilan.aset.AsetControl;
import tampilan.aset.AsetManajemenPanel;
import tampilan.monitoring.MonitoringPanel;

public class SidebarShell extends JPanel {

    private JPanel contentArea;

    public SidebarShell(JComponent content) {
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.PRIMARY);
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        JLabel logo = new JLabel("BYEBELI");
        logo.setFont(UIStyle.fontBold(22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sidebar.add(logo);

        String[] menu = {"Home", "Booking", "Play At Home", "Aset Control", "Manajemen Aset", "Monitoring", "Logout"};
        for (String item : menu) {
            HoverLabel label = new HoverLabel(item);
            label.setFont(UIStyle.fontRegular(14));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            label.setMaximumSize(new Dimension(160, 40));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(new Color(0, 0, 0, 0));

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
                            javaapplication1.MainFrame.setPage(new SidebarShell(new AsetManajemenPanel()), "manajemenaset");
                            break;
                        case "Monitoring":
                            javaapplication1.MainFrame.setPage(new SidebarShell(new MonitoringPanel()), "monitoring");
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

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UIStyle.BACKGROUND);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentArea.add(content, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }
} 
