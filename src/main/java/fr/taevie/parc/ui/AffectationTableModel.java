package fr.taevie.parc.ui;

import fr.taevie.parc.model.Affectation;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Fait le pont entre une liste d'affectations et un JTable.
 * Le tableau ne stocke rien : il interroge ce modèle pour savoir
 * combien de lignes afficher et ce que contient chaque cellule.
 * <p>
 * La colonne « Véhicule » n'apparaît qu'en vue d'ensemble : quand on regarde
 * un seul camion, elle répéterait la même plaque sur chaque ligne.
 */
public class AffectationTableModel extends AbstractTableModel {

    static final String VEHICULE = "Véhicule";
    static final String MATERIEL = "Matériel";
    static final String QUANTITE = "Quantité";
    static final String DEPUIS = "Depuis le";

    private static final String[] AVEC_VEHICULE = {VEHICULE, MATERIEL, QUANTITE, DEPUIS};
    private static final String[] SANS_VEHICULE = {MATERIEL, QUANTITE, DEPUIS};

    private List<Affectation> affectations = new ArrayList<>();
    private boolean afficherVehicule = true;

    /**
     * Remplace les données affichées.
     *
     * @param affectations     les lignes à montrer
     * @param afficherVehicule true en vue d'ensemble, false pour un seul véhicule
     */
    public void setAffectations(List<Affectation> affectations, boolean afficherVehicule) {
        boolean colonnesChangent = this.afficherVehicule != afficherVehicule;

        this.affectations = affectations;
        this.afficherVehicule = afficherVehicule;

        if (colonnesChangent) {
            // le nombre de colonnes change : le JTable doit reconstruire ses colonnes
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

    /** L'affectation d'une ligne donnée, pour savoir sur quoi l'utilisateur a cliqué. */
    public Affectation getAffectation(int ligne) {
        return affectations.get(ligne);
    }

    private String[] colonnes() {
        return afficherVehicule ? AVEC_VEHICULE : SANS_VEHICULE;
    }

    @Override
    public int getRowCount() {
        return affectations.size();
    }

    @Override
    public int getColumnCount() {
        return colonnes().length;
    }

    @Override
    public String getColumnName(int colonne) {
        return colonnes()[colonne];
    }

    /**
     * Le type de chaque colonne. Swing s'en sert pour choisir l'affichage :
     * un Integer est aligné à droite et trié comme un nombre (2 avant 10),
     * pas comme du texte (« 10 » avant « 2 »).
     */
    @Override
    public Class<?> getColumnClass(int colonne) {
        return switch (colonnes()[colonne]) {
            case QUANTITE -> Integer.class;
            case DEPUIS -> LocalDate.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int ligne, int colonne) {
        Affectation a = affectations.get(ligne);

        return switch (colonnes()[colonne]) {
            case VEHICULE -> a.getVehicule().getImmatriculation();
            case MATERIEL -> a.getMateriel().toString();
            case QUANTITE -> a.getQuantite();
            case DEPUIS -> a.getDateDebut();
            default -> "";
        };
    }
}
