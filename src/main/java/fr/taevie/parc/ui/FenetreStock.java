package fr.taevie.parc.ui;

import fr.taevie.parc.dao.StockDao;
import fr.taevie.parc.model.LigneStock;
import fr.taevie.parc.service.MouvementStockService;
import fr.taevie.parc.service.RegleGestionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.sql.SQLException;

/**
 * Écran de stock : état par matériel, et saisie des mouvements.
 */
public class FenetreStock extends JDialog {

    private final MouvementStockService service = new MouvementStockService();
    private final StockDao stockDao = new StockDao();
    private final StockTableModel modele = new StockTableModel();
    private final JTable tableau = new JTable(modele);

    public FenetreStock(Frame parent) {
        super(parent, "Stock", true);

        tableau.setRowHeight(26);
        tableau.setAutoCreateRowSorter(true);
        tableau.setShowVerticalLines(false);
        tableau.setIntercellSpacing(new Dimension(0, 0));

        JButton achat = new JButton("Achat");
        JButton casse = new JButton("Casse");
        JButton perte = new JButton("Perte");
        JButton inventaire = new JButton("Inventaire");

        achat.addActionListener(e -> saisirEntree());
        casse.addActionListener(e -> saisirSortie(MouvementStockService.CASSE));
        perte.addActionListener(e -> saisirSortie(MouvementStockService.PERTE));
        inventaire.addActionListener(e -> saisirInventaire());

        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barre.add(achat);
        barre.add(casse);
        barre.add(perte);
        barre.add(inventaire);

        JScrollPane defilement = new JScrollPane(tableau);
        defilement.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        add(barre, BorderLayout.NORTH);
        add(defilement, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(parent);

        charger();
    }

    private void charger() {
        try {
            modele.setLignes(stockDao.findAll());
        } catch (SQLException e) {
            erreur("Impossible de lire le stock : " + e.getMessage());
        }
    }

    /** La ligne sélectionnée, ou null avec un message si rien n'est sélectionné. */
    private LigneStock selection() {
        int ligne = tableau.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un matériel.");
            return null;
        }
        return modele.getLigne(tableau.convertRowIndexToModel(ligne));
    }

    /** Demande un nombre à l'utilisateur, ou null s'il annule ou saisit n'importe quoi. */
    private Integer demanderQuantite(String message, int defaut) {
        String saisie = JOptionPane.showInputDialog(this, message, String.valueOf(defaut));
        if (saisie == null) {
            return null;
        }
        try {
            return Integer.parseInt(saisie.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Saisissez un nombre entier.");
            return null;
        }
    }

    private void saisirEntree() {
        LigneStock ligne = selection();
        if (ligne == null) {
            return;
        }

        Integer quantite = demanderQuantite("Quantité achetée pour " + ligne.materiel() + " :", 1);
        if (quantite == null) {
            return;
        }

        executer(() -> service.acheter(ligne.materiel(), quantite, "Saisie manuelle"));
    }

    private void saisirSortie(String type) {
        LigneStock ligne = selection();
        if (ligne == null) {
            return;
        }

        Integer quantite = demanderQuantite(
                "Quantité en " + type.toLowerCase() + " pour " + ligne.materiel() + " :", 1);
        if (quantite == null) {
            return;
        }

        executer(() -> service.sortir(ligne.materiel(), type, quantite, "Saisie manuelle"));
    }

    private void saisirInventaire() {
        LigneStock ligne = selection();
        if (ligne == null) {
            return;
        }

        Integer reelle = demanderQuantite(
                "Quantité réellement comptée pour " + ligne.materiel() + " :", ligne.total());
        if (reelle == null) {
            return;
        }

        executer(() -> service.ajuster(ligne.materiel(), reelle, "Inventaire"));
    }

    /** Exécute une opération de stock, affiche l'erreur éventuelle, rafraîchit. */
    private void executer(OperationStock operation) {
        try {
            operation.executer();
            charger();
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

    /** Petite interface pour factoriser le try/catch des quatre opérations. */
    @FunctionalInterface
    private interface OperationStock {
        void executer() throws SQLException;
    }
}