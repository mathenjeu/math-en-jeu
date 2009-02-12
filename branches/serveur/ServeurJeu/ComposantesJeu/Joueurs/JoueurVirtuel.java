package ServeurJeu.ComposantesJeu.Joueurs;

import ClassesUtilitaires.UtilitaireNombres;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.InformationPartie;
import java.awt.Point;
import java.util.TreeMap;
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Cases.CaseSpeciale;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import ServeurJeu.Configuration.GestionnaireMessages;
 
/**
 * @author Jean-François Fournier
 */
public class JoueurVirtuel extends Joueur implements Runnable {
	
	// Cette variable va contenir le nom du joueur virtuel
	private String strNom;
        
        // Le joueur virtuel est-il destiné à subir une banane?
        public String vaSubirUneBanane;
	
    // Déclaration d'une référence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;
	
	// Déclaration d'une référence vers un objet contenant tous
	// les paramètres des joueurs virtuels
	private ParametreIA objParametreIA;
	
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
        
        private int intArgent;

	// Déclaration de la position du joueur virtuel dans le plateau de jeu
	private Point objPositionJoueur;

	// Déclaration d'une liste d'objets utilisables ramassés par le joueur
	// virtuel
	private TreeMap lstObjetsUtilisablesRamasses;
	
	// Déclaration d'une référence vers le controleur jeu
	private ControleurJeu objControleurJeu;
	
    // Déclaration d'une variable qui contient le nombre de fois 
    // que le joueur virtuel a joué à un mini-jeu
    private int intNbMiniJeuJoues;
    
    // Déclaration d'une variable qui contient le nombre de fois
    // que le joueur virtuel a visité un magasin
    private int intNbMagasinVisites;
    
    // Déclaration d'un tableau contenant les temps où le joueur
    // virtuel peut jouer à un mini-jeu (lorsque le temps arrive, 
    // cela donne un jeton au joueur virtuel, lorsqu'il croisera 
    // une case de mini-jeu, il y jouera)
    private int tJetonsMiniJeu[];
    
    // Déclaration d'un tableau contenant les temps où le joueur
    // virtuel peut se servir d'un magasin (lorsque le temps arrive,
    // cela donne un jeton au joueur virtuel, lorsqu'il croisera un
    // magasin, il ira pour peut-être acheter un objet)
    private int tJetonsMagasins[];
    
    // Cette variable contient le nombre maximum d'items que le joueur
    // virtuel traînera
    private int intNbObjetsMax;
    
    // Cette liste va contenir les magasins déjà visités
    // par le joueur virtuel, pour empêcher qu'il les visite
    // à plus d'une reprise
    private Vector lstMagasinsVisites;
    
    // Tableau contenant une référence vers le plateau de jeu
    private Case objttPlateauJeu[][];
    
    // Obtenir le nombre de lignes et de colonnes du plateau de jeu
    private int intNbLignes;
    private int intNbColonnes;
    
    // Cette matrice contiendra les valeurs indiquants quelles cases ont
    // été parcourue par l'algorithme
    private boolean matriceParcourue[][];
    
    // Cette matrice contiendra, pour chaque case enfilée, de quelle case
    // celle-ci a été enfilée. Cela nous permettra de trouver le chemin
    // emprunté par l'algorithme. 
    private Point matricePrec[][];

    // Déclaration d'une matrice qui contiendra un pointage pour chaque
    // case du plateau de jeu, ce qui permettra de choisir le meilleur
    // coup à jouer
    private int matPoints[][];
    
	// Constante pour la compilation conditionnelle
	private static final boolean ccDebug = false;
	
	// Constante pour accélérer les déplacements
	// (voir fonction pause() )
	private static final boolean ccFast = false;
	
	// Objet logger pour afficher les erreurs dans le fichier log
	static private Logger objLogger = Logger.getLogger(JoueurVirtuel.class);
	
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
	    GestionnaireEvenements gestionnaireEv, ControleurJeu controleur, int idPersonnage)
	{
	    objControleurJeu = controleur;
	    
	    objParametreIA = objControleurJeu.obtenirParametreIA();
	    
		strNom = nom;
                vaSubirUneBanane = "";
		
		// Cette variable sera utilisée dans la thread
		objPositionFinaleVisee = null;
		
		// Faire la référence vers le gestionnaire d'évenements
		objGestionnaireEv = gestionnaireEv;
			
		// Cette variable sert à arrêter la thread lorsqu'à true
		bolStopThread = false;		
			
		// Faire la référence vers la table courante
		objTable = tableCourante;	
			
		
		if (idPersonnage == -1)
		{
			// Choisir un id de personnage aléatoirement
			intIdPersonnage = genererNbAleatoire(ParametreIA.NOMBRE_PERSONNAGE_ID) + 1;
		}
		else
		{
			// Affecter le id personnage pour ce joueur
			intIdPersonnage = idPersonnage;
		}
		
		// Initialisation du pointage
		intPointage = 0;
                intArgent = 0;
		
		// Initialisation à null de la position, le joueur virtuel n'est nul part
		objPositionJoueur = null;
		
	    // Créer la liste des objets utilisables qui ont été ramassés
	    lstObjetsUtilisablesRamasses = new TreeMap();
		
        // Création du profil du joueur virtuel
        intNiveauDifficulte = niveauDifficulte;

        // Tableau contenant une référence vers le plateau de jeu
        objttPlateauJeu = objTable.obtenirPlateauJeuCourant();
        
        // Obtenir le nombre de lignes et de colonnes du plateau de jeu
        intNbLignes = objttPlateauJeu.length;
        intNbColonnes = objttPlateauJeu[0].length;
        
        // Initialiser les matrices
        matriceParcourue = new boolean[intNbLignes][intNbColonnes];
        matricePrec = new Point[intNbLignes][intNbColonnes];
        matPoints = new int[intNbLignes][intNbColonnes];

        // Déterminer les temps des jetons des minijeus
        determinerJetonsMiniJeu();

        // Au départ, le joueur virtuel n'a joué aucun mini-jeu
        intNbMiniJeuJoues = 0;
        
        // Déterminer les temps des jetons pour les magasins
        determinerJetonsMagasins();
        
        // Au départ, le joueur virtuel n'a pas visité de magasin
        intNbMagasinVisites = 0;

        // Définir le nombre d'objets max par une valeur de base
        intNbObjetsMax = ParametreIA.MAX_NOMBRE_OBJETS;
        
        // Créer une liste de magasin déjà visité vide
        lstMagasinsVisites = new Vector();
	}


	/**
	 * Cette méthode est appelée lorsqu'une partie commence. C'est la thread
	 * qui fait jouer le joueur virtuel.
	 * 
	 */
	public void run()
	{
		try
		{	
		    // Assigner le priorité TRACE au logger pour qu'il puisse
		    // écrire les traces des exceptions si il en arrivent
		    objLogger.setLevel((Level) Level.TRACE);
		
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
			
			// La grandeur de déplacement demandé par le joueur virtuel
			int intGrandeurDeplacement;
			
			// Le pourcentage de réussite à la question
			int intPourcentageReussite;
			
			while(bolStopThread == false)
			{		

				//System.out.println("bolStopThead == false"); // trace
			
				// Déterminer le temps de réflexion pour le prochain coup
				intTempsReflexionCoup = obtenirTempsReflexionCoup();
	
				// Pause pour moment de réflexion de décision
				pause(intTempsReflexionCoup);
				
				
				// CHANGES WIN THE GAME - OFF
				// **********************************************************
				
                // Trouver une case intéressante à atteindre
 
                if (reviserPositionFinaleVisee() == true)
                {
                    int essais = 0;
                    do
                    {
                        objPositionFinaleVisee = trouverPositionFinaleVisee();
                        essais++;
                    }while(essais < 50 && objPositionFinaleVisee.equals(this.obtenirTable().obtenirPositionWinTheGame()));
                }

                
                // On trouve une position entre le joueur virtuel et son objectif
                {
                    int essais = 0;
                    if(this.obtenirTable().peutAllerSurLeWinTheGame(this.obtenirPointage())) objPositionIntermediaire = trouverPositionIntermediaire();
                    else
                    {
                        do
                        {
                            objPositionIntermediaire = trouverPositionIntermediaire();
                            essais++;
                        }while(essais < 50 && objPositionIntermediaire.equals(this.obtenirTable().obtenirPositionWinTheGame()));
                    }
                }
                /* **********************************************************
                 
                
                 
                // ORIGINAL CODE - TODO CHANGE BACK TO GET WIN THE GAME
                // AS WELL AS THE CHANGES IN SALLE.JAVA
				/* **********************************************************
                                // Trouver une case intéressante à atteindre
                                // Si on a assez de points pour atteindre le WinTheGame, allons-y!
                                if(!this.obtenirTable().obtenirButDuJeu().equals("original") && this.obtenirTable().peutAllerSurLeWinTheGame(this.obtenirPointage()))
                                {
                                    objPositionFinaleVisee = this.obtenirTable().obtenirPositionWinTheGame();
                                }
                                else
                                {
                                    if (reviserPositionFinaleVisee() == true)
                                    {
                                        int essais = 0;
                                        do
                                        {
                                            objPositionFinaleVisee = trouverPositionFinaleVisee();
                                            essais++;
                                        }while(!this.obtenirTable().obtenirButDuJeu().equals("original") && essais < 50 && objPositionFinaleVisee.equals(this.obtenirTable().obtenirPositionWinTheGame()));
                                    }
                                }
                                
                                // On trouve une position entre le joueur virtuel et son objectif
                                {
                                    int essais = 0;
                                    if(this.obtenirTable().peutAllerSurLeWinTheGame(this.obtenirPointage())) objPositionIntermediaire = trouverPositionIntermediaire();
                                    else
                                    {
                                        do
                                        {
                                            objPositionIntermediaire = trouverPositionIntermediaire();
                                            essais++;
                                        }while(!this.obtenirTable().obtenirButDuJeu().equals("original") && essais < 50 && objPositionIntermediaire.equals(this.obtenirTable().obtenirPositionWinTheGame()));
                                    }
                                }
				 ********************************************************** */
				
				
				// S'il y a erreur de recherche ou si le joueur virtuel est pris
				// on ne le fait pas bouger
				if (objPositionIntermediaire.x != objPositionJoueur.x || 
				    objPositionIntermediaire.y != objPositionJoueur.y)
				{
					// Calculer la grandeur du déplacement demandé
					intGrandeurDeplacement = obtenirPointage(objPositionJoueur, objPositionIntermediaire);
					
					// Vérifier si on utilise un objet livre
					boolean bolUtiliserLivre = nombreObjetsPossedes(Objet.UID_OU_LIVRE) > 0;
					    
					// Aller chercher le pourcentage de réussite à la question
					intPourcentageReussite = objParametreIA.tPourcentageReponse[intNiveauDifficulte][intGrandeurDeplacement-1];
					
					if (bolUtiliserLivre == true)
					{
                                            if (ccDebug)
                                            {
                                                    System.out.println("Utilise objet: Livre");
                                            }
					    
					    // Enlever un objet livre des objets du joueur
					    enleverObjet(Objet.UID_OU_LIVRE);
	
					}
					
		            // Vérifier si c'est une question à choix de réponse
		            boolean bolQuestionChoixDeReponse = (genererNbAleatoire(100)+1 <= ParametreIA.RATIO_CHOIX_DE_REPONSE);
					if (bolQuestionChoixDeReponse)
					{						
						// Augmenter les chances de réussites utilisant le 
						// tableau de % de réponse lorsqu'il reste des charges
						// à l'objet et si cette question est à choix de réponse
					    intPourcentageReussite = objParametreIA.tPourcentageReponseObjetLivre[intNiveauDifficulte][intGrandeurDeplacement-1];
					}
					
	    			// Déterminer si le joueur virtuel répondra à la question
	                bolQuestionReussie = (genererNbAleatoire(100)+1 <= intPourcentageReussite);
	    			        
	    			// Déterminer le temps de réponse à la question
	    			intTempsReflexionQuestion = obtenirTempsReflexionReponse();
	                
	    			// Pause pour moment de réflexion de réponse
	    			pause(intTempsReflexionQuestion);	
	    					
	    			// Faire déplacer le personnage si le joueur virtuel a 
	    			// réussi à répondre à la question
	    			if (bolQuestionReussie == true)
	    			{
	    				//System.out.println("Question reussie"); // trace
	    				
	    				// Déplacement du joueur virtuel
                                        if(!vaSubirUneBanane.equals(""))
                                        {
                                            Banane.utiliserBanane(vaSubirUneBanane, this.obtenirPositionJoueur(), this.obtenirNom(), this.obtenirTable(), false);
                                            vaSubirUneBanane = "";
                                        }
                                        else
                                        {
                                            deplacerJoueurVirtuelEtMajPlateau(objPositionIntermediaire);
                                        }
	    				
	    				// Obtenir le temps que le déplacement dure
	    				intTempsDeplacement = obtenirTempsDeplacement(obtenirPointage(objPositionJoueur, objPositionIntermediaire));
	
	    				// Pause pour laisser le personnage se déplacer
	    				pause(intTempsDeplacement);
	    			}
	    			else
	    			{
	    				if (ccDebug)
	    				{
	    					System.out.println("Question ratée");
	    				}
	
	    				// Pause pour rétroaction
	    				pause(ParametreIA.TEMPS_RETROACTION);
	    			}
	    			
	    	    }	
			}
		}
		catch (Exception e)
		{
			// Envoyer la trace de l'erreur dans le log
			objLogger.trace(GestionnaireMessages.message("joueur_virtuel.erreur_thread") + strNom, e);
			
			// Envoyer la trace de l'erreur à l'écran
			e.printStackTrace();
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

        // Variable pour boucler dans le tableau ptDxDy[]
        int dxIndex = 0;
        
        // Ce tableau servira à enfiler les cases de façons aléatoire, ce qui
        // permettra de peut-être trouver différents chemin
        int tRandom[] = {0,1,2,3};
        
        // Sert pour brasser tRandom
        int indiceA;
        int indiceB;
        int indiceNombreMelange;
        int valeurTemp;
        
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

                       
            // Enfiler les 4 cases accessibles depuis cette position  
            for (dxIndex = 0; dxIndex < 4; dxIndex++)
            {
                ptPosTemp.x = ptPosDefile.x + objParametreIA.ptDxDy[tRandom[dxIndex]].x;
                ptPosTemp.y = ptPosDefile.y + objParametreIA.ptDxDy[tRandom[dxIndex]].y;
                
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
        
        boolean considererMiniJeu = determinerPretAJouerMiniJeu();
        boolean considererMagasin = determinerPretAVisiterMagasin();
        
		for (int i = 0; i < lstPositions.size() - 1; i++)
		{

           ptTemp = (Point) lstPositions.get(i);
           
           int intPointsCase = 0;
           
           if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseCouleur)
           {
               if (((CaseCouleur) objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof Piece)
               {
               	   // Piece sur la case
                   intPointsCase = objParametreIA.objParametreIAPiece.intPointsChemin;
               }
               else if (((CaseCouleur) objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof Magasin)
               {
                   if (considererMagasin == true)
                   {
                   	   // Magasin sur la case
                       intPointsCase = objParametreIA.objParametreIAMagasin.intPointsChemin;
                   }
               }
               
               else if (((CaseCouleur) objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof ObjetUtilisable &&
                   lstObjetsUtilisablesRamasses.size() < intNbObjetsMax)
               {
                   ObjetUtilisable objObjet = (ObjetUtilisable)((CaseCouleur)objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase();
                   if (objObjet.estVisible() && determinerPretARamasserObjet(objObjet.obtenirUniqueId()))
                   {
                       // Objet réponse sur la case
                       intPointsCase = objParametreIA.tParametresIAObjetUtilisable[objObjet.obtenirUniqueId()].intPointsChemin;
                       intPointsCase -= objParametreIA.tParametresIAObjetUtilisable[objObjet.obtenirUniqueId()].intPointsEnleverDistance * (nombreObjetsPossedes(objObjet.obtenirUniqueId()));
                       if (intPointsCase < 0)
                       {
                           intPointsCase = 0;
                       }
                   }
               }

           }
           else if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseSpeciale)
           {
               if (considererMiniJeu == true)
               {
               	   // Mini-jeu sur la case
                   intPointsCase = objParametreIA.objParametreIAMinijeu.intPointsChemin;
               }
           }
           
           intPoints += intPointsCase;
           
		}
		
		return intPoints;
	}
	
	/*
	 * Cette fonction trouve une case intermédiaire qui permettra au joueur virtuel
	 * de progresser vers sa mission qu'est celle de se rendre à la case finale visée.
	 */
	private Point trouverPositionIntermediaire()
	{
            // Si on est déjà sur le WinTheGame et qu'on a le pointage requis, restons là!!
            // Peut-être que les joueurs virtuels essaient ensuite de se déplacer
            // mais le serveur refusera alors ils resteront vraiment là
            if(this.obtenirTable().peutAllerSurLeWinTheGame(this.obtenirPointage()) && this.obtenirPositionJoueur().equals(this.obtenirTable().obtenirPositionWinTheGame())) return this.obtenirPositionJoueur();
            
	    // Variable contenant la position à retourner à la fonction appelante
		Point objPositionTrouvee;

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

        if (ccDebug)
        {
        	System.out.print("Chemin : ");
        	for (int i = lstPositionsTrouvees.size()-1 ; i >=0 ; i--)
        	{
        		if (i < lstPositionsTrouvees.size()-1)
        		{
        			System.out.print(", ");
        		}
        		
        		System.out.print("(" + ((Point)lstPositionsTrouvees.get(i)).x +"-" +
        		    ((Point)lstPositionsTrouvees.get(i)).y + ")");
        	}
        	System.out.println("");
        }
        
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
        
        boolean bolConsidererMiniJeu = determinerPretAJouerMiniJeu();
        boolean bolConsidererMagasin = determinerPretAVisiterMagasin();
        
        // On part du début du chemin jusqu'à la fin et on trouve le premier croche
        for (int i = lstPositionsTrouvees.size() - 2; i >= 0 ; i--)
        {
            ptTemp = (Point) lstPositionsTrouvees.get(i);

            iIndiceTableau++;       
            
            // S'il y a un mini-jeu ici et que le joueur n'a pas de jeton
            // pour y jouer, alors on va mettre à 0 les possiblités
            // de choisir cette case
	        if (bolConsidererMiniJeu == false && objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseSpeciale)
	        {
	        	traiterCaseEliminerDansLigne(intPourcentageCase, iIndiceTableau -1);
	        }
                                 
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
            
            // S'il y a un mini-jeu et que le joueur à un jeton pour
            // un mini-jeu, on s'assure de ne pas dépasser cette case
            else if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseSpeciale && 
                bolConsidererMiniJeu == true)
            {
            	traiterPieceTrouveeDansLigne(intPourcentageCase, iIndiceTableau - 1);
                break; 
            }
            
            // S'il y a un magasin et que le joueur à un jeton pour
            // le visiter, on s'assure de ne pas dépasser cette case
            else if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseCouleur && 
                ((CaseCouleur)objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof Magasin &&
                bolConsidererMagasin == true)
            {
            	traiterPieceTrouveeDansLigne(intPourcentageCase, iIndiceTableau - 1);
                break; 
            }
            
            // S'il y a un objet visible, alors on s'assurer de ne pas dépasser cette case
            else if (objttPlateauJeu[ptTemp.x][ptTemp.y] instanceof CaseCouleur &&
                ((CaseCouleur)objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase() instanceof ObjetUtilisable &&
                determinerPretARamasserObjet(((ObjetUtilisable)((CaseCouleur)objttPlateauJeu[ptTemp.x][ptTemp.y]).obtenirObjetCase()).obtenirUniqueId()))
            {
            	traiterPieceTrouveeDansLigne(intPourcentageCase, iIndiceTableau - 1);
                break; 
            }

            
            if (iIndiceTableau > ParametreIA.DEPLACEMENT_MAX-1)
            {
                break;
            }  
               
        }
        
        // Si on est près de la position finale, on s'assure de ne pas la dépasser
        if (lstPositionsTrouvees.size() <= ParametreIA.DEPLACEMENT_MAX)
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
        for (int i = 0 ; i <= ParametreIA.DEPLACEMENT_MAX-1 ; i++)
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
        if (ccDebug)
        {
        	int intTempsEcoule = objTable.obtenirTempsTotal() * 60 - objTable.obtenirTempsRestant();
	        System.out.println("Temps ecoule:" + intTempsEcoule);
	        System.out.println("Magasins: " + intNbMagasinVisites + "/" + 
	            obtenirNombreJetonsDisponibles(tJetonsMagasins, intTempsEcoule));
	        System.out.println("Minijeu: " + intNbMiniJeuJoues + "/" + 
	            obtenirNombreJetonsDisponibles(tJetonsMiniJeu, intTempsEcoule));
		    System.out.println("Pointage: " + intPointage);
		    
		    System.out.print("Liste objets: ");
	        if (lstObjetsUtilisablesRamasses.size() > 0)
	        {

		        Set lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
		        Iterator objIterateurListeObjets = lstEnsembleObjets.iterator();
		        int i = 0;
		        	        
		        while (objIterateurListeObjets.hasNext())
		        {
		        	ObjetUtilisable objObjet = (ObjetUtilisable)(((Map.Entry)(objIterateurListeObjets.next())).getValue());
		        	
		        	if (i > 0)
		        	{
		        		System.out.print(", ");
		        	}
		        	
		        	if (objObjet instanceof Livre)
		        	{
		        		System.out.print("Livre");
		        	}
		        	System.out.print("(" + objObjet.obtenirId() + ")");
		        	i++;
		        }
		        System.out.println("");
	        }
	        else
	        {
	        	System.out.println("Aucun objet");
	        }
	        
	        System.out.println("Position du joueur: " + objPositionJoueur.x + "," + 
	            objPositionJoueur.y);        
	        System.out.println("Position trouvee: " + objPositionTrouvee.x + "," + 
	            objPositionTrouvee.y);           
	        System.out.println("Position a atteindre: " + objPositionFinaleVisee.x + "," + 
	            objPositionFinaleVisee.y);
        }
        //--------------------------------
        
        return objPositionTrouvee;

	}
	
	/* Cette fonction permet d'attribuer de l'importance à une case en
	 * lui attribuant des points
	 *
	 * ptCase: La case à traiter
	 *
	 * pointCase: Le pointage à ajouter à la case
	 *
	 * pointAleatoire: on ajoute entre [0, pointAleatoire[ au pointage, 
	 *                 ce qui ajoute un élément aléatoire
	 *
	 * limitDistance: On enlève un nombre de points par coup de distance, 
	 *                limité à limitDistance, ce qui permet d'attirer le 
	 *                joueur virtuel vers des cases importantes même si
	 *                elles sont très loin (empêche les points négatifs)
	 *
	 * pointDistance: Nombre de points qu'on décrémente par coup de distance
	 *
	 * ttPointsRegion: Un tableau contenant les informations pour 
	 *                 ajouter un nombre de points aux cases adjacentes, 
	 *                 ce qui permet de valoriser les regroupements de cases
	 *                 importantes
	 *
	 * deplacementMoyen: Le déplacement moyen du joueur virtuel, trouvé
	 *                   selon son niveau de difficulté
	 * 
	 * dblFacteurAdj: Un facteur d'ajustement pour les points ajoutés
	 *                via ttPointsRegion, normalement à 1, on le met 
	 *                entre 0 et 1 si on veut rendre moins important la
	 *                case, et à 0 si on ne veut pas ajouter de points
	 *                aux cases adjacentes
	 */ 
	private void attribuerImportanceCase(Point ptCase, int pointCase, 
	    int pointAleatoire, int limitDistance, int pointDistance, 
	    int[][] ttPointsRegion, double deplacementMoyen,
	    double dblFacteurAdj)
	{

        int x = ptCase.x;
        int y = ptCase.y;
        
        // Point en cours d'analyse
        Point ptTemp = ptCase;

        // Autre point en cours d'analyse
        Point ptTemp2 = new Point(0,0);
        
        // Chemin entre le joueur et une case importante analysée
        Vector lstChemin;
        
        // Cette variable contiendra le nombre de coups estimé pour se rendre
        // à la case en cours d'analyse
        double dblDistance;
        
        // Déplacement moyen, contient le nombre de cases que l'on peut
        // s'attendre à franchir par coup (prend en compte niveau de
        // difficulté)
        double dblDeplacementMoyen = deplacementMoyen;
        
        // Ce facteur d'éloignement sert à considérer l'éloingement
        // de la case lorsqu'on attribue des points aux cases adjacentes
        double dblFacteurEloignement = 1.0; 
        
        // Cette case augmente d'environ pointCase +pointAleatoire/2 
        // le pointage
        matPoints[x][y] += pointCase + genererNbAleatoire(pointAleatoire);
    
        // On va trouver le chemin le plus court
        lstChemin = trouverCheminPlusCourt(objPositionJoueur, ptTemp);
        
        if (lstChemin == null)
        {
            // Une case inaccessible ne doit pas être choisie
            matPoints[x][y] = ParametreIA.POINTS_IGNORER_CASE;
        }
        else
        {
            // On a un chemin qui contient chaque case, maintenant, on
            // va trouver, pour ce chemin, le nombre de coups estimé
            // pour le parcourir, et ce, en prenant en compte le niveau
            // de difficulté
            // TODO:Prendre en compte nombre de croches
            dblDistance = lstChemin.size() / dblDeplacementMoyen;
            

            // Pour permettre de quand même prioriser les cases
            // lointaines, on va limiter le nombre de coups
            // ce qui enlèvera un maximum de points pour une case lointaine
            // et évitera les pointages négatifs
            if (dblDistance > limitDistance)
            {
                dblDistance = limitDistance;
            }
            
            // Plus la case est loin, plus son pointage diminue
            // On enlève pointDistance points par coup
            matPoints[x][y] -= (int) (pointDistance * dblDistance + .5);

            // Calculer le facteur d'éloignement pour les cases adjacents
            dblFacteurEloignement = ((double)pointCase - pointDistance * dblDistance) / pointCase;
            
            if (dblFacteurEloignement <= 0)
            {
            	dblFacteurEloignement = ParametreIA.FACTEUR_AJUSTEMENT_MIN;
            }
            
            // Cette case étant accessible, on va augmenter les
            // points des cases aux alentours pour attirer
            // le joueur virtuel vers des regroupements de cases
            // importantes
            int intBorneI = ttPointsRegion.length - 1;
            int intBorneJ = ttPointsRegion[0].length - 1;
            
            for (int i = -intBorneI; i <= intBorneI; i++)
            {
                for (int j = -intBorneJ; j <= intBorneJ; j++)
                {
                    ptTemp2.x = x + i;
                    ptTemp2.y = y + j;
                    
                    if (ptTemp2.x >= 0 && ptTemp2.x < intNbLignes &&
                        ptTemp2.y >=0 && ptTemp2.y < intNbColonnes &&
                        objttPlateauJeu[ptTemp2.x][ptTemp2.y] != null)  
                    {
                        matPoints[ptTemp2.x][ptTemp2.y] += ttPointsRegion[intBorneI - Math.abs(i)][Math.abs(j)] * dblFacteurAdj * dblFacteurEloignement;
                    }  
                } 
            }
        }

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
        double dblDeplacementMoyen = objParametreIA.tDeplacementMoyen[intNiveauDifficulte];
        
        // Ce tableau contiendra les cases les plus intéressantes
        Point tPlusGrand[] = new Point[ParametreIA.NOMBRE_CHOIX_ALTERNATIF];
        
        // Variable qui indiquera à l'algorithme s'il faut considérer
        // les minis-jeu. On considère les minis-jeu s'il y a un jeton de
        // disponible
        boolean bolConsidererMiniJeu = determinerPretAJouerMiniJeu();
        
        // Variable qui indiquera à l'algorithme s'il faut considérer
        // les magasins. 
        boolean bolConsidererMagasin = determinerPretAVisiterMagasin();
        
        //Variables pour calcul des points pour objets et pièces
		int intPointsObjet;
		int intPointsEnleverNb;
		int intPointsEnleverDistance;
		int intPointsAleat;
		int intDistanceMax;

        // Variable qui contiendra après calcul les points pour une case
        int intPointsCase;
                    		
        // Facteur d'ajustement pour les regroupements de pièces
        double dblFacteurAdj;
               
        // Initialiser la matrice
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                // Pointage de départ (environ 0)
                matPoints[x][y] = ParametreIA.POINTS_BASE_CASE_COULEUR + 
                    genererNbAleatoire(ParametreIA.POINTS_ALEATOIRE_CASE_COULEUR);
            }
        }

        // Parcourir toutes les cases du plateau et leur attribuer
        // un pointage
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                ptTemp.x = x;
                ptTemp.y = y;
                    
                if (objPositionJoueur.x == x && objPositionJoueur.y == y)
                {
                    // La position courante du joueur ne doit pas être choisie
                    matPoints[x][y] = ParametreIA.POINTS_IGNORER_CASE;
                }
                else
                {

                    // Modification du pointage de la case
                    if (objttPlateauJeu[x][y] == null)
                    {
                        // Une case nulle ne doit pas être chosie
                        matPoints[x][y] = ParametreIA.POINTS_IGNORER_CASE;
                    }
                    
                    
                    // Objets
                    else if (objttPlateauJeu[x][y] instanceof CaseCouleur && 
                        ((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase() instanceof ObjetUtilisable &&
                        lstObjetsUtilisablesRamasses.size() < intNbObjetsMax)
                    {
                    	// Ici, dépendamment de l'objet, on peut lui attribuer
                    	// un pointage différent.
                    	// TODO: Prendre en compte objets déjà ramassés
                    	//       pour par exemple diminuer les pointages
                    	//       pour les objets qu'on possède déjà
                    	
                    	// Obtenir une référence à l'objet sur la case
                    	ObjetUtilisable objObjet = (ObjetUtilisable)((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase();
                    	
                    	// Le joueur virtuel ne voit pas les objets invisibles
                    	if (objObjet.estVisible() && determinerPretARamasserObjet(objObjet.obtenirUniqueId()))
                    	{
                    		// Aller chercher l'UID de l'objet
                    		int uidObjet = objObjet.obtenirUniqueId();
                    		
                    		// Aller chercher les paramètres pour cet objet
                    		intPointsObjet = objParametreIA.tParametresIAObjetUtilisable[uidObjet].intValeurPoints;
                    		intPointsEnleverNb = objParametreIA.tParametresIAObjetUtilisable[uidObjet].intPointsEnleverQuantite;
                    		intPointsEnleverDistance = objParametreIA.tParametresIAObjetUtilisable[uidObjet].intPointsEnleverDistance;
                    		intPointsAleat = objParametreIA.tParametresIAObjetUtilisable[uidObjet].intValeurAleatoire;
                    		intDistanceMax = objParametreIA.tParametresIAObjetUtilisable[uidObjet].intMaxDistance;


                		    // Plus on possède d'objet, moins cette
                		    // case est importante
                		    intPointsCase = intPointsObjet - intPointsEnleverNb * nombreObjetsPossedes(uidObjet);
                		    
                		    // On va aussi diminuer l'influence sur les
                		    // cases autour de celle-ci
	                    	dblFacteurAdj = intPointsCase / intPointsObjet;
	                    	if (dblFacteurAdj <= 0.00)
	                    	{
	                    		dblFacteurAdj = ParametreIA.FACTEUR_AJUSTEMENT_MIN; 
	                    	}
	                    	
	                    	// Attribuer les points pour l'objet Livre
                		    attribuerImportanceCase(ptTemp, intPointsCase, intPointsAleat, 
                		        intDistanceMax, intPointsEnleverDistance, 
                		        objParametreIA.ttPointsRegionPiece, 
                		        dblDeplacementMoyen, dblFacteurAdj);
                		    

                    	}
                    }
                    
                    
                    // Pièces
                    else if (objttPlateauJeu[x][y] instanceof CaseCouleur && 
                        ((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase() instanceof Piece)
                    {

                        intPointsObjet = objParametreIA.objParametreIAPiece.intValeurPoints;
                        intPointsEnleverDistance = objParametreIA.objParametreIAPiece.intPointsEnleverDistance;
                        intPointsAleat = objParametreIA.objParametreIAPiece.intValeurAleatoire;
                        intDistanceMax = objParametreIA.objParametreIAPiece.intMaxDistance;
                        
                        //objParametreIAObjetPiece;
                        attribuerImportanceCase(ptTemp, intPointsObjet, intPointsAleat, 
                             intDistanceMax, intPointsEnleverDistance, 
                             objParametreIA.ttPointsRegionPiece, 
                             dblDeplacementMoyen, 1.0);
                        
                    }
                    
                    // Magasins
                    else if(objttPlateauJeu[x][y] instanceof CaseCouleur && 
                        ((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase() instanceof Magasin &&
                        bolConsidererMagasin == true &&
                        !lstMagasinsVisites.contains(((CaseCouleur)objttPlateauJeu[x][y]).obtenirObjetCase()))
                    {
                    	// Plus le joueur virtuel possède d'objets, moins il est
                    	// important d'aller visiter un magasin
                    	intPointsCase = objParametreIA.objParametreIAMagasin.intValeurPoints;
                    	
                    	// On enlève les points par objets possédés
                    	intPointsCase -= lstObjetsUtilisablesRamasses.size() * ParametreIA.PTS_ENLEVER_MAGASIN_NB_OBJETS;
                    	
                    	dblFacteurAdj = intPointsCase / objParametreIA.objParametreIAMagasin.intValeurPoints;
                    	if (dblFacteurAdj <= 0.00)
                    	{
                    		dblFacteurAdj = ParametreIA.FACTEUR_AJUSTEMENT_MIN; 
                    	}
                    	
                    	attribuerImportanceCase(ptTemp, intPointsCase, 
                    	    objParametreIA.objParametreIAMagasin.intValeurAleatoire, 
                    	    objParametreIA.objParametreIAMagasin.intMaxDistance, 
                    	    objParametreIA.objParametreIAMagasin.intPointsEnleverDistance, 
                    	    objParametreIA.ttPointsRegionPiece, 
                    	    dblDeplacementMoyen, dblFacteurAdj);
                    }
                    
                    // Mini-jeu
                    else if(objttPlateauJeu[x][y] instanceof CaseSpeciale)
                    {
                    	// Attribuer un pointage à la case de MiniJeu
                    	if (bolConsidererMiniJeu == true)
                    	{
                    		attribuerImportanceCase(ptTemp, 
                    		    objParametreIA.objParametreIAMinijeu.intValeurPoints, 
                    		    objParametreIA.objParametreIAMinijeu.intValeurAleatoire, 
                    		    objParametreIA.objParametreIAMinijeu.intMaxDistance, 
                    		    objParametreIA.objParametreIAMinijeu.intPointsEnleverDistance, 
                    		    objParametreIA.ttPointsRegionPiece, 
                    		    dblDeplacementMoyen, 1.0);
                    	}
                    	else
                    	{
                    		// On veut s'assurer que le joueur virtuel
                    		// ne tombe pas sur cette case
                    		matPoints[x][y] = ParametreIA.POINTS_IGNORER_CASE;
                    	}
                    }
                }

                
            }
        }
         
         // On va maintenant trouver les meilleurs déplacements
        for (int x = 0; x < intNbLignes; x++)
        {
            for (int y = 0; y < intNbColonnes; y++)
            {
                // Gestion de la liste des 5 plus grands
                // On ajoute la case qu'on est en train de parcourir dans la liste
                // des 5 plus grands pointage si elle est digne d'y être
                for (int i = 0; i < ParametreIA.NOMBRE_CHOIX_ALTERNATIF; i++)
                {
                    if (tPlusGrand[i] == null)
                    {
                        tPlusGrand[i] = new Point(x, y);
                        break;
                    }
                    else if (matPoints[x][y] > matPoints[tPlusGrand[i].x][tPlusGrand[i].y])
                    {
                        // Tout décaler vers la droite
                        for (int j = ParametreIA.NOMBRE_CHOIX_ALTERNATIF-1; j > i; j--)
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
        int intDifferenceMax = objParametreIA.tNombrePointsMaximalChoixFinal[intNiveauDifficulte];
        
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
        for (int i = 1; i < ParametreIA.NOMBRE_CHOIX_ALTERNATIF; i ++)
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
        intRaisonPositionFinale = ParametreIA.RAISON_AUCUNE;
        if (objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y] instanceof CaseCouleur)
        {
        	if (((CaseCouleur)objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y]).obtenirObjetCase() instanceof Piece)
        	{
        		intRaisonPositionFinale = ParametreIA.RAISON_PIECE;
        	}
        	
        	else if (((CaseCouleur)objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y]).obtenirObjetCase() instanceof Magasin)
        	{
        		intRaisonPositionFinale = ParametreIA.RAISON_MAGASIN;
        	}
        	
        	if (((CaseCouleur)objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y]).obtenirObjetCase() instanceof ObjetUtilisable)
        	{
        		ObjetUtilisable objObjet = (ObjetUtilisable)((CaseCouleur)objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y]).obtenirObjetCase();
                   
        		if (objObjet.estVisible())
        		{
        			intRaisonPositionFinale = ParametreIA.RAISON_OBJET;
        		}
        	}
        }
        else if (objttPlateauJeu[tPlusGrand[intDecision].x][tPlusGrand[intDecision].y] instanceof CaseSpeciale)
        {
        	intRaisonPositionFinale = ParametreIA.RAISON_MINIJEU;
        }
        
        // Retourner la position trouvée     
        objPositionTrouvee = new Point(tPlusGrand[intDecision].x, tPlusGrand[intDecision].y);
        return objPositionTrouvee;
	}

      
    /*
     * Cette fonction s'occupe de déplacer le joueur virtuel s'il a bien répondu
     * à la question, met à jour le plateau de jeu, envoie les événements aux autres joueurs
     * et modifie le pointage, l'argent et la position du joueur virtuel
     */
    private void deplacerJoueurVirtuelEtMajPlateau(Point objNouvellePosition)
    {
    	
    	String reponse = "";
    	
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour =
		    InformationPartie.verifierReponseEtMettreAJourPlateauJeu(reponse, objNouvellePosition, this);
    	
    	Case objCaseDestination = objTable.obtenirPlateauJeuCourant()[objNouvellePosition.x][objNouvellePosition.y];
        
        ObjetUtilisable objObjetRamasse = objRetour.obtenirObjetRamasse();
        
        if (ccDebug)
        {
        	System.out.println("Nouvelle position: "  + objPositionJoueur.x + "," + 
	            objPositionJoueur.y);
        }
        
        // Si le joueur virtuel a atteint le WinTheGame, on arrête la partie
        if(!this.obtenirTable().obtenirButDuJeu().equals("original") && objNouvellePosition.equals(this.obtenirTable().obtenirPositionWinTheGame())) this.obtenirTable().arreterPartie(this.obtenirNom());
        
        if (objCaseDestination instanceof CaseSpeciale)
        {
        	// Émulation du mini-jeu
        	
        	// Pour l'instant, il n'y a qu'un type de mini-jeu, donc
            // on ne lui fait jouer que balle-au-mur
        	int intTypeMiniJeu = ParametreIA.MINIJEU_BALLE_AU_MUR;
        	
        	// On détermine le temps que va passer le joueur à jouer
        	int intTempsJeu = determinerTempsJeuMiniJeu(intTypeMiniJeu);
        	
        	// On s'assure que ce temps ne dépasse pas le temps restant
        	if (intTempsJeu > (objTable.obtenirTempsRestant() - ParametreIA.TEMPS_SURETE_MINIJEU_FIN_PARTIE))
        	{
        		intTempsJeu = objTable.obtenirTempsRestant() - ParametreIA.TEMPS_SURETE_MINIJEU_FIN_PARTIE;
        	}
        	
        	if (intTempsJeu < 0)
        	{
        		intTempsJeu = 0;
        	}
        	
        	// On détermine le nombre de points que le joueur virtuel
        	// fera selon le temps pris pour jouer
        	int intPointsJeu = determinerPointsJeuMiniJeu(intTypeMiniJeu, intTempsJeu);

        	//---------------------------------------
        	if (ccDebug)
        	{
        		System.out.println("Début du mini jeu");
        	    System.out.println("Temps="+intTempsJeu);
        	    System.out.println("Points="+intPointsJeu);
        	}
        	//----------------------------------------
        	
        	// On fait une pause pour le laisser jouer
        	pause(intTempsJeu);
        	
        	// On incrémente les points du joueur virtuel
        	intPointage += intPointsJeu;
        	
			// Préparer un événement pour les autres joueurs de la table
			// pour qu'il se tienne à jour du pointage de ce joueur
			objTable.preparerEvenementMAJPointage(strNom, intPointage);
        	
        	// On incrémente le compteur de mini-jeu
        	intNbMiniJeuJoues++;
    	}
    	else if (objCaseDestination instanceof CaseCouleur &&
    	    ((CaseCouleur)objCaseDestination).obtenirObjetCase() instanceof Magasin)
    	{
   		
   		    // Temps de pause pour que le joueur virtuel
   		    // pense à ce qu'il achète
            int intTempsReflexion = 0;
            
            // Pour décision de l'objet
	    	int intPlusGrand = -9999999;
	    	int intIndicePlusGrand = -1;
	    		
            // Décision d'acheter quelque chose ou non
    		boolean bolDecision;
    		
    		// Aller chercher une référence vers le magasin
    		Magasin objMagasin = (Magasin)((CaseCouleur)objCaseDestination).obtenirObjetCase();
    		
            // Aller chercher une référence vers la liste des objets du magasin
            Vector lstObjetsMagasins = objMagasin.obtenirListeObjetsUtilisables();
            Vector lstCopieObjetsMagasins = new Vector();
            
            synchronized (lstObjetsMagasins)
            {
            	// Faire une copie de la liste des objets du magasin
    		    //(Vector)objMagasin.obtenirListeObjetsUtilisables().clone();
    		    
    		    // Copier tous les objets du magasin
    		    for (int i = 0; i < lstObjetsMagasins.size(); i++)
    		    {
    		    	// Aller chercher l'objet du magasin
    		    	ObjetUtilisable objObjet = (ObjetUtilisable)lstObjetsMagasins.get(i);
    		    	
    		    	// Créer une copie
    		    	ObjetUtilisable objCopieObjet = new ObjetUtilisable(objObjet.obtenirId(),
    		    	    objObjet.estVisible(), objObjet.obtenirUniqueId(),
    		    	    objObjet.obtenirPrix(), objObjet.obtenirPeutEtreArme(),
    		    	    objObjet.obtenirEstLimite(), objObjet.obtenirTypeObjet());
    		    	
	                // Ajouter la copie à notre liste
    		    	lstCopieObjetsMagasins.add(objCopieObjet);
    		    }
    		}
    		
    		/**********************************
    		 *
    		 * Maintenant, on fait les calculs sur la copie de la liste
    		 * d'objets. Il est possible qu'un ou que des joueurs
    		 * achètent les objets pendant ce temps. Si l'objet choisit 
    		 * n'y est plus après les calculs, le joueur
    		 * virtuel va passer son tour et n'achètera rien
    		 *
    		 **********************************/
    		
    		// Si le magasin ne possède aucun item, si le joueur
    		// virtuel a atteint sa limite d'objets ou si le magasin
    		// est dans la liste des magasins à ne pas visiter, alors le 
    		// temps de réflexion est de 0 et la décision est de ne 
    		// rien acheter
    		if (lstCopieObjetsMagasins.size() >= 0 && 
    		    lstObjetsUtilisablesRamasses.size() < intNbObjetsMax &&
    		    !lstMagasinsVisites.contains(objMagasin))
    		{
    			intTempsReflexion = obtenirTempsReflexionAchat();
	    		
	    		// Pour chaque objet de la liste, on va attribuer un pointage
	    		int tPointageObjets[] = new int[lstCopieObjetsMagasins.size()];
	    		
	    		for (int i = 0; i < lstCopieObjetsMagasins.size(); i ++)
	    		{
	    			// Aller chercher l'objet
	    			ObjetUtilisable objObjetAVendre = (ObjetUtilisable) lstCopieObjetsMagasins.get(i);
	    			
	    			// Si le joueur virtuel n'a pas assez d'argent pour acheter
	    			// l'objet, alors on donne un pointage très bas
	    			if (intArgent < objObjetAVendre.obtenirPrix())
	    			{
	    				tPointageObjets[i] = -9999999;
	    			}
	    			
	    			// Attribuer des points à l'objet selon le nombre
	    			// d'objets de ce type déjà en possession
	    			else
	    			{
	    				tPointageObjets[i] = -9999999;
                                        //TODO: régler ça
                                    /*tPointageObjets[i] = objParametreIA.tParametresIAObjetUtilisable[objObjetAVendre.obtenirUniqueId()].intValeurPoints - 
	    				    objParametreIA.tParametresIAObjetUtilisable[objObjetAVendre.obtenirUniqueId()].intPointsEnleverQuantite * 
	    				    nombreObjetsPossedes(objObjetAVendre.obtenirUniqueId());*/
	    			}

	    		}
	    		
	    		// Choisir l'objet
	    		for (int i = 0; i < tPointageObjets.length; i ++)
	    		{
	    			if (tPointageObjets[i] > intPlusGrand)
	    			{
	    				intPlusGrand = tPointageObjets[i];
	    				intIndicePlusGrand = i;
	    			}
	    		}
	    		
	    		if (intIndicePlusGrand >= 0 && intIndicePlusGrand < tPointageObjets.length)
	    		{
	    			bolDecision = true;
	    		}
	    		else
	    		{
	    			bolDecision = false;
	    		}

    		}
    		else
    		{
    			intTempsReflexion = 0;
    			bolDecision = false;
    		}
    		
    		// On incrément le compteur de magasin visités
    		intNbMagasinVisites++;
    		
    		// Ajouter ce magasin à la liste des magasins déjà visités
    		if (!lstMagasinsVisites.contains(objMagasin))
    		{
    			lstMagasinsVisites.add(objMagasin);
    		}
    		
        	//---------------------------------------
        	if (ccDebug)
        	{
        		System.out.println("***************** Magasin visite");
            }
        	//----------------------------------------

	    	pause(intTempsReflexion);

    		if (bolDecision)
    		{
    			// Aller chercher, dans la copie, l'indice de l'objet à acheter
                int intObjetId = ((ObjetUtilisable)lstCopieObjetsMagasins.get(intIndicePlusGrand)).obtenirId();
                
                // Permet de savoir si l'achat a eu lieu
                boolean bolAchatOk;
                
                // Va contenir l'objet 
                ObjetUtilisable objObjet = null;
                
                // Vérifier si l'objet existe encore
                synchronized(objMagasin)
                {
                	if (objMagasin.objetExiste(intObjetId))
                	{
		    			// Aller chercher l'objet choisit
		    			objObjet = (ObjetUtilisable)lstObjetsMagasins.get(intIndicePlusGrand);
		    			
		    			// Acheter l'objet
		    			objObjet = objMagasin.acheterObjet(objObjet.obtenirId(), objTable.obtenirProchainIdObjet());
		    			
		    			// On indique que l'achat a eu lieu puis on sort de la s.c.
		    			bolAchatOk = true;
                	}
                	else
                	{
                		bolAchatOk = false;
                	}
                }
                
                if (bolAchatOk)
                {
	    			// Ajouter l'objet dans la liste
	    			lstObjetsUtilisablesRamasses.put(new Integer(objObjet.obtenirId()), objObjet);
	    		
	    		    // Défrayer les coûts
	    		    intArgent -= objObjet.obtenirPrix();
	    		    
	    		    //---------------------------------------
	    		    if (ccDebug)
	    		    {
	    		    	System.out.println("***************** Objet acheté: " + objObjet.obtenirTypeObjet());
	    		        System.out.println("***************** Cout: " + objObjet.obtenirPrix());
	    		        System.out.println("***************** Prochain id: " + objTable.obtenirProchainIdObjet());
	    		        
	    		        System.out.print("***** Liste objets dans le magasin après achat:");
	    		        for (int i = 0; i < objMagasin.obtenirListeObjetsUtilisables().size(); i++)
	    		        {
	    		        	System.out.print(((ObjetUtilisable)objMagasin.obtenirListeObjetsUtilisables().get(i)).obtenirTypeObjet() + 
	    		                "(" + ((ObjetUtilisable)objMagasin.obtenirListeObjetsUtilisables().get(i)).obtenirId() + "),");
	    		        }
	    		        System.out.println("");
	    		    }
	    		    //---------------------------------------
	    		    
					// Préparer un événement pour les autres joueurs de la table
					// pour qu'il se tienne à jour de l'argent de ce joueur
					objTable.preparerEvenementMAJArgent(strNom, intArgent);
					
                }
                else
                {
                	if (ccDebug)
                	{
                		System.out.println("Objet envolé après réflexion (" + strNom + 
                		    ", " + objPositionJoueur.x + "-" + objPositionJoueur.y + 
                		    ", " + System.currentTimeMillis() + ")");
                	}
                }

    		}

    	}
    	
    	if (objObjetRamasse instanceof Livre)
    	{
    		//---------------------------------------
    		if (ccDebug)
    		{
    			System.out.println("Objet ramasse: Livre");
    		}
    		//---------------------------------------
    	}
    	
    	if (objRetour.obtenirCollision().equals("piece"))
    	{
    		//---------------------------------------
    		if (ccDebug)
    		{
    			System.out.println("Objet ramasse: Piece");
    		}
    		//---------------------------------------
    	}
 
    }
    
    /*
     * Cette fonction prépare l'événement indiquant que le joueur virtuel se déplace
     */
    /*private void preparerEvenementJoueurVirtuelDeplacePersonnage( String collision, Point objNouvellePosition, int nouveauPointage )
    {
        objTable.preparerEvenementJoueurDeplacePersonnage(strNom, collision, objPositionJoueur, 
            objNouvellePosition, nouveauPointage);
    }*/


    private int genererNbAleatoire(int max)
    {
        return objControleurJeu.genererNbAleatoire(max);
    }
    
    
    public void definirPointage(int valeur)
    {
        intPointage = valeur;
    }
    
    public void definirArgent(int valeur)
    {
        intArgent = valeur;
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
    		if (!ccFast)
    		{
    	        Thread.sleep(nbSecondes * 1000);
    	    }
    	    else
    	    {
    	    	Thread.sleep(500);
    	    }
    	    
    	}
    	catch(InterruptedException e)
    	{ 
    	}

    }
  
	
    /* 
     * Cette fonction retourne le pointage d'un déplacement
     *
     */
    public int obtenirPointage(Point ptFrom, Point ptTo)
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
    
    
    /* Cette fonction de service utilisée dans l'algorithme de recherche
     * de position permet d'éliminer une case du choix, si par exemple, 
     * on ne veut pas diriger le joueur virtuel sur un minijeu, on
     * enlève cette case du choix
     */
    private void traiterCaseEliminerDansLigne(int intPourcentageCase[], int indiceCase)
    {
    	// On va éliminer la case dans les pourcentages puis
    	// remettre le tout sur 100
        int intDenominateur = 0;
        
        for (int i = 0; i < ParametreIA.DEPLACEMENT_MAX; i++)
        {
        	if (i != indiceCase)
        	{
        		intDenominateur += intPourcentageCase[i];
        	}
        }
        
        // On élinie la case ici
        intPourcentageCase[indiceCase] = 0;
        
        // On repondère car le total n'est plus 100
        for (int i = 0; i < ParametreIA.DEPLACEMENT_MAX; i++)
        {
        	if (i != indiceCase)
        	{
        		intPourcentageCase[i] = intPourcentageCase[i] * 100 / intDenominateur;
        	}
        }
        
        // On s'assure que le total est bien de 100
        int intTotal = 0;
        for (int i = 0; i < ParametreIA.DEPLACEMENT_MAX; i++)
        {
        	intTotal += intPourcentageCase[i];
        }
        
        if (intTotal < 100)
        {
        	// Ajouter au prochaine indice != 0 ce qui manque
        	for (int j = 0; j < ParametreIA.DEPLACEMENT_MAX; j++)
        	{
        		if (intPourcentageCase[j] > 0)
        		{
        			intPourcentageCase[j] += 100 - intTotal;
        			break;
        		}
        	}
        }
        else if (intTotal > 100)
        {
        	// Enlever au prochaine indice != 0 ce qui a de trop
        	for (int j = 0; j < ParametreIA.DEPLACEMENT_MAX; j++)
        	{
        		if (intPourcentageCase[j] > 0)
        		{
        			intPourcentageCase[j] -= intTotal - 100;
        			break;
        		}
        	}	
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
    	if (indice + 1 <= ParametreIA.DEPLACEMENT_MAX - 1 && indice >= 0)
    	{
    	    for(x = indice + 1; x <= ParametreIA.DEPLACEMENT_MAX - 1; x++)
    	    {
    	    	tPourcentageCase[indice] += tPourcentageCase[x];
    	    	tPourcentageCase[x] = 0;
    	    }
    	
    	}
    }
    public GestionnaireEvenements obtenirGestionnaireEvenements()
    {
    	return objGestionnaireEv;
    }
    
    public Point obtenirPositionJoueur()
    {
    	return objPositionJoueur;
    }
    
    public TreeMap obtenirListeObjetsRamasses()
    {
    	return lstObjetsUtilisablesRamasses;
    }
    
    public Table obtenirTable()
    {
    	return objTable;
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
    
    /* Cette fonction permet d'obtenir l'argent du joueur virtuel
     */
    public int obtenirArgent()
    {
        return intArgent;
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
        
        int tTableauSource[][];
        
        // Déterminer dans quel tableau on va chercher les pourcentages
        // de choix. Si le joueur possède l'objet Livre,
        // il va choisir des choix plus difficile car l'objet va l'aider
        if (nombreObjetsPossedes(Objet.UID_OU_LIVRE) > 0)
        {
        	tTableauSource = objParametreIA.tPourcentageChoixObjetLivre;
        }
        else
        {
        	tTableauSource = objParametreIA.tPourcentageChoix;
        }
        
        int intPourcentageCase[] = new int[ParametreIA.DEPLACEMENT_MAX];
        for (int i = 0; i < ParametreIA.DEPLACEMENT_MAX; i++)
        {
        	intPourcentageCase[i] = tTableauSource[intNiveauDifficulte][i];
        }
        
        return intPourcentageCase;
 
    }
       
    /* Cette fonction permet d'obtenir le temps de réflexion d'un joueur
     * virtuel pour penser à son achat dans un magasin. Ce temps est basé 
     * sur le niveau de difficulté du joueur virtuel et comprend un élément 
     * aléatoire.
     */
    private int obtenirTempsReflexionAchat()
    {
        
        return objParametreIA.tTempsReflexionBase[ParametreIA.TYPE_REFLEXION_ACHAT][intNiveauDifficulte] + 
            genererNbAleatoire(objParametreIA.tTempsReflexionAleatoire[ParametreIA.TYPE_REFLEXION_ACHAT][intNiveauDifficulte]);

    }
    
    /* Cette fonction permet d'obtenir le temps de réflexion d'un joueur
     * virtuel pour planifier son prochain coup. Ce temps est basé sur le niveau
     * de difficulté du joueur virtuel et comprend un élément aléatoire.
     */
    private int obtenirTempsReflexionCoup()
    {
        
        return objParametreIA.tTempsReflexionBase[ParametreIA.TYPE_REFLEXION_COUP][intNiveauDifficulte] + 
            genererNbAleatoire(objParametreIA.tTempsReflexionAleatoire[ParametreIA.TYPE_REFLEXION_COUP][intNiveauDifficulte]);

    }
    
    
    /* Cette fonction permet d'obtenir le temps de réflexion d'un joueur
     * virtuel lorsqu'il répond à une question. Ce temps est basé sur le niveau
     * de difficulté du joueur virtuel et comprend un élément aléatoire.
     */
    private int obtenirTempsReflexionReponse()
    {
        
        return objParametreIA.tTempsReflexionBase[ParametreIA.TYPE_REFLEXION_REPONSE][intNiveauDifficulte] + 
            genererNbAleatoire(objParametreIA.tTempsReflexionAleatoire[ParametreIA.TYPE_REFLEXION_REPONSE][intNiveauDifficulte]);
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
     
    /* Cette fonction retourne un tableau contenant les pourcentages pour les 
     * choix alternatifs de positions finales selon le niveau de difficulté
     */
    private int[] obtenirPourcentageChoixAlternatifFinal()
    {
		int intPourcentageChoix[] = new int[ParametreIA.NOMBRE_CHOIX_ALTERNATIF];
	    	
	    for (int i = 0; i < ParametreIA.NOMBRE_CHOIX_ALTERNATIF; i++)
	    {
	    	intPourcentageChoix[i] = objParametreIA.tPourcentageChoixAlternatifFinal[intNiveauDifficulte][i];
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
    private boolean reviserPositionFinaleVisee()
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
        	case ParametreIA.RAISON_AUCUNE: 
        	
	        	// Aucune raison = erreur en général, donc on va recalculer
	            // une position finale
	            return true;

            
        	case ParametreIA.RAISON_PIECE:
        	
	        	// Vérifier si la pièce a été capturée
	    	    if (((CaseCouleur)objttPlateauJeu[objPositionFinaleVisee.x][objPositionFinaleVisee.y]).obtenirObjetCase() == null)
	    	    {
	    	    	return true;
	    	    }
	    	    else
	    	    {
	    	    	return false;
	    	    }
	    	
	    	case ParametreIA.RAISON_MINIJEU:
	    	
	    	    // Vérifier si encore prêt pour un minijeu
	    	    return !determinerPretAJouerMiniJeu();
	    	    
	    	case ParametreIA.RAISON_MAGASIN:
	    	    
	    	    // Vérifier si encore prêt à visiter un magasin
	    	    return !determinerPretAVisiterMagasin();
	    	    
	    	case ParametreIA.RAISON_OBJET:
	    	
	    	    // Vérifier si l'objet a été capturé et si encore prêt
	    	    // à ramasser l'objet
	    	    if (((CaseCouleur)objttPlateauJeu[objPositionFinaleVisee.x][objPositionFinaleVisee.y]).obtenirObjetCase() == null ||
	    	        determinerPretARamasserObjet(((ObjetUtilisable)((CaseCouleur)objttPlateauJeu[objPositionFinaleVisee.x][objPositionFinaleVisee.y]).obtenirObjetCase()).obtenirUniqueId()) == false)
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
 
    /* Cette fonction détermine le nombre de points que fera un 
     * joueur virtuel en jouant à un mini-jeu dépendamment du temps qu'il
     * y met
     */
    private int determinerPointsJeuMiniJeu(int intTypeMiniJeu, int intTempsMiniJeu)
    {
    	// Le temps pour les calculs
    	double dblTempsCalcul = 0.0;
    	
    	// Le pointage obtenu
    	int intPointsJeu = 0;
    	
    	// Un petit délai au début de la partie
    	double dblDelaiDepart = 0.0;
    	
    	// Un délai additionnel, pour ball-au-mur, correspond au temps du
    	// dernier coup qui lui, ne donne pas de points
    	double dblDelaiAdditionnel = 0.0;
    	
        switch(intTypeMiniJeu)
        {
        	case ParametreIA.MINIJEU_BALLE_AU_MUR:
        	    
        	    // Délai de départ de 0 à 2 secondes
        	    dblDelaiDepart = objControleurJeu.genererNbAleatoire(3);
        	    
                // Trouver le temps de jeu pour le calcul
                dblTempsCalcul = (double)intTempsMiniJeu - dblDelaiDepart;

                // Trouver le temps additionnel
        	    if (dblTempsCalcul > 125.0)
        	    {
        	    	dblDelaiAdditionnel = 0.5;
        	    }
        	    else if (dblTempsCalcul > 68.0)
        	    {
        	    	dblDelaiAdditionnel = 1.0;
        	    }
        	    else if (dblTempsCalcul > 50.0)
        	    {
        	    	dblDelaiAdditionnel = 2.0;
        	    }
        	    else if (dblTempsCalcul > 32)
        	    {
        	    	dblDelaiAdditionnel = 3.0;
        	    }
        	    else if (dblTempsCalcul > 20)
        	    {
        	    	dblDelaiAdditionnel = 4.0;
        	    }
        	    else
        	    {
        	    	dblDelaiAdditionnel = 5.0;
        	    }
        	    
        	    // Modifier le temps de calcul selon le temps additionnel
        	    dblTempsCalcul -= dblDelaiAdditionnel; 
                
                if (dblTempsCalcul < 0.0)
                {
                	dblTempsCalcul = 0.0;
                }   	    
                
        	    // Dépendamment du temps pris à jouer, trouver le
        	    // nombre de points que fera le joueur
        	    if (dblTempsCalcul >= 125.5)
        	    {
        	    	intPointsJeu = (int)(79 + (dblTempsCalcul - 125) * 2);
        	    }
        	    else if (dblTempsCalcul >= 69.0)
        	    {
        	    	intPointsJeu = (int)(22 + (dblTempsCalcul - 68));
        	    }
        	    else if (dblTempsCalcul >= 52.0)
        	    {
        	    	intPointsJeu = (int)(13 + (dblTempsCalcul - 50) * 0.5);
        	    }
        	    else if (dblTempsCalcul >= 35)
        	    {
        	    	intPointsJeu = (int)(7 + (dblTempsCalcul - 32) / 3);
        	    }
        	    else if (dblTempsCalcul >= 24)
        	    {
        	    	intPointsJeu = (int)(4 + (dblTempsCalcul - 20) * 0.25);
        	    }
        	    else
        	    {
        	    	intPointsJeu = (int)(dblTempsCalcul * 0.2);
        	    }

				break;
		}
		
		if (intPointsJeu < 0)
		{
			intPointsJeu = 0;
		}
		
    	return intPointsJeu;
    }
    
    
    /* Cette fonction détermine le temps que jouera le joueur virtuel
     * au mini-jeu. Ce temps permettra de connaître le nombre de points
     * qu'il fera pendant le jeu.
     */
    private int determinerTempsJeuMiniJeu(int intTypeMiniJeu)
    {
    	// Un temps minimal que l'on donne au joueur pour ce jeu
    	int intMinimum = 0;
    	
    	// Un temps maximal que l'on donne au joueur pour ce jeu
    	int intMaximum = 0;
    	
    	// La valeur moyenne de la loi normale
    	double dblMoyenne = 0.0;
    	
    	// La variance de la loi normale
    	double dblVariance = 0.0;
    	
    	// La valeur à retourner
    	int intTemps;
    	       
    	switch(intTypeMiniJeu)
        {
        	case ParametreIA.MINIJEU_BALLE_AU_MUR:
        	
				switch (intNiveauDifficulte)
				{
				    case ParametreIA.DIFFICULTE_FACILE:
				        intMinimum = 24;
				        intMaximum = 86;
				        dblMoyenne = 47.00;
				        dblVariance = 100.00;
				        break;
				        
				    case ParametreIA.DIFFICULTE_MOYEN:
				        intMinimum = 71;
				        intMaximum = 135;
				        dblMoyenne = 31.00;
				        dblVariance = 225.00;
				        break;
				        
				    case ParametreIA.DIFFICULTE_DIFFICILE: 
				        intMinimum = 91;
				        intMaximum = 190;
				        dblMoyenne = 39.50;
				        dblVariance = 225.00;
				        break;
				}
				break;
		}
		
		
		// Maintenant, faire le calcul
		intTemps = intMinimum + UtilitaireNombres.genererNbAleatoireLoiNormale(dblMoyenne, dblVariance);
		
		if (intTemps > intMaximum)
		{
			intTemps = intMaximum;
		}
		else if (intTemps < intMinimum)
		{
			intTemps = intMinimum;
		}
		
		return intTemps;
    	
    }
    

    // Pour tous les minijeus
    private int obtenirTempsSureteMiniJeu()
    {
    	// Temps à la fin de la partie où le joueur ne doit pas
    	// débuter un mini-jeu
    	int intTempsSurete = 0;
    	
		switch (intNiveauDifficulte)
		{
		    case ParametreIA.DIFFICULTE_FACILE:
    	        intTempsSurete = 80;
		        break;
		        
		    case ParametreIA.DIFFICULTE_MOYEN:
    	        intTempsSurete = 150;
		        break;
		        
		    case ParametreIA.DIFFICULTE_DIFFICILE:
    	        intTempsSurete = 240;
		        break;
		}	
		
		return intTempsSurete;
    }
    
    /* Cette fonction va remplir le tableau tJetonsMagasins[] qui
     * permettra aux joueurs virtuels de se servir des magasins, mais
     * en limitant le nombre de fois qu'il s'en servira au cours
     * de la partie
     */
    private void determinerJetonsMagasins()
    {
    	// Nombre de visite à l'heure
    	int intNombreMagasins = 0;
    	
    	// Maintenant, on trouve le nombre de magasins que le joueur
    	// virtuel visitera dans les prochaines 60 minutes
		intNombreMagasins = objParametreIA.tNbJetonsMagasinBase[intNiveauDifficulte] + 
		    objControleurJeu.genererNbAleatoire(objParametreIA.tNbJetonsMagasinAleatoire[intNiveauDifficulte]);

        // Obtenir le temps de la partie en minutes
        int intTempsPartie = objTable.obtenirTempsTotal();
        
        // Obtenir un temps pour les calculs
        int intTempsCalcul = (((int)(intTempsPartie / 61)) + 1) * 60;

        // Obtenir le nombre de jetons à générer
        int intNombreJetons = intTempsCalcul * intNombreMagasins / 60;
        
        // Initialiser le tableau pour contenir les jetons
        tJetonsMagasins = new int[intNombreJetons];
		
        // Generer tous les jetons
        for (int i = 0; i < intNombreJetons; i++)
        {
        	tJetonsMagasins[i] = objControleurJeu.genererNbAleatoire(intTempsCalcul * 60);
        }
    	
        int intTempsSurete = ParametreIA.TEMPS_SURETE_MAGASIN;
    	int intTempsMax = intTempsPartie * 60 - intTempsSurete;
    	
    	// Déplacer en arrière les jetons dans les dernières X secondes
    	for (int i = 0; i < intNombreJetons; i++)
    	{
    		if (tJetonsMagasins[i] >= intTempsMax && tJetonsMagasins[i] < intTempsPartie * 60)
    		{
    			tJetonsMagasins[i] -= intTempsSurete * 2;
    		}
    	}

        if (ccDebug)
	    {  
	        System.out.print("Jetons magasins: ");
	    	for (int i = 0; i < intNombreJetons ; i++)
	    	{
	    		if (i > 0)
	    		{
	    			System.out.print(", ");
	    		}
	    		System.out.print(tJetonsMagasins[i]);
	    	}
	    	System.out.println("");
	    }
    }
    
    /* Cette fonction va remplir le tableau tJetonsMiniJeu[] qui
     * permettra aux joueurs virtuels de jouer à des mini-jeux, mais
     * en limitant le nombre de fois qu'il jouera au cours de la partie
     */    
    private void determinerJetonsMiniJeu()
    {
    	
    	// Nombre de mini-jeu à l'heure
    	int intNombreMiniJeu = 0;
    	
    	// Maintenant, on trouve le nombre de partie de minijeu que le
    	// joueur virtuel fera pour les prochaines 60 minutes	
		intNombreMiniJeu = objParametreIA.tNbJetonsMinijeuBase[intNiveauDifficulte] + 
		    objControleurJeu.genererNbAleatoire(objParametreIA.tNbJetonsMinijeuAleatoire[intNiveauDifficulte]);

        // Obtenir le temps de la partie en minutes
        int intTempsPartie = objTable.obtenirTempsTotal();
        
        // Obtenir un temps pour les calculs
        int intTempsCalcul = (((int)(intTempsPartie / 61)) + 1) * 60;

        // Obtenir le nombre de jetons à générer
        int intNombreJetons = intTempsCalcul * intNombreMiniJeu / 60;
        
        // Initialiser le tableau pour contenir les jetons
        tJetonsMiniJeu = new int[intNombreJetons];
        
        // Generer tous les jetons
        for (int i = 0; i < intNombreJetons; i++)
        {
        	tJetonsMiniJeu[i] = objControleurJeu.genererNbAleatoire(intTempsCalcul * 60);
        }
    	
        int intTempsSurete = obtenirTempsSureteMiniJeu();
    	int intTempsMax = intTempsPartie * 60 - intTempsSurete;
    	
    	// Déplacer en arrière les jetons dans les dernières X secondes
    	for (int i = 0; i < intNombreJetons; i++)
    	{
    		if (tJetonsMiniJeu[i] >= intTempsMax && tJetonsMiniJeu[i] < intTempsPartie * 60)
    		{
    			tJetonsMiniJeu[i] -= intTempsSurete * 2;
    		}
    	}

        if (ccDebug)
        {
        	System.out.print("Jetons minijeu: ");
	    	for (int i = 0; i < intNombreJetons ; i++)
	    	{
	    		if (i > 0)
	    		{
	    			System.out.print(", ");
	    		}
	    		System.out.print(tJetonsMiniJeu[i]);
	    	}
	    	System.out.println("");
    	}
    }
    
    /* Cette fonction retourne le nombre de jetons disponibles
     * d'un tableau de jetons en fonction du temps écoulé
     */ 
    private int obtenirNombreJetonsDisponibles(int tTableauJetons[], int intTempsEcoule)
    {
    	int intNombreJetonsDisponibles = 0;
    	for (int i = 0; i < tTableauJetons.length; i++)
    	{
    		if (tTableauJetons[i] <= intTempsEcoule)
    		{
    			intNombreJetonsDisponibles++;
    		}
    	}
    	
    	return intNombreJetonsDisponibles;	
    }
    
    /* Cette fonction permet à l'algorithme de recherche de position
     * de déterminer s'il faut considérer les cases avec des
     * objets comme des cases importantes, puisque leur utilisation
     * risque d'être limité dans le temps, alors en fin de partie, on
     * essaye de ne pas ramasser d'objets.
     */
    private boolean determinerPretARamasserObjet(int uidObjet)
    {
    	// Vérifier s'il reste assez de temps et que le joueur a de la place
        if (uidObjet>0) return false; //TODO: régler ça
    	if (lstObjetsUtilisablesRamasses.size() >= intNbObjetsMax || 
    		objTable.obtenirTempsRestant() <= objParametreIA.tParametresIAObjetUtilisable[uidObjet].intTempsSureteRamasser ||
    	    nombreObjetsPossedes(uidObjet) >= objParametreIA.tParametresIAObjetUtilisable[uidObjet].intQuantiteMax)
    	{
    		return false;
    	}	
    	else
    	{
    		return true;
    	}
    }
    
    /* Cette fonction permet à l'algorithme de recherche de position
     * finale de déterminer s'il doit considérer les cases magasins
     * comme des cases importantes. On prend en compte le tableau
     * tJetonsMagasins[], intNbMagasinVisites, le temps de la partie
     * ainsi que les items que le joueur virtuel possède
     */
    private boolean determinerPretAVisiterMagasin()
    {
      	
   	    int intNombreJetonsDisponibles = 0;
    	int intTempsEcoule = objTable.obtenirTempsTotal() * 60 - objTable.obtenirTempsRestant();
    	
    	// Vérifier d'abord s'il reste assez de temps
    	if (objTable.obtenirTempsRestant() <= ParametreIA.TEMPS_SURETE_MAGASIN * 2)
    	{
    		return false;
    	}
    	
    	// Si le joueur a atteint son quotas d'objets, alors
    	// il ne visite plus de magasins
    	if (lstObjetsUtilisablesRamasses.size() >= intNbObjetsMax)
    	{
    		return false;
    	}
    	
    	// Il faut au moins 1 dollar pour acheter ne serait-ce qu'un objet
    	if (intArgent < 1)
    	{
    		return false;
    	}
    	
    	intNombreJetonsDisponibles = obtenirNombreJetonsDisponibles(tJetonsMagasins, intTempsEcoule);
    	
    	// Enlever les magasins déjà jouées
    	intNombreJetonsDisponibles -= intNbMagasinVisites;
    	
    	// S'il y a des jetons disponibles, on permet au joueur
    	// virtuel de se déplacer vers un magasin
    	if (intNombreJetonsDisponibles > 0)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    /* Cette fonction permet à l'algorithme de recherche de position
     * finale de déterminer s'il doit considérer les cases minis-jeu
     * comme des cases importantes. Pour ce faire, il faut prendre en
     * considération le tableau tJetonsMiniJeu[], le nombre de minis-jeu
     * déjà joués par le joueur virtuel dans cette partie et aussi le
     * temps de la partie.
     *
     * Tout ça a pour but de limiter le nombre de mini-jeu que le
     * joueur virtuel va jouer, faire en sorte que ce soit différent
     * à chaque partie et aussi espacer les parties pour ne pas que le 
     * joueur virtuel "bloque" lorsqu'il arrive sur une case mini-jeu,
     * fait un va-et-vient et joue sans cesse sur cette case.
     */
    private boolean determinerPretAJouerMiniJeu()
    {
    	
    	int intNombreJetonsDisponibles = 0;
    	int intTempsEcoule = objTable.obtenirTempsTotal() * 60 - objTable.obtenirTempsRestant();
    	
    	// Vérifier d'abord s'il reste assez de temps
    	if (objTable.obtenirTempsRestant() <= obtenirTempsSureteMiniJeu() * 2)
    	{
    		return false;
    	}
    	
    	intNombreJetonsDisponibles = obtenirNombreJetonsDisponibles(tJetonsMiniJeu, intTempsEcoule);
    	
    	// Enlever les parties déjà jouées
    	intNombreJetonsDisponibles -= intNbMiniJeuJoues;
    	
    	// S'il y a des jetons disponibles, on permet au joueur
    	// virtuel de jouer sur une case mini-jeu
    	if (intNombreJetonsDisponibles > 0)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}

    	
    }
    

    private void enleverObjet(int uidObjet)
    {
        Set lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
        Iterator objIterateurListeObjets = lstEnsembleObjets.iterator();
        int i = 0;
        	        
        while (objIterateurListeObjets.hasNext())
        {
        	ObjetUtilisable objObjet = (ObjetUtilisable)(((Map.Entry)(objIterateurListeObjets.next())).getValue());
        	
        	if (objObjet.obtenirUniqueId() == uidObjet)
        	{
        		lstObjetsUtilisablesRamasses.remove(objObjet.obtenirId());
        		break;
        	}
        	
        	i++;
        }
    }
    
    
    /* Cette fonction calcule le nombre d'objets que le joueur
     * virtuel possède du type uidObjet
     */
    private int nombreObjetsPossedes(int uidObjet)
    {
    	int intNbObjets = 0;
    	
        if (lstObjetsUtilisablesRamasses.size() > 0)
        {

	        Set lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
	        Iterator objIterateurListeObjets = lstEnsembleObjets.iterator();
	        	        
	        while (objIterateurListeObjets.hasNext())
	        {
	        	ObjetUtilisable objObjet = (ObjetUtilisable)(((Map.Entry)(objIterateurListeObjets.next())).getValue());
	        	
	        	if (objObjet.obtenirUniqueId() == uidObjet)
	        	{
	        		intNbObjets++;
	        	}
	        }

        }
        
        return intNbObjets;
    }
    /*
     * Cette méthode statique permet de valider un niveau de joueur virtuel en format String
     * 
     * @param String s : le Niveau des joueurs virtuel en chaîne de caractères (Facile, Intermediaire,..)
     * @return boolean : true : si le paramètre est valide
     * 					 false : si le paramètre n'est pas valide
     */
    public static boolean validerParamNiveau(String s)
    {
    	return (s.equals("Aucun") || s.equals("Facile") || s.equals("Intermediaire") ||
    			s.equals("Difficile") || s.equals("TresDifficile"));
    }
    
    // it is not absolutely correct because PositionWinTheGame is a array, but it is not so important 
    public int obtenirDistanceAuWinTheGame()
    {
        return Math.abs(objPositionJoueur.x - objTable.obtenirPositionWinTheGame()[0].x) + Math.abs(objPositionJoueur.y - objTable.obtenirPositionWinTheGame()[0].y);
    }
}

