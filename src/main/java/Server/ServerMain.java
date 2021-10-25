package Server;

/**
 * @Author Dustyn
 * @version 1.0
 * @since 16.0.2
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server that allows for multiple client connections, each connection is sent to a new instance of the class ServerWorker
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = 8085;
        Server server = new Server(port);
        server.start();


    }

}


