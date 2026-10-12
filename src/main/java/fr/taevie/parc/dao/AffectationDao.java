package fr.taevie.parc.dao;

import fr.taevie.parc.model.Affectation;
import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès à la table affectation : quel matériel se trouve dans quel véhicule.
 */
public class AffectationDao {

    /**
     * Enregistre une nouvelle affectation.
     *
     * @param affectation l'affectation à créer
     * @return l'id attribué par la base
     * @throws SQLException si une affectation en cours existe déjà pour ce couple (RG16)
     */
    public int insert(Affectation affectation) throws SQLException {
        String sql = """
                INSERT INTO affectation (id_materiel, id_vehicule, quantite, date_debut, date_fin)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, affectation.getMateriel().getId());
            stmt.setInt(2, affectation.getVehicule().getId());
            stmt.setInt(3, affectation.getQuantite());
            stmt.setString(4, affectation.getDateDebut().toString());

            LocalDate dateFin = affectation.getDateFin();
            stmt.setString(5, dateFin == null ? null : dateFin.toString());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    affectation.setId(id);
                    return id;
                }
            }
            throw new SQLException("Aucun id généré pour l'affectation");
        }
    }

    /**
     * Renvoie les affectations en cours, c'est-à-dire celles dont la date de fin
     * est vide (RG13). C'est la requête qui répond à « quel matériel dans quel véhicule ».
     *
     * @return la liste des affectations actives, triée par véhicule puis par famille
     * @throws SQLException si la lecture échoue
     */
    public List<Affectation> findEnCours() throws SQLException {
        String sql = """
                SELECT a.id_affectation, a.quantite, a.date_debut, a.date_fin,
                       m.id_materiel, m.variante, m.ordre, m.unite,
                       f.id_famille, f.libelle AS famille_libelle,
                       g.id_gamme,   g.libelle AS gamme_libelle,
                       v.id_vehicule, v.immatriculation, v.marque, v.modele
                FROM affectation a
                JOIN materiel m ON m.id_materiel = a.id_materiel
                JOIN famille  f ON f.id_famille  = m.id_famille
                JOIN gamme    g ON g.id_gamme    = m.id_gamme
                JOIN vehicule v ON v.id_vehicule = a.id_vehicule
                WHERE a.date_fin IS NULL
                ORDER BY v.immatriculation, f.libelle, m.ordre
                """;

        List<Affectation> affectations = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Famille famille = new Famille(
                        rs.getInt("id_famille"),
                        rs.getString("famille_libelle"));

                Gamme gamme = new Gamme(
                        rs.getInt("id_gamme"),
                        rs.getString("gamme_libelle"));

                Materiel materiel = new Materiel(
                        rs.getInt("id_materiel"),
                        famille,
                        gamme,
                        rs.getString("variante"),
                        rs.getInt("ordre"),
                        rs.getString("unite"));

                Vehicule vehicule = new Vehicule(
                        rs.getInt("id_vehicule"),
                        rs.getString("immatriculation"),
                        rs.getString("marque"),
                        rs.getString("modele"));

                String texteFin = rs.getString("date_fin");

                affectations.add(new Affectation(
                        rs.getInt("id_affectation"),
                        materiel,
                        vehicule,
                        rs.getInt("quantite"),
                        LocalDate.parse(rs.getString("date_debut")),
                        texteFin == null ? null : LocalDate.parse(texteFin)));
            }
        }

        return affectations;
    }

    /**
     * Clôture une affectation en cours au lieu de la supprimer (RG15).
     *
     * @param idAffectation l'affectation à clôturer
     * @param dateFin       date de retrait du matériel
     * @throws SQLException si la mise à jour échoue
     */
    public void cloturer(int idAffectation, LocalDate dateFin) throws SQLException {
        String sql = "UPDATE affectation SET date_fin = ? WHERE id_affectation = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dateFin.toString());
            stmt.setInt(2, idAffectation);
            stmt.executeUpdate();
        }
    }
}