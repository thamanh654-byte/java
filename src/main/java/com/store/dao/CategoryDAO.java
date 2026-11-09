package com.store.dao;

import com.store.model.Category;
import java.sql.SQLException;
import java.util.List;

public interface CategoryDAO {
    List<Category> getAllCategories() throws SQLException;
    Category getCategoryById(int categoryId) throws SQLException;
}

