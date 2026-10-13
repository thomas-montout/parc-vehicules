package fr.taevie.parc.ui;

import fr.taevie.parc.service.AffectationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.*;
import java.sql.SQLException;

/**
 * Fenêtre principale : ce qui est actuellement chargé dans chaque véhicule.
 * Elle ne connaît que la couche service — aucun DAO, aucun SQL.
 */
public class FenetrePrincipale extends JFrame {

    private final AffectationService service = new AffectationService();
    private final AffectationTableModel modele = new AffectationTableModel();

    public FenetrePrincipale() {
        setTitle("Parc véhicules — matériel embarqué");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTable tableau = new JTable(modele);
        tableau.setRowHeight(28);
        tableau.setAutoCreateRowSorter(true);
        tableau.setShowVerticalLines(false);
        tableau.setIntercellSpacing(new Dimension(0, 0));

        JButton rafraichir = new JButton("Rafraîchir");
        rafraichir.addActionListener(e -> charger());

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barre.add(rafraichir);

        JScrollPane defilement = new JScrollPane(tableau);
        defilement.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        add(barre, BorderLayout.NORTH);
        add(defilement, BorderLayout.CENTER);

        charger();
    }

    /** Interroge le service et met le tableau à jour. */
    private void charger() {
        try {
            modele.setAffectations(service.listerEnCours());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de lire les affectations : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}