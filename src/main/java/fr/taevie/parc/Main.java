package fr.taevie.parc;

import fr.taevie.parc.dao.Database;
import fr.taevie.parc.dao.FamilleDao;
import fr.taevie.parc.model.Famille;

public class Main {

    public static void main(String[] args) throws Exception {
        Database.initSchema();

        FamilleDao dao = new FamilleDao();
        dao.insert(new Famille("Grattoir"));
        dao.insert(new Famille("Mouilleur"));

        for (Famille f : dao.findAll()) {
            System.out.println(f.getId() + " - " + f);
        }
        System.out.println("Base prête !");
    }
}