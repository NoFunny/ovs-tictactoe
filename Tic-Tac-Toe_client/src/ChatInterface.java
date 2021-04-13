import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ChatInterface {
    public JPanel chat_1 = new JPanel(), chat = new JPanel();
    public JTextArea textArea = new JTextArea(), textArea_1 = new JTextArea();
    public JSplitPane splitPane = new JSplitPane();

    public void initChatGUI(JPanel mainPanel) {
        //Message panels
        splitPane.setDividerLocation(300);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        //Receive message
        FlowLayout flowLayout_2 = new FlowLayout();
        flowLayout_2.setAlignment(FlowLayout.CENTER);
        chat_1.setLayout(flowLayout_2);

        JScrollPane scrollPane_1 = new JScrollPane();
        chat_1.add(scrollPane_1, BorderLayout.EAST);

        Border border_1 = BorderFactory.createLineBorder(Color.BLACK);
        textArea.setCursor(Cursor.getDefaultCursor());
        textArea.setBorder(BorderFactory.createCompoundBorder(border_1, BorderFactory.createEmptyBorder(100, 200, 100, 200)));
        scrollPane_1.setViewportView(textArea);

        // Send message
        FlowLayout flowLayout_1 = new FlowLayout();
        flowLayout_1.setAlignment(FlowLayout.RIGHT);
        chat.setLayout(flowLayout_1);

        JScrollPane scrollPane = new JScrollPane();
        chat.add(scrollPane, BorderLayout.CENTER);

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        textArea_1.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 200)));
        textArea_1.setWrapStyleWord(true);
        scrollPane.setViewportView(textArea_1);

        JButton button_1 = new JButton();
        button_1.setText("Send");
        chat.add(button_1);

        splitPane.setLeftComponent(chat);
        splitPane.setRightComponent(chat_1);
        mainPanel.add(splitPane,BorderLayout.SOUTH);
        button_1.addActionListener(new SendMessage());
    }

    public class SendMessage implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (Client.socket != null && !Client.socket.isClosed() && Client.outputStream != null) {
                try {
                    Client.outputStream.writeUTF("message/" + textArea_1.getText() + "/message");
                    Client.outputStream.flush();

                    textArea.setText(textArea.getText() + "\n" + "You: " + textArea_1.getText());
                    System.out.println("Send Message: " + textArea_1.getText());
                    textArea_1.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
