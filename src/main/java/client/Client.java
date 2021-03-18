package client;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Client {

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("...");

    private GameBoard[] board = new GameBoard[9];
    private GameBoard currentGameBoard;

    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    public Client(String serverAddress, String serverPort) throws Exception {
        socket = new Socket(serverAddress, Integer.parseInt(serverPort));
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

        var boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (var i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new GameBoard();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentGameBoard = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
    }

    /**
     *  Основной поток клиента будет прослушивать сообщения от сервера.
     *  Первым сообщением будет сообщение «WELCOME»,
     *  в котором мы получим нашу оценку. Затем мы переходим в цикл,
     *  прослушивая любые другие сообщения и соответствующим образом
     *  обрабатывая каждое сообщение. Сообщения «VICTORY», «DEFEAT», «TIE»
     *  и «OTHER_PLAYER_LEFT» будут спрашивать пользователя,
     *  играть ли в другую игру или нет. Если ответ отрицательный,
     *  цикл завершается, и серверу отправляется сообщение «ВЫЙТИ».
     */
    public void play() throws Exception {
        try {
            var response = in.nextLine();
            var mark = response.charAt(8);
            var opponentMark = mark == 'X' ? 'O' : 'X';
            frame.setTitle("Tic Tac Toe: Player " + mark);
            while (in.hasNextLine()) {
                response = in.nextLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentGameBoard.setText(mark);
                    currentGameBoard.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    var loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(opponentMark);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                } else if (response.startsWith("VICTORY")) {
                    JOptionPane.showMessageDialog(frame, "Winner Winner");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    JOptionPane.showMessageDialog(frame, "Sorry you lost");
                    break;
                } else if (response.startsWith("TIE")) {
                    JOptionPane.showMessageDialog(frame, "Tie");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(frame, "Other player left");
                    break;
                }
            }
            out.println("QUIT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Enter ip address and port\nUsage:\n\t127.0.0.1   123\n\t<address> <port>\n");
            return;
        }
        Client client = new Client(args[0], args[1]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setSize(320, 320);
        client.frame.setVisible(true);
        client.frame.setResizable(false);
        client.play();
    }
}