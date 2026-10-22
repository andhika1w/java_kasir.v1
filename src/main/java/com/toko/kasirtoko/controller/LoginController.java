package com.toko.kasirtoko.controller;

// Tambahkan import UserDAO
import com.toko.kasirtoko.dao.UserDAO;
import com.toko.kasirtoko.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

// Import BCrypt dari at.favre.lib
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // 1. Inisialisasi UserDAO untuk mengambil data dari database
    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            tampilkanError("Username dan password tidak boleh kosong!");
            return;
        }

        try {
            // 2. Ambil data user menggunakan DAO (Logika SQL sudah disembunyikan di UserDAO)
            User user = userDAO.getUserByUsername(username);

            // Jika user ditemukan di database
            if (user != null) {
                String hashDb = user.getPasswordHash();

                // 3. Logika verifikasi password menggunakan BCrypt
                if (BCrypt.verifyer().verify(password.toCharArray(), hashDb).verified) {
                    errorLabel.setVisible(false);

                    // Pindah ke layar POS Dashboard dengan membawa data user
                    bukaDashboardKasir(user);
                    return;
                }
            }
            // Jika user null (tidak ada) ATAU password salah
            tampilkanError("Username atau password salah!");

        } catch (SQLException | IOException e) {
            tampilkanError("Terjadi kegagalan sistem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bukaDashboardKasir(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/toko/kasirtoko/pos-view.fxml"));
        Parent root = loader.load();

        // Kirim data kasir yang aktif ke PosController
        PosController posController = loader.getController();
        posController.setActiveUser(user);

        Stage stage = new Stage();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        stage.setTitle("Kasir Toko - Point of Sale");
        stage.setScene(scene);
        stage.show();

        // Tutup jendela login saat ini
        Stage currentStage = (Stage) usernameField.getScene().getWindow();
        currentStage.close();
    }

    private void tampilkanError(String pesan) {
        errorLabel.setText(pesan);
        errorLabel.setVisible(true);
    }
}