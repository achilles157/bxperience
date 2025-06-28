
package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javaapplication1.MainFrame;
import tampilan.util.UIStyle;


public class TampilanLoginManual extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel feedbackLabel;
    private JCheckBox showPasswordCheck;

    public TampilanLoginManual() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND);

        // Panel Kiri: Logo dan Branding
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(260, 0));
        leftPanel.setBackground(UIStyle.PRIMARY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JLabel logoImage = new JLabel(loadLogo("/img/icon.png", 80, 80));
        logoImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoText = new JLabel("BYEBELI EXPERIENCE");
        logoText.setFont(UIStyle.fontBold(18));
        logoText.setForeground(Color.WHITE);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoText.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel subtitle = new JLabel("Admin Panel");
        subtitle.setFont(UIStyle.fontRegular(14));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoImage);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(logoText);
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalGlue());

        // Panel Kanan: Form login dengan rounded dan shadow
        UIStyle.RoundedPanel formWrapper = new UIStyle.RoundedPanel(20);
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setLayout(new GridBagLayout());
        formWrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UIStyle.fontRegular(14));
        usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UIStyle.fontRegular(14));
        passwordField = new JPasswordField(20);

        showPasswordCheck = new JCheckBox("Tampilkan Password");
        showPasswordCheck.setFont(UIStyle.fontRegular(12));
        showPasswordCheck.setBackground(Color.WHITE);
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '•');
        });

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setFont(UIStyle.fontRegular(12));

        JButton loginButton = UIStyle.modernButton("Login");
        loginButton.addActionListener(e -> validateLogin());

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(userLabel, gbc);
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(passLabel, gbc);
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(showPasswordCheck, gbc);
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(feedbackLabel, gbc);
        gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(loginButton, gbc);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIStyle.BACKGROUND);
        rightPanel.add(formWrapper);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private ImageIcon loadLogo(String path, int width, int height) {
        try {
            Image img = ImageIO.read(getClass().getResource(path));
            return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            return new ImageIcon(); // fallback
        }
    }

    private void validateLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText("Username dan password tidak boleh kosong.");
        } else {
            boolean loginBerhasil = service.LoginService.login(user, pass);
            if (loginBerhasil) {
                feedbackLabel.setForeground(new Color(0, 128, 0));
                feedbackLabel.setText("Login berhasil! Mengalihkan...");
                Timer timer = new Timer(1000, e -> MainFrame.showPage("home"));
                timer.setRepeats(false);
                timer.start();
            } else {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText("Username atau password salah.");
            }
        }
    }
}