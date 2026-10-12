package fr.taevie.parc.dao;

import fr.taevie.parc.model.Composition;
import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès à la table composition : de quoi un kit est fait.
 */
public class CompositionDao {

    /**
     * Ajoute un composant à un kit. Pas de clé générée : la clé primaire
     * est le couple (kit, composant).
     */
    public void insert(Composition composition) throws SQLException {
        String sql = "INSERT INTO composition (id_kit, id_composant, quantite) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, composition.getKit().getId());
            stmt.setInt(2, composition.getComposant().getId());
            stmt.setInt(3, composition.getQuantite());
            stmt.executeUpdate();
        }
    }

    /**
     * Composants d'un kit donné.
     */
    public List<Composition> findByKit(Materiel kit) throws SQLException {
        String sql = """
                SELECT c.quantite,
                       m.id_materiel, m.variante, m.ordre, m.unite,
                       f.id_famille, f.libelle AS famille_libelle,
                       g.id_gamme,   g.libelle AS gamme_libelle
                FROM composition c
                JOIN materiel m ON m.id_materiel = c.id_composant
                JOIN famille  f ON f.id_famille  = m.id_famille
                JOIN gamme    g ON g.id_gamme    = m.id_gamme
                WHERE c.id_kit = ?
                ORDER BY f.libelle, m.ordre
                """;

        List<Composition> compositions = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kit.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Famille famille = new Famille(
                            rs.getInt("id_famille"),
                            rs.getString("famille_libelle"));

                    Gamme gamme = new Gamme(
                            rs.getInt("id_gamme"),
                            rs.getString("gamme_libelle"));

                    Materiel composant = new Materiel(
                            rs.getInt("id_materiel"),
                            famille,
                            gamme,
                            rs.getString("variante"),
                            rs.getInt("ordre"),
                            rs.getString("unite"));

                    compositions.add(new Composition(kit, composant, rs.getInt("quantite")));
                }
            }
        }

        return compositions;
    }

    /**
     * Retire un composant d'un kit.
     */
    public void delete(int idKit, int idComposant) throws SQLException {
        String sql = "DELETE FROM composition WHERE id_kit = ? AND id_composant = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idKit);
            stmt.setInt(2, idComposant);
            stmt.executeUpdate();
        }
    }
}