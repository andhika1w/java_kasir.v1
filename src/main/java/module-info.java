module com.toko.kasirtoko {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires bcrypt;
    requires itextpdf;

    // Membuka akses refleksi runtime JavaFX
    opens com.toko.kasirtoko to javafx.fxml;
    opens com.toko.kasirtoko.controller to javafx.fxml;
    opens com.toko.kasirtoko.model to javafx.base;

    exports com.toko.kasirtoko;
    exports com.toko.kasirtoko.controller;
    exports com.toko.kasirtoko.model;
}