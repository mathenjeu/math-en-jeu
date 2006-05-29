package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.Salle;
import java.awt.Point;
import java.util.TreeMap;
import ServeurJeu.ComposantesJeu.Table;
import ClassesUtilitaires.UtilitaireNombres;
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


import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementJoueurDeplacePersonnage;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;

import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;



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
	
	
	// Cette variable contient les d�tails du joueur virtuel: son
	// niveau de difficult�, son type de jeu (aggressif ou non), sa
	// fa�on de jouer en certaine situation (lorsqu'il perd ou gagne par ex),
	// et ce qu'il priorise (pi�ces, objets, magasin, etc)
	// TODO: Profil des joueurs virtuels
	//private ProfilJoueurVirtuel objProfilJoueurVirtuel;
	
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
	
	// Cette constante d�finit le temps de pause lors d'une r�troaction
	private final static int TEMPS_RETROACTION = 10;
	
	
	// Autres constantes utilis�s dans les algorithmes de recherche de choix
    private final static int DROITE = 0;
    private final static int BAS = 1;
    private final static int GAUCHE = 2;
    private final static int HAUT = 3;
    private final static int DEPLACEMENT_MAX = 6;
	
	
	
	/**
	 * Constructeur de la classe JoueurVirtuel qui permet d'initialiser les 
	 * membres priv�s du joueur virtuel
	 * 
	 * @param String nom : L'objet g�rant le protocole de 
	 * @param Integer niveauDifficulte : Le niveau de difficult� pour ce joueur
	 *                                   virtuel

	 */
	public JoueurVirtuel(String nom, Integer niveauDifficulte, Table tableCourante, 
	    GestionnaireEvenements gestionnaireEv)
	{
		strNom = nom;
		
		// Cette variable sera initialis� lorsque la partie commencera
		// et � chaque fois que la case sera atteinte par le joueur virtuel
		objPositionFinaleVisee = null;
		
		// Faire la r�f�rence vers le gestionnaire d'evenements
		objGestionnaireEv = gestionnaireEv;
		
		// Initialiser le profil du joueur virtuel selon le niveau de 
		// difficult� pass� en param�tre. On modifie al�atoirement
		// quelques param�tres pour diversifier les joueurs virtuels et
		// on lui attribue un type de jeu (aggressif, passif ou normal)
		// TODO: Profil des joueurs virtuels
		//objProfilJoueurVirtuel = new ProfilJoueurVirtuel(niveauDifficulte);
		
		// Cette variable sert � arr�ter la thread lorsqu'� true
		bolStopThread = false;		
			
		// Faire la r�f�rence vers la table courante
		objTable = tableCourante;	
			
		// Choisir un id de personnage al�atoirement
		// TODO: choisir al�atoirement
		intIdPersonnage = 1;
		
		// Initialisation du pointage
		intPointage = 0;
		
		// Initialisation � null de la position, le joueur virtuel n'est nul part
		objPositionJoueur = null;
		
	    // Cr�er la liste des objets utilisables qui ont �t� ramass�s
	    lstObjetsUtilisablesRamasses = new TreeMap();
		

	}


	/**
	 * Cette m�thode est appel�e lorsqu'une partie commence. C'est la thread
	 * qui fait jouer le joueur virtuel
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
		Integer intTempsReflexionQuestion;
		
		// Cette variable contient le temps de r�flexion pour choisir 
		// le prochaine coup � jouer
		Integer intTempsReflexionCoup;
		
		while(bolStopThread == false)
		{
			
			// V�rifier s'il faut rechercher une nouvelle case cible
			if (objPositionFinaleVisee == null || 
			    (objPositionFinaleVisee.x == objPositionJoueur.x && 
			     objPositionFinaleVisee.y == objPositionJoueur.y))
			{
				
				// TODO: recherche intelligente de PositionFinaleVisee
				// (al�atoire pour l'instant)
				objPositionFinaleVisee = trouverPositionFinaleVisee();
				
			}
			
			// D�terminer le temps de r�flexion pour le prochain coup
			// TODO: Temps de r�flexion selon niveau de difficult�
			intTempsReflexionCoup = 2;
			
			// Pause pour moment de r�flexion de d�cision
			pause(intTempsReflexionCoup);
			
			// Trouver une case interm�diaire
			objPositionIntermediaire = trouverPositionIntermediaire();
			
			// D�terminer si le joueur virtuel r�pondra � la question
			// TODO: D�terminer selon niveau de difficult� (pour fin
			//       de test, on va le laisser toujours r�ussis)
			bolQuestionReussie = true;
			
			// D�terminer le temps de r�ponse � la question
			// TODO: Temps de r�ponse selon niveau de difficult�
			intTempsReflexionQuestion = 8;

			// Pause pour moment de r�flexion de r�ponse
			pause(intTempsReflexionQuestion);	
					
			// Faire d�placer le personnage si le joueur virtuel a 
			// r�ussi � r�pondre � la question
			if (bolQuestionReussie == true)
			{
				// D�placement du joueur virtuel
				deplacerJoueurVirtuelEtMajPlateau(objPositionIntermediaire);
			}
			else
			{
				// Pause pour r�troaction
				pause(TEMPS_RETROACTION);
			}
				
		}
	}
	
	/*
	 * Cette fonction trouve une case interm�diaire qui permettra au joueur virtuel
	 * de progresser vers sa mission qu'est celle de se rendre � la case finale vis�e.
	 */
	private Point trouverPositionIntermediaire()
	{
		Point objPositionTrouvee = new Point();
		Case objttPlateauJeu[][] = objTable.obtenirPlateauJeuCourant();
		int x;
		int y;
		int i;
		int intIndiceDirection;
		boolean bolCaseTrouvee = false;

        // D�claration d'un tableau qui va permettre de savoir quelle ligne on choisie
	    boolean bolLigne[] = new boolean[4];
	    bolLigne[DROITE] = true;         // DROITE
	    bolLigne[BAS] = true;         // BAS
	    bolLigne[GAUCHE] = true;         // GAUCHE
	    bolLigne[HAUT] = true;         // HAUT
	    
	    // D�claration d'un tableau qui contient le dx et dy de gauche, droite, etc.
	    Point ptDxDy[] = new Point[4];
	    ptDxDy[DROITE] = new Point(1,0);
	    ptDxDy[BAS] = new Point(0,-1);
	    ptDxDy[GAUCHE] = new Point(-1,0);
	    ptDxDy[HAUT] = new Point(0,-1);
	    
	    int intNbLignes = objttPlateauJeu[0].length;
	    int intNbColonnes = objttPlateauJeu.length;
	    

        CaseCouleur objCaseCouleurTemp = null;

	    // On �limine deux directions (ou 3 si en ligne) qui ne permettraient pas de se rapprocher
	    // de la case vis�e
	    if (objPositionFinaleVisee.x > objPositionJoueur.x)
	    {
	    	bolLigne[GAUCHE] = false;
	    }
	    else if (objPositionFinaleVisee.x < objPositionJoueur.x)
	    {
	    	bolLigne[DROITE] = false;
	    }
	    else
	    {
	    	bolLigne[GAUCHE] = false;
	    	bolLigne[DROITE] = false;
	    }
	    
	    if (objPositionFinaleVisee.y > objPositionJoueur.y)
	    {
	    	bolLigne[HAUT] = false;
	    }
	    else if (objPositionFinaleVisee.y < objPositionJoueur.y)
	    {
	    	bolLigne[BAS] = false;
	    }
	    else
	    {
	    	bolLigne[BAS] = false;
	    	bolLigne[HAUT] = false;
	    }

	    // Maintenant, on choisit une des deux directions restantes selon sa valeur potentiel
	    int intValeurLigne[] = new int[4];
		intValeurLigne[DROITE] = 0;
		intValeurLigne[GAUCHE] = 0;	
		intValeurLigne[BAS] = 0;   
		intValeurLigne[HAUT] = 0;
		
        // TODO: Supprimer les copier-coller
		if (bolLigne[DROITE] == true && objPositionJoueur.x+1 <= objttPlateauJeu.length)
		{
			for (i = objPositionJoueur.x+1; i <= objPositionFinaleVisee.x ; i++)
			{
				if (objttPlateauJeu[i][objPositionJoueur.y] == null)
				{
					break;
				}
				else 
				{
					intValeurLigne[DROITE]++;
                    
					if (objttPlateauJeu[i][objPositionJoueur.y] instanceof CaseCouleur)
				    {
                        objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[i][objPositionJoueur.y];
				    	if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
				    	{
				    		intValeurLigne[DROITE]+=10;
				    	}
				    }	
				}
			}
		}
			
		if (bolLigne[GAUCHE] == true && objPositionJoueur.x-1 >= 0)
		{
			for (i = objPositionJoueur.x-1; i >= objPositionFinaleVisee.x ; i--)
			{
				if (objttPlateauJeu[i][objPositionJoueur.y] == null)
				{
					break;
				}
				else
				{
				    intValeurLigne[GAUCHE]++;
				    
				    if (objttPlateauJeu[i][objPositionJoueur.y] instanceof CaseCouleur)
				    {
				    	objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[i][objPositionJoueur.y];
				    	if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
				    	{
				    		intValeurLigne[GAUCHE]+=10;
				    	}
				    }	
			    }
			    
			}
		}
		
		if (bolLigne[BAS] == true && objPositionJoueur.y+1 <= objttPlateauJeu[0].length)
		{
			for (i = objPositionJoueur.y+1; i <= objPositionFinaleVisee.y ; i++)
			{
				
				if (objttPlateauJeu[i][objPositionJoueur.y] == null)
				{
					break;
				}
				else
				{
				    intValeurLigne[BAS]++;		
				    
				    if (objttPlateauJeu[objPositionJoueur.x][i] instanceof CaseCouleur)
				    {
				    	objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x][i];
				    	if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
				    	{
				    		intValeurLigne[BAS]+=10;
				    	}
				    }
				}	
			}
		}

		if (bolLigne[HAUT] == true && objPositionJoueur.y-1 >= 0)
		{	    
			for (i = objPositionJoueur.y-1; i >= objPositionFinaleVisee.y ; i--)
			{
				if (objttPlateauJeu[i][objPositionJoueur.y] == null)
				{
					break;
				}
				else
				{
				    intValeurLigne[HAUT]++;	
				    
					if (objttPlateauJeu[objPositionJoueur.x][i] instanceof CaseCouleur)
				    {
				    	objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x][i];
				    	if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
				    	{
				    		intValeurLigne[HAUT]+=10;
				    	}
				    }	
				}
			}
		}
		
		// Ici, on d�termine la meilleur ligne selon intValeurLigne[]
		int intPlusGrand = 0;
		for ( i = 1; i <= 3; i++)
		{
			if (intValeurLigne[i] > intValeurLigne[intPlusGrand])
			{
				intPlusGrand = i;
			}
		}
		
		// On �limine les autres lignes (en m�me temps on va s'assurer qu'il n'y
		// a qu'une seule ligne de choisie)
		for (i = 1; i <=3; i++)
		{
			if (i != intPlusGrand)
			{
				bolLigne[i] = false;
			}
			else
			{
				bolLigne[i] = true;
			}
		}
		
		
		// D�claration d'un tableau qui va contenir les pourcentages pour le choix de
		// la case. On va modifier ces pourcentages selon la disposition des pi�ces
		int intPourcentageCase[] = new int [6];
		
		// TODO: remplir selon niveau de difficult�
		intPourcentageCase[0] = 5;
		intPourcentageCase[1] = 19;
		intPourcentageCase[2] = 40;
		intPourcentageCase[3] = 25;
		intPourcentageCase[4] = 10;
		intPourcentageCase[5] = 1;
		

        // Parcourir les cases et modifier les pourcentages selon les pi�ces et trous trouv�s
        // On s'assure aussi que le joueur ne d�passe pas sa position finale vis�e
        // TODO: Supprimer les copier-coller
		for (i = 1; i <= 6 ; i++)
		{
			// V�rifier les cases � droite
			if (bolLigne[DROITE])
			{
				if (objPositionJoueur.x + i < intNbColonnes && objttPlateauJeu[objPositionJoueur.x + i][objPositionJoueur.y] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x + i][objPositionJoueur.y] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x + i][objPositionJoueur.y];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de d�passer une pi�ce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile � captur�e et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages sup�rieures
				    		// et on les ajoute � cette case puis on arr�te de chercher
				    		traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);
				    		break;
				    	}
				    }
				    
				    if (objPositionJoueur.x + i == objPositionFinaleVisee.x && objPositionJoueur.y == 
				        objPositionFinaleVisee.y)
				    {
				        traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);	
				    }
			    }
			    else
			    {
				    // On arrive sur le bord d'un trou ou sur le bord du plateau de jeu       
                    traiterPieceTrouveeDansLigne(intPourcentageCase, i - 2);
			        break;			    	
			    }
			}

			// V�rifier les cases � gauche
			if (bolLigne[GAUCHE])
			{
				if (objPositionJoueur.x - i >= 0 && objttPlateauJeu[objPositionJoueur.x - i][objPositionJoueur.y] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x - i][objPositionJoueur.y] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x - i][objPositionJoueur.y];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de d�passer une pi�ce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile � captur�e et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages sup�rieures
				    		// et on les ajoute � cette case puis on arr�te de chercher
				    		traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);
				    		break;
				    	}
				    }
				    
				    if (objPositionJoueur.x - i == objPositionFinaleVisee.x && objPositionJoueur.y == 
				        objPositionFinaleVisee.y)
				    {
				        traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);	
				    }
			    }
			    else
			    {
				    // On arrive sur le bord d'un trou ou sur le bord du plateau de jeu       
                    traiterPieceTrouveeDansLigne(intPourcentageCase, i - 2);
			        break;			    	
			    }
			}

			// V�rifier les cases � bas
			if (bolLigne[BAS])
			{
				if (objPositionJoueur.y + i < intNbLignes && objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y+i] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y+i] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y+i];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de d�passer une pi�ce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile � captur�e et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages sup�rieures
				    		// et on les ajoute � cette case puis on arr�te de chercher
				    		traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);
				    		break;
				    	}
				    }
				    
				    if (objPositionJoueur.x == objPositionFinaleVisee.x && objPositionJoueur.y+i == 
				        objPositionFinaleVisee.y)
				    {
				        traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);	
				    }
			    }
			    else
			    {
				    // On arrive sur le bord d'un trou ou sur le bord du plateau de jeu       
                    traiterPieceTrouveeDansLigne(intPourcentageCase, i - 2);
			        break;			    	
			    }
			}
			
			// V�rifier les cases en haut
			if (bolLigne[BAS])
			{
				if (objPositionJoueur.y - i >= 0 && objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y-i] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y-i] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y-i];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de d�passer une pi�ce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile � captur�e et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages sup�rieures
				    		// et on les ajoute � cette case puis on arr�te de chercher
				    		traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);
				    		break;
				    	}
				    }
				    
				    if (objPositionJoueur.x == objPositionFinaleVisee.x && objPositionJoueur.y-i == 
				        objPositionFinaleVisee.y)
				    {
				        traiterPieceTrouveeDansLigne(intPourcentageCase, i - 1);	
				    }
			    }
			    else
			    {
				    // On arrive sur le bord d'un trou ou sur le bord du plateau de jeu       
                    traiterPieceTrouveeDansLigne(intPourcentageCase, i - 2);
			        break;			    	
			    }
			}
		}
		
		// Effectuer le choix
		int intPourcentageAleatoire;
		
		// On g�n�re un nombre entre 1 et 100
		intPourcentageAleatoire = ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire(100)+1;

		
		int intValeurAccumulee = 0;
		int intDecision = 0;
		
		// On d�termine � quel d�cision cela appartient
		for (i = 0 ; i <= DEPLACEMENT_MAX-1 ; i++)
		{
			intValeurAccumulee += intPourcentageCase[i];
			if (intPourcentageAleatoire <= intValeurAccumulee)
			{
				intDecision = i + 1;
				break;
			}
		}
		
		// Retourner la position qui correspond � la d�cision
		if (bolLigne[DROITE] == true)
		{
			objPositionTrouvee.x = objPositionJoueur.x + intDecision;
			objPositionTrouvee.y = objPositionJoueur.y;
		}
		else if (bolLigne[BAS] == true)
		{
			objPositionTrouvee.x = objPositionJoueur.x;
			objPositionTrouvee.y = objPositionJoueur.y + intDecision;
		}
		else if (bolLigne[GAUCHE] == true)
		{
			objPositionTrouvee.x = objPositionJoueur.x - intDecision;
			objPositionTrouvee.y = objPositionJoueur.y;
		}
		else
		{
			objPositionTrouvee.x = objPositionJoueur.x;
			objPositionTrouvee.y = objPositionJoueur.y - intDecision;
		}
		
		
		
		// Obtenir une liste des cases accessibles depuis la position du joueur
		// TODO	
			
		// D�terminer les cases utiles pour atteindre la position finale
		// TODO
		
		// Choisir parmi ces cases selon niveau de difficult� et type de joueur
		// TODO
		

		// On va obtenir une des 4 cases autour du personnage en essayant de se
		// rapprocher de la case visee, ceci est temporaire
		/*if (objPositionFinaleVisee.x > x && x+1 < objPlateauJeu.length && objPlateauJeu[x+1][y] != null)
		{
			objPositionTrouvee.x = x+1;
			objPositionTrouvee.y = y;
		}
		else if (objPositionFinaleVisee.y > y && y+1 < objPlateauJeu[0].length && objPlateauJeu[x][y+1] != null)
		{
			objPositionTrouvee.x = x;
			objPositionTrouvee.y = y+1;
		}
		else if (objPositionFinaleVisee.x < x && x > 0 && objPlateauJeu[x-1][y] != null)
		{
			objPositionTrouvee.x = x-1;
			objPositionTrouvee.y = y;
		}
		else if (objPositionFinaleVisee.y < y && y > 0 && objPlateauJeu[x][y-1] != null)
		{
		    objPositionTrouvee.x = x;
		    objPositionTrouvee.y = y-1;	
		}
		else
		{
			objPositionTrouvee.x = x;
			objPositionTrouvee.y = y;
		}*/

		
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
		
		Point objPositionTrouvee = null;
		
		// Parcourir toutes les cases avec pi�ces et trouver
		// la plus proche 
		//TODO: recherche d'une bonne case
		
		// Pr�sentement, on choisit al�atoirement
		Integer x, y;
		Boolean bolTerminee = false;
		while (bolTerminee == false)
		{
		    x = objTable.obtenirPlateauJeuCourant().length;
		    y = objTable.obtenirPlateauJeuCourant()[0].length;
		     
		    if (objTable.obtenirPlateauJeuCourant()[x][y] != null)
		    {
		    	objPositionTrouvee = new Point(UtilitaireNombres.genererNbAleatoire(x), 
		            UtilitaireNombres.genererNbAleatoire(y));
		        bolTerminee = true;
		    }
		        
		}
		return objPositionTrouvee;
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
     * Cette fonction s'occupe de d�placer le joueur virtuel s'il a bien r�pondu
     * � la question, met � jour le plateau de, envoie les �v�nements aux autres joueurs
     * et modifie le pointage et la position du joueur virtuel
     */
    private void deplacerJoueurVirtuelEtMajPlateau(Point objNouvellePosition)
    {
    	String collision = "";
    	
    	// D�claration de l'objet de retour
    	// TODO: V�rifier si c'est utile pour un joueur virtuel
    	//RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
    	
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
					// 		 les pi�ces sur le plateau de jeu s'il n'y en n'a
					//		 plus
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
		
		// Cr�er l'objet de retour
		/*objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(true, intNouveauPointage);
		objRetour.definirObjetRamasse(objObjetRamasse);
		objRetour.definirObjetSubi(objObjetSubi);
		objRetour.definirNouvellePosition(objNouvellePosition);
		objRetour.definirCollision( collision );*/
		
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
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de d�marrer la partie, alors on peut envoyer un 
			// �v�nement � cet utilisateur

			// Obtenir un num�ro de commande pour le joueur courant, cr�er 
			// un InformationDestination et l'ajouter � l'�v�nement
		    joueurDeplacePersonnage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));

		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEv.ajouterEvenement(joueurDeplacePersonnage);
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
     *
     */
    private void traiterPieceTrouveeDansLigne(int tPourcentageCase[], int indice)
    {
    	int x;
    	if (indice + 1 <= DEPLACEMENT_MAX - 1)
    	{
    	
    	    for(x = indice + 1; x <= DEPLACEMENT_MAX - 1; x++)
    	    {
    	    	tPourcentageCase[indice] += tPourcentageCase[indice + x];
    	    	tPourcentageCase[indice + x] = 0;
    	    }
    	
    	}
    }

}
