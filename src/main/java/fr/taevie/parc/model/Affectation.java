package fr.taevie.parc.model;

import java.time.LocalDate;

public class Affectation {
    private int id;
    private final Materiel materiel;
    private final Vehicule vehicule;
    private final int quantite;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;

    public Affectation(int id, Materiel materiel, Vehicule vehicule, int quantite, LocalDate dateDebut, LocalDate dateFin) {

        this.id = id;
        this.materiel = materiel;
        this.vehicule = vehicule;
        this.quantite = quantite;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Affectation(Materiel materiel, Vehicule vehicule, int quantite, LocalDate dateDebut) {

        this(0, materiel, vehicule, quantite, dateDebut, null);
    }

    public boolean estEnCours() {
        return dateFin == null;
    }

    public int getId() {
        return id;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public int getQuantite() {
        return quantite;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return materiel.toString();
    }
}
