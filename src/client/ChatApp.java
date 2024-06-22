package src.client;

import javax.swing.SwingUtilities;

public class ChatApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }

}
