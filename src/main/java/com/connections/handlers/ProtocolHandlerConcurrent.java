package com.connections.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.io.*;


public class ProtocolHandlerConcurrent {


    private static final Map<String, Function<BufferedReader, Object>> handlerMap = new HashMap<>();


    static {
        handlerMap.put("+", com.connections.handlers.ProtocolHandler::handleSimpleString);
        handlerMap.put("-", com.connections.handlers.ProtocolHandler::handleError);
        handlerMap.put(":", com.connections.handlers.ProtocolHandler::handleInteger);
        handlerMap.put("$", com.connections.handlers.ProtocolHandler::handleString);
        handlerMap.put("*", com.connections.handlers.ProtocolHandler::handleArray);
        handlerMap.put("%", com.connections.handlers.ProtocolHandler::handleMap);
        handlerMap.put("~", com.connections.handlers.ProtocolHandler::handleSet);

    }

    public static String readData(BufferedReader in) throws IOException {
        return in.readLine();
    }

    public static String handleSimpleString(BufferedReader in) {
        System.out.println("Inside string handler");
        try {
            return readData(in);
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;

    }

    public static Error handleError(BufferedReader in) {
        System.out.println("Inside Error handler");

        try {
            return new Error(readData(in));
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return new Error("Exception during reading data from input stream");

    }

    public static Integer handleInteger(BufferedReader in) {
        System.out.println("Inside Integer handler");

        try {
            return Integer.parseInt(readData(in));
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;

    }

    public static String handleString(BufferedReader in) {
        System.out.println("Inside bulk string handler");
        try {
            Integer length = Integer.parseInt(readData(in));
            return readData(in);
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;

    }

    public static List<Object> handleArray(BufferedReader in) {
        System.out.println("Inside array handler");
        try {
            int length = Integer.parseInt(readData(in));
            List<Object> resp = new ArrayList<>();
            for (int i = 1; i <= length; i++) {
                resp.add(handleRequest(in));
            }
            return resp;
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;


    }

    public static Map<Object, Object> handleMap(BufferedReader in) {
        System.out.println("Inside map handler");
        try {
            int length = Integer.parseInt(readData(in));
            Map<Object, Object> resp = new HashMap<>();
            for (int i = 1; i <= length; i++) {
                Object key = handleRequest(in);
                Object value = handleRequest(in);
                resp.put(key, value);
            }
            return resp;
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;

    }

    public static Set<Object> handleSet(BufferedReader in) {
        System.out.println("Inside set handler");
        try {
            int length = Integer.parseInt(readData(in));
            Set<Object> resp = new HashSet<>();
            for (int i = 1; i <= length; i++) {
                resp.add(handleRequest(in));
            }
            return resp;
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;

    }

    public static BufferedReader readChannelToReader(SocketChannel clientChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        BufferedReader in = null;
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            System.out.println("Reading is done!");
        } else if (bytesRead > 0) {
            buffer.flip();
            Charset charset = StandardCharsets.UTF_8;
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = CharBuffer.allocate(buffer.remaining());
            decoder.decode(buffer, charBuffer, true);
            charBuffer.flip();
            System.out.println(charBuffer);
            in = new BufferedReader(new CharArrayReader(charBuffer.array()));
        }
        return in;

    }

    public static Object handleRequest(BufferedReader inputReader) throws IOException {
        int firstByte = inputReader.read();
        if (firstByte != -1) {

            String type = String.valueOf((char) firstByte).trim();
            System.out.println(type);
            Function<BufferedReader, Object> function = (Function<BufferedReader, Object>) handlerMap.get(type);
            if (function != null) {
                return function.apply(inputReader);
            } else {
                System.out.println("Function returns null");
            }
        } else {
            System.out.println("Data is empty");
        }

        return null;
    }

    public static Object readRequest(SocketChannel clientChannel) throws IOException {
        System.out.println("Inside handle request");
        BufferedReader inputReader = readChannelToReader(clientChannel);
        return handleRequest(inputReader);
    }

    public static String simpleStringsEncoding(String data) {
        return ("+" + (data) + "\r\n");
    }

    public static String integerEncoding(Integer data) {
        return (":" + (data) + "\r\n");
    }

    public static String errorEncoding(Error data) {
        return ("-" + (data.getErrorMessage()) + "\r\n");
    }


    public static String prepareResponse(Object writeData) throws IOException {
        final String[] toWrite = {""};
        if (writeData instanceof String) {
            System.out.println("Inside write response of String");
            System.out.println(writeData);
            toWrite[0] = simpleStringsEncoding((String) writeData);
            System.out.println(toWrite[0]);

        } else if (writeData instanceof Integer) {
            System.out.println("Inside write response of Integer");
            System.out.println(writeData);
            toWrite[0] = integerEncoding((Integer) writeData);
            System.out.println(toWrite[0]);

        } else if (writeData instanceof Error) {
            System.out.println("Inside write of Error");
            System.out.println(writeData);
            toWrite[0] = errorEncoding((Error) writeData);
            System.out.println(toWrite[0]);

        } else if (writeData instanceof List<?>) {
            System.out.println("Inside write response of List");
            System.out.println(writeData);
            int listSize = ((List<?>) writeData).size();
            toWrite[0] = "*" + (listSize) + "\r\n";
            for (Object entry : (List<?>) writeData) {
                toWrite[0] = toWrite[0].concat(prepareResponse(entry));
            }

        } else if (writeData instanceof Set<?>) {
            System.out.println("Inside write response of set");
            System.out.println(writeData);
            int setSize = ((Set<?>) writeData).size();
            toWrite[0] = "~" + (setSize) + "\r\n";
            for (Object entry : (Set<?>) writeData) {
                toWrite[0] = toWrite[0].concat(prepareResponse(entry));
            }

        } else if (writeData instanceof Map<?, ?>) {
            System.out.println("Inside write response of Map");
            System.out.println(writeData);
            int mapSize = ((Map<?, ?>) writeData).size();
            toWrite[0] = "%" + (mapSize) + "\r\n";
            ((Map<?, ?>) writeData).forEach((key, value) -> {
                try {
                    toWrite[0] = toWrite[0].concat(prepareResponse(key));
                    toWrite[0] = toWrite[0].concat(prepareResponse(value));
                } catch (IOException e) {
                    System.out.println("Exception while writing map to stream : " + e.getMessage());
                    throw new RuntimeException(e);

                }
            });


        }
        return toWrite[0];


    }

    public static void writeResponse(SocketChannel clientChannel, Object writeData) throws IOException {
        String response = prepareResponse(writeData);
        clientChannel.write(ByteBuffer.wrap(response.getBytes()));
    }


}


