import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBSetup {
    public static void setup() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:../database/banking.db");
            Statement stmt = conn.createStatement();

            String sql = Files.readString(Paths.get("../database/Bankingdb.sql"));
            stmt.executeUpdate(sql);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            int count = rs.getInt(1);

            if (count == 0) {
                String hashedPassword = hash("abcd1234");
                stmt.executeUpdate(
                    "INSERT INTO users (full_name, email, password_hash, account_no, balance) " +
                    "VALUES ('Asim Shaikh', 'asim@gmail.com', '" +
                    hashedPassword + "', '10003', 50000.0)"
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}