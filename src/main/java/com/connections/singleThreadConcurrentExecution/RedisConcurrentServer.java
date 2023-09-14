package com.connections.singleThreadConcurrentExecution;

import com.connections.handlers.CommandException;
import com.connections.handlers.ProtocolHandlerConcurrent;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.function.Function;

@FunctionalInterface
interface MultiArgumentFunction {
    Object apply(Object... args) throws CommandException;
}

public class RedisConcurrentServer {
    private static final int port = 6369;
    private static ServerSocketChannel serverSocket;
    private static Selector selector;
    private static final Map<String, MultiArgumentFunction> commands = new HashMap<>();
    private static Map<Object, Object> redisStore = new HashMap<>();

    static {
        commands.put("GET", RedisConcurrentServer::getValue);
        commands.put("SET", RedisConcurrentServer::setValue);
        commands.put("DELETE", RedisConcurrentServer::deleteKeyValue);
        commands.put("FLUSH", RedisConcurrentServer::flush);
        commands.put("MGET", RedisConcurrentServer::getMultipleValues);
        commands.put("MSET", RedisConcurrentServer::setMultipleValues);


    }


    private static Object setMultipleValues(Object[] args) throws CommandException {
        int n = args.length;
        if (n >= 2 && n % 2 == 0) {
            for (int i = 0; i < n; i += 2) {
                String key = (String) args[i];
                String value = (String) args[i + 1];
                redisStore.put(key, value);

            }
            return "OK";
        }

        throw new CommandException(new Error("Invalid Command and arguments for MSET"));
    }

    private static List<?> getMultipleValues(Object[] args) throws CommandException {
        int n = args.length;
        List<String> allValues = new ArrayList<>();
        if (n >= 1) {
            for (int i = 0; i < n; i++) {
                String key = (String) args[i];

                allValues.add((String) redisStore.getOrDefault(key, null));
            }
            return allValues;
        }
        throw new CommandException(new Error("Invalid Command and arguments for MGET"));

    }

    private static Object flush(Object[] args) {
        int length = redisStore.size();
        redisStore.clear();
        return "OK";
    }

    private static Object deleteKeyValue(Object[] args) throws CommandException {
        if (args.length == 1) {
            String key = (String) args[0];
            if (redisStore.containsKey(key)) {
                redisStore.remove(key);
                return "OK";
            } else {
                throw new CommandException(new Error("Delete key not found"));
            }
        }
        throw new CommandException(new Error("Invalid Command and arguments.DELETE command needs a key"));

    }

    private static Object setValue(Object[] args) throws CommandException {
//        System.out.println(objArgs.getClass());
//        List<?> args= (List<?>) objArgs;

        if (args.length == 2) {
            String key = (String) args[0];
            String value = (String) args[1];
            redisStore.put(key, value);
            System.out.println(redisStore);
            return "OK";
        }
        throw new CommandException(new Error("Invalid Command and arguments.SET command needs a key and a value"));
    }

    private static Object getValue(Object[] args) throws CommandException {
        if (args.length == 1) {
            String key = (String) args[0];
            if (redisStore.containsKey(key)) {
                return redisStore.get(key);
            }
            return null;
        }

        throw new CommandException(new Error("Invalid Command and arguments for GET"));


    }

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
            if (d instanceof List<?>) {
                String cmd = (String) ((List<?>) d).remove(0);
                System.out.println(cmd);
                MultiArgumentFunction multiArgsFunction = commands.get(cmd);
                if (multiArgsFunction != null) {
                    try {
                        System.out.println(d.getClass());
                        Object resp = multiArgsFunction.apply(((List<?>) d).toArray());
                        System.out.println("Response of command : " + resp);
                        if (resp != null) {
                            ProtocolHandlerConcurrent.writeResponse(clientChannel, resp);
                        } else {
                            ProtocolHandlerConcurrent.writeResponse(clientChannel, "null");
                        }

                    } catch (CommandException ex) {
                        System.out.println("Command Exception : " + ex.getMessage());
                    }
                } else {
                    System.out.println("Unknown command given!");
                }
            }
//            ProtocolHandlerConcurrent.writeResponse(clientChannel, Arrays.asList(2, 3, 4, "hello"));
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
