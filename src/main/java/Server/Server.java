package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0
 * @Author Dustyn
 * @since 16.0.2
 */
public class Server extends Thread {

    private final int serverPort;
    //A list of workers making it possible to send messages to different instances of ServerWorker
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    //A getter for said list
    public List<ServerWorker> getWorkerList() {
        return workerList;

    }

    //for constructing server with port
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }


    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("Server is about accept client connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server has accepted client connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //for removing worker when they quit/logoff
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
