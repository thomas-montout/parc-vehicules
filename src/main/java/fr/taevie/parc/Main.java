package fr.taevie.parc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) throws IOException {
        String url = "jdbc:sqlite:parc.db";

        InputStream in = Main.class.getResourceAsStream("/schema.sql");
        String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(sql);

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("Connexion réussie !");
            System.out.println("Base : " + conn.getMetaData().getURL());
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tables créées !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}