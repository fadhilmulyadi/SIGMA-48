package com.sigma48.model;

import java.util.ArrayList;
import java.util.List;

public class Agent extends User {

    private List<String> spesialisasi;

    public Agent() {
        super();
        this.spesialisasi = new ArrayList<>();
    }

    public Agent(String username, String passwordHash, Role role) {
        super(username, passwordHash, role);
        this.spesialisasi = new ArrayList<>();
    }

    public Agent(String username, String passwordHash, Role role, List<String> spesialisasi) {
        this(username, passwordHash, role);
        if (spesialisasi != null) {
            this.spesialisasi = new ArrayList<>(spesialisasi);
        } else {
            this.spesialisasi = new ArrayList<>();
        }
    }

    public List<String> getSpesialisasi() {
        return this.spesialisasi;
    }

    public void setSpesialisasi(List<String> spesialisasi) {
        if (spesialisasi != null) {
            this.spesialisasi = new ArrayList<>(spesialisasi);
        } else {
            this.spesialisasi = new ArrayList<>();
        }
    }

    public void tambahSpesialisasi(String spesialisasiItem) {
        if (spesialisasiItem != null && !spesialisasiItem.trim().isEmpty()) {
            if (this.spesialisasi == null) {
                this.spesialisasi = new ArrayList<>();
            }
            if (!this.spesialisasi.contains(spesialisasiItem)) {
                this.spesialisasi.add(spesialisasiItem);
            }
        }
    }

    public void hapusSpesialisasi(String spesialisasiItem) {
        if (spesialisasiItem != null && this.spesialisasi != null) {
            this.spesialisasi.remove(spesialisasiItem);
        }
    }

    @Override
    public String toString() {
        return "Agent{" +
               "id='" + getId() + '\'' +
               ", username='" + getUsername() + '\'' +
               ", role=" + getRole() +
               ", spesialisasi=" + (spesialisasi != null ? String.join(", ", spesialisasi) : "Tidak ada") +
               '}';
    }
}