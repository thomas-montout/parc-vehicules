package fr.taevie.parc.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import fr.taevie.parc.model.Affectation;
import fr.taevie.parc.model.Vehicule;
import fr.taevie.parc.service.AffectationService;
import fr.taevie.parc.service.RegleGestionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Fenêtre principale, organisée en maître-détail :
 * <ul>
 *   <li>à gauche, la liste des véhicules avec ce qu'ils ont à bord ;</li>
 *   <li>à droite, le chargement du véhicule sélectionné.</li>
 * </ul>
 * C'est la façon dont l'utilisateur raisonne : « qu'y a-t-il dans tel camion ? ».
 * <p>
 * La fenêtre ne connaît que la couche service — aucun DAO, aucun SQL.
 */
public class FenetrePrincipale extends JFrame {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String CARTE_TABLEAU = "tableau";
    private static final String CARTE_VIDE = "vide";

    private final AffectationService service = new AffectationService();

    // ---- maître : les véhicules
    private final DefaultListModel<LigneVehicule> modeleVehicules = new DefaultListModel<>();
    private final JList<LigneVehicule> listeVehicules = new JList<>(modeleVehicules);
    private final JTextField rechercheVehicule = new JTextField();

    // ---- détail : le chargement du véhicule sélectionné
    private final AffectationTableModel modele = new AffectationTableModel();
    private final JTable tableau = new JTable(modele);
    private final TableRowSorter<AffectationTableModel> tri = new TableRowSorter<>(modele);
    private final JTextField rechercheMateriel = new JTextField(18);
    private final JLabel titreDetail = new JLabel(" ");
    private final JLabel sousTitreDetail = new JLabel(" ");
    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneDetail = new JPanel(cartes);
    private final JLabel messageVide = new JLabel(" ", SwingConstants.CENTER);
    private final JButton chargerDepuisVide = new JButton("Charger du matériel");

    // ---- actions
    private final JButton boutonCharger = new JButton("Charger du matériel");
    private final JButton boutonQuantite = new JButton("Modifier la quantité");
    private final JButton boutonDeplacer = new JButton("Déplacer");
    private final JButton boutonRetirer = new JButton("Retirer");
    private final JButton boutonStock = new JButton("Stock");

    // ---- données, relues à chaque rafraîchissement
    private List<Affectation> toutesAffectations = new ArrayList<>();
    private List<Vehicule> vehicules = new ArrayList<>();

    public FenetrePrincipale() {
        setTitle("ISO Stock");
        setIconImages(Icones.application());
        setSize(1180, 720);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(barreOutils(), BorderLayout.NORTH);

        JSplitPane separation = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panneauVehicules(), panneauDetail());
        separation.setDividerLocation(270);
        separation.setResizeWeight(0);      // la liste garde sa largeur, le détail prend le reste
        separation.setBorder(null);
        add(separation, BorderLayout.CENTER);

        installerRaccourcis();
        majBoutons();
        rafraichir();
    }

    // ================================================================ construction

    private JPanel barreOutils() {
        accentuer(boutonCharger);
        accentuer(chargerDepuisVide);

        boutonCharger.addActionListener(e -> ouvrirChargement());
        chargerDepuisVide.addActionListener(e -> ouvrirChargement());
        boutonQuantite.addActionListener(e -> modifierQuantite());
        boutonDeplacer.addActionListener(e -> deplacer());
        boutonRetirer.addActionListener(e -> retirer());
        boutonStock.addActionListener(e -> ouvrirStock());

        boutonCharger.setToolTipText("Ajouter du matériel dans un véhicule");
        boutonStock.setToolTipText("Achats, casse, perte et inventaire");

        JSeparator separateur = new JSeparator(SwingConstants.VERTICAL);
        separateur.setPreferredSize(new Dimension(12, 24));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        gauche.add(boutonCharger);
        gauche.add(separateur);
        gauche.add(boutonQuantite);
        gauche.add(boutonDeplacer);
        gauche.add(boutonRetirer);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        droite.add(boutonStock);

        JPanel barre = new JPanel(new BorderLayout());
        barre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
        barre.add(gauche, BorderLayout.WEST);
        barre.add(droite, BorderLayout.EAST);
        return barre;
    }

    private JPanel panneauVehicules() {
        champDeRecherche(rechercheVehicule, "Rechercher un véhicule  (Ctrl+F)");
        rechercheVehicule.getDocument().addDocumentListener(quandLeTexteChange(this::filtrerVehicules));

        listeVehicules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listeVehicules.setCellRenderer(new LigneVehiculeRenderer());
        listeVehicules.setFixedCellHeight(38);
        listeVehicules.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                afficherDetail();
            }
        });

        JLabel titre = new JLabel("Véhicules");
        titre.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");

        JPanel haut = new JPanel(new BorderLayout(0, 10));
        haut.setBorder(BorderFactory.createEmptyBorder(14, 12, 10, 12));
        haut.add(titre, BorderLayout.NORTH);
        haut.add(rechercheVehicule, BorderLayout.CENTER);

        JScrollPane defilement = new JScrollPane(listeVehicules);
        defilement.setBorder(BorderFactory.createEmptyBorder());

        JPanel panneau = new JPanel(new BorderLayout());
        panneau.setMinimumSize(new Dimension(210, 0));
        panneau.add(haut, BorderLayout.NORTH);
        panneau.add(defilement, BorderLayout.CENTER);
        return panneau;
    }

    private JPanel panneauDetail() {
        titreDetail.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        sousTitreDetail.setForeground(UIManager.getColor("Label.disabledForeground"));

        champDeRecherche(rechercheMateriel, "Rechercher un matériel");
        rechercheMateriel.getDocument().addDocumentListener(quandLeTexteChange(this::appliquerRechercheMateriel));

        JPanel textes = new JPanel();
        textes.setLayout(new BoxLayout(textes, BoxLayout.Y_AXIS));
        textes.add(titreDetail);
        textes.add(Box.createVerticalStrut(2));
        textes.add(sousTitreDetail);

        JPanel blocRecherche = new JPanel(new GridBagLayout());   // centre verticalement le champ
        blocRecherche.add(rechercheMateriel);

        JPanel entete = new JPanel(new BorderLayout(16, 0));
        entete.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        entete.add(textes, BorderLayout.CENTER);
        entete.add(blocRecherche, BorderLayout.EAST);

        configurerTableau();

        JScrollPane defilement = new JScrollPane(tableau);
        defilement.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                UIManager.getColor("Component.borderColor")));

        zoneDetail.add(defilement, CARTE_TABLEAU);
        zoneDetail.add(etatVide(), CARTE_VIDE);

        JPanel panneau = new JPanel(new BorderLayout());
        panneau.add(entete, BorderLayout.NORTH);
        panneau.add(zoneDetail, BorderLayout.CENTER);
        return panneau;
    }

    private void configurerTableau() {
        tableau.setRowSorter(tri);
        tableau.setRowHeight(32);
        tableau.setShowVerticalLines(false);
        tableau.setShowHorizontalLines(true);
        tableau.setIntercellSpacing(new Dimension(0, 1));
        tableau.setFillsViewportHeight(true);
        tableau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableau.getTableHeader().setReorderingAllowed(false);
        tableau.setDefaultRenderer(LocalDate.class, new RenduDate());

        // quantité centrée sous son en-tête : alignée à droite, elle collait à la date
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        tableau.setDefaultRenderer(Integer.class, centre);

        tableau.getSelectionModel().addListSelectionListener(e -> majBoutons());

        JPopupMenu menu = new JPopupMenu();
        menu.add(element("Modifier la quantité", this::modifierQuantite));
        menu.add(element("Déplacer vers un autre véhicule…", this::deplacer));
        menu.addSeparator();
        menu.add(element("Retirer du véhicule", this::retirer));

        tableau.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ouvrirMenu(e, menu);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ouvrirMenu(e, menu);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && tableau.rowAtPoint(e.getPoint()) >= 0) {
                    modifierQuantite();
                }
            }
        });
    }

    /** Ce qui s'affiche à la place du tableau quand il n'y a rien à montrer. */
    private JPanel etatVide() {
        messageVide.putClientProperty(FlatClientProperties.STYLE_CLASS, "large");
        messageVide.setForeground(UIManager.getColor("Label.disabledForeground"));
        messageVide.setAlignmentX(Component.CENTER_ALIGNMENT);
        chargerDepuisVide.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel contenu = new JPanel();
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.add(messageVide);
        contenu.add(Box.createVerticalStrut(16));
        contenu.add(chargerDepuisVide);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.add(contenu);
        return centre;
    }

    /** Raccourcis clavier : les gestes qu'on attend d'un logiciel de bureau. */
    private void installerRaccourcis() {
        JComponent racine = getRootPane();

        // Ctrl+F : chercher un véhicule, où que soit le curseur
        racine.registerKeyboardAction(e -> {
                    rechercheVehicule.requestFocusInWindow();
                    rechercheVehicule.selectAll();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // dans le tableau : Entrée modifie la quantité, Suppr retire
        tableau.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "modifier");
        tableau.getActionMap().put("modifier", action(this::modifierQuantite));
        tableau.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "retirer");
        tableau.getActionMap().put("retirer", action(this::retirer));

        // dans la recherche de véhicule : flèche bas pour descendre dans la liste
        rechercheVehicule.registerKeyboardAction(e -> {
                    listeVehicules.requestFocusInWindow();
                    if (listeVehicules.getSelectedIndex() < 0 && !modeleVehicules.isEmpty()) {
                        listeVehicules.setSelectedIndex(0);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                JComponent.WHEN_FOCUSED);

        // Échap vide un champ de recherche
        for (JTextField champ : List.of(rechercheVehicule, rechercheMateriel)) {
            champ.registerKeyboardAction(e -> champ.setText(""),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_FOCUSED);
        }
    }

    // ================================================================ données

    /** Relit la base, puis réaffiche en gardant le véhicule sélectionné. */
    private void rafraichir() {
        try {
            toutesAffectations = service.listerEnCours();
            vehicules = service.listerVehicules();
        } catch (SQLException e) {
            erreur("Impossible de lire les données : " + e.getMessage());
            return;
        }
        filtrerVehicules();
    }

    /**
     * Reconstruit la liste de gauche selon la recherche, avec le compte de
     * ce que chaque véhicule a à bord, puis resélectionne le véhicule courant.
     */
    private void filtrerVehicules() {
        LigneVehicule avant = listeVehicules.getSelectedValue();
        String recherche = normaliser(rechercheVehicule.getText());

        // une seule passe sur les affectations pour compter par véhicule
        Map<Vehicule, int[]> aBord = new HashMap<>();
        int piecesTotales = 0;
        for (Affectation a : toutesAffectations) {
            int[] compte = aBord.computeIfAbsent(a.getVehicule(), v -> new int[2]);
            compte[0]++;                          // articles
            compte[1] += a.getQuantite();         // pièces
            piecesTotales += a.getQuantite();
        }

        modeleVehicules.clear();
        if (recherche.isEmpty()) {
            modeleVehicules.addElement(LigneVehicule.tous(toutesAffectations.size(), piecesTotales));
        }
        for (Vehicule v : vehicules) {
            if (normaliser(v.getImmatriculation()).contains(recherche)) {
                int[] compte = aBord.getOrDefault(v, new int[2]);
                modeleVehicules.addElement(new LigneVehicule(v, compte[0], compte[1]));
            }
        }

        if (modeleVehicules.isEmpty()) {
            afficherAucunVehicule();
            return;
        }

        int index = indexDe(avant);
        listeVehicules.setSelectedIndex(index >= 0 ? index : 0);
        listeVehicules.ensureIndexIsVisible(listeVehicules.getSelectedIndex());
    }

    /** Position dans la liste de la ligne qui était sélectionnée, ou -1. */
    private int indexDe(LigneVehicule ancienne) {
        if (ancienne == null) {
            return -1;
        }
        for (int i = 0; i < modeleVehicules.size(); i++) {
            LigneVehicule ligne = modeleVehicules.get(i);
            boolean memeVehicule = ancienne.estTous()
                    ? ligne.estTous()
                    : ancienne.vehicule().equals(ligne.vehicule());
            if (memeVehicule) {
                return i;
            }
        }
        return -1;
    }

    /** Affiche à droite le chargement de la ligne sélectionnée à gauche. */
    private void afficherDetail() {
        LigneVehicule ligne = listeVehicules.getSelectedValue();
        if (ligne == null) {
            return;     // la liste est en cours de reconstruction
        }

        List<Affectation> contenu = ligne.estTous()
                ? toutesAffectations
                : toutesAffectations.stream()
                        .filter(a -> a.getVehicule().equals(ligne.vehicule()))
                        .toList();

        modele.setAffectations(contenu, ligne.estTous());
        dimensionnerColonnes();

        titreDetail.setText(ligne.libelle());
        sousTitreDetail.setText(resume(ligne));
        appliquerRechercheMateriel();
    }

    private void afficherAucunVehicule() {
        modele.setAffectations(List.of(), false);
        titreDetail.setText("Aucun véhicule");
        sousTitreDetail.setText("Aucune immatriculation ne correspond à la recherche");
        messageVide.setText("Aucun véhicule trouvé");
        chargerDepuisVide.setVisible(false);
        cartes.show(zoneDetail, CARTE_VIDE);
        majBoutons();
    }

    private String resume(LigneVehicule ligne) {
        if (ligne.estTous() && ligne.articles() == 0) {
            return "Aucun matériel chargé · " + pluriel(vehicules.size(), "véhicule", "véhicules") + " au parc";
        }
        if (ligne.estTous()) {
            long equipes = toutesAffectations.stream().map(Affectation::getVehicule).distinct().count();
            return pluriel(ligne.articles(), "chargement", "chargements")
                    + " dans " + pluriel((int) equipes, "véhicule", "véhicules")
                    + " sur " + vehicules.size()
                    + " · " + pluriel(ligne.pieces(), "pièce", "pièces");
        }
        if (ligne.articles() == 0) {
            return "Aucun matériel à bord";
        }
        return pluriel(ligne.articles(), "article", "articles")
                + " · " + pluriel(ligne.pieces(), "pièce", "pièces") + " à bord";
    }

    /** Filtre le tableau sur le texte recherché, puis choisit tableau ou état vide. */
    private void appliquerRechercheMateriel() {
        String texte = rechercheMateriel.getText().trim();

        tri.setRowFilter(texte.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texte)));

        if (tableau.getRowCount() > 0) {
            cartes.show(zoneDetail, CARTE_TABLEAU);
        } else {
            LigneVehicule ligne = listeVehicules.getSelectedValue();
            if (!texte.isEmpty()) {
                messageVide.setText("Aucun matériel ne correspond à « " + texte + " »");
                chargerDepuisVide.setVisible(false);
            } else if (ligne == null || ligne.estTous()) {
                messageVide.setText("Aucun matériel n'est chargé pour l'instant");
                chargerDepuisVide.setVisible(true);
            } else {
                messageVide.setText("Aucun matériel dans " + ligne.libelle());
                chargerDepuisVide.setVisible(true);
            }
            cartes.show(zoneDetail, CARTE_VIDE);
        }
        majBoutons();
    }

    /** Les boutons qui agissent sur une ligne ne sont actifs que si une ligne est choisie. */
    private void majBoutons() {
        boolean ligneChoisie = tableau.getSelectedRow() >= 0;
        String aide = ligneChoisie ? null : "Sélectionnez d'abord une ligne du tableau";

        for (JButton bouton : List.of(boutonQuantite, boutonDeplacer, boutonRetirer)) {
            bouton.setEnabled(ligneChoisie);
            bouton.setToolTipText(aide);
        }
    }

    // ================================================================ actions

    private Affectation selection() {
        int ligne = tableau.getSelectedRow();
        if (ligne < 0) {
            return null;
        }
        // le tri et la recherche réordonnent l'affichage : on repasse par l'index du modèle
        return modele.getAffectation(tableau.convertRowIndexToModel(ligne));
    }

    /** Le véhicule choisi à gauche, ou null en vue d'ensemble. */
    private Vehicule vehiculeSelectionne() {
        LigneVehicule ligne = listeVehicules.getSelectedValue();
        return ligne == null ? null : ligne.vehicule();
    }

    private void ouvrirChargement() {
        // le véhicule affiché est présélectionné : c'est presque toujours celui qu'on veut
        DialogueAffectation dialogue = new DialogueAffectation(this, vehiculeSelectionne());
        dialogue.setVisible(true);          // bloque tant que la boîte est ouverte
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

        if (nouvelle == affectation.getQuantite()) {
            return;
        }

        executer(() -> service.changerQuantite(affectation, nouvelle));
    }

    private void deplacer() {
        Affectation affectation = selection();
        if (affectation == null) {
            return;
        }

        // on ne propose pas le véhicule où se trouve déjà le matériel
        Vehicule[] destinations = vehicules.stream()
                .filter(v -> !v.equals(affectation.getVehicule()))
                .toArray(Vehicule[]::new);

        if (destinations.length == 0) {
            JOptionPane.showMessageDialog(this, "Aucun autre véhicule disponible.");
            return;
        }

        Object choix = JOptionPane.showInputDialog(this,
                "Déplacer " + affectation.getQuantite() + " × " + affectation.getMateriel()
                        + " vers quel véhicule ?",
                "Déplacer", JOptionPane.QUESTION_MESSAGE,
                null, destinations, destinations[0]);

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

        // des boutons qui disent ce qu'ils font, plutôt qu'un « Oui / Non » à relire
        Object[] choix = {"Retirer", "Annuler"};
        int reponse = JOptionPane.showOptionDialog(this,
                "Retirer " + affectation.getQuantite() + " × " + affectation.getMateriel()
                        + " du véhicule " + affectation.getVehicule() + " ?",
                "Retirer du véhicule", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, choix, choix[0]);

        if (reponse != 0) {
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

    // ================================================================ utilitaires

    /** Largeurs adaptées au contenu : la quantité n'a pas besoin du quart de l'écran. */
    private void dimensionnerColonnes() {
        for (int i = 0; i < tableau.getColumnCount(); i++) {
            TableColumn colonne = tableau.getColumnModel().getColumn(i);
            switch (String.valueOf(colonne.getHeaderValue())) {
                case AffectationTableModel.VEHICULE -> colonne.setPreferredWidth(140);
                case AffectationTableModel.MATERIEL -> colonne.setPreferredWidth(420);
                case AffectationTableModel.QUANTITE -> {
                    colonne.setPreferredWidth(90);
                    colonne.setMaxWidth(120);
                }
                case AffectationTableModel.DEPUIS -> {
                    colonne.setPreferredWidth(120);
                    colonne.setMaxWidth(150);
                }
                default -> { }
            }
        }
    }

    /** Apparence commune des champs de recherche : loupe, texte d'aide, croix pour effacer. */
    private static void champDeRecherche(JTextField champ, String aide) {
        champ.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, aide);
        champ.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        champ.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
    }

    /** Donne à un bouton la couleur d'accent : c'est l'action principale de l'écran. */
    private static void accentuer(JButton bouton) {
        Color fond = UIManager.getColor("Button.default.background");
        Color texte = UIManager.getColor("Button.default.foreground");
        if (fond != null && texte != null) {
            bouton.setBackground(fond);
            bouton.setForeground(texte);
        }
    }

    /** Ignore tirets, espaces et casse : « dp 455 » trouve DP455PG. */
    private static String normaliser(String texte) {
        return texte.replaceAll("[\\s-]", "").toUpperCase();
    }

    private static String pluriel(int nombre, String singulier, String plurielForme) {
        return nombre + " " + (nombre > 1 ? plurielForme : singulier);
    }

    private void ouvrirMenu(MouseEvent e, JPopupMenu menu) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int ligne = tableau.rowAtPoint(e.getPoint());
        if (ligne < 0) {
            return;
        }
        // un clic droit sélectionne d'abord la ligne visée, comme partout ailleurs
        tableau.setRowSelectionInterval(ligne, ligne);
        menu.show(tableau, e.getX(), e.getY());
    }

    private static JMenuItem element(String texte, Runnable action) {
        JMenuItem item = new JMenuItem(texte);
        item.addActionListener(e -> action.run());
        return item;
    }

    private static AbstractAction action(Runnable traitement) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                traitement.run();
            }
        };
    }

    /** Un DocumentListener qui réagit à toute modification du texte. */
    private static DocumentListener quandLeTexteChange(Runnable traitement) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                traitement.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                traitement.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                traitement.run();
            }
        };
    }

    /** Affiche une date au format français, centrée sous son en-tête. */
    private static final class RenduDate extends DefaultTableCellRenderer {
        RenduDate() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void setValue(Object valeur) {
            setText(valeur instanceof LocalDate date ? date.format(FORMAT_DATE) : "");
        }
    }

    @FunctionalInterface
    private interface OperationAffectation {
        void executer() throws SQLException;
    }
}
