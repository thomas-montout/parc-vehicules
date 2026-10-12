package fr.taevie.parc.dao;

import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.MouvementStock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès à la table mouvement_stock.
 */
public class MouvementStockDao {

    public int insert(MouvementStock mouvement) throws SQLException {
        String sql = """
                INSERT INTO mouvement_stock (id_materiel, type_mouvement, quantite,
                                             date_mouvement, commentaire)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mouvement.getMateriel().getId());
            stmt.setString(2, mouvement.getType());
            stmt.setInt(3, mouvement.getQuantite());
            stmt.setString(4, mouvement.getDate().toString());
            stmt.setString(5, mouvement.getCommentaire());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    mouvement.setId(id);
                    return id;
                }
            }
            throw new SQLException("Aucun id généré pour le mouvement de stock");
        }
    }

    /**
     * Mouvements d'un matériel, du plus récent au plus ancien.
     */
    public List<MouvementStock> findByMateriel(int idMateriel) throws SQLException {
        String sql = """
                SELECT s.id_mouvement, s.type_mouvement, s.quantite,
                       s.date_mouvement, s.commentaire,
                       m.id_materiel, m.variante, m.ordre, m.unite,
                       f.id_famille, f.libelle AS famille_libelle,
                       g.id_gamme,   g.libelle AS gamme_libelle
                FROM mouvement_stock s
                JOIN materiel m ON m.id_materiel = s.id_materiel
                JOIN famille  f ON f.id_famille  = m.id_famille
                JOIN gamme    g ON g.id_gamme    = m.id_gamme
                WHERE s.id_materiel = ?
                ORDER BY s.date_mouvement DESC
                """;

        List<MouvementStock> mouvements = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMateriel);

            try (ResultSet rs = stmt.executeQuery()) {
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

                    mouvements.add(new MouvementStock(
                            rs.getInt("id_mouvement"),
                            materiel,
                            rs.getString("type_mouvement"),
                            rs.getInt("quantite"),
                            LocalDate.parse(rs.getString("date_mouvement")),
                            rs.getString("commentaire")));
                }
            }
        }

        return mouvements;
    }

    /**
     * Stock total d'un matériel : la somme de ses mouvements (RG7).
     *
     * @return la quantité possédée, 0 si aucun mouvement enregistré
     */
    public int stockTotal(int idMateriel) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantite), 0) FROM mouvement_stock WHERE id_materiel = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMateriel);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}