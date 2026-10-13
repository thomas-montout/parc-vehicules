package fr.taevie.parc.ui;

import fr.taevie.parc.model.Vehicule;

/**
 * Une ligne de la liste des véhicules, avec ce qu'il y a à bord.
 * Une ligne sans véhicule représente l'entrée « Tous les véhicules ».
 *
 * @param vehicule le véhicule, ou null pour la vue d'ensemble
 * @param articles nombre d'articles différents à bord
 * @param pieces   nombre total de pièces à bord
 */
record LigneVehicule(Vehicule vehicule, int articles, int pieces) {

    static LigneVehicule tous(int articles, int pieces) {
        return new LigneVehicule(null, articles, pieces);
    }

    boolean estTous() {
        return vehicule == null;
    }

    String libelle() {
        return estTous() ? "Tous les véhicules" : vehicule.getImmatriculation();
    }

    /** Le petit texte affiché à droite dans la liste. */
    String resume() {
        if (articles == 0) {
            return "vide";
        }
        return articles + (articles > 1 ? " articles" : " article");
    }
}
