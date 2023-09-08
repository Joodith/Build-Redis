package com.connections.handlers;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class ProtocolHandler {

    private static final Map<String, Function<BufferedReader, Object>> handlerMap = new HashMap<>();

    public ProtocolHandler() {
        handlerMap.put("+", ProtocolHandler::handleSimpleString);
        handlerMap.put("-", ProtocolHandler::handleError);
        handlerMap.put(":", ProtocolHandler::handleInteger);
        handlerMap.put("$", ProtocolHandler::handleString);
        handlerMap.put("*", ProtocolHandler::handleArray);
        handlerMap.put("%", ProtocolHandler::handleMap);
        handlerMap.put("~", ProtocolHandler::handleSet);

    }

    public static String readData(BufferedReader in) throws IOException {
        return in.readLine();
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        int data;
//        boolean enterPressed = false;
//        while ((data = in.read()) != -1) {
//            char bytesRead = (char) data;
//            buffer.write(bytesRead);
//            if ((bytesRead == '\n' || bytesRead == '\r')) {
//                enterPressed = true;
//                break;
//
//            }
//
//        }
//
//        if (enterPressed) {
//            return buffer.toString().trim();
//        }
//        return "";

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
                resp.add(ProtocolHandler.handleRequest(in));
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
                Object key = ProtocolHandler.handleRequest(in);
                Object value = ProtocolHandler.handleRequest(in);
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
                resp.add(ProtocolHandler.handleRequest(in));
            }
            return resp;
        } catch (IOException e) {
            System.out.println("Exception during reading data from input stream");
            System.out.println(e.getMessage());
        }
        return null;

    }

    public static Object handleRequest(BufferedReader inputReader) throws IOException {
        System.out.println("Inside handle request");
        int firstByte = inputReader.read();
        if (firstByte != -1) {

            String type = String.valueOf((char) firstByte);
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

    public static String simpleStringsEncoding(String data) {
        return ("+" + (data) + "\r\n");
    }
    public static String integerEncoding(Integer data) {
        return (":" + (data) + "\r\n");
    }
    public static String errorEncoding(Error data) {
        return ("-" + (data.getErrorMessage()) + "\r\n");
    }



    public static void writeResponse(OutputStream outputStream, Object writeData) throws IOException {
        if (writeData instanceof String) {
            System.out.println("Inside write response of String");
            System.out.println(writeData);
            String toWrite =simpleStringsEncoding((String) writeData);
            System.out.println(toWrite);
            outputStream.write(toWrite.getBytes());
            outputStream.flush();
        }
        else if (writeData instanceof Integer) {
            System.out.println("Inside write response of Integer");
            System.out.println(writeData);
            String toWrite =integerEncoding((Integer) writeData);
            System.out.println(toWrite);
            outputStream.write(toWrite.getBytes());
            outputStream.flush();
        }
        else if (writeData instanceof Error) {
            System.out.println("Inside write of Error");
            System.out.println(writeData);
            String toWrite = errorEncoding((Error) writeData);
            System.out.println(toWrite);
            outputStream.write(toWrite.getBytes());
            outputStream.flush();
        }
        else if (writeData instanceof List<?>) {
            System.out.println("Inside write response of List");
            System.out.println(writeData);
            int listSize=((List<?>) writeData).size();
            String toWrite = "*" + (listSize) + "\r\n";
            outputStream.write(toWrite.getBytes());
            for(Object entry:(List<?>) writeData){
                writeResponse(outputStream,entry);
            }
            outputStream.flush();
        }
        else if (writeData instanceof Set<?>) {
            System.out.println("Inside write response of set");
            System.out.println(writeData);
            int setSize=((Set<?>) writeData).size();
            String toWrite = "~" + (setSize) + "\r\n";
            outputStream.write(toWrite.getBytes());
            for(Object entry:(Set<?>) writeData){
                writeResponse(outputStream,entry);
            }
            outputStream.flush();
        }
        else if (writeData instanceof Map<?,?>) {
            System.out.println("Inside write response of Map");
            System.out.println(writeData);
            int mapSize=((Map<?,?>) writeData).size();
            String toWrite = "%" + (mapSize) + "\r\n";
            outputStream.write(toWrite.getBytes());
            ((Map<?, ?>) writeData).forEach((key,value)->{
                try {
                    writeResponse(outputStream,key);
                    writeResponse(outputStream,value);
                } catch (IOException e) {
                    System.out.println("Exception while writing map to stream : "+e.getMessage());
                    throw new RuntimeException(e);

                }
            });

            outputStream.flush();
        }


    }

    public static byte[] encodeResponseInRESP(String value) {
        int length = value.getBytes(StandardCharsets.UTF_8).length;
        byte[] res = new byte[1 + length + 2];
        int ind = 0;
        res[ind++] = '+';
        byte[] valueInBytes = value.getBytes(StandardCharsets.UTF_8);
        for (byte b : valueInBytes) {
            res[ind++] = b;
        }
        res[ind++] = '\r';
        res[ind] = '\n';
        return res;
    }
}
