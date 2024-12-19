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

    private static final String TAG = "ConnectionClass";

    public Connection CONN() {
        Connection conn = null;
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG, "MySQL JDBC Driver Registered!");

            // Create connection string
            String connectionString = "jdbc:mysql://" + ip + ":" + port + "/" + db + "?useSSL=false&connectTimeout=5000";
            Log.d(TAG, "Connecting to database: " + connectionString);

            // Establish connection
            conn = DriverManager.getConnection(connectionString, username, password);
            Log.d(TAG, "Connection established successfully!");

        } catch (Exception e) {
            Log.e(TAG, "Connection Error: " + e.getMessage());
        }
        return conn;
    }
}
