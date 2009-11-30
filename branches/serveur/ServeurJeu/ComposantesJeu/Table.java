package ServeurJeu.ComposantesJeu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.awt.Point;
import java.util.Date;
import java.util.Map.Entry;

import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Evenements.EvenementJoueurDeplacePersonnage;
import ServeurJeu.Evenements.EvenementJoueurEntreTable;
import ServeurJeu.Evenements.EvenementJoueurQuitteTable;
import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementJoueurRejoindrePartie;
import ServeurJeu.Evenements.EvenementPartieDemarree;
import ServeurJeu.Evenements.EvenementMAJPointage;
import ServeurJeu.Evenements.EvenementMAJArgent;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ClassesUtilitaires.GenerateurPartie;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Temps.*;
import ServeurJeu.Evenements.EvenementSynchroniserTemps;
import ServeurJeu.Evenements.EvenementPartieTerminee;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.ParametreIA;
import ServeurJeu.Evenements.EvenementMessageChat;
import ServeurJeu.Evenements.EvenementUtiliserObjet;

/**
 * @author Jean-François Brind'Amour
 */
public class Table implements ObservateurSynchroniser, ObservateurMinuterie
{
	// Déclaration d'une référence vers le gestionnaire d'événements
	private GestionnaireEvenements objGestionnaireEvenements;
	
    // On déclare la classe qui permettra les déplacements du WinTheGame
    //private WinTheGame winTheGame;
	
	// points for Finish WinTheGame
	private ArrayList<Point> lstPointsFinish = new ArrayList<Point>();
        
	// Déclaration d'une référence vers le contr™leur de jeu
	private ControleurJeu objControleurJeu;
	
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
	// Déclaration d'une référence vers la salle parente dans laquelle se 
	// trouve cette table 
	private Salle objSalle;
	
	// Cette variable va contenir le numéro de la table
	private final int intNoTable;
	
	// Déclaration d'une constante qui définit le nombre maximal de joueurs 
	// dans une table
	private final int MAX_NB_PLAYERS;
	
	private int intNbJoueurDemande; 
	
	// Cette variable va contenir le nom d'utilisateur du créateur de cette table
	private String strNomUtilisateurCreateur;
	
	// Déclaration d'une variable qui va garder le temps total défini pour 
	// cette table
	private final int intTempsTotal;
	
	// Cet objet est une liste des joueurs qui sont présentement sur cette table
	private TreeMap<String, JoueurHumain> lstJoueurs;
	
	// Cet objet est une liste des joueurs qui attendent de joueur une partie
	private TreeMap<String, JoueurHumain> lstJoueursEnAttente;
	
	// Déclaration d'une variable qui va permettre de savoir si la partie est 
	// commencée ou non
	private boolean bolEstCommencee;
	   
	// Déclaration d'une variable qui va permettre d'arrçter la partie en laissant
	// l'état de la partie ˆ "commencée" tant que les joueurs sont ˆ l'écran des pointages
	private boolean bolEstArretee;
	
	// Déclaration d'un tableau ˆ 2 dimensions qui va contenir les informations 
	// sur les cases du jeu
	private Case[][] objttPlateauJeu;
	
	// This object determine the rules of the game for this board
	private Regles objRegles;
	
	private GestionnaireTemps objGestionnaireTemps;
	private TacheSynchroniser objTacheSynchroniser;
	private Minuterie objMinuterie;
        
    // Position where is placed WinTheGame
    private Point positionWinTheGame;
        
    // Cet objet est une liste des joueurs virtuels qui jouent sur cette table
    private Vector<JoueurVirtuel> lstJoueursVirtuels;
    
    // Cette variable indique le nombre de joueurs virtuels sur la table
    private int intNombreJoueursVirtuels;
	
    // Cette liste contient le nom des joueurs qui ont été déconnectés
    // dans cette table, ce qui nous permettra, lorsqu'une partie se termine, de
    // faire la mise ˆ jour de la liste des joueurs déconnectés du gestionnaire
    // de communication
    private Vector<String> lstJoueursDeconnectes;
      
    private Date objDateDebutPartie;
    
    // Déclaration d'une variable qui permettra de créer des id pour les objets
    // On va initialisé cette variable lorsque le plateau de jeu sera créé
    private Integer objProchainIdObjet;
    
    // Name of the table
    private String tableName;
    
    // Array of players pictures 
    private ArrayList<Integer> pictures; 
    
    
	

	/**
	 * Constructeur de la classe Table qui permet d'initialiser les membres 
	 * privés de la table.
	 *
	 * @param Salle salleParente : La salle dans laquelle se trouve cette table
	 * @param GestionnaireBD gestionnaireBD : Le gestionnaire de base de données
	 * @param int noTable : Le numéro de la table
	 * @param String nomUtilisateurCreateur : Le nom d'utilisateur du créateur
	 * 										  de la table
	 * @param int tempsPartie : Le temps de la partie en minute
	 * @param Regles reglesTable : Les règles pour une partie sur cette table
	 */
	public Table(Salle salleParente, int noTable, JoueurHumain joueur ,	int tempsPartie, ControleurJeu controleurJeu, String name) 
	{
		super();
   	
		MAX_NB_PLAYERS = salleParente.getRegles().getMaxNbPlayers();
		
		positionWinTheGame = new Point(-1, -1); 		
        //this.butDuJeu = butDuJeu;
        
		// Faire la référence vers le gestionnaire d'événements et le 
		// gestionnaire de base de données
		objGestionnaireEvenements = new GestionnaireEvenements();
		objGestionnaireBD = controleurJeu.obtenirGestionnaireBD();
		
		// Garder en mémoire la référence vers la salle parente, le numéro de 
		// la table, le nom d'utilisateur du créateur de la table et le temps
		// total d'une partie
		setObjSalle(salleParente);
		intNoTable = noTable;
		
		setTableName(name);
		
		intTempsTotal = tempsPartie;
              //  if(!this.butDuJeu.equals("original")) winTheGame = new WinTheGame(this);
               
		// Créer une nouvelle liste de joueurs
		lstJoueurs = new TreeMap<String, JoueurHumain>();
		lstJoueursEnAttente = new TreeMap<String, JoueurHumain>();
		//String nomUtilisateurCreateur = joueur.obtenirNomUtilisateur();
		//lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);
		strNomUtilisateurCreateur = joueur.obtenirNomUtilisateur();//nomUtilisateurCreateur;
		// Au départ, aucune partie ne se joue sur la table
		bolEstCommencee = false;
		bolEstArretee = true;
		intNbJoueurDemande = MAX_NB_PLAYERS;
		
		// Définir les rçgles de jeu pour la salle courante
		objRegles = salleParente.getRegles();
		
		// take new table dimentions if changed in DB
		objGestionnaireBD.getNewTableDimentions(objRegles, salleParente.getRoomName(""));
		//objRegles.definirTempsMaximal(objGestionnaireBD.getNewTableDimentionTours(salleParente.getRoomName("")));
		//objRegles.definirTempsMinimal(objGestionnaireBD.getNewTableDimentionLines(salleParente.getRoomName("")));
		
		// initialaise gameboard - set null
		objttPlateauJeu = null;
		
		objGestionnaireTemps = controleurJeu.obtenirGestionnaireTemps();
		objTacheSynchroniser = controleurJeu.obtenirTacheSynchroniser();

        // Au départ, on considçre qu'il n'y a que des joueurs humains.
        // Lorsque l'on démarrera une partie dans laPartieCommence(), on créera
        // autant de joueurs virtuels que intNombreJoueursVirtuels (qui devra donc
        // çtre affecté du bon nombre au préalable)
        intNombreJoueursVirtuels = 0;
        lstJoueursVirtuels = null;
        
        // Cette liste sera modifié si jamais un joueur est déconnecté
        lstJoueursDeconnectes = new Vector<String>();
        pictures = new ArrayList<Integer>();
        
        
        // Faire la référence vers le controleu jeu
        objControleurJeu = controleurJeu;
        
        // Créer un thread pour le GestionnaireEvenements
		Thread threadEvenements = new Thread(objGestionnaireEvenements);
		
		// Démarrer le thread du gestionnaire d'événements
		threadEvenements.start();

	}
	
	public void creation()
	{

	}
	
	public void destruction()
	{
		arreterPartie("");
                
             /*   // On doit aussi arrçter le thread du WinTheGame si nécessaire
                if(winTheGame.thread.isAlive())
                {
                    winTheGame.arreter();
                }*/
	}
	
	/**
	 * Cette fonction permet au joueur d'entrer dans la table courante. 
	 * On suppose que le joueur n'est pas dans une autre table, que la table 
	 * courante n'est pas complçte et qu'il n'y a pas de parties en cours. 
	 * Cette fonction va avoir pour effet de connecter le joueur dans la table 
	 * courante.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la table
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @param TreeMap listePersonnageJoueurs : La liste des joueurs dont la clé 
	 * 								est le nom d'utilisateur du joueur et le contenu 
	 * 								est le Id du personnage choisi
	 * @throws NullPointerException : Si la liste listePersonnageJoueurs est nulle
	 * 
	 * Synchronisme : Cette fonction est synchronisée pour éviter que deux 
	 * 				  joueurs puissent entrer ou quitter la table en mçme temps.
	 * 				  On n'a pas ˆ s'inquiéter que le joueur soit modifié
	 * 				  pendant le temps qu'on exécute cette fonction. De plus
	 * 				  on n'a pas ˆ revérifier que la table existe bien (car
	 * 				  elle ne peut çtre supprimée en mçme temps qu'un joueur 
	 * 				  entre dans la table), qu'elle n'est pas complçte ou 
	 * 				  qu'une partie est en cours (car toutes les fonctions 
	 * 				  permettant de changer ça sont synchronisées).
	 */
	public void entrerTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, TreeMap<String, Integer> listePersonnageJoueurs, TreeMap<String, Integer> listeRoleJoueurs)  throws NullPointerException
	{
		//System.out.println("start table: " + System.currentTimeMillis());
	    // Empçcher d'autres thread de toucher ˆ la liste des joueurs de 
	    // cette table pendant l'ajout du nouveau joueur dans cette table
	    synchronized (lstJoueurs)
	    {
	    	// Remplir la liste des personnages choisis
	    	remplirListePersonnageJoueurs(listePersonnageJoueurs, listeRoleJoueurs);
	    	
			// Ajouter ce nouveau joueur dans la liste des joueurs de cette table
			lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
			
			// Le joueur est maintenant entré dans la table courante (il faut
			// créer un objet InformationPartie qui va pointer sur la table
			// courante)
			joueur.definirPartieCourante(new InformationPartie(objGestionnaireEvenements, objGestionnaireBD, joueur, this));
			
			
			// Si on doit générer le numéro de commande de retour, alors
			// on le génçre, sinon on ne fait rien
			if (doitGenererNoCommandeRetour == true)
			{
				// Générer un nouveau numéro de commande qui sera 
			    // retourné au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

			// Empçcher d'autres thread de toucher ˆ la liste des joueurs de 
		    // cette salle pendant qu'on parcourt tous les joueurs de la salle
			// pour leur envoyer un événement
		    synchronized (getObjSalle().obtenirListeJoueurs())
		    {
				// Préparer l'événement de nouveau joueur dans la table. 
				// Cette fonction va passer les joueurs et créer un 
				// InformationDestination pour chacun et ajouter l'événement 
				// dans la file de gestion d'événements
				preparerEvenementJoueurEntreTable(joueur.obtenirNomUtilisateur(), joueur.getRole());		    	
		    }
	    }
	    //System.out.println("end table : " + System.currentTimeMillis());
	}// end methode

	/**
	 * Cette méthode permet au joueur passé en paramçtres de quitter la table. 
	 * On suppose que le joueur est dans la table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de quitter la table
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * 
	 * Synchronisme : Cette fonction est synchronisée sur la liste des tables
	 * 				  puis sur la liste des joueurs de cette table, car il se
	 * 				  peut qu'on doive détruire la table si c'est le dernier
	 * 				  joueur et qu'on va modifier la liste des joueurs de cette
	 * 				  table, car le joueur quitte la table. Cela évite que des
	 * 				  joueurs entrent ou quittent une table en mçme temps.
	 * 				  On n'a pas ˆ s'inquiéter que le joueur soit modifié
	 * 				  pendant le temps qu'on exécute cette fonction. Si on 
	 * 				  inverserait les synchronisations, ça pourrait créer un 
	 * 				  deadlock avec les personnes entrant dans la salle.
	 */
	public void quitterTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean detruirePartieCourante)
	{
	    // Empçcher d'autres thread de toucher ˆ la liste des tables de 
	    // cette salle pendant que le joueur quitte cette table
	    synchronized (getObjSalle().obtenirListeTables())
	    {
		    // Empçcher d'autres thread de toucher ˆ la liste des joueurs de 
		    // cette table pendant que le joueur quitte cette table
		    synchronized (lstJoueurs)
		    {
		    	// Enlever le joueur de la liste des joueurs de cette table
				lstJoueurs.remove(joueur.obtenirNomUtilisateur());
				lstJoueursEnAttente.remove(joueur.obtenirNomUtilisateur());
				
				// Le joueur est maintenant dans aucune table
				if (detruirePartieCourante == true)
				{
					joueur.definirPartieCourante(null);
				}
				
				// Si on doit générer le numéro de commande de retour, alors
				// on le génçre, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}

				// Empçcher d'autres thread de toucher ˆ la liste des joueurs de 
			    // cette salle pendant qu'on parcourt tous les joueurs de la salle
				// pour leur envoyer un événement
			    synchronized (getObjSalle().obtenirListeJoueurs())
			    {
					// Préparer l'événement qu'un joueur a quitté la table. 
					// Cette fonction va passer les joueurs et créer un 
					// InformationDestination pour chacun et ajouter l'événement 
					// dans la file de gestion d'événements
					preparerEvenementJoueurQuitteTable(joueur.obtenirNomUtilisateur());
			    }

			    // S'il ne reste aucun joueur dans la table et que la partie
			    // est terminée, alors on doit détruire la table
			    if ((lstJoueurs.size() == 0 && bolEstArretee == true)||(joueur.obtenirNomUtilisateur() == strNomUtilisateurCreateur && !bolEstCommencee))
			    {
			    	//Arreter le gestionnaire de temps
			    	//objGestionnaireTemps.arreterGestionnaireTemps();
			    	// Détruire la table courante et envoyer les événements 
			    	// appropriés
			    	getObjSalle().detruireTable(this);
			    }
		    }
		}
	}
	
	
	/**
	 * Cette méthode permet au joueur passé en paramçtres de recommencer la partie. 
	 * On suppose que le joueur est dans la table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de recommencer
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * 
	 * Synchronisme : Cette fonction est synchronisée sur la liste des tables
	 * 				  puis sur la liste des joueurs de cette table, car il se
	 * 				  peut qu'on doive détruire la table si c'est le dernier
	 * 				  joueur et qu'on va modifier la liste des joueurs de cette
	 * 				  table, car le joueur quitte la table. Cela évite que des
	 * 				  joueurs entrent ou quittent une table en mçme temps.
	 * 				  On n'a pas ˆ s'inquiéter que le joueur soit modifié
	 * 				  pendant le temps qu'on exécute cette fonction. Si on 
	 * 				  inverserait les synchronisations, ça pourrait créer un 
	 * 				  deadlock avec les personnes entrant dans la salle.
	 */
	public void restartGame(JoueurHumain joueur, boolean doitGenererNoCommandeRetour)
	{
	    // Empçcher d'autres thread de toucher ˆ la liste des tables de 
	    // cette salle pendant que le joueur quitte cette table
	    synchronized (getObjSalle().obtenirListeTables())
	    {
		    // Empçcher d'autres thread de toucher ˆ la liste des joueurs de 
		    // cette table pendant que le joueur et ajouter a la table
		    synchronized (lstJoueurs)
		    {
		    	// ajouter le joueur a la liste des joueurs de cette table
				lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
				
				
				
				// Si on doit générer le numéro de commande de retour, alors
				// on le génçre, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}
			
				preparerEvenementJoueurRejoindrePartie(joueur.obtenirNomUtilisateur(), joueur.obtenirPartieCourante().obtenirIdPersonnage(), joueur.obtenirPartieCourante().obtenirPointage());
			   
		    }
		    synchronized (lstJoueursDeconnectes)
		    {
		    	lstJoueursDeconnectes.remove(joueur);
		    }
		}
	}
	
	/**
	 * Cette méthode permet au joueur passé en paramçtres de démarrer la partie. 
	 * On suppose que le joueur est dans la table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de démarrer la partie
	 * @param int idPersonnage : Le numéro Id du personnage choisi par le joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return String : Succes : si le joueur est maintenant en attente
	 * 					DejaEnAttente : si le joueur était déjˆ en attente
	 * 					PartieEnCours : si une partie était en cours
	 * 
	 * Synchronisme : Cette fonction est synchronisée sur la liste des joueurs 
	 * 				  en attente, car il se peut qu'on ajouter ou retirer des
	 * 				  joueurs de la liste en attente en mçme temps. On n'a pas
	 * 				  ˆ s'inquiéter que le mçme joueur soit mis dans la liste 
	 * 				  des joueurs en attente par un autre thread.
	 */
	public String demarrerPartie(JoueurHumain joueur, int idPersonnage, boolean doitGenererNoCommandeRetour)
	{
		// Cette variable va permettre de savoir si le joueur est maintenant
		// attente ou non
		String strResultatDemarrerPartie;
		
	    // Empçcher d'autres thread de toucher ˆ la liste des joueurs en attente 
	    // de cette table pendant que le joueur tente de démarrer la partie
	    synchronized (lstJoueursEnAttente)
	    {
	    	// Si une partie est en cours alors on va retourner PartieEnCours
	    	if (bolEstCommencee == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
	    	}
	    	// Sinon si le joueur est déjˆ en attente, alors on va retourner 
	    	// DejaEnAttente
	    	else if (lstJoueursEnAttente.containsKey(joueur.obtenirNomUtilisateur()) == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.DejaEnAttente;
	    	}
	    	else
	    	{
	    		// La commande s'est effectuée avec succçs
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.Succes;
	    		
	    		// Ajouter le joueur dans la liste des joueurs en attente
				lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);
				
				// Garder en mémoire le Id du personnage choisi par le joueur
				joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);
				
		//System.out.println(idPersonnage);
				pictures.add((idPersonnage - 10000)/100);
				
	    		// Si on doit générer le numéro de commande de retour, alors
				// on le génçre, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}
				
				// Empçcher d'autres thread de toucher ˆ la liste des joueurs de 
			    // cette table pendant qu'on parcourt tous les joueurs de la table
				// pour leur envoyer un événement
			    synchronized (lstJoueurs)
			    {
					// Préparer l'événement de joueur en attente. 
					// Cette fonction va passer les joueurs et créer un 
					// InformationDestination pour chacun et ajouter l'événement 
					// dans la file de gestion d'événements
					preparerEvenementJoueurDemarrePartie(joueur.obtenirNomUtilisateur(), idPersonnage);		    	
			    }
				
				// Si le nombre de joueurs en attente est maintenant le nombre 
				// de joueurs que ça prend pour joueur au jeu, alors on lance 
				// un événement qui indique que la partie est commencée
				if (lstJoueursEnAttente.size() == intNbJoueurDemande)
				{
					laPartieCommence("Aucun");			
				}
	    	}
		}
	    
	    return strResultatDemarrerPartie;
	}
	
	public String demarrerMaintenant(JoueurHumain joueur, int idPersonnage, boolean doitGenererNoCommandeRetour, String strParamJoueurVirtuel)
	{
		// Lorsqu'on fait démarré maintenant, le nombre de joueurs sur la
		// table devient le nombre de joueurs demandé, lorsqu'ils auront tous
		// fait OK, la partie démarrera
		intNbJoueurDemande = lstJoueurs.size();
		
		String strResultatDemarrerPartie;
		synchronized (lstJoueursEnAttente)
	    {
            // Si une partie est en cours alors on va retourner PartieEnCours
	    	if (bolEstCommencee == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
	    	}
	    	//TODO si joueur pas en attente?????
	    	else
	    	{
	    		// La commande s'est effectuée avec succçs
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.Succes;
	    		
	    		// Ajouter le joueur dans la liste des joueurs en attente
				//lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);
				
				// Garder en mémoire le Id du personnage choisi par le joueur
				joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);
				
							
	    		// Si on doit générer le numéro de commande de retour, alors
				// on le génçre, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}
				
				// Si le nombre de joueurs en attente est maintenant le nombre 
				// de joueurs que ça prend pour joueur au jeu, alors on lance 
				// un événement qui indique que la partie est commencée
				if (lstJoueursEnAttente.size() == intNbJoueurDemande)
				{
					laPartieCommence(strParamJoueurVirtuel);			
				}
	    	}
	    }
		return strResultatDemarrerPartie;
	}
		
    /* Cette fonction permet d'obtenir un tableau contenant intNombreJoueurs
     * noms de joueurs virtuels différents
     */
	private String[] obtenirNomsJoueursVirtuels(int intNombreJoueurs)
	{
		// Obtenir une référence vers l'objet ParametreIA contenant
		// la banque de noms
		ParametreIA objParametreIA = objControleurJeu.obtenirParametreIA();
		
		// Obtenir le nombre de noms dans la banque
		int intQuantiteBanque = objParametreIA.tBanqueNomsJoueurVirtuels.length;
		
		// Déclaration d'un tableau pour mélanger les indices de noms
		int tIndexNom[] = new int[intQuantiteBanque];
		
		// Permet d'échanger des indices du tableau pour mélanger
		int intTemp;
		int intA;
		int intB;
		
		// Préparer le tableau pour le mélange
		for (int i = 0; i < tIndexNom.length; i++)
		{
			tIndexNom[i] = i;
		}
		
		// Mélanger les noms
		for (int i = 0; i < intNombreJoueurs; i++)
		{
			intA = i;
			intB = objControleurJeu.genererNbAleatoire(intQuantiteBanque);
		    
		    intTemp = tIndexNom[intA];
		    tIndexNom[intA] = tIndexNom[intB];
		    tIndexNom[intB] = intTemp;
		}

       // Créer le tableau de retour
       String tRetour[] = new String[intNombreJoueurs];
       
       // Choisir au hasard oç aller chercher les indices
       int intDepart = objControleurJeu.genererNbAleatoire(intQuantiteBanque);
       
       // Remplir le tableau avec les valeurs trouvées
       for (int i = 0; i < intNombreJoueurs; i++)
       {
           tRetour[i] = new String(objParametreIA.tBanqueNomsJoueurVirtuels[(i + intDepart) % intQuantiteBanque]);
       }
       
       return tRetour;
	}
	
	private void laPartieCommence(String strParamJoueurVirtuel)
	{
        // Créer une nouvelle liste qui va garder les points des 
		// cases libres (n'ayant pas d'objets dessus)
		ArrayList<Point> lstPointsCaseLibre = new ArrayList<Point>();
		
				
		// Créer un tableau de points qui va contenir la position 
		// des joueurs
		Point[] objtPositionsJoueurs;
		
		// Création d'une nouvelle liste dont la clé est le nom 
		// d'utilisateur du joueur et le contenu est un point 
		// représentant la position du joueur
		TreeMap<String, Point> lstPositionsJoueurs = new TreeMap<String, Point>();
        
                // Contient les noms des joueurs virtuels
                String tNomsJoueursVirtuels[] = null;

                // Contiendra le dernier ID des objets
                objProchainIdObjet = new Integer(0);
        
		//TODO: Peut-çtre devoir synchroniser cette partie, il 
		//      faut voir avec les autres bouts de code qui 
		// 		vérifient si la partie est commencée (c'est OK 
		//		pour entrerTable)
		// Changer l'état de la table pour dire que maintenant une 
		// partie est commencée
		bolEstCommencee = true;
		
		// Change l'état de la table pour dire que la partie
		// n'est pas arrçtée (note: bolEstCommencee restera à true
		// pendant que les joueurs sont à l'écran de pointage)
		bolEstArretee = false;
		
		// Générer le plateau de jeu selon les règles de la table et 
		// garder le plateau en mémoire dans la table
		objttPlateauJeu = GenerateurPartie.genererPlateauJeu(objGestionnaireBD, 
				objRegles, intTempsTotal, lstPointsCaseLibre, objProchainIdObjet, lstPointsFinish, getObjSalle().getGameType());

        // Définir le prochain id pour les objets
        objProchainIdObjet++;
        
		// Obtenir la position des joueurs de cette table
		int nbJoueur = lstJoueursEnAttente.size(); //TODO a vérifier
		
		// Contient le niveau de difficulté que le joueur désire pour
		// les joueurs virtuels
		// on obtient la difficulté par défaut à partir du fichier de configuration
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		int intDifficulteJoueurVirtuel = config.obtenirNombreEntier("joueurs-virtuels.difficulte_defaut");
		
		
		// Obtenir le nombre de joueurs virtuel requis
		// Vérifier d'abord le paramçtre envoyer par le joueur
		if (strParamJoueurVirtuel.equals("Aucun"))
		{
			intNombreJoueursVirtuels = 0;
		}
		else
		{
			// Le joueur veut des joueurs virtuels
			if (strParamJoueurVirtuel.equals("Facile"))
			{
				intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_FACILE;
			}
			else if(strParamJoueurVirtuel.equals("Intermediaire"))
			{
				intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_MOYEN;
			}
			else if(strParamJoueurVirtuel.equals("Difficile"))
			{
				intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_DIFFICILE;
			}
			else if(strParamJoueurVirtuel.equals("TresDifficile"))
			{
				intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_TRES_DIFFICILE;
			}
			
			// Déterminer combien de joueurs on veut
			intNombreJoueursVirtuels = objRegles.getNbVirtualPlayers();
			if(intNombreJoueursVirtuels + nbJoueur > objRegles.getMaxNbPlayers()) 
			   intNombreJoueursVirtuels = objRegles.getMaxNbPlayers() - nbJoueur;
			
		
		}
		
		
		// Aller chercher les positions de départ pour les joueurs humains et virtuels
        if(getObjSalle().getGameType().equals("Tournament"))
        {
		   objtPositionsJoueurs = GenerateurPartie.genererPositionJoueurs(nbJoueur + intNombreJoueursVirtuels, objRegles.getNbTracks());
        }else{
           objtPositionsJoueurs = GenerateurPartie.genererPositionJoueurs(nbJoueur + intNombreJoueursVirtuels, lstPointsCaseLibre);	
        }
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueursEnAttente (chaque élément est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueursEnAttente.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les personnages
		Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();

		// S'il y a des joueurs virtuels, alors on va créer une nouvelle liste
		// qui contiendra ces joueurs
		if (intNombreJoueursVirtuels > 0)
		{
		    lstJoueursVirtuels = new Vector<JoueurVirtuel>();
		    
		    // Aller trouver les noms des joueurs virtuels
		    tNomsJoueursVirtuels = obtenirNomsJoueursVirtuels(intNombreJoueursVirtuels);
		}
		
		// Cette variable permettra d'affecter aux joueurs virtuels des id
		// de personnage différents de ceux des joueurs humains
		int intIdPersonnage = 1;
        int position = 0;
		
		// Passer toutes les positions des joueurs et les définir
		for (int i = 0; i < objtPositionsJoueurs.length; i++)
		{
			
			// On doit affecter certains positions aux joueurs humains et d'autres aux joueurs
		    // virtuels. La grandeur de objtPositionsJoueurs est nbJoueur + intNombreJoueursVirtuels
		    if (i < nbJoueur )
		    {
    		    
    			// Comme les positions sont générées aléatoirement, on 
    			// se fou un peu duquel on va définir la position en 
    			// premier, on va donc passer simplement la liste des 
    			// joueurs
    			// Créer une référence vers le joueur courant 
    		    // dans la liste (pas besoin de vérifier s'il y en a un 
    			// prochain, car on a généré la position des joueurs 
    			// selon cette liste
    			JoueurHumain objJoueur = (JoueurHumain) (((Map.Entry<String,JoueurHumain>)(objIterateurListeJoueurs.next())).getValue());
    			
    			if(objJoueur.getRole() == 2)
    			{
    				// Définir la position du joueur master
        			objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[objtPositionsJoueurs.length - 1]);
        			
        			// Ajouter la position du master dans la liste
        			lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[objtPositionsJoueurs.length - 1]);
        			
        			position--;
    			}else{

    				// Définir la position du joueur courant
    				objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[position]);

    				// Ajouter la position du joueur dans la liste
    				lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[position]);
    			}
    		}
    		else
    		{
    			int IDdess;
    			
    			// to have differents pictures for the virtual players
    			do{
    			    IDdess = objControleurJeu.genererNbAleatoire(9) + 1;
    			}while(pictures.contains(IDdess));
    		    
    			// On se rendra ici seulement si intNombreJoueursVirtuels > 0
    		    // C'est ici qu'on crée les joueurs virtuels, ils vont commencer
    		    // à jouer plus loin
    		  
                // Ajouter un joueur virtuel dans la table
    			intIdPersonnage = 10000 + 100*IDdess + 50 + i;
 
		        // Utiliser le prochaine id de personnage libre
		        while (!idPersonnageEstLibre(intIdPersonnage))
		        {
		        	// Incrémenter le id du personnage en espérant en trouver un autre
		        	intIdPersonnage++;
		        }
		        
		        // to have virtual players of all difficulty levels
		        intDifficulteJoueurVirtuel = objControleurJeu.genererNbAleatoire(4);
		        System.out.println("Virtuel : " + intDifficulteJoueurVirtuel);
		        
		        // Créé le joueur virtuel selon le niveau de difficulté désiré
                JoueurVirtuel objJoueurVirtuel = new JoueurVirtuel(tNomsJoueursVirtuels[i - nbJoueur], 
                    intDifficulteJoueurVirtuel, this, objGestionnaireEvenements, objControleurJeu, intIdPersonnage);
                
                // Définir sa position
                objJoueurVirtuel.definirPositionJoueurVirtuel(objtPositionsJoueurs[position]);
                
                // Ajouter le joueur virtuel à la liste
                lstJoueursVirtuels.add(objJoueurVirtuel);
                
                pictures.add(IDdess);
                
                // Ajouter le joueur virtuel à la liste des positions, liste qui sera envoyée
                // aux joueurs humains
                lstPositionsJoueurs.put(objJoueurVirtuel.obtenirNom(), objtPositionsJoueurs[position]);
                
                // Pour le prochain joueur virtuel
                intIdPersonnage++;
                
    		}
		    position++;
		}
		
		// On peut maintenant vider la liste des joueurs en attente
		// car elle ne nous sert plus à rien
		lstJoueursEnAttente.clear();
		
		
		// Maintenant pour tous les joueurs, s'il y a des joueurs
		// virtuels de présents, on leur envoit un message comme
		// quoi les joueurs virtuels sont prçts
		if (intNombreJoueursVirtuels > 0)
		{
		    synchronized (lstJoueurs)
		    {
	    	    for (int i = 0; i < lstJoueursVirtuels.size(); i++)
	    	    {
					// Préparer l'événement de joueur en attente. 
					// Cette fonction va passer les joueurs et créer un 
					// InformationDestination pour chacun et ajouter l'événement 
					// dans la file de gestion d'événements
					JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel) lstJoueursVirtuels.get(i);
					
					preparerEvenementJoueurEntreTable(objJoueurVirtuel.obtenirNom(), 1);
					preparerEvenementJoueurDemarrePartie(objJoueurVirtuel.obtenirNom(), objJoueurVirtuel.obtenirIdPersonnage());		    	
			    }
		    }
	    }
		
		
		// Empçcher d'autres thread de toucher à la liste des joueurs de 
	    // cette table pendant qu'on parcourt tous les joueurs de la table
		// pour leur envoyer un événement
	    synchronized (lstJoueurs)
	    {
			// Préparer l'événement que la partie est commencée. 
			// Cette fonction va passer les joueurs et créer un 
			// InformationDestination pour chacun et ajouter l'événement 
			// dans la file de gestion d'événements
			preparerEvenementPartieDemarree(lstPositionsJoueurs);
	    }
	    
	    int tempsStep = 1;
	    objTacheSynchroniser.ajouterObservateur( this );
	    objMinuterie = new Minuterie( intTempsTotal * 60, tempsStep );
	    objMinuterie.ajouterObservateur( this );
	    objGestionnaireTemps.ajouterTache( objMinuterie, tempsStep );
	    
	    // Obtenir la date à ce moment précis
	    objDateDebutPartie = new Date();
	    
	    // Démarrer tous les joueurs virtuels 
	    if (intNombreJoueursVirtuels > 0)
	    {
    	    for (int i = 0; i < lstJoueursVirtuels.size(); i++)
    	    {
                Thread threadJoueurVirtuel = new Thread((JoueurVirtuel) lstJoueursVirtuels.get(i));
                threadJoueurVirtuel.start();
            }
                 
        }
         // On trouve une position initiale au WinTheGame et on part son thread si nécessaire
         definirNouvellePositionWinTheGame();
                //winTheGame.demarrer();
          
	}// end method
	
	public void arreterPartie(String joueurGagnant)
	{
	    // bolEstArretee permet de savoir si cette fonction a déjà été appelée
	    // de plus, bolEstArretee et bolEstCommencee permettent de conna”tre 
	    // l'état de la partie
		if(bolEstArretee == false)
		{
			// Arrçter la partie
			bolEstArretee = true;
			objTacheSynchroniser.enleverObservateur(this);
			objGestionnaireTemps.enleverTache(objMinuterie);
			objMinuterie = null;

			// S'il y a au moins un joueur qui a complété la partie,
			// alors on ajoute les informations de cette partie dans la BD
			if(lstJoueurs.size() > 0)
			{				
				// Ajouter la partie dans la BD
				int clePartie = objGestionnaireBD.ajouterInfosPartieTerminee(objDateDebutPartie, intTempsTotal);
	
		        // Sert à déterminer si le joueur a gagné
		        boolean boolGagnant;
		
		        // Sert à déterminer le meilleur score pour cette partie
				int meilleurPointage = 0;
							
				// Parcours des joueurs virtuels pour trouver le meilleur pointage
				if (lstJoueursVirtuels != null)
				{
					for (int i = 0; i < lstJoueursVirtuels.size(); i++)
					{
						JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel) lstJoueursVirtuels.get(i);
					    if (objJoueurVirtuel.obtenirPointage() > meilleurPointage)
					    {
					    	meilleurPointage = objJoueurVirtuel.obtenirPointage();
					    }
					}
				}
					
				synchronized (lstJoueurs)
			    {
			    	// Parcours des joueurs pour trouver le meilleur pointage
					Iterator<JoueurHumain> iteratorJoueursHumains = lstJoueurs.values().iterator();
					while (iteratorJoueursHumains.hasNext())
					{
						JoueurHumain objJoueurHumain = (JoueurHumain)iteratorJoueursHumains.next();
						if (objJoueurHumain.obtenirPartieCourante().obtenirPointage() > meilleurPointage)
						{
							meilleurPointage = objJoueurHumain.obtenirPartieCourante().obtenirPointage();
						}
					}
			    	
					preparerEvenementPartieTerminee(joueurGagnant);
					
					// Parcours des joueurs pour mise à jour de la BD et
					// pour ajouter les infos de la partie complétée
					Iterator<JoueurHumain> it = lstJoueurs.values().iterator();
					while(it.hasNext())
					{
						// Mettre a jour les données des joueurs
						JoueurHumain joueur = (JoueurHumain)it.next();
						objGestionnaireBD.mettreAJourJoueur(joueur, intTempsTotal);
						
						// if the game was with the permission to use user's money from DB
						if (joueur.obtenirPartieCourante().isMoneyPermit())
						{
						   objGestionnaireBD.setNewPlayersMoney(joueur.obtenirCleJoueur(), joueur.obtenirPartieCourante().obtenirArgent());
						} 
						
                                                // Si un joueur a atteint le WinTheGame, joueurGagnant contiendra le nom de ce joueur
                                                if(joueurGagnant.equals(""))
                                                {
                                                    // Vérififer si ce joueur a gagné par les points
                                                    if (joueur.obtenirPartieCourante().obtenirPointage() == meilleurPointage)
                                                    {
                                                            boolGagnant = true;
                                                    }
                                                    else
                                                    {
                                                            boolGagnant = false;
                                                    }
                                                }
                                                else
                                                {
                                                    if(joueurGagnant.equals(joueur.obtenirNomUtilisateur())) boolGagnant = true;
                                                    else boolGagnant = false;
                                                }
						
						// Ajouter l'information pour cette partie et ce joueur
						objGestionnaireBD.ajouterInfosJoueurPartieTerminee(clePartie, joueur , boolGagnant);
						
						
					}
			    }
		    }
		    
		    // Arrçter les threads des joueurs virtuels
            if (intNombreJoueursVirtuels > 0)
		    {
		        for (int i = 0; i < lstJoueursVirtuels.size(); i++)
		        {
                    ((JoueurVirtuel)lstJoueursVirtuels.get(i)).arreterThread();
		        }
		    }
		    
		    // Enlever les joueurs déconnectés de cette table de la
		    // liste des joueurs déconnectés du serveur pour éviter
		    // qu'ils ne se reconnectent et tentent de rejoindre une partie terminée
		    for (int i = 0; i < lstJoueursDeconnectes.size(); i++)
		    {
		    	objControleurJeu.enleverJoueurDeconnecte((String) lstJoueursDeconnectes.get(i));
		    }
		    
		    // Enlever les joueurs déconnectés de cette table
		    lstJoueursDeconnectes = new Vector<String>();
		    
		    // Si jamais les joueurs humains sont tous déconnectés, alors
		    // il faut détruire la table ici
		    if (lstJoueurs.size() == 0)
		    {
		    	// Détruire la table courante et envoyer les événements 
		    	// appropriés
		    	getObjSalle().detruireTable(this);
		    }
		}
	}
	
	/**
	 * Cette fonction permet de retourner le numéro de la table courante.
	 * 
	 * @return int : Le numéro de la table
	 */
	public int obtenirNoTable()
	{
		return intNoTable;
	}
	
	/**
	 * Cette fonction permet de retourner la liste des joueurs. La vraie liste
	 * est retournée.
	 * 
	 * @return TreeMap : La liste des joueurs se trouvant dans la table courante
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				  l'çtre par l'appelant de cette fonction tout dépendant
	 * 				  du traitement qu'elle doit faire
	 */
	public TreeMap<String, JoueurHumain> obtenirListeJoueurs()
	{
		return lstJoueurs;
	}
	
	/**
	 * Cette fonction permet de retourner la liste des joueurs qui sont en 
	 * attente de jouer une partie. La vraie liste est retournée.
	 * 
	 * @return TreeMap : La liste des joueurs en attente se trouvant dans la 
	 * 					 table courante
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				  l'çtre par l'appelant de cette fonction tout dépendant
	 * 				  du traitement qu'elle doit faire
	 */
	public TreeMap<String, JoueurHumain> obtenirListeJoueursEnAttente()
	{
		return lstJoueursEnAttente;
	}
	
	/**
	 * Cette fonction permet de retourner le temps total des parties de cette 
	 * table.
	 * 
	 * @return int : Le temps total des parties de cette table
	 */
	public int obtenirTempsTotal()
	{
		return intTempsTotal;
	}
	
	/**
	 * Cette fonction permet de déterminer si la table est complçte ou non 
	 * (elle est complçte si le nombre de joueurs dans cette table égale le 
	 * nombre de joueurs maximum par table).
	 * 
	 * @return boolean : true si la table est complçte
	 * 					 false sinon
	 * 
	 * Synchronisme : Cette fonction est synchronisée car il peut s'ajouter de
	 * 				  nouveaux joueurs ou d'autres peuvent quitter pendant la 
	 * 				  vérification.
	 */
	public boolean estComplete()
	{
	    // Empçcher d'autres Thread de toucher à la liste des joueurs de cette
	    // table pendant qu'on fait la vérification (un TreeMap n'est pas 
	    // synchronisé)
	    synchronized (lstJoueurs)
	    {
			// Si la taille de la liste de joueurs égale le nombre maximal de 
			// joueurs alors la table est complçte, sinon elle ne l'est pas
			return (lstJoueurs.size() == intNbJoueurDemande);	        
	    }
	}
	
	/**
	 * Cette fonction permet de déterminer si une partie est commencée ou non.
	 * 
	 * @return boolean : true s'il y a une partie en cours
	 * 					 false sinon
	 */
	public boolean estCommencee()
	{
		return bolEstCommencee;	        
	}
	
	/**
	 * Cette fonction retourne les rçgles pour la table courante.
	 * 
	 * @return Regles : Les règles pour la table courante
	 */
	public Regles obtenirRegles()
	{
		return objRegles;
	}
	
	/**
	 * Cette fonction retourne le plateau de jeu courant.
	 * 
	 * @return Case[][] : Le plateau de jeu courant,
	 * 					  null s'il n'y a pas de partie en cours
	 */
	public Case[][] obtenirPlateauJeuCourant()
	{
		return objttPlateauJeu;
	}
	
	/**
	 * Cette méthode permet de remplir la liste des personnages des joueurs 
	 * ou les clés seront le id d'utilisateur du joueur et le contenu le 
	 * numéro du personnage. On suppose que le joueur courant n'est pas 
	 * encore dans la liste.
	 *  
	 * @param TreeMap listePersonnageJoueurs : La liste des personnages 
	 * 										   pour chaque joueur
	 * @throws NullPointerException : Si la liste des personnages est à nulle
	 */
	private void remplirListePersonnageJoueurs(TreeMap<String, Integer> listePersonnageJoueurs, TreeMap<String, Integer> listeRoleJoueurs) throws NullPointerException
	{
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Ajouter le joueur dans la liste des personnages (il se peut que 
			// le joueur n'aie pas encore de personnages, alors le id est 0)
			listePersonnageJoueurs.put(objJoueur.obtenirNomUtilisateur(), new Integer(objJoueur.obtenirPartieCourante().obtenirIdPersonnage()));
			
			listeRoleJoueurs.put(objJoueur.obtenirNomUtilisateur(), objJoueur.getRole());
		}
		
		// Déclaration d'un compteur
		int i = 1;
		
		// Boucler tant qu'on n'a pas atteint le nombre maximal de 
		// joueurs moins le joueur courant car on ne le met pas dans la liste
		while (listePersonnageJoueurs.size() < MAX_NB_PLAYERS - 1)
		{
			// On ajoute un joueur inconnu ayant le personnage 0
			listePersonnageJoueurs.put("Inconnu" + Integer.toString(i), new Integer(0));
			listeRoleJoueurs.put("Inconnu" + Integer.toString(i), new Integer(0));
			
			i++;
		}
	}

	/**
	 * Cette méthode permet de préparer l'événement de l'entrée d'un joueur 
	 * dans la table courante. Cette méthode va passer tous les joueurs 
	 * de la salle courante et pour ceux devant çtre avertis (tous sauf le 
	 * joueur courant passé en paramçtre), on va obtenir un numéro de commande, 
	 * on va créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient d'entrer dans la table
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				  par l'appelant (entrerTable).
	 */
	private void preparerEvenementJoueurEntreTable(String nomUtilisateur, int role)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur est entré dans la table
	    EvenementJoueurEntreTable joueurEntreTable = new EvenementJoueurEntreTable(intNoTable, nomUtilisateur, role);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la salle (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = getObjSalle().obtenirListeJoueurs().entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient d'entrer dans la table, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
			    joueurEntreTable.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurEntreTable);
	}

	/**
	 * Cette méthode permet de préparer l'événement du départ d'un joueur 
	 * de la table courante. Cette méthode va passer tous les joueurs 
	 * de la salle courante et pour ceux devant çtre avertis (tous sauf le 
	 * joueur courant passé en paramçtre), on va obtenir un numéro de commande, 
	 * on va créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de quitter la table
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				  par l'appelant (quitterTable).
	 */
	private void preparerEvenementJoueurQuitteTable(String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur a quitté la table
	    EvenementJoueurQuitteTable joueurQuitteTable = new EvenementJoueurQuitteTable(intNoTable, nomUtilisateur);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la salle (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = getObjSalle().obtenirListeJoueurs().entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de quitter la table, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
			    joueurQuitteTable.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurQuitteTable);
	}
	
	/**
	 * Cette méthode permet de préparer l'événement du démarrage d'une partie 
	 * de la table courante. Cette méthode va passer tous les joueurs 
	 * de la table courante et pour ceux devant çtre avertis (tous sauf le 
	 * joueur courant passé en paramçtre), on va obtenir un numéro de commande, 
	 * on va créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de démarrer la partie
	 * @param int idPersonnage : Le numéro Id du personnage choisi par le joueur 
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				  par l'appelant (demarrerPartie).
	 */
	private void preparerEvenementJoueurDemarrePartie(String nomUtilisateur, int idPersonnage)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur démarré une partie
	    EvenementJoueurDemarrePartie joueurDemarrePartie = new EvenementJoueurDemarrePartie(nomUtilisateur, idPersonnage);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de démarrer la partie, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
				joueurDemarrePartie.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurDemarrePartie);
	}
	
	/**
	 * Cette méthode permet de préparer l'événement du démarrage de partie 
	 * de la table courante. Cette méthode va passer tous les joueurs 
	 * de la table courante et on va obtenir un numéro de commande, on va 
	 * créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronisée.
	 * 
	 * @param TreeMap : La liste contenant les positions des joueurs
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				  par l'appelant (demarrerPartie).
	 */
	private void preparerEvenementPartieDemarree(TreeMap<String, Point> listePositionJoueurs)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs de la table qu'un joueur a démarré une partie
	    EvenementPartieDemarree partieDemarree = new EvenementPartieDemarree(intTempsTotal, listePositionJoueurs, this);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());

		    // Obtenir un numéro de commande pour le joueur courant, créer 
		    // un InformationDestination et l'ajouter à l'événement de la 
			// table
			partieDemarree.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            										objJoueur.obtenirProtocoleJoueur()));				
		}
		
		// Ajouter les nouveaux événements créés dans la liste d'événements 
		// à traiter
		objGestionnaireEvenements.ajouterEvenement(partieDemarree);
	}
	
	/**
	 * 
	 * @param nomUtilisateur
	 * @param nouveauPointage
	 */
	public void preparerEvenementMAJPointage(String nomUtilisateur, int nouveauPointage)
	{
		// Créer un nouveal événement qui va permettre d'envoyer l'événment
		// aux joueurs pour signifier une modification du pointage
		EvenementMAJPointage majPointage = new EvenementMAJPointage(nomUtilisateur, nouveauPointage);
		
		// Créer un ensemble contenant tous les tuples de la liste des joueurs
		// de la table
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passser tous les joueurs de la table et leur envoyer l'événement
		// NOTE: On omet d'envoyer au joueur nomUtilisateur étant donné
		//       qu'il connait déjà son pointage
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur n'est pas nomUtilisateur, alors
			// on peut envoyer un événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
				// Obtenir un numéro de commande pour le joueur courant, créer
				// un InformationDestination et l'ajouter à l'événement
				majPointage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));      																	 
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(majPointage);
	}
	
	/**
	 * Used to inform another players that one player is back to the game
	 * We need to give them his user name and his points
	 * @param nomUtilisateur
	 * @param nouveauPointage
	 */
	public void preparerEvenementJoueurRejoindrePartie(String userName, int idPersonnage, int points)
	{
		// Créer un nouveal événement qui va permettre d'envoyer l'événment
		// aux joueurs pour signifier une modification du pointage
		EvenementJoueurRejoindrePartie maPartie = new EvenementJoueurRejoindrePartie(userName, idPersonnage, points);
		
		// Créer un ensemble contenant tous les tuples de la liste des joueurs
		// de la table
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passser tous les joueurs de la table et leur envoyer l'événement
		// NOTE: On omet d'envoyer au joueur nomUtilisateur étant donné
		//       qu'il connait déjà son pointage
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur n'est pas nomUtilisateur, alors
			// on peut envoyer un événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(userName) == false)
			{
				// Obtenir un numéro de commande pour le joueur courant, créer
				// un InformationDestination et l'ajouter à l'événement
				maPartie.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));      																	 
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(maPartie);
	}


	public void preparerEvenementMAJArgent(String nomUtilisateur, int nouvelArgent)
	{
		// Créer un nouveal événement qui va permettre d'envoyer l'événment
		// aux joueurs pour signifier une modification de l'argent
		EvenementMAJArgent majArgent = new EvenementMAJArgent(nomUtilisateur, nouvelArgent);
		
		// Créer un ensemble contenant tous les tuples de la liste des joueurs
		// de la table
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passser tous les joueurs de la table et leur envoyer l'événement
		// NOTE: On omet d'envoyer au joueur nomUtilisateur étant donné
		//       qu'il connait déjà son argent
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur n'est pas nomUtilisateur, alors
			// on peut envoyer un événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
				// Obtenir un numéro de commande pour le joueur courant, créer
				// un InformationDestination et l'ajouter à l'événement
				majArgent.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(), objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(majArgent);
	}
       
	/**
	 * 
	 * @param joueurQuiUtilise
	 * @param joueurAffecte
	 * @param objetUtilise
	 * @param autresInformations
	 */
	public void preparerEvenementUtiliserObjet(String joueurQuiUtilise, String joueurAffecte, String objetUtilise, String autresInformations)
	{
        // Mçme chose que la fonction précédente, mais envoie plut™t les informations quant à l'utilisation d'un objet dont tous devront çtre au courant
		EvenementUtiliserObjet utiliserObjet = new EvenementUtiliserObjet(joueurQuiUtilise, joueurAffecte, objetUtilise, autresInformations);
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		while (objIterateurListe.hasNext() == true)
		{
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
            utiliserObjet.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),objJoueur.obtenirProtocoleJoueur()));
		}
		objGestionnaireEvenements.ajouterEvenement(utiliserObjet);
	}
        
    
	public void preparerEvenementMessageChat(String joueurQuiEnvoieLeMessage, String messageAEnvoyer)
	{
        // Meme chose que la fonction précédente, mais envoie plut™t un message de la part d'un joueur à tous les joueurs de la table
		EvenementMessageChat messageChat = new EvenementMessageChat(joueurQuiEnvoieLeMessage, messageAEnvoyer);
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		while (objIterateurListe.hasNext() == true)
		{
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
                        messageChat.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),objJoueur.obtenirProtocoleJoueur()));
		}
		objGestionnaireEvenements.ajouterEvenement(messageChat);
	}

/*	public void preparerEvenementDeplacementWinTheGame()
	{
        definirNouvellePositionWinTheGame();
                
		EvenementDeplacementWinTheGame deplacementWTG = new EvenementDeplacementWinTheGame(positionWinTheGame.x, positionWinTheGame.y);
		
		// Créer un ensemble contenant tous les tuples de la liste des joueurs de la table
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passser tous les joueurs de la table et leur envoyer l'événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Obtenir un numéro de commande pour le joueur courant, créer
			// un InformationDestination et l'ajouter à l'événement
		deplacementWTG.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(), objJoueur.obtenirProtocoleJoueur()));
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(deplacementWTG);
	}  */

	/**
	 * Method that is used to prepare event of move of player
	 * @param nomUtilisateur
	 * @param collision
	 * @param oldPosition
	 * @param positionJoueur
	 * @param nouveauPointage
	 * @param nouvelArgent
	 * @param bonus 
	 * @param objetUtilise
	 */
	public void preparerEvenementJoueurDeplacePersonnage( String nomUtilisateur, String collision, 
	    Point oldPosition, Point positionJoueur, int nouveauPointage, int nouvelArgent, int bonus, String objetUtilise)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur se deplace 
		
		EvenementJoueurDeplacePersonnage joueurDeplacePersonnage = new EvenementJoueurDeplacePersonnage( nomUtilisateur, 
		    oldPosition, positionJoueur, collision, nouveauPointage, nouvelArgent, bonus);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de démarrer la partie, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false )
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
				joueurDeplacePersonnage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));
				//System.out.println("Control : " + objJoueur.obtenirProtocoleJoueur() + " " + objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande());
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurDeplacePersonnage);
	}
	
	/**
	 * 
	 */
	private void preparerEvenementSynchroniser()
	{
		//Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs de la table
	    EvenementSynchroniserTemps synchroniser = new EvenementSynchroniserTemps( objMinuterie.obtenirTempsActuel() );
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());

		    // Obtenir un numéro de commande pour le joueur courant, créer 
		    // un InformationDestination et l'ajouter à l'événement de la 
			// table
			synchroniser.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            										objJoueur.obtenirProtocoleJoueur()));				
		}
		
		// Ajouter les nouveaux événements créés dans la liste d'événements 
		// à traiter
		objGestionnaireEvenements.ajouterEvenement(synchroniser);
	}
	
	/**
	 * 
	 * @param joueurGagnant
	 */
	private void preparerEvenementPartieTerminee(String joueurGagnant)
	{
            // joueurGagnant réfçre à la personne qui a atteint le WinTheGame (s'il y a lieu)
            
            // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs de la table
	    EvenementPartieTerminee partieTerminee = new EvenementPartieTerminee(lstJoueurs, lstJoueursVirtuels, joueurGagnant);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());

		    // Obtenir un numéro de commande pour le joueur courant, créer 
		    // un InformationDestination et l'ajouter à l'événement de la 
			// table
			partieTerminee.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            										objJoueur.obtenirProtocoleJoueur()));				
		}
		
		// Ajouter les nouveaux événements créés dans la liste d'événements 
		// à traiter
		objGestionnaireEvenements.ajouterEvenement(partieTerminee);
	}
	
	public void tempsEcoule()
	{
		arreterPartie("");
	}

	public int getObservateurMinuterieId()
	{
		return obtenirNoTable();
	}
	
	public void synchronise()
	{
		synchronized (lstJoueurs)
	    {
			preparerEvenementSynchroniser();
	    }
	}
	
	public int getObservateurSynchroniserId()
	{
		return obtenirNoTable();
	}
	
	public boolean estArretee()
	{
		return bolEstArretee;
	}
	
	public int obtenirTempsRestant()
	{
	    if (objMinuterie == null)
	    {
	    	return intTempsTotal;
	    }
	    else
	    {
	    	return objMinuterie.obtenirTempsActuel();
	    }
		
	}

    /* Cette fonction permet de définir le nombre de joueurs virtuels que l'on
     * veut pour cette table
     * @param: nb -> Nouveau nombre de joueurs virtuels
     */	
	public void setNombreJoueursVirtuels(int nb)
	{
	   intNombreJoueursVirtuels = nb;
	}
	
	/* Cette fonction permet d'obtenir le nombre de joueurs virtuels pour 
	 * cette table
	 */
	public int getNombreJoueursVirtuels()
	{
	   return intNombreJoueursVirtuels;
	}
	
	public Vector<JoueurVirtuel> obtenirListeJoueursVirtuels()
	{
	   return lstJoueursVirtuels;
	}
	
	/*
	 * Lorsqu'un joueur est déconnecté d'une partie en cours, on appelle
	 * cette fonction qui se charge de conserver les références vers
	 * les informations pour ce joueur
	 */
	public void ajouterJoueurDeconnecte(JoueurHumain joueurHumain)
	{
		lstJoueursDeconnectes.add(joueurHumain.obtenirNomUtilisateur().toLowerCase());
	}
	
	public Vector<String> obtenirListeJoueursDeconnectes()
	{
		return lstJoueursDeconnectes;
	}
	
	public Integer obtenirProchainIdObjet()
	{
		return objProchainIdObjet;
	}
	
	/** 
	 * Aller chercher dans la liste des joueurs sur cette table
	 * les ID des personnages choisi et vérifier si le id intID est
	 * déjà choisi
	 *
	 * Cette fonction vérifie dans la liste des joueurs et non dans
	 * la liste des joueurs en attente
	 */
	private boolean idPersonnageEstLibre(int intID)
	{
   	    // Préparation pour parcourir la liste des joueurs
        Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
        Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();

        // Parcourir la liste des joueurs et vérifier le id
   	    while(objIterateurListeJoueurs.hasNext() == true)
   	    {
	        // Aller chercher l'objet JoueurHumain
	        JoueurHumain objJoueurHumain = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListeJoueurs.next())).getValue());
	         
	        // Vérifier le id
	        if (objJoueurHumain.obtenirPartieCourante().obtenirIdPersonnage() == intID)
	        {
	         	// Déjà utilisé
	            return false;
	        }	   	     	
   	    }
   	     
   	    // Si on se rend ici, on a parcouru tous les joueurs et on n'a pas
   	    // trouvé ce id de personnage, donc le id est libre
   	    return true;
	}
	
	/**
	 * Aller chercher dans la liste des joueurs en attente
	 * les ID des personnages choisi et vérifier si le id intID est
	 * déjà choisi
	 *
	 * Cette fonction vérifie dans la liste des joueurs en attente
	 * la liste des joueurs (doit donc çtre utilisé avant que la partie commence)
	 *
	 */     
	public boolean idPersonnageEstLibreEnAttente(int intID)
	{
		synchronized (lstJoueursEnAttente)
		{
			// Préparation pour parcourir la liste des joueurs
			Set<Map.Entry<String, JoueurHumain>> lstEnsembleJoueurs = lstJoueursEnAttente.entrySet();
			Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();

			// Parcourir la liste des joueurs et vérifier le id
			while(objIterateurListeJoueurs.hasNext() == true)
			{
				// Aller chercher l'objet JoueurHumain
				JoueurHumain objJoueurHumain = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListeJoueurs.next())).getValue());

				// Vérifier le id
				if (objJoueurHumain.obtenirPartieCourante().obtenirIdPersonnage() == intID)
				{
					// Déjà utilisé
					return false;
				}	   	     	
			}


		}
		
		// Si on se rend ici, on a parcouru tous les joueurs et on n'a pas
		// trouvé ce id de personnage, donc le id est libre
		return true;		
	}// end method
     
	/**
	 * 
	 * @param username
	 * @return
	 */
	public JoueurHumain obtenirJoueurHumainParSonNom(String username)
	{
            Set<Map.Entry<String, JoueurHumain>> nomsJoueursHumains = lstJoueurs.entrySet();
            Iterator<Entry<String, JoueurHumain>> objIterateurListeJoueurs = nomsJoueursHumains.iterator();
            while(objIterateurListeJoueurs.hasNext() == true)
            {
                JoueurHumain j = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListeJoueurs.next())).getValue());
                if(username.equals(j.obtenirNomUtilisateur())) return j;
            }
            return (JoueurHumain)null;
	}
      
	/**
	 * 
	 * @param username
	 * @return
	 */
	public JoueurVirtuel obtenirJoueurVirtuelParSonNom(String username)
	{
            for(int i=0; i<lstJoueursVirtuels.size(); i++)
            {
                JoueurVirtuel j = (JoueurVirtuel)lstJoueursVirtuels.get(i);
                if(username.equals(j.obtenirNom())) return j;
            }
            return (JoueurVirtuel)null;
	}
        
  /*  // Cette méthode permettra de dire si un joueur a gagné la partie en
    // ayant accumulé assez de points et en ayant rejoint le WinTheGame
    public boolean aRejointLeWinTheGame(int pointageDuJoueur, Point positionDuJoueur)
    {
        	
      	boolean resultat = true;
       	if (getObjSalle().getGameType().equals("Tournament"))
       	{
       	   for(int i = 0; i < positionWinTheGameTournament.length; i++)
       	   {
       		   if(positionDuJoueur.equals(positionWinTheGameTournament[i]))
       			resultat = true;
        		   
       	   }
       	   return (peutAllerSurLeWinTheGame(pointageDuJoueur) && (resultat));
       	} else{
       	   return (peutAllerSurLeWinTheGame(pointageDuJoueur) && (positionDuJoueur.equals(positionWinTheGame)));
       	}
            
    } //end method */
        
        public Point obtenirPositionWinTheGame()
        {
            return positionWinTheGame;
        }
        
       
        public boolean peutAllerSurLeWinTheGame(int pointage)
        {
            return pointage >= pointageRequisPourAllerSurLeWinTheGame();
        }
        
        public int pointageRequisPourAllerSurLeWinTheGame()
        {
            return intTempsTotal*5;
        }   
        
        
        /**
         * 
         */ 
        public void definirNouvellePositionWinTheGame()
        {
        	if (getObjSalle().getGameType().equals("Tournament"))
    		{
        	  positionWinTheGame = new Point(objttPlateauJeu.length - 1,objttPlateauJeu[0].length - 1);
        	   
    		}else{
        	
    			positionWinTheGame = new Point(-1,-1);
    			
            /*   Random objRandom = new Random();
               boolean pasTrouve = true;
               int grandeurDeplacement = 3;
               int nbEssaisI = 0;
               int nbEssaisJ = 0;
               int maxEssais = 9000;
            
               // On obtient les positions des 4 joueurs afin de ne pas déplacer le WinTheGame
               // sur un joueur, ou encore sur une case oç un joueur voulait aller
               Point positionsJoueurs[] = new Point[4];
               Point positionsJoueursDesirees[] = new Point[4];
               {
                   for(int k=0; k<4; k++)
               {
                    positionsJoueurs[k] = new Point(0, 0);
                    positionsJoueursDesirees[k] = new Point(0, 0);
               }
                int i=0;
                {
                    Set nomsJoueursHumains = lstJoueurs.entrySet();
                    Iterator objIterateurListeJoueurs = nomsJoueursHumains.iterator();
                    while(objIterateurListeJoueurs.hasNext() == true)
                    {
                        JoueurHumain j = (JoueurHumain)(((Map.Entry)(objIterateurListeJoueurs.next())).getValue());
                        positionsJoueurs[i] = j.obtenirPartieCourante().obtenirPositionJoueur();
                        positionsJoueursDesirees[i] = j.obtenirPartieCourante().obtenirPositionJoueurDesiree();
                        if(positionsJoueursDesirees[i] == null) positionsJoueursDesirees[i] = positionsJoueurs[i];
                        i++;
                    }
                }
                {   
                    for(int k=0; k<lstJoueursVirtuels.size(); k++)
                    {
                        JoueurVirtuel j = (JoueurVirtuel)lstJoueursVirtuels.get(k);
                        positionsJoueurs[i] = j.obtenirPositionJoueur();
                        positionsJoueursDesirees[i] = positionsJoueurs[i];
                        i++;
                    }
                }
               }
            
               // On commence par regarder si les cases pas trop loin sont OK si on n'est pas en -1 -1
               if(positionWinTheGame.x != -1 && positionWinTheGame.y != -1)
               {
                  for(int i= positionWinTheGame.x-(objRandom.nextInt(grandeurDeplacement+1)-grandeurDeplacement/2); pasTrouve && nbEssaisI<maxEssais; i = positionWinTheGame.x-(objRandom.nextInt(grandeurDeplacement+1)-grandeurDeplacement/2))
                  {
                    //System.out.println("i: " + Integer.toString(i));
                    nbEssaisJ = 0;
                    objRandom.setSeed(System.currentTimeMillis());
                    if(i>=0 && i<objttPlateauJeu.length) for(int j=positionWinTheGame.y-(objRandom.nextInt(grandeurDeplacement+1)-grandeurDeplacement/2); pasTrouve && i >= 0 && nbEssaisJ < maxEssais; j = positionWinTheGame.y-(objRandom.nextInt(grandeurDeplacement+1)-grandeurDeplacement/2))
                    {
                        //System.out.println("   j: " + Integer.toString(j));
                        objRandom.setSeed(System.currentTimeMillis());
                        // Est-ce que la case existe? Est-ce que c'est une case couleur?
                        if(j>=0 && j<objttPlateauJeu[i].length) if(j >= 0 && objttPlateauJeu[i][j] != null && objttPlateauJeu[i][j] instanceof CaseCouleur)
                        {
                            CaseCouleur caseTemp = (CaseCouleur)objttPlateauJeu[i][j];
                            // Est-ce qu'il n'y a rien dessus?
                            if(caseTemp.obtenirObjetArme() == null && caseTemp.obtenirObjetCase() == null)
                            {
                                // Est-ce que c'est la mçme case? Est-ce dans les limites?
                                if(i != positionWinTheGame.x && j != positionWinTheGame.y && i >= 0 && j >= 0 && i < objttPlateauJeu.length && j < objttPlateauJeu[i].length)
                                {
                                    // Est-ce qu'un joueur est sur cette case ou veut s'y déplacer?
                                    boolean presence = false;
                                    for(int k=0; k<4 && !presence; k++) if((new Point(i, j)).equals(positionsJoueurs[k]) || (new Point(i, j)).equals(positionsJoueursDesirees[k])) presence = true;
                                    if(!presence)
                                    {
                                        // Tout est OK, on déplace le WinTheGame
                                        pasTrouve = false;
                                        positionWinTheGame.move(i, j);
                                    }
                                }
                            }
                        }
                        nbEssaisJ++;
                    }
                    nbEssaisI++;
                }
            }
            
            // Sinon, on prend la case qui minimise la distance maximale à chacun des joueurs
            if(pasTrouve)
            {
                int meilleureDistMax = 666;
                int meilleurI = positionWinTheGame.x;
                int meilleurJ = positionWinTheGame.y;
                for(int i=0; i < objttPlateauJeu.length; i++)
                {
                    for(int j=0; j < objttPlateauJeu[i].length; j++)
                    {
                        // Est-ce que la case existe? Est-ce que c'est une case couleur?
                        if(objttPlateauJeu[i][j] != null && objttPlateauJeu[i][j] instanceof CaseCouleur)
                        {
                            CaseCouleur caseTemp = (CaseCouleur)objttPlateauJeu[i][j];
                            // Est-ce qu'il n'y a rien dessus?
                            if(caseTemp.obtenirObjetArme() == null && caseTemp.obtenirObjetCase() == null)
                            {
                                // Est-ce qu'un joueur est sur cette case ou veut s'y déplacer?
                                boolean presence = false;
                                for(int k=0; k<4 && !presence; k++) if((new Point(i, j)).equals(positionsJoueurs[k]) || (new Point(i, j)).equals(positionsJoueursDesirees[k])) presence = true;
                                if(!presence)
                                {
                                    // On regarde la distance maximale aux joueurs
                                    int[] distances = {0,0,0,0};
                                    for(int z=0; z<positionsJoueurs.length; z++) distances[z] = Math.abs(i - positionsJoueurs[z].x) + Math.abs(j - positionsJoueurs[z].y);
                                    int distMax = Math.max(Math.max(distances[0], distances[1]), Math.max(distances[2], distances[3]));
                                    if(distMax <= meilleureDistMax)
                                    {
                                        meilleurI = i;
                                        meilleurJ = j;
                                        meilleureDistMax = distMax;
                                    }
                                }
                            }
                        }
                    }
                }
                // Tout est OK, on déplace le WinTheGame
                positionWinTheGame.move(meilleurI, meilleurJ);
            } */
        }
    }// end 

		public void setObjSalle(Salle objSalle) {
			this.objSalle = objSalle;
		}

		public Salle getObjSalle() {
			return objSalle;
		}
		
		/**
		 * @return the tableName
		 */
		public String getTableName() {
			return tableName;
		}

		/**
		 * @param tableName the tableName to set
		 */
		public void setTableName(String tableName) {
			if(tableName == "")
			{
			   this.tableName = "Table. " + this.intNoTable;
			}else
			{
				this.tableName = tableName;
			}
		}
		
/*		private Boolean controlForRole(int userName)
		{
			// Bloc of code to treat the username
	        int firstDel = userName.indexOf("-");                 // find first delimiter
	        int secondDel = userName.indexOf(".",firstDel + 1);   // find second delimiter
	        String master = "";

	        //Now extract the 'master' from username
	        if (firstDel != -1 && secondDel != -1)
	           master = userName.substring(firstDel + 1, secondDel);
	       
			
			return master.equalsIgnoreCase("master");
			
		}*/
}// end class    
