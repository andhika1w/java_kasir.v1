package com.toko.kasirtoko.model;

import java.math.BigDecimal;

public class Transaction {
    private String noFaktur;
    private int userId;
    private BigDecimal subtotal;
    private BigDecimal diskonTransaksi;
    private BigDecimal pajakPpn;
    private BigDecimal grandTotal;
    private String metodeBayar;
    private BigDecimal nominalBayar;
    private BigDecimal kembalian;

    public Transaction(String noFaktur, int userId, BigDecimal subtotal, BigDecimal diskonTransaksi,
                       BigDecimal pajakPpn, BigDecimal grandTotal, String metodeBayar,
                       BigDecimal nominalBayar, BigDecimal kembalian) {
        this.noFaktur = noFaktur;
        this.userId = userId;
        this.subtotal = subtotal;
        this.diskonTransaksi = diskonTransaksi;
        this.pajakPpn = pajakPpn;
        this.grandTotal = grandTotal;
        this.metodeBayar = metodeBayar;
        this.nominalBayar = nominalBayar;
        this.kembalian = kembalian;
    }

    // Getter methods
    public String getNoFaktur() { return noFaktur; }
    public int getUserId() { return userId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiskonTransaksi() { return diskonTransaksi; }
    public BigDecimal getPajakPpn() { return pajakPpn; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getMetodeBayar() { return metodeBayar; }
    public BigDecimal getNominalBayar() { return nominalBayar; }
    public BigDecimal getKembalian() { return kembalian; }
}