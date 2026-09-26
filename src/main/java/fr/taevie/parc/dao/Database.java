package fr.taevie.parc.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Point d'accès unique à la base SQLite du parc.
 * Toutes les classes du package dao passent par ici pour obtenir une connexion.
 */
public class Database {

    /** Emplacement du fichier de base, relatif au dossier de lancement. */
    private static final String URL = "jdbc:sqlite:parc.db";

    /**
     * Ouvre une connexion vers la base et active les clés étrangères.
     * <p>
     * La connexion n'est pas fermée ici : c'est à l'appelant de le faire,
     * idéalement dans un try-with-resources.
     *
     * @return une connexion ouverte
     * @throws SQLException si la base est inaccessible
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /**
     * Crée les tables si elles n'existent pas encore, en exécutant schema.sql.
     * Le script étant écrit avec IF NOT EXISTS, cette méthode peut être
     * appelée à chaque démarrage sans risque.
     *
     * @throws IOException  si schema.sql est introuvable ou illisible
     * @throws SQLException si le script échoue
     */
    public static void initSchema() throws IOException, SQLException {
        String sql = lireScript("/schema.sql");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Lit un fichier texte placé dans src/main/resources.
     *
     * @param chemin chemin de la ressource, avec un / initial
     * @return le contenu du fichier
     * @throws IOException si la ressource est absente ou illisible
     */
    private static String lireScript(String chemin) throws IOException {
        try (InputStream in = Database.class.getResourceAsStream(chemin)) {
            if (in == null) {
                throw new IOException("Ressource introuvable : " + chemin);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}