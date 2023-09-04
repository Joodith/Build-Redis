import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
//                PrintWriter out=new PrintWriter(clientSocket.getOutputStream(),true);
                OutputStream out=clientSocket.getOutputStream();
                String con_msg=in.readLine();
                String msg="PONG";
                byte[] resp=encodeResponseInRESP(msg);
                if(con_msg!=null && resp.length!=0){
                    System.out.println("Received message: "+con_msg);
                    out.write(resp);
                    System.out.println("Sent response: "+msg);
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

    public static byte[] encodeResponseInRESP(String value){
        int length = value.getBytes(StandardCharsets.UTF_8).length;
        byte[] res = new byte[1 + length + 2];
        int ind=0;
        res[ind++]='+';
        byte[] valueInBytes=value.getBytes(StandardCharsets.UTF_8);
        for(byte b:valueInBytes){
            res[ind++]=b;
        }
        res[ind++]='\r';
        res[ind]='\n';
        return res;
    }
}
