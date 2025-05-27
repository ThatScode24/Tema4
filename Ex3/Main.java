import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        SimpleConnectionPool pool = new SimpleConnectionPool(3);
        List<Thread> workers = new ArrayList<Thread>();

        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(new Worker(pool, "Mesaj de la worker " + i));
            workers.add(t);
            t.start();
        }

        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        }

        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Log")) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total inregistrari in tabel: " + count);
            }
            pool.releaseConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }

        pool.cleanupOldEntries();
    }
}