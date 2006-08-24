package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Joueurs.ParametreIAObjet;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import java.awt.Point;
import ServeurJeu.Configuration.GestionnaireConfiguration;

// Cette classe contient tous les paramètres utilisés
// par les joueurs virtuels
public class ParametreIA {

	
	// Ce tableau contient les informations pour tous les objets
	// utilisables
	public ParametreIAObjet tParametresIAObjetUtilisable[];
	
	// Paramètres pour les pièces
	// (Séparé car différent, pourrait être regroupé dans un 
	// tableau d'objet à usage immédiat, on ne se sert
	// pas des champs qui tiennent compte de la quantité)
	public ParametreIAObjet objParametreIAPiece;
	
	// Paramètres pour les magasins, on ne se sert pas de tout
	public ParametreIAObjet objParametreIAMagasin;
	
	// Paramètres pour les minijeus, on ne se sert pas de tout
	public ParametreIAObjet objParametreIAMinijeu;
	
	// Tableau pour le pourcentage des choix
	public int tPourcentageChoix[][];
	
	// Tableau pour le pourcentage des réponses
	public int tPourcentageReponse[][];
	
	// Tableau pour le pourcentage des choix lorsqu'on possède l'objet réponse
	public int tPourcentageChoixObjetReponse[][];
	
	// Tableau pour le pourcentage des réponses lorsqu'on possède l'objet réponse
	public int tPourcentageReponseObjetReponse[][];
		
	// Tableaux pour les temps de réflexion
	public int tTempsReflexionBase[][];
	public int tTempsReflexionAleatoire[][];
	
	// Tableau pour le déplacement moyen
	public double tDeplacementMoyen[];
	
	// Tableau pour le nombre de points maximum qu'un joueur
    // virtuel peut négliger lors du choix de la position finale
    public int tNombrePointsMaximalChoixFinal[];
	
	// Tableau pour les choix alternatif final
	public int tPourcentageChoixAlternatifFinal[][];
	
    // Ce tableau nous permettra de traiter les 4 cases autour d'une case
    // à l'intérieur d'une boucle.
    public Point ptDxDy[];
	
    // Ce tableau contient des paramètres pour prioriser les
    // regroupement de pièces
    public int ttPointsRegionPiece[][];
	
	// Ces tableaux permettent de générer les jetons pour les minijeus
	public int tNbJetonsMinijeuBase[];
	public int tNbJetonsMinijeuAleatoire[];
	
	// Ces tableaux permettent de générer les jetons pour les magasins
	public int tNbJetonsMagasinBase[];
	public int tNbJetonsMagasinAleatoire[];
	
	// Ce tableau contient les noms des joueurs virtuels
	public String tBanqueNomsJoueurVirtuels[];	
	
	//-----------------------------
	//           CONSTANTES
	//-----------------------------
	
	
	
	// Constantes pour les points de départ des cases couleurs
	public final static int POINTS_BASE_CASE_COULEUR = -50;
	public final static int POINTS_ALEATOIRE_CASE_COULEUR = 101;
	
	// Constante donnant un pointage très bas au case que l'on ne veut pas
	// choisir
	public final static int POINTS_IGNORER_CASE = -999999999;
	
	// Constante pour le nombre de niveau de difficulté disponible
	public final static int NOMBRE_NIVEAU_DIFFICULTE = 3;
	
	// Constante pour le nombre de type de réflexion
	public final static int NOMBRE_TYPE_REFLEXION = 3;
	
    // Déplacement maximum autorisé
    public final static int DEPLACEMENT_MAX = 6;
    
	// Constantes pour définir les niveaux de difficulté disponibles
    public static final int DIFFICULTE_FACILE = 0;
    public static final int DIFFICULTE_MOYEN = 1;
    public static final int DIFFICULTE_DIFFICILE = 2;
    
    // Constantes pour définir le type de réflexion
    public static final int TYPE_REFLEXION_COUP = 0;
    public static final int TYPE_REFLEXION_REPONSE = 1;
    public static final int TYPE_REFLEXION_ACHAT = 2;
    
    // Constant pour définir le nombre de choix alternatif final à considérer
    public static final int NOMBRE_CHOIX_ALTERNATIF = 5;
    
	// Cette constante définit le temps de pause lors d'une rétroaction
	public final static int TEMPS_RETROACTION = 14;
	
	// Quelques raisons pour déplacer le joueur virtuel
	public final static int RAISON_AUCUNE = 0;
	public final static int RAISON_PIECE = 1;
	public final static int RAISON_MINIJEU = 2;
	public final static int RAISON_MAGASIN = 3;
	public final static int RAISON_OBJET = 4;

    // Constante représentant les mini-jeus
    public final static int MINIJEU_BALLE_AU_MUR = 0;
    
	// Autres constantes utilisés dans les algorithmes de recherche de choix
    public final static int DROITE = 0;
    public final static int BAS = 1;
    public final static int GAUCHE = 2;
    public final static int HAUT = 3;
    
    // Constante contenant le nombre d'objets maximum de base qu'un joueur
    // virtuel se permet de trainer
    public final static int MAX_NOMBRE_OBJETS = 10;
    
    // Constante contenant le nombre de id personnage
    public final static int NOMBRE_PERSONNAGE_ID = 4;
     
    // Facteur d'éloignement minimal
    public final static double FACTEUR_AJUSTEMENT_MIN = 0.01;
    
    // Valeur en points qu'on enlève pour chaque objets qu'on possède
    public final static int PTS_ENLEVER_MAGASIN_NB_OBJETS = 400;
    
    // Temps de sûreté pour magasin
    public final static int TEMPS_SURETE_MAGASIN = 60;
    
    // Temps de sûreté fin de partie fin de mini-jeu
    public final static int TEMPS_SURETE_MINIJEU_FIN_PARTIE = 3;
    
    // Ratio de questions à choix de réponse (pour objet Reponse)
    public final static int RATIO_CHOIX_DE_REPONSE = 85;
    
	public ParametreIA()
	{
		// Aller chercher la référence vers le gestionnaire de configuration
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		// Créer le tableau contenant l'info pour chaque objet utilisable
		tParametresIAObjetUtilisable = new ParametreIAObjet[1];
		
		// Objet Utilisable "Reponse" UID = 0
		tParametresIAObjetUtilisable[Objet.UID_OU_REPONSE] = 
		    new ParametreIAObjet(4950, 101, 6, 800, 200, 100, 20, 3, 60);
		
		// Paramètres pour les pièces
		objParametreIAPiece = 
		    new ParametreIAObjet(4950, 101, 6, 800, 0, 100, 0, 0, 0);
		    
		// Paramètres pour les magasins
		objParametreIAMagasin = 
		    new ParametreIAObjet(4950, 101, 6, 800, 0, 100, 0, 0, 0);
		    
		// Paramètres pour les minijeus
		objParametreIAMinijeu = 
		    new ParametreIAObjet(4950, 101, 6, 800, 0, 100, 0, 0, 0);
		
		// Créer le tableau contenant les pourcentages pour les choix
		// des déplacements
		tPourcentageChoix = new int[NOMBRE_NIVEAU_DIFFICULTE][DEPLACEMENT_MAX];
		tPourcentageChoix[DIFFICULTE_FACILE][0] = 50;
		tPourcentageChoix[DIFFICULTE_FACILE][1] = 30;
		tPourcentageChoix[DIFFICULTE_FACILE][2] = 19;
		tPourcentageChoix[DIFFICULTE_FACILE][3] = 1;
		tPourcentageChoix[DIFFICULTE_FACILE][4] = 0;
		tPourcentageChoix[DIFFICULTE_FACILE][5] = 0;
		tPourcentageChoix[DIFFICULTE_MOYEN][0] = 5;
		tPourcentageChoix[DIFFICULTE_MOYEN][1] = 19;
		tPourcentageChoix[DIFFICULTE_MOYEN][2] = 40;
		tPourcentageChoix[DIFFICULTE_MOYEN][3] = 25;
		tPourcentageChoix[DIFFICULTE_MOYEN][4] = 10;
		tPourcentageChoix[DIFFICULTE_MOYEN][5] = 1;
		tPourcentageChoix[DIFFICULTE_DIFFICILE][0] = 0;
		tPourcentageChoix[DIFFICULTE_DIFFICILE][1] = 5;
		tPourcentageChoix[DIFFICULTE_DIFFICILE][2] = 15;
		tPourcentageChoix[DIFFICULTE_DIFFICILE][3] = 40;
		tPourcentageChoix[DIFFICULTE_DIFFICILE][4] = 30;
		tPourcentageChoix[DIFFICULTE_DIFFICILE][5] = 10;    
		
		// Créer le tableau contenant les pourcentages pour les
		// réussites des réponses
		tPourcentageReponse = new int[NOMBRE_NIVEAU_DIFFICULTE][DEPLACEMENT_MAX];
		tPourcentageReponse[DIFFICULTE_FACILE][0] = 90;
		tPourcentageReponse[DIFFICULTE_FACILE][1] = 80;
		tPourcentageReponse[DIFFICULTE_FACILE][2] = 60;
		tPourcentageReponse[DIFFICULTE_FACILE][3] = 10;
		tPourcentageReponse[DIFFICULTE_FACILE][4] = 0;
		tPourcentageReponse[DIFFICULTE_FACILE][5] = 0;
		tPourcentageReponse[DIFFICULTE_MOYEN][0] = 95;
		tPourcentageReponse[DIFFICULTE_MOYEN][1] = 90;
		tPourcentageReponse[DIFFICULTE_MOYEN][2] = 85;
		tPourcentageReponse[DIFFICULTE_MOYEN][3] = 50;
		tPourcentageReponse[DIFFICULTE_MOYEN][4] = 30;
		tPourcentageReponse[DIFFICULTE_MOYEN][5] = 15;
		tPourcentageReponse[DIFFICULTE_DIFFICILE][0] = 100;
		tPourcentageReponse[DIFFICULTE_DIFFICILE][1] = 95;
		tPourcentageReponse[DIFFICULTE_DIFFICILE][2] = 90;
		tPourcentageReponse[DIFFICULTE_DIFFICILE][3] = 80;
		tPourcentageReponse[DIFFICULTE_DIFFICILE][4] = 70;
		tPourcentageReponse[DIFFICULTE_DIFFICILE][5] = 60; 
        
        // Créer le tableau pour le pourcentage des choix lorsque le 
        // joueur possède l'objet Reponse
        tPourcentageChoixObjetReponse = new int[NOMBRE_NIVEAU_DIFFICULTE][DEPLACEMENT_MAX];
		tPourcentageChoixObjetReponse[DIFFICULTE_FACILE][0] = 5;
		tPourcentageChoixObjetReponse[DIFFICULTE_FACILE][1] = 20;
		tPourcentageChoixObjetReponse[DIFFICULTE_FACILE][2] = 50;
		tPourcentageChoixObjetReponse[DIFFICULTE_FACILE][3] = 20;
		tPourcentageChoixObjetReponse[DIFFICULTE_FACILE][4] = 5;
		tPourcentageChoixObjetReponse[DIFFICULTE_FACILE][5] = 0;
		tPourcentageChoixObjetReponse[DIFFICULTE_MOYEN][0] = 0;
		tPourcentageChoixObjetReponse[DIFFICULTE_MOYEN][1] = 5;
		tPourcentageChoixObjetReponse[DIFFICULTE_MOYEN][2] = 20;
		tPourcentageChoixObjetReponse[DIFFICULTE_MOYEN][3] = 50;
		tPourcentageChoixObjetReponse[DIFFICULTE_MOYEN][4] = 15;
		tPourcentageChoixObjetReponse[DIFFICULTE_MOYEN][5] = 10;
		tPourcentageChoixObjetReponse[DIFFICULTE_DIFFICILE][0] = 0;
		tPourcentageChoixObjetReponse[DIFFICULTE_DIFFICILE][1] = 0;
		tPourcentageChoixObjetReponse[DIFFICULTE_DIFFICILE][2] = 5;
		tPourcentageChoixObjetReponse[DIFFICULTE_DIFFICILE][3] = 5;
		tPourcentageChoixObjetReponse[DIFFICULTE_DIFFICILE][4] = 50;
		tPourcentageChoixObjetReponse[DIFFICULTE_DIFFICILE][5] = 40; 
        
        
        // Créer le tableau pour le pourcentage de réponse lorsque le
        // joueur utilise l'objet Reponse
        tPourcentageReponseObjetReponse = new int[NOMBRE_NIVEAU_DIFFICULTE][DEPLACEMENT_MAX];
		tPourcentageReponseObjetReponse[DIFFICULTE_FACILE][0] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_FACILE][1] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_FACILE][2] = 85;
		tPourcentageReponseObjetReponse[DIFFICULTE_FACILE][3] = 35;
		tPourcentageReponseObjetReponse[DIFFICULTE_FACILE][4] = 30;
		tPourcentageReponseObjetReponse[DIFFICULTE_FACILE][5] = 0;
		tPourcentageReponseObjetReponse[DIFFICULTE_MOYEN][0] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_MOYEN][1] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_MOYEN][2] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_MOYEN][3] = 80;
		tPourcentageReponseObjetReponse[DIFFICULTE_MOYEN][4] = 55;
		tPourcentageReponseObjetReponse[DIFFICULTE_MOYEN][5] = 40;
		tPourcentageReponseObjetReponse[DIFFICULTE_DIFFICILE][0] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_DIFFICILE][1] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_DIFFICILE][2] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_DIFFICILE][3] = 100;
		tPourcentageReponseObjetReponse[DIFFICULTE_DIFFICILE][4] = 95;
		tPourcentageReponseObjetReponse[DIFFICULTE_DIFFICILE][5] = 85; 


        // Créer les tableaux pour les temps de réflexion
        tTempsReflexionBase = new int [NOMBRE_TYPE_REFLEXION][NOMBRE_NIVEAU_DIFFICULTE];
        tTempsReflexionAleatoire = new int [NOMBRE_TYPE_REFLEXION][NOMBRE_NIVEAU_DIFFICULTE];
	    
	    // Temps de réflexion lors d'achat
	    tTempsReflexionBase[TYPE_REFLEXION_ACHAT][DIFFICULTE_FACILE] = 3;
	    tTempsReflexionBase[TYPE_REFLEXION_ACHAT][DIFFICULTE_MOYEN] = 2;
	    tTempsReflexionBase[TYPE_REFLEXION_ACHAT][DIFFICULTE_DIFFICILE] = 1;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_ACHAT][DIFFICULTE_FACILE] = 4;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_ACHAT][DIFFICULTE_MOYEN] = 3;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_ACHAT][DIFFICULTE_DIFFICILE] = 2;
	      
        // Temps de réflexion avant un coup
	    tTempsReflexionBase[TYPE_REFLEXION_COUP][DIFFICULTE_FACILE] = 3;
	    tTempsReflexionBase[TYPE_REFLEXION_COUP][DIFFICULTE_MOYEN] = 2;
	    tTempsReflexionBase[TYPE_REFLEXION_COUP][DIFFICULTE_DIFFICILE] = 1;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_COUP][DIFFICULTE_FACILE] = 4;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_COUP][DIFFICULTE_MOYEN] = 3;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_COUP][DIFFICULTE_DIFFICILE] = 2;
	    
        // Temps de réflexion pour répondre à une question  
	    tTempsReflexionBase[TYPE_REFLEXION_REPONSE][DIFFICULTE_FACILE] = 24;
	    tTempsReflexionBase[TYPE_REFLEXION_REPONSE][DIFFICULTE_MOYEN] = 16;
	    tTempsReflexionBase[TYPE_REFLEXION_REPONSE][DIFFICULTE_DIFFICILE] = 8;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_REPONSE][DIFFICULTE_FACILE] = 12;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_REPONSE][DIFFICULTE_MOYEN] = 10;
	    tTempsReflexionAleatoire[TYPE_REFLEXION_REPONSE][DIFFICULTE_DIFFICILE] = 8;
	 
	    // Créer le tableau pour le déplacement moyen
	    tDeplacementMoyen = new double[NOMBRE_NIVEAU_DIFFICULTE];   
	    tDeplacementMoyen[DIFFICULTE_FACILE] = 1.276;
	    tDeplacementMoyen[DIFFICULTE_MOYEN] = 2.0685 ;
	    tDeplacementMoyen[DIFFICULTE_DIFFICILE] = 3.19 ;
	    
	    // Créer le tableau pour le nombre de points maximum pour la position
	    // final
	    tNombrePointsMaximalChoixFinal = new int[NOMBRE_NIVEAU_DIFFICULTE];
	    tNombrePointsMaximalChoixFinal[DIFFICULTE_FACILE] = 2000;
	    tNombrePointsMaximalChoixFinal[DIFFICULTE_MOYEN] = 1000;
	    tNombrePointsMaximalChoixFinal[DIFFICULTE_DIFFICILE] = 400;
	    
	    // Créer le tableau pour les pourcentages des choix alternatif finaux
	    tPourcentageChoixAlternatifFinal = new int[NOMBRE_NIVEAU_DIFFICULTE][NOMBRE_CHOIX_ALTERNATIF];
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_FACILE][0] = 70;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_FACILE][1] = 20;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_FACILE][2] = 5;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_FACILE][3] = 4;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_FACILE][4] = 1;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_MOYEN][0] = 80;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_MOYEN][1] = 17;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_MOYEN][2] = 2;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_MOYEN][3] = 1;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_MOYEN][4] = 0;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_DIFFICILE][0] = 90;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_DIFFICILE][1] = 8;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_DIFFICILE][2] = 2;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_DIFFICILE][3] = 0;
	    tPourcentageChoixAlternatifFinal[DIFFICULTE_DIFFICILE][4] = 0;
	    
		// Créer le tableau qui permet de parcourir les 4 cases adjacentes
	    ptDxDy = new Point[4];
	    ptDxDy[DROITE] = new Point(0,1);
	    ptDxDy[BAS] = new Point(1,0);
	    ptDxDy[GAUCHE] = new Point(0,-1);
	    ptDxDy[HAUT] = new Point(-1,0);
	    
        // Créer le tableau pour les regroupements de pièces
        int[][] ttTemp =
                    {{ 10, 10,  0,  0,  0,  0,  0}, 
                     { 20, 15, 10  ,0,  0,  0,  0},
                     { 50, 30, 15, 10,  0,  0,  0},
                     {100, 75, 30, 15, 10,  0,  0},
                     {200,150, 75, 30, 15, 10,  0},
                     {400,300,150, 75, 30, 15, 10},
                     {500,400,200,100, 50, 20, 10}};
       
	    ttPointsRegionPiece = ttTemp;
	    
	    // Créer les tableaux pour les jetons minijeu
	    tNbJetonsMinijeuBase = new int[NOMBRE_NIVEAU_DIFFICULTE];
	    tNbJetonsMinijeuBase[DIFFICULTE_FACILE] = 1;
	    tNbJetonsMinijeuBase[DIFFICULTE_MOYEN] = 2;
	    tNbJetonsMinijeuBase[DIFFICULTE_DIFFICILE] = 3;
	    tNbJetonsMinijeuAleatoire = new int[NOMBRE_NIVEAU_DIFFICULTE];
	    tNbJetonsMinijeuAleatoire[DIFFICULTE_FACILE] = 3;
	    tNbJetonsMinijeuAleatoire[DIFFICULTE_MOYEN] = 3;
	    tNbJetonsMinijeuAleatoire[DIFFICULTE_DIFFICILE] = 3;
	    
	    // Créer les tableaux pour les jetons magasins
	    tNbJetonsMagasinBase = new int[NOMBRE_NIVEAU_DIFFICULTE];
	    tNbJetonsMagasinBase[DIFFICULTE_FACILE] = 12;
	    tNbJetonsMagasinBase[DIFFICULTE_MOYEN] = 12;
	    tNbJetonsMagasinBase[DIFFICULTE_DIFFICILE] = 12;
	    tNbJetonsMagasinAleatoire = new int[NOMBRE_NIVEAU_DIFFICULTE];
	    tNbJetonsMagasinAleatoire[DIFFICULTE_FACILE] = 6;
	    tNbJetonsMagasinAleatoire[DIFFICULTE_MOYEN] = 6;
	    tNbJetonsMagasinAleatoire[DIFFICULTE_DIFFICILE] = 6;
	    
	    // Initialiser les noms des joueurs virtuels        
        String[] tNomsTemp = config.obtenirString("joueurs-virtuels.noms").split("/");
	    tBanqueNomsJoueurVirtuels = tNomsTemp;
	    
	}

}

