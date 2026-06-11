import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ReceiveMoneyFrame extends JFrame {
    private JTextField senderField = new JTextField(20);
    private JTextField amountField = new JTextField(20);
    private JLabel statusLabel = new JLabel();

    public ReceiveMoneyFrame() {
        setTitle("Banking App - Receive Money");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 5, 5));

        JButton receiveButton = new JButton("Receive");
        JButton backButton = new JButton("Back");

        add(new JLabel("Sender Name:"));
        add(senderField);
        add(new JLabel("Amount:"));
        add(amountField);
        add(receiveButton);
        add(backButton);
        add(statusLabel);

        receiveButton.addActionListener(e -> doReceive());
        backButton.addActionListener(e -> {
            dispose();
            new DashboardFrame();
        });

        setVisible(true);
    }

    private void doReceive() {
        String sender = senderField.getText().trim();
        String amount = amountField.getText().trim();

        if (sender.isEmpty() || amount.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("RECEIVE " + sender + " " + amount);

            String response = in.readLine();

            if (response.equals("SUCCESS")) {
                statusLabel.setText("Money received successfully.");
                senderField.setText("");
                amountField.setText("");
            } else {
                statusLabel.setText("Transaction failed.");
            }

            socket.close();

        } catch (Exception ex) {
            statusLabel.setText("Cannot connect to server.");
            ex.printStackTrace();
        }
    }
}