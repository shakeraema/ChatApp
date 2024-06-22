package src.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import src.database.UserAuthentication;
import src.database.UserRegistration;

import java.awt.*;
import java.awt.event.*;

public class ChatClientGUI {

    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton actionButton;
    private JButton switchModeButton;
    private JPanel loginPanel;
    private JPanel chatPanel;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton emojiButton;
    private JButton fileButton;
    private JButton groupChatButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private DefaultListModel<String> activeUserListModel;
    private JList<String> activeUserList;
    private String currentRecipient = "All Messages"; // To handle private/group messages
    private boolean isRegisterMode = false;
    private boolean isAuthenticated = false;
    private ChatClientLogic clientLogic;


    public ChatClientGUI() {

        clientLogic = new ChatClientLogic(this);

        // Login panel setup
        loginPanel = new JPanel(new GridLayout(4, 2));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        actionButton = new JButton("Login");
        switchModeButton = new JButton("New user? Register");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(actionButton);
        loginPanel.add(switchModeButton);

        // Chat panel setup
        chatPanel = new JPanel(new BorderLayout());
        messageArea = new JTextArea(25, 60);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        emojiButton = new JButton("\uD83D\uDE00"); // 😀
        fileButton = new JButton("File");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(fileButton, BorderLayout.SOUTH);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // User list panel
        JPanel userListPanel = new JPanel(new BorderLayout());
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane userScrollPane = new JScrollPane(userList);
        activeUserListModel = new DefaultListModel<>();
        activeUserList = new JList<>(activeUserListModel);
        activeUserList.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane activeUserScrollPane = new JScrollPane(activeUserList);
        userListPanel.add(new JLabel("All Users:"), BorderLayout.NORTH);
        userListPanel.add(userScrollPane, BorderLayout.CENTER);
        userListPanel.add(new JLabel("Active Users:"), BorderLayout.SOUTH);
        userListPanel.add(activeUserScrollPane, BorderLayout.SOUTH);
        chatPanel.add(userListPanel, BorderLayout.EAST);

        // Frame setup
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(loginPanel);
        frame.pack();
        frame.setVisible(true);

        // Action listeners
        actionButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (!isRegisterMode) {
                if (!isAuthenticated) {
                    if (UserAuthentication.authenticateUser(username, password)) {
                        isAuthenticated = true;
                        switchToChatPanel();
                        clientLogic.startConnection("127.0.0.1", 60000);
                        clientLogic.fetchAllUsers();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password");
                    }
                } else {
                    clientLogic.sendMessage();
                }
            } else {
                if (UserRegistration.registerUser(username, password)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful");
                    switchToLoginMode();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to register");
                }
            }
        });

        switchModeButton.addActionListener(e -> {
            if (isRegisterMode) {
                switchToLoginMode();
            } else {
                switchToRegisterMode();
            }
        });

        sendButton.addActionListener(e -> clientLogic.sendMessage());
        inputField.addActionListener(e -> clientLogic.sendMessage());
        emojiButton.addActionListener(e -> showEmojiPopup(emojiButton));
        fileButton.addActionListener(e -> clientLogic.sendFile());
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    showUserContextMenu(evt, userList);
                }
            }
        });
        activeUserList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    showUserContextMenu(evt, activeUserList);
                }
            }
        });
        groupChatButton = new JButton("Group Chat");
        groupChatButton.addActionListener(e -> initiateGroupChat());
        inputPanel.add(groupChatButton, BorderLayout.NORTH);
    }

    private void showUserContextMenu(MouseEvent evt, JList<String> userList) {
        int index = userList.locationToIndex(evt.getPoint());
        if (index != -1) {
            userList.setSelectedIndex(index);
            JPopupMenu contextMenu = new JPopupMenu();
            JMenuItem privateMessageMenuItem = new JMenuItem("Private Message");
            privateMessageMenuItem.addActionListener(e -> {
                String selectedUser = userList.getSelectedValue();
                setCurrentRecipient(selectedUser);
                frame.setTitle("Chat Client - Private Chat with " + selectedUser);
            });
            contextMenu.add(privateMessageMenuItem);
            contextMenu.show(userList, evt.getX(), evt.getY());
        }
    }

    private void showEmojiPopup(JButton emojiButton) {
        JPopupMenu emojiPopup = new JPopupMenu();

        String[] emojis = {
                "\uD83D\uDE03", // 😀
                "\uD83D\uDE02", // 😂
                "\uD83D\uDE05", // 😅
                "\uD83D\uDE0A", // 😊
                "\uD83D\uDE0D", // 😍
                "\uD83D\uDE0C", // 😌
                "\uD83D\uDE0E", // 😎
                "\uD83D\uDE0F", // 😏
                "\uD83D\uDC4D", // 👍
                "\uD83D\uDE20", // 😠
                "\uD83D\uDE31", // 😱
                "\uD83D\uDE37", // 😷
                "\uD83D\uDE44", // 🙄
                "\uD83D\uDE48", // 🙈
                "\uD83D\uDE49", // 🙉
                "\uD83D\uDE4A", // 🙊
                "\uD83D\uDE80", // 🚀
                "\uD83D\uDCAF", // 💯
                "\uD83C\uDF89" // 🎉
        };
        for (String emoji : emojis) {
            JMenuItem emojiItem = new JMenuItem(emoji);
            emojiItem.addActionListener(e -> inputField.setText(inputField.getText() + emoji));
            emojiPopup.add(emojiItem);
        }
        emojiPopup.show(emojiButton, emojiButton.getWidth() / 2, emojiButton.getHeight() / 2);
    }

    private void initiateGroupChat() {
        String groupName = JOptionPane.showInputDialog(frame, "Enter group name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            setCurrentRecipient("Group:" + groupName);
            frame.setTitle("Chat Client - Group Chat: " + groupName);
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public JTextArea getMessageArea() {
        return messageArea;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public DefaultListModel<String> getUserListModel() {
        return userListModel;
    }

    public String getCurrentRecipient() {
        return currentRecipient;
    }

    public void setCurrentRecipient(String currentRecipient) {
        this.currentRecipient = currentRecipient;
    }

    private void switchToLoginMode() {
        isRegisterMode = false;
        actionButton.setText("Login");
        switchModeButton.setText("Switch to Register");
        frame.remove(chatPanel);
        frame.add(loginPanel);
        frame.pack();
    }

    private void switchToRegisterMode() {
        isRegisterMode = true;
        actionButton.setText("Register");
        switchModeButton.setText("Switch to Login");
        frame.remove(chatPanel);
        frame.add(loginPanel);
        frame.pack();
    }

    public void switchToChatPanel() {
        frame.remove(loginPanel);
        frame.add(chatPanel);
        frame.pack();
    }
}