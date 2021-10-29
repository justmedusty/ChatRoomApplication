
/**
 * @Author Dustyn
 * @version 1.0
 * @since 16.0.2
 */

package Server;

import io.smallrye.config.common.utils.StringUtil;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

enum ServerCommand {
    LOGOFF("logoff"),
    QUIT("quit"),
    LOGIN("login"),
    MSG("msg"),
    JOIN("join"),
    LEAVE("leave");

    private String command;

    ServerCommand(String command) {
        this.command = command;
    }

    public static ServerCommand fromString(String text) throws IllegalArgumentException {
        for (ServerCommand cmd : ServerCommand.values()) {
            if (cmd.command.equals(text)) {
                return cmd;
            }
        }
        throw new IllegalArgumentException("No ServerCommand with text " + text + " found");
    }


}

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();
    private String login = "null";

    public ServerWorker(Server server, Socket clientSocket) {

        this.clientSocket = clientSocket;
        this.server = server;
    }


    @Override
    public void run() {

        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * For establishing a connection with each client
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleClientSocket() throws IOException, InterruptedException {

        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {

            String[] tokens = StringUtil.split(line);

            if (tokens == null || tokens.length == 0) {
                return;
            }

            String cmdString = tokens[0];
            ServerCommand cmd = ServerCommand.fromString(cmdString);
            switch (cmd) {
                case LOGOFF -> handleLogoff();
                case QUIT -> handleLogoff();
                case LOGIN -> handleLogin(outputStream, tokens);
                case JOIN -> handleJoin(tokens);
                case LEAVE -> handleLeave(tokens);
                case MSG -> {
                    String[] tokensMsg = StringUtil.split(line);
                    handleMessage(tokensMsg);
            }

        }
}

    }


    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);

    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    //for sending private messages to another user
    //format msg login body
    //format msg topic body
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) ;
                {
                    String outMsg = this.login + " to " + sendTo + ": " + body;
                    worker.send(outMsg);
                    outputStream.write(10);


                }
            }
            if (sendTo.equals(worker.getLogin())) {
                String outMsg = "Message from " + worker.getLogin() + ": " + body;
                worker.send(outMsg);
            }

        }
    }


    private void handleLogoff() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        server.removeWorker(this);
        for (ServerWorker worker : workerList) {
            //makes sure to not send you your own offline message
            if (!login.equals(worker.getLogin())) {
                worker.send(getLogin() + " is now offline");
            }

        }
        clientSocket.close();
        System.out.println("User has logged out successfully");
    }


    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        //for checking the # of tokens , login,username,password
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            //hardcoding values in for the time being
            if ((login.equals("guest") && password.equals("guest")) || (login.equals("dustyn") && password.equals("dustyn"))) {

                String msg = "Successful Login";
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.write(10);
                this.login = login;
                System.out.println("User logged in successfully");
                List<ServerWorker> workerList = server.getWorkerList();

                //This goes thru each server worker and sends who is online to the output stream
                for (ServerWorker worker : workerList) {
                    //Does not send your own login message to you
                    if (!login.equals(worker.getLogin())) {
                        worker.send(getLogin() + " is now online");
                    }
                }
                //Incorrect login credentials
            } else {
                String msg = "Incorrect Login Info";
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.write(10);
            }
        }
    }

    //for sending messages to the client
    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        outputStream.write(10);

    }

}