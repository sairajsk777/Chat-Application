import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class Server {

    private static ServerSocket serverSocket = null;

    private static Socket clientSocket = null;

    private static final int maxClientsCount = 10;
    private static final threadClient[] threads = new threadClient[maxClientsCount];

    public static void main(String args[]) {

        int portNumber = 2222;
        if (args.length < 1) {
            System.out
                    .println("Usage: java Server <portNumber>\n"
                            + "Now using port number=" + portNumber);
        } else {
            portNumber = Integer.valueOf(args[0]).intValue();
        }


        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new threadClient(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}

class threadClient extends Thread {

    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final threadClient[] threads;
    private int maxClientsCount;

    public threadClient(Socket clientSocket, threadClient[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        threadClient[] threads = this.threads;

        try {

            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            os.println("Enter your name.");
            String name = is.readLine().trim();
            os.println("Hello " + name
                    + " to our chat room.\nTo leave enter /quit in a new line");
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].os.println("*** A new user " + name
                            + " entered the chat room !!! ***");
                }
            }
            while (true) {
                String line = is.readLine();
                if (line.startsWith("/quit")) {
                    break;
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null) {
                        threads[i].os.println("<" + name + "&gr; " + line);
                    }
                }
            }
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].os.println("*** The user " + name
                            + " is leaving the chat room !!! ***");
                }
            }
            os.println("*** Bye " + name + " ***");


            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }

            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
        }
    }
}
