package fr.taevie.parc;

import com.formdev.flatlaf.FlatDarkLaf;
import fr.taevie.parc.dao.Database;
import fr.taevie.parc.dao.MaterielDao;
import fr.taevie.parc.service.ImportService;
import fr.taevie.parc.ui.FenetrePrincipale;

import javax.swing.SwingUtilities;

/**
 * Point d'entrée de l'application ISO Stock.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Database.initSchema();

        // premier lancement : on charge le parc depuis les CSV embarqués
        if (new MaterielDao().findAll().isEmpty()) {
            ImportService importService = new ImportService();
            importService.importerVehicules("/vehicules.csv");
            importService.importerMateriels("/materiels.csv");
        }

        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> new FenetrePrincipale().setVisible(true));
    }
}