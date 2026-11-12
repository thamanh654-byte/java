package com.store.service;

import com.store.dao.CategoryDAO;
import com.store.dao.CategoryDAOImpl;
import com.store.dao.ProductDAO;
import com.store.dao.ProductDAOImpl;
import com.store.exception.InvalidInputException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Product;
import com.store.util.MoneyUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Lớp Service xử lý toàn bộ logic nghiệp vụ liên quan đến sản phẩm
 */
public class ProductService {
    /** Mã lỗi MySQL khi vi phạm khóa ngoại (category_id không tồn tại). */
    private static final int MYSQL_ERR_NO_REFERENCED_ROW_2 = 1452;

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;

    public ProductService() {
        this(new ProductDAOImpl(), new CategoryDAOImpl());
    }

    /** Constructor cho phép truyền DAO vào (dễ viết test / thay thế implementation). */
    public ProductService(ProductDAO productDAO, CategoryDAO categoryDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
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

        Product product = new Product(0, name.trim(), MoneyUtils.round(price), quantity, categoryId, true);
        try {
            if (!productDAO.addProduct(product)) {
                throw new SQLException("Không thể thêm sản phẩm vào cơ sở dữ liệu!");
            }
        } catch (SQLException e) {
            throw translateCategoryError(e, categoryId);
        }
    }

    public void updateProduct(int id, String name, double price, int quantity, int categoryId)
            throws InvalidInputException, ProductNotFoundException, SQLException {
        // Kiểm tra tồn tại
        getProductById(id);
        validateProductInput(name, price, quantity, categoryId);

        Product product = new Product(id, name.trim(), MoneyUtils.round(price), quantity, categoryId, true);
        try {
            if (!productDAO.updateProduct(product)) {
                throw new ProductNotFoundException("Không tìm thấy sản phẩm có ID: " + id);
            }
        } catch (SQLException e) {
            throw translateCategoryError(e, categoryId);
        }
    }

    public void softDeleteProduct(int id) throws ProductNotFoundException, SQLException {
        // Kiểm tra tồn tại trước khi xóa mềm
        getProductById(id);
        productDAO.softDeleteProduct(id);
    }

    /**
     * Kiểm tra dữ liệu đầu vào của sản phẩm.
     * Cho phép categoryId = 0 (chưa phân loại); nếu &gt; 0 thì danh mục phải tồn tại.
     */
    private void validateProductInput(String name, double price, int quantity, int categoryId)
            throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Tên sản phẩm không được để trống!");
        }
        if (name.trim().length() > 150) {
            throw new InvalidInputException("Tên sản phẩm không được vượt quá 150 ký tự!");
        }
        if (price < 0) {
            throw new InvalidInputException("Đơn giá không được là số âm!");
        }
        if (quantity < 0) {
            throw new InvalidInputException("Số lượng tồn kho không được âm!");
        }
        if (categoryId < 0) {
            throw new InvalidInputException("Mã danh mục không hợp lệ (phải >= 0)!");
        }
    }

    /** Đổi lỗi khóa ngoại thành thông điệp dễ hiểu cho người dùng. */
    private SQLException translateCategoryError(SQLException e, int categoryId) throws SQLException {
        if (e.getErrorCode() == MYSQL_ERR_NO_REFERENCED_ROW_2) {
            return new SQLException("Danh mục có mã " + categoryId + " không tồn tại. "
                    + "Vui lòng chọn danh mục khác!", e.getSQLState(), e.getErrorCode(), e);
        }
        return e;
    }
}

