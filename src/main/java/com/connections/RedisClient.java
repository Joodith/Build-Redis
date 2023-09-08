package com.connections;

import com.connections.handlers.CommandException;
import com.connections.handlers.Error;
import com.connections.handlers.ProtocolHandler;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RedisClient {
    private final String greenColor = "\u001B[32m";
    private final String resetColor = "\u001B[0m";
    private ProtocolHandler protocolHandler;

    public void connectToServer(String hostname) {
        protocolHandler = new ProtocolHandler();
        try (
                Socket socket = new Socket(hostname, 6369);
                OutputStream outStream = socket.getOutputStream();
                PrintWriter out = new PrintWriter(outStream, true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        ) {
            System.out.println("Client connected to " + hostname + " at port " + 6369);
            String prompt = "redis-cli> ";
            System.out.print(greenColor + prompt + resetColor);
            String line;
            while ((line= userInput.readLine())!=null && !line.isEmpty()) {
//                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//                int bytesRead;
//                int count = 0;
//                boolean enterPressed = false;
//                while ((bytesRead = System.in.read()) != -1) {
//
//                    buffer.write(bytesRead);
//
//                    if ((bytesRead == '\n' || bytesRead == '\r')) {
//                        if (count != 0) {
//                            enterPressed = true;
//                            break;
//                        }
//                    }
//                    count += 1;
//
//
//                }
//                if (enterPressed) {
//                    String inputString = buffer.toString();
                    System.out.println(line);
                    ProtocolHandler.writeResponse(outStream, line);
                    Object serverResponse = ProtocolHandler.handleRequest(in);
                    if (serverResponse instanceof Error) {
                        throw new CommandException("Command specified is incorrect");
                    }
                    System.out.println("Server response : " + serverResponse);
                    System.out.print(greenColor + prompt + resetColor);
                }


//            } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        } catch (CommandException ex) {
//            throw new RuntimeException(ex);
//        }


    } catch (IOException e) {
            System.out.println("Client is trying to exit!");
            System.out.println("IO Exception: " + e.getMessage());
        } catch (CommandException e) {
            System.out.println("Done!");
//            throw new RuntimeException(e);
        }
    }
}
