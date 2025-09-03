package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javaapplication1.MainFrame;
import tampilan.util.UIStyle;
import service.LoginService; // Pastikan import ini ada

public class TampilanLoginManual extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel feedbackLabel;
    private JCheckBox showPasswordCheck;
    private UIStyle.RoundedPanel formWrapper;

    public TampilanLoginManual() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND);

        // Panel Kiri: Logo dan Branding dengan gradient modern
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, UIStyle.PRIMARY, 0, getHeight(), UIStyle.PRIMARY_DARK);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JLabel logoImage = new JLabel(loadLogo("/img/icon.png", 100, 100));
        logoImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel logoText = new JLabel("BYEBELI EXPERIENCE");
        logoText.setFont(UIStyle.fontBold(20));
        logoText.setForeground(Color.WHITE);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoText.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel subtitle = new JLabel("Admin Panel");
        subtitle.setFont(UIStyle.fontRegular(16));
        subtitle.setForeground(new Color(255, 255, 255, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoImage);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(logoText);
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalGlue());

        // Panel Kanan: Form login dengan rounded dan shadow
        formWrapper = new UIStyle.RoundedPanel(20);
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setLayout(new GridBagLayout());
        formWrapper.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        formWrapper.setPreferredSize(new Dimension(450, 500)); // Memberikan height yang cukup

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Login Admin");
        title.setFont(UIStyle.fontBold(24));
        title.setForeground(UIStyle.PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UIStyle.fontMedium(14));
        userLabel.setForeground(UIStyle.TEXT);
        usernameField = new JTextField(20);
        UIStyle.styleTextField(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UIStyle.fontMedium(14));
        passLabel.setForeground(UIStyle.TEXT);
        passwordField = new JPasswordField(20);
        UIStyle.styleTextField(passwordField);

        showPasswordCheck = new JCheckBox("Tampilkan Password");
        showPasswordCheck.setFont(UIStyle.fontRegular(13));
        showPasswordCheck.setBackground(Color.WHITE);
        showPasswordCheck.setForeground(UIStyle.TEXT_LIGHT);
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '•');
        });

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(UIStyle.fontRegular(13));
        feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton loginButton = UIStyle.modernButton("Login");
        loginButton.setPreferredSize(new Dimension(0, 45));
        loginButton.addActionListener(e -> validateLogin());

        // Enter key listener untuk login
        usernameField.addActionListener(e -> validateLogin());
        passwordField.addActionListener(e -> validateLogin());

        // Perbaikan tata letak GridBagLayout
        int row = 0;
        
        // Title - mengambil 2 kolom
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = row++;
        formWrapper.add(title, gbc);
        
        // Username label
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        formWrapper.add(userLabel, gbc);
        
        // Username field
        gbc.gridx = 1;
        gbc.gridy = row++;
        formWrapper.add(usernameField, gbc);
        
        // Password label
        gbc.gridx = 0;
        gbc.gridy = row;
        formWrapper.add(passLabel, gbc);
        
        // Password field
        gbc.gridx = 1;
        gbc.gridy = row++;
        formWrapper.add(passwordField, gbc);
        
        // Checkbox - mengambil 2 kolom
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = row++;
        formWrapper.add(showPasswordCheck, gbc);
        
        // Feedback label - mengambil 2 kolom
        gbc.gridx = 0;
        gbc.gridy = row++;
        formWrapper.add(feedbackLabel, gbc);
        
        // Login button - mengambil 2 kolom
        gbc.gridx = 0;
        gbc.gridy = row++;
        formWrapper.add(loginButton, gbc);
        
        // Tambahkan glue untuk mengisi ruang kosong
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.BOTH;
        formWrapper.add(Box.createGlue(), gbc);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIStyle.BACKGROUND);
        
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.weightx = 1.0;
        rightGbc.weighty = 1.0;
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(formWrapper, rightGbc);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private ImageIcon loadLogo(String path, int width, int height) {
        try {
            Image img = ImageIO.read(getClass().getResource(path));
            return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            // Fallback ke ikon default jika gambar tidak ditemukan
            JLabel fallbackIcon = new JLabel("🎮");
            fallbackIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, width/2));
            return new ImageIcon(createImageFromComponent(fallbackIcon, width, height));
        }
    }
    
    private Image createImageFromComponent(JComponent component, int width, int height) {
        component.setSize(width, height);
        component.setPreferredSize(new Dimension(width, height));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        component.printAll(g2);
        g2.dispose();
        return image;
    }

    private void validateLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            feedbackLabel.setForeground(UIStyle.DANGER_COLOR);
            feedbackLabel.setText("Username dan password tidak boleh kosong.");
        } else {
            boolean loginBerhasil = LoginService.login(user, pass);
            if (loginBerhasil) {
                feedbackLabel.setForeground(UIStyle.SUCCESS_COLOR);
                feedbackLabel.setText("Login berhasil! Mengalihkan...");
                
                // Disable inputs selama redirect
                usernameField.setEnabled(false);
                passwordField.setEnabled(false);
                showPasswordCheck.setEnabled(false);
                
                Timer timer = new Timer(1000, e -> MainFrame.showPage("home"));
                timer.setRepeats(false);
                timer.start();
            } else {
                feedbackLabel.setForeground(UIStyle.DANGER_COLOR);
                feedbackLabel.setText("Username atau password salah.");
                
                // Shake animation untuk feedback visual
                animateShake(formWrapper);
            }
        }
    }
    
    private void animateShake(JComponent component) {
        int originalX = component.getX();
        int shakeDelta = 5;
        
        Timer shakeTimer = new Timer(50, null);
        shakeTimer.setRepeats(true);
        final int[] count = {0};
        
        shakeTimer.addActionListener(e -> {
            if (count[0] < 6) {
                component.setLocation(originalX + (count[0] % 2 == 0 ? shakeDelta : -shakeDelta), component.getY());
                count[0]++;
            } else {
                component.setLocation(originalX, component.getY());
                shakeTimer.stop();
            }
        });
        
        shakeTimer.start();
    }
}