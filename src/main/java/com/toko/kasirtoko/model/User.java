package com.toko.kasirtoko.model;

public class User {
    private int id;
    private String namaLengkap;
    private String username;
    private String passwordHash;
    private String role;

    // Constructor
    public User(int id, String namaLengkap, String username, String passwordHash, String role) {
        this.id = id;
        this.namaLengkap = namaLengkap;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getter Methods (Ini yang dicari oleh LoginController & PosController)
    public int getId() { return id; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}