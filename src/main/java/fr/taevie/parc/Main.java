import fr.taevie.parc.dao.*;
import fr.taevie.parc.model.*;
import fr.taevie.parc.service.AffectationService;
import fr.taevie.parc.service.ImportService;
import fr.taevie.parc.service.MouvementStockService;
import fr.taevie.parc.service.RegleGestionException;

public static void main(String[] args) throws Exception {
    Database.initSchema();

// recharger les données de base
    ImportService importService = new ImportService();
    importService.importerVehicules("/vehicules.csv");
    importService.importerMateriels("/materiels.csv");

    MaterielDao materielDao = new MaterielDao();
    VehiculeDao vehiculeDao = new VehiculeDao();
    MouvementStockService stockService = new MouvementStockService();
    AffectationService affectationService = new AffectationService();

// on prend le premier matériel et le premier véhicule de la base
    Materiel materiel = materielDao.findAll().get(0);
    Vehicule vehicule = vehiculeDao.findAll().get(0);

// l'entreprise achète 10 exemplaires
    stockService.acheter(materiel, 10, "Stock initial");
    System.out.println("Stock total : " + stockService.stockTotal(materiel));

// on en charge 4 dans le véhicule : ça doit passer
    affectationService.affecter(materiel, vehicule, 4);
    System.out.println("Disponible après chargement : " + affectationService.stockDisponible(materiel));

// on essaie d'en charger 15 : ça doit être refusé
    try {
        affectationService.affecter(materiel, vehicule, 15);
    } catch (RegleGestionException e) {
        System.out.println("Refusé : " + e.getMessage());
    }
}