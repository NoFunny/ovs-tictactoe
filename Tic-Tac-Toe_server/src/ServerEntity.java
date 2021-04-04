import org.w3c.dom.ls.LSOutput;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.stream.IntStream;

public class ServerEntity {
    private ServerSocket serversSocket;
    private ServerSocket clientsSocket;

    public int initialization(Domain domain) {
        try {
            serversSocket = new ServerSocket(domain.port);
            clientsSocket = new ServerSocket(domain.port - 100);

            System.out.println(ConsoleColors.GREEN_BOLD
                    + "Server initializing...\n"
                    + ConsoleColors.GREEN
                    + "\tServer port: " + domain.port + ".\n"
                    + "\tClient port: " + (domain.port - 100)
                    + ConsoleColors.RESET);
            return 0;
        } catch (IOException e) {
            System.out.println("Port " + domain.port + " is busy.");
            return -1;
        }
    }

    public boolean isServerInitialized() {
        if (serversSocket != null && clientsSocket != null
                && !serversSocket.isClosed() && !clientsSocket.isClosed()) {
            return true;
        }
        return false;
    }

    class MySocket {
        private Socket socket;
        private DataOutputStream out;
        private MyDataInputStream in;

        private int port;
        private boolean online;

        private MySocket() {
            socket = null;
            out = null;
            in = null;

            port = Integer.MAX_VALUE;

            online = false;
        }

    }

    private ArrayList<MySocket> connectionList = new ArrayList<MySocket>();
    public void waitConnect() {
        Thread myThread = new Thread(() -> {
            while (true) {
                MySocket strSocket = new MySocket();

                try {
                    Socket tempSocket = null;

                    tempSocket = serversSocket.accept();

                    strSocket.socket = tempSocket;
                    strSocket.out = new DataOutputStream(tempSocket.getOutputStream());
                    strSocket.in = new MyDataInputStream(tempSocket.getInputStream());

                    System.out.println(ConsoleColors.YELLOW_BOLD
                            +"Open server connection on port: " + strSocket.socket.getPort() + "."
                            + ConsoleColors.RESET);

                    strSocket.online = true;

                    connectionList.add(strSocket);

                    if (Server.server.isGeneralServer()) {
                        for (int i = 0; i < 2; i++) {
                            if (Server.message[i] != null) {
                                Server.server.sendToAnotherServers(Server.message[i].substring(0));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Wait connection thread");

        myThread.start();
    }

    private MySocket socketsSList[] = new MySocket[Domains.amountDomains];
    private MySocket socketsCList[] = new MySocket[Domains.amountDomains];

    ServerEntity() {
        serversSocket = null;
        clientsSocket = null;

        for (int i = 0; i < Domains.amountDomains; i++) {
            socketsCList[i] = new MySocket();
            socketsSList[i] = new MySocket();
        }
    }

    public ServerSocket getClientsSocket() {
        return clientsSocket;
    }

    public int getServersPort() {
        return serversSocket.getLocalPort();
    }

    public void sendToAnotherServers(String message) {
        for (int i = 0; i < connectionList.size(); i++) {
            try {
                connectionList.get(i).out.writeUTF(message);
                connectionList.get(i).out.flush();

                System.out.println(ConsoleColors.YELLOW
                        + "Сообщение: "
                        + ConsoleColors.RESET
                        + message
                        + ConsoleColors.YELLOW
                        + " отправлено серверу "
                        + connectionList.get(i).socket.getPort() + ";"
                        + ConsoleColors.RESET);

            } catch (IOException ioException) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Сообщение: "
                        + ConsoleColors.RESET
                        + message
                        + ConsoleColors.RED_BOLD
                        + " не удалось отправить серверу "
                        + connectionList.get(i).socket.getPort() + ";"
                        + ConsoleColors.RESET);

                closeSocket(connectionList.get(i));

                connectionList.remove(i);
            }
        }
    }

    public String onlineServersList() {
        StringBuffer list = new StringBuffer("");

        for (int j = 0; j < Domains.domains.size(); j++) {
            if (socketsCList[j].online) {
                list.append("1");
            } else {
                list.append("0");
            }
        }
        return list.substring(0);
    }

    public boolean isGeneralServer() {
        for (int i = 0; i < Domains.amountDomains; i++) {
            if (socketsCList[i].online && Domains.domains.get(i).port < serversSocket.getLocalPort()) {
                return false;
            }
        }
        return true;
    }

    public void listenServer(MySocket socket) {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.online == true) {
                    String msg = null;

                    try {
                        System.out.println(ConsoleColors.YELLOW_BOLD
                                + "Чтение данных с порта " +
                                socket.socket.getPort() + ";"
                                + ConsoleColors.RESET);

                        msg = socket.in.myReadUTF();

                        if (msg != null) {
                            System.out.println(ConsoleColors.YELLOW_BOLD
                                    + "Получено сообщение от другого сервера: " + msg + "."
                                    + ConsoleColors.RESET);

                            parseMSG(msg, socket);
                        }
                    } catch (IOException e) {
                        System.out.println(ConsoleColors.RED_BOLD
                                + "Не удалось корректно принять данные от главного сервера. Соединение с ним будет закрыто."
                                + ConsoleColors.RESET);
                            closeSocket(socket);
                    }
                }
            }
        }, "Listen thread");

        myThread.start();
    }

    public void parseMSG(String message, MySocket socket) {
        String[] args = message.split("/");

        if (args.length == 4) {
            for (int i = 0; i < Server.players.length; i++) {
                if (args[0].equals(Character.toString(Server.players[i].getPlayerType()))) {
                    Server.message[i] = new StringBuffer(message);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equals("port")) {
                try {
                    socket.port = Integer.parseInt(args[1]);
                } catch (NumberFormatException en) {
                    en.printStackTrace();
                }
            }
        }

    }

    public boolean restoreGame(String message, GameLogic gameboard, Player[] players) {
        String[] args = message.split("/");
        if (args.length == 4) {
            gameboard.setCurrentMove(args[1].charAt(0));
            if (gameboard.setBoard(args[3]) == false) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Получены битые данные от главного сервера."
                        + ConsoleColors.RESET);

                return false;
            }
        }

        return true;
    }

    public void connectToOtherServers() {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i = 0; i < Domains.amountDomains; i++) {
                        try {
                            if (socketsCList[i].socket == null
                                    && Domains.domains.get(i).port != serversSocket.getLocalPort()) {

                                socketsCList[i].socket = new Socket(Domains.domains.get(i).hostname,
                                        Domains.domains.get(i).port);

                                socketsCList[i].out = new DataOutputStream(socketsCList[i].socket.getOutputStream());
                                socketsCList[i].in = new MyDataInputStream(socketsCList[i].socket.getInputStream());

                                socketsCList[i].online = true;

                                System.out.println(ConsoleColors.YELLOW_BOLD
                                        + "Каналы ввода и вывода инициализированны."
                                        + ConsoleColors.RESET);

                                listenServer(socketsCList[i]);
                            }
                        } catch (UnknownHostException e) {
                            System.out.println(ConsoleColors.RED_BOLD
                                    + "Не удалось определить IP адрес сервера: "
                                    + Domains.domains.get(i).hostname + "-"
                                    + Domains.domains.get(i).port
                                    + ConsoleColors.RESET);

                            closeSocket(socketsCList[i]);
                        } catch (IOException e) {
                            closeSocket(socketsCList[i]);
                        }
                    }
                }
            }
        }, "Connection thread");

        myThread.start();
    }

    public boolean closeSocket(MySocket mySocket) {
        boolean value = true;

        mySocket.online = false;

        if (mySocket == null) return false;

        if (mySocket != null && mySocket.socket != null) {
            System.out.println(ConsoleColors.RED
                    + "\tЗакрытие сокета " + mySocket.socket.getInetAddress().getHostAddress()
                    + "-" + mySocket.socket.getPort() + "..."
                    + ConsoleColors.RESET);

            if (mySocket.in != null) {
                try {
                    mySocket.in.close();
                    mySocket.in = null;

                    System.out.println(ConsoleColors.RED
                            + "\tЗакрыт канал ввода."
                            + ConsoleColors.RESET);
                } catch (IOException exception) {
                    exception.printStackTrace();

                    value = false;
                }
            }

            if (mySocket.out != null) {
                try {
                    mySocket.out.close();
                    mySocket.out = null;

                    System.out.println(ConsoleColors.RED
                            + "\tЗакрыт канал вывода."
                            + ConsoleColors.RESET);
                } catch (IOException exception) {
                    exception.printStackTrace();

                    value = false;
                }
            }

            if (mySocket.socket != null) {
                try {
                    mySocket.socket.close();
                    mySocket.socket = null;

                    System.out.println(ConsoleColors.RED
                            + "\tЗакрыт сокет."
                            + ConsoleColors.RESET);
                } catch (IOException exception) {
                    exception.printStackTrace();

                    value = false;
                }
            }

            mySocket = null;
        } else {
            value = false;
        }

        return value;
    }

    public boolean closeServer() {
        boolean value = true;

        if (serversSocket != null) {
            try {
                serversSocket.close();

                System.out.println(ConsoleColors.YELLOW_BOLD
                        + "Закрыт серверный сокет для серверов."
                        + ConsoleColors.RESET);
            } catch (IOException exception) {
                exception.printStackTrace();

                value = false;
            }
        }

        if (clientsSocket != null) {
            try {
                clientsSocket.close();

                System.out.println(ConsoleColors.YELLOW_BOLD
                        + "Закрыт серверный сокет для клиентов."
                        + ConsoleColors.RESET);
            } catch (IOException exception) {
                exception.printStackTrace();

                value = false;
            }
        }

        return value;
    }
}
