package com.store.dao;

import com.store.model.Product;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Giao diện DAO quản lý các thao tác CRUD và cập nhật kho cho Product
 */
public interface ProductDAO {
    List<Product> getAllActiveProducts() throws SQLException;
    Product getProductById(int id) throws SQLException;
    Product getProductById(Connection conn, int id) throws SQLException;
    boolean addProduct(Product product) throws SQLException;
    boolean updateProduct(Product product) throws SQLException;
    boolean softDeleteProduct(int id) throws SQLException;
    void decreaseStock(Connection conn, int productId, int quantity) throws SQLException;
}

