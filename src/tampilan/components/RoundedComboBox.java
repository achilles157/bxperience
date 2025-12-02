package tampilan.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import tampilan.util.UIStyle;

public class RoundedComboBox<E> extends JComboBox<E> {

    public RoundedComboBox() {
        super();
        initStyle();
    }

    public RoundedComboBox(E[] items) {
        super(items);
        initStyle();
    }

    private void initStyle() {
        setFont(UIStyle.fontRegular(14));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        setRenderer(new DefaultListCellRenderer() {
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
}
