package fr.taevie.parc.ui;

import fr.taevie.parc.model.Affectation;
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
    private final JTable tableau = new JTable(modele);

    public FenetrePrincipale() {
        setTitle("Parc véhicules — matériel embarqué");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableau.setRowHeight(28);
        tableau.setAutoCreateRowSorter(true);
        tableau.setShowVerticalLines(false);
        tableau.setIntercellSpacing(new Dimension(0, 0));

        JButton rafraichir = new JButton("Rafraîchir");
        rafraichir.addActionListener(e -> charger());

        JButton charger = new JButton("Charger du matériel");
        charger.addActionListener(e -> ouvrirDialogue());

        JButton retirer = new JButton("Retirer");
        retirer.addActionListener(e -> retirerSelection());

        JButton stock = new JButton("Stock");
        stock.addActionListener(e -> {
            new FenetreStock(this).setVisible(true);
            charger();   // les affectations n'ont pas changé, mais le disponible si
        });

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barre.add(rafraichir);
        barre.add(charger);
        barre.add(retirer);
        barre.add(stock);

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

    /** Ouvre la boîte de chargement, puis rafraîchit si l'utilisateur a validé. */
    private void ouvrirDialogue() {
        DialogueAffectation dialogue = new DialogueAffectation(this);
        dialogue.setVisible(true);          // bloque ici tant qu'elle est ouverte

        if (dialogue.estValide()) {
            charger();
        }
    }

    /** Clôture l'affectation sélectionnée dans le tableau. */
    private void retirerSelection() {
        int ligne = tableau.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne dans le tableau.");
            return;
        }

        // le tri réordonne l'affichage : il faut convertir vers l'index du modèle
        Affectation affectation = modele.getAffectation(tableau.convertRowIndexToModel(ligne));

        int reponse = JOptionPane.showConfirmDialog(this,
                "Retirer " + affectation.getQuantite() + " × " + affectation.getMateriel()
                        + " du véhicule " + affectation.getVehicule() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (reponse != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.retirer(affectation);
            charger();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }
}