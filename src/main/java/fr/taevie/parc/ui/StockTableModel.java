package fr.taevie.parc.ui;

import fr.taevie.parc.model.LigneStock;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StockTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {"Matériel", "Unité", "Total", "Affecté", "Disponible"};

    private List<LigneStock> lignes = new ArrayList<>();

    public void setLignes(List<LigneStock> lignes) {
        this.lignes = lignes;
        fireTableDataChanged();
    }

    public LigneStock getLigne(int ligne) {
        return lignes.get(ligne);
    }

    @Override
    public int getRowCount() {
        return lignes.size();
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
        LigneStock l = lignes.get(ligne);

        return switch (colonne) {
            case 0 -> l.materiel().toString();
            case 1 -> l.materiel().getUnite();
            case 2 -> l.total();
            case 3 -> l.affecte();
            case 4 -> l.disponible();
            default -> "";
        };
    }
}