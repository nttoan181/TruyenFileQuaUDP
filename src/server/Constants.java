package server;

public class Constants {
    // Cổng UDP dùng để truyền file
    public static final int UDP_PORT = 5000;

    // Cổng TCP dùng để xác thực đăng nhập/đăng ký
    public static final int TCP_PORT = 5001;

    // Kích thước gói UDP
    public static final int PACKET_SIZE = 4096;   // 4KB

    // Timeout chờ ACK từ server (ms)
    public static final int TIMEOUT_MS = 2000;

    // Số lần gửi lại nếu mất gói
    public static final int MAX_RETRIES = 5;

    // Thư mục lưu file nhận về
    public static final String STORAGE_DIR = "storage/";
}
