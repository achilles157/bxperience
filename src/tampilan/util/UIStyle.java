package tampilan.util;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.toedter.calendar.JTextFieldDateEditor;

public class UIStyle {

    // --- COLORS (CONSOLERENT INDONESIA THEME) ---

    public static final Color PRIMARY = new Color(21, 62, 161);
    public static final Color PRIMARY_DARK = new Color(15, 45, 115);
    public static final Color PRIMARY_LIGHT = new Color(50, 90, 190);

    public static final Color SECONDARY = new Color(215, 0, 38);
    public static final Color SECONDARY_DARK = new Color(170, 0, 30);
    public static final Color SECONDARY_LIGHT = new Color(255, 50, 70);

    public static final Color BACKGROUND = new Color(238, 240, 242);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT = new Color(33, 33, 33);
    public static final Color TEXT_LIGHT = new Color(117, 117, 117);

    // Warna untuk status (success, danger, warning) tetap sama atau disesuaikan

    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    public static final Color DANGER_COLOR = new Color(244, 67, 54);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);

    // Font
    public static Font fontRegular(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontMedium(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    // Typography Helpers
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(fontBold(24));
        label.setForeground(PRIMARY);
        return label;
    }

    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(fontMedium(18));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(fontMedium(14));
        label.setForeground(TEXT);
        return label;
    }

    // Panel bundar dengan shadow modern - Diganti dengan
    // tampilan.components.RoundedPanel
    // Label dengan efek hover modern
    public static class HoverLabel extends JLabel {
        private Color defaultBackground = UIStyle.PRIMARY;
        private Color hoverBackground = UIStyle.PRIMARY_LIGHT;
        private Color defaultForeground = Color.WHITE;
        private int borderRadius = 8;

        public HoverLabel(String text) {
            super(text);
            setOpaque(false);
            setForeground(defaultForeground);
            setHorizontalAlignment(SwingConstants.CENTER);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFont(fontMedium(14));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverBackground);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(defaultBackground);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background dengan rounded corners
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), borderRadius, borderRadius);

            super.paintComponent(g2);
            g2.dispose();
        }

        public void setBackground(Color bg) {
            super.setBackground(bg);
            defaultBackground = bg;
        }
    }

    // Tombol dengan desain modern - Diganti dengan
    // tampilan.components.RoundedButton

    // Metode untuk membuat tombol modern
    public static JButton modernButton(String text) {
        tampilan.components.RoundedButton button = new tampilan.components.RoundedButton(text, 8);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(fontMedium(14));
        return button;
    }

    // Style untuk text field modern
    public static void styleTextField(JTextField field) {
        field.setFont(fontRegular(14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        field.setBackground(Color.WHITE);

        // Add focus listener for visual feedback
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(PRIMARY, 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0, 0, 0, 20), 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }
        });
    }

    // Style untuk combo box modern
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(fontRegular(14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
                return label;
            }
        });
    }

    // Style untuk text area modern
    public static void styleTextArea(JTextArea area) {
        area.setFont(fontRegular(14));
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        area.setBackground(Color.WHITE);
    }

    // Style untuk checkbox modern
    public static void styleCheckBox(JCheckBox checkbox) {
        checkbox.setFont(fontRegular(14));
        checkbox.setFocusPainted(false);
        checkbox.setBackground(null);
    }

    // Style untuk tabel modern
    public static void styleTable(JTable table) {
        table.setFont(fontRegular(14));
        table.getTableHeader().setFont(fontMedium(14));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(false);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(74, 111, 255, 30));
        table.setSelectionForeground(TEXT);

        // Center align header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setBackground(PRIMARY);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Zebra striping and padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // Dialog pesan modern
    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static String formatCurrency(double amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
        return formatter.format(amount);
    }

    /**
     * Memberikan gaya modern pada komponen JDateChooser.
     * 
     * @param dateChooser Komponen JDateChooser yang akan di-style.
     */
    public static void styleDateChooser(com.toedter.calendar.JDateChooser dateChooser) {

        dateChooser.setFont(fontRegular(14));

        JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();

        editor.setFont(fontRegular(14));
        editor.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        dateChooser.setPreferredSize(new Dimension(150, 40));

        dateChooser.getJCalendar().getDayChooser().setWeekdayForeground(TEXT_LIGHT);
        dateChooser.getJCalendar().getDayChooser().setSundayForeground(DANGER_COLOR);
        dateChooser.getJCalendar().getDayChooser().setDecorationBackgroundColor(PRIMARY_LIGHT);
    }

    // Custom scroll bar UI for a modern look
    public static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private static final int SCROLL_BAR_ALPHA_ROLLOVER = 100;
        private static final int SCROLL_BAR_ALPHA = 60;
        private static final int THUMB_SIZE = 8;
        private static final Color THUMB_COLOR = PRIMARY;

        public ModernScrollBarUI() {
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Paint no track
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            int alpha = isThumbRollover() ? SCROLL_BAR_ALPHA_ROLLOVER : SCROLL_BAR_ALPHA;
            int orientation = scrollbar.getOrientation();
            int x = thumbBounds.x;
            int y = thumbBounds.y;

            int width = orientation == JScrollBar.VERTICAL ? THUMB_SIZE : thumbBounds.width;
            width = Math.max(width, THUMB_SIZE);

            int height = orientation == JScrollBar.VERTICAL ? thumbBounds.height : THUMB_SIZE;
            height = Math.max(height, THUMB_SIZE);

            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(THUMB_COLOR.getRed(), THUMB_COLOR.getGreen(), THUMB_COLOR.getBlue(), alpha));
            graphics2D.fillRoundRect(x, y, width, height, 10, 10);
            graphics2D.dispose();
        }

        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, width, height);
            scrollbar.repaint();
        }
    }
}
