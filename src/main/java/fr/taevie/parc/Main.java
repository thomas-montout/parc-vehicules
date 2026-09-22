package fr.taevie.parc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:parc.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("Connexion réussie !");
            System.out.println("Base : " + conn.getMetaData().getURL());
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}