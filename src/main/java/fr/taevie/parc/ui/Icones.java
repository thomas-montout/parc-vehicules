package fr.taevie.parc.ui;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Chargement de l'icône de l'application, fournie en plusieurs tailles.
 * <p>
 * On donne à Swing toute la série : Windows choisit lui-même la plus adaptée
 * selon l'endroit où elle s'affiche (barre de titre, barre des tâches, Alt+Tab).
 * Une seule grande image réduite à 16 px donnerait un résultat flou.
 */
public final class Icones {

    private static final int[] TAILLES = {16, 20, 24, 32, 40, 48, 64, 128, 256};

    private Icones() {
        // classe utilitaire : pas d'instance
    }

    /**
     * Les icônes disponibles dans les ressources. Une taille manquante est
     * simplement ignorée : l'application démarre même sans icône.
     *
     * @return la liste des images trouvées, éventuellement vide
     */
    public static List<Image> application() {
        List<Image> images = new ArrayList<>();

        for (int taille : TAILLES) {
            try (InputStream in = Icones.class.getResourceAsStream("/icons/icone-" + taille + ".png")) {
                if (in != null) {
                    images.add(ImageIO.read(in));
                }
            } catch (IOException e) {
                // une icône illisible ne doit pas empêcher le démarrage
            }
        }

        return images;
    }
}
