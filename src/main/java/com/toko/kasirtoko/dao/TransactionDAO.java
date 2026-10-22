package com.toko.kasirtoko.dao;

import com.toko.kasirtoko.config.DatabaseConfig;
import com.toko.kasirtoko.model.CartItem;
import com.toko.kasirtoko.model.Transaction;

import java.sql.*;
import java.util.List;

public class TransactionDAO {

    public boolean saveTransaction(Transaction trx, List<CartItem> cartItems) throws SQLException {
        String insertTrxSql = "INSERT INTO transactions (no_faktur, user_id, subtotal, diskon_transaksi, pajak_ppn, grand_total, metode_bayar, nominal_bayar, kembalian) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertDetailSql = "INSERT INTO transaction_details (transaction_id, product_id, kuantitas, harga_modal_saat_ini, harga_jual_saat_ini, diskon_item, subtotal_item) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET stok = stok - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            // Matikan auto-commit untuk memulai transaksi yang aman (ACID)
            conn.setAutoCommit(false);

            long generatedTrxId;

            // 1. Simpan tabel utama (transactions)
            try (PreparedStatement stmtTrx = conn.prepareStatement(insertTrxSql, Statement.RETURN_GENERATED_KEYS)) {
                stmtTrx.setString(1, trx.getNoFaktur());
                stmtTrx.setInt(2, trx.getUserId());
                stmtTrx.setBigDecimal(3, trx.getSubtotal());
                stmtTrx.setBigDecimal(4, trx.getDiskonTransaksi());
                stmtTrx.setBigDecimal(5, trx.getPajakPpn());
                stmtTrx.setBigDecimal(6, trx.getGrandTotal());
                stmtTrx.setString(7, trx.getMetodeBayar());
                stmtTrx.setBigDecimal(8, trx.getNominalBayar());
                stmtTrx.setBigDecimal(9, trx.getKembalian());
                stmtTrx.executeUpdate();

                try (ResultSet keys = stmtTrx.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedTrxId = keys.getLong(1);
                    } else {
                        throw new SQLException("Gagal mendapatkan ID transaksi dari database.");
                    }
                }
            }

            // 2. Simpan detail produk dan kurangi stok (Batch Processing agar cepat)
            try (PreparedStatement stmtDetail = conn.prepareStatement(insertDetailSql);
                 PreparedStatement stmtStock = conn.prepareStatement(updateStockSql)) {

                for (CartItem item : cartItems) {
                    // Siapkan antrean insert detail
                    stmtDetail.setLong(1, generatedTrxId);
                    stmtDetail.setInt(2, item.getProductId());
                    stmtDetail.setInt(3, item.getKuantitas());
                    stmtDetail.setBigDecimal(4, item.getHargaModal());
                    stmtDetail.setBigDecimal(5, item.getHargaSatuan());
                    stmtDetail.setBigDecimal(6, item.getDiskon());
                    stmtDetail.setBigDecimal(7, item.getSubtotal());
                    stmtDetail.addBatch();

                    // Siapkan antrean update stok
                    stmtStock.setInt(1, item.getKuantitas());
                    stmtStock.setInt(2, item.getProductId());
                    stmtStock.addBatch();
                }

                // Eksekusi antrean query sekaligus
                stmtDetail.executeBatch();
                stmtStock.executeBatch();
            }

            // Jika semua lancar, simpan permanen
            conn.commit();
            return true;

        } catch (SQLException ex) {
            // Jika ada error di tengah jalan, batalkan semua perubahan
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex; // Lempar kembali error ke Controller untuk ditampilkan ke UI

        } finally {
            // Kembalikan ke mode normal
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }
}