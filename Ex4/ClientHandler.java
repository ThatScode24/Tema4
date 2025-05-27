import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {
    private static final ThreadLocal<Socket> localSocket = new ThreadLocal<>();
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isAdmin;

    public ClientHandler(Socket _socket, boolean _isAdmin) {
        socket = _socket;
        isAdmin = _isAdmin;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) { e.printStackTrace(System.out); }
    }

    public void run() {
        localSocket.set(socket);
        System.out.println("Client conectat: " + socket.getRemoteSocketAddress());
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.equals("/quit")) {
                    System.out.println("Client deconectat: " + socket.getRemoteSocketAddress());
                    break;
                } else if (line.equals("/shutdown")) {
                    if (isAdmin) {
                        System.out.println("Comanda /shutdown initiata de: " + socket.getRemoteSocketAddress());
                        Server.shutdown();
                        break;
                    } else {
                        System.out.println("Comanda /shutdown respinsa: " + socket.getRemoteSocketAddress());
                        out.println("Nu ai drepturi.");
                    }
                } else {
                    System.out.println("Mesaj de la " + socket.getRemoteSocketAddress() + ": " + line);
                    broadcast(line);
                }
            }
        } catch (IOException e) { e.printStackTrace(System.out);} finally { shutdown(); }
    }

    private void broadcast(String msg) {
        synchronized (Server.clients) {
            for (ClientHandler client : Server.clients) client.out.println(msg);
        }
    }

    public void shutdown() {
        try {
            System.out.println("Client deconectat: " + socket.getRemoteSocketAddress());
            in.close();
            out.close();
            Socket s = localSocket.get();
            if (s != null && !s.isClosed()) s.close();

        } catch (IOException e) { e.printStackTrace(System.out); }
    }
}