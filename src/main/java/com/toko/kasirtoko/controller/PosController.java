package com.toko.kasirtoko.controller;

import com.toko.kasirtoko.dao.ProductDAO;
import com.toko.kasirtoko.dao.TransactionDAO;
import com.toko.kasirtoko.model.CartItem;
import com.toko.kasirtoko.model.Product;
import com.toko.kasirtoko.model.Transaction;
import com.toko.kasirtoko.model.User;
import com.toko.kasirtoko.util.ReceiptPrinter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PosController {

    @FXML private Label cashierInfoLabel;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private Spinner<Integer> qtySpinner;
    @FXML private TextField itemDiscountField;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colProductName;
    @FXML private TableColumn<CartItem, BigDecimal> colPrice;
    @FXML private TableColumn<CartItem, Integer> colQty;
    @FXML private TableColumn<CartItem, BigDecimal> colDiscount;
    @FXML private TableColumn<CartItem, BigDecimal> colSubtotal;

    @FXML private Label subtotalLabel;
    @FXML private TextField trxDiscountField;
    @FXML private CheckBox taxCheckBox;
    @FXML private Label taxAmountLabel;
    @FXML private Label grandTotalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField cashReceivedField;
    @FXML private Label changeLabel;

    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    // Peringatan "Locale is deprecated" sudah diatasi dengan penulisan Locale.of() untuk Java versi baru
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
    private User activeUser;

    private BigDecimal currentSubtotal = BigDecimal.ZERO;
    private BigDecimal currentGrandTotal = BigDecimal.ZERO;

    // === INISIALISASI DAO DAN PRINTER ===
    private final ProductDAO productDAO = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();

    public void setActiveUser(User user) {
        this.activeUser = user;
        if (cashierInfoLabel != null && user != null) {
            cashierInfoLabel.setText("Kasir: " + user.getNamaLengkap() + " (" + user.getRole() + ")");
        }
    }

    @FXML
    public void initialize() {
        initTableColumns();
        initSpinnersAndCombos();
        loadProductsFromDatabase();

        // Listener kalkulasi otomatis saat opsi diskon/pajak/uang diubah
        trxDiscountField.textProperty().addListener((obs, oldV, newV) -> calculateTotals());
        taxCheckBox.selectedProperty().addListener((obs, oldV, newV) -> calculateTotals());
        cashReceivedField.textProperty().addListener((obs, oldV, newV) -> calculateChange());
    }

    private void initTableColumns() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("hargaSatuan"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("kuantitas"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("diskon"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        cartTable.setItems(cartList);
    }

    private void initSpinnersAndCombos() {
        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("TUNAI", "TRANSFER", "QRIS"));
        paymentMethodComboBox.getSelectionModel().selectFirst();

        productComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product p) {
                return (p == null) ? "" : p.getNamaProduk() + " - Stok: " + p.getStok() + " (Rp " + p.getHargaJual().intValue() + ")";
            }
            @Override
            public Product fromString(String string) { return null; }
        });
    }

    private void loadProductsFromDatabase() {
        productList.clear();
        try {
            List<Product> availableProducts = productDAO.getAvailableProducts();
            productList.addAll(availableProducts);
            productComboBox.setItems(productList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat katalog produk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = productComboBox.getValue();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih produk terlebih dahulu!");
            return;
        }

        int qty = qtySpinner.getValue();
        if (qty > selectedProduct.getStok()) {
            showAlert(Alert.AlertType.WARNING, "Stok Kurang", "Stok hanya tersedia: " + selectedProduct.getStok());
            return;
        }

        BigDecimal discount = BigDecimal.ZERO;
        try {
            if (!itemDiscountField.getText().isBlank()) {
                discount = new BigDecimal(itemDiscountField.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Format Salah", "Diskon harus berupa nominal angka valid.");
            return;
        }

        boolean exists = false;
        for (CartItem item : cartList) {
            if (item.getProductId() == selectedProduct.getId()) {
                int newQty = item.getKuantitas() + qty;
                if (newQty > selectedProduct.getStok()) {
                    showAlert(Alert.AlertType.WARNING, "Stok Tidak Cukup", "Total kuantitas melebihi stok yang ada!");
                    return;
                }
                item.setKuantitas(newQty);
                item.setDiskon(item.getDiskon().add(discount));
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartList.add(new CartItem(
                    selectedProduct.getId(),
                    selectedProduct.getNamaProduk(),
                    selectedProduct.getHargaBeli(),
                    selectedProduct.getHargaJual(),
                    qty,
                    discount
            ));
        }

        cartTable.refresh();
        calculateTotals();
        itemDiscountField.clear();
        qtySpinner.getValueFactory().setValue(1);
    }

    @FXML
    private void handleRemoveFromCart() {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartList.remove(selectedItem);
            calculateTotals();
        } else {
            showAlert(Alert.AlertType.WARNING, "Pilih Item", "Pilih baris pada tabel keranjang yang ingin dihapus.");
        }
    }

    private void calculateTotals() {
        currentSubtotal = BigDecimal.ZERO;
        for (CartItem item : cartList) {
            currentSubtotal = currentSubtotal.add(item.getSubtotal());
        }
        subtotalLabel.setText(rupiahFormat.format(currentSubtotal));

        BigDecimal trxDiscount = BigDecimal.ZERO;
        try {
            if (!trxDiscountField.getText().isBlank()) {
                trxDiscount = new BigDecimal(trxDiscountField.getText().trim());
            }
        } catch (NumberFormatException ignored) {}

        BigDecimal taxableAmount = currentSubtotal.subtract(trxDiscount);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) taxableAmount = BigDecimal.ZERO;

        BigDecimal taxAmount = BigDecimal.ZERO;
        if (taxCheckBox.isSelected()) {
            taxAmount = taxableAmount.multiply(new BigDecimal("0.11")).setScale(2, RoundingMode.HALF_UP);
        }
        taxAmountLabel.setText(rupiahFormat.format(taxAmount));

        currentGrandTotal = taxableAmount.add(taxAmount);
        grandTotalLabel.setText(rupiahFormat.format(currentGrandTotal));

        calculateChange();
    }

    private void calculateChange() {
        BigDecimal cash = BigDecimal.ZERO;
        try {
            if (!cashReceivedField.getText().isBlank()) {
                cash = new BigDecimal(cashReceivedField.getText().trim());
            }
        } catch (NumberFormatException ignored) {}

        BigDecimal change = cash.subtract(currentGrandTotal);
        if (change.compareTo(BigDecimal.ZERO) < 0) {
            changeLabel.setText("Kurang: " + rupiahFormat.format(change.abs()));
        } else {
            changeLabel.setText(rupiahFormat.format(change));
        }
    }

    // ==========================================
    // FITUR MEMBUKA HALAMAN MANAJEMEN PRODUK
    // ==========================================
    @FXML
    private void handleOpenProductManagement() {
        // Batasi akses hanya untuk Admin (opsional)
        if (activeUser != null && !"Admin".equalsIgnoreCase(activeUser.getRole())) {
            showAlert(Alert.AlertType.WARNING, "Akses Ditolak", "Hanya Admin yang dapat mengakses Manajemen Produk.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/toko/kasirtoko/product-view.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Manajemen Produk - Sistem Kasir");
            stage.setScene(new javafx.scene.Scene(root));

            // Mengatur jendela ini sebagai modal (tidak bisa klik kasir sebelum jendela ini ditutup)
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Muat ulang daftar barang di kasir setelah jendela ditutup
            loadProductsFromDatabase();

        } catch (java.io.IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman Manajemen Produk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // FITUR MEMBUKA HALAMAN LAPORAN TRANSAKSI
    // ==========================================
    @FXML
    private void handleOpenHistory() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/toko/kasirtoko/history-view.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Laporan & Riwayat Transaksi - Sistem Kasir");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka Laporan Transaksi: " + e.getMessage());
        }
    }

    // ==========================================
    // FITUR PROSES PEMBAYARAN & SIMPAN
    // ==========================================
    @FXML
    private void handleProcessPayment() {
        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang Kosong", "Belum ada produk di dalam keranjang.");
            return;
        }

        BigDecimal cashReceived;
        try {
            cashReceived = new BigDecimal(cashReceivedField.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Validasi Pembayaran", "Masukkan nominal uang yang diterima.");
            return;
        }

        String paymentMethod = paymentMethodComboBox.getValue();
        if ("TUNAI".equals(paymentMethod) && cashReceived.compareTo(currentGrandTotal) < 0) {
            showAlert(Alert.AlertType.WARNING, "Uang Kurang", "Nominal pembayaran belum mencukupi grand total!");
            return;
        }

        // Siapkan data Model Transaksi
        String noFaktur = "INV-" + System.currentTimeMillis();
        BigDecimal diskonTrx = new BigDecimal(trxDiscountField.getText().isBlank() ? "0" : trxDiscountField.getText().trim());
        BigDecimal pajak = taxCheckBox.isSelected() ? currentSubtotal.subtract(diskonTrx).multiply(new BigDecimal("0.11")) : BigDecimal.ZERO;
        BigDecimal kembalian = cashReceived.subtract(currentGrandTotal).max(BigDecimal.ZERO);
        int userId = (activeUser != null) ? activeUser.getId() : 1;

        Transaction trx = new Transaction(noFaktur, userId, currentSubtotal, diskonTrx, pajak, currentGrandTotal, paymentMethod, cashReceived, kembalian);

        try {
            // === SIMPAN MENGGUNAKAN TransactionDAO ===
            boolean success = transactionDAO.saveTransaction(trx, cartList);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Transaksi Berhasil", "Faktur: " + noFaktur + "\nTransaksi telah berhasil disimpan!");

                // === CETAK STRUK ===
                String namaKasir = (activeUser != null) ? activeUser.getNamaLengkap() : "Admin";
                receiptPrinter.printReceipt(trx, cartList, namaKasir);

                // Bersihkan antarmuka
                cartList.clear();
                cashReceivedField.clear();
                trxDiscountField.setText("0");
                taxCheckBox.setSelected(false);
                calculateTotals();

                // Muat ulang data agar stok terbaru tampil di ComboBox
                loadProductsFromDatabase();
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Transaksi Gagal", "Terjadi kesalahan di database: " + ex.getMessage());
            ex.printStackTrace();
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