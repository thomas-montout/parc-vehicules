package fr.taevie.parc;

import fr.taevie.parc.dao.Database;
import fr.taevie.parc.dao.VehiculeDao;
import fr.taevie.parc.model.Vehicule;

public class Main {

    public static void main(String[] args) throws Exception {
        Database.initSchema();

        VehiculeDao dao = new VehiculeDao();
        dao.insert(new Vehicule("125B69C"));
        dao.insert(new Vehicule("128N64D"));

        for (Vehicule v : dao.findAll()) {
            System.out.println(v.getId() + " - " + v);
        }
        System.out.println("Base prête !");
    }
}