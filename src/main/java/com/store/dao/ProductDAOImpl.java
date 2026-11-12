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

    /** Câu SELECT dùng chung cho các truy vấn sản phẩm (kèm tên danh mục). */
    private static final String SELECT_BASE =
            "SELECT p.product_id, p.name, p.price, p.quantity, p.category_id, p.status, "
                    + "p.import_price, c.name AS category_name "
                    + "FROM products p LEFT JOIN categories c ON p.category_id = c.category_id ";

    // ------------------------------------------------------------------
    // Truy vấn danh sách
    // ------------------------------------------------------------------
    @Override
    public List<Product> getAllActiveProducts() throws SQLException {
        String sql = SELECT_BASE + "WHERE p.status = TRUE ORDER BY p.product_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapProducts(rs);
        }
    }

    @Override
    public List<Product> searchProducts(String keyword) throws SQLException {
        String sql = SELECT_BASE
                + "WHERE p.status = TRUE AND (p.name LIKE ? OR c.name LIKE ?) "
                + "ORDER BY p.product_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                return mapProducts(rs);
            }
        }
    }

    @Override
    public Product getProductById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return getProductById(conn, id);
        }
    }

    @Override
    public Product getProductById(Connection conn, int id) throws SQLException {
        return queryOne(conn, id, false);
    }

    /**
     * Đọc sản phẩm kèm khóa dòng (SELECT ... FOR UPDATE) bên trong transaction,
     * đảm bảo không hai giao dịch nào cùng kiểm tra/trừ một dòng tồn kho (chống oversell).
     */
    @Override
    public Product getProductByIdForUpdate(Connection conn, int id) throws SQLException {
        return queryOne(conn, id, true);
    }

    private Product queryOne(Connection conn, int id, boolean forUpdate) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.product_id = ?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapProduct(rs) : null;
            }
        }
    }


    @Override
    public boolean addProduct(Product product) throws SQLException {
        // Bao gom import_price (gia nhap = 70% gia ban) de tranh loi "doesn't have a default value"
        String sql = "INSERT INTO products (name, price, quantity, category_id, status, import_price) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            setCategoryId(ps, 4, product.getCategoryId());
            ps.setBoolean(5, product.isStatus());
            // import_price mac dinh = 70% gia ban (co the sua sau)
            ps.setDouble(6, product.getPrice() * 0.7);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ?, quantity = ?, "
                + "category_id = ?, import_price = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            setCategoryId(ps, 4, product.getCategoryId());
            // Giữ quy ước giá nhập = 70% giá bán để dữ liệu không lệch khi đổi giá
            ps.setDouble(5, product.getPrice() * 0.7);
            ps.setInt(6, product.getId());
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

    /**
     * Trừ tồn kho nguyên tử: chỉ trừ khi kho còn đủ hàng.
     *
     * @return {@code false} nếu không còn đủ tồn kho (bị giao dịch khác lấy mất)
     */
    @Override
    public boolean decreaseStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    /** Gán category_id, cho phép NULL khi sản phẩm chưa được phân loại. */
    private void setCategoryId(PreparedStatement ps, int index, int categoryId) throws SQLException {
        if (categoryId > 0) {
            ps.setInt(index, categoryId);
        } else {
            ps.setNull(index, java.sql.Types.INTEGER);
        }
    }

    // ------------------------------------------------------------------
    // Mapping ResultSet -> Product
    // ------------------------------------------------------------------
    private List<Product> mapProducts(ResultSet rs) throws SQLException {
        List<Product> products = new ArrayList<>();
        while (rs.next()) {
            products.add(mapProduct(rs));
        }
        return products;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getDouble("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setStatus(rs.getBoolean("status"));
        p.setImportPrice(rs.getDouble("import_price"));
        return p;
    }
}
