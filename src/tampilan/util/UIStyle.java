
package tampilan.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.LineBorder;

public class UIStyle {

    // Palet warna refined
    public static final Color PRIMARY = Color.decode("#3674B5");       // Biru tua profesional
    public static final Color PRIMARY_LIGHT = Color.decode("#5B9DDC"); // Biru terang untuk hover
    public static final Color ACCENT = Color.decode("#A1E3F9");        // Aksen lembut
    public static final Color SECONDARY = new Color(108, 117, 125);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color BACKGROUND = new Color(248, 249, 250);
// Abu muda netral
    public static final Color TEXT = Color.decode("#222222");

    // Font
    public static Font fontRegular(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    // Panel bundar dengan outline & shadow halus
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;

        public RoundedPanel(int radius) {
            super();
            this.cornerRadius = radius;
            setOpaque(false);
            setBorder(new LineBorder(new Color(0, 0, 0, 30), 1)); // outline lembut
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

            // Efek shadow dalam
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawRoundRect(1, 1, width - 3, height - 3, arcs.width, arcs.height);
        }
    }

    
    
    public static class HoverLabel extends JLabel {
        private Color defaultBackground = UIStyle.PRIMARY; // hitam
        private Color hoverBackground = UIStyle.PRIMARY_LIGHT;
        private Color defaultForeground = Color.WHITE;

        public HoverLabel(String text) {
            super(text);
            setOpaque(true);
            setBackground(defaultBackground);
            setForeground(defaultForeground);
            setHorizontalAlignment(SwingConstants.CENTER);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverBackground);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(defaultBackground);
                }
            });
        }
    }

     public static class RoundedButton extends JButton {
        private final int radius;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBackground(PRIMARY);
            setForeground(Color.BLACK);
            setFont(fontBold(14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(new Color(0, 0, 0, 30));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }
    
    public static JButton modernButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(fontBold(14));
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_LIGHT);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY);
            }
        });

        return button;
    }
}
