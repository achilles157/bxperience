package tampilan.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import tampilan.util.UIStyle;

/**
 * TextField kustom dengan sudut membulat dan border modern.
 * Memberikan feedback visual saat fokus.
 */
public class RoundedTextField extends JTextField {

    /**
     * Membuat text field dengan jumlah kolom tertentu.
     *
     * @param columns Jumlah kolom (lebar).
     */
    public RoundedTextField(int columns) {
        super(columns);
        initStyle();
    }

    /**
     * Membuat text field default.
     */
    public RoundedTextField() {
        super();
        initStyle();
    }

    private void initStyle() {
        setFont(UIStyle.fontRegular(14));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        setBackground(Color.WHITE);

        // Add focus listener for visual feedback
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIStyle.PRIMARY, 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0, 0, 0, 20), 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }
        });
    }
}
