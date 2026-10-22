package com.toko.kasirtoko.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private int categoryId;
    private String namaProduk;
    private String satuan;
    private BigDecimal hargaBeli;
    private BigDecimal hargaJual;
    private int stok;
    private int stokMinimum;

    // Constructor (Ini yang dicari oleh PosController baris 102)
    public Product(int id, int categoryId, String namaProduk, String satuan,
                   BigDecimal hargaBeli, BigDecimal hargaJual, int stok, int stokMinimum) {
        this.id = id;
        this.categoryId = categoryId;
        this.namaProduk = namaProduk;
        this.satuan = satuan;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.stokMinimum = stokMinimum;
    }

    // Getter Methods (Ini yang dicari oleh PosController)
    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getNamaProduk() { return namaProduk; }
    public String getSatuan() { return satuan; }
    public BigDecimal getHargaBeli() { return hargaBeli; }
    public BigDecimal getHargaJual() { return hargaJual; }
    public int getStok() { return stok; }
    public int getStokMinimum() { return stokMinimum; }
}