package fr.taevie.parc.service;

import fr.taevie.parc.model.Affectation;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Règles de gestion des affectations : RG8, RG9, RG15, RG16, RG29.
 */
class AffectationServiceTest extends BaseDeTest {

    private final AffectationService affectations = new AffectationService();
    private final MouvementStockService stock = new MouvementStockService();

    private Materiel grattoir;
    private Vehicule camionA;
    private Vehicule camionB;

    @BeforeEach
    void creerDonnees() throws Exception {
        grattoir = materiel("Grattoir", "10 cm");
        camionA = vehicule("AA-111-AA");
        camionB = vehicule("BB-222-BB");
    }

    @Test
    @DisplayName("RG8 — on ne peut pas charger plus que le stock disponible")
    void refuseAuDelaDuStockDisponible() throws Exception {
        stock.acheter(grattoir, 5, "test");
        affectations.affecter(grattoir, camionA, 3);

        RegleGestionException erreur = assertThrows(RegleGestionException.class,
                () -> affectations.affecter(grattoir, camionB, 3));

        assertTrue(erreur.getMessage().contains("2 disponible"),
                "le message doit indiquer ce qui reste : " + erreur.getMessage());
        assertEquals(2, affectations.stockDisponible(grattoir));
    }

    @Test
    @DisplayName("RG9 — le disponible est le stock total moins ce qui est chargé")
    void disponibleEgalTotalMoinsAffecte() throws Exception {
        stock.acheter(grattoir, 10, "test");
        affectations.affecter(grattoir, camionA, 4);
        affectations.affecter(grattoir, camionB, 1);

        assertEquals(5, affectations.stockDisponible(grattoir));
    }

    @Test
    @DisplayName("RG29 — recharger le même matériel dans le même véhicule cumule les quantités")
    void rechargerCumule() throws Exception {
        stock.acheter(grattoir, 10, "test");
        affectations.affecter(grattoir, camionA, 3);
        affectations.affecter(grattoir, camionA, 4);

        List<Affectation> enCours = affectations.listerEnCours();

        assertEquals(1, enCours.size(), "RG16 : une seule ligne active par couple");
        assertEquals(7, enCours.get(0).getQuantite());
    }

    @Test
    @DisplayName("RG15 — retirer clôture l'affectation et rend le stock disponible")
    void retirerRendLeStock() throws Exception {
        stock.acheter(grattoir, 4, "test");
        affectations.affecter(grattoir, camionA, 4);
        assertEquals(0, affectations.stockDisponible(grattoir));

        Affectation chargee = affectations.listerEnCours().get(0);
        affectations.retirer(chargee);

        assertTrue(affectations.listerEnCours().isEmpty());
        assertEquals(4, affectations.stockDisponible(grattoir));
    }

    @Test
    @DisplayName("Déplacer transfère le matériel sans changer le disponible")
    void deplacerTransfere() throws Exception {
        stock.acheter(grattoir, 10, "test");
        affectations.affecter(grattoir, camionA, 4);

        affectations.deplacer(affectations.listerEnCours().get(0), camionB);

        List<Affectation> enCours = affectations.listerEnCours();
        assertEquals(1, enCours.size());
        assertEquals(camionB, enCours.get(0).getVehicule());
        assertEquals(4, enCours.get(0).getQuantite());
        assertEquals(6, affectations.stockDisponible(grattoir));
    }

    @Test
    @DisplayName("RG16 — déplacer vers un véhicule qui a déjà ce matériel est refusé")
    void deplacerVersVehiculeDejaEquipeRefuse() throws Exception {
        stock.acheter(grattoir, 10, "test");
        affectations.affecter(grattoir, camionA, 2);
        affectations.affecter(grattoir, camionB, 3);

        Affectation dansA = affectations.listerEnCours().stream()
                .filter(a -> a.getVehicule().equals(camionA))
                .findFirst()
                .orElseThrow();

        assertThrows(RegleGestionException.class,
                () -> affectations.deplacer(dansA, camionB));
    }

    @Test
    @DisplayName("RG12 — une quantité nulle ou négative est refusée")
    void quantiteNulleRefusee() throws Exception {
        stock.acheter(grattoir, 10, "test");

        assertThrows(RegleGestionException.class,
                () -> affectations.affecter(grattoir, camionA, 0));
        assertThrows(RegleGestionException.class,
                () -> affectations.affecter(grattoir, camionA, -2));
    }
}
