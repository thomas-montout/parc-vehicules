package fr.taevie.parc.service;

/**
 * Signale qu'une règle de gestion empêche l'opération demandée.
 * Le message est destiné à être affiché à l'utilisateur tel quel.
 */
public class RegleGestionException extends RuntimeException {

    public RegleGestionException(String message) {
        super(message);
    }
}