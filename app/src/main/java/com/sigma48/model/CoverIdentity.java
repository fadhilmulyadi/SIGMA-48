package com.sigma48.model;

public class CoverIdentity {

    private String coverName;
    private String coverPassport;
    private String coverRole;

    //Konstruktor Default
    public CoverIdentity() {
        // Tidak ada parameter, ini konstruktor default
    }

    //Konstruktor Berparameter
    public CoverIdentity(String coverName, String coverPassport, String coverRole) {
        this.coverName = coverName;
        this.coverPassport = coverPassport;
        this.coverRole = coverRole;
    }

    //Getter
    public String getCoverName() {
        return coverName;
    }

    public String getCoverPassport() {
        return coverPassport;
    }

    public String getCoverRole() {
        return coverRole;
    }

    //Setter
    public void setCoverName(String coverName) {
        this.coverName = coverName;
    }

    public void setCoverPassport(String coverPassport) {
        this.coverPassport = coverPassport;
    }

    public void setCoverRole(String coverRole) {
        this.coverRole = coverRole;
    }

    //Method toString
    @Override
    public String toString() {
        return "CoverIdentity{coverName='" + coverName + "', coverRole='" + coverRole + "'}";
    }
}
