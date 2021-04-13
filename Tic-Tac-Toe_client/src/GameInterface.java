import javax.swing.*;
import java.awt.*;

public class GameInterface extends JFrame {

    public GameButton[][] buttons;
    private JLabel statusLabel;
    ChatInterface chatInterface = new ChatInterface();
    private final int SIZE = 3;
    public JPanel mainPanel = new JPanel(new BorderLayout());
    public GameInterface() {
        super("Tic-Tac-Toe");
        createGUI();
    }

    public void setStatusLabel(String status){
        statusLabel.setText(status);
    }
    public JTextArea getTextAreaForReceiveMessage() { return chatInterface.textArea;}

    private void createGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon("res/1.jpg");
        setIconImage(icon.getImage());

//        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE, 2, 2));
        gridPanel.setBackground(Color.BLACK);

        statusLabel = new JLabel("Awaiting connection opponent...");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.CYAN);
        statusLabel.setPreferredSize(new Dimension(600, 20));
        statusLabel.setMinimumSize(new Dimension(100, 20));

        gridPanel.setPreferredSize(new Dimension(600, 600));

        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        chatInterface.initChatGUI(mainPanel);
//        System.out.println(mainPanel.getComponent(2));
//        SplitPane  = 2 component

        buttons = new GameButton[SIZE][];
        for (int i = 0; i < SIZE; i++) {
            buttons[i] = new GameButton[SIZE];
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j] = new GameButton(i, j);
                buttons[i][j].setMargin(new Insets(0, 0, 0, 0));
                buttons[i][j].setEnabled(false);
                buttons[i][j].setBackground(Color.CYAN);
                gridPanel.add(buttons[i][j]);
            }
        }
        setSize(600, 600);
    }
}
