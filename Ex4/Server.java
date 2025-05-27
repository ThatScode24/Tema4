import java.net.*;
import java.util.*;
import java.io.*;

public class Server {
    public final static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
    public static volatile boolean running = true;
    private static boolean adminAssigned = false;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        System.out.println("Server pornit pe portul 8888.");
        try {
            serverSocket = new ServerSocket(8888);
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client nou conectat: " + clientSocket.getRemoteSocketAddress());
                    boolean isAdmin = false;
                    synchronized (Server.class) {
                        if (!adminAssigned) {
                            System.out.println("Admin-ul a fost asignat: " + clientSocket.getRemoteSocketAddress());
                            isAdmin = true;
                            adminAssigned = true;
                        }
                    }
                    ClientHandler handler = new ClientHandler(clientSocket, isAdmin);
                    clients.add(handler);
                    new Thread(handler).start();
                } catch (SocketException e) { break; }
            }
        } catch (IOException e) { e.printStackTrace(System.out); }
        System.out.println("Inchis.");
    }

    public static void shutdown() {
        running = false;
        for (ClientHandler client : clients) client.shutdown();

        try { serverSocket.close(); } catch (IOException e) { e.printStackTrace(System.out); }
        System.out.println("Oprit.");
    }
}