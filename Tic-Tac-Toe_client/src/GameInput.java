import javax.swing.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.Socket;

public class GameInput {

    private final int SIZE = 3;

    public boolean connection = true;

    public boolean isWin() {
        return win;
    }

    private boolean win = false;

    GameInterface gameInterface;

    GameInput(GameInterface _gameInterface) {
        gameInterface = _gameInterface;
    }

    public boolean read(MyDataInputStream inputStream, Socket socket) {
        System.out.println("Waiting for a response from the server...");

        String in = null;

        try {
            in = inputStream.myReadUTF();

            System.out.println("Received message from server: " + in);
        } catch (EOFException e) {
            System.out.println("Data from server " + Client.socket.getInetAddress().getHostName()
                    + " " + Client.socket.getPort() + " received not completely, check the connection.");

            return false;
        } catch (UTFDataFormatException e) {
            System.out.println("Data from client " + Client.socket.getInetAddress().getHostName() + " " + Client.socket.getPort() + " received in the wrong format.");

            return false;
        } catch (IOException e) {
            System.out.println("There is no connection with the client: " + Client.socket.getInetAddress().getHostName() + " " + Client.socket.getPort() + ".");

            return false;
        }

        if (in != null) {
            adapter(in, socket);
        }

        return true;
    }

    private void adapter(String in, Socket socket) {
        String[] args = in.split("/");
        if (args.length > 0) {
            if (args.length == 4) {
                if (args[2].equals("_")) {
                    if (args[0].equals(args[1])) {
                        gameInterface.setStatusLabel("Your turn (" + args[1] + ")");
                    } else {
                        gameInterface.setStatusLabel("Opponent's move (" + args[1] + ")");
                    }

                    setActiveBoard(args[0].equals(args[1]));
                } else {
                    win = true;

                    if (args[2].equals(args[0])) {
                        gameInterface.setStatusLabel("You win!");

                        System.out.println("You win!");
                    } else {
                        gameInterface.setStatusLabel("You lose!");

                        System.out.println("You lose!");
                    }

                    setActiveBoard(false);
                }


                setBoard(args[3]);
            } else if (args.length == 3) {
                if (args[0].equals("message") && !args[1].isEmpty()) {
                    System.out.println(gameInterface.getComponent(0));

                    JTextArea textArea = gameInterface.getTextAreaForReceiveMessage();
                    textArea.setText(textArea.getText() + "\n" + "Opponent: " + args[1]);
                }
            } else if (args.length == 2) {
                if (args[0].equals("error")) {
                    if (args[1].equals("opponent_connection")) {
                        System.out.println("The opponent has disconnected from the server. Game over.");
                        gameInterface.setStatusLabel("The opponent has disconnected from the server. Game over.");
                        try {
                            socket.close();
                            connection = false;
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    } else if (args[1].equals("bad_data")) {
                        System.out.println("The server received suspicious data from the client. " +
                                "The session will be closed");

                        gameInterface.setStatusLabel("The server received suspicious data from one of the clients. " +
                                "Game over.");
                        try {
                            socket.close();
                            connection = false;
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                } else if (args[0].equals("servers")) {
                    for (int k = 0; k < args[1].length(); k++) {
                        if (args[1].charAt(k) == '1')
                        {
                            Domains.valid[k] = true;
                        } else if (args[1].charAt(k) == '0') {
                            Domains.valid[k] = false;
                        } else {
                            badData(socket);
                        }
                    }
                } else {
                    badData(socket);
                }
            }
        }
    }

    private void badData(Socket socket) {
        System.out.println("Bad data from server");
        gameInterface.setStatusLabel("Suspicious server behavior. The connection will be dropped.");
        try {
            socket.close();
            connection = false;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    void setActiveBoard(boolean value) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                gameInterface.buttons[i][j].setEnabled(value);
            }
        }
    }

    private void setBoard(String board) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                char c = board.charAt(i * SIZE + j);
                if (c != '_') {
                    if( c == 'x') {
                        gameInterface.buttons[i][j].setIcon(new ImageIcon("res/x.png"));
                    }else if ( c == 'o') {
                        gameInterface.buttons[i][j].setIcon(new ImageIcon("res/0.png"));
                    }
                }
            }
        }
    }
}
