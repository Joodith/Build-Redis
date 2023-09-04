import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //  Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Client connected from "+clientSocket.getInetAddress().getHostAddress());
                BufferedReader in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out=new PrintWriter(clientSocket.getOutputStream(),true);
                String con_msg=in.readLine();
                System.out.println(con_msg);
//                System.out.println(con_msg!=null && con_msg.contains("PING"));
                if(con_msg!=null){
                    System.out.println("Received message: "+con_msg);
                    String resp="+PONG\\r\\n";
                    out.println(resp);
                    System.out.println("Sent response: "+resp);
                }
                clientSocket.close();
                System.out.println("Client disconnected!");

            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
//        } finally {
//            try {
//                if (clientSocket != null) {
//                    clientSocket.close();
//                }
//            } catch (IOException e) {
//                System.out.println("IOException: " + e.getMessage());
//            }
//        }
    }
}
