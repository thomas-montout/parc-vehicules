package fr.taevie.parc.service;

import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.MouvementStock;
import fr.taevie.parc.model.Vehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Règles de gestion du stock : RG7, RG19, RG20, RG21.
 */
class MouvementStockServiceTest extends BaseDeTest {

    private final MouvementStockService stock = new MouvementStockService();
    private final AffectationService affectations = new AffectationService();

    private Materiel mouilleur;
    private Vehicule camion;

    @BeforeEach
    void creerDonnees() throws Exception {
        mouilleur = materiel("Mouilleur", "35 cm");
        camion = vehicule("CC-333-CC");
    }

    @Test
    @DisplayName("RG7 — le stock total est la somme des mouvements")
    void totalEgalSommeDesMouvements() throws Exception {
        stock.acheter(mouilleur, 10, "commande");
        stock.sortir(mouilleur, MouvementStockService.CASSE, 3, "lames tordues");
        stock.acheter(mouilleur, 2, "réassort");

        assertEquals(9, stock.stockTotal(mouilleur));
    }

    @Test
    @DisplayName("RG21 — on ne peut pas déclarer en casse du matériel encore dans un véhicule")
    void sortieRefuseeSiMaterielCharge() throws Exception {
        stock.acheter(mouilleur, 5, "commande");
        affectations.affecter(mouilleur, camion, 4);

        // 5 - 2 = 3, or 4 sont encore dans le camion
        assertThrows(RegleGestionException.class,
                () -> stock.sortir(mouilleur, MouvementStockService.CASSE, 2, "test"));

        // 5 - 1 = 4 : exactement ce qui est chargé, accepté
        stock.sortir(mouilleur, MouvementStockService.CASSE, 1, "test");
        assertEquals(4, stock.stockTotal(mouilleur));
    }

    @Test
    @DisplayName("RG20 — un inventaire enregistre l'écart, sans modifier l'historique")
    void inventaireEnregistreLEcart() throws Exception {
        stock.acheter(mouilleur, 10, "commande");
        stock.ajuster(mouilleur, 8, "inventaire");

        assertEquals(8, stock.stockTotal(mouilleur));

        List<MouvementStock> historique = stock.historique(mouilleur);
        assertEquals(2, historique.size(), "l'achat initial est conservé");
        assertTrue(historique.stream().anyMatch(m ->
                        MouvementStockService.AJUSTEMENT.equals(m.getType()) && m.getQuantite() == -2),
                "un mouvement d'ajustement de -2 doit exister");
    }

    @Test
    @DisplayName("RG20 — un inventaire ne peut pas descendre sous ce qui est chargé")
    void inventaireSousLAffecteRefuse() throws Exception {
        stock.acheter(mouilleur, 10, "commande");
        affectations.affecter(mouilleur, camion, 6);

        assertThrows(RegleGestionException.class,
                () -> stock.ajuster(mouilleur, 5, "inventaire"));
    }

    @Test
    @DisplayName("RG19 — seuls la casse et la perte sont des sorties valides")
    void typeDeSortieInconnuRefuse() throws Exception {
        stock.acheter(mouilleur, 10, "commande");

        assertThrows(RegleGestionException.class,
                () -> stock.sortir(mouilleur, "VOL", 1, "test"));
    }

    @Test
    @DisplayName("Un achat d'une quantité nulle ou négative est refusé")
    void achatNegatifRefuse() {
        assertThrows(RegleGestionException.class,
                () -> stock.acheter(mouilleur, 0, "test"));
        assertThrows(RegleGestionException.class,
                () -> stock.acheter(mouilleur, -3, "test"));
    }
}
