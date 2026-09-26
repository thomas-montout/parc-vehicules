package fr.taevie.parc;

import fr.taevie.parc.dao.Database;

public class Main {

    public static void main(String[] args) throws Exception {
        Database.initSchema();
        System.out.println("Base prête !");
    }
}