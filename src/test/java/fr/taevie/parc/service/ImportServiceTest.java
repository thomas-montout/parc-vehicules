package fr.taevie.parc.service;

import fr.taevie.parc.dao.GammeDao;
import fr.taevie.parc.dao.MaterielDao;
import fr.taevie.parc.dao.VehiculeDao;
import fr.taevie.parc.model.Gamme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Import initial depuis les CSV embarqués dans l'application.
 */
class ImportServiceTest extends BaseDeTest {

    private final ImportService importService = new ImportService();

    @Test
    @DisplayName("L'import charge tout le parc du client")
    void importChargeToutLeParc() throws Exception {
        int vehicules = importService.importerVehicules("/vehicules.csv");
        int materiels = importService.importerMateriels("/materiels.csv");

        assertEquals(28, vehicules);
        assertEquals(81, materiels);
        assertEquals(28, new VehiculeDao().findAll().size());
        assertEquals(81, new MaterielDao().findAll().size());
    }

    @Test
    @DisplayName("Les familles et gammes répétées ne sont créées qu'une fois")
    void gammesCreeesUneSeuleFois() throws Exception {
        importService.importerMateriels("/materiels.csv");

        List<String> gammes = new GammeDao().findAll().stream()
                .map(Gamme::getLibelle)
                .toList();

        // Standard vient du schéma, PAD et UNGER de l'import
        assertEquals(List.of("PAD", "Standard", "UNGER"), gammes);
    }
}
