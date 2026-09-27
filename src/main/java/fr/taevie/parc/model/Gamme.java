package fr.taevie.parc.model;

public class Gamme {

    private int id;
    private final String libelle;

    public Gamme(int id, String libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public Gamme(String libelle) {
        this(0, libelle);
    }

    public int getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
