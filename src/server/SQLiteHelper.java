package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper {
    private static final String DB_URL = "jdbc:sqlite:storage/db.sqlite";
    private Connection conn;

    public SQLiteHelper() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "fullname TEXT)";
        String fileTable = "CREATE TABLE IF NOT EXISTS files (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "filename TEXT," +
                "sender TEXT," +
                "size INTEGER," +
                "timestamp TEXT)";
        Statement stmt = conn.createStatement();
        stmt.execute(userTable);
        stmt.execute(fileTable);
        stmt.close();
    }

    public boolean addUser(String username, String password, String fullname) {
        try {
            String sql = "INSERT INTO users(username,password,fullname) VALUES(?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullname);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        try {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            boolean ok = rs.next();
            rs.close();
            ps.close();
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertFileMeta(String filename, String sender, String timestamp, long size) {
        try {
            String sql = "INSERT INTO files(filename,sender,timestamp,size) VALUES(?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, filename);
            ps.setString(2, sender);
            ps.setString(3, timestamp);
            ps.setLong(4, size);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FileRecord> getAllFiles() {
        List<FileRecord> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM files ORDER BY id DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                FileRecord fr = new FileRecord();
                fr.filename = rs.getString("filename");
                fr.sender = rs.getString("sender");
                fr.size = rs.getLong("size");
                fr.timestamp = rs.getString("timestamp");
                list.add(fr);
            }
            rs.close();
            stmt.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void close() {
        try {
            if(conn != null) conn.close();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
