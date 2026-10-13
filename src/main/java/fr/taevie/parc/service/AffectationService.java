package fr.taevie.parc.service;

import fr.taevie.parc.dao.AffectationDao;
import fr.taevie.parc.dao.MouvementStockDao;
import fr.taevie.parc.model.Affectation;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Règles de gestion des affectations : qui peut être chargé où, et en quelle
 * quantité. C'est ici que vivent RG8, RG9 et RG15 — les DAO, eux, ne font
 * qu'écrire et lire.
 */
public class AffectationService {

    private final AffectationDao affectationDao = new AffectationDao();
    private final MouvementStockDao mouvementDao = new MouvementStockDao();

    /**
     * Quantité d'un matériel encore disponible, c'est-à-dire possédée mais
     * pas encore chargée dans un véhicule (RG9).
     */
    public int stockDisponible(Materiel materiel) throws SQLException {
        int total = mouvementDao.stockTotal(materiel.getId());
        int affecte = affectationDao.quantiteAffectee(materiel.getId());
        return total - affecte;
    }

    /**
     * Charge un matériel dans un véhicule après vérification du stock (RG8).
     *
     * @throws RegleGestionException si la quantité demandée dépasse le disponible
     */
    public void affecter(Materiel materiel, Vehicule vehicule, int quantite) throws SQLException {
        if (quantite <= 0) {
            throw new RegleGestionException("La quantité doit être supérieure à 0.");
        }

        int disponible = stockDisponible(materiel);
        if (quantite > disponible) {
            throw new RegleGestionException(
                    "Stock insuffisant pour " + materiel + " : "
                            + disponible + " disponible(s), " + quantite + " demandé(s).");
        }

        affectationDao.insert(new Affectation(materiel, vehicule, quantite, LocalDate.now()));
    }

    /**
     * Retire un matériel d'un véhicule. L'affectation n'est pas supprimée mais
     * clôturée, ce qui préserve l'historique (RG15).
     */
    public void retirer(Affectation affectation) throws SQLException {
        if (!affectation.estEnCours()) {
            throw new RegleGestionException("Cette affectation est déjà clôturée.");
        }

        affectationDao.cloturer(affectation.getId(), LocalDate.now());
    }

    /**
     * Change la quantité d'une affectation en cours. Conformément à RG15,
     * l'ancienne ligne est clôturée et une nouvelle est créée : on ne modifie
     * jamais une affectation existante.
     */
    public void changerQuantite(Affectation affectation, int nouvelleQuantite) throws SQLException {
        if (!affectation.estEnCours()) {
            throw new RegleGestionException("Cette affectation est déjà clôturée.");
        }
        if (nouvelleQuantite <= 0) {
            throw new RegleGestionException("La quantité doit être supérieure à 0.");
        }

        int ecart = nouvelleQuantite - affectation.getQuantite();
        if (ecart > 0 && ecart > stockDisponible(affectation.getMateriel())) {
            throw new RegleGestionException(
                    "Stock insuffisant : il manque "
                            + (ecart - stockDisponible(affectation.getMateriel())) + " exemplaire(s).");
        }

        affectationDao.cloturer(affectation.getId(), LocalDate.now());
        affectationDao.insert(new Affectation(
                affectation.getMateriel(),
                affectation.getVehicule(),
                nouvelleQuantite,
                LocalDate.now()));
    }

    /**
     * Déplace un matériel d'un véhicule vers un autre : l'affectation d'origine
     * est clôturée et une nouvelle est ouverte sur le véhicule de destination.
     */
    public void deplacer(Affectation affectation, Vehicule destination) throws SQLException {
        if (!affectation.estEnCours()) {
            throw new RegleGestionException("Cette affectation est déjà clôturée.");
        }
        if (affectation.getVehicule().getId() == destination.getId()) {
            throw new RegleGestionException("Le matériel est déjà dans ce véhicule.");
        }

        affectationDao.cloturer(affectation.getId(), LocalDate.now());
        affectationDao.insert(new Affectation(
                affectation.getMateriel(),
                destination,
                affectation.getQuantite(),
                LocalDate.now()));
    }

    /** Tout ce qui est actuellement chargé, tous véhicules confondus. */
    public List<Affectation> listerEnCours() throws SQLException {
        return affectationDao.findEnCours();
    }
}