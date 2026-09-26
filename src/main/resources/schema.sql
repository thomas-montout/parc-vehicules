-- =====================================================================
--  schema.sql — Base de données du parc véhicules / matériel
--
--  Rôle : créer toutes les tables si elles n'existent pas encore.
--  Ce script est exécuté par l'application au démarrage : il est donc
--  conçu pour pouvoir être rejoué sans erreur (IF NOT EXISTS partout).
--
--  SGBD : SQLite. Particularités à connaître :
--    - pas de type DATE natif  -> on stocke du TEXT 'AAAA-MM-JJ'
--    - pas de VARCHAR(n)       -> TEXT sans longueur
--    - pas de BOOLEAN          -> INTEGER 0 / 1
-- =====================================================================


-- SQLite N'APPLIQUE PAS les clés étrangères par défaut (héritage
-- historique de compatibilité). Sans cette ligne, on pourrait insérer
-- un matériel rattaché à une famille inexistante sans aucune erreur.
-- Attention : ce réglage vaut pour LA CONNEXION EN COURS uniquement.
-- Il devra donc être réexécuté à chaque ouverture de connexion en Java.
PRAGMA foreign_keys = ON;


-- ---------------------------------------------------------------------
--  FAMILLE — le nom générique de l'article (Grattoir, Mouilleur, Casque)
--  Le libellé vit ici et nulle part ailleurs : un seul endroit à
--  corriger en cas de faute de frappe.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS famille (
    -- INTEGER PRIMARY KEY AUTOINCREMENT : SQLite attribue les numéros
    -- tout seul (1, 2, 3...). Le type DOIT être INTEGER et non INT,
    -- sinon l'auto-incrémentation ne fonctionne pas.
                                       id_famille INTEGER PRIMARY KEY AUTOINCREMENT,

    -- NOT NULL : la colonne ne peut pas être vide.
    -- UNIQUE   : deux familles ne peuvent pas porter le même nom.
                                       libelle    TEXT NOT NULL UNIQUE
);


-- ---------------------------------------------------------------------
--  GAMME — deuxième axe de déclinaison, indépendant de la variante.
--  Vient de la liste réelle du client : (PAD), UNGER.
--  Un mouilleur peut être « 35 cm » ET « PAD » en même temps : un seul
--  champ texte ne pouvait pas porter les deux, d'où cette table.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gamme (
                                     id_gamme INTEGER PRIMARY KEY AUTOINCREMENT,
                                     libelle  TEXT NOT NULL UNIQUE
);


-- ---------------------------------------------------------------------
--  MATERIEL — l'article concret, tel qu'on le compte.
--  Un matériel = une famille + une gamme + une variante.
--  Exemple : Mouilleur / Standard / « 35 cm »
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS materiel (
                                        id_materiel INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Clés étrangères. Elles descendent ici parce que MATERIEL est du
    -- côté 1,1 des associations dans le MCD (règle de dérivation
    -- MCD -> MLD : la clé va du côté qui porte le 1,1).
                                        id_famille  INTEGER NOT NULL,
                                        id_gamme    INTEGER NOT NULL,

    -- La déclinaison : « 35 cm », « 4 M », « XL », « orange »...
    -- DEFAULT '' plutôt que NULL : voir l'explication sur UNIQUE plus bas.
                                        variante    TEXT NOT NULL DEFAULT '',

    -- Sert uniquement au tri d'affichage. En tri alphabétique,
    -- « 10 cm » passerait avant « 4 cm », et « XL » avant « S ».
    -- On range donc manuellement : 1, 2, 3...
                                        ordre       INTEGER NOT NULL DEFAULT 0,

    -- Tout ne se compte pas en unités : « Caoutchouc (en mètre) »,
    -- « Carton de chiffons ». Sans ça, « quantité : 3 » est ambigu.
    -- CHECK limite les valeurs acceptées : toute autre valeur est refusée
    -- à l'insertion. C'est l'équivalent SQL d'une énumération.
                                        unite       TEXT NOT NULL DEFAULT 'unite'
                                        CHECK (unite IN ('unite', 'metre', 'carton')),

    -- Déclaration des clés étrangères, en fin de table.
    -- Lecture : « la colonne id_famille de cette table pointe vers la
    -- colonne id_famille de la table famille ».
    FOREIGN KEY (id_famille) REFERENCES famille (id_famille),
    FOREIGN KEY (id_gamme)   REFERENCES gamme (id_gamme),

    -- RG25 : un article est unique par famille + gamme + variante.
    -- UNIQUE sur PLUSIEURS colonnes = c'est la COMBINAISON qui doit être
    -- unique, pas chaque colonne prise isolément.
    --
    -- ATTENTION, piège SQL : NULL n'est jamais égal à NULL. Si variante
    -- valait NULL, deux lignes identiques passeraient sans erreur.
    -- C'est pour ça que variante est NOT NULL DEFAULT '' : la chaîne
    -- vide, elle, est bien égale à elle-même.
    UNIQUE (id_famille, id_gamme, variante)
    );


-- ---------------------------------------------------------------------
--  COMPOSITION — quels articles composent un kit (RG26).
--  Cas particulier : la table référence DEUX FOIS materiel. On appelle
--  ça une relation réflexive (une entité liée à elle-même).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS composition (
                                           id_kit       INTEGER NOT NULL,   -- le contenant
                                           id_composant INTEGER NOT NULL,   -- le contenu
                                           quantite     INTEGER NOT NULL CHECK (quantite > 0),

    -- Clé primaire sur DEUX colonnes, sans identifiant propre.
    -- Contrairement à affectation, on n'a pas besoin d'historique ici :
    -- un composant n'apparaît qu'une fois dans un kit donné, et la ligne
    -- peut être modifiée ou supprimée sans rien perdre.
    PRIMARY KEY (id_kit, id_composant),

    FOREIGN KEY (id_kit)       REFERENCES materiel (id_materiel),
    FOREIGN KEY (id_composant) REFERENCES materiel (id_materiel),

    -- RG27 : un matériel ne peut pas se contenir lui-même.
    -- Ce CHECK ne bloque que le cas direct (A contient A). Une boucle
    -- indirecte (A contient B, B contient A) reste possible et devra
    -- être contrôlée dans le code Java.
    CHECK (id_kit <> id_composant)   -- <> est le « différent de » du SQL
    );


-- ---------------------------------------------------------------------
--  VEHICULE — les camionnettes des laveurs.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicule (
                                        id_vehicule     INTEGER PRIMARY KEY AUTOINCREMENT,

    -- RG1 : l'immatriculation identifie le véhicule de façon unique.
    -- C'est une clé dite « naturelle » : on aurait pu s'en servir comme
    -- clé primaire. On garde quand même un id technique, parce qu'une
    -- plaque peut changer (réimmatriculation) alors qu'un id, jamais.
                                        immatriculation TEXT NOT NULL UNIQUE,

    -- Pas de NOT NULL : le client ne connaît pas forcément ces infos,
    -- et elles ne sont pas nécessaires au fonctionnement.
                                        marque          TEXT,
                                        modele          TEXT
);


-- ---------------------------------------------------------------------
--  MOUVEMENT_STOCK — l'historique des entrées et sorties de stock.
--  Principe : on ne stocke PAS le stock total dans une colonne, on
--  stocke les mouvements, et le total se calcule par SUM(quantite).
--  Même logique qu'un relevé bancaire : on écrit les opérations, jamais
--  le solde. Un solde stocké finit toujours par être faux.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mouvement_stock (
                                               id_mouvement   INTEGER PRIMARY KEY AUTOINCREMENT,
                                               id_materiel    INTEGER NOT NULL,

    -- RG19 : liste fermée des types de mouvement.
                                               type_mouvement TEXT NOT NULL
                                               CHECK (type_mouvement IN ('ACHAT', 'CASSE', 'PERTE', 'AJUSTEMENT')),

    -- Quantité SIGNÉE : positive pour un achat, négative pour une casse
    -- ou une perte. D'où <> 0 (différent de zéro) et non > 0 :
    -- seul zéro n'aurait aucun sens.
    quantite       INTEGER NOT NULL CHECK (quantite <> 0),

    -- Format 'AAAA-MM-JJ' impératif : c'est le seul qui se trie
    -- correctement en comparaison de texte.
    date_mouvement TEXT NOT NULL,

    -- Facultatif : « commande fournisseur », « lame tordue »...
    commentaire    TEXT,

    FOREIGN KEY (id_materiel) REFERENCES materiel (id_materiel)
    );


-- ---------------------------------------------------------------------
--  AFFECTATION — quel matériel, dans quel véhicule, en quelle quantité.
--  C'est le cœur du besoin : « quel matériel se trouve dans quelle
--  voiture ».
--
--  Pourquoi une table avec son propre id, et non une simple table de
--  liaison (id_materiel, id_vehicule) en clé primaire ?
--  Parce que RG15 impose de conserver l'historique : le même couple
--  matériel + véhicule peut revenir plusieurs fois dans le temps.
--  Une clé primaire sur le couple l'aurait interdit.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS affectation (
                                           id_affectation INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Les DEUX clés étrangères : dans le MCD, affectation est en 1,1
    -- des deux côtés, elle récupère donc les deux.
                                           id_materiel    INTEGER NOT NULL,
                                           id_vehicule    INTEGER NOT NULL,

    -- RG12 : affecter 0 exemplaire n'a pas de sens ; pour retirer du
    -- matériel on clôture la ligne (date_fin), on ne met pas 0.
                                           quantite       INTEGER NOT NULL CHECK (quantite > 0),

    date_debut     TEXT NOT NULL,

    -- RG13 : NULL = le matériel est TOUJOURS À BORD.
    -- C'est le marqueur central de toute l'application : les requêtes
    -- « que contient ce véhicule aujourd'hui ? » filtrent sur
    -- « date_fin IS NULL ».
    date_fin       TEXT,

    FOREIGN KEY (id_materiel) REFERENCES materiel (id_materiel),
    FOREIGN KEY (id_vehicule) REFERENCES vehicule (id_vehicule),

    -- Cohérence des dates : on ne peut pas retirer un matériel avant
    -- de l'avoir posé. Le OR est nécessaire car une affectation en
    -- cours a date_fin à NULL, et NULL >= date_debut ne vaut pas
    -- « vrai » mais NULL, ce qui ferait échouer le CHECK.
    CHECK (date_fin IS NULL OR date_fin >= date_debut)
    );


-- ---------------------------------------------------------------------
--  RG16 : une seule affectation EN COURS par couple matériel/véhicule.
--
--  Index UNIQUE PARTIEL : le WHERE restreint la contrainte aux seules
--  lignes où date_fin est NULL, c'est-à-dire aux affectations actives.
--  Conséquence : l'historique peut contenir dix lignes closes pour le
--  même couple, mais une seule ouverte à la fois.
--
--  Sans ce WHERE, un UNIQUE classique aurait interdit tout historique.
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS idx_affectation_en_cours
    ON affectation (id_materiel, id_vehicule)
    WHERE date_fin IS NULL;


-- ---------------------------------------------------------------------
--  Données initiales
-- ---------------------------------------------------------------------

-- RG24 : la majorité des articles n'ont pas de gamme particulière.
-- Plutôt que d'autoriser id_gamme à NULL (et de gérer ce cas partout
-- dans le code), on crée une gamme « Standard » à laquelle ils sont
-- tous rattachés.
--
-- INSERT OR IGNORE : si la ligne existe déjà, SQLite passe son chemin
-- au lieu de lever une erreur de doublon. Indispensable ici, puisque le
-- script est rejoué à chaque démarrage de l'application.
INSERT OR IGNORE INTO gamme (id_gamme, libelle) VALUES (1, 'Standard');