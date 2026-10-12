package fr.taevie.parc.service;

import fr.taevie.parc.dao.FamilleDao;
import fr.taevie.parc.dao.GammeDao;
import fr.taevie.parc.dao.MaterielDao;
import fr.taevie.parc.dao.VehiculeDao;
import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Chargement initial du parc à partir de fichiers CSV placés dans les ressources.
 * Les fichiers utilisent le point-virgule comme séparateur et comportent une
 * ligne d'en-tête.
 */
public class ImportService {

    private final FamilleDao familleDao = new FamilleDao();
    private final GammeDao gammeDao = new GammeDao();
    private final MaterielDao materielDao = new MaterielDao();
    private final VehiculeDao vehiculeDao = new VehiculeDao();

    /**
     * Importe les matériels : famille;gamme;variante;ordre;unite
     *
     * @param chemin chemin de la ressource, par exemple "/materiels.csv"
     * @return le nombre de matériels insérés
     */
    public int importerMateriels(String chemin) throws IOException, SQLException {
        int insertions = 0;

        for (String[] champs : lireCsv(chemin)) {
            Famille famille = trouverOuCreerFamille(champs[0]);
            Gamme gamme = trouverOuCreerGamme(champs[1]);

            materielDao.insert(new Materiel(
                    famille,
                    gamme,
                    champs[2],
                    Integer.parseInt(champs[3]),
                    champs[4]));

            insertions++;
        }

        return insertions;
    }

    /**
     * Importe les véhicules : immatriculation;marque;modele
     *
     * @param chemin chemin de la ressource, par exemple "/vehicules.csv"
     * @return le nombre de véhicules insérés
     */
    public int importerVehicules(String chemin) throws IOException, SQLException {
        int insertions = 0;

        for (String[] champs : lireCsv(chemin)) {
            vehiculeDao.insert(new Vehicule(champs[0], champs[1], champs[2]));
            insertions++;
        }

        return insertions;
    }

    /**
     * Cherche une famille par son libellé, la crée si elle n'existe pas encore.
     * Sans cela, « Grattoir » serait inséré trois fois et la contrainte UNIQUE
     * ferait échouer l'import dès la deuxième ligne.
     */
    private Famille trouverOuCreerFamille(String libelle) throws SQLException {
        Famille famille = familleDao.findByLibelle(libelle);
        if (famille == null) {
            famille = new Famille(libelle);
            familleDao.insert(famille);
        }
        return famille;
    }

    /** Même principe pour les gammes. */
    private Gamme trouverOuCreerGamme(String libelle) throws SQLException {
        Gamme gamme = gammeDao.findByLibelle(libelle);
        if (gamme == null) {
            gamme = new Gamme(libelle);
            gammeDao.insert(gamme);
        }
        return gamme;
    }

    /**
     * Lit un CSV des ressources et renvoie ses lignes découpées, en-tête exclu.
     * Les lignes vides sont ignorées.
     */
    private java.util.List<String[]> lireCsv(String chemin) throws IOException {
        java.util.List<String[]> lignes = new java.util.ArrayList<>();

        try (InputStream in = ImportService.class.getResourceAsStream(chemin)) {
            if (in == null) {
                throw new IOException("Fichier introuvable : " + chemin);
            }

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

                reader.readLine();   // on saute la ligne d'en-tête

                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    if (ligne.isBlank()) {
                        continue;
                    }
                    lignes.add(ligne.split(";", -1));
                }
            }
        }

        return lignes;
    }
}