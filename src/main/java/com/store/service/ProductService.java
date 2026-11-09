package com.store.service;

import com.store.dao.ProductDAO;
import com.store.dao.ProductDAOImpl;
import com.store.exception.InvalidInputException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * Lớp Service xử lý toàn bộ logic nghiệp vụ liên quan đến sản phẩm
 */
public class ProductService {
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAOImpl();
    }

    public List<Product> listAllProducts() throws SQLException {
        return productDAO.getAllActiveProducts();
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAllProducts();
        }
        return productDAO.searchProducts(keyword);
    }

    public Product getProductById(int id) throws SQLException, ProductNotFoundException {
        Product p = productDAO.getProductById(id);
        if (p == null || !p.isStatus()) {
            throw new ProductNotFoundException("Không tìm thấy sản phẩm có ID: " + id);
        }
        return p;
    }

    public void addProduct(String name, double price, int quantity, int categoryId) 
            throws InvalidInputException, SQLException {
        validateProductInput(name, price, quantity, categoryId);
        Product product = new Product(0, name.trim(), price, quantity, categoryId, true);
        productDAO.addProduct(product);
    }

    public void updateProduct(int id, String name, double price, int quantity, int categoryId) 
            throws InvalidInputException, ProductNotFoundException, SQLException {
        // Kiểm tra tồn tại
        getProductById(id);
        validateProductInput(name, price, quantity, categoryId);
        Product product = new Product(id, name.trim(), price, quantity, categoryId, true);
        productDAO.updateProduct(product);
    }

    public void softDeleteProduct(int id) throws ProductNotFoundException, SQLException {
        // Kiểm tra tồn tại trước khi xóa mềm
        getProductById(id);
        productDAO.softDeleteProduct(id);
    }

    private void validateProductInput(String name, double price, int quantity, int categoryId) 
            throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Tên sản phẩm không được để trống!");
        }
        if (price < 0) {
            throw new InvalidInputException("Đơn giá không được là số âm!");
        }
        if (quantity < 0) {
            throw new InvalidInputException("Số lượng tồn kho không được âm!");
        }
        if (categoryId <= 0) {
            throw new InvalidInputException("Mã danh mục không hợp lệ (phải > 0)!");
        }
    }
}
