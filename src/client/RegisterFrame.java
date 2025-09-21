package client;

import javax.swing.*;
import java.awt.*;
import server.SQLiteHelper;

public class RegisterFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JTextField tfFullname;
    private SQLiteHelper db;

    public RegisterFrame() {
        db = new SQLiteHelper();
        setTitle("Đăng ký hệ thống gửi file");
        setSize(500, 350);
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

        JLabel lblTitle = new JLabel("ĐĂNG KÝ", JLabel.CENTER);
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

        JLabel lblFullname = new JLabel("Họ và tên:");
        c.gridx = 0; c.gridy = 3;
        panel.add(lblFullname, c);
        tfFullname = new JTextField(20);
        c.gridx = 1; c.gridy = 3;
        panel.add(tfFullname, c);

        JButton btnRegister = new JButton("Đăng ký");
        btnRegister.setBackground(new Color(60,179,113));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        c.gridx = 0; c.gridy = 4;
        panel.add(btnRegister, c);
        btnRegister.addActionListener(e -> register());

        JButton btnGoLogin = new JButton("Đăng nhập");
        btnGoLogin.setBackground(new Color(100,149,237));
        btnGoLogin.setForeground(Color.WHITE);
        btnGoLogin.setFocusPainted(false);
        c.gridx = 1; c.gridy = 4;
        panel.add(btnGoLogin, c);
        btnGoLogin.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        add(panel);
    }

    private void register() {
        String user = tfUsername.getText().trim();
        String pass = new String(pfPassword.getPassword());
        String fullname = tfFullname.getText().trim();
        if(user.isEmpty() || pass.isEmpty() || fullname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa nhập đầy đủ thông tin!");
            return;
        }

        boolean ok = db.addUser(user, pass, fullname);
        if(ok) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại, username đã tồn tại!");
        }
    }
}
