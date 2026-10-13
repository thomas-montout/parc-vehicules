import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import fr.taevie.parc.dao.*;
import fr.taevie.parc.model.*;
import fr.taevie.parc.service.ImportService;
import fr.taevie.parc.ui.FenetrePrincipale;

import javax.swing.*;

public static void main(String[] args) throws Exception {
    Database.initSchema();

    if (new MaterielDao().findAll().isEmpty()) {
        ImportService importService = new ImportService();
        importService.importerVehicules("/vehicules.csv");
        importService.importerMateriels("/materiels.csv");
    }

    FlatDarkLaf.setup();

    SwingUtilities.invokeLater(() -> new FenetrePrincipale().setVisible(true));
    SwingUtilities.invokeLater(() -> new FenetrePrincipale().setVisible(true));
}