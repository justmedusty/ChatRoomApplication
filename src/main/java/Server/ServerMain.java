package Server;

/**
 * @Author Dustyn
 * @since Java 16
 * @version 1.0
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A very simple server 
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = 8085;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Server is about accept client connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server has accepted client connection from " +clientSocket);
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write("Hello there bud".getBytes(StandardCharsets.UTF_8));
                clientSocket.close();


            }
        } catch (IOException e) {
            e.printStackTrace();
    }

}
    }


