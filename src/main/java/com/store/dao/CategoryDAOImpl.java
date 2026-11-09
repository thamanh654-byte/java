package com.store.dao;

import com.store.config.DatabaseConnection;
import com.store.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, name, description FROM categories ORDER BY category_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                categories.add(c);
            }
        }
        return categories;
    }

    @Override
    public Category getCategoryById(int categoryId) throws SQLException {
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getInt("category_id"));
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    return c;
                }
            }
        }
        return null;
    }
}

