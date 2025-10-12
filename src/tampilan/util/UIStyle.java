package tampilan.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.toedter.calendar.JTextFieldDateEditor;

import tampilan.aset.AsetControl;
import tampilan.aset.AsetManajemenPanel;
import tampilan.booking.BookingManual;
import tampilan.booking.PlayAtHomeRentalPanel;
import tampilan.monitoring.MonitoringPanel;

public class UIStyle {

    // Palet warna modern dan trendy
    public static final Color PRIMARY = Color.decode("#4A6FFF");       // Biru cerah modern
    public static final Color PRIMARY_LIGHT = Color.decode("#6B8CFF"); // Biru terang untuk hover
    public static final Color PRIMARY_DARK = Color.decode("#3A5DE8");  // Biru gelap untuk aksen
    public static final Color ACCENT = Color.decode("#FF6B9D");        // Pink cerah untuk aksen
    public static final Color SECONDARY = Color.decode("#8A8D93");     // Abu-abu netral
    public static final Color SUCCESS_COLOR = Color.decode("#47D764"); // Hijau terang modern
    public static final Color WARNING_COLOR = Color.decode("#FFC107"); // Kuning
    public static final Color DANGER_COLOR = Color.decode("#FF4757");  // Merah modern
    public static final Color BACKGROUND = Color.decode("#F5F7FF");    // Biru sangat muda
    public static final Color CARD_BG = Color.WHITE;                   // Putih untuk kartu
    public static final Color TEXT = Color.decode("#2D3748");          // Abu-abu gelap untuk teks
    public static final Color TEXT_LIGHT = Color.decode("#718096");    // Abu-abu medium untuk teks sekunder

    // Font
    public static Font fontRegular(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontMedium(int size) {
        return new Font("Segoe UI", Font.PLAIN, size); // Medium weight akan diatur dengan turunan
    }

    public static Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    // Panel bundar dengan shadow modern
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private boolean shadow = true;

        public RoundedPanel(int radius) {
            this(radius, true);
        }
        
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
            
            // Efek shadow
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, cornerRadius, cornerRadius);
            }
            
            // Panel utama
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius);
            
            // Border halus
            g2.setColor(new Color(0, 0, 0, 10));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius);
        }
    }

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

    // Tombol dengan desain modern
    public static class RoundedButton extends JButton {
        private final int radius;
        private Color hoverBackground = PRIMARY_LIGHT;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBackground(PRIMARY);
            setForeground(Color.WHITE);
            setFont(fontMedium(14));
            setBorder(new EmptyBorder(12, 20, 12, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverBackground);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(PRIMARY);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background
            if (getModel().isPressed()) {
                g2.setColor(PRIMARY_DARK);
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
            // Tidak menggambar border default
        }
    }
    
    // Metode untuk membuat tombol modern
    public static JButton modernButton(String text) {
        JButton button = new RoundedButton(text, 8);
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
            new EmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
    }
    
    // Style untuk combo box modern
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(fontRegular(14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0, 0, 0, 20), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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
            new EmptyBorder(10, 12, 10, 12)
        ));
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
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(74, 111, 255, 30));
        table.setSelectionForeground(TEXT);
        
        // Center align header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }
    
    // Dialog pesan modern
    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorMessage(MonitoringPanel parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    /**
     * Memberikan gaya modern pada komponen JDateChooser.
     * @param dateChooser Komponen JDateChooser yang akan di-style.
     */
    public static void styleDateChooser(com.toedter.calendar.JDateChooser dateChooser) {
        dateChooser.setFont(fontRegular(14));
        dateChooser.setPreferredSize(new Dimension(200, 40));
        dateChooser.setBackground(Color.WHITE);
        dateChooser.getJCalendar().getDayChooser().setWeekdayForeground(TEXT_LIGHT);
        dateChooser.getJCalendar().getDayChooser().setSundayForeground(DANGER_COLOR);
        dateChooser.getJCalendar().getDayChooser().setDecorationBackgroundColor(PRIMARY_LIGHT);
        
        // Style text field di dalam JDateChooser
        JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();
        editor.setFont(fontRegular(14));
        editor.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0, 0, 0, 20), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
    }
}