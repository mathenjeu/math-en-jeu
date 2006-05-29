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
	
	
	// Cette variable contient les détails du joueur virtuel: son
	// niveau de difficulté, son type de jeu (aggressif ou non), sa
	// façon de jouer en certaine situation (lorsqu'il perd ou gagne par ex),
	// et ce qu'il priorise (pièces, objets, magasin, etc)
	// TODO: Profil des joueurs virtuels
	//private ProfilJoueurVirtuel objProfilJoueurVirtuel;
	
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
	
	// Cette constante définit le temps de pause lors d'une rétroaction
	private final static int TEMPS_RETROACTION = 10;
	
	
	// Autres constantes utilisés dans les algorithmes de recherche de choix
    private final static int DROITE = 0;
    private final static int BAS = 1;
    private final static int GAUCHE = 2;
    private final static int HAUT = 3;
    private final static int DEPLACEMENT_MAX = 6;
	
	
	
	/**
	 * Constructeur de la classe JoueurVirtuel qui permet d'initialiser les 
	 * membres privés du joueur virtuel
	 * 
	 * @param String nom : L'objet gérant le protocole de 
	 * @param Integer niveauDifficulte : Le niveau de difficulté pour ce joueur
	 *                                   virtuel

	 */
	public JoueurVirtuel(String nom, Integer niveauDifficulte, Table tableCourante, 
	    GestionnaireEvenements gestionnaireEv)
	{
		strNom = nom;
		
		// Cette variable sera initialisé lorsque la partie commencera
		// et à chaque fois que la case sera atteinte par le joueur virtuel
		objPositionFinaleVisee = null;
		
		// Faire la référence vers le gestionnaire d'evenements
		objGestionnaireEv = gestionnaireEv;
		
		// Initialiser le profil du joueur virtuel selon le niveau de 
		// difficulté passé en paramètre. On modifie aléatoirement
		// quelques paramètres pour diversifier les joueurs virtuels et
		// on lui attribue un type de jeu (aggressif, passif ou normal)
		// TODO: Profil des joueurs virtuels
		//objProfilJoueurVirtuel = new ProfilJoueurVirtuel(niveauDifficulte);
		
		// Cette variable sert à arrêter la thread lorsqu'à true
		bolStopThread = false;		
			
		// Faire la référence vers la table courante
		objTable = tableCourante;	
			
		// Choisir un id de personnage aléatoirement
		// TODO: choisir aléatoirement
		intIdPersonnage = 1;
		
		// Initialisation du pointage
		intPointage = 0;
		
		// Initialisation à null de la position, le joueur virtuel n'est nul part
		objPositionJoueur = null;
		
	    // Créer la liste des objets utilisables qui ont été ramassés
	    lstObjetsUtilisablesRamasses = new TreeMap();
		

	}


	/**
	 * Cette méthode est appelée lorsqu'une partie commence. C'est la thread
	 * qui fait jouer le joueur virtuel
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
		Integer intTempsReflexionQuestion;
		
		// Cette variable contient le temps de réflexion pour choisir 
		// le prochaine coup à jouer
		Integer intTempsReflexionCoup;
		
		while(bolStopThread == false)
		{
			
			// Vérifier s'il faut rechercher une nouvelle case cible
			if (objPositionFinaleVisee == null || 
			    (objPositionFinaleVisee.x == objPositionJoueur.x && 
			     objPositionFinaleVisee.y == objPositionJoueur.y))
			{
				
				// TODO: recherche intelligente de PositionFinaleVisee
				// (aléatoire pour l'instant)
				objPositionFinaleVisee = trouverPositionFinaleVisee();
				
			}
			
			// Déterminer le temps de réflexion pour le prochain coup
			// TODO: Temps de réflexion selon niveau de difficulté
			intTempsReflexionCoup = 2;
			
			// Pause pour moment de réflexion de décision
			pause(intTempsReflexionCoup);
			
			// Trouver une case intermédiaire
			objPositionIntermediaire = trouverPositionIntermediaire();
			
			// Déterminer si le joueur virtuel répondra à la question
			// TODO: Déterminer selon niveau de difficulté (pour fin
			//       de test, on va le laisser toujours réussis)
			bolQuestionReussie = true;
			
			// Déterminer le temps de réponse à la question
			// TODO: Temps de réponse selon niveau de difficulté
			intTempsReflexionQuestion = 8;

			// Pause pour moment de réflexion de réponse
			pause(intTempsReflexionQuestion);	
					
			// Faire déplacer le personnage si le joueur virtuel a 
			// réussi à répondre à la question
			if (bolQuestionReussie == true)
			{
				// Déplacement du joueur virtuel
				deplacerJoueurVirtuelEtMajPlateau(objPositionIntermediaire);
			}
			else
			{
				// Pause pour rétroaction
				pause(TEMPS_RETROACTION);
			}
				
		}
	}
	
	/*
	 * Cette fonction trouve une case intermédiaire qui permettra au joueur virtuel
	 * de progresser vers sa mission qu'est celle de se rendre à la case finale visée.
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

        // Déclaration d'un tableau qui va permettre de savoir quelle ligne on choisie
	    boolean bolLigne[] = new boolean[4];
	    bolLigne[DROITE] = true;         // DROITE
	    bolLigne[BAS] = true;         // BAS
	    bolLigne[GAUCHE] = true;         // GAUCHE
	    bolLigne[HAUT] = true;         // HAUT
	    
	    // Déclaration d'un tableau qui contient le dx et dy de gauche, droite, etc.
	    Point ptDxDy[] = new Point[4];
	    ptDxDy[DROITE] = new Point(1,0);
	    ptDxDy[BAS] = new Point(0,-1);
	    ptDxDy[GAUCHE] = new Point(-1,0);
	    ptDxDy[HAUT] = new Point(0,-1);
	    
	    int intNbLignes = objttPlateauJeu[0].length;
	    int intNbColonnes = objttPlateauJeu.length;
	    

        CaseCouleur objCaseCouleurTemp = null;

	    // On élimine deux directions (ou 3 si en ligne) qui ne permettraient pas de se rapprocher
	    // de la case visée
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
		
		// Ici, on détermine la meilleur ligne selon intValeurLigne[]
		int intPlusGrand = 0;
		for ( i = 1; i <= 3; i++)
		{
			if (intValeurLigne[i] > intValeurLigne[intPlusGrand])
			{
				intPlusGrand = i;
			}
		}
		
		// On élimine les autres lignes (en même temps on va s'assurer qu'il n'y
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
		
		
		// Déclaration d'un tableau qui va contenir les pourcentages pour le choix de
		// la case. On va modifier ces pourcentages selon la disposition des pièces
		int intPourcentageCase[] = new int [6];
		
		// TODO: remplir selon niveau de difficulté
		intPourcentageCase[0] = 5;
		intPourcentageCase[1] = 19;
		intPourcentageCase[2] = 40;
		intPourcentageCase[3] = 25;
		intPourcentageCase[4] = 10;
		intPourcentageCase[5] = 1;
		

        // Parcourir les cases et modifier les pourcentages selon les pièces et trous trouvés
        // On s'assure aussi que le joueur ne dépasse pas sa position finale visée
        // TODO: Supprimer les copier-coller
		for (i = 1; i <= 6 ; i++)
		{
			// Vérifier les cases à droite
			if (bolLigne[DROITE])
			{
				if (objPositionJoueur.x + i < intNbColonnes && objttPlateauJeu[objPositionJoueur.x + i][objPositionJoueur.y] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x + i][objPositionJoueur.y] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x + i][objPositionJoueur.y];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de dépasser une pièce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile à capturée et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages supérieures
				    		// et on les ajoute à cette case puis on arrête de chercher
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

			// Vérifier les cases à gauche
			if (bolLigne[GAUCHE])
			{
				if (objPositionJoueur.x - i >= 0 && objttPlateauJeu[objPositionJoueur.x - i][objPositionJoueur.y] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x - i][objPositionJoueur.y] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x - i][objPositionJoueur.y];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de dépasser une pièce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile à capturée et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages supérieures
				    		// et on les ajoute à cette case puis on arrête de chercher
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

			// Vérifier les cases à bas
			if (bolLigne[BAS])
			{
				if (objPositionJoueur.y + i < intNbLignes && objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y+i] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y+i] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y+i];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de dépasser une pièce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile à capturée et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages supérieures
				    		// et on les ajoute à cette case puis on arrête de chercher
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
			
			// Vérifier les cases en haut
			if (bolLigne[BAS])
			{
				if (objPositionJoueur.y - i >= 0 && objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y-i] != null)
				{
			
			        if (objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y-i] instanceof CaseCouleur)
			        {
			            objCaseCouleurTemp = (CaseCouleur) objttPlateauJeu[objPositionJoueur.x][objPositionJoueur.y-i];
			    	    if (objCaseCouleurTemp.obtenirObjetCase() instanceof Piece)
			    	    {
			    		    // On ne permet pas de dépasser une pièce, d'ailleurs, plus celle-ci est proche,
				    		// plus elle sera facile à capturée et les points obtenues sont trop importants
				    		// pour qu'on tente d'aller plus loin, donc on prend tous les pourcentages supérieures
				    		// et on les ajoute à cette case puis on arrête de chercher
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
		
		// On génère un nombre entre 1 et 100
		intPourcentageAleatoire = ClassesUtilitaires.UtilitaireNombres.genererNbAleatoire(100)+1;

		
		int intValeurAccumulee = 0;
		int intDecision = 0;
		
		// On détermine à quel décision cela appartient
		for (i = 0 ; i <= DEPLACEMENT_MAX-1 ; i++)
		{
			intValeurAccumulee += intPourcentageCase[i];
			if (intPourcentageAleatoire <= intValeurAccumulee)
			{
				intDecision = i + 1;
				break;
			}
		}
		
		// Retourner la position qui correspond à la décision
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
			
		// Déterminer les cases utiles pour atteindre la position finale
		// TODO
		
		// Choisir parmi ces cases selon niveau de difficulté et type de joueur
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
	 * d'atteindre. C'est ici que la personnalité du joueur peut influencer la décision.
	 * Par la suite, le joueur virtuel devra choisir des cases intermédiaires pour se
	 * rendre à la case finale, cela peut être immédiat au prochain coup.
	 */
	private Point trouverPositionFinaleVisee()
	{
		
		Point objPositionTrouvee = null;
		
		// Parcourir toutes les cases avec pièces et trouver
		// la plus proche 
		//TODO: recherche d'une bonne case
		
		// Présentement, on choisit aléatoirement
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
     * Cette fonction s'occupe de déplacer le joueur virtuel s'il a bien répondu
     * à la question, met à jour le plateau de, envoie les événements aux autres joueurs
     * et modifie le pointage et la position du joueur virtuel
     */
    private void deplacerJoueurVirtuelEtMajPlateau(Point objNouvellePosition)
    {
    	String collision = "";
    	
    	// Déclaration de l'objet de retour
    	// TODO: Vérifier si c'est utile pour un joueur virtuel
    	//RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
    	
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
					// 		 les pièces sur le plateau de jeu s'il n'y en n'a
					//		 plus
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
		
		// Créer l'objet de retour
		/*objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(true, intNouveauPointage);
		objRetour.definirObjetRamasse(objObjetRamasse);
		objRetour.definirObjetSubi(objObjetSubi);
		objRetour.definirNouvellePosition(objNouvellePosition);
		objRetour.definirCollision( collision );*/
		
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
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de démarrer la partie, alors on peut envoyer un 
			// événement à cet utilisateur

			// Obtenir un numéro de commande pour le joueur courant, créer 
			// un InformationDestination et l'ajouter à l'événement
		    joueurDeplacePersonnage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));

		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEv.ajouterEvenement(joueurDeplacePersonnage);
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
