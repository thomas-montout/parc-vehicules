package fr.taevie.parc.model;

import java.time.LocalDate;

/**
 * Une entrée ou une sortie de stock pour un matériel (RG17).
 * Le stock total ne se stocke pas : il est la somme de ces mouvements.
 */
public class MouvementStock {

    private int id;
    private final Materiel materiel;
    private final String type;
    private final int quantite;
    private final LocalDate date;
    private final String commentaire;

    public MouvementStock(int id, Materiel materiel, String type, int quantite,
                          LocalDate date, String commentaire) {
        this.id = id;
        this.materiel = materiel;
        this.type = type;
        this.quantite = quantite;
        this.date = date;
        this.commentaire = commentaire;
    }

    public MouvementStock(Materiel materiel, String type, int quantite, LocalDate date) {
        this(0, materiel, type, quantite, date, "");
    }

    public int getId() {
        return id;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public String getType() {
        return type;
    }

    public int getQuantite() {
        return quantite;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return type + " " + quantite + " × " + materiel;
    }
}