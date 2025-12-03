package tampilan.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel kustom dengan sudut membulat dan opsi efek bayangan (shadow).
 * Cocok untuk container kartu atau bagian UI yang terpisah.
 */
public class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private boolean shadow = true;

    /**
     * Membuat panel rounded dengan shadow default (aktif).
     *
     * @param radius Radius sudut panel.
     */
    public RoundedPanel(int radius) {
        this(radius, true);
    }

    /**
     * Membuat panel rounded dengan opsi shadow.
     *
     * @param radius Radius sudut panel.
     * @param shadow True jika ingin menampilkan bayangan, false jika tidak.
     */
    public RoundedPanel(int radius, boolean shadow) {
        super();
        this.cornerRadius = radius;
        this.shadow = shadow;
        setOpaque(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow effect
        if (shadow) {
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, cornerRadius, cornerRadius);
        }

        // Main panel background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Subtle border
        g2.setColor(new Color(0, 0, 0, 10));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
    }
}
