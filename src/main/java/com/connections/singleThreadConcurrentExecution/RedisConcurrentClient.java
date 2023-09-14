package com.connections.singleThreadConcurrentExecution;

import com.connections.handlers.CommandException;
import com.connections.handlers.ProtocolHandlerConcurrent;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


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
            List<String> cmdArgs,splitLargeValues;
            while ((line = userInput.readLine()) != null && !line.isEmpty()) {
                cmdArgs=new ArrayList<>();
                splitLargeValues = new ArrayList<>(Arrays.asList(line.split("\"")));
                System.out.println(splitLargeValues);
                if (!splitLargeValues.isEmpty()) {
                    List<String> firstArg=Arrays.asList(splitLargeValues.get(0).split(" "));
                    splitLargeValues.remove(splitLargeValues.get(0));
                    System.out.println(splitLargeValues);
                    cmdArgs.addAll(firstArg);
                    if (!splitLargeValues.isEmpty()) {
                        cmdArgs.addAll(splitLargeValues);
//                        splitLargeValues.remove(0);
                    }
                    ProtocolHandlerConcurrent.writeResponse(client, cmdArgs);
                }
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


