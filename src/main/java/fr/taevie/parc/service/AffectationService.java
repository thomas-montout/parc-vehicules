package fr.taevie.parc.service;

import fr.taevie.parc.dao.AffectationDao;
import fr.taevie.parc.dao.MouvementStockDao;
import fr.taevie.parc.dao.VehiculeDao;
import fr.taevie.parc.model.Affectation;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Règles de gestion des affectations : qui peut être chargé où, et en quelle
 * quantité. C'est ici que vivent RG8, RG9, RG15, RG16 et RG29 — les DAO, eux,
 * ne font qu'écrire et lire.
 */
public class AffectationService {

    private final AffectationDao affectationDao = new AffectationDao();
    private final MouvementStockDao mouvementDao = new MouvementStockDao();
    private final VehiculeDao vehiculeDao = new VehiculeDao();

    /**
     * Tous les véhicules du parc, triés par immatriculation.
     * L'interface passe par ici plutôt que d'appeler le DAO directement :
     * elle ne parle qu'à la couche service.
     */
    public List<Vehicule> listerVehicules() throws SQLException {
        return vehiculeDao.findAll();
    }

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
     * Si ce matériel est déjà présent dans ce véhicule, les quantités se
     * cumulent (RG29) : l'affectation en cours est clôturée et une nouvelle est
     * créée avec le total, ce qui respecte à la fois RG15 (historique conservé)
     * et RG16 (une seule affectation active par couple).
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

        int idExistante = affectationDao.idAffectationEnCours(
                materiel.getId(), vehicule.getId());

        if (idExistante == 0) {
            affectationDao.insert(
                    new Affectation(materiel, vehicule, quantite, LocalDate.now()));
            return;
        }

        int dejaPresent = affectationDao.quantiteEnCours(materiel.getId(), vehicule.getId());

        affectationDao.cloturer(idExistante, LocalDate.now());
        affectationDao.insert(new Affectation(
                materiel, vehicule, dejaPresent + quantite, LocalDate.now()));
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
        int disponible = stockDisponible(affectation.getMateriel());

        if (ecart > disponible) {
            throw new RegleGestionException(
                    "Stock insuffisant : il manque " + (ecart - disponible) + " exemplaire(s).");
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
     * Le stock n'est pas revérifié, la quantité totale affectée ne changeant pas.
     */
    public void deplacer(Affectation affectation, Vehicule destination) throws SQLException {
        if (!affectation.estEnCours()) {
            throw new RegleGestionException("Cette affectation est déjà clôturée.");
        }
        if (affectation.getVehicule().getId() == destination.getId()) {
            throw new RegleGestionException("Le matériel est déjà dans ce véhicule.");
        }
        if (affectationDao.idAffectationEnCours(
                affectation.getMateriel().getId(), destination.getId()) != 0) {
            throw new RegleGestionException(
                    "Ce matériel est déjà présent dans le véhicule de destination.");
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