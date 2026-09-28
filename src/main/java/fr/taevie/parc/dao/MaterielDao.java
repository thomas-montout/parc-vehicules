package fr.taevie.parc.dao;

import fr.taevie.parc.model.Famille;
import fr.taevie.parc.model.Gamme;
import fr.taevie.parc.model.Materiel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterielDao {

    public List<Materiel> findAll() throws SQLException {

        String sql = """
                SELECT m.id_materiel, m.variante, m.ordre ,m.unite,
                       f.id_famille, f.libelle AS famille_libelle,
                       g.id_gamme, g.libelle AS gamme_libelle
                FROM materiel m
                JOIN famille f ON f.id_famille = m.id_famille
                JOIN gamme g ON g.id_gamme = m.id_gamme
                ORDER BY f.libelle, m.ordre
                """;

        List<Materiel> materiels = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

                 while (rs.next()) {
                     int id = rs.getInt("id_materiel");
                     String variante = rs.getString("variante");
                     int ordre = rs.getInt("ordre");
                     String unite = rs.getString("unite");
                     int familleId = rs.getInt("id_famille");
                     String familleLibelle = rs.getString("famille_libelle");
                     int gammeId = rs.getInt("id_gamme");
                     String gammeLibelle = rs.getString("gamme_libelle");
                     Famille famille = new Famille(familleId, familleLibelle);
                     Gamme gamme = new Gamme(gammeId, gammeLibelle);
                     materiels.add(new Materiel(id, famille, gamme, variante, ordre, unite));
                 }
        }
        return materiels;
    }

    public int insert(Materiel materiel) throws SQLException {

        String sql = "INSERT INTO materiel (id_famille, id_gamme, variante, ordre, unite) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, materiel.getFamille().getId());
            stmt.setInt(2, materiel.getGamme().getId());
            stmt.setString(3, materiel.getVariante());
            stmt.setInt(4, materiel.getOrdre());
            stmt.setString(5, materiel.getUnite());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    materiel.setId(id);
                    return id;
                }
            }
            throw new SQLException("Aucun id généré pour le matériel");
        }
    }
}
