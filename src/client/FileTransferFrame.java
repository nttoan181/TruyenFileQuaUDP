package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import server.SQLiteHelper;
import server.FileRecord;
import server.Constants;
import java.awt.Desktop;

public class FileTransferFrame extends JFrame implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private JTextField tfPath;
    private File chosen;
    private JTextArea taHistory;
    private SQLiteHelper db;

    public FileTransferFrame(String user) {
        username = user;
        db = new SQLiteHelper();
        setTitle("UDP File Transfer - " + user);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        loadHistory();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.setBackground(new Color(245,250,255));

        // Header
        JLabel lblHeader = new JLabel("HỆ THỐNG GỬI FILE UDP", JLabel.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 24));
        lblHeader.setForeground(new Color(30, 60, 120));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Center panel
        JPanel center = new JPanel(new BorderLayout(10,10));
        center.setBackground(new Color(245,250,255));

        // Top panel - chọn/gửi/mở
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(new Color(245,250,255));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
        tfPath = new JTextField();
        tfPath.setFont(new Font("Arial", Font.PLAIN, 14));
        tfPath.setEditable(false);
        topPanel.add(tfPath, c);

        c.weightx = 0;

        JButton btnChoose = new JButton("📁 Chọn file");
        btnChoose.setBackground(new Color(100,149,237));
        btnChoose.setForeground(Color.WHITE);
        btnChoose.setFocusPainted(false);
        btnChoose.addActionListener(e -> chooseFile());
        c.gridx = 1; c.gridy = 0;
        topPanel.add(btnChoose, c);

        JButton btnSend = new JButton("🚀 Gửi file");
        btnSend.setBackground(new Color(60,179,113));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.addActionListener(e -> sendFile());
        c.gridx = 2; c.gridy = 0;
        topPanel.add(btnSend, c);

        JButton btnOpenFolder = new JButton("📂 Mở thư mục");
        btnOpenFolder.setBackground(new Color(255,140,0));
        btnOpenFolder.setForeground(Color.WHITE);
        btnOpenFolder.setFocusPainted(false);
        btnOpenFolder.addActionListener(e -> openStorageFolder());
        c.gridx = 3; c.gridy = 0;
        topPanel.add(btnOpenFolder, c);

        center.add(topPanel, BorderLayout.NORTH);

        // TextArea lịch sử
        taHistory = new JTextArea();
        taHistory.setEditable(false);
        taHistory.setFont(new Font("Monospaced", Font.PLAIN, 13));
        taHistory.setBackground(new Color(230,240,250));
        JScrollPane scroll = new JScrollPane(taHistory);
        scroll.setBorder(BorderFactory.createTitledBorder("📜 Lịch sử file"));
        center.add(scroll, BorderLayout.CENTER);

        mainPanel.add(center, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(245,250,255));
        JLabel lblUser = new JLabel("Người dùng: " + username);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setForeground(new Color(0, 100, 200));
        footer.add(lblUser);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chosen = fc.getSelectedFile();
            tfPath.setText(chosen.getAbsolutePath());
        }
    }

    private void sendFile() {
        if(chosen == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn file!");
            return;
        }

        new Thread(() -> {
            try {
                client.ReliableUDPClient client = new client.ReliableUDPClient("localhost", Constants.UDP_PORT);

                SwingUtilities.invokeLater(() -> taHistory.append("Đang gửi file: " + chosen.getName() + "\n"));

                // Hàm callback hiển thị trạng thái chunk gửi
                client.sendFile(chosen, msg -> SwingUtilities.invokeLater(() -> taHistory.append(msg + "\n")));

                SwingUtilities.invokeLater(() -> {
                    taHistory.append("✅ Gửi file hoàn tất: " + chosen.getName() + "\n");
                    loadHistory();
                });

            } catch(Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Gửi file thất bại: " + e.getMessage())
                );
            }
        }).start();
    }

    private void loadHistory() {
        taHistory.setText("");
        try {
            List<FileRecord> files = db.getAllFiles();
            for(FileRecord fr : files) {
                taHistory.append(String.format("%s | %s | %d bytes | %s\n",
                        fr.timestamp, fr.sender, fr.size, fr.filename));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi load lịch sử file: " + e.getMessage());
        }
    }

    private void openStorageFolder() {
        try {
            Desktop.getDesktop().open(new File(Constants.STORAGE_DIR));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không mở được thư mục storage: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileTransferFrame("user_demo").setVisible(true));
    }
}
