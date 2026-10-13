package fr.taevie.parc.service;

import fr.taevie.parc.dao.Database;
import fr.taevie.parc.dao.FamilleDao;
import fr.taevie.parc.dao.GammeDao;
import fr.taevie.parc.dao.MaterielDao;
import fr.taevie.parc.dao.VehiculeDao;
import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Socle commun des tests : chaque test travaille sur une base SQLite neuve,
 * créée dans un dossier temporaire et supprimée ensuite.
 * <p>
 * Les vraies données (parc.db) ne sont jamais touchées : la propriété système
 * {@link Database#PROPRIETE_CHEMIN} redirige toutes les connexions.
 */
abstract class BaseDeTest {

    /** Dossier temporaire fourni par JUnit, différent pour chaque test. */
    @TempDir
    Path dossier;

    @BeforeEach
    void creerBaseVide() throws Exception {
        System.setProperty(Database.PROPRIETE_CHEMIN, dossier.resolve("test.db").toString());
        Database.initSchema();
    }

    @AfterEach
    void oublierBase() {
        System.clearProperty(Database.PROPRIETE_CHEMIN);
    }

    // ------------------------------------------------- fabriques de données

    /** Crée et enregistre un matériel de gamme Standard. */
    protected Materiel materiel(String famille, String variante) throws SQLException {
        Famille f = new Famille(famille);
        new FamilleDao().insert(f);

        Gamme standard = new GammeDao().findByLibelle("Standard");

        Materiel m = new Materiel(f, standard, variante);
        new MaterielDao().insert(m);
        return m;
    }

    /** Crée et enregistre un véhicule. */
    protected Vehicule vehicule(String immatriculation) throws SQLException {
        Vehicule v = new Vehicule(immatriculation);
        new VehiculeDao().insert(v);
        return v;
    }
}
