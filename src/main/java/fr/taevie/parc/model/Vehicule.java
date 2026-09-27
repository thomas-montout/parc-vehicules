package fr.taevie.parc.model;

public class Vehicule {

    private int id;
    private final String immatriculation;
    private final String marque;
    private final String modele;

    public Vehicule(int id, String immatriculation, String marque, String modele) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
    }

    public Vehicule(String immatriculation, String marque, String modele) {
        this(0, immatriculation, marque, modele);
    }

    public int getId() {
        return id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public String getModele() {
        return modele;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return immatriculation;
    }
}
