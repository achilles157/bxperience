package tampilan.components;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import tampilan.util.UIStyle;

/**
 * Panel custom untuk menampilkan Bar Chart sederhana menggunakan Graphics2D.
 */
public class SimpleBarChartPanel extends JPanel {

    private List<String> categories;
    private List<Double> values;
    private double maxValue;
    private NumberFormat currencyFormat;

    public SimpleBarChartPanel() {
        this.categories = new ArrayList<>();
        this.values = new ArrayList<>();
        this.maxValue = 0;
        this.currencyFormat = NumberFormat
                .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());

        setBackground(UIStyle.CARD_BG);
        setPreferredSize(new Dimension(0, 250)); // Default height
    }

    public void setData(List<String> categories, List<Double> values) {
        this.categories = categories;
        this.values = values;
        this.maxValue = 0;
        for (Double val : values) {
            if (val > maxValue) {
                maxValue = val;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (categories.isEmpty() || values.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        int bottomPadding = 60; // Increased to allow multi-line labels

        // Draw background
        g2.setColor(getBackground());
        g2.fillRect(0, 0, width, height);

        // Calculate dimensions
        int chartWidth = width - (2 * padding);
        int chartHeight = height - (padding + bottomPadding);
        int barWidth = chartWidth / categories.size();
        int gap = barWidth / 4;
        int actualBarWidth = barWidth - gap;

        // Draw bars
        for (int i = 0; i < categories.size(); i++) {
            double value = values.get(i);
            int barHeight = (int) ((value / maxValue) * chartHeight);

            // Avoid zero height for visibility if value > 0
            if (value > 0 && barHeight < 2)
                barHeight = 2;

            int x = padding + (i * barWidth) + (gap / 2);
            int y = height - bottomPadding - barHeight;

            // Bar
            g2.setColor(UIStyle.PRIMARY);
            g2.fillRoundRect(x, y, actualBarWidth, barHeight, 10, 10);

            // Value Label (top of bar)
            g2.setColor(UIStyle.TEXT);
            g2.setFont(UIStyle.fontMedium(10));
            String valueText = currencyFormat.format(value);
            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(valueText);
            g2.drawString(valueText, x + (actualBarWidth - stringWidth) / 2, y - 5);

            // Category Label (bottom of bar)
            String category = categories.get(i);
            int catStringWidth = fm.stringWidth(category);

            // Smart Label Rendering logic
            if (catStringWidth <= actualBarWidth) {
                // Determine horizontal center
                g2.drawString(category, x + (actualBarWidth - catStringWidth) / 2, height - bottomPadding + 15);
            } else {
                // Try to split into two lines
                String[] words = category.split(" ");
                if (words.length > 1) {
                    StringBuilder line1 = new StringBuilder();
                    StringBuilder line2 = new StringBuilder();

                    for (String word : words) {
                        if (fm.stringWidth(line1.toString() + word) < actualBarWidth) {
                            line1.append(word).append(" ");
                        } else {
                            line2.append(word).append(" ");
                        }
                    }

                    // Draw Line 1
                    String s1 = line1.toString().trim();
                    g2.drawString(s1, x + (actualBarWidth - fm.stringWidth(s1)) / 2, height - bottomPadding + 15);

                    // Draw Line 2
                    String s2 = line2.toString().trim();
                    g2.drawString(s2, x + (actualBarWidth - fm.stringWidth(s2)) / 2, height - bottomPadding + 30);
                } else {
                    // Single long word, draw as much as possible centered
                    g2.drawString(category, x + (actualBarWidth - catStringWidth) / 2, height - bottomPadding + 15);
                }
            }
        }

        // Draw baseline
        g2.setColor(Color.GRAY);
        g2.drawLine(padding, height - bottomPadding, width - padding, height - bottomPadding);
    }
}
