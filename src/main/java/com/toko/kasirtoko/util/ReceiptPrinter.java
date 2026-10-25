package com.toko.kasirtoko.util;

import com.toko.kasirtoko.model.CartItem;
import com.toko.kasirtoko.model.Transaction;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
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
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // 1. Buat folder "Riwayat_Struk" jika belum ada
        String folderPath = "Riwayat_Struk";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // 2. Tentukan nama file PDF (Contoh: INV-1698765432.pdf)
        String pdfFileName = folderPath + "/" + trx.getNoFaktur() + ".pdf";

        // 3. Inisialisasi Dokumen PDF (Kertas Thermal 80mm)
        // 226 point setara dengan lebar sekitar 80mm. Tinggi 800 (akan memanjang sesuai isi).
        Rectangle pageSize = new Rectangle(226, 800);
        // Margin: Kiri 10, Kanan 10, Atas 15, Bawah 15
        Document document = new Document(pageSize, 10, 10, 15, 15);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
            document.open();

            // Font PDF
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            Font fontBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
            Font fontGrandTotal = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);

            // Garis pembatas putus-putus
            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setGap(2f);
            Chunk dottedLine = new Chunk(separator);

            // --- HEADER STRUK ---
            Paragraph tokoName = new Paragraph("SISTEM KASIR TOKO", fontHeader);
            tokoName.setAlignment(Element.ALIGN_CENTER);
            document.add(tokoName);

            Paragraph alamat = new Paragraph("Jl. Contoh Alamat No. 123 - Kediri\n", fontNormal);
            alamat.setAlignment(Element.ALIGN_CENTER);
            document.add(alamat);
            document.add(new Paragraph(dottedLine));

            // --- INFO TRANSAKSI ---
            document.add(new Paragraph("No Faktur : " + trx.getNoFaktur(), fontNormal));
            document.add(new Paragraph("Tanggal   : " + LocalDateTime.now().format(dtFormatter), fontNormal));
            document.add(new Paragraph("Kasir     : " + namaKasir, fontNormal));
            document.add(new Paragraph(dottedLine));

            // --- DAFTAR BARANG (Menggunakan Tabel Rata Kanan Kiri) ---
            PdfPTable tableItems = new PdfPTable(2);
            tableItems.setWidthPercentage(100);
            tableItems.setWidths(new float[]{5f, 4f}); // Lebar kolom rincian vs subtotal
            tableItems.setSpacingBefore(5);
            tableItems.setSpacingAfter(5);

            for (CartItem item : cartItems) {
                // Baris Nama Produk (Mengambil 2 kolom)
                PdfPCell cellName = new PdfPCell(new Phrase(item.getNamaProduk(), fontBold));
                cellName.setColspan(2);
                cellName.setBorder(Rectangle.NO_BORDER);
                tableItems.addCell(cellName);

                // Baris Rincian Qty x Harga
                String rincian = "   " + item.getKuantitas() + " x " + rupiahFormat.format(item.getHargaSatuan());
                if (item.getDiskon().compareTo(BigDecimal.ZERO) > 0) {
                    rincian += "\n   (Diskon: -" + rupiahFormat.format(item.getDiskon()) + ")";
                }
                PdfPCell cellRincian = new PdfPCell(new Phrase(rincian, fontNormal));
                cellRincian.setBorder(Rectangle.NO_BORDER);
                tableItems.addCell(cellRincian);

                // Baris Subtotal (Rata Kanan)
                PdfPCell cellSub = new PdfPCell(new Phrase(rupiahFormat.format(item.getSubtotal()), fontNormal));
                cellSub.setBorder(Rectangle.NO_BORDER);
                cellSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellSub.setVerticalAlignment(Element.ALIGN_BOTTOM);
                tableItems.addCell(cellSub);
            }
            document.add(tableItems);
            document.add(new Paragraph(dottedLine));

            // --- TOTAL & PEMBAYARAN ---
            PdfPTable tableTotals = new PdfPTable(2);
            tableTotals.setWidthPercentage(100);
            tableTotals.setWidths(new float[]{4f, 5f});
            tableTotals.setSpacingBefore(5);

            // Subtotal
            addTotalRow(tableTotals, "Subtotal", rupiahFormat.format(trx.getSubtotal()), fontNormal);

            // Diskon & PPN (Hanya tampil jika ada)
            if (trx.getDiskonTransaksi().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(tableTotals, "Diskon Transaksi", "-" + rupiahFormat.format(trx.getDiskonTransaksi()), fontNormal);
            }
            if (trx.getPajakPpn().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(tableTotals, "PPN (11%)", rupiahFormat.format(trx.getPajakPpn()), fontNormal);
            }

            // Grand Total
            PdfPCell cellGtLabel = new PdfPCell(new Phrase("\nGRAND TOTAL", fontGrandTotal));
            cellGtLabel.setBorder(Rectangle.NO_BORDER);
            tableTotals.addCell(cellGtLabel);

            PdfPCell cellGtValue = new PdfPCell(new Phrase("\n" + rupiahFormat.format(trx.getGrandTotal()), fontGrandTotal));
            cellGtValue.setBorder(Rectangle.NO_BORDER);
            cellGtValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableTotals.addCell(cellGtValue);

            // Metode & Uang
            addTotalRow(tableTotals, "Metode Bayar", trx.getMetodeBayar(), fontNormal);
            addTotalRow(tableTotals, "Bayar", rupiahFormat.format(trx.getNominalBayar()), fontNormal);
            addTotalRow(tableTotals, "Kembalian", rupiahFormat.format(trx.getKembalian()), fontNormal);

            document.add(tableTotals);
            document.add(new Paragraph(dottedLine));

            // --- FOOTER ---
            Paragraph footer = new Paragraph("Terima Kasih Atas Kunjungan Anda\n\n", fontNormal);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();
            System.out.println("Berhasil mencetak PDF: " + pdfFileName);

        } catch (Exception e) {
            System.err.println("Gagal mencetak struk PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fungsi bantu untuk menambahkan baris total ke tabel dengan cepat
    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, font));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, font));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellValue);
    }
}