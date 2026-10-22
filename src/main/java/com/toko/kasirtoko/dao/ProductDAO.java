package com.toko.kasirtoko.dao;

import com.toko.kasirtoko.config.DatabaseConfig;
import com.toko.kasirtoko.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Mengambil daftar produk yang stoknya masih ada (digunakan di halaman POS/Kasir)
    public List<Product> getAvailableProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stok > 0 ORDER BY nama_produk ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    // Mengambil daftar seluruh produk tanpa filter stok (digunakan di halaman Inventaris)
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY nama_produk ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    // Mengambil peringatan stok tipis (Low Stock Alert)
    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stok <= stok_minimum ORDER BY stok ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    // Method pembantu (helper) agar tidak menulis ulang pemetaan kolom database berulang kali
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getInt("category_id"),
                rs.getString("nama_produk"),
                rs.getString("satuan"),
                rs.getBigDecimal("harga_beli"),
                rs.getBigDecimal("harga_jual"),
                rs.getInt("stok"),
                rs.getInt("stok_minimum")
        );
    }
    // --- TAMBAHAN UNTUK FITUR CRUD MANAJEMEN PRODUK ---

    public boolean addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (category_id, nama_produk, satuan, harga_beli, harga_jual, stok, stok_minimum) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getNamaProduk());
            stmt.setString(3, product.getSatuan());
            stmt.setBigDecimal(4, product.getHargaBeli());
            stmt.setBigDecimal(5, product.getHargaJual());
            stmt.setInt(6, product.getStok());
            stmt.setInt(7, product.getStokMinimum());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id=?, nama_produk=?, satuan=?, harga_beli=?, harga_jual=?, stok=?, stok_minimum=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getNamaProduk());
            stmt.setString(3, product.getSatuan());
            stmt.setBigDecimal(4, product.getHargaBeli());
            stmt.setBigDecimal(5, product.getHargaJual());
            stmt.setInt(6, product.getStok());
            stmt.setInt(7, product.getStokMinimum());
            stmt.setInt(8, product.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
}