package com.sigma48.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Agent extends User {

    private List<String> spesialisasi = new ArrayList<>();

    public Agent(String username, String passwordHash, Role role) {
        super(username, passwordHash, role);
    }

    public void tambahSpesialisasi(String spesialisasiItem) {
        if (spesialisasiItem != null && !spesialisasiItem.trim().isEmpty() && !spesialisasi.contains(spesialisasiItem)) {
            spesialisasi.add(spesialisasiItem);
        }
    }

    public void hapusSpesialisasi(String spesialisasiItem) {
        spesialisasi.remove(spesialisasiItem);
    }
}
