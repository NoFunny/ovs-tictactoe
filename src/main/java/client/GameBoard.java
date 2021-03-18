package client;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.*;

class GameBoard extends JPanel {
    JLabel label = new JLabel();

    public GameBoard() {
        setBackground(Color.white);
        setLayout(new GridBagLayout());
        label.setFont(new Font("Arial", Font.BOLD, 40));
        add(label);
    }

    public void setText(char text) {
        label.setForeground(text == 'X' ? Color.BLUE : Color.RED);
        label.setText(text + "");
    }
}