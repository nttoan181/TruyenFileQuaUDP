package server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class UDPServer {
    private DatagramSocket socket;
    private SQLiteHelper db;
    private Map<String, FileSession> sessions = new ConcurrentHashMap<>();

    public UDPServer() throws Exception {
        socket = new DatagramSocket(Constants.UDP_PORT);
        db = new SQLiteHelper();
        File dir = new File(Constants.STORAGE_DIR);
        if(!dir.exists()) dir.mkdirs();
    }

    public void start() throws Exception {
        System.out.println("UDPServer chạy cổng " + Constants.UDP_PORT);
        byte[] buffer = new byte[Constants.PACKET_SIZE + 200];
        while(true){
            DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
            socket.receive(pkt);
            new Thread(() -> {
                try { handlePacket(pkt); } catch(Exception e){ e.printStackTrace(); }
            }).start();
        }
    }

    private void handlePacket(DatagramPacket pkt) throws Exception {
        InetAddress addr = pkt.getAddress();
        int port = pkt.getPort();
        ByteBuffer bb = ByteBuffer.wrap(pkt.getData(), 0, pkt.getLength());

        int seq = bb.getInt();
        byte last = bb.get();
        int fnameLen = bb.getInt();
        byte[] fnameBytes = new byte[fnameLen];
        bb.get(fnameBytes);
        String filename = new String(fnameBytes,"UTF-8");
        int payloadLen = bb.getInt();
        byte[] payload = new byte[payloadLen];
        bb.get(payload);

        String sessionId = addr.getHostAddress() + "_" + port + "_" + filename;
        FileSession session = sessions.computeIfAbsent(sessionId, id -> new FileSession(id, filename));

        synchronized(session){
            if(!session.received.contains(seq)){
                session.map.put(seq,payload);
                session.received.add(seq);
            }
        }

        ByteBuffer ackBuf = ByteBuffer.allocate(4); ackBuf.putInt(seq);
        socket.send(new DatagramPacket(ackBuf.array(),4,addr,port));

        if(last==1) session.lastSeq = seq;

        if(session.lastSeq!=-1 && session.received.size()>session.lastSeq){
            session.saveFile();
            System.out.println("File nhận xong: "+session.filename);
            sessions.remove(sessionId);
        }
    }

    private class FileSession{
        public String id, filename; public int lastSeq=-1;
        public Set<Integer> received = new HashSet<>();
        public Map<Integer, byte[]> map = new TreeMap<>();
        public FileSession(String id, String filename){ this.id=id; this.filename=filename; }

        public void saveFile() throws Exception{
            Path path = Paths.get(Constants.STORAGE_DIR, filename);
            try(FileOutputStream fos=new FileOutputStream(path.toFile())){
                for(byte[] b: map.values()) fos.write(b);
            }
            String sender = id.split("_")[0]+":"+id.split("_")[1];
            long size = Files.size(path);
            db.insertFileMeta(filename, path.toString(), sender, size);
        }
    }

    public static void main(String[] args) throws Exception {
        new UDPServer().start();
    }
}
