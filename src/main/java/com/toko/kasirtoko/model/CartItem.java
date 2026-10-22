package com.toko.kasirtoko.model;

import java.math.BigDecimal;

public class CartItem {
    private int productId;
    private String namaProduk;
    private BigDecimal hargaModal;
    private BigDecimal hargaSatuan;
    private int kuantitas;
    private BigDecimal diskon;
    private BigDecimal subtotal;

    public CartItem(int productId, String namaProduk, BigDecimal hargaModal, BigDecimal hargaSatuan, int kuantitas, BigDecimal diskon) {
        this.productId = productId;
        this.namaProduk = namaProduk;
        this.hargaModal = hargaModal;
        this.hargaSatuan = hargaSatuan;
        this.kuantitas = kuantitas;
        this.diskon = diskon;
        hitungSubtotal();
    }

    public void hitungSubtotal() {
        BigDecimal totalHarga = this.hargaSatuan.multiply(BigDecimal.valueOf(this.kuantitas));
        this.subtotal = totalHarga.subtract(this.diskon);
        if (this.subtotal.compareTo(BigDecimal.ZERO) < 0) {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public int getProductId() { return productId; }
    public String getNamaProduk() { return namaProduk; }
    public BigDecimal getHargaModal() { return hargaModal; }
    public BigDecimal getHargaSatuan() { return hargaSatuan; }
    public int getKuantitas() { return kuantitas; }
    public void setKuantitas(int kuantitas) {
        this.kuantitas = kuantitas;
        hitungSubtotal();
    }
    public BigDecimal getDiskon() { return diskon; }
    public void setDiskon(BigDecimal diskon) {
        this.diskon = diskon;
        hitungSubtotal();
    }
    public BigDecimal getSubtotal() { return subtotal; }
}