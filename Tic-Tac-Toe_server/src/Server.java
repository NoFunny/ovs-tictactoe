import java.util.ArrayList;

public class Server {
    private static boolean createServersConnections(ArrayList<Domain> domains) {
        for (int i = 0; i < domains.size(); i++) {
            if (server.initialization(domains.get(i)) >= 0)
                break;
        }

        if (!server.isServerInitialized()) {
            System.out.println(ConsoleColors.RED_BOLD
                    + "Server cannot be started, all ports are already occupied."
                    + ConsoleColors.RESET);
            return false;
        }
        return true;
    }

    static void generateTypes(Player[] players) {
        players[0].setPlayerType('o');
        players[1].setPlayerType('x');
    }

    public static StringBuffer[] message = new StringBuffer[2];
    public static Player[] players = new Player[2];
    public static GameLogic board;

    private static void newGame() {
        for (int i = 0; i < 2; i++) {
            players[i] = new Player();
            message[i] = null;
        }
        board = new GameLogic();
        generateTypes(players);
    }

    public static ServerEntity server = new ServerEntity();

    public static void main(String[] args) {
        Domains.AddDomains(Domains.amountDomains);

        if (!createServersConnections(Domains.domains)) return;

        newGame();

        server.waitConnect();
        server.connectToOtherServers();

        boolean reservFlag = false;

        while(true) {
            do {
                if (!reservFlag) {
                    System.out.println(ConsoleColors.GREEN_BOLD
                            + "Server marked as reserved."
                            + ConsoleColors.RESET);
                    reservFlag = true;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            } while(!server.isGeneralServer());

            reservFlag = false;

            System.out.println(
                ConsoleColors.GREEN_BOLD
                + "Server marked as general."
                + ConsoleColors.RESET
            );
            boolean successRestore = false;
            for (int i = 0; i < 2; i++) {
                if (!players[i].connection)
                    players[i].waitConnection(server.getClientsSocket());

                if (message[i] != null) {
                    successRestore = server.restoreGame(message[i].substring(0), board, players);
                }
            }

            if (!successRestore) {
                message[0] = null;
                message[1] = null;
                board = new GameLogic();
            }

            while (players[0].connection && players[1].connection) {
                System.out.println(ConsoleColors.YELLOW_BOLD
                        + "\nGame takes place on the server: " + board.toString()
                        + ConsoleColors.RESET);

                char win = board.getWinner();

                for (int i = 0; i < 2; i++) {
                    message[i] = new StringBuffer("");
                    message[i].append(players[i].getPlayerType());
                    message[i].append("/");
                    message[i].append(board.currentMove());
                    message[i].append("/");
                    message[i].append(win);
                    message[i].append("/");
                    message[i].append(board.toString());
                    message[i].append("/");

                    players[i].send(message[i].substring(0));

                    server.sendToAnotherServers(message[i].substring(0));

                    players[i].send("servers/" + server.onlineServersList());
                }

                if (win != '_') {
                    newGame();
                    for (int i = 0; i < 2; i++) {
                        message[i] = new StringBuffer("");
                        message[i].append(players[i].getPlayerType());
                        message[i].append("/");
                        message[i].append(board.currentMove());
                        message[i].append("/");
                        message[i].append("_");
                        message[i].append("/");
                        message[i].append("_________");
                        message[i].append("/");

                        server.sendToAnotherServers(message[i].substring(0));
                    }
                    break;
                }

                String move = null;

                for (int i = 0; i < 2; i++) {
                    String currentMove = players[i].readMoveIfActive(board.currentMove());
                    if (currentMove == null)
                        continue;
                    move = currentMove;
                }

                if (move != null) {
                    if (!board.process(move)) {
                        for (int i = 0; i < 2; i++)
                            players[i].send("error/bad_data");
                    }
                } else {
                    for (int i = 0; i < 2; i++)
                        if (players[i].getPlayerType() != board.currentMove())
                            players[i].send("error/opponent_connection");
                    newGame();
                }
            }
        }
    }
}

