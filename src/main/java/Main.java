
import com.connections.RedisClient;
import com.connections.RedisServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");


        if(args.length==0) {
            RedisServer server = new RedisServer();
            server.connectionHandler();
        }
        else{
            if(args.length==2 && args[0].equals("redis-cli")){
                RedisClient client=new RedisClient();
                client.connectToServer(args[1]);
            }
            else{
                System.out.println("Invalid command line arguments");
            }
        }

    }
}
