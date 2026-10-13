package fr.taevie.parc.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Dessine une ligne de la liste des véhicules : la plaque à gauche,
 * le nombre d'articles à bord à droite, en plus discret.
 * <p>
 * Un JList ne dessine pas ses éléments lui-même : pour chaque ligne visible,
 * il demande ce composant au renderer, le configure, le « tamponne » à
 * l'écran, puis passe à la ligne suivante. Le même objet sert pour toutes
 * les lignes — d'où l'obligation de tout reconfigurer à chaque appel.
 */
class LigneVehiculeRenderer extends JPanel implements ListCellRenderer<LigneVehicule> {

    private final JLabel nom = new JLabel();
    private final JLabel detail = new JLabel();

    LigneVehiculeRenderer() {
        super(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        add(nom, BorderLayout.CENTER);
        add(detail, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends LigneVehicule> liste,
                                                  LigneVehicule ligne, int index,
                                                  boolean selectionne, boolean focus) {
        nom.setText(ligne.libelle());
        detail.setText(ligne.resume());

        Font police = liste.getFont();
        nom.setFont(ligne.estTous() ? police.deriveFont(Font.BOLD) : police);
        detail.setFont(police.deriveFont(police.getSize2D() - 1f));

        Color texte = selectionne ? liste.getSelectionForeground() : liste.getForeground();
        Color discret = UIManager.getColor("Label.disabledForeground");

        setBackground(selectionne ? liste.getSelectionBackground() : liste.getBackground());
        nom.setForeground(texte);
        // un camion vide se repère d'un coup d'œil : son compteur est estompé
        detail.setForeground(selectionne || ligne.articles() > 0 ? texte : discret);

        return this;
    }
}
