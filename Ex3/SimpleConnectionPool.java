import java.sql.*;
import java.util.*;

public class SimpleConnectionPool {
    private final List<Connection> pool = new ArrayList<Connection>();

    public SimpleConnectionPool(int _size) {
        for (int i = 0; i < _size; i++) {
            try {
                String url = "jdbc:postgresql://localhost:5432/postgres";
                String user = "postgres";
                String password = "password";
                Connection conn = DriverManager.getConnection(url, user, password);
                pool.add(conn);
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
        initDatabase();
    }

    public synchronized Connection getConnection() {
        while (pool.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
        return pool.removeFirst();
    }

    public synchronized void releaseConnection(Connection _conn) {
        pool.add(_conn);
        notifyAll();
    }

    public void cleanupOldEntries() {
        Connection conn = null;
        CallableStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareCall("{ call delete_old_entries() }");
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void initDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();



        } catch (SQLException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}