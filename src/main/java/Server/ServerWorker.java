
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
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private OutputStream outputStream;
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

            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;

                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);

                } else if ("msg".equals(cmd)) {
                    String[] tokensMsg = StringUtil.split(line);
                    handleMessage(tokensMsg);

                } else {
                    String msg = "unknown " + cmd;
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(10);
                }
            }
               /* String msg = "You typed: " + line;
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.write(10);
                */
        }

    }

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];
        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
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
            if ((login.equals("1") && password.equals("1")) || (login.equals("dustyn") && password.equals("dustyn"))) {

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
