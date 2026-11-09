package com.store.dao;

import com.store.config.DatabaseConnection;
import com.store.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<Product> getAllActiveProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.price, p.quantity, p.category_id, p.status, c.name AS category_name " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.status = TRUE ORDER BY p.product_id ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));
                p.setStatus(rs.getBoolean("status"));
                products.add(p);
            }
        }
        return products;
    }

    @Override
    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.price, p.quantity, p.category_id, p.status, c.name AS category_name " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.status = TRUE AND (p.name LIKE ? OR c.name LIKE ?) " +
                     "ORDER BY p.product_id ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setCategoryName(rs.getString("category_name"));
                    p.setStatus(rs.getBoolean("status"));
                    products.add(p);
                }
            }
        }
        return products;
    }

    @Override
    public Product getProductById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return getProductById(conn, id);
        }
    }

    @Override
    public Product getProductById(Connection conn, int id) throws SQLException {
        String sql = "SELECT p.product_id, p.name, p.price, p.quantity, p.category_id, p.status, c.name AS category_name " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setCategoryName(rs.getString("category_name"));
                    p.setStatus(rs.getBoolean("status"));
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public boolean addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, price, quantity, category_id, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.setInt(4, product.getCategoryId());
            ps.setBoolean(5, true);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ?, quantity = ?, category_id = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.setInt(4, product.getCategoryId());
            ps.setInt(5, product.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDeleteProduct(int id) throws SQLException {
        String sql = "UPDATE products SET status = FALSE WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void decreaseStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }
}
