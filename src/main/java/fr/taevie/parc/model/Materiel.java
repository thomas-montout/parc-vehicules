package fr.taevie.parc.model;

/**
 * Un article concret du parc : une famille, une gamme et une variante.
 * Exemple : Mouilleur / PAD / "35 cm".
 */
public class Materiel {

    private int id;
    private final Famille famille;
    private final Gamme gamme;
    private final String variante;
    private final int ordre;
    private final String unite;

    /**
     * Constructeur complet, utilisé à la relecture depuis la base.
     *
     * @param id       identifiant en base
     * @param famille  famille de rattachement
     * @param gamme    gamme de rattachement
     * @param variante déclinaison ("35 cm", "XL", "" si aucune)
     * @param ordre    rang d'affichage au sein de la famille
     * @param unite    unite, metre ou carton
     */
    public Materiel(int id, Famille famille, Gamme gamme, String variante, int ordre, String unite) {
        this.id = id;
        this.famille = famille;
        this.gamme = gamme;
        this.variante = variante;
        this.ordre = ordre;
        this.unite = unite;
    }

    /** Matériel pas encore enregistré : l'id sera attribué à l'insertion. */
    public Materiel(Famille famille, Gamme gamme, String variante, int ordre, String unite) {
        this(0, famille, gamme, variante, ordre, unite);
    }

    /** Cas courant : ni ordre particulier, ni unité spécifique. */
    public Materiel(Famille famille, Gamme gamme, String variante) {
        this(0, famille, gamme, variante, 0, "unite");
    }

    public int getId() {
        return id;
    }

    public Famille getFamille() {
        return famille;
    }

    public Gamme getGamme() {
        return gamme;
    }

    public String getVariante() {
        return variante;
    }

    public int getOrdre() {
        return ordre;
    }

    public String getUnite() {
        return unite;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Libellé complet de l'article, tel qu'il sera affiché dans l'interface.
     * La gamme n'apparaît que si elle est différente de Standard.
     *
     * @return par exemple "Mouilleur 35 cm PAD" ou simplement "Casque"
     */
    @Override
    public String toString() {
        StringBuilder texte = new StringBuilder(famille.getLibelle());

        if (!variante.isEmpty()) {
            texte.append(" ").append(variante);
        }
        if (!"Standard".equals(gamme.getLibelle())) {
            texte.append(" ").append(gamme.getLibelle());
        }

        return texte.toString();
    }
}