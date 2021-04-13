import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Client {

    public static Socket socket = null;
    public static DataOutputStream outputStream = null;
    public static MyDataInputStream inputStream = null;

    public static int createValidConnection() {
       try {
           Thread.sleep(2000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }

        String hostname;
        Integer port;

        for (int i = 0; i < Domains.domains.size(); i++) {
            hostname = Domains.domains.get(i).hostname;
            port = Domains.domains.get(i).port;
                try {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                    if (socket != null && !socket.isClosed()) socket.close();

                    if (Domains.valid[i]) {
                        socket = new Socket(hostname, port);
                        outputStream = new DataOutputStream(socket.getOutputStream());
                        inputStream = new MyDataInputStream(socket.getInputStream());
                        System.out.println("Client connected to server: " + hostname + "-" + port);
                        System.out.println();
                        System.out.println("Input and output channels has been initialized.");

                        break; //Выходим из цикла, если удалось подключиться хоть к чему-то;
                    }

                } catch (UnknownHostException e) {
                    System.out.println("Failed was defined host IP address: " + hostname + "-" + port);
                } catch (IOException e) {
                    System.out.println("Failed connecting to server: " + hostname + "-" + port);
                }
        }
        if (socket == null || socket.isClosed()) {
            System.out.println("Not find available connections");
            return -1;
        }

        return 0;
    }

    public static void main(String[] args) {
        Domains.AddDomains(Domains.amountDomains);
        Arrays.fill(Domains.valid, Boolean.TRUE);

        /*Создание окна*/
        GameInterface gameInterface = new GameInterface();
        gameInterface.pack();
        gameInterface.setVisible(true);

        if (createValidConnection() < 0) {
            gameInterface.setStatusLabel("Failed connecting to server.");
        } else {

            GameInput gameInput = new GameInput(gameInterface);

            while (!socket.isOutputShutdown() && !gameInput.isWin() && gameInput.connection) {
                if (!gameInput.read(inputStream, socket)) {
                    if (Client.createValidConnection() < 0) {
                        gameInterface.setStatusLabel("Failed connecting to server.");
                        gameInput.connection = false;
                    } else {
                        gameInput.read(inputStream, socket);
                    }
                }
            }

            try {
                outputStream.close();
                inputStream.close();
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("For some reason, unable to close the socket or its channels.");
            }

            System.out.println("Client connections and channels closed successfully.");
        }
    }
}
