import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.nio.file.Files;

public class MulticastChatClient {

    private String name;
    private InetAddress group;
    private int port = 4446;
    private MulticastSocket socket;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, leaveButton;
    private Thread receiverThread;

    public MulticastChatClient(String name) throws IOException {
        this.name = name;
        this.group = InetAddress.getByName("230.0.0.0");
        this.socket = new MulticastSocket(port);
        socket.setReuseAddress(true);
        socket.joinGroup(group);

        createGUI();
        startReceiverThread();
    }

    private void createGUI() {
        JFrame frame = new JFrame("Multicast Chat's - " + name);
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        Color bgDark = new Color(40, 40, 40);
        Color bgField = new Color(60, 60, 60);
        Color textColor = Color.WHITE;
        Color buttonColor = new Color(140, 90, 255);

        frame.getContentPane().setBackground(bgDark);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setBackground(bgField);
        chatArea.setForeground(textColor);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.getViewport().setBackground(bgField);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgDark);

        messageField = new JTextField();
        messageField.setBackground(bgField);
        messageField.setForeground(textColor);
        messageField.setCaretColor(textColor);
        messageField.setBorder(BorderFactory.createLineBorder(new Color(120,120,120)));

        sendButton = new JButton("Send");
        leaveButton = new JButton("Leave");

        sendButton.setBackground(buttonColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        leaveButton.setBackground(new Color(180, 60, 60));
        leaveButton.setForeground(Color.WHITE);
        leaveButton.setFocusPainted(false);

        bottomPanel.add(leaveButton, BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        leaveButton.addActionListener(e -> leaveChat(frame));

        frame.setVisible(true);
    }

    private void startReceiverThread() {

        Runnable receiverTask = new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (!socket.isClosed()) {
                    try {
                        socket.receive(packet);
                        String received = new String(packet.getData(), packet.getOffset(), packet.getLength());

                        // Mos shfaq mesazhet qe jane te dërguara nga vetvetja
                        if (received.startsWith(name + ":")) continue;

                        // Mos shfaq mesazhin e largimit që është dërguar nga vetvetja
                        if (received.contains(name + " left the chat")) continue;

                        chatArea.append(received + "\n");

                    } catch (IOException e) {
                        break;
                    }
                }
            }
        };

        receiverThread = new Thread(receiverTask);
        receiverThread.start();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        try {
            String fullMessage = name + ": " + message;
            byte[] buf = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            socket.send(packet);

            chatArea.append("ME: " + message + "\n");

            saveMessageToJson(name, message);

            messageField.setText("");

        } catch (IOException e) {
            chatArea.append("Failed to send message.\n");
        }
    }

    private void leaveChat(JFrame frame) {
        try {
            // Dërgo mesazh largimi vetëm për të tjerët
            String leaveMsg = "-" + name + " left the chat.";
            byte[] buf = leaveMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            socket.send(packet);

            socket.leaveGroup(group);
            socket.close();

            // Shfaq vetëm te klienti që largohet
            chatArea.append("You left the  chat.\n");

            JOptionPane.showMessageDialog(frame, "You left the group chat.", "Exit", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToJson(String user, String message) {
        try {
            File file = new File("chat_history.json");

            if (!file.exists()) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"messages\": []} ");
                }
            }

            String content = new String(Files.readAllBytes(file.toPath()));
            int insertPos = content.lastIndexOf("]");

            String timestamp = LocalDateTime.now().toString();

            String newEntry = String.format(
                "%s{\"name\":\"%s\",\"message\":\"%s\",\"time\":\"%s\"}",
                (content.contains("{\"messages\": []}") ? "" : ","),
                user,
                message.replace("\"", "'"),
                timestamp
            );

            String updated = content.substring(0, insertPos) + newEntry + content.substring(insertPos);

            Files.write(file.toPath(), updated.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog("Enter your name:");
            if (name != null && !name.trim().isEmpty()) {
                try {
                    new MulticastChatClient(name.trim());
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error connecting to group!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
