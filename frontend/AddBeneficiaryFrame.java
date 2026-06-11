import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class AddBeneficiaryFrame extends JFrame {
    private JTextField nameField = new JTextField(20);
    private JTextField accountField = new JTextField(20);
    private JTextField ifscField = new JTextField(20);
    private JLabel statusLabel = new JLabel();

    public AddBeneficiaryFrame() {
        setTitle("Banking App - Add Beneficiary");
        setSize(300, 230);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 5, 5));

        JButton addButton = new JButton("Add");
        JButton backButton = new JButton("Back");

        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Account No:"));
        add(accountField);
        add(new JLabel("IFSC Code:"));
        add(ifscField);
        add(addButton);
        add(backButton);
        add(statusLabel);

        addButton.addActionListener(e -> doAdd());
        backButton.addActionListener(e -> {
            dispose();
            new DashboardFrame();
        });

        setVisible(true);
    }

    private void doAdd() {
        String name = nameField.getText().trim();
        String account = accountField.getText().trim();
        String ifsc = ifscField.getText().trim();

        if (name.isEmpty() || account.isEmpty() || ifsc.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("ADD_BENEFICIARY " + name + " " + account + " " + ifsc);

            String response = in.readLine();

            if (response.equals("SUCCESS")) {
                statusLabel.setText("Beneficiary added successfully.");
                nameField.setText("");
                accountField.setText("");
                ifscField.setText("");
            } else {
                statusLabel.setText("Failed to add beneficiary.");
            }

            socket.close();

        } catch (Exception ex) {
            statusLabel.setText("Cannot connect to server.");
            ex.printStackTrace();
        }
    }
}