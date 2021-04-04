import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Player {

    private DataOutputStream out;
    private MyDataInputStream in;
    private Socket clientSocket;
    private char playerType;

    private String host;
    private int port;

    public boolean connection = false;

    Player() {
        clientSocket = null;
        in = null;
        out = null;
        playerType = '_';
    }

    void waitConnection(ServerSocket serverSocket) {

        try {
            System.out.println(ConsoleColors.YELLOW_BOLD
                    + "\nWaiting for client connection..."
                    + ConsoleColors.RESET);

            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED_BOLD
                    + "Failed to connect to client."
                    + ConsoleColors.RESET);
            return;
        }
        System.out.print(ConsoleColors.GREEN_BOLD
                + "Connection confirmed.\n"
                + ConsoleColors.RESET);
        connection = true;

        host = clientSocket.getInetAddress().getHostAddress();
        port = clientSocket.getPort();

        /******************** Connected client address ****************/
        System.out.println(ConsoleColors.GREEN
                + "Host: " + host
                + ConsoleColors.RESET);
        System.out.println(ConsoleColors.GREEN
                + "Port: " + port
                + ConsoleColors.RESET);
        /**************************************************************/

        try {
            in = new MyDataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED_BOLD
                    + "Could not open I / O pipes for socket "
                    + clientSocket.getInetAddress().getHostAddress()
                    + "-" + clientSocket.getPort() + "."
                    + ConsoleColors.RESET);
        }
    }

/*
    void waitConnection(ServerSocket serverSocket) throws IOException {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(2);


        Runnable playerConnect = new Runnable() {
            @Override
            public void run() {

                try {
                    clientSocket = serverSocket.accept();

                    System.out.print("\nConnection accepted.\n");
                    connection = true;

                    host = clientSocket.getInetAddress().getHostAddress();
                    port = clientSocket.getPort();

                    //Адрес подключенного клиента
                    System.out.println("Host: " + host);
                    System.out.println("Port: " + port);

                    in = new DataInputStream(clientSocket.getInputStream());
                    out = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Подключение клиента провалено.");
                }
            }
        };

        Thread serverThread = new Thread(playerConnect);
        serverThread.start();
    }
*/

        String readMoveIfActive ( char currentPlayerType){
            String result = null;

            if (playerType != currentPlayerType)
                return null;

            try {
                result = in.myReadUTF();

                System.out.println(ConsoleColors.YELLOW
                        + "Received data from client: "
                        + ConsoleColors.RESET
                        + result);
            } catch (EOFException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Client data: " + host + " " + port + " incompletely received, check the connection."
                        + ConsoleColors.RESET);
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } catch (UTFDataFormatException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Client data: " + host + " " + port + " received in invalid format."
                        + ConsoleColors.RESET);
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "No connection with the client: " + host + " " + port + "."
                        + ConsoleColors.RESET);
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            return result;
        }

        void send (String message) {
            try {
                out.writeUTF(message);
                out.flush();

                System.out.println(ConsoleColors.YELLOW
                        + "Message: "
                        + ConsoleColors.RESET
                        + message
                        + ConsoleColors.YELLOW
                        + " sent to client " + host + "-" + port + "."
                        + ConsoleColors.RESET);
            } catch (IOException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Failed to send message to client" + host + "-" + port + "."
                        + ConsoleColors.RED_BOLD);

                connection = false;
            }
        }

        void setPlayerType ( char type){
            playerType = type;
        }

        char getPlayerType () {
            return playerType;
        }

        public int getPort () {
            return port;
        }

        public String getHost () {
            return host;
        }
    }