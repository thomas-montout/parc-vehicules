package fr.taevie.parc.dao;

import fr.taevie.parc.model.Gamme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GammeDao {

    public int insert(Gamme gamme) throws SQLException {
        String sql = "INSERT INTO gamme (libelle) VALUES (?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, gamme.getLibelle());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    gamme.setId(id);
                    return id;
                }
            }
            throw new SQLException("Aucun id généré pour la gamme");
        }
    }

    public List<Gamme> findAll() throws SQLException {
        String sql = "SELECT id_gamme, libelle FROM gamme ORDER BY libelle";
        List<Gamme> gammes = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_gamme");
                String libelle = rs.getString("libelle");
                gammes.add(new Gamme(id, libelle));
            }
        }

        return gammes;
    }


    public Gamme findByLibelle(String libelle) throws SQLException {
        String sql = "SELECT id_gamme, libelle FROM gamme WHERE libelle = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libelle);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_gamme");
                    String gamme = rs.getString("libelle");
                    return new Gamme(id, gamme);
                }
            }
        }
        return null;
    }
}