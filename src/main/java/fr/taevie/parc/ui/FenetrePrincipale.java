package fr.taevie.parc.ui;

import fr.taevie.parc.dao.VehiculeDao;
import fr.taevie.parc.model.Affectation;
import fr.taevie.parc.model.Vehicule;
import fr.taevie.parc.service.AffectationService;
import fr.taevie.parc.service.RegleGestionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Fenêtre principale : ce qui est actuellement chargé dans chaque véhicule,
 * avec filtrage par véhicule et recherche libre.
 */
public class FenetrePrincipale extends JFrame {

    private static final String TOUS = "Tous les véhicules";

    private final AffectationService service = new AffectationService();
    private final AffectationTableModel modele = new AffectationTableModel();
    private final JTable tableau = new JTable(modele);
    private final TableRowSorter<AffectationTableModel> tri = new TableRowSorter<>(modele);

    private final JComboBox<Object> filtreVehicule = new JComboBox<>();
    private final JTextField recherche = new JTextField(18);
    private final JLabel compteur = new JLabel();

    public FenetrePrincipale() {
        setTitle("ISO Stock");
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableau.setRowSorter(tri);
        tableau.setRowHeight(28);
        tableau.setShowVerticalLines(false);
        tableau.setIntercellSpacing(new Dimension(0, 0));

        add(barreOutils(), BorderLayout.NORTH);
        add(barreFiltres(), BorderLayout.SOUTH);

        JScrollPane defilement = new JScrollPane(tableau);
        defilement.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(defilement, BorderLayout.CENTER);

        raccourciRecherche();
        rafraichir();
    }

    // ------------------------------------------------------------------ UI

    private JPanel barreOutils() {
        JButton charger = new JButton("Charger du matériel");
        JButton quantite = new JButton("Modifier la quantité");
        JButton deplacer = new JButton("Déplacer");
        JButton retirer = new JButton("Retirer");
        JButton stock = new JButton("Stock");

        charger.addActionListener(e -> ouvrirChargement());
        quantite.addActionListener(e -> modifierQuantite());
        deplacer.addActionListener(e -> deplacer());
        retirer.addActionListener(e -> retirer());
        stock.addActionListener(e -> ouvrirStock());

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barre.add(charger);
        barre.add(quantite);
        barre.add(deplacer);
        barre.add(retirer);
        barre.add(stock);
        return barre;
    }

    private JPanel barreFiltres() {
        filtreVehicule.addItem(TOUS);
        filtreVehicule.addActionListener(e -> appliquerFiltres());

        // réagit à chaque frappe, sans attendre la validation
        recherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { appliquerFiltres(); }
            @Override public void removeUpdate(DocumentEvent e) { appliquerFiltres(); }
            @Override public void changedUpdate(DocumentEvent e) { appliquerFiltres(); }
        });

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        barre.add(new JLabel("Véhicule"));
        barre.add(filtreVehicule);
        barre.add(new JLabel("Rechercher"));
        barre.add(recherche);
        barre.add(compteur);
        return barre;
    }

    /** Ctrl+F place le curseur dans le champ de recherche. */
    private void raccourciRecherche() {
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);

        getRootPane().registerKeyboardAction(
                (ActionEvent e) -> {
                    recherche.requestFocusInWindow();
                    recherche.selectAll();
                },
                ctrlF,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ------------------------------------------------------------- filtres

    /**
     * Combine le filtre véhicule et la recherche libre.
     * Un filtre nul signifie « tout afficher ».
     */
    private void appliquerFiltres() {
        List<RowFilter<Object, Object>> filtres = new ArrayList<>();

        Object choix = filtreVehicule.getSelectedItem();
        if (choix instanceof Vehicule vehicule) {
            // ancrage ^...$ pour ne pas confondre deux plaques qui se ressemblent
            filtres.add(RowFilter.regexFilter(
                    "^" + Pattern.quote(vehicule.getImmatriculation()) + "$", 0));
        }

        String texte = recherche.getText().trim();
        if (!texte.isEmpty()) {
            // (?i) = insensible à la casse ; sur toutes les colonnes
            filtres.add(RowFilter.regexFilter("(?i)" + Pattern.quote(texte)));
        }

        tri.setRowFilter(filtres.isEmpty() ? null : RowFilter.andFilter(filtres));
        majCompteur();
    }

    private void majCompteur() {
        int affichees = tableau.getRowCount();
        int total = modele.getRowCount();

        compteur.setText(affichees == total
                ? total + " ligne(s)"
                : affichees + " sur " + total + " ligne(s)");
    }

    /** Recharge la liste des véhicules sans perdre la sélection en cours. */
    private void chargerVehicules() {
        Object selection = filtreVehicule.getSelectedItem();

        filtreVehicule.removeAllItems();
        filtreVehicule.addItem(TOUS);

        try {
            for (Vehicule v : new VehiculeDao().findAll()) {
                filtreVehicule.addItem(v);
            }
            if (selection != null) {
                filtreVehicule.setSelectedItem(selection);
            }
        } catch (SQLException e) {
            erreur("Impossible de lire les véhicules : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------- actions

    private void rafraichir() {
        try {
            modele.setAffectations(service.listerEnCours());
            chargerVehicules();
            appliquerFiltres();
        } catch (SQLException e) {
            erreur("Impossible de lire les affectations : " + e.getMessage());
        }
    }

    private Affectation selection() {
        int ligne = tableau.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne dans le tableau.");
            return null;
        }
        return modele.getAffectation(tableau.convertRowIndexToModel(ligne));
    }

    private void ouvrirChargement() {
        DialogueAffectation dialogue = new DialogueAffectation(this);
        dialogue.setVisible(true);
        if (dialogue.estValide()) {
            rafraichir();
        }
    }

    private void ouvrirStock() {
        new FenetreStock(this).setVisible(true);
        rafraichir();
    }

    private void modifierQuantite() {
        Affectation affectation = selection();
        if (affectation == null) {
            return;
        }

        String saisie = JOptionPane.showInputDialog(this,
                "Nouvelle quantité de " + affectation.getMateriel()
                        + " dans " + affectation.getVehicule() + " :",
                String.valueOf(affectation.getQuantite()));

        if (saisie == null) {
            return;
        }

        int nouvelle;
        try {
            nouvelle = Integer.parseInt(saisie.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Saisissez un nombre entier.");
            return;
        }

        executer(() -> service.changerQuantite(affectation, nouvelle));
    }

    private void deplacer() {
        Affectation affectation = selection();
        if (affectation == null) {
            return;
        }

        Vehicule[] vehicules;
        try {
            vehicules = new VehiculeDao().findAll().toArray(new Vehicule[0]);
        } catch (SQLException e) {
            erreur("Impossible de lire les véhicules : " + e.getMessage());
            return;
        }

        Object choix = JOptionPane.showInputDialog(this,
                "Déplacer " + affectation.getQuantite() + " × " + affectation.getMateriel()
                        + " vers quel véhicule ?",
                "Déplacer", JOptionPane.QUESTION_MESSAGE,
                null, vehicules, affectation.getVehicule());

        if (choix == null) {
            return;
        }

        executer(() -> service.deplacer(affectation, (Vehicule) choix));
    }

    private void retirer() {
        Affectation affectation = selection();
        if (affectation == null) {
            return;
        }

        int reponse = JOptionPane.showConfirmDialog(this,
                "Retirer " + affectation.getQuantite() + " × " + affectation.getMateriel()
                        + " du véhicule " + affectation.getVehicule() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (reponse != JOptionPane.YES_OPTION) {
            return;
        }

        executer(() -> service.retirer(affectation));
    }

    private void executer(OperationAffectation operation) {
        try {
            operation.executer();
            rafraichir();
        } catch (RegleGestionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Impossible", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            erreur("Erreur technique : " + e.getMessage());
        }
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface OperationAffectation {
        void executer() throws SQLException;
    }
}