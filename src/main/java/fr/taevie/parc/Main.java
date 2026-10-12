import fr.taevie.parc.dao.*;
import fr.taevie.parc.model.*;

public static void main(String[] args) throws Exception {
    Database.initSchema();

    FamilleDao familleDao = new FamilleDao();
    GammeDao gammeDao = new GammeDao();
    MaterielDao materielDao = new MaterielDao();
    VehiculeDao vehiculeDao = new VehiculeDao();
    AffectationDao affectationDao = new AffectationDao();

    Famille mouilleur = new Famille("Mouilleur");
    familleDao.insert(mouilleur);

    Gamme standard = gammeDao.findByLibelle("Standard");

    Materiel m35 = new Materiel(mouilleur, standard, "35 cm");
    materielDao.insert(m35);

    Vehicule kangoo = new Vehicule("AB-123-CD");
    vehiculeDao.insert(kangoo);

    affectationDao.insert(new Affectation(m35, kangoo, 3, LocalDate.now()));

    for (Affectation a : affectationDao.findEnCours()) {
        System.out.println(a.getVehicule() + " : " + a.getQuantite() + " × " + a.getMateriel());
    }
}