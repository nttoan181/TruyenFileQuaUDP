package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import server.Constants;

public class ReliableUDPClient {
    private InetAddress addr;
    private int port;
    private DatagramSocket socket;

    public ReliableUDPClient(String host, int port) throws Exception {
        this.addr = InetAddress.getByName(host);
        this.port = port;
        socket = new DatagramSocket();
        socket.setSoTimeout(Constants.TIMEOUT_MS);
    }

    // Gửi file với callback trạng thái
    public void sendFile(File file, Consumer<String> statusCallback) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[Constants.PACKET_SIZE];
        int seq = 0;
        int read;
        while((read = fis.read(buffer)) != -1) {
            boolean last = fis.available() == 0;
            sendPacket(seq, buffer, read, file.getName(), last);
            waitAck(seq);

            final int s = seq;
            if(statusCallback != null) {
                statusCallback.accept("Chunk " + s + " gửi thành công\n");
            }
            seq++;
        }
        fis.close();
    }

    private void sendPacket(int seq, byte[] data, int len, String fname, boolean last) throws Exception {
        byte[] fnameBytes = fname.getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(4 + 1 + 4 + fnameBytes.length + 4 + len);
        bb.putInt(seq);
        bb.put((byte)(last ? 1 : 0));
        bb.putInt(fnameBytes.length);
        bb.put(fnameBytes);
        bb.putInt(len);
        bb.put(data, 0, len);

        DatagramPacket pkt = new DatagramPacket(bb.array(), bb.capacity(), addr, port);
        socket.send(pkt);
    }

    private void waitAck(int seq) throws Exception {
        byte[] buf = new byte[4];
        DatagramPacket pkt = new DatagramPacket(buf, 4);
        while(true){
            try {
                socket.receive(pkt);
                int ack = ByteBuffer.wrap(pkt.getData()).getInt();
                if(ack == seq) return;
            } catch(SocketTimeoutException e) {
                // resend packet nếu muốn (hiện tại để loop chờ ACK)
            }
        }
    }
}
