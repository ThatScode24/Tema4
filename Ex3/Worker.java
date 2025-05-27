import java.sql.*;
import java.util.Random;

public class Worker implements Runnable {
    private final SimpleConnectionPool pool;
    private final String message;

    public Worker(SimpleConnectionPool _pool, String _message) {
        pool = _pool;
        message = _message;
    }

    public void run() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = pool.getConnection();
            stmt = conn.prepareStatement("INSERT INTO Log(message) VALUES(?)");
            stmt.setString(1, message);
            stmt.executeUpdate();
            Thread.sleep(new Random().nextInt(400) + 100);
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace(System.out);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) pool.releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace(System.out);
            }
        }
    }
}