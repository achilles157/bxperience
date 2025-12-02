package tampilan.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import tampilan.util.UIStyle;

public class RoundedButton extends JButton {
    private final int radius;
    private Color hoverBackground = UIStyle.PRIMARY_LIGHT;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBackground(UIStyle.PRIMARY);
        setForeground(Color.WHITE);
        setFont(UIStyle.fontMedium(14));
        setBorder(new EmptyBorder(12, 20, 12, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackground);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UIStyle.PRIMARY);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        if (getModel().isPressed()) {
            g2.setColor(UIStyle.PRIMARY_DARK);
        } else if (getModel().isRollover()) {
            g2.setColor(hoverBackground);
        } else {
            g2.setColor(getBackground());
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // No default border
    }
}
