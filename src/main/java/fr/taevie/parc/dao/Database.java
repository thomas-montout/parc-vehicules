package fr.taevie.parc.dao;

import java.io.File;
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

    /**
     * Propriété système permettant de choisir un autre fichier de base.
     * Les tests s'en servent pour travailler sur une base temporaire, sans
     * jamais toucher aux vraies données : -Diso.db=C:\chemin\test.db
     */
    public static final String PROPRIETE_CHEMIN = "iso.db";

    /**
     * Adresse JDBC de la base. Par défaut, la base vit dans le même dossier
     * que l'application ; la propriété {@link #PROPRIETE_CHEMIN} la remplace.
     * Recalculée à chaque appel pour que les tests puissent en changer.
     */
    private static String url() {
        String chemin = System.getProperty(PROPRIETE_CHEMIN);
        return "jdbc:sqlite:" + (chemin != null ? chemin : cheminBase());
    }

    /**
     * Chemin absolu de parc.db :
     * <ul>
     *   <li>version .exe (jpackage) : à côté de « ISO Stock.exe » ;</li>
     *   <li>version .jar : à côté du JAR ;</li>
     *   <li>dans l'IDE : dans le dossier courant.</li>
     * </ul>
     */
    private static String cheminBase() {
        // le lanceur .exe fabriqué par jpackage renseigne son propre chemin dans
        // cette propriété ; sans elle, la base finirait dans le sous-dossier « app »
        String exe = System.getProperty("jpackage.app-path");
        if (exe != null) {
            return new File(new File(exe).getAbsoluteFile().getParentFile(), "parc.db").getAbsolutePath();
        }

        try {
            File source = new File(Database.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File dossier = source.isFile() ? source.getParentFile() : new File(".");
            return new File(dossier, "parc.db").getAbsolutePath();
        } catch (Exception e) {
            return "parc.db";   // repli : dossier courant
        }
    }

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
        Connection conn = DriverManager.getConnection(url());
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