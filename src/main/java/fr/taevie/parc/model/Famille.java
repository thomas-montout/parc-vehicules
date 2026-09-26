package fr.taevie.parc.model;

public class Famille {

    private int id;
    private final String libelle;

    public Famille(int id, String libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public Famille(String libelle) {
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