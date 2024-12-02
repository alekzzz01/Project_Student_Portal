package com.example.studentportal;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionClass {
    protected static String db = "u207026370_cvsunaic_cvsud";
    protected static String ip = "153.92.15.31";
    protected static String port = "3306";
    protected static String username = "u207026370_root";
    protected static String password = "@Dmin_cvsunaic123";

    public Connection CONN() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://" + ip + ":" + port + "/" + db + "?useSSL=false&connectTimeout=5000";
            conn = DriverManager.getConnection(connectionString, username, password);
        } catch (Exception e) {
            Log.e("ConnectionClass", "Connection Error: " + e.getMessage());
        }
        return conn;
    }
}



