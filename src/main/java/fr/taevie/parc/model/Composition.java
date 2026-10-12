package fr.taevie.parc.model;

/**
 * Un composant d'un kit, avec sa quantité (RG26).
 */
public class Composition {

    private final Materiel kit;
    private final Materiel composant;
    private final int quantite;

    public Composition(Materiel kit, Materiel composant, int quantite) {
        this.kit = kit;
        this.composant = composant;
        this.quantite = quantite;
    }

    public Materiel getKit() {
        return kit;
    }

    public Materiel getComposant() {
        return composant;
    }

    public int getQuantite() {
        return quantite;
    }

    @Override
    public String toString() {
        return quantite + " × " + composant;
    }
}