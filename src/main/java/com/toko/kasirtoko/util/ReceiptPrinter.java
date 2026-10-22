package com.toko.kasirtoko.util;

import com.toko.kasirtoko.model.CartItem;
import com.toko.kasirtoko.model.Transaction;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReceiptPrinter {

    public void printReceipt(Transaction trx, List<CartItem> cartItems, String namaKasir) {
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // 1. Buat folder "Riwayat_Struk" jika belum ada
        String folderPath = "Riwayat_Struk";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // 2. Tentukan nama file PDF (Contoh: INV-1698765432.pdf)
        String pdfFileName = folderPath + "/" + trx.getNoFaktur() + ".pdf";

        // 3. Inisialisasi Dokumen PDF (ukuran standar kertas A4, bisa disesuaikan nanti)
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
            document.open();

            // Font PDF
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font fontBold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            DottedLineSeparator separator = new DottedLineSeparator();

            // --- HEADER STRUK ---
            Paragraph tokoName = new Paragraph("SISTEM KASIR TOKO", fontHeader);
            tokoName.setAlignment(Element.ALIGN_CENTER);
            document.add(tokoName);

            Paragraph alamat = new Paragraph("Jl. Contoh Alamat No. 123 - Kediri\n\n", fontNormal);
            alamat.setAlignment(Element.ALIGN_CENTER);
            document.add(alamat);
            document.add(separator);

            // --- INFO TRANSAKSI ---
            document.add(new Paragraph("No Faktur : " + trx.getNoFaktur(), fontNormal));
            document.add(new Paragraph("Tanggal   : " + LocalDateTime.now().format(dtFormatter), fontNormal));
            document.add(new Paragraph("Kasir     : " + namaKasir, fontNormal));
            document.add(new Paragraph("\n"));
            document.add(separator);
            document.add(new Paragraph("\n"));

            // --- DAFTAR BARANG ---
            for (CartItem item : cartItems) {
                // Nama Produk
                document.add(new Paragraph(item.getNamaProduk(), fontBold));

                // Rincian Qty x Harga = Subtotal
                String rincian = item.getKuantitas() + " x " + rupiahFormat.format(item.getHargaSatuan()) +
                        " = " + rupiahFormat.format(item.getSubtotal());
                document.add(new Paragraph(rincian, fontNormal));

                // Diskon item (jika ada)
                if (item.getDiskon().compareTo(BigDecimal.ZERO) > 0) {
                    document.add(new Paragraph("  (Diskon: " + rupiahFormat.format(item.getDiskon()) + ")", fontNormal));
                }
                document.add(new Paragraph("\n"));
            }

            document.add(separator);
            document.add(new Paragraph("\n"));

            // --- TOTAL & PEMBAYARAN ---
            document.add(new Paragraph("Subtotal      : " + rupiahFormat.format(trx.getSubtotal()), fontNormal));

            if (trx.getDiskonTransaksi().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Diskon Transaksi: -" + rupiahFormat.format(trx.getDiskonTransaksi()), fontNormal));
            }
            if (trx.getPajakPpn().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("PPN (11%)     : " + rupiahFormat.format(trx.getPajakPpn()), fontNormal));
            }

            document.add(new Paragraph("\nGRAND TOTAL : " + rupiahFormat.format(trx.getGrandTotal()), fontHeader));
            document.add(new Paragraph("Metode Bayar  : " + trx.getMetodeBayar(), fontNormal));
            document.add(new Paragraph("Bayar         : " + rupiahFormat.format(trx.getNominalBayar()), fontNormal));
            document.add(new Paragraph("Kembalian     : " + rupiahFormat.format(trx.getKembalian()), fontNormal));

            document.add(new Paragraph("\n"));
            document.add(separator);

            // --- FOOTER ---
            Paragraph footer = new Paragraph("\nTerima Kasih Atas Kunjungan Anda", fontNormal);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            System.out.println("Berhasil mencetak PDF: " + pdfFileName);

        } catch (Exception e) {
            System.err.println("Gagal mencetak struk PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}