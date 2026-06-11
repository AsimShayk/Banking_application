import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TransactionsFrame extends JFrame {

    public TransactionsFrame() {
        setTitle("Banking App - Transactions");
        setSize(500, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Type", "Party", "Amount", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JButton backButton = new JButton("Back");

        backButton.addActionListener(e -> {
            dispose();
            new DashboardFrame();
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);

        loadTransactions(model);
        setVisible(true);
    }

    private void loadTransactions(DefaultTableModel model) {
        try {
            Socket socket = new Socket("localhost", 9090);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("GET_TRANSACTIONS");

            String line;
            while (!(line = in.readLine()).equals("END")) {
                String[] row = line.split(",");
                model.addRow(row);
            }

            socket.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}