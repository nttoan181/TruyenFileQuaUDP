package server;

import java.io.*;
import java.net.*;

public class AuthServer {
    private SQLiteHelper db;
    private ServerSocket server;

    public AuthServer(int port) throws Exception {
        db = new SQLiteHelper();
        server = new ServerSocket(port);
        System.out.println("AuthServer chạy cổng " + port);
    }

    public void start() throws Exception {
        while(true){
            Socket client = server.accept();
            new Thread(() -> handle(client)).start();
        }
    }

    private void handle(Socket s){
        try(BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pw=new PrintWriter(s.getOutputStream(), true)) {

            String line = br.readLine();
            String[] parts = line.split("\\|");
            String action = parts[0];
            String username = parts[1];
            String password = parts[2];
            String email = parts.length>3 ? parts[3] : "";

            if("REGISTER".equalsIgnoreCase(action)){
                if(db.addUser(username,password,email)) pw.println("OK"); else pw.println("FAIL");
            } else if("LOGIN".equalsIgnoreCase(action)){
                if(db.authenticate(username,password)) pw.println("OK"); else pw.println("FAIL");
            } else pw.println("FAIL");
        } catch(Exception e){ e.printStackTrace();}
    }

    public static void main(String[] args) throws Exception {
        new AuthServer(Constants.TCP_PORT).start();
    }
}
