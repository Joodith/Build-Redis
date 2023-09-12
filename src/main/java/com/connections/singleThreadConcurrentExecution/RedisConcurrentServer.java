package com.connections.singleThreadConcurrentExecution;

import com.connections.handlers.ProtocolHandlerConcurrent;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class RedisConcurrentServer {
    private static final int port = 6369;
    private static ServerSocketChannel serverSocket;
    private static Selector selector;

    static {
        try {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.out.println("Error creating selectors");
            System.out.println("IO Exception :" + e.getMessage());

        }


    }

    private static void register() throws IOException {
        SocketChannel client = serverSocket.accept();
        System.out.println("Client connected from " + client.getLocalAddress());
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

    }

    private static void processRequest(SelectionKey selectionKey) throws IOException {
        SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
        Object d = ProtocolHandlerConcurrent.readRequest(clientChannel);
        if (d != null) {
            System.out.println("Client has sent : " + d);
            ProtocolHandlerConcurrent.writeResponse(clientChannel, Arrays.asList(2, 3, 4, "hello"));
        }


    }


    public static void connectionHandler() {

        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                System.out.println("No events found!");
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                try {
                    if (key.isAcceptable()) {
                        register();
                    }
                    if (key.isReadable()) {
                        processRequest(key);
                    }
                    iter.remove();
                } catch (IOException e) {
                    key.cancel();
                    System.out.println("Cancelling the key!");
                    try {
                        System.out.println("Closing the client channel");
                        key.channel().close();
                    } catch (IOException channelClose) {
                        System.out.println("Exception during client channel close!");
                    }
                }
            }


        }
    }


}
