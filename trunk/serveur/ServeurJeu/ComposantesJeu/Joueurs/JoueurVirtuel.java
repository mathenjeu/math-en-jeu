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


/* Priorit� haute
 * -----------------
 * TODO: Ne pas utiliser "instanceof" mais utiliser obtenirTypeCase
 *
 * Priorit� moyenne
 * -----------------
 * TODO: Optimiser l'utilisation des matrices (r�utiliser les m�mes)
 * TODO: Prendre en compte magasins, mini-jeu et objets lors des choix
 * TODO: Profil: Importance magasin, mini-jeu et objets
 * TODO: �muler mini-jeu
 * TODO: Achat magasin
 * TODO: Utiliser objet
 * TODO: Noms des joueurs virtuels
 * TODO: Conserver le chemin trouv� dans trouverPosFinale pour posIntermediaire
 * TODO: Optimisation: Analyser la pertinence d'utiliser des listes des cases
 *       importantes et de v�rifier ces listes plut�t que de parcourir toute
 *       la matrice du plateau de jeu lorsqu'on cherche une case finale
 * TODO: Case finale: am�liorer en prenant en compte les autres joueurs
 *
 * Priorit� basse
 * -----------------
 * TODO: Dans l'algorithme de valorisation de regroupements de pi�ces, prendre
 *       en compte le fait que la case se trouve par-dessus un trou (ou qu'elle est trop loin)
 * TODO: Case finale: am�liorer pour ce qui est des croches (le joueur virtuel
 *       ne voit pas de diff�rence entre 3 cases en ligne droite et 3 cases 
 *       avec un croche)
 * TODO: Am�liorer la case finale si jamais il n'y a plus de pi�ces
 *       (trouver de grands d�placements en ligne droite qui permet
 *        d'avoir des d�placements maximales)
 * TODO: Profil: prendre en compte position des ennemis
 * TODO: Profil: aggressivit�: Joueurs aggressif essaie de "voler" des pi�ces, alors
 *       que les joueurs passifs restent plus � l'�cart, impact aussi sur les objets
 *       que le joueur ach�tera
 * TODO: Profil: Prioriser certains objets (en plus de ceux prioris�s par l'aggressivit�, 
 *       par exemple, on pourrait avoir un joueur virtuel qui adore jouer un certain
 *       objet m�me si l'objet est tr�s mauvais)
 * TODO: Profil: R�action lorsque gagne / perd (augmenter/diminuer t�m�rit�, un joueur
 *       qui perd avec un tr�s grand �cart va tenter de plus grands mouvements)
 * TODO: Pos finale: prendre en compte quelques coups d'avance
 * TODO: Profil: Certain joueur virtuel meilleur en statistique, alg�bre, etc, 
 *       donc prioriser certains cases couleurs ainsi que les pi�ces sur ces cases
 *       couleurs.
 * TODO: Profil: On ne demande qu'un niveau de difficult�, le reste est g�n�rer al�atoirement
 *       En g�n�ral, on obtient des settings prochent de la normal, mais il est possible
 *       de se ramasser quelque fois avec des joueurs un peu sp�cial (tr�s aggressif,
 *       tr�s port� � jouer � des mini-jeu sans arr�t, fixation sur tel objet, etc.)
 * TODO: Prioriser mouvements au centre du plateau plut�t que dans les c�t�s
 * 
 **/
 
 
/**
 * @author Jean-Fran�ois Fournier
 */
public class JoueurVirtuel extends Joueur implements Runnable {
	
	// Cette variable va contenir le nom du joueur virtuel
	private String strNom;	
	
    // D�claration d'une r�f�rence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;
	
	// Cette variable contient la case cibl�e par la joueur virtuel.
	// Il tentera de s'y rendre. Cette case sera choisie selon 
	// sa valeur en points et le type de joueur virtuel, en g�n�ral,
	// cette case poss�de une pi�ce, un objet ou un magasin.
	private Point objPositionFinaleVisee;
	
	// Cette variable conserve la raison pour laquelle le joueur
	// virtuel tente d'atteindre la position finale. Ceci est utile
	// pour d�tecter si, par exemple, l'objet que le joueur virtuel
	// voulait prendre n'existe plus.
	private int intRaisonPositionFinale;
	
	// Cette variable contient le niveau de difficult� du joueur virtuel
    private int intNiveauDifficulte;
	
	// Cette variable permet de savoir s'il faut arr�ter le thread ou non
	private boolean bolStopThread;
	
	// D�claration d'une r�f�rence vers la table courante
	private Table objTable;
	
    // D�claration d'une variable qui va contenir le num�ro Id du personnage 
	// du joueur virtuel
	private int intIdPersonnage;

    // D�claration d'une variable qui va contenir le pointage de la 
    // partie du joueur virtuel
	private int intPointage;

	// D�claration de la position du joueur virtuel dans le plateau de jeu
	private Point objPositionJoueur;

	// D�claration d'une liste d'objets utilisables ramass�s par le joueur
	// virtuel
	private TreeMap lstObjetsUtilisablesRamasses;
	
	// D�claration d'une r�f�rence vers le controleur jeu
	private ControleurJeu objControleurJeu;
	
	
	// D�claration d'une variable pour g�n�rer des nombres al�atoires
    //private Random objRandom;
	
	// Cette constante d�finit le temps de pause lors d'une r�troaction
	private final static int TEMPS_RETROACTION = 10;
	
	
	// Autres constantes utilis�s dans les algorithmes de recherche de choix
    private final static int DROITE = 0;
    private final static int BAS = 1;
    private final static int GAUCHE = 2;
    private final static int HAUT = 3;
    
    // D�placement maximum autoris�
    private final static int DEPLACEMENT_MAX = 6;
	
	// Constantes pour d�finir le niveu de difficult� du joueur virtuel
    public static final int DIFFICULTE_FACILE = 0;
    public static final int DIFFICULTE_MOYEN = 1;
    public static final int DIFFICULTE_DIFFICILE = 2;
	
	// Quelques raisons pour d�placer le joueur virtuel
	private final static int RAISON_AUCUNE = 0;
	private final static int RAISON_PIECE = 1;
	private final static int RAISON_MINIJEU = 2;
	private final static int RAISON_MAGASIN = 3;

	
	/**
	 * Constructeur de la classe JoueurVirtuel qui permet d'initialiser les 
	 * membres priv�s du joueur virtuel
	 * 
	 * @param String nom : Nom du joueur virtuel
	 * @param Integer niveauDifficulte : Le niveau de difficult� pour ce joueur
	 *                                   virtuel
	 * @param Table tableCourante: La table sur laquelle le joueur joue
	 * @param GestionnaireEvenements gestionnaireEv: R�f�rence vers le gestionnaire
	 *        d'�v�nements pour envoyer aux joueurs humains les mouvements
	 *        du joueur virtuel

	 */
	public JoueurVirtuel(String nom, int niveauDifficulte, Table tableCourante, 
	    GestionnaireEvenements gestionnaireEv, ControleurJeu controleur)
	{
	   
        // Pr�paration de l'objet pour cr�er des nombres al�atoires
        //Date d = new Date();
        //long seed = d.getTime();
        //objRandom = new Random(System.currentTimeMillis());
	   
	    objControleurJeu = controleur;
	    
		strNom = nom;
		
		// Cette variable sera utilis�e dans la thread
		objPositionFinaleVisee = null;
		
		// Faire la r�f�rence vers le gestionnaire d'�venements
		objGestionnaireEv = gestionnaireEv;
			
		// Cette variable sert � arr�ter la thread lorsqu'� true
		bolStopThread = false;		
			
		// Faire la r�f�rence vers la table courante
		objTable = tableCourante;	
			
		// Choisir un id de personnage al�atoirement
		intIdPersonnage = genererNbAleatoire(4) + 1;
		
		// Initialisation du pointage
		intPointage = 0;
		
		// Initialisation � null de la position, le joueur virtuel n'est nul part
		objPositionJoueur = null;
		
	    // Cr�er la liste des objets utilisables qui ont �t� ramass�s
	    lstObjetsUtilisablesRamasses = new TreeMap();
		
        // Cr�ation du profil du joueur virtuel
        intNiveauDifficulte = niveauDifficulte;
        


	}


	/**
	 * Cette m�thode est appel�e lorsqu'une partie commence. C'est la thread
	 * qui fait jouer le joueur virtuel.
	 * 
	 */
	public void run()
	{
			
		// Cette variable conserve la case sur laquelle le joueur virtuel
		// tente de se d�placer
		Point objPositionIntermediaire = null;
		
		// Cette variable indique si le joueur virtuel a r�pondu correctement
		// � la question
		boolean bolQuestionReussie;
		
		// Cette variable contient le temps de r�flexion pour r�pondre � 
		// la question
		int intTempsReflexionQuestion;
		
		// Cette variable contient le temps de r�flexion pour choisir 
		// le prochain coup � jouer
        int intTempsReflexionCoup;
		
		// Cette variable contient le temps de pause pour le d�placement
		// du personnage
		int intTempsDeplacement;
		
		//System.out.println("Joueur virtuel d�marr�");
		
		while(bolStopThread == false)
		{		

			// D�terminer le temps de r�flexion pour le prochain coup
			intTempsReflexionCoup = obtenirTempsReflexionCoup();
			
			// Pause pour moment de r�flexion de d�cision
			pause(intTempsReflexionCoup);
			
            // Trouver une case int�ressante � atteindre
            if (reviserPositioinFinaleVisee() == true)
            {
            	objPositionFinaleVisee = trouverPositionFinaleVisee();	
            }
                    
			// Trouver une case interm�diaire
			objPositionIntermediaire = trouverPositionIntermediaire();

			// S'il y a erreur de recherche ou si le joueur virtuel est pris
			// on ne le fait pas bouger
			if (objPositionIntermediaire.x != objPositionJoueur.x || 
			    objPositionIntermediaire.y != objPositionJoueur.y)
			{
    			// D�terminer si le joueur virtuel r�pondra � la question
                bolQuestionReussie = obtenirValiditeReponse(
                    obtenirPointage(objPositionJoueur, objPositionIntermediaire));
    			
    			// D�terminer le temps de r�ponse � la question
    			intTempsReflexionQuestion = obtenirTempsReflexionReponse();
                
    			// Pause pour moment de r�flexion de r�ponse
    			pause(intTempsReflexionQuestion);	
    					
    			// Faire d�placer le personnage si le joueur virtuel a 
    			// r�ussi � r�pondre � la question
    			if (bolQuestionReussie == true)
    			{
    				// D�placement du joueur virtuel
    				deplacerJoueurVirtuelEtMajPlateau(objPositionIntermediaire);
    				
    				// Obtenir le temps que le d�placement dure
    				intTempsDeplacement = obtenirTempsDeplacement(obtenirPointage(objPositionJoueur, objPositionIntermediaire));
    				
    				// Pause pour laisser le personnage se d�placer
    				pause(intTempsDeplacement);
    			}
    			else
    			{
    				// Pause pour r�troaction
    				pause(TEMPS_RETROACTION);
    			}
    			
    	    }	
		}
	}
	
	/* Cette fonction trouve le chemin le plus court entre deux points et
	 * le retourne sous forme de Vector. Le chemin retourn� est en ordre inverse
	 * (l'indice 0 correspondra au point d'arriv�e)
	 *
	 * @param: Point depart: Point de d�part du chemin
	 * @param: Point arrivee: Point d'arriv�e du chemin 
	 */
    public Vector trouverCheminPlusCourt(Point depart, Point arrivee)
    {
        // Tableau contenant une r�f�rence vers le plateau de jeu
        Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        int intNbLignes = objttPlateauJeu.length;
        int intNbColonnes = objttPlateauJeu[0].length;
        
        // Cette matrice contiendra les valeurs indiquants quelles cases ont
        // �t� parcourue par l'algorithme
        boolean matriceParcourue[][] = new boolean[intNbLignes][intNbColonnes];
        
        // Cette matrice contiendra, pour chaque case enfil�e, de quelle case
        // celle-ci a �t� enfil�e. Cela nous permettra de trouver le chemin
        // emprunt� par l'algorithme. 
        Point matricePrec[][] = new Point[intNbLignes][intNbColonnes];
        
        // Liste des points � traiter pour l'algorithme de recherche de chemin
        Vector lstPointsATraiter = new Vector();
        
        // Le chemin r�sultat que l'on retourne � la fonction appelante
        Vector lstResultat;
        
        // Point temporaire qui sert dans l'algorithme de recherche
        Point ptPosTemp = new Point();
        
        // Point d�fil� de la liste des points � traiter
        Point ptPosDefile;
        
        // Cette variable nous indiquera si l'algorithme a trouv� un chemin
        boolean bolCheminTrouve = false;
        
        // Ce tableau nous permettra de traiter les 4 cases autour d'une case
        // � l'int�rieur d'une boucle.
        Point ptDxDy[] = new Point[4];
        ptDxDy[DROITE] = new Point(0,1);
        ptDxDy[BAS] = new Point(1,0);
        ptDxDy[GAUCHE] = new Point(0,-1);
        ptDxDy[HAUT] = new Point(-1,0);
        
        // Variable pour boucler dans le tableau ptDxDy[]
        int dxIndex = 0;
        
        // Ce tableau servira � enfiler les cases de fa�ons al�atoire, ce qui
        // permettra de peut-�tre trouver diff�rents chemin
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
                // On met chaque indice de la matrice des cases parcourues � false
                matriceParcourue[i][j] = false;
                
                // Chaque case pr�c�dente sera le point -1,-1
                matricePrec[i][j] = new Point(-1,-1);
            }
        }
        
        // Enfiler notre position de d�part
        lstPointsATraiter.add(depart);
        matriceParcourue[depart.x][depart.y] = true;
                
        // On va boucler jusqu'� ce qu'il ne reste plus rien ou jusqu'�
        // ce qu'on arrive � l'arriv�e
        while (lstPointsATraiter.size() > 0 && bolCheminTrouve == false)
        {
            // D�filer une position
            ptPosDefile = (Point) lstPointsATraiter.get(0);
            lstPointsATraiter.remove(0);
                       
            // V�rifier si on vient d'atteindre l'arriv�e
            if (ptPosDefile.x == arrivee.x && ptPosDefile.y == arrivee.y)
            {
                bolCheminTrouve = true;
                break;
            }
            
            // On va faire 3 m�langes, ce sera suffisant
            for (indiceNombreMelange = 1; indiceNombreMelange <= 3;indiceNombreMelange++)
            {
                // Brasser al�atoirement le tableau al�atoire
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
                    
                    // Indiquer que cette case est trait�e pour ne pas
                    // l'enfiler � nouveau
                    matriceParcourue[ptPosTemp.x][ptPosTemp.y] = true;
                    
                    // Conserver les traces pour savoir de quel case on a enfil�
                    matricePrec[ptPosTemp.x][ptPosTemp.y].x = ptPosDefile.x;
                    matricePrec[ptPosTemp.x][ptPosTemp.y].y = ptPosDefile.y;
                    
                }
            }


        }
        
        if (bolCheminTrouve == true)
        {
            // Pr�parer le chemin de retour
            lstResultat = new Vector();
            
            // On part de l'arriv�e puis on retrace jusqu'au d�part
            ptPosTemp = arrivee;

            // Ajouter chaque case indiqu� dans matricePrec[] jusqu'� la
            // position de d�part
            while (ptPosTemp.x != depart.x || ptPosTemp.y != depart.y)
            {
                lstResultat.add(new Point(ptPosTemp.x, ptPosTemp.y));
                ptPosTemp = matricePrec[ptPosTemp.x][ptPosTemp.y];
            }
            
            // Ajouter la position de d�part
            lstResultat.add(new Point(depart.x, depart.y));
            
        }
        else
        {
            // Si on n'a pas trouv� de chemin, on retourne null
            lstResultat = null;
        }
        
        return lstResultat;
        
    }
	
	/* Cette fonction calcule les points pour un chemin. Les points sont bas�s sur
	 * le nombre de pi�ces que le chemin contient et aussi le type de case
	 * que le chemin contient au cas o� le joueur virtuel pr�f�rerait certaines cases.
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
	 * Cette fonction trouve une case interm�diaire qui permettra au joueur virtuel
	 * de progresser vers sa mission qu'est celle de se rendre � la case finale vis�e.
	 */
	private Point trouverPositionIntermediaire()
	{
	    // Variable contenant la position � retourner � la fonction appelante
		Point objPositionTrouvee;
		
        // Tableau contenant une r�f�rence vers le plateau de jeu
        Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        int intNbLignes = objttPlateauJeu.length;
        int intNbColonnes = objttPlateauJeu[0].length;

        Vector lstPositions[] = new Vector[5];
        Vector lstPositionsTrouvees;
        int tPoints[] = new int[5];
        int intPlusGrand = -1;
        
        // Recherche de plusiuers chemins pour se rendre � la position finale
        for (int i = 0; i < 5; i++)
        {
            lstPositions[i] = trouverCheminPlusCourt(objPositionJoueur, objPositionFinaleVisee);
            
            // V�rifier si on a trouv� un chemin
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

        // Valeur du point de d�part (�gale � objPositionJoueur en principe)
        Point ptDepart = (Point) lstPositionsTrouvees.get(lstPositionsTrouvees.size() - 1);
        
        // Point temporaire qui nous permettra de parcourir la liste et trouver
        // o� le joueur virtuel avancera
        Point ptTemp;
               
        // Obtenir les pourcentages de choix pour les cases selon le niveau
        // de difficult�, on va modifier ces pourcentages par la suite car il peut
        // y avoir des trous qu'on veut �viter, des pi�ces que l'on veut ramasser ou
        // bien une case finale que l'on ne veut pas d�passer
        int intPourcentageCase[] = obtenirPourcentageChoix();
        int iIndiceTableau = 0;
        
        // On part du d�but du chemin jusqu'� la fin et on trouve le premier croche
        for (int i = lstPositionsTrouvees.size() - 2; i >= 0 ; i--)
        {
            ptTemp = (Point) lstPositionsTrouvees.get(i);

            iIndiceTableau++;       
                                 
            // On v�rifie si le premier "croche" est ici
            if (ptTemp.x != ptDepart.x && ptTemp.y != ptDepart.y)
            {
                // Le premier "croche" est � ptTemp, c'est donc le d�placement
                // maximal que le joueur virtuel pourra faire
                traiterPieceTrouveeDansLigne(intPourcentageCase, iIndiceTableau - 2);
                break;
            }
            
            // S'il y a une pi�ce sur cette case, alors on s'assure que
            // le joueur virtuel ne la d�passera pas
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
        
        // Si on est pr�s de la position finale, on s'assure de ne pas la d�passer
        if (lstPositionsTrouvees.size() < DEPLACEMENT_MAX)
        {
            traiterPieceTrouveeDansLigne(intPourcentageCase, lstPositionsTrouvees.size() - 2); 
        }
        
        // Effectuer le choix
        int intPourcentageAleatoire;
        
        // On g�n�re un nombre entre 1 et 100
        intPourcentageAleatoire = genererNbAleatoire(100)+1;

        
        int intValeurAccumulee = 0;
        int intDecision = 0;
        
        // On d�termine � quel d�cision cela appartient
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
        System.out.println("Position trouv�e: " + objPositionTrouvee.x + "," + 
            objPositionTrouvee.y);           
        System.out.println("Position a atteindre: " + objPositionFinaleVisee.x + "," + 
            objPositionFinaleVisee.y);
        TestJoueurVirtuel.outputPlateau(objttPlateauJeu);*/
        //--------------------------------
        
        return objPositionTrouvee;

	}
	
	/*
	 * Cette fonction trouve une position finale que le joueur virtuel va tenter
	 * d'atteindre. C'est ici que la personnalit� du joueur peut influencer la d�cision.
	 * Par la suite, le joueur virtuel devra choisir des cases interm�diaires pour se
	 * rendre � la case finale, cela peut �tre imm�diat au prochain coup.
	 */
	private Point trouverPositionFinaleVisee()
	{
		
		// Position trouv�e par l'algorithme
		Point objPositionTrouvee = null;
		
        // Tableau contenant une r�f�rence vers le plateau de jeu
        Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        int intNbLignes = objttPlateauJeu.length;
        int intNbColonnes = objttPlateauJeu[0].length;

        // D�claration d'une matrice qui contiendra un pointage pour chaque
        // case du plateau de jeu, ce qui permettra de choisir le meilleur
        // coup � jouer
        int matPoints[][] = new int[intNbLignes][intNbColonnes];

        // Cette variable contiendra le nombre de coups estim� pour se rendre
        // � la case en cours d'analyse
        double dblDistance;
        
        // Point en cours d'analyse
        Point ptTemp = new Point(0,0);
        
        // Autre point en cours d'analyse
        Point ptTemp2 = new Point(0,0);
        
        // Chemin entre le joueur et une case importante analys�e
        Vector lstChemin;

        // D�placement moyen, contient le nombre de cases que l'on peut
        // s'attendre � franchir par coup (prend en compte niveau de
        // difficult�)
        double dblDeplacementMoyen = obtenirDeplacementMoyen();
        
        // Ce tableau contiendra les 5 cases les plus int�ressantes
        Point tPlusGrand[] = new Point[5];
        
        // Ce tableau contient des param�tres pour prioriser les
        // regroupement de pi�ces
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
                // Pointage de d�part (environ 0)
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
                    // La position courante du joueur ne doit pas �tre choisie
                    matPoints[x][y] = -999999999;
                }
                else
                {

                    // Modification du pointage de la case
                    if (objttPlateauJeu[x][y] == null)
                    {
                        // Une case nulle ne doit pas �tre chosie
                        matPoints[x][y] = -999999999;
                    }
                    else if(objttPlateauJeu[x][y] instanceof CaseCouleur && 
                        ((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase() instanceof Piece)
                    {
                        
                        // Une pi�ce augmente d'environ 5000 le pointage
                        matPoints[x][y] += 4950 + genererNbAleatoire(101);
                    
                        // On va trouver le chemin le plus court
                        lstChemin = trouverCheminPlusCourt(objPositionJoueur, ptTemp);
                        
                        if (lstChemin == null)
                        {
                            // Une case inaccessible ne doit pas �tre choisie
                            matPoints[x][y] = -999999999;
                        }
                        else
                        {
                            // On a un chemin qui contient chaque case, maintenant, on
                            // va trouver, pour ce chemin, le nombre de coups estim�
                            // pour le parcourir, et ce, en prenant en compte le niveau
                            // de difficult�
                            // TODO:Prendre en compte nombre de croches
                            dblDistance = lstChemin.size() / dblDeplacementMoyen;
                            

                            // Pour permettre de quand m�me prioriser les pi�ces
                            // lointaines, on va limiter le nombre de coups
                            // ce qui enl�vera 4800 points pour une pi�ce lointaine
                            if (dblDistance > 6)
                            {
                                dblDistance = 6;
                            }
                            
                            // Plus la pi�ce est loin, plus son pointage diminue
                            // On enl�ve 800 points par coup
                            matPoints[x][y] -= (int) (800 * dblDistance + .5);

                            // Cette pi�ce �tant accessible, on va augmenter les
                            // points des cases aux alentours pour attirer
                            // le joueur virtuel vers des regroupements de pi�ces
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
         
         // On va maintenant trouver les 5 meilleurs d�placements
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                // Gestion de la liste des 5 plus grands
                // On ajoute la case qu'on est en train de parcourir dans la liste
                // des 5 plus grands pointage si elle est digne d'y �tre
                for (int i = 0; i < 5; i++)
                {
                    if (tPlusGrand[i] == null)
                    {
                        tPlusGrand[i] = new Point(x, y);
                        break;
                    }
                    else if (matPoints[x][y] > matPoints[tPlusGrand[i].x][tPlusGrand[i].y])
                    {
                        // Tout d�caler vers la droite
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
                        
                        // Ins�rer notre �l�ment
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
        
        // Nombre de choix possible qui ne d�passe pas la limite de intDifferenceMax
        int intNombreChoix = 1;
        
        // Valeur maximum pour g�n�rer la valeur al�atoire
        int intValeurMax;
        
        // Valeur al�atoire permettant d'effectuer le choix
        int intValeurAleatoire;

        // Tableau contenant le pourcentage des choix alternatifs
        int tPourcentageChoix[] = obtenirPourcentageChoixAlternatifFinal();
        
        // La d�cision selon le r�sultat al�atoire
        int intDecision = 0;
        
        // Valeur accumul�e pour trouver la d�cision correspondante
        int intValeurAccumulee = 0;
        
        // On doit trouver le nombre de choix possible pour le joueur virtuel
        // selon la diff�rence maximum calcul�e (qui tient compte du niveau
        // de difficult�)
        intValeurMax = tPourcentageChoix[0];
        for (int i = 1; i < 5; i ++)
        {
        	if (matPoints[tPlusGrand[i].x][tPlusGrand[i].y] < 0 || 
        	    matPoints[tPlusGrand[i].x][tPlusGrand[i].y] < 
        	    matPoints[tPlusGrand[0].x][tPlusGrand[0].y] - intDifferenceMax)
        	{
        		// Ce choix est en-dessous de la limite permise pour
        		// ce niveau de difficult�
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
        
        // Ce nombre correspond � notre choix
        for (int i = 0; i < intNombreChoix; i++)
        {
        	intValeurAccumulee += tPourcentageChoix[i];
            if (intValeurAleatoire <= intValeurAccumulee)
            {
            	intDecision = i;
            }
        }

        // D�terminer la raison
        intRaisonPositionFinale = RAISON_AUCUNE;
        if (objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y] instanceof CaseCouleur)
        {
        	if (((CaseCouleur)objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y]).obtenirObjetCase() instanceof Piece)
        	{
        		intRaisonPositionFinale = RAISON_PIECE;
        	}
        }
        
        // Retourner la position trouv�e     
        objPositionTrouvee = new Point(tPlusGrand[intDecision].x, tPlusGrand[intDecision].y);
        return objPositionTrouvee;
		
		/*	
		// Choisir la case al�atoirement
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
     * Cette fonction s'occupe de d�placer le joueur virtuel s'il a bien r�pondu
     * � la question, met � jour le plateau de jeu, envoie les �v�nements aux autres joueurs
     * et modifie le pointage et la position du joueur virtuel
     */
    private void deplacerJoueurVirtuelEtMajPlateau(Point objNouvellePosition)
    {
        String collision = "";
                
        // D�claration d'une r�f�rence vers l'objet ramass�
        ObjetUtilisable objObjetRamasse = null;
        
        // D�claration d'une r�f�rence vers l'objet subi
        ObjetUtilisable objObjetSubi = null;
        
        // Faire la r�f�rence vers la case de destination
        Case objCaseDestination = objTable.obtenirPlateauJeuCourant()[objNouvellePosition.x][objNouvellePosition.y];
            
        // Le pointage est initialement celui courant
        int intNouveauPointage = intPointage;   
            
        // Calculer le nouveau pointage du joueur (on ajoute la difficult� 
        // de la question au pointage)
        intNouveauPointage += obtenirPointage(objPositionJoueur, objNouvellePosition);
        
        // Si la case de destination est une case de couleur, alors on 
        // v�rifie l'objet qu'il y a dessus et si c'est un objet utilisable, 
        // alors on l'enl�ve et on le donne au joueur virtuel, sinon si c'est une 
        // pi�ce on l'enl�ve et on met � jour le pointage, sinon 
        // on ne fait rien
        if (objCaseDestination instanceof CaseCouleur)
        {
            // Faire la r�f�rence vers la case de couleur
            CaseCouleur objCaseCouleurDestination = (CaseCouleur) objCaseDestination;
                    
            // S'il y a un objet sur la case, alors on va faire l'action 
            // tout d�pendant de l'objet (pi�ce, objet utilisable ou autre)
            if (objCaseCouleurDestination.obtenirObjetCase() != null)
            {
                // Si l'objet est un objet utilisable, alors on l'ajoute � 
                // la liste des objets utilisables du joueur virtuel
                if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable)
                {
                    // Faire la r�f�rence vers l'objet utilisable
                    ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetCase();
                    
                    // Garder la r�f�rence vers l'objet utilisable pour l'ajouter � l'objet de retour
                    objObjetRamasse = objObjetUtilisable;
                    
                    // Ajouter l'objet ramass� dans la liste des objets du joueur virtuel
                    lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
                    
                    // Enlever l'objet de la case du plateau de jeu
                    objCaseCouleurDestination.definirObjetCase(null);
                }
                else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece)
                {
                    // Faire la r�f�rence vers la pi�ce
                    Piece objPiece = (Piece) objCaseCouleurDestination.obtenirObjetCase();
                    
                    // Mettre � jour le pointage du joueur virtuel
                    intNouveauPointage += objPiece.obtenirValeur();
                    
                    // Enlever la pi�ce de la case du plateau de jeu
                    objCaseCouleurDestination.definirObjetCase(null);
                    
                    collision = "piece";
                    

                    
                    // TODO: Il faut peut-�tre lancer un algo qui va placer 
                    //       les pi�ces sur le plateau de jeu s'il n'y en n'a
                    //       plus
                }

            }
            
            // S'il y a un objet � subir sur la case, alors on va faire une
            // certaine action (TODO: � compl�ter)
            if (objCaseCouleurDestination.obtenirObjetArme() != null)
            {
                // Faire la r�f�rence vers l'objet utilisable
                ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetArme();
                
                // Garder la r�f�rence vers l'objet utilisable � subir
                objObjetSubi = objObjetUtilisable;
                
                //TODO: Faire une certaine action au joueur virtuel
                
                // Enlever l'objet subi de la case
                objCaseCouleurDestination.definirObjetArme(null);
            }
            
        }
        else
        { 
            //TODO: �muler mini-jeu
        }
                
        synchronized (objTable.obtenirListeJoueurs())
        {
            // Pr�parer l'�v�nement de deplacement de personnage. 
            // Cette fonction va passer les joueurs et cr�er un 
            // InformationDestination pour chacun et ajouter l'�v�nement 
            // dans la file de gestion d'�v�nements
            preparerEvenementJoueurVirtuelDeplacePersonnage(collision, objNouvellePosition);                
        }
        
        // Mettre � jour pointage et position du joueur virtuel
        objPositionJoueur = objNouvellePosition;
        intPointage = intNouveauPointage;

    }
    
    /*
     * Cette fonction pr�pare l'�v�nement indiquant que le joueur virtuel se d�place
     */
    private void preparerEvenementJoueurVirtuelDeplacePersonnage( String collision, Point objNouvellePosition )
    {

        EvenementJoueurDeplacePersonnage joueurDeplacePersonnage = new EvenementJoueurDeplacePersonnage(strNom, objPositionJoueur, objNouvellePosition, collision );
        
        // Cr�er un ensemble contenant tous les tuples de la liste 
        // des joueurs de la table (chaque �l�ment est un Map.Entry)
        Set lstEnsembleJoueurs = objTable.obtenirListeJoueurs().entrySet();
        
        // Obtenir un it�rateur pour l'ensemble contenant les joueurs
        Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
        
        // Passer tous les joueurs de la table et leur envoyer un �v�nement
        while (objIterateurListe.hasNext() == true)
        {
            // Cr�er une r�f�rence vers le joueur humain courant dans la liste
            JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

            // Cr�er un InformationDestination et l'ajouter � l'�v�nement
            joueurDeplacePersonnage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                                                                                             objJoueur.obtenirProtocoleJoueur()));
        }
        
        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
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
     * Cette fonction fait une pause de X secondes �mulant une r�flexion
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
     * Cette fonction retourne le pointage d'un d�placement
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
     * Fonction de service utilis�e dans l'algorithme de recherche de 
     * position qui permet de modifier les pourcentages du choix � faire.
     * La fonction prend un tableau de longueur X et un indice du tableau. 
     * De indice + 1 � X - 1, on ajoute les valeurs � tableau[indice]
     * puis on met � z�ro ces indices
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
    
    /* Cette fonction permet � la boucle dans run() de s'arr�ter
     */
    public void arreterThread()
    {
        bolStopThread = true;
    }
    
    /* Cette fonction permet d'obtenir un tableau qui contient les pourcentages de
     * choix de d�placement pour chaque grandeur de d�placement. Ces pourcentages
     * sont bas�s sur le niveau de difficult� du joueur virtuel
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
     * de d�placement, le pourcentage de r�ussite � la question. Ce pourcentage est
     * bas� sur le niveau de difficult� du joueur virtuel
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
    
    /* Cette fonction permet d'obtenir le temps de r�flexion d'un joueur
     * virtuel pour planifier son prochain coup. Ce temps est bas� sur le niveau
     * de difficult� du joueur virtuel et comprend un �l�ment al�atoire.
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
    
    
    /* Cette fonction permet d'obtenir le temps de r�flexion d'un joueur
     * virtuel lorsqu'il r�pond � une question. Ce temps est bas� sur le niveau
     * de difficult� du joueur virtuel et comprend un �l�ment al�atoire.
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
    
    /* Cette fonction permet de savoir si le joueur virtuel r�pondra correctement
     * � la question. En param�tre, la grandeur du d�placement que le joueur virtuel
     * demande.
     */
    private boolean obtenirValiditeReponse(int grandeurDeplacement)
    {
        int intPourcentageReponse[] = obtenirPourcentageReponse();
        
        if (grandeurDeplacement < 1 || grandeurDeplacement > DEPLACEMENT_MAX)
        {
            return false;
        }
        
        // G�n�rer un nombre al�atoire
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
     * en moyenne selon son niveau de difficult�.
     * NOTE: Pr�-calcul� et hard-cod� selon les pourcentages de choix et de r�ussites
     *       pour les diff�rentes grandeurs de d�placements
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
    
    /* Cette fonction retourne le temps en secondes que dure un d�placement
     * de joueur selon le nombre de cases du d�placement.
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
     * virtuel peut n�gliger lors du choix de la position finale
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
     * choix alternatifs de positions finales selon le niveau de difficult�
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
     * une nouvelle position finale vis�e par le joueur virtuel. On
     * fait cela dans les circonstances suivantes:
     *
     * - Aucune position encore trouv�e (d�but)
     * - Le joueur a atteint la position qu'il visait
     * - L'�tat de la case vis�e a chang� (l'objet a disparu)
     */
    private boolean reviserPositioinFinaleVisee()
    {
    	// V�rifier si aucune position trouv�e
    	if (objPositionFinaleVisee == null)
    	{
    		return true;
    	}
    	
    	// V�rifier si on a atteint la position pr�c�damment vis�e
    	if (objPositionJoueur.x == objPositionFinaleVisee.x &&
    	    objPositionJoueur.y == objPositionFinaleVisee.y)
    	{
    		return true;
    	}
    	
    	// Aller chercher le plateau de jeu
    	Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
        
        // V�rifier si l'�tat de la case a chang�
        switch (intRaisonPositionFinale)
        {
        	case RAISON_AUCUNE: 
        	
	        	// Aucune raison = erreur en g�n�ral, donc on va recalculer
	            // une position finale
	            return true;

            
        	case RAISON_PIECE:
        	
	        	// V�rifier si la pi�ce a �t� captur�e
	    	    if (((CaseCouleur)objttPlateauJeu[objPositionFinaleVisee.x][objPositionFinaleVisee.y]).obtenirObjetCase() == null)
	    	    {
	    	    	return true;
	    	    }
	    	    else
	    	    {
	    	    	return false;
	    	    }    
        }
        
        // Dans les autres cas, ce n'est pas n�cessaire de rechercher
        // tout de suite (on attend que le joueur virtuel atteigne la
        // position finale avant de recalculer une position)
        return false;

    }
}
