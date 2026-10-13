package fr.taevie.parc.dao;

import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.LigneStock;
import fr.taevie.parc.model.Materiel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecture du stock de tous les matériels en une seule requête.
 */
public class StockDao {

    public List<LigneStock> findAll() throws SQLException {
        String sql = """
                SELECT m.id_materiel, m.variante, m.ordre, m.unite,
                       f.id_famille, f.libelle AS famille_libelle,
                       g.id_gamme,   g.libelle AS gamme_libelle,
                       COALESCE((SELECT SUM(s.quantite) FROM mouvement_stock s
                                 WHERE s.id_materiel = m.id_materiel), 0) AS total,
                       COALESCE((SELECT SUM(a.quantite) FROM affectation a
                                 WHERE a.id_materiel = m.id_materiel
                                   AND a.date_fin IS NULL), 0) AS affecte
                FROM materiel m
                JOIN famille f ON f.id_famille = m.id_famille
                JOIN gamme   g ON g.id_gamme   = m.id_gamme
                ORDER BY f.libelle, m.ordre
                """;

        List<LigneStock> lignes = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Famille famille = new Famille(
                        rs.getInt("id_famille"), rs.getString("famille_libelle"));
                Gamme gamme = new Gamme(
                        rs.getInt("id_gamme"), rs.getString("gamme_libelle"));

                Materiel materiel = new Materiel(
                        rs.getInt("id_materiel"), famille, gamme,
                        rs.getString("variante"), rs.getInt("ordre"), rs.getString("unite"));

                lignes.add(new LigneStock(
                        materiel, rs.getInt("total"), rs.getInt("affecte")));
            }
        }

        return lignes;
    }
}