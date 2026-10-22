package com.toko.kasirtoko.controller;

import com.toko.kasirtoko.dao.ProductDAO;
import com.toko.kasirtoko.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductController {

    @FXML private TextField categoryIdField;
    @FXML private TextField namaProdukField;
    @FXML private TextField satuanField;
    @FXML private TextField hargaBeliField;
    @FXML private TextField hargaJualField;
    @FXML private TextField stokField;
    @FXML private TextField stokMinField;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colNama;
    @FXML private TableColumn<Product, String> colSatuan;
    @FXML private TableColumn<Product, BigDecimal> colHargaBeli;
    @FXML private TableColumn<Product, BigDecimal> colHargaJual;
    @FXML private TableColumn<Product, Integer> colStok;

    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct = null;

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        // Listener untuk mengisi form otomatis saat baris tabel diklik
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
                populateForm(newSelection);
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colHargaBeli.setCellValueFactory(new PropertyValueFactory<>("hargaBeli"));
        colHargaJual.setCellValueFactory(new PropertyValueFactory<>("hargaJual"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        productTable.setItems(productList);
    }

    private void loadData() {
        productList.clear();
        try {
            List<Product> products = productDAO.getAllProducts(); // Memuat semua produk
            productList.addAll(products);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    private void populateForm(Product p) {
        categoryIdField.setText(String.valueOf(p.getCategoryId()));
        namaProdukField.setText(p.getNamaProduk());
        satuanField.setText(p.getSatuan());
        hargaBeliField.setText(p.getHargaBeli().toString());
        hargaJualField.setText(p.getHargaJual().toString());
        stokField.setText(String.valueOf(p.getStok()));
        stokMinField.setText(String.valueOf(p.getStokMinimum()));
    }

    @FXML
    private void handleAdd() {
        try {
            Product p = readFormInput(0); // 0 karena ID digenerate otomatis oleh MySQL
            if (productDAO.addProduct(p)) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Produk berhasil ditambahkan!");
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Input Gagal", "Pastikan semua format angka diisi dengan benar.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Produk", "Pilih produk dari tabel terlebih dahulu!");
            return;
        }
        try {
            Product p = readFormInput(selectedProduct.getId());
            if (productDAO.updateProduct(p)) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data produk berhasil diperbarui!");
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Update Gagal", "Pastikan semua format angka benar.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Produk", "Pilih produk yang ingin dihapus!");
            return;
        }
        try {
            if (productDAO.deleteProduct(selectedProduct.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Produk berhasil dihapus!");
                clearForm();
                loadData();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Hapus Gagal", "Produk tidak bisa dihapus karena sudah tercatat di riwayat transaksi.");
        }
    }

    @FXML
    private void clearForm() {
        selectedProduct = null;
        categoryIdField.clear();
        namaProdukField.clear();
        satuanField.clear();
        hargaBeliField.clear();
        hargaJualField.clear();
        stokField.clear();
        stokMinField.clear();
        productTable.getSelectionModel().clearSelection();
    }

    private Product readFormInput(int id) {
        int catId = Integer.parseInt(categoryIdField.getText());
        String nama = namaProdukField.getText();
        String satuan = satuanField.getText();
        BigDecimal hBeli = new BigDecimal(hargaBeliField.getText());
        BigDecimal hJual = new BigDecimal(hargaJualField.getText());
        int stok = Integer.parseInt(stokField.getText());
        int stokMin = Integer.parseInt(stokMinField.getText());
        return new Product(id, catId, nama, satuan, hBeli, hJual, stok, stokMin);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}