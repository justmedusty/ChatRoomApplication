package Client;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient {

    private final int serverPort;
    private final String serverName;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {

        ChatClient client = new ChatClient("localhost", 8085);
        if (!client.connect()){
            System.err.println("Connection failed");
        }else {
            System.out.println("Connection successful");
            client.login("dustyn","dustyn");

        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is : " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    //
    private void login(String login,String password) throws IOException {
        String cmd = "login," + login + "," + password +"\n";
        serverOut.write(cmd.getBytes(StandardCharsets.UTF_8));

        String response = bufferedIn.readLine();
        System.out.println("Response : "+ response);

    }
}

