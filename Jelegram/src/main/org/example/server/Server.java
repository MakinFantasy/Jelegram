package main.org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        this.connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(8080);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(client);
                connections.add(connectionHandler);
                pool.execute(connectionHandler);
            }
        } catch (IOException e) {
            shutdown();
        }

    }

    public void broadcast (String msg) {
        for (ConnectionHandler cH : connections) {
            assert cH != null;
            cH.sendMsg(msg);
        }
    }

    public void shutdown () {
        done = true;
        pool.shutdown();
        if (!server.isClosed()) {
            try {
                server.close();
                for (ConnectionHandler cH : connections) {
                    cH.shutdown();
                }
            } catch (IOException e) {
                // ignore
            }
        }

    }


    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler (Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter your nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " connected");
                broadcast(nickname + " joined the chat!");
                String msg;
                while ( (msg = in.readLine()) != null ) {
                    if (msg.startsWith("/nick")) {
                        // TODO: handle nick
                        String[] messageSplit = msg.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed to " + messageSplit[1]);
                            System.out.println(nickname + " renamed to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Nickname changed to " + nickname);
                        } else {
                            out.println("No nickname provided");
                        }
                    } else if (msg.startsWith("/quit")) {
                        broadcast(nickname + " left the chat");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMsg(String msg) {
            out.println(msg);
        }

        public void shutdown ()  {
            if (!client.isClosed()) {
                try {
                    client.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
    public static void main (String[] args) {
        Server server = new Server();
        server.run();
    }
}
