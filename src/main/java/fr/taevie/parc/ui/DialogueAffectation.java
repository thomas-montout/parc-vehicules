package fr.taevie.parc.ui;

import fr.taevie.parc.dao.MaterielDao;
import fr.taevie.parc.model.Materiel;
import fr.taevie.parc.model.Vehicule;
import fr.taevie.parc.service.AffectationService;
import fr.taevie.parc.service.RegleGestionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

/**
 * Boîte de dialogue de chargement : choisir un matériel, un véhicule,
 * une quantité. La vérification du stock est faite par le service.
 */
public class DialogueAffectation extends JDialog {

    private final AffectationService service = new AffectationService();

    private final JComboBox<Materiel> listeMateriels = new JComboBox<>();
    private final JComboBox<Vehicule> listeVehicules = new JComboBox<>();
    private final JSpinner quantite = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JLabel disponible = new JLabel(" ");

    /** Passe à true si l'utilisateur a validé, pour que l'appelant rafraîchisse. */
    private boolean valide = false;

    public DialogueAffectation(Frame parent) {
        this(parent, null);
    }

    /**
     * @param parent     la fenêtre principale
     * @param vehicule   le véhicule à présélectionner, ou null pour le premier de la liste
     */
    public DialogueAffectation(Frame parent, Vehicule vehicule) {
        super(parent, "Charger du matériel", true);

        remplirListes();
        if (vehicule != null) {
            // fonctionne grâce à Vehicule.equals : l'objet reçu et celui de la liste
            // sont deux instances différentes du même camion
            listeVehicules.setSelectedItem(vehicule);
        }

        JPanel formulaire = new JPanel(new GridLayout(4, 2, 8, 8));
        formulaire.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        formulaire.add(new JLabel("Matériel"));
        formulaire.add(listeMateriels);
        formulaire.add(new JLabel("Véhicule"));
        formulaire.add(listeVehicules);
        formulaire.add(new JLabel("Quantité"));
        formulaire.add(quantite);
        formulaire.add(new JLabel("Disponible"));
        formulaire.add(disponible);

        JButton valider = new JButton("Charger");
        JButton annuler = new JButton("Annuler");
        valider.addActionListener(e -> charger());
        annuler.addActionListener(e -> dispose());

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 12));
        boutons.add(annuler);
        boutons.add(valider);

        add(formulaire, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);

        // le disponible se met à jour dès qu'on change de matériel
        listeMateriels.addActionListener(e -> majDisponible());
        majDisponible();

        // Entrée valide, Échap annule : les réflexes attendus d'une boîte de dialogue
        getRootPane().setDefaultButton(valider);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean estValide() {
        return valide;
    }

    /** Charge les deux listes déroulantes depuis la base. */
    private void remplirListes() {
        try {
            for (Materiel m : new MaterielDao().findAll()) {
                listeMateriels.addItem(m);
            }
            for (Vehicule v : service.listerVehicules()) {
                listeVehicules.addItem(v);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les listes : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Affiche le stock encore disponible pour le matériel sélectionné. */
    private void majDisponible() {
        Materiel materiel = (Materiel) listeMateriels.getSelectedItem();
        if (materiel == null) {
            return;
        }
        try {
            disponible.setText(service.stockDisponible(materiel) + " exemplaire(s)");
        } catch (SQLException e) {
            disponible.setText("?");
        }
    }

    /** Tente l'affectation et ferme la fenêtre si elle réussit. */
    private void charger() {
        Materiel materiel = (Materiel) listeMateriels.getSelectedItem();
        Vehicule vehicule = (Vehicule) listeVehicules.getSelectedItem();

        if (materiel == null || vehicule == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un matériel et un véhicule.");
            return;
        }

        try {
            service.affecter(materiel, vehicule, (int) quantite.getValue());
            valide = true;
            dispose();

        } catch (RegleGestionException e) {
            // règle métier : message destiné à l'utilisateur
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Impossible", JOptionPane.WARNING_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur technique : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}