
package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TampilanHomeManual extends JPanel {

    public TampilanHomeManual() {
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel panelSatu = new JPanel(new BorderLayout(10, 10));
        panelSatu.setBackground(Color.WHITE);
        panelSatu.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel logo = new JLabel(resizeIcon("/img/icon.png", 60, 60));
        JLabel title = new JLabel("BYEBELI EXPERIENCE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(54, 116, 181));
        panelSatu.add(logo, BorderLayout.WEST);
        panelSatu.add(title, BorderLayout.CENTER);

        // Sidebar removed - now handled by SidebarShell

        // Konten Tengah
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel image = new JLabel(resizeIcon("/img/png1.png", 300, 150));
        image.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome to Bybeli Experience Admin System");
        welcome.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JTextArea infoArea = new JTextArea(
            "Byebeli Experience adalah layanan yang menghadirkan pengalaman bermain game terbaik "
          + "tanpa harus memiliki peralatannya sendiri. Kami menyediakan berbagai alat gaming untuk disewa – "
          + "mulai dari konsol, PC high-end, hingga VR set – yang siap digunakan untuk keperluan pribadi, komunitas, atau event."
          + "Dengan layanan cepat, alat yang terawat, dan dukungan teknis profesional, Byebeli memastikan setiap pelanggan "
          + "merasakan kemudahan, kenyamanan, dan keseruan maksimal dalam bermain game."
        );
        infoArea.setWrapStyleWord(true);
        infoArea.setLineWrap(true);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setBackground(new Color(248, 248, 248));
        infoArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setPreferredSize(new Dimension(800, 160));
        infoScroll.setMaximumSize(new Dimension(850, 200));

        content.add(image);
        content.add(welcome);
        content.add(infoScroll);

        // Footer
        JPanel footer = new JPanel();
        JLabel footerText = new JLabel("© 2025 Bybeli Experience Admin System v1.0.0 | Internal Use Only");
        footerText.setForeground(Color.DARK_GRAY);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        footer.add(footerText);

        // Layout utama
        setLayout(new BorderLayout());
        add(panelSatu, BorderLayout.NORTH);
        // Sidebar panel removed - now handled externally
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

    }

    private Icon resizeIcon(String path, int width, int height) {
        try {
            BufferedImage img = ImageIO.read(getClass().getResource(path));
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException | IllegalArgumentException e) {
            return new ImageIcon(); // return empty icon if image not found
        }
    }
}
