package client;

import java.io.*;
import java.net.*;

public class AuthClient {
    private String host; private int port;
    public AuthClient(String h, int p){ host=h; port=p; }

    private boolean sendRequest(String msg){
        try(Socket s=new Socket(host, port);
            BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pw=new PrintWriter(s.getOutputStream(), true)) {
            pw.println(msg); String resp=br.readLine();
            return "OK".equals(resp);
        } catch(Exception e){ e.printStackTrace(); return false;}
    }

    public boolean register(String user, String pass, String email){ return sendRequest("REGISTER|"+user+"|"+pass+"|"+email); }
    public boolean login(String user, String pass){ return sendRequest("LOGIN|"+user+"|"+pass); }
}
