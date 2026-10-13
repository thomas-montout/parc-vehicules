package fr.taevie.parc.ui;

import fr.taevie.parc.model.Affectation;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Fait le pont entre une liste d'affectations et un JTable.
 * Le tableau ne stocke rien : il interroge ce modèle pour savoir
 * combien de lignes afficher et ce que contient chaque cellule.
 */
public class AffectationTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {"Véhicule", "Matériel", "Quantité", "Depuis le"};

    private List<Affectation> affectations = new ArrayList<>();

    /** Remplace les données affichées et demande au tableau de se redessiner. */
    public void setAffectations(List<Affectation> affectations) {
        this.affectations = affectations;
        fireTableDataChanged();
    }

    /** L'affectation d'une ligne donnée, pour savoir sur quoi l'utilisateur a cliqué. */
    public Affectation getAffectation(int ligne) {
        return affectations.get(ligne);
    }

    @Override
    public int getRowCount() {
        return affectations.size();
    }

    @Override
    public int getColumnCount() {
        return COLONNES.length;
    }

    @Override
    public String getColumnName(int colonne) {
        return COLONNES[colonne];
    }

    @Override
    public Object getValueAt(int ligne, int colonne) {
        Affectation a = affectations.get(ligne);

        return switch (colonne) {
            case 0 -> a.getVehicule().getImmatriculation();
            case 1 -> a.getMateriel().toString();
            case 2 -> a.getQuantite();
            case 3 -> a.getDateDebut();
            default -> "";
        };
    }
}