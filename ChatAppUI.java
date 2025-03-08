import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatAppUI {
    private static PrintWriter out;
    private static BufferedReader in;
    private static JTextArea chatArea;
    private static String username;
    private static String serverIp;
    private static int fontSize = 14;

    public static void main(String[] args) {

        JFrame frame = new JFrame("One-time Messenger");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true); // Disable resizing
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);

        JMenuBar menuBar = new JMenuBar();
        JMenu themeMenu = new JMenu("Theme");
        JMenuItem white = new JMenuItem("White");
        JMenuItem dark = new JMenuItem("Dark");
        JMenuItem black = new JMenuItem("Black");
        JMenuItem gray = new JMenuItem("Gray");
        JMenu controlMenu = new JMenu("Controls");
        JMenuItem show = new JMenuItem("Show");

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.LIGHT_GRAY);
        chatArea.setForeground(Color.BLACK);
        chatArea.setFont(new Font("MyFont", Font.PLAIN, fontSize));

        JScrollPane scrollPane = new JScrollPane(chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, // Vertical scrollbar as needed
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Horizontal scrollbar as needed
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextArea inputField = new JTextArea();
        JButton sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Create action listener for the themes
        ActionListener themeChangeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color newColor = Color.LIGHT_GRAY; // Default
                Color newforecolor = Color.BLACK;

                if (e.getSource() == white) {
                    newColor = Color.WHITE;
                    newforecolor = Color.BLACK;
                } else if (e.getSource() == dark) {
                    newColor = Color.DARK_GRAY;
                    newforecolor = Color.WHITE;
                } else if (e.getSource() == black) {
                    newforecolor = Color.WHITE;
                    newColor = Color.BLACK;
                } else if (e.getSource() == gray) {
                    newforecolor = Color.BLACK;
                    newColor = Color.LIGHT_GRAY;
                }

                frame.getContentPane().setBackground(newColor);
                chatArea.setBackground(newColor);
                chatArea.setForeground(newforecolor);
            }
        };

        white.addActionListener(themeChangeListener);
        dark.addActionListener(themeChangeListener);
        black.addActionListener(themeChangeListener);
        gray.addActionListener(themeChangeListener);

        themeMenu.add(white);
        themeMenu.add(dark);
        themeMenu.add(black);
        themeMenu.add(gray);
        menuBar.add(themeMenu);
        menuBar.add(controlMenu);
        controlMenu.add(show);
        frame.setJMenuBar(menuBar);
        

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    inputField.setText("");
                    inputField.requestFocusInWindow(); // Refocus input field
                }
            }
        });

        show.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "F1 for Zoom-IN \nF2 for Zoom-OUT \nF3 for BOLD text", "Controls", JOptionPane.NO_OPTION);
            }
        });
    
        // Add KeyListener to inputField to send message when Enter is pressed
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // Check if Enter key is pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // If Shift is not held, send the message
                    if (!e.isShiftDown()) {
                        String message = inputField.getText().trim();
                        if (!message.isEmpty()) {
                            sendMessage(message.trim());
                            inputField.setText(""); // Clear input after sending
                        }
                    } else {
                        // If Shift is held, add a new line (append newline to text area)
                        inputField.append("\n");
                    }
                    e.consume(); // it will consume or stop it from processing further and adding unwanted new
                                 // line
                }
            }
        });

        chatArea.addKeyListener(new KeyAdapter() {
            private boolean isBold = false; // Variable to track font style
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F1) { 
					chatArea.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, fontSize++));
                }

                if (e.getKeyCode() == KeyEvent.VK_F2) { 
                    chatArea.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, fontSize--));
                }

                if (e.getKeyCode() == KeyEvent.VK_F3) { 
                    isBold = !isBold; // Toggle bold state
                    chatArea.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, fontSize)); 
                }
            }
        });

        frame.setVisible(true);

        // Start the client in a new thread
        new Thread(ChatAppUI::startClient).start();
    }
    
    private static void startClient() {

        serverIp = JOptionPane.showInputDialog("Enter Server IP:");
        if (serverIp == null || serverIp.trim().isEmpty()) {
            serverIp = ""; // Default
        }

        username = JOptionPane.showInputDialog("Enter your name:");
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous";
        }

        try {
            // System.out.println("Connecting to server...");
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverIp, 8080), 10000); // 10 second timeout
            JOptionPane.showMessageDialog(null, "Connection Successful");
            chatArea.append("Connected to server\n");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Listen for incoming messages from the server
            while (true) {
                String message = in.readLine();
                if (message == null)
                    break;

                // check the message received
                // System.out.println("Received message: " + message);

                // Update UI safely in the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
            }

            socket.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.", "Connection Error",JOptionPane.ERROR_MESSAGE);
            // System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sendMessage(String message) {
        if (out != null) {
            out.println(username + ": " + message);
            chatArea.append("ME: " + message + "\n");

            // confirm message is being sent
            // System.out.println("Sent message: ME: " + message);
        }
    }
}
