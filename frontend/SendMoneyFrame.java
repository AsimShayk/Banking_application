import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class SendMoneyFrame extends JFrame {
    private JComboBox<String> beneficiaryDropdown = new JComboBox<>();
    private JTextField amountField = new JTextField(20);
    private JLabel statusLabel = new JLabel();

    public SendMoneyFrame() {
        setTitle("Banking App - Send Money");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 5, 5));

        JButton sendButton = new JButton("Send");
        JButton backButton = new JButton("Back");

        add(new JLabel("Beneficiary:"));
        add(beneficiaryDropdown);
        add(new JLabel("Amount:"));
        add(amountField);
        add(sendButton);
        add(backButton);
        add(statusLabel);

        loadBeneficiaries();

        sendButton.addActionListener(e -> doSend());
        backButton.addActionListener(e -> {
            dispose();
            new DashboardFrame();
        });

        setVisible(true);
    }

    private void loadBeneficiaries() {
        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("GET_BENEFICIARIES");

            String line;
            while (!(line = in.readLine()).equals("END")) {
                beneficiaryDropdown.addItem(line);
            }

            socket.close();

        } catch (Exception ex) {
            statusLabel.setText("Cannot load beneficiaries.");
            ex.printStackTrace();
        }
    }

    private void doSend() {
        String beneficiary = (String) beneficiaryDropdown.getSelectedItem();
        String amount = amountField.getText().trim();

        if (amount.isEmpty()) {
            statusLabel.setText("Please enter an amount.");
            return;
        }

        String beneficiarySafe = beneficiary.replace(" ", "_");

        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("SEND " + beneficiarySafe + " " + amount);

            String response = in.readLine();

            if (response.equals("SUCCESS")) {
                statusLabel.setText("Money sent successfully.");
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