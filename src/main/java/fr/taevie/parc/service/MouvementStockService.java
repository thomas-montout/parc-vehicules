package fr.taevie.parc.service;

import fr.taevie.parc.dao.AffectationDao;
import fr.taevie.parc.dao.MouvementStockDao;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.MouvementStock;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Règles de gestion du stock. Le stock total n'est jamais saisi directement :
 * il résulte de la somme des mouvements (RG7).
 */
public class MouvementStockService {

    public static final String ACHAT = "ACHAT";
    public static final String CASSE = "CASSE";
    public static final String PERTE = "PERTE";
    public static final String AJUSTEMENT = "AJUSTEMENT";

    private final MouvementStockDao mouvementDao = new MouvementStockDao();
    private final AffectationDao affectationDao = new AffectationDao();

    /** Quantité possédée par l'entreprise, affectée ou non (RG7). */
    public int stockTotal(Materiel materiel) throws SQLException {
        return mouvementDao.stockTotal(materiel.getId());
    }

    /**
     * Enregistre une entrée de stock.
     *
     * @param quantite nombre d'exemplaires achetés, strictement positif
     */
    public void acheter(Materiel materiel, int quantite, String commentaire) throws SQLException {
        if (quantite <= 0) {
            throw new RegleGestionException("La quantité achetée doit être supérieure à 0.");
        }
        enregistrer(materiel, ACHAT, quantite, commentaire);
    }

    /**
     * Enregistre une sortie de stock pour casse ou perte.
     *
     * @param quantite nombre d'exemplaires concernés, saisi positivement
     * @throws RegleGestionException si le stock restant passerait sous ce qui
     *                               est encore chargé dans les véhicules (RG21)
     */
    public void sortir(Materiel materiel, String type, int quantite, String commentaire)
            throws SQLException {

        if (!CASSE.equals(type) && !PERTE.equals(type)) {
            throw new RegleGestionException("Type de sortie inconnu : " + type);
        }
        if (quantite <= 0) {
            throw new RegleGestionException("La quantité doit être supérieure à 0.");
        }

        int total = mouvementDao.stockTotal(materiel.getId());
        int affecte = affectationDao.quantiteAffectee(materiel.getId());

        if (total - quantite < affecte) {
            throw new RegleGestionException(
                    "Impossible : " + affecte + " exemplaire(s) sont encore dans les véhicules. "
                            + "Retirez-les d'abord, ou réduisez la quantité à "
                            + (total - affecte) + " au maximum.");
        }

        enregistrer(materiel, type, -quantite, commentaire);
    }

    /**
     * Corrige le stock après un inventaire. Une erreur ne se rattrape jamais en
     * modifiant un mouvement passé, mais par un mouvement d'ajustement (RG20).
     *
     * @param quantiteReelle ce que l'inventaire a réellement compté
     */
    public void ajuster(Materiel materiel, int quantiteReelle, String commentaire)
            throws SQLException {

        int total = mouvementDao.stockTotal(materiel.getId());
        int ecart = quantiteReelle - total;

        if (ecart == 0) {
            throw new RegleGestionException("Le stock est déjà à " + quantiteReelle + ".");
        }

        int affecte = affectationDao.quantiteAffectee(materiel.getId());
        if (quantiteReelle < affecte) {
            throw new RegleGestionException(
                    "Impossible : " + affecte + " exemplaire(s) sont chargés dans les véhicules.");
        }

        enregistrer(materiel, AJUSTEMENT, ecart, commentaire);
    }

    /** Historique des mouvements d'un matériel, du plus récent au plus ancien. */
    public List<MouvementStock> historique(Materiel materiel) throws SQLException {
        return mouvementDao.findByMateriel(materiel.getId());
    }

    /** Écriture effective, quantité déjà signée. */
    private void enregistrer(Materiel materiel, String type, int quantiteSignee, String commentaire)
            throws SQLException {

        MouvementStock mouvement = new MouvementStock(
                0, materiel, type, quantiteSignee, LocalDate.now(),
                commentaire == null ? "" : commentaire);

        mouvementDao.insert(mouvement);
    }
}