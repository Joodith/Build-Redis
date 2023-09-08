package com.connections;

import com.connections.handlers.ProtocolHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RedisServer {
    private static final int port = 6369;
    private static final int MAX_THREADS = 10;
    private ServerSocket serverSocket;
    private final ExecutorService executorService;
    public static ProtocolHandler protocolHandler;


    public RedisServer() {
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            protocolHandler = new ProtocolHandler();
            System.out.println("Server listening on port: " + port);
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }
    }

    static public class ClientHandler implements Runnable {
        private final Socket clientSocket;

        private final ProtocolHandler protocolHandlerObj;

        public ClientHandler(Socket socket, ProtocolHandler handler) {
            this.clientSocket = socket;
            this.protocolHandlerObj = handler;

        }

        @Override
        public void run() {
            try (
                    InputStream inStream = clientSocket.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                    OutputStream outStream = clientSocket.getOutputStream();
//                    PrintWriter out = new PrintWriter(outStream, true);

            ) {
                while (true) {
                    if (inStream.available() != 0) {

                        Object d = ProtocolHandler.handleRequest(in);
                        if (d != null) {
                            System.out.println("Client has sent : " + d);
                            ProtocolHandler.writeResponse(outStream, Arrays.asList(2, 3, 4,"hello"));
                        }
                    }


                }


            } catch (IOException e) {

                System.out.println("Client " + clientSocket.getInetAddress().getHostAddress() + " has disconnected!");

            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Server Connection with " + clientSocket.getInetAddress().getHostAddress() + " is closed ");
                } catch (IOException ex) {
                    System.out.println(" **  Problem in closing connection **");
                    System.out.println("IO Exception: " + ex.getMessage());

                }


            }

        }

    }


    public void connectionHandler() throws IOException {

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());
                Runnable clientHandler = new ClientHandler(clientSocket, protocolHandler);
                executorService.execute(clientHandler);
            } catch (IOException e) {
                System.out.println("IO Exception: " + e.getMessage());
                System.out.println("Shutting down executor service!");
                executorService.shutdown();
                break;

            }
//            finally {
//                System.out.println("Shutting down executor service!");
//                executorService.shutdown();
//                break;
//            }
        }


    }

}

