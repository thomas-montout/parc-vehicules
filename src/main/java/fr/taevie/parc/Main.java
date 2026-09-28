package fr.taevie.parc;

import fr.taevie.parc.dao.*;
import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;

public class Main {

    public static void main(String[] args) throws Exception {
        Database.initSchema();

        FamilleDao familleDao = new FamilleDao();
        GammeDao gammeDao = new GammeDao();
        MaterielDao materielDao = new MaterielDao();

        Famille mouilleur = new Famille("Mouilleur");
        familleDao.insert(mouilleur);

        Gamme standard = gammeDao.findByLibelle("Standard");

        materielDao.insert(new Materiel(mouilleur, standard, "35 cm"));
        materielDao.insert(new Materiel(mouilleur, standard, "45 cm"));

        for (Materiel m : materielDao.findAll()) {
            System.out.println(m.getId() + " - " + m);
        }
        System.out.println("Base prête !");
    }
}