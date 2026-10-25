package com.toko.kasirtoko.controller;

import com.toko.kasirtoko.config.DatabaseConfig;
import com.toko.kasirtoko.dao.TransactionDAO;
import com.toko.kasirtoko.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoryController {

    @FXML private TableView<Transaction> historyTable;
    @FXML private TableColumn<Transaction, String> colFaktur;
    @FXML private TableColumn<Transaction, String> colMetode;
    @FXML private TableColumn<Transaction, BigDecimal> colSubtotal;
    @FXML private TableColumn<Transaction, BigDecimal> colDiskon;
    @FXML private TableColumn<Transaction, BigDecimal> colPpn;
    @FXML private TableColumn<Transaction, BigDecimal> colGrandTotal;

    @FXML private Label totalBarangLabel;
    @FXML private Label totalPendapatanLabel;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    @FXML
    public void initialize() {
        setupTable();
        loadHistoryData();
        loadTotalItemsSold();
    }

    private void setupTable() {
        colFaktur.setCellValueFactory(new PropertyValueFactory<>("noFaktur"));
        colMetode.setCellValueFactory(new PropertyValueFactory<>("metodeBayar"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colDiskon.setCellValueFactory(new PropertyValueFactory<>("diskonTransaksi"));
        colPpn.setCellValueFactory(new PropertyValueFactory<>("pajakPpn"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));

        historyTable.setItems(transactionList);
    }

    private void loadHistoryData() {
        transactionList.clear();
        BigDecimal totalPendapatan = BigDecimal.ZERO;

        try {
            List<Transaction> histories = transactionDAO.getAllTransactions();
            transactionList.addAll(histories);

            for (Transaction t : histories) {
                totalPendapatan = totalPendapatan.add(t.getGrandTotal());
            }
            totalPendapatanLabel.setText(rupiahFormat.format(totalPendapatan));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat riwayat transaksi: " + e.getMessage());
        }
    }

    private void loadTotalItemsSold() {
        String sql = "SELECT COALESCE(SUM(kuantitas), 0) AS total_qty FROM transaction_details";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int totalQty = rs.getInt("total_qty");
                totalBarangLabel.setText(totalQty + " Item / Pcs");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // 1. EKSPOR TRANSAKSI (BARANG DIGABUNG DALAM SATU KOLOM BERDASARKAN FAKTUR)
    // =========================================================================
    @FXML
    private void handleExportDetailTransaksi() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan Penjualan (Barang Digabung)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Excel CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("Laporan_Penjualan_Faktur_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());
        if (file == null) return;

        // SQL DIPERBARUI: Menggunakan GROUP_CONCAT untuk menyatukan nama barang
        String sql = """
            SELECT t.no_faktur, 
                   GROUP_CONCAT(p.nama_produk SEPARATOR ', ') AS daftar_barang,
                   SUM(td.kuantitas) AS total_qty,
                   t.subtotal AS total_belanja, 
                   t.metode_bayar
            FROM transaction_details td
            JOIN transactions t ON td.transaction_id = t.id
            JOIN products p ON td.product_id = p.id
            GROUP BY t.id, t.no_faktur, t.subtotal, t.metode_bayar
            ORDER BY t.id DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("sep=;");

            // Header Tabel Excel Disesuaikan (Kolom disederhanakan karena barang digabung)
            writer.println("No Faktur;Daftar Barang;Total Qty Barang;Subtotal Belanja;Metode Bayar");

            int totalQtyTerjual = 0;
            BigDecimal totalOmzet = BigDecimal.ZERO;

            while (rs.next()) {
                int qty = rs.getInt("total_qty");
                BigDecimal totalBelanja = rs.getBigDecimal("total_belanja");

                totalQtyTerjual += qty;
                totalOmzet = totalOmzet.add(totalBelanja);

                writer.printf("%s;%s;%d;%s;%s\n",
                        rs.getString("no_faktur"),
                        rs.getString("daftar_barang"),
                        qty,
                        totalBelanja.toPlainString(),
                        rs.getString("metode_bayar")
                );
            }

            // Tambahkan baris ringkasan total di bagian paling bawah Excel
            writer.println(";;;;");
            writer.printf("TOTAL KESELURUHAN;;%d;%s;\n", totalQtyTerjual, totalOmzet.toPlainString());

            showAlert(Alert.AlertType.INFORMATION, "Ekspor Berhasil", "Laporan berhasil disimpan di:\n" + file.getAbsolutePath());

        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ekspor Gagal", "Terjadi kesalahan saat mengekspor data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // 2. EKSPOR LAPORAN DATA STOK BARANG GUDANG KE EXCEL (CSV)
    // =========================================================================
    @FXML
    private void handleExportStokBarang() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan Data Stok Barang");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Excel CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("Laporan_Stok_Barang_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());
        if (file == null) return;

        String sql = """
            SELECT p.id, COALESCE(c.nama_kategori, 'Tanpa Kategori') AS kategori,
                   p.nama_produk, p.satuan, p.harga_beli, p.harga_jual, p.stok, p.stok_minimum,
                   COALESCE(SUM(td.kuantitas), 0) AS total_terjual
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN transaction_details td ON p.id = td.product_id
            GROUP BY p.id, c.nama_kategori, p.nama_produk, p.satuan, p.harga_beli, p.harga_jual, p.stok, p.stok_minimum
            ORDER BY p.nama_produk ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("sep=;");
            writer.println("ID Barang;Kategori;Nama Barang;Satuan;Harga Beli (Modal);Harga Jual;Sisa Stok;Stok Minimum;Total Terjual;Status Stok");

            int totalStokTersedia = 0;

            while (rs.next()) {
                int stok = rs.getInt("stok");
                int stokMin = rs.getInt("stok_minimum");
                String status = (stok <= stokMin) ? "STOK MENIPIS" : "AMAN";
                totalStokTersedia += stok;

                writer.printf("%d;%s;%s;%s;%s;%s;%d;%d;%d;%s\n",
                        rs.getInt("id"),
                        rs.getString("kategori"),
                        rs.getString("nama_produk"),
                        rs.getString("satuan"),
                        rs.getBigDecimal("harga_beli").toPlainString(),
                        rs.getBigDecimal("harga_jual").toPlainString(),
                        stok,
                        stokMin,
                        rs.getInt("total_terjual"),
                        status
                );
            }

            writer.println(";;;;;;;;;");
            writer.printf("TOTAL SISA STOK BARANG;;;;;;%d;;;\n", totalStokTersedia);

            showAlert(Alert.AlertType.INFORMATION, "Ekspor Berhasil", "Laporan Data Stok Barang berhasil disimpan di:\n" + file.getAbsolutePath());

        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ekspor Gagal", "Terjadi kesalahan saat mengekspor stok: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}