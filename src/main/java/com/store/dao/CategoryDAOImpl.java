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

    // Bảng categories thực tế chỉ có 2 cột: category_id, name
    private static final String SELECT_BASE = "SELECT category_id, name FROM categories ";

    @Override
    public List<Category> getAllCategories() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY category_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
            return categories;
        }
    }

    @Override
    public Category getCategoryById(int categoryId) throws SQLException {
        String sql = SELECT_BASE + "WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapCategory(rs) : null;
            }
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("category_id"));
        c.setName(rs.getString("name"));
        return c;
    }
}

