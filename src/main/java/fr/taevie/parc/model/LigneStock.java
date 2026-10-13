package fr.taevie.parc.model;

/**
 * Une ligne du tableau de stock. Ne correspond à aucune table :
 * c'est le résultat d'un calcul sur les mouvements et les affectations.
 */
public record LigneStock(Materiel materiel, int total, int affecte) {

    public int disponible() {
        return total - affecte;
    }
}