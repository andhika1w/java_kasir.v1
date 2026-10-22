package com.toko.kasirtoko.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordGenerator {
    public static void main(String[] args) {
        // Ganti teks di bawah dengan password yang Anda inginkan
        String passwordAsli = "rahasia123";

        String bcryptHashString = BCrypt.withDefaults().hashToString(12, passwordAsli.toCharArray());

        System.out.println("Copy teks hasil enkripsi di bawah ini ke phpMyAdmin:");
        System.out.println(bcryptHashString);
    }
}