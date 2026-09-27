package fr.taevie.parc.dao;

import fr.taevie.parc.model.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDao {

    public int insert(Vehicule vehicule) throws SQLException {

        String sql = "INSERT INTO vehicule (immatriculation, marque, modele) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    vehicule.setId(id);
                    return id;
                }
            }
            throw new SQLException("Aucun id généré pour le véhicule");
        }

    }

    public List<Vehicule> findAll() throws SQLException {
        String sql = "SELECT id_vehicule, immatriculation, marque, modele FROM vehicule ORDER BY immatriculation";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_vehicule");
                String immatriculation = rs.getString("immatriculation");
                String marque = rs.getString("marque");
                String modele = rs.getString("modele");
                vehicules.add(new Vehicule(id, immatriculation, marque, modele));
            }
        }
        return vehicules;
    }
}
