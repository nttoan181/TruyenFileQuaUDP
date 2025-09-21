package client;

import javax.swing.*;
import java.awt.*;
import server.SQLiteHelper;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private SQLiteHelper db;

    public LoginFrame() {
        db = new SQLiteHelper();
        setTitle("Đăng nhập hệ thống gửi file");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 250));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(30, 60, 120));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(lblTitle, c);

        c.gridwidth = 1;

        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        c.gridx = 0; c.gridy = 1;
        panel.add(lblUsername, c);
        tfUsername = new JTextField(20);
        c.gridx = 1; c.gridy = 1;
        panel.add(tfUsername, c);

        JLabel lblPassword = new JLabel("Mật khẩu:");
        c.gridx = 0; c.gridy = 2;
        panel.add(lblPassword, c);
        pfPassword = new JPasswordField(20);
        c.gridx = 1; c.gridy = 2;
        panel.add(pfPassword, c);

        // Tạo 2 nút với cùng kích thước
        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnGoRegister = new JButton("Đăng ký");

        Dimension btnSize = new Dimension(140, 35); // cùng size
        btnLogin.setPreferredSize(btnSize);
        btnGoRegister.setPreferredSize(btnSize);

        btnLogin.setBackground(new Color(100,149,237));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);

        btnGoRegister.setBackground(new Color(60,179,113));
        btnGoRegister.setForeground(Color.WHITE);
        btnGoRegister.setFocusPainted(false);

        // Panel chứa 2 nút, để GridLayout chia đều
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnGoRegister);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        panel.add(buttonPanel, c);

        add(panel);

        // Sự kiện
        btnLogin.addActionListener(e -> login());
        btnGoRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
    }

    private void login() {
        String user = tfUsername.getText().trim();
        String pass = new String(pfPassword.getPassword());
        if(user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa nhập username hoặc password!");
            return;
        }

        if(db.authenticate(user, pass)) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            new FileTransferFrame(user).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Sai username hoặc password!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
