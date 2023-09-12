package com.connections.singleThreadConcurrentExecution;

import com.connections.handlers.CommandException;
import com.connections.handlers.ProtocolHandlerConcurrent;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;


public class RedisConcurrentClient {

    private final String greenColor = "\u001B[32m";
    private final String resetColor = "\u001B[0m";

    public void connectToServer(String hostname) {
        try (
                SocketChannel client = SocketChannel.open(new InetSocketAddress(hostname, 6369));
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        ) {
            System.out.println("Client connected to " + hostname + " at port " + 6369);
            String prompt = "redis-cli> ";
            System.out.print(greenColor + prompt + resetColor);
            String line;
            while ((line = userInput.readLine()) != null && !line.isEmpty()) {
                ProtocolHandlerConcurrent.writeResponse(client, line);
                Object serverResponse = ProtocolHandlerConcurrent.readRequest(client);
                if (serverResponse instanceof Error) {
                    throw new CommandException("Command specified is incorrect");
                }
                System.out.println("Server response : " + serverResponse);
                System.out.print(greenColor + prompt + resetColor);
            }


        } catch (IOException e) {
            System.out.println("Client is trying to exit!");
            System.out.println("IO Exception: " + e.getMessage());
        } catch (CommandException e) {
            System.out.println("Command Exception");
        }
    }
}


