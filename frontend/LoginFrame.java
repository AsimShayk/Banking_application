import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField emailField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JLabel statusLabel = new JLabel();

    public LoginFrame() {
        setTitle("Banking App - Login");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 5, 5));

        JButton loginButton = new JButton("Login");
        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(statusLabel);

        loginButton.addActionListener(e -> doLogin());
        setVisible(true);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("LOGIN " + email + " " + password);

            String response = in.readLine();

            if (response.equals("SUCCESS")) {
                statusLabel.setText("Login successful.");
                dispose();
                new DashboardFrame();
            } else {
                statusLabel.setText("Invalid email or password.");
            }

            socket.close();

        } catch (Exception ex) {
            statusLabel.setText("Cannot connect to server.");
            ex.printStackTrace();
        }
    }
}