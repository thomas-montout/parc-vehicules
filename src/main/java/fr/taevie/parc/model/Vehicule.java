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

    public Vehicule(String immatriculation) {
        this(immatriculation, "", "");
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

    /**
     * Deux véhicules sont égaux s'ils désignent la même ligne en base.
     * <p>
     * Sans cette méthode, Java compare les adresses mémoire : deux objets
     * relus séparément pour le même camion seraient « différents ». C'est ce
     * qui faisait perdre la sélection de la liste déroulante après chaque
     * rafraîchissement, puisque la liste est rechargée avec de nouveaux objets.
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Vehicule v)) {
            return false;
        }
        // un véhicule pas encore enregistré (id 0) n'est égal qu'à lui-même
        return id != 0 && id == v.id;
    }

    /** Toujours redéfini avec equals : deux objets égaux doivent avoir le même hashCode. */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return immatriculation;
    }
}
