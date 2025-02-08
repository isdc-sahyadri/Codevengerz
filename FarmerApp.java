import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FarmerApp {
    private JFrame frame;
    private JTextField usernameField, passwordField;
    private boolean isAdmin = false;
    private static final String URL = "jdbc:sqlite:farmerapp.db";

    public FarmerApp() {
        setupDatabase();
        showRoleSelectionScreen();
    }

    private void showRoleSelectionScreen() {
        frame = new JFrame("Select Role");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel label = new JLabel("Choose Login Type:", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton btnAdmin = new JButton("Admin Login");
        JButton btnUser = new JButton("User Login");

        btnAdmin.addActionListener(e -> {
            frame.dispose();
            showLoginScreen(true);
        });

        btnUser.addActionListener(e -> {
            frame.dispose();
            showLoginScreen(false);
        });

        buttonPanel.add(btnAdmin);
        buttonPanel.add(btnUser);

        frame.add(label, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void showLoginScreen(boolean isAdminLogin) {
        this.isAdmin = isAdminLogin;
        frame = new JFrame(isAdmin ? "Admin Login" : "User Login");
        frame.setSize(350, 200);
        frame.setLayout(new GridLayout(3, 2));

        frame.add(new JLabel("Username:"));
        usernameField = new JTextField();
        frame.add(usernameField);

        frame.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        frame.add(passwordField);

        JButton btnLogin = new JButton("Login");
        frame.add(btnLogin);
        btnLogin.addActionListener(e -> loginUser());

        frame.setVisible(true);
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isAdmin && username.equals("admin") && password.equals("admin123")) {
            JOptionPane.showMessageDialog(frame, "Admin Login Successful!");
            frame.dispose();
            showAdminPanel();
        } else if (!isAdmin) {
            JOptionPane.showMessageDialog(frame, "User Login Successful!");
            frame.dispose();
            showUserPanel();
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid Credentials");
        }
    }

    private void showAdminPanel() {
        frame = new JFrame("Admin Panel");
        frame.setSize(500, 400);
        frame.setLayout(new GridLayout(4, 1));

        JButton btnAddProduct = new JButton("Add Product");
        JButton btnViewProducts = new JButton("View Products");
        JButton btnViewOrders = new JButton("View Orders");
        JButton btnExit = new JButton("Exit");

        frame.add(btnAddProduct);
        frame.add(btnViewProducts);
        frame.add(btnViewOrders);
        frame.add(btnExit);

        btnAddProduct.addActionListener(e -> addProduct());
        btnViewProducts.addActionListener(e -> viewProducts());
        btnViewOrders.addActionListener(e -> viewOrders());
        btnExit.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private void addProduct() {
        String name = JOptionPane.showInputDialog("Enter Product Name:");
        String priceStr = JOptionPane.showInputDialog("Enter Product Price:");

        if (name != null && priceStr != null) {
            try {
                double price = Double.parseDouble(priceStr);
                try (Connection conn = DriverManager.getConnection(URL);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO products (name, price) VALUES (?, ?)")) {
                    pstmt.setString(1, name);
                    pstmt.setDouble(2, price);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Product Added Successfully!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error adding product.");
            }
        }
    }

    private void viewProducts() {
        StringBuilder productsList = new StringBuilder("Products:\n");
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            while (rs.next()) {
                productsList.append(rs.getInt("id")).append(": ")
                        .append(rs.getString("name")).append(" - ₹")
                        .append(rs.getDouble("price")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(frame, productsList.toString());
    }

    private void viewOrders() {
        StringBuilder ordersList = new StringBuilder("Orders:\n");
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT orders.id, products.name, orders.address FROM orders INNER JOIN products ON orders.product_id = products.id")) {
            while (rs.next()) {
                ordersList.append("Order ").append(rs.getInt("id"))
                        .append(": ").append(rs.getString("name"))
                        .append(" - Address: ").append(rs.getString("address")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(frame, ordersList.toString());
    }

    private void showUserPanel() {
        frame = new JFrame("User Panel");
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(2, 1));

        JButton btnViewProducts = new JButton("View & Buy Products");
        JButton btnExit = new JButton("Exit");

        frame.add(btnViewProducts);
        frame.add(btnExit);

        btnViewProducts.addActionListener(e -> buyProduct());
        btnExit.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private void buyProduct() {
        String productIdStr = JOptionPane.showInputDialog("Enter Product ID to Buy:");
        String address = JOptionPane.showInputDialog("Enter Your Address:");

        if (productIdStr != null && address != null) {
            try {
                int productId = Integer.parseInt(productIdStr);
                String upiTransactionId = JOptionPane.showInputDialog("Enter Payment Transaction ID:");

                if (upiTransactionId != null && !upiTransactionId.trim().isEmpty()) {
                    try (Connection conn = DriverManager.getConnection(URL);
                         PreparedStatement pstmt = conn.prepareStatement("INSERT INTO orders (product_id, address) VALUES (?, ?)")) {
                        pstmt.setInt(1, productId);
                        pstmt.setString(2, address);
                        pstmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Payment Successful! Order Placed.");
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error processing payment.");
            }
        }
    }

    private void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, address TEXT, FOREIGN KEY(product_id) REFERENCES products(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FarmerApp();
    }
}



 //java -cp ".;sqlite-jdbc-3.49.0.0.jar" FarmerApp
