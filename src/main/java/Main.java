
import com.connections.singleThreadConcurrentExecution.RedisConcurrentClient;
import com.connections.singleThreadConcurrentExecution.RedisConcurrentServer;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");


        if(args.length==0) {
//            RedisServer server = new RedisServer();
            RedisConcurrentServer.connectionHandler();
        }
        else{
            if(args.length==2 && args[0].equals("redis-cli")){
                RedisConcurrentClient client=new RedisConcurrentClient();
//                RedisClient client=new RedisClient();
                client.connectToServer(args[1]);
            }
            else{
                System.out.println("Invalid command line arguments");
            }
        }

    }
}
