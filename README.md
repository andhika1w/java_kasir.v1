# java_kasir.v1

**Aplikasi kasir (Point of Sale) sederhana berbasis Java dengan database MySQL.**

> *English summary:* java_kasir.v1 is a simple open-source point-of-sale (POS) desktop app written in Java and backed by a MySQL database via JDBC. It is designed for small shops and is at an early stage. Contributions are welcome.

---

## Tentang Proyek

java_kasir.v1 dibuat untuk membantu usaha kecil seperti warung dan toko kelontong mencatat penjualan dengan sistem yang gratis dan bisa dimodifikasi sendiri. Aplikasi dibangun dengan Java dan terhubung ke database MySQL untuk menyimpan data barang dan transaksi.

Status proyek: **tahap awal (early stage)**.

## Fitur

- [ ] Kelola data barang (tambah, ubah, hapus)
- [ ] Transaksi penjualan dan perhitungan total
- [ ] Penyimpanan data ke MySQL
- [ ] Riwayat transaksi
- [ ] Cetak struk
- [ ] Laporan penjualan

## Teknologi

| Komponen | Keterangan |
|---|---|
| Bahasa | Java (JDK 8 atau lebih baru) |
| Database | MySQL / MariaDB |
| Koneksi database | JDBC (MySQL Connector/J) |
| IDE | NetBeans, IntelliJ IDEA, Eclipse, atau IDE Java lainnya |

## Persiapan

Pastikan sudah terpasang:

1. JDK 8 atau lebih baru
2. MySQL Server (atau XAMPP/Laragon yang menyertakan MySQL)
3. Sebuah IDE Java
4. MySQL Connector/J (file `.jar` driver JDBC)

## Cara Menjalankan

### 1. Clone repositori

```bash
git clone https://github.com/andhika1w/java_kasir.v1.git
```

### 2. Buat database

Buka MySQL (phpMyAdmin atau terminal), lalu jalankan:

```sql
CREATE DATABASE db_kasir;
USE db_kasir;

CREATE TABLE barang (
    id_barang   INT AUTO_INCREMENT PRIMARY KEY,
    nama_barang VARCHAR(100) NOT NULL,
    harga       DECIMAL(12,2) NOT NULL,
    stok        INT NOT NULL DEFAULT 0
);

CREATE TABLE transaksi (
    id_transaksi INT AUTO_INCREMENT PRIMARY KEY,
    tanggal      DATETIME DEFAULT CURRENT_TIMESTAMP,
    total        DECIMAL(12,2) NOT NULL
);

CREATE TABLE detail_transaksi (
    id_detail    INT AUTO_INCREMENT PRIMARY KEY,
    id_transaksi INT NOT NULL,
    id_barang    INT NOT NULL,
    jumlah       INT NOT NULL,
    subtotal     DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi),
    FOREIGN KEY (id_barang) REFERENCES barang(id_barang)
);
```

> Sesuaikan struktur tabel dengan yang dipakai di kodemu. Jika kamu menyimpan file `.sql`, letakkan di folder `database/` agar mudah diimpor.

### 3. Atur koneksi database

Contoh koneksi JDBC di Java:

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static final String URL  = "jdbc:mysql://localhost:3306/db_kasir";
    private static final String USER = "root";
    private static final String PASS = ""; // isi sesuai password MySQL-mu

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
```

Ubah `USER` dan `PASS` sesuai pengaturan MySQL di komputermu. Jangan mengunggah password asli ke GitHub.

### 4. Buka di IDE dan jalankan

1. Buka proyek di IDE Java.
2. Tambahkan MySQL Connector/J (`.jar`) ke library proyek.
   - **NetBeans:** klik kanan *Libraries* → *Add JAR/Folder*
   - **IntelliJ:** *File* → *Project Structure* → *Libraries* → *+*
   - **Eclipse:** klik kanan proyek → *Build Path* → *Add External JARs*
3. Pastikan MySQL sedang berjalan.
4. Jalankan kelas utama (main class) proyek.

## Struktur Folder

> Sesuaikan dengan struktur proyek sebenarnya.

```
java_kasir.v1/
├── src/            # kode sumber Java
├── database/       # file SQL (opsional)
├── lib/            # library .jar (opsional)
├── README.md
└── LICENSE
```

## Rencana Pengembangan

- [ ] Login kasir dan manajemen pengguna
- [ ] Laporan penjualan harian dan bulanan
- [ ] Cetak struk
- [ ] Rekomendasi harga jual berbasis AI dari riwayat penjualan dan harga modal
- [ ] Dokumentasi yang lebih lengkap

## Kontribusi

Kontribusi sangat diterima, terutama dari pemula.

1. Fork repositori ini
2. Buat branch baru: `git checkout -b fitur-baru`
3. Commit perubahanmu: `git commit -m "Menambah fitur X"`
4. Push ke branch: `git push origin fitur-baru`
5. Buka Pull Request

Kamu juga bisa membuka *Issue* untuk melaporkan bug atau mengusulkan fitur.

## Lisensi

Proyek ini dirilis di bawah lisensi **MIT**. Lihat file [LICENSE](LICENSE) untuk detail.

## Kontak

Dikembangkan oleh [@andhika1w](https://github.com/andhika1w).
