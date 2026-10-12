import fr.taevie.parc.dao.*;
import fr.taevie.parc.model.*;
import fr.taevie.parc.service.ImportService;

public static void main(String[] args) throws Exception {
    Database.initSchema();

    ImportService importService = new ImportService();
    System.out.println(importService.importerVehicules("/vehicules.csv") + " véhicules");
    System.out.println(importService.importerMateriels("/materiels.csv") + " matériels");
}