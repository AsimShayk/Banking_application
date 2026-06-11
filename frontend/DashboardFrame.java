import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class DashboardFrame extends JFrame {
    private JLabel balanceLabel = new JLabel("Current Balance: Loading...");

    public DashboardFrame() {
        setTitle("Banking App - Dashboard");
        setSize(300, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1, 5, 5));

        JButton sendButton = new JButton("Send Money");
        JButton receiveButton = new JButton("Receive Money");
        JButton addBeneficiaryButton = new JButton("Add Beneficiary");
        JButton transactionsButton = new JButton("View Transactions");

        add(balanceLabel);
        add(sendButton);
        add(receiveButton);
        add(addBeneficiaryButton);
        add(transactionsButton);

        sendButton.addActionListener(e -> { dispose(); new SendMoneyFrame(); });
        receiveButton.addActionListener(e -> { dispose(); new ReceiveMoneyFrame(); });
        addBeneficiaryButton.addActionListener(e -> { dispose(); new AddBeneficiaryFrame(); });
        transactionsButton.addActionListener(e -> { dispose(); new TransactionsFrame(); });

        loadBalance();
        setVisible(true);
    }

    private void loadBalance() {
        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("GET_BALANCE");

            String response = in.readLine();
            balanceLabel.setText("Current Balance: " + response);

            socket.close();

        } catch (Exception ex) {
            balanceLabel.setText("Balance: Unable to load.");
            ex.printStackTrace();
        }
    }
}