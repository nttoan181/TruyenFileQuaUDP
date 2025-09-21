package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import server.Constants;

public class UDPFileClient {
    private InetAddress serverAddr;
    private int serverPort;

    public UDPFileClient(String host, int port) throws UnknownHostException {
        serverAddr = InetAddress.getByName(host);
        serverPort = port;
    }

    public boolean sendFile(File file) {
        try(DatagramSocket socket=new DatagramSocket()){
            socket.setSoTimeout(Constants.TIMEOUT_MS);
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[Constants.PACKET_SIZE];
            int seq=0, read;

            while((read=fis.read(buf))!=-1){
                byte[] payload = new byte[read];
                System.arraycopy(buf,0,payload,0,read);

                fis.mark(1);
                boolean last = fis.read()==-1;
                if(!last) fis.reset();

                byte[] fnameBytes = file.getName().getBytes("UTF-8");
                ByteBuffer bb = ByteBuffer.allocate(4+1+4+fnameBytes.length+4+payload.length);
                bb.putInt(seq);
                bb.put((byte)(last?1:0));
                bb.putInt(fnameBytes.length);
                bb.put(fnameBytes);
                bb.putInt(payload.length);
                bb.put(payload);
                byte[] pktBytes = bb.array();
                DatagramPacket dp = new DatagramPacket(pktBytes,pktBytes.length,serverAddr,serverPort);

                boolean acked=false;
                int attempts=0;
                while(!acked && attempts<Constants.MAX_RETRIES){
                    socket.send(dp);
                    try{
                        byte[] ackBuf=new byte[4];
                        DatagramPacket ackPkt=new DatagramPacket(ackBuf,ackBuf.length);
                        socket.receive(ackPkt);
                        int ackSeq = ByteBuffer.wrap(ackBuf).getInt();
                        if(ackSeq==seq) acked=true;
                    }catch(SocketTimeoutException ste){ attempts++; }
                }
                if(!acked){ System.out.println("Packet "+seq+" gửi thất bại"); return false; }
                seq++;
            }
            fis.close(); return true;
        }catch(Exception e){ e.printStackTrace(); return false; }
    }
}
