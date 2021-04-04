import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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

        for (int i = 0; i < ServersList.ServersAddresses.length; i++) {
                try {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                    if (socket != null && !socket.isClosed()) socket.close();

                    if (ServersList.valid[i]) {
                        socket = new Socket(ServersList.ServersAddresses[i][0], Integer.parseInt(ServersList.ServersAddresses[i][1]));
                        outputStream = new DataOutputStream(socket.getOutputStream());
                        inputStream = new MyDataInputStream(socket.getInputStream());

                        System.out.println("Clien connected to server: "
                                + ServersList.ServersAddresses[i][0] + "-"
                                + ServersList.ServersAddresses[i][1]);

                        System.out.println();
                        System.out.println("Input and output channels has been initialized.");

                        break; //Выходим из цикла, если удалось подключиться хоть к чему-то;
                    }

                } catch (UnknownHostException e) {
                    System.out.println("Failed was defined host IP address: "
                            + ServersList.ServersAddresses[i][0] + "-"
                            + ServersList.ServersAddresses[i][1]);
                } catch (IOException e) {
                    System.out.println("Failed connecting to server: "
                            + ServersList.ServersAddresses[i][0] + "-"
                            + ServersList.ServersAddresses[i][1]);
                }
        }
        if (socket == null || socket.isClosed()) {
            System.out.println("Not find available connections");
            return -1;
        }

        return 0;
    }

    public static void main(String[] args) {
        /*Создание окна*/
        GameInterface gameInterface = new GameInterface();
        gameInterface.pack();
        gameInterface.setVisible(true);

        for (int i = 0; i < ServersList.ServersAddresses.length; i++) {
            ServersList.valid[i] = true;
        }

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
