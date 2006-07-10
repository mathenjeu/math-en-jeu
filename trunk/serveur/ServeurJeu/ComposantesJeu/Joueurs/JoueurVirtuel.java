package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ControleurJeu;
import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.Salle;
import java.awt.Point;
import java.util.TreeMap;
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.Evenements.EvenementJoueurDeplacePersonnage;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.lang.Math;
import java.util.Vector;

import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementJoueurDeplacePersonnage;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;

import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;

import java.util.Random;
import java.util.Date;

//import ServeurJeu.ComposantesJeu.Joueurs.TestJoueurVirtuel;


/* Priorité haute
 * -----------------
 * TODO: Ne pas utiliser "instanceof" mais utiliser obtenirTypeCase
 *
 * Priorité moyenne
 * -----------------
 * TODO: Optimiser l'utilisation des matrices (réutiliser les mêmes)
 * TODO: Prendre en compte magasins, mini-jeu et objets lors des choix
 * TODO: Profil: Importance magasin, mini-jeu et objets
 * TODO: Émuler mini-jeu
 * TODO: Achat magasin
 * TODO: Utiliser objet
 * TODO: Noms des joueurs virtuels
 * TODO: Conserver le chemin trouvé dans trouverPosFinale pour posIntermediaire
 * TODO: Optimisation: Analyser la pertinence d'utiliser des listes des cases
 *       importantes et de vérifier ces listes plutôt que de parcourir toute
 *       la matrice du plateau de jeu lorsqu'on cherche une case finale
 * TODO: Case finale: améliorer en prenant en compte les autres joueurs
 *
 * Priorité basse
 * -----------------
 * TODO: Dans l'algorithme de valorisation de regroupements de pièces, prendre
 *       en compte le fait que la case se trouve par-dessus un trou (ou qu'elle est trop loin)
 * TODO: Case finale: améliorer pour ce qui est des croches (le joueur virtuel
 *       ne voit pas de différence entre 3 cases en ligne droite et 3 cases 
 *       avec un croche)
 * TODO: Améliorer la case finale si jamais il n'y a plus de pièces
 *       (trouver de grands déplacements en ligne droite qui permet
 *        d'avoir des déplacements maximales)
 * TODO: Profil: prendre en compte position des ennemis
 * TODO: Profil: aggressivité: Joueurs aggressif essaie de "voler" des pièces, alors
 *       que les joueurs passifs restent plus à l'écart, impact aussi sur les objets
 *       que le joueur achètera
 * TODO: Profil: Prioriser certains objets (en plus de ceux priorisés par l'aggressivité, 
 *       par exemple, on pourrait avoir un joueur virtuel qui adore jouer un certain
 *       objet même si l'objet est très mauvais)
 * TODO: Profil: Réaction lorsque gagne / perd (augmenter/diminuer témérité, un joueur
 *       qui perd avec un très grand écart va tenter de plus grands mouvements)
 * TODO: Pos finale: prendre en compte quelques coups d'avance
 * TODO: Profil: Certain joueur virtuel meilleur en statistique, algèbre, etc, 
 *       donc prioriser certains cases couleurs ainsi que les pièces sur ces cases
 *       couleurs.
 * TODO: Profil: On ne demande qu'un niveau de difficulté, le reste est générer aléatoirement
 *       En général, on obtient des settings prochent de la normal, mais il est possible
 *       de se ramasser quelque fois avec des joueurs un peu spécial (très aggressif,
 *       très porté à jouer à des mini-jeu sans arrêt, fixation sur tel objet, etc.)
 * TODO: Prioriser mouvements au centre du plateau plutôt que dans les côtés
 * 
 **/
 
 
/**
 * @author Jean-François Fournier
 */
public class JoueurVirtuel extends Joueur implements Runnable {
	
	// Cette variable va contenir le nom du joueur virtuel
	private String strNom;	
	
    // Déclaration d'une référence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;
	
	// Cette variable contient la case ciblée par la joueur virtuel.
	// Il tentera de s'y rendre. Cette case sera choisie selon 
	// sa valeur en points et le type de joueur virtuel, en général,
	// cette case possède une pièce, un objet ou un magasin.
	private Point objPositionFinaleVisee;
	
	// Cette variable conserve la raison pour laquelle le joueur
	// virtuel tente d'atteindre la position finale. Ceci est utile
	// pour détecter si, par exemple, l'objet que le joueur virtuel
	// voulait prendre n'existe plus.
	private int intRaisonPositionFinale;
	
	// Cette variable contient le niveau de difficulté du joueur virtuel
    private int intNiveauDifficulte;
	
	// Cette variable permet de savoir s'il faut arrêter le thread ou non
	private boolean bolStopThread;
	
	// Déclaration d'une référence vers la table courante
	private Table objTable;
	
    // Déclaration d'une variable qui va contenir le numéro Id du personnage 
	// du joueur virtuel
	private int intIdPersonnage;

    // Déclaration d'une variable qui va contenir le pointage de la 
    // partie du joueur virtuel
	private int intPointage;

	// Déclaration de la position du joueur virtuel dans le plateau de jeu
	private Point objPositionJoueur;

	// Déclaration d'une liste d'objets utilisables ramassés par le joueur
	// virtuel
	private TreeMap lstObjetsUtilisablesRamasses;
	
	// Déclaration d'une référence vers le controleur jeu
	private ControleurJeu objControleurJeu;
	
	
	// Déclaration d'une variable pour générer des nombres aléatoires
    //private Random objRandom;
	
	// Cette constante définit le temps de pause lors d'une rétroaction
	private final static int TEMPS_RETROACTION = 10;
	
	
	// Autres constantes utilisés dans les algorithmes de recherche de choix
    private final static int DROITE = 0;
    private final static int BAS = 1;
    private final static int GAUCHE = 2;
    private final static int HAUT = 3;
    
    // Déplacement maximum autorisé
    private final static int DEPLACEMENT_MAX = 6;
	
	// Constantes pour définir le niveu de difficulté du joueur virtuel
    public static final int DIFFICULTE_FACILE = 0;
    public static final int DIFFICULTE_MOYEN = 1;
    public static final int DIFFICULTE_DIFFICILE = 2;
	
	// Quelques raisons pour déplacer le joueur virtuel
	private final static int RAISON_AUCUNE = 0;
	private final static int RAISON_PIECE = 1;
	private final static int RAISON_MINIJEU = 2;
	private final static int RAISON_MAGASIN = 3;

	
	/**
	 * Constructeur de la classe JoueurVirtuel qui permet d'initialiser les 
	 * membres privés du joueur virtuel
	 * 
	 * @param String nom : Nom du joueur virtuel
	 * @param Integer niveauDifficulte : Le niveau de difficulté pour ce joueur
	 *                                   virtuel
	 * @param Table tableCourante: La table sur laquelle le joueur joue
	 * @param GestionnaireEvenements gestionnaireEv: Référence vers le gestionnaire
	 *        d'événements pour envoyer aux joueurs humains les mouvements
	 *        du joueur virtuel

	 */
	public JoueurVirtuel(String nom, int niveauDifficulte, Table tableCourante, 
	    GestionnaireEvenements gestionnaireEv, ControleurJeu controleur)
	{
	   
        // Préparation de l'objet pour créer des nombres aléatoires
        //Date d = new Date();
        //long seed = d.getTime();
        //objRandom = new Random(System.currentTimeMillis());
	   
	    objControleurJeu = controleur;
	    
		strNom = nom;
		
		// Cette variable sera utilisée dans la thread
		objPositionFinaleVisee = null;
		
		// Faire la référence vers le gestionnaire d'évenements
		objGestionnaireEv = gestionnaireEv;
			
		// Cette variable sert à arrêter la thread lorsqu'à true
		bolStopThread = false;		
			
		// Faire la référence vers la table courante
		objTable = tableCourante;	
			
		// Choisir un id de personnage aléatoirement
		intIdPersonnage = genererNbAleatoire(4) + 1;
		
		// Initialisation du pointage
		intPointage = 0;
		
		// Initialisation à null de la position, le joueur virtuel n'est nul part
		objPositionJoueur = null;
		
	    // Créer la liste des objets utilisables qui ont été ramassés
	    lstObjetsUtilisablesRamasses = new TreeMap();
		
        // Création du profil du joueur virtuel
        intNiveauDifficulte = niveauDifficulte;
        


	}


	/**
	 * Cette méthode est appelée lorsqu'une partie commence. C'est la thread
	 * qui fait jouer le joueur virtuel.
	 * 
	 */
	public void run()
	{
			
		// Cette variable conserve la case sur laquelle le joueur virtuel
		// tente de se déplacer
		Point objPositionIntermediaire = null;
		
		// Cette variable indique si le joueur virtuel a répondu correctement
		// à la question
		boolean bolQuestionReussie;
		
		// Cette variable contient le temps de réflexion pour répondre à 
		// la question
		int intTempsReflexionQuestion;
		
		// Cette variable contient le temps de réflexion pour choisir 
		// le prochain coup à jouer
        int intTempsReflexionCoup;
		
		// Cette variable contient le temps de pause pour le déplacement
		// du personnage
		int intTempsDeplacement;
		
		//System.out.println("Joueur virtuel démarré");
		
		while(bolStopThread == false)
		{		

			// Déterminer le temps de réflexion pour le prochain coup
			intTempsReflexionCoup = obtenirTempsReflexionCoup();
			
			// Pause pour moment de réflexion de décision
			pause(intTempsReflexionCoup);
			
            // Trouver une case intéressante à atteindre
            if (reviserPositioinFinaleVisee() == true)
            {
            	objPositionFinaleVisee = trouverPositionFinaleVisee();	
            }
                    
			// Trouver une case intermédiaire
			objPositionIntermediaire = trouverPositionIntermediaire();

			// S'il y a erreur de recherche ou si le joueur virtuel est pris
			// on ne le fait pas bouger
			if (objPositionIntermediaire.x != objPositionJoueur.x || 
			    objPositionIntermediaire.y != objPositionJoueur.y)
			{
    			// Déterminer si le joueur virtuel répondra à la question
                bolQuestionReussie = obtenirValiditeReponse(
                    obtenirPointage(objPositionJoueur, objPositionIntermediaire));
    			
    			// Déterminer le temps de réponse à la question
    			intTempsReflexionQuestion = obtenirTempsReflexionReponse();
                
    			// Pause pour moment de réflexion de réponse
    			pause(intTempsReflexionQuestion);	
    					
    			// Faire déplacer le personnage si le joueur virtuel a 
    			// réussi à répondre à la question
    			if (bolQuestionReussie == true)
    			{
    				// Déplacement du joueur virtuel
    				deplacerJoueurVirtuelEtMajPlateau(objPositionIntermediaire);
    				
    				// Obtenir le temps que le déplacement dure
    				intTempsDeplacement = obtenirTempsDeplacement(obtenirPointage(objPositionJoueur, objPositionIntermediaire));
    				
    				// Pause pour laisser le personnage se déplacer
    				pause(intTempsDeplacement);
    			}
    			else
    			{
    				// Pause pour rétroaction
    				pause(TEMPS_RETROACTION);
    			}
    			
    	    }	
		}
	}
	
	/* Cette fonction trouve le chemin le plus court entre deux points et
	 * le retourne sous forme de Vector. Le chemin retourné est en ordre inverse
	 * (l'indice 0 correspondra au point d'arrivée)
	 *
	 * @param: Point depart: Point de départ du chemin
	 * @param: Point arrivee: Point d'arrivée du chemin 
	 */
    public Vector trouverCheminPlusCourt(Point depart, Point arrivee)
    {
        // Tableau contenant une référence vers le plateau de jeu
        Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        int intNbLignes = objttPlateauJeu.length;
        int intNbColonnes = objttPlateauJeu[0].length;
        
        // Cette matrice contiendra les valeurs indiquants quelles cases ont
        // été parcourue par l'algorithme
        boolean matriceParcourue[][] = new boolean[intNbLignes][intNbColonnes];
        
        // Cette matrice contiendra, pour chaque case enfilée, de quelle case
        // celle-ci a été enfilée. Cela nous permettra de trouver le chemin
        // emprunté par l'algorithme. 
        Point matricePrec[][] = new Point[intNbLignes][intNbColonnes];
        
        // Liste des points à traiter pour l'algorithme de recherche de chemin
        Vector lstPointsATraiter = new Vector();
        
        // Le chemin résultat que l'on retourne à la fonction appelante
        Vector lstResultat;
        
        // Point temporaire qui sert dans l'algorithme de recherche
        Point ptPosTemp = new Point();
        
        // Point défilé de la liste des points à traiter
        Point ptPosDefile;
        
        // Cette variable nous indiquera si l'algorithme a trouvé un chemin
        boolean bolCheminTrouve = false;
        
        // Ce tableau nous permettra de traiter les 4 cases autour d'une case
        // à l'intérieur d'une boucle.
        Point ptDxDy[] = new Point[4];
        ptDxDy[DROITE] = new Point(0,1);
        ptDxDy[BAS] = new Point(1,0);
        ptDxDy[GAUCHE] = new Point(0,-1);
        ptDxDy[HAUT] = new Point(-1,0);
        
        // Variable pour boucler dans le tableau ptDxDy[]
        int dxIndex = 0;
        
        // Ce tableau servira à enfiler les cases de façons aléatoire, ce qui
        // permettra de peut-être trouver différents chemin
        int tRandom[] = {0,1,2,3};
        
        // Servira pour brasser tRandom
        int indiceA;
        int indiceB;
        int indiceNombreMelange;
        int valeurTemp;
        
        // Initialiser les objets pour la recherche de chemin
        for (int i = 0; i < intNbLignes; i++)
        {
            for (int j = 0; j < intNbColonnes; j++)
            {
                // On met chaque indice de la matrice des cases parcourues à false
                matriceParcourue[i][j] = false;
                
                // Chaque case précédente sera le point -1,-1
                matricePrec[i][j] = new Point(-1,-1);
            }
        }
        
        // Enfiler notre position de départ
        lstPointsATraiter.add(depart);
        matriceParcourue[depart.x][depart.y] = true;
                
        // On va boucler jusqu'à ce qu'il ne reste plus rien ou jusqu'à
        // ce qu'on arrive à l'arrivée
        while (lstPointsATraiter.size() > 0 && bolCheminTrouve == false)
        {
            // Défiler une position
            ptPosDefile = (Point) lstPointsATraiter.get(0);
            lstPointsATraiter.remove(0);
                       
            // Vérifier si on vient d'atteindre l'arrivée
            if (ptPosDefile.x == arrivee.x && ptPosDefile.y == arrivee.y)
            {
                bolCheminTrouve = true;
                break;
            }
            
            // On va faire 3 mélanges, ce sera suffisant
            for (indiceNombreMelange = 1; indiceNombreMelange <= 3;indiceNombreMelange++)
            {
                // Brasser aléatoirement le tableau aléatoire
                indiceA = genererNbAleatoire(4);
                indiceB = genererNbAleatoire(4); 
                
                // Permutter les deux valeurs
                valeurTemp = tRandom[indiceA];
                tRandom[indiceA] = tRandom[indiceB];
                tRandom[indiceB] = valeurTemp;
            }
                       
            // Enfiler les 4 cases accessibles depuis cette position  
            for (dxIndex = 0; dxIndex < 4; dxIndex++)
            {
                ptPosTemp.x = ptPosDefile.x + ptDxDy[tRandom[dxIndex]].x;
                ptPosTemp.y = ptPosDefile.y + ptDxDy[tRandom[dxIndex]].y;
                
                if (ptPosTemp.y >= 0 &&
                    ptPosTemp.y < intNbColonnes && 
                    ptPosTemp.x >= 0 &&
                    ptPosTemp.x < intNbLignes &&
                    matriceParcourue[ptPosTemp.x][ptPosTemp.y] == false &&
                    objttPlateauJeu[ptPosTemp.x][ptPosTemp.y] != null)
                {
                    // Ajouter la nouvelle case accessible
                    lstPointsATraiter.add(new Point(ptPosTemp.x, ptPosTemp.y));
                    
                    // Indiquer que cette case est traitée pour ne pas
                    // l'enfiler à nouveau
                    matriceParcourue[ptPosTemp.x][ptPosTemp.y] = true;
                    
                    // Conserver les traces pour savoir de quel case on a enfilé
                    matricePrec[ptPosTemp.x][ptPosTemp.y].x = ptPosDefile.x;
                    matricePrec[ptPosTemp.x][ptPosTemp.y].y = ptPosDefile.y;
                    
                }
            }


        }
        
        if (bolCheminTrouve == true)
        {
            // Préparer le chemin de retour
            lstResultat = new Vector();
            
            // On part de l'arrivée puis on retrace jusqu'au départ
            ptPosTemp = arrivee;

            // Ajouter chaque case indiqué dans matricePrec[] jusqu'à la
            // position de départ
            while (ptPosTemp.x != depart.x || ptPosTemp.y != depart.y)
            {
                lstResultat.add(new Point(ptPosTemp.x, ptPosTemp.y));
                ptPosTemp = matricePrec[ptPosTemp.x][ptPosTemp.y];
            }
            
            // Ajouter la position de départ
            lstResultat.add(new Point(depart.x, depart.y));
            
        }
        else
        {
            // Si on n'a pas trouvé de chemin, on retourne null
            lstResultat = null;
        }
        
        return lstResultat;
        
    }
	
	/* Cette fonction calcule les points pour un chemin. Les points sont basés sur
	 * le nombre de pièces que le chemin contient et aussi le type de case
	 * que le chemin contient au cas où le joueur virtuel préfèrerait certaines cases.
	 */
	private int calculerPointsChemin(Vector lstPositions, Case objttPlateauJeu[][])
	{
		Point ptTemp;
		int intPoints = 0;;
		
		for (int i = 0; i < lstPositions.size() - 1; i++)
		{

           ptTemp = (Point) lstPositions.get(i);
           
           if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseCouleur)
           {
           	   // Points pour une case couleur
               if (((CaseCouleur) objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() == null)
               {
               	   // Case couleur sans objet
		           //TODO: Ajouter points selon type de case et type de joueur
                   intPoints += 10;
               }
               else if (((CaseCouleur) objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof Piece)
               {
               	   // Piece sur la case
                   intPoints += 100;
               }
               else
               {
               	   // Autre objet
                   intPoints += 10;
               }
           }
           else
           {
           	   // Points pour une case pas couleur
               intPoints += 10;
           }
           
		}
		
		return intPoints;
	}
	
	/*
	 * Cette fonction trouve une case intermédiaire qui permettra au joueur virtuel
	 * de progresser vers sa mission qu'est celle de se rendre à la case finale visée.
	 */
	private Point trouverPositionIntermediaire()
	{
	    // Variable contenant la position à retourner à la fonction appelante
		Point objPositionTrouvee;
		
        // Tableau contenant une référence vers le plateau de jeu
        Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        int intNbLignes = objttPlateauJeu.length;
        int intNbColonnes = objttPlateauJeu[0].length;

        Vector lstPositions[] = new Vector[5];
        Vector lstPositionsTrouvees;
        int tPoints[] = new int[5];
        int intPlusGrand = -1;
        
        // Recherche de plusiuers chemins pour se rendre à la position finale
        for (int i = 0; i < 5; i++)
        {
            lstPositions[i] = trouverCheminPlusCourt(objPositionJoueur, objPositionFinaleVisee);
            
            // Vérifier si on a trouvé un chemin
            if (i == 0  && lstPositions[0] == null)
            {
            	return new Point(objPositionJoueur.x, objPositionJoueur.y);
            }
            
            // On va calculer les points pour ce chemin
            tPoints[i] = calculerPointsChemin(lstPositions[i], objttPlateauJeu);
            
            // Trouver le plus grand chemin
            if (intPlusGrand == -1 || tPoints[i] > tPoints[intPlusGrand])
            {
                intPlusGrand = i;            	
            }
        }
        
        // Choisir le meilleur chemin
        lstPositionsTrouvees = lstPositions[intPlusGrand];

        // Valeur du point de départ (égale à objPositionJoueur en principe)
        Point ptDepart = (Point) lstPositionsTrouvees.get(lstPositionsTrouvees.size() - 1);
        
        // Point temporaire qui nous permettra de parcourir la liste et trouver
        // où le joueur virtuel avancera
        Point ptTemp;
               
        // Obtenir les pourcentages de choix pour les cases selon le niveau
        // de difficulté, on va modifier ces pourcentages par la suite car il peut
        // y avoir des trous qu'on veut éviter, des pièces que l'on veut ramasser ou
        // bien une case finale que l'on ne veut pas dépasser
        int intPourcentageCase[] = obtenirPourcentageChoix();
        int iIndiceTableau = 0;
        
        // On part du début du chemin jusqu'à la fin et on trouve le premier croche
        for (int i = lstPositionsTrouvees.size() - 2; i >= 0 ; i--)
        {
            ptTemp = (Point) lstPositionsTrouvees.get(i);

            iIndiceTableau++;       
                                 
            // On vérifie si le premier "croche" est ici
            if (ptTemp.x != ptDepart.x && ptTemp.y != ptDepart.y)
            {
                // Le premier "croche" est à ptTemp, c'est donc le déplacement
                // maximal que le joueur virtuel pourra faire
                traiterPieceTrouveeDansLigne(intPourcentageCase, iIndiceTableau - 2);
                break;
            }
            
            // S'il y a une pièce sur cette case, alors on s'assure que
            // le joueur virtuel ne la dépassera pas
            if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseCouleur)
            {
                if (((CaseCouleur) objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof Piece)   
                {
                    traiterPieceTrouveeDansLigne(intPourcentageCase, iIndiceTableau - 1);
                    break;    
                }
            }
            

            
            if (iIndiceTableau > DEPLACEMENT_MAX-1)
            {
                break;
            }  
               
        }
        
        // Si on est près de la position finale, on s'assure de ne pas la dépasser
        if (lstPositionsTrouvees.size() < DEPLACEMENT_MAX)
        {
            traiterPieceTrouveeDansLigne(intPourcentageCase, lstPositionsTrouvees.size() - 2); 
        }
        
        // Effectuer le choix
        int intPourcentageAleatoire;
        
        // On génère un nombre entre 1 et 100
        intPourcentageAleatoire = genererNbAleatoire(100)+1;

        
        int intValeurAccumulee = 0;
        int intDecision = 0;
        
        // On détermine à quel décision cela appartient
        for (int i = 0 ; i <= DEPLACEMENT_MAX-1 ; i++)
        {
            intValeurAccumulee += intPourcentageCase[i];
            if (intPourcentageAleatoire <= intValeurAccumulee)
            {
                intDecision = i + 1;
                break;
            }
        }
        

        // On peut donc retourner la case choisie par le joueur virtuel
        ptTemp = (Point)lstPositionsTrouvees.get(lstPositionsTrouvees.size() - 1 - intDecision);
        objPositionTrouvee = new Point(ptTemp.x, ptTemp.y);

        //--------------------------------
        /*System.out.println("Position du joueur: " + objPositionJoueur.x + "," + 
            objPositionJoueur.y);        
        System.out.println("Position trouvée: " + objPositionTrouvee.x + "," + 
            objPositionTrouvee.y);           
        System.out.println("Position a atteindre: " + objPositionFinaleVisee.x + "," + 
            objPositionFinaleVisee.y);
        TestJoueurVirtuel.outputPlateau(objttPlateauJeu);*/
        //--------------------------------
        
        return objPositionTrouvee;

	}
	
	/*
	 * Cette fonction trouve une position finale que le joueur virtuel va tenter
	 * d'atteindre. C'est ici que la personnalité du joueur peut influencer la décision.
	 * Par la suite, le joueur virtuel devra choisir des cases intermédiaires pour se
	 * rendre à la case finale, cela peut être immédiat au prochain coup.
	 */
	private Point trouverPositionFinaleVisee()
	{
		
		// Position trouvée par l'algorithme
		Point objPositionTrouvee = null;
		
        // Tableau contenant une référence vers le plateau de jeu
        Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        int intNbLignes = objttPlateauJeu.length;
        int intNbColonnes = objttPlateauJeu[0].length;

        // Déclaration d'une matrice qui contiendra un pointage pour chaque
        // case du plateau de jeu, ce qui permettra de choisir le meilleur
        // coup à jouer
        int matPoints[][] = new int[intNbLignes][intNbColonnes];

        // Cette variable contiendra le nombre de coups estimé pour se rendre
        // à la case en cours d'analyse
        double dblDistance;
        
        // Point en cours d'analyse
        Point ptTemp = new Point(0,0);
        
        // Autre point en cours d'analyse
        Point ptTemp2 = new Point(0,0);
        
        // Chemin entre le joueur et une case importante analysée
        Vector lstChemin;

        // Déplacement moyen, contient le nombre de cases que l'on peut
        // s'attendre à franchir par coup (prend en compte niveau de
        // difficulté)
        double dblDeplacementMoyen = obtenirDeplacementMoyen();
        
        // Ce tableau contiendra les 5 cases les plus intéressantes
        Point tPlusGrand[] = new Point[5];
        
        // Ce tableau contient des paramètres pour prioriser les
        // regroupement de pièces
        int[][] ttPointsRegion = 
                    {{ 10, 10,  0,  0,  0,  0,  0}, 
                     { 20, 15, 10  ,0,  0,  0,  0},
                     { 50, 30, 15, 10,  0,  0,  0},
                     {100, 75, 30, 15, 10,  0,  0},
                     {200,150, 75, 30, 15, 10,  0},
                     {400,300,150, 75, 30, 15, 10},
                     {500,400,200,100, 50, 20, 10}};
               
        // Initialiser la matrice
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                // Pointage de départ (environ 0)
                matPoints[x][y] = -50 + genererNbAleatoire(101);
            }
        }
        
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                ptTemp.x = x;
                ptTemp.y = y;
                    
                if (objPositionJoueur.x == x && objPositionJoueur.y == y)
                {
                    // La position courante du joueur ne doit pas être choisie
                    matPoints[x][y] = -999999999;
                }
                else
                {

                    // Modification du pointage de la case
                    if (objttPlateauJeu[x][y] == null)
                    {
                        // Une case nulle ne doit pas être chosie
                        matPoints[x][y] = -999999999;
                    }
                    else if(objttPlateauJeu[x][y] instanceof CaseCouleur && 
                        ((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase() instanceof Piece)
                    {
                        
                        // Une pièce augmente d'environ 5000 le pointage
                        matPoints[x][y] += 4950 + genererNbAleatoire(101);
                    
                        // On va trouver le chemin le plus court
                        lstChemin = trouverCheminPlusCourt(objPositionJoueur, ptTemp);
                        
                        if (lstChemin == null)
                        {
                            // Une case inaccessible ne doit pas être choisie
                            matPoints[x][y] = -999999999;
                        }
                        else
                        {
                            // On a un chemin qui contient chaque case, maintenant, on
                            // va trouver, pour ce chemin, le nombre de coups estimé
                            // pour le parcourir, et ce, en prenant en compte le niveau
                            // de difficulté
                            // TODO:Prendre en compte nombre de croches
                            dblDistance = lstChemin.size() / dblDeplacementMoyen;
                            

                            // Pour permettre de quand même prioriser les pièces
                            // lointaines, on va limiter le nombre de coups
                            // ce qui enlèvera 4800 points pour une pièce lointaine
                            if (dblDistance > 6)
                            {
                                dblDistance = 6;
                            }
                            
                            // Plus la pièce est loin, plus son pointage diminue
                            // On enlève 800 points par coup
                            matPoints[x][y] -= (int) (800 * dblDistance + .5);

                            // Cette pièce étant accessible, on va augmenter les
                            // points des cases aux alentours pour attirer
                            // le joueur virtuel vers des regroupements de pièces
                            for (int i = -6; i <= 6; i++)
                            {
                                for (int j = -6; j <= 6; j++)
                                {
                                    ptTemp2.x = x + i;
                                    ptTemp2.y = y + j;
                                    
                                    if (ptTemp2.x >= 0 && ptTemp2.x < intNbLignes &&
                                        ptTemp2.y >=0 && ptTemp2.y < intNbColonnes &&
                                        objttPlateauJeu[ptTemp2.x][ptTemp2.y] != null)  
                                    {
                                        matPoints[ptTemp2.x][ptTemp2.y] += ttPointsRegion[6 - Math.abs(i)][Math.abs(j)];
                                    }  
                                } 
                            }
                        }
                        
                    }
                }

                
            }
        }
         
         // On va maintenant trouver les 5 meilleurs déplacements
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                // Gestion de la liste des 5 plus grands
                // On ajoute la case qu'on est en train de parcourir dans la liste
                // des 5 plus grands pointage si elle est digne d'y être
                for (int i = 0; i < 5; i++)
                {
                    if (tPlusGrand[i] == null)
                    {
                        tPlusGrand[i] = new Point(x, y);
                        break;
                    }
                    else if (matPoints[x][y] > matPoints[tPlusGrand[i].x][tPlusGrand[i].y])
                    {
                        // Tout décaler vers la droite
                        for (int j = 4; j > i; j--)
                        {
                        	if (tPlusGrand[j-1] != null)
                        	{
                        		if (tPlusGrand[j] == null)
                        		{
                        			tPlusGrand[j] = new Point(tPlusGrand[j-1].x, tPlusGrand[j-1].y);
                        		}
                        		else
                        		{
                                    tPlusGrand[j].x = tPlusGrand[j - 1].x;
                                    tPlusGrand[j].y = tPlusGrand[j - 1].y;
                                }
                            }
                        }
                        
                        // Insérer notre élément
                        tPlusGrand[i].x = x;
                        tPlusGrand[i].y = y;

                        break;
                    }
                }   
            }
        }

        // Maintenant, on rend le joueur virtuel faillible et on fait en sorte
        // qu'il ne choisisse pas toujours le meilleur choix
        int intDifferenceMax = obtenirNombrePointsMaximumChoixFinal();
        
        // Nombre de choix possible qui ne dépasse pas la limite de intDifferenceMax
        int intNombreChoix = 1;
        
        // Valeur maximum pour générer la valeur aléatoire
        int intValeurMax;
        
        // Valeur aléatoire permettant d'effectuer le choix
        int intValeurAleatoire;

        // Tableau contenant le pourcentage des choix alternatifs
        int tPourcentageChoix[] = obtenirPourcentageChoixAlternatifFinal();
        
        // La décision selon le résultat aléatoire
        int intDecision = 0;
        
        // Valeur accumulée pour trouver la décision correspondante
        int intValeurAccumulee = 0;
        
        // On doit trouver le nombre de choix possible pour le joueur virtuel
        // selon la différence maximum calculée (qui tient compte du niveau
        // de difficulté)
        intValeurMax = tPourcentageChoix[0];
        for (int i = 1; i < 5; i ++)
        {
        	if (matPoints[tPlusGrand[i].x][tPlusGrand[i].y] < 0 || 
        	    matPoints[tPlusGrand[i].x][tPlusGrand[i].y] < 
        	    matPoints[tPlusGrand[0].x][tPlusGrand[0].y] - intDifferenceMax)
        	{
        		// Ce choix est en-dessous de la limite permise pour
        		// ce niveau de difficulté
        		intNombreChoix = i;
        		break;
        	}
        	else
        	{
        		intValeurMax += tPourcentageChoix[i];
        	}
        }
        
        // On va chercher un nombre entre 1 et la valeur max inclusivement
        intValeurAleatoire = genererNbAleatoire(intValeurMax) + 1;
        
        // Ce nombre correspond à notre choix
        for (int i = 0; i < intNombreChoix; i++)
        {
        	intValeurAccumulee += tPourcentageChoix[i];
            if (intValeurAleatoire <= intValeurAccumulee)
            {
            	intDecision = i;
            }
        }

        // Déterminer la raison
        intRaisonPositionFinale = RAISON_AUCUNE;
        if (objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y] instanceof CaseCouleur)
        {
        	if (((CaseCouleur)objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y]).obtenirObjetCase() instanceof Piece)
        	{
        		intRaisonPositionFinale = RAISON_PIECE;
        	}
        }
        
        // Retourner la position trouvée     
        objPositionTrouvee = new Point(tPlusGrand[intDecision].x, tPlusGrand[intDecision].y);
        return objPositionTrouvee;
		
		/*	
		// Choisir la case aléatoirement
		int x, y;
        int nbLignes = objTable.obtenirPlateauJeuCourant().length;
        int nbColonnes = objTable.obtenirPlateauJeuCourant()[0].length;
		
		Boolean bolTerminee = false;
		while (bolTerminee == false)
		{
            x = genererNbAleatoire(nbLignes);
            y = genererNbAleatoire(nbColonnes);
		    
            //System.out.println("Dimension du plateau : " + nbLignes + "," + nbColonnes);
		    //System.out.println("Position random: " + x + "," + y); 
		     
		    if (objTable.obtenirPlateauJeuCourant()[x][y] != null && x != objPositionJoueur.x &&
		        objPositionJoueur.y != y)
		    {
		    	objPositionTrouvee = new Point(x, y);
		        bolTerminee = true;
		    }
		        
		}
        return objPositionTrouvee;
        */

	}

      
    /*
     * Cette fonction s'occupe de déplacer le joueur virtuel s'il a bien répondu
     * à la question, met à jour le plateau de jeu, envoie les événements aux autres joueurs
     * et modifie le pointage et la position du joueur virtuel
     */
    private void deplacerJoueurVirtuelEtMajPlateau(Point objNouvellePosition)
    {
        String collision = "";
                
        // Déclaration d'une référence vers l'objet ramassé
        ObjetUtilisable objObjetRamasse = null;
        
        // Déclaration d'une référence vers l'objet subi
        ObjetUtilisable objObjetSubi = null;
        
        // Faire la référence vers la case de destination
        Case objCaseDestination = objTable.obtenirPlateauJeuCourant()[objNouvellePosition.x][objNouvellePosition.y];
            
        // Le pointage est initialement celui courant
        int intNouveauPointage = intPointage;   
            
        // Calculer le nouveau pointage du joueur (on ajoute la difficulté 
        // de la question au pointage)
        intNouveauPointage += obtenirPointage(objPositionJoueur, objNouvellePosition);
        
        // Si la case de destination est une case de couleur, alors on 
        // vérifie l'objet qu'il y a dessus et si c'est un objet utilisable, 
        // alors on l'enlève et on le donne au joueur virtuel, sinon si c'est une 
        // pièce on l'enlève et on met à jour le pointage, sinon 
        // on ne fait rien
        if (objCaseDestination instanceof CaseCouleur)
        {
            // Faire la référence vers la case de couleur
            CaseCouleur objCaseCouleurDestination = (CaseCouleur) objCaseDestination;
                    
            // S'il y a un objet sur la case, alors on va faire l'action 
            // tout dépendant de l'objet (pièce, objet utilisable ou autre)
            if (objCaseCouleurDestination.obtenirObjetCase() != null)
            {
                // Si l'objet est un objet utilisable, alors on l'ajoute à 
                // la liste des objets utilisables du joueur virtuel
                if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable)
                {
                    // Faire la référence vers l'objet utilisable
                    ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetCase();
                    
                    // Garder la référence vers l'objet utilisable pour l'ajouter à l'objet de retour
                    objObjetRamasse = objObjetUtilisable;
                    
                    // Ajouter l'objet ramassé dans la liste des objets du joueur virtuel
                    lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
                    
                    // Enlever l'objet de la case du plateau de jeu
                    objCaseCouleurDestination.definirObjetCase(null);
                }
                else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece)
                {
                    // Faire la référence vers la pièce
                    Piece objPiece = (Piece) objCaseCouleurDestination.obtenirObjetCase();
                    
                    // Mettre à jour le pointage du joueur virtuel
                    intNouveauPointage += objPiece.obtenirValeur();
                    
                    // Enlever la pièce de la case du plateau de jeu
                    objCaseCouleurDestination.definirObjetCase(null);
                    
                    collision = "piece";
                    

                    
                    // TODO: Il faut peut-être lancer un algo qui va placer 
                    //       les pièces sur le plateau de jeu s'il n'y en n'a
                    //       plus
                }

            }
            
            // S'il y a un objet à subir sur la case, alors on va faire une
            // certaine action (TODO: à compléter)
            if (objCaseCouleurDestination.obtenirObjetArme() != null)
            {
                // Faire la référence vers l'objet utilisable
                ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetArme();
                
                // Garder la référence vers l'objet utilisable à subir
                objObjetSubi = objObjetUtilisable;
                
                //TODO: Faire une certaine action au joueur virtuel
                
                // Enlever l'objet subi de la case
                objCaseCouleurDestination.definirObjetArme(null);
            }
            
        }
        else
        { 
            //TODO: Émuler mini-jeu
        }
                
        synchronized (objTable.obtenirListeJoueurs())
        {
            // Préparer l'événement de deplacement de personnage. 
            // Cette fonction va passer les joueurs et créer un 
            // InformationDestination pour chacun et ajouter l'événement 
            // dans la file de gestion d'événements
            preparerEvenementJoueurVirtuelDeplacePersonnage(collision, objNouvellePosition);                
        }
        
        // Mettre à jour pointage et position du joueur virtuel
        objPositionJoueur = objNouvellePosition;
        intPointage = intNouveauPointage;

    }
    
    /*
     * Cette fonction prépare l'événement indiquant que le joueur virtuel se déplace
     */
    private void preparerEvenementJoueurVirtuelDeplacePersonnage( String collision, Point objNouvellePosition )
    {

        EvenementJoueurDeplacePersonnage joueurDeplacePersonnage = new EvenementJoueurDeplacePersonnage(strNom, objPositionJoueur, objNouvellePosition, collision );
        
        // Créer un ensemble contenant tous les tuples de la liste 
        // des joueurs de la table (chaque élément est un Map.Entry)
        Set lstEnsembleJoueurs = objTable.obtenirListeJoueurs().entrySet();
        
        // Obtenir un itérateur pour l'ensemble contenant les joueurs
        Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
        
        // Passer tous les joueurs de la table et leur envoyer un événement
        while (objIterateurListe.hasNext() == true)
        {
            // Créer une référence vers le joueur humain courant dans la liste
            JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

            // Créer un InformationDestination et l'ajouter à l'événement
            joueurDeplacePersonnage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                                                                                             objJoueur.obtenirProtocoleJoueur()));
        }
        
        // Ajouter le nouvel événement créé dans la liste d'événements à traiter
        objGestionnaireEv.ajouterEvenement(joueurDeplacePersonnage);
    }
    


    private int genererNbAleatoire(int max)
    {
        //return objRandom.nextInt(max);
        return objControleurJeu.genererNbAleatoire(max);
    }
    
    
    public void definirPositionJoueurVirtuel(Point pos)
    {
        objPositionJoueur = new Point(pos.x, pos.y);
    }

    /*
     * Cette fonction fait une pause de X secondes émulant une réflexion
     * par le joueur virtuel
     */
    private void pause(Integer nbSecondes)
    {
    	try
    	{
    	    Thread.sleep(nbSecondes * 1000);
    	}
    	catch(InterruptedException e)
    	{ 
    	}

    }
  
	
    /* 
     * Cette fonction retourne le pointage d'un déplacement
     *
     */
    private int obtenirPointage(Point ptFrom, Point ptTo)
    {
    	if (ptFrom.x == ptTo.x)
    	{
    		return Math.abs(ptFrom.y - ptTo.y);
    	}
    	else
    	{
    		return Math.abs(ptFrom.x - ptTo.x);
    	}
    }
    
    /* 
     * Fonction de service utilisée dans l'algorithme de recherche de 
     * position qui permet de modifier les pourcentages du choix à faire.
     * La fonction prend un tableau de longueur X et un indice du tableau. 
     * De indice + 1 à X - 1, on ajoute les valeurs à tableau[indice]
     * puis on met à zéro ces indices
     */
    private void traiterPieceTrouveeDansLigne(int tPourcentageCase[], int indice)
    {
    	int x;
    	if (indice + 1 <= DEPLACEMENT_MAX - 1 && indice >= 0)
    	{
    	    for(x = indice + 1; x <= DEPLACEMENT_MAX - 1; x++)
    	    {
    	    	tPourcentageCase[indice] += tPourcentageCase[x];
    	    	tPourcentageCase[x] = 0;
    	    }
    	
    	}
    }

    public int obtenirIdPersonnage()
    {
    	return intIdPersonnage;
    }
    
    
    /* Cette fonction permet d'obtenir le nom du joueur virtuel
     */
    public String obtenirNom()
    {
        return strNom;
    }
    
    /* Cette fonction permet d'obtenir le pointage du joueur virtuel
     */
    public int obtenirPointage()
    {
        return intPointage;
    }
    
    /* Cette fonction permet à la boucle dans run() de s'arrêter
     */
    public void arreterThread()
    {
        bolStopThread = true;
    }
    
    /* Cette fonction permet d'obtenir un tableau qui contient les pourcentages de
     * choix de déplacement pour chaque grandeur de déplacement. Ces pourcentages
     * sont basés sur le niveau de difficulté du joueur virtuel
     */
    private int[] obtenirPourcentageChoix()
    {
        int intPourcentageCase[] = new int[DEPLACEMENT_MAX];
        
        switch (intNiveauDifficulte)
        {
            case DIFFICULTE_FACILE:
                intPourcentageCase[0] = 50;
                intPourcentageCase[1] = 30;
                intPourcentageCase[2] = 19;
                intPourcentageCase[3] = 1;
                intPourcentageCase[4] = 0;
                intPourcentageCase[5] = 0;                  
                break;
                
            case DIFFICULTE_MOYEN:
                intPourcentageCase[0] = 5;
                intPourcentageCase[1] = 19;
                intPourcentageCase[2] = 40;
                intPourcentageCase[3] = 25;
                intPourcentageCase[4] = 10;
                intPourcentageCase[5] = 1;  
                break;
                
            case DIFFICULTE_DIFFICILE:
                intPourcentageCase[0] = 0;
                intPourcentageCase[1] = 5;
                intPourcentageCase[2] = 15;
                intPourcentageCase[3] = 40;
                intPourcentageCase[4] = 30;
                intPourcentageCase[5] = 10;  
                break;
        }
        
        return intPourcentageCase;
    }
    
    /* Cette fonction permet d'obtenir un tableau qui indique, pour chaque grandeur
     * de déplacement, le pourcentage de réussite à la question. Ce pourcentage est
     * basé sur le niveau de difficulté du joueur virtuel
     */
    private int[] obtenirPourcentageReponse()
    {
        int intPourcentageCase[] = new int[DEPLACEMENT_MAX];
        
        switch (intNiveauDifficulte)
        {
            case DIFFICULTE_FACILE:
                intPourcentageCase[0] = 90;
                intPourcentageCase[1] = 80;
                intPourcentageCase[2] = 60;
                intPourcentageCase[3] = 10;
                intPourcentageCase[4] = 0;
                intPourcentageCase[5] = 0;                  
                break;
                
            case DIFFICULTE_MOYEN:
                intPourcentageCase[0] = 95;
                intPourcentageCase[1] = 90;
                intPourcentageCase[2] = 85;
                intPourcentageCase[3] = 50;
                intPourcentageCase[4] = 30;
                intPourcentageCase[5] = 15;  
                break;
                
            case DIFFICULTE_DIFFICILE:
                intPourcentageCase[0] = 100;
                intPourcentageCase[1] = 95;
                intPourcentageCase[2] = 90;
                intPourcentageCase[3] = 80;
                intPourcentageCase[4] = 70;
                intPourcentageCase[5] = 60;  
                break;
        }
        
        return intPourcentageCase;
    }
    
    /* Cette fonction permet d'obtenir le temps de réflexion d'un joueur
     * virtuel pour planifier son prochain coup. Ce temps est basé sur le niveau
     * de difficulté du joueur virtuel et comprend un élément aléatoire.
     */
    private int obtenirTempsReflexionCoup()
    {
        int intTemps = 0;
        
        switch (intNiveauDifficulte)
        {
            case DIFFICULTE_FACILE:  
                intTemps = 3 + genererNbAleatoire(4);           
                break;
                
            case DIFFICULTE_MOYEN:
                intTemps = 2 + genererNbAleatoire(3); 
                break;
                
            case DIFFICULTE_DIFFICILE:
                intTemps = 1 + genererNbAleatoire(2); 
                break; 
        }
        
        return intTemps;
    }
    
    
    /* Cette fonction permet d'obtenir le temps de réflexion d'un joueur
     * virtuel lorsqu'il répond à une question. Ce temps est basé sur le niveau
     * de difficulté du joueur virtuel et comprend un élément aléatoire.
     */
    private int obtenirTempsReflexionReponse()
    {
        int intTemps = 0;
        
        switch (intNiveauDifficulte)
        {
            case DIFFICULTE_FACILE: 
                intTemps = 24 + genererNbAleatoire(12);           
                break;
                
            case DIFFICULTE_MOYEN:
                intTemps = 16 + genererNbAleatoire(10); 
                break;
                
            case DIFFICULTE_DIFFICILE:
                intTemps = 8 + genererNbAleatoire(8); 
                break; 
        }
        
        return intTemps;
    }
    
    /* Cette fonction permet de savoir si le joueur virtuel répondra correctement
     * à la question. En paramètre, la grandeur du déplacement que le joueur virtuel
     * demande.
     */
    private boolean obtenirValiditeReponse(int grandeurDeplacement)
    {
        int intPourcentageReponse[] = obtenirPourcentageReponse();
        
        if (grandeurDeplacement < 1 || grandeurDeplacement > DEPLACEMENT_MAX)
        {
            return false;
        }
        
        // Générer un nombre aléatoire
        int intValeurAleatoire = genererNbAleatoire(100)+1;
        
        if (intValeurAleatoire <= intPourcentageReponse[grandeurDeplacement - 1])
        {
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    /* Cette fonction obtient le nombre de cases que le joueur virtuel franchira
     * en moyenne selon son niveau de difficulté.
     * NOTE: Pré-calculé et hard-codé selon les pourcentages de choix et de réussites
     *       pour les différentes grandeurs de déplacements
     */
    private double obtenirDeplacementMoyen()
    {
        
        switch (intNiveauDifficulte)
        {
            case DIFFICULTE_FACILE: 
                return 1.276;          
                
            case DIFFICULTE_MOYEN:
                return 2.0685;
                
            case DIFFICULTE_DIFFICILE:
                return 3.19;
        }

        return 1;
    }
    
    /* Cette fonction retourne le temps en secondes que dure un déplacement
     * de joueur selon le nombre de cases du déplacement.
     */
    private int obtenirTempsDeplacement(int nombreCase)
    {
    	if (nombreCase < 4)
    	{
    		return nombreCase;
    	}
    	else
    	{
    		return nombreCase - 1;
    	}
    }
    
    /* Cette fonction retourne le nombre de points maximum qu'un joueur
     * virtuel peut négliger lors du choix de la position finale
     */
    private int obtenirNombrePointsMaximumChoixFinal()
    {
        switch (intNiveauDifficulte)
        {
            case DIFFICULTE_FACILE: 
                return 2000;          
                
            case DIFFICULTE_MOYEN:
                return 1000;
                
            case DIFFICULTE_DIFFICILE:
                return 400;
        }

        return 0;
    }
     
    /* Cette fonction retourne un tableau contenant les pourcentages pour les 
     * choix alternatifs de positions finales selon le niveau de difficulté
     */
    private int[] obtenirPourcentageChoixAlternatifFinal()
    {
		int intPourcentageChoix[] = new int[5];
	    	
		switch (intNiveauDifficulte)
		{
		    case DIFFICULTE_FACILE:
		        intPourcentageChoix[0] = 70;
		        intPourcentageChoix[1] = 20;
		        intPourcentageChoix[2] = 5;
		        intPourcentageChoix[3] = 4;
		        intPourcentageChoix[4] = 1; 
		        break;
		        
		    case DIFFICULTE_MOYEN:
		        intPourcentageChoix[0] = 80;
		        intPourcentageChoix[1] = 17;
		        intPourcentageChoix[2] = 2;
		        intPourcentageChoix[3] = 1;
		        intPourcentageChoix[4] = 0; 
		        break;
		        
		    case DIFFICULTE_DIFFICILE:
		        intPourcentageChoix[0] = 90;
		        intPourcentageChoix[1] = 8;
		        intPourcentageChoix[2] = 2;
		        intPourcentageChoix[3] = 0;
		        intPourcentageChoix[4] = 0;  

		        break;
		}
		
		return intPourcentageChoix;
    }
    
    /* Cette fonction permet de savoir si c'est le temps de calculer 
     * une nouvelle position finale visée par le joueur virtuel. On
     * fait cela dans les circonstances suivantes:
     *
     * - Aucune position encore trouvée (début)
     * - Le joueur a atteint la position qu'il visait
     * - L'état de la case visée a changé (l'objet a disparu)
     */
    private boolean reviserPositioinFinaleVisee()
    {
    	// Vérifier si aucune position trouvée
    	if (objPositionFinaleVisee == null)
    	{
    		return true;
    	}
    	
    	// Vérifier si on a atteint la position précédamment visée
    	if (objPositionJoueur.x == objPositionFinaleVisee.x &&
    	    objPositionJoueur.y == objPositionFinaleVisee.y)
    	{
    		return true;
    	}
    	
    	// Aller chercher le plateau de jeu
    	Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Vérifier si l'état de la case a changé
        switch (intRaisonPositionFinale)
        {
        	case RAISON_AUCUNE: 
        	
	        	// Aucune raison = erreur en général, donc on va recalculer
	            // une position finale
	            return true;

            
        	case RAISON_PIECE:
        	
	        	// Vérifier si la pièce a été capturée
	    	    if (((CaseCouleur)objttPlateauJeu[objPositionFinaleVisee.x][objPositionFinaleVisee.y]).obtenirObjetCase() == null)
	    	    {
	    	    	return true;
	    	    }
	    	    else
	    	    {
	    	    	return false;
	    	    }    
        }
        
        // Dans les autres cas, ce n'est pas nécessaire de rechercher
        // tout de suite (on attend que le joueur virtuel atteigne la
        // position finale avant de recalculer une position)
        return false;

    }
}
