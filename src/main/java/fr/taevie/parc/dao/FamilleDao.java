package fr.taevie.parc.dao;

import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès à la table famille.
 */
public class FamilleDao {

    /**
     * Insère une nouvelle famille et lui affecte l'id généré par la base.
     *
     * @param famille la famille à enregistrer
     * @return l'id attribué par la base
     * @throws SQLException si l'insertion échoue (libellé en doublon par exemple)
     */
    public int insert(Famille famille) throws SQLException {
        String sql = "INSERT INTO famille (libelle) VALUES (?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, famille.getLibelle());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    famille.setId(id);
                    return id;
                }
            }
            throw new SQLException("Aucun id généré pour la famille");
        }
    }

    /**
     * Renvoie toutes les familles, triées par libellé.
     *
     * @return la liste des familles, vide si la table ne contient rien
     * @throws SQLException si la lecture échoue
     */
    public List<Famille> findAll() throws SQLException {
        String sql = "SELECT id_famille, libelle FROM famille ORDER BY libelle";
        List<Famille> familles = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_famille");
                String libelle = rs.getString("libelle");
                familles.add(new Famille(id, libelle));
            }
        }

        return familles;
    }
}