import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            Thread reader = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(">> " + msg);
                    }
                } catch (IOException ignored) {}
            });

            Thread writer = new Thread(() -> {
                try {
                    String input;
                    while ((input = userIn.readLine()) != null) {
                        out.println(input);
                        if (input.equals("/quit") || input.equals("/shutdown")) {
                            socket.close();
                            break;
                        }
                    }
                } catch (IOException e) {}
            });

            reader.start();
            writer.start();
            writer.join();
            reader.interrupt();
        } catch (IOException | InterruptedException e) { e.printStackTrace(System.out); }
    }
}