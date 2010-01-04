package ServeurJeu.ComposantesJeu;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import Enumerations.Categories;
import Enumerations.RetourFonctions.ResultatEntreeTable;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Evenements.EvenementJoueurEntreSalle;
import ServeurJeu.Evenements.EvenementJoueurQuitteSalle;
import ServeurJeu.Evenements.EvenementNouvelleTable;
import ServeurJeu.Evenements.EvenementTableDetruite;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.ControleurJeu;

//TODO: Le mot de passe d'une salle ne doit pas être modifiée pendant le jeu,
//      sinon il va falloir ajouter des synchronisations à chaque fois qu'on
//      fait des validations avec le mot de passe de la salle.
/**
 * @author Jean-François Brind'Amour
 */
public class Salle 
{
	// Déclaration d'une référence vers le gestionnaire d'événements
	private GestionnaireEvenements objGestionnaireEvenements;
	
	// Déclaration d'une référence vers le contr™leur de jeu
	private ControleurJeu objControleurJeu;
	
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
	// Cette variable va contenir le nom de la salle
	private final String strNomSalle;

	// Cette variable va contenir le mot de passe permettant d'accéder à la salle
	private final String strPassword;
	
	// Cette variable va contenir le nom d'utilisateur du créateur de cette salle
	private final String strCreatorUserName;
        
    // Contient le type de jeu (ex. mathEnJeu)
	private final String gameType;
	
	private GenerateurPartie gameFactory;
	
	//Room short description
	private String roomDescription;
	
	// Cet objet est une liste de numéros utilisés pour les tables (sert à 
	// générer de nouvelles tables)
	private TreeSet<Integer> lstNoTables;
	
	// Cet objet est une liste des joueurs qui sont présentement dans cette salle
	private TreeMap<String, JoueurHumain> lstJoueurs;
	
	// Cet objet est une liste des tables qui sont présentement dans cette salle
	private TreeMap<Integer, Table> lstTables;
	
	// Cet objet permet de déterminer les règles de jeu pour cette salle
	private Regles objRegles;
	
	// Date when room will be activated
	private Date beginDate;
	
	// Date untill room will be activ
	private Date endDate;
	
	// ID in DB.  
	private final int roomID;
	
	//default time for the room 
	private final int masterTime;
	
	//use all general categories or only room's specyfied categories
	private final boolean roomCategories;
	
	//specifyed room's categories
	private final ArrayList<Integer> categories;
	
	// last room number
	private int lastNumber;
	
	   
	
	/**
	 * Constructeur de la classe Salle qui permet d'initialiser les membres 
	 * privés de la salle. Ce constructeur a en plus un mot de passe permettant
	 * d'accéder à la salle.
	 * @param roomCategories 
	 * 
	 * @param GestionnaireBD gestionnaireBD : Le gestionnaire de base de données
	 * @param String nomSalle : Le nom de la salle
	 * @param String nomUtilisateurCreateur : Le nom d'utilisateur du créateur
	 * 										  de la salle
	 * @param String motDePasse : Le mot de passe
	 * @param Regles reglesSalle : Les règles de jeu pour la salle courante
	 */
	public Salle(String nomSalle, String nomUtilisateurCreateur, String motDePasse, 
				 Regles reglesSalle, ControleurJeu controleurJeu, String gameType, 
				 int roomID, Date beginDate, Date endDate, int masterTime, 
				 boolean roomCategories)
	{
		super();
		
		// Faire la référence vers le gestionnaire d'événements et le 
		// gestionnaire de base de données
		objGestionnaireEvenements = new GestionnaireEvenements();
		objGestionnaireBD = controleurJeu.obtenirGestionnaireBD();
		
		// Garder en mémoire le nom de la salle, le nom d'utilisateur du 
		// créateur de la salle et le mot de passe
		strNomSalle = nomSalle;
		 
		strCreatorUserName = nomUtilisateurCreateur;
		strPassword = motDePasse;
		//System.out.println(strPassword);
                
        // Type de jeu de la salle
        this.gameType = gameType;
        this.roomID = roomID;
        this.setBeginDate(beginDate);
        this.setEndDate(endDate);
        this.masterTime = masterTime;
        this.roomCategories = roomCategories;
        
        categories = new ArrayList<Integer>();
		
		// Créer une nouvelle liste de joueurs, de tables et de numéros
		lstJoueurs = new TreeMap <String, JoueurHumain>();
		lstTables = new TreeMap <Integer, Table>();
		lstNoTables = new TreeSet <Integer>();
		
		// Définir les règles de jeu pour la salle courante
		objRegles = reglesSalle;
                
        // Faire la référence vers le controleur de jeu
		setObjControleurJeu(controleurJeu);
		
		// Créer un thread pour le GestionnaireEvenements
		Thread threadEvenements = new Thread(objGestionnaireEvenements);
		
		// Démarrer le thread du gestionnaire d'événements
		threadEvenements.start();
		
		this.gameFactory = null;
		
		try {
			this.gameFactory = (GenerateurPartie) Class.forName("ServeurJeu.ComposantesJeu.GenerateurPartie" + gameType).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @return the beginDate
	 */
	public Date getBeginDate() {
		return beginDate;
	}

	
	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	
	/**
	 * @param beginDate the beginDate to set
	 */
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	

	/**
	 * Cette fonction permet de générer un nouveau numéro de table.
	 * 
	 * @return int : Le numéro de table généré
	 * 
	 * @synchronism Cette fonction n'a pas besoin d'être synchronisée, car 
	 * 				elle doit l'être par la fonction appelante. La 
	 * 				synchronisation devrait se faire sur la liste des tables.
	 */
	private int genererNoTable()
	{
		// Déclaration d'une variable qui va contenir le numéro de table
		// généré
		int intNoTable = getLastNumber() + 1;
				
		// Boucler tant qu'on n'a pas trouvé de numéro n'étant pas utilisé
		while (lstNoTables.contains(new Integer(intNoTable)) == true)
		{
			intNoTable++;
		}
		
		setLastNumber(intNoTable);
		
		return intNoTable;
	}
	
	/**
	 * Cette fonction permet de valider que le mot de passe pour entrer dans la
	 * salle est correct. On suppose suppose que le joueur n'est pas dans la
	 * salle courante. Cette fonction va avoir pour effet de connecter le joueur 
	 * dans la salle courante.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la salle
	 * @param String motDePasse : Le mot de passe pour entrer dans la salle
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return false : Le mot de passe pour entrer dans la salle n'est pas
	 * 				   le bon
	 * 		   true  : Le joueur a réussi à entrer dans la salle
	 * 
	 * @synchronism Cette fonction est synchronisée pour éviter que deux 
	 * 				puissent entrer ou quitter une salle en même temps.
	 * 				On n'a pas à s'inquiéter que le joueur soit modifié
	 * 				pendant le temps qu'on exécute cette fonction. De plus
	 * 				on n'a pas à revérifier que la salle existe bien (car
	 * 				elle ne peut être supprimée) et que le joueur n'est 
	 * 				pas toujours dans une autre salle (car le protocole
	 * 				ne peut pas exécuter plusieurs fonctions en même temps)
	 */
	public boolean entrerSalle(JoueurHumain joueur, String motDePasse, boolean doitGenererNoCommandeRetour)
	{
		// Si le mot de passe est le bon, alors on ajoute le joueur dans la liste
		// des joueurs de cette salle et on envoit un événement aux autres
		// joueurs de cette salle pour leur dire qu'il y a un nouveau joueur
		
		if (getStrPassword().equals(objGestionnaireBD.controlPWD(motDePasse)))
		{
		    // Empêcher d'autres thread de toucher à la liste des joueurs de 
		    // cette salle pendant l'ajout du nouveau joueur dans cette salle
		    synchronized (lstJoueurs)
		    {
				// Ajouter ce nouveau joueur dans la liste des joueurs de cette salle
				lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
				
				// Le joueur est maintenant entré dans la salle courante
				joueur.definirSalleCourante(this);
				
				// Si on doit générer le numéro de commande de retour, alors
				// on le génère, sinon on ne fait rien (ça devrait toujours
				// être vrai, donc on le génère tout le temps)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}

				// Préparer l'événement de nouveau joueur dans la salle. 
				// Cette fonction va passer les joueurs et créer un 
				// InformationDestination pour chacun et ajouter l'événement 
				// dans la file de gestion d'événements
				preparerEvenementJoueurEntreSalle(joueur.obtenirNomUtilisateur());
		    }
				    
		    objGestionnaireBD.fillUserLevels(joueur, this);
		   
			// On retourne vrai
			return true;
		}
		else
		{
			// On retourne faux
			return false;
		}
	}
	
	/**
	 * Cette méthode permet au joueur passé en paramètres de quitter la salle. 
	 * On suppose que le joueur est dans la salle et qu'il n'est pas en train
	 * de jouer dans aucune table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de quitter la salle
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * 
	 * @synchronism Cette fonction est synchronisée pour éviter que deux 
	 * 				puissent entrer ou quitter une salle en même temps.
	 * 				On n'a pas à s'inquiéter que le joueur soit modifié
	 * 				pendant le temps qu'on exécute cette fonction. De plus
	 * 				on n'a pas à revérifier que la salle existe bien (car
	 * 				elle ne peut être supprimée) et que le joueur n'est 
	 * 				pas toujours dans une autre salle (car le protocole
	 * 				ne peut pas exécuter plusieurs fonctions en même temps)
	 */
	public void quitterSalle(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean detruirePartieCourante)
	{
		//TODO: Peut-être va-t-il falloir ajouter une synchronisation ici
		// 		lorsque la commande sortir joueur de la table sera codée
		// Si le joueur est en train de jouer dans une table, alors
		// il doit quitter cette table avant de quitter la salle
		if (joueur.obtenirPartieCourante() != null)
		{
		    // Quitter la table courante avant de quitter la salle
		    joueur.obtenirPartieCourante().obtenirTable().quitterTable(joueur, false, detruirePartieCourante);
		}
	    
	    // Empêcher d'autres thread de toucher à la liste des joueurs de 
	    // cette salle pendant que le joueur quitte cette salle
	    synchronized (lstJoueurs)
	    {
			// Enlever le joueur de la liste des joueurs de cette salle
			lstJoueurs.remove(joueur.obtenirNomUtilisateur());
			
			// Le joueur est maintenant dans aucune salle
			joueur.definirSalleCourante(null);
			
			// Si on doit générer le numéro de commande de retour, alors
			// on le génère, sinon on ne fait rien (ça se peut que ce soit
			// faux)
			if (doitGenererNoCommandeRetour == true)
			{
				// Générer un nouveau numéro de commande qui sera 
			    // retourné au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

			// Préparer l'événement qu'un joueur a quitté la salle. 
			// Cette fonction va passer les joueurs et créer un 
			// InformationDestination pour chacun et ajouter l'événement 
			// dans la file de gestion d'événements
			preparerEvenementJoueurQuitteSalle(joueur.obtenirNomUtilisateur());	        
	    }
	}
	
	/**
	 * Cette méthode permet de créer une nouvelle table et d'y faire entrer le
	 * joueur qui en fait la demande. On suppose que le joueur n'est pas dans 
	 * aucune autre table.
	 * @param intNbColumns 
	 * @param intNbLines 
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de créer la table
	 * @param int tempsPartie : Le temps que doit durer la partie
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return int : Le numéro de la nouvelle table créée
	 * 
	 * @synchronism Cette fonction est synchronisée pour la liste des tables
	 * 				car on va ajouter une nouvelle table et il ne faut pas 
	 * 				qu'on puisse détruire une table ou obtenir la liste des
	 * 				tables pendant ce temps. On synchronise également la 
	 * 				liste des joueurs de la salle, car on va passer les 
	 * 				joueurs de la salle et leur envoyer un événement. La
	 * 				fonction entrerTable est synchronisée automatiquement.
	 */
	public int creerTable(JoueurHumain joueur, int tempsPartie, boolean doitGenererNoCommandeRetour, String name, int intNbLines, int intNbColumns)
	{
		// Déclaration d'une variable qui va contenir le numéro de la table
		int intNoTable;
		
	    // Empêcher d'autres thread de toucher à la liste des tables de 
	    // cette salle pendant la création de la table
	    synchronized (lstTables)
	    {
	    	
	    	// Créer une nouvelle table en passant les paramètres appropriés
	    	Table objTable = new Table( this, genererNoTable(), joueur, tempsPartie, name, intNbLines, intNbColumns);
	    		    	
	    	objTable.creation();
	    		    	
	    	// Ajouter la table dans la liste des tables
	    	lstTables.put(new Integer(objTable.obtenirNoTable()), objTable);
	    	
	    	// Ajouter le numéro de la table dans la liste des numéros de table
	    	lstNoTables.add(new Integer(objTable.obtenirNoTable()));
	    	
			// Si on doit générer le numéro de commande de retour, alors
			// on le génère, sinon on ne fait rien (ça devrait toujours
			// être vrai, donc on le génère tout le temps)
			if (doitGenererNoCommandeRetour == true)
			{
				// Générer un nouveau numéro de commande qui sera 
			    // retourné au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

		    // Empêcher d'autres thread de toucher à la liste des tables de 
		    // cette salle pendant la création de la table
		    synchronized (lstJoueurs)
		    {
				// Préparer l'événement de nouvelle table. 
				// Cette fonction va passer les joueurs et créer un 
				// InformationDestination pour chacun et ajouter l'événement 
				// dans la file de gestion d'événements
				preparerEvenementNouvelleTable(objTable.obtenirNoTable(), tempsPartie, joueur.obtenirNomUtilisateur(), objTable.getTableName());
		    }

		    // Entrer dans la table on ne fait rien avec la liste des 
		    // personnages
		    objTable.entrerTable(joueur, false, new TreeMap<String,Integer>(), new TreeMap<String, Integer>());
		    
		    // Garder le numéro de table pour le retourner
		    intNoTable = objTable.obtenirNoTable();
	    }
	    
	    return intNoTable;
	}
	
	/**
	 * Cette fonction permet au joueur d'entrer dans la table désirée. On 
	 * suppose que le joueur n'est pas dans aucune table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la table
	 * @param int noTable : Le numéro de la table dans laquelle entrer
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @param TreeMap listePersonnageJoueurs : La liste des joueurs dont la clé 
	 * 								est le nom d'utilisateur du joueur et le contenu 
	 * 								est le Id du joueur 
	 * 
	 * @return String : Succes : Le joueur est maintenant dans la table
	 * 		   			TableNonExistante : Le joueur a tenté d'entrer dans une
	 * 										table non existante
	 * 					TableComplete : Le joueur a tenté d'entrer dans une 
	 * 									table ayant déjà le maximum de joueurs
	 * 					PartieEnCours : Une partie est déjà en cours dans la 
	 * 									table désirée
	 * 
	 * @synchronism Cette fonction est synchronisée sur la liste des tables
	 * 				pour éviter qu'un joueur puisse commencer à quitter et 
	 * 				que le joueur courant débute son entrée dans la table 
	 * 				courante qui a des chances d'être détruite si le joueur 
	 * 				qui veut quitter est le dernier de la table.
	 */
	public String entrerTable(JoueurHumain joueur, int noTable, boolean doitGenererNoCommandeRetour, TreeMap<String, Integer> listePersonnageJoueurs, TreeMap<String, Integer> listeRoleJoueurs)
	{
	    // Déclaration d'une variable qui va contenir le résultat à retourner
	    // à la fonction appelante, soit les valeurs de l'énumération 
	    // ResultatEntreeTable
	    String strResultatEntreeTable;
	    
	    // Empêcher d'autres thread de toucher à la liste des tables de 
	    // cette salle pendant que le joueur entre dans la table
	    synchronized (lstTables)
	    {
			// Si la table n'existe pas dans la salle où se trouve le joueur, 
			// alors il y a une erreur
			if (lstTables.containsKey(new Integer(noTable)) == false)
			{
				// La table n'existe pas
				strResultatEntreeTable = ResultatEntreeTable.TableNonExistante;
			}
			// Si la table est complète, alors il y a une erreur (aucune 
			// synchronisation supplémentaire à faire car elle ne peut devenir 
			// complète ou ne plus l'être que par l'entrée ou la sortie d'un 
			// joueur dans la table. Or ces actions sont synchronisées avec 
			// lstTables, donc ça va.
			else if (((Table) lstTables.get(new Integer(noTable))).estComplete() == true)
			{
				// La table est complète
				strResultatEntreeTable = ResultatEntreeTable.TableComplete;
			}
			//TODO: Cette validation dépend de l'état de la partie (de la table)
			// 		et lorsque cette partie se terminera ou débutera, son état va changer,
			//		il va donc falloir revoir cette validation
			// Si la table n'est pas complète et une partie est en cours, 
			// alors il y a une erreur
			else if (((Table) lstTables.get(new Integer(noTable))).estCommencee() == true)
			{
				// Une partie est en cours
				strResultatEntreeTable = ResultatEntreeTable.PartieEnCours;
			}
			else
			{
				// Appeler la méthode permettant d'entrer dans la table
				((Table) lstTables.get(new Integer(noTable))).entrerTable(joueur, doitGenererNoCommandeRetour, listePersonnageJoueurs, listeRoleJoueurs);
				
				// Il n'y a eu aucun problème pour entrer dans la table
				strResultatEntreeTable = ResultatEntreeTable.Succes;
			}
	    }
	    
	    return strResultatEntreeTable;
	}

	/**
	 * Cette méthode permet de détruire la table passée en paramètres. 
	 * On suppose que la table n'a plus aucuns joueurs.
	 * 
	 * @param Table tableADetruire : La table à détruire
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée car elle l'est par
	 * 				la fonction qui l'appelle. On synchronise seulement
	 * 				la liste des joueurs de cette salle lorsque va venir
	 * 				le temps d'envoyer l'événement que la table est détruite
	 * 				aux joueurs de la salle. On n'a pas à s'inquiéter que la 
	 * 				table soit modifiée pendant le temps qu'on exécute cette 
	 * 				fonction, car il n'y a plus personne dans la table.
	 */
	public void detruireTable(Table tableADetruire)
	{
		Table t = (Table)lstTables.get( new Integer(tableADetruire.obtenirNoTable() ) );
		if( t != null )
		{
			t.destruction();
		}
		// Enlever la table de la liste des tables de cette salle
		lstTables.remove(new Integer(tableADetruire.obtenirNoTable()));
		
		// On enlève le numéro de la table dans la liste des numéros de table
		// pour le rendre disponible pour une autre table
		lstNoTables.remove(new Integer(tableADetruire.obtenirNoTable()));
		
		// Empêcher d'autres thread de toucher à la liste des joueurs de 
	    // cette salle pendant qu'on parcourt tous les joueurs de la salle
		// pour leur envoyer un événement
	    synchronized (lstJoueurs)
	    {
			// Préparer l'événement qu'une table a été détruite. 
			// Cette fonction va passer les joueurs et créer un 
			// InformationDestination pour chacun et ajouter l'événement 
			// dans la file de gestion d'événements
			preparerEvenementTableDetruite(tableADetruire.obtenirNoTable());	    	
	    }
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des joueurs se trouvant dans la
	 * salle courante. La vraie liste de joueurs est retournée.
	 * 
	 * @return TreeMap : La liste des joueurs se trouvant dans la salle courante
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				l'être par l'appelant de cette fonction tout dépendant
	 * 				du traitement qu'elle doit faire
	 */
	public TreeMap<String, JoueurHumain> obtenirListeJoueurs()
	{
		return lstJoueurs;
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des tables se trouvant dans la
	 * salle courante. La vraie liste est retournée.
	 * 
	 * @return TreeMap : La liste des tables de la salle courante
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				l'être par l'appelant de cette fonction tout dépendant
	 * 				du traitement qu'elle doit faire
	 */
	public TreeMap<Integer, Table> obtenirListeTables()
	{
		return lstTables;
	}
	
	/**
	 * Cette méthode permet de préparer l'événement de l'entrée d'un joueur 
	 * dans la salle courante. Cette méthode va passer tous les joueurs 
	 * de cette salle et pour ceux devant être avertis (tous sauf le joueur 
	 * courant passé en paramètre), on va obtenir un numéro de commande, on 
	 * va créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient d'entrer dans la salle
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (entrerSalle).
	 */
	private void preparerEvenementJoueurEntreSalle(String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur est entré dans la salle
	    EvenementJoueurEntreSalle joueurEntreSalle = new EvenementJoueurEntreSalle(nomUtilisateur);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque élément est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient d'entrer dans la salle, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
			    joueurEntreSalle.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurEntreSalle);
	}

	/**
	 * Cette méthode permet de préparer l'événement du depart d'un joueur 
	 * de la salle courante. Cette méthode va passer tous les joueurs 
	 * de cette salle et pour ceux devant être avertis (tous sauf le joueur 
	 * courant passé en paramètre), on va obtenir un numéro de commande, on 
	 * va créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de quitter la salle
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (quitterSalle).
	 */
	private void preparerEvenementJoueurQuitteSalle(String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur a quitté la salle
	    EvenementJoueurQuitteSalle joueurQuitteSalle = new EvenementJoueurQuitteSalle(nomUtilisateur);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque élément est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de quitter la salle, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
			    joueurQuitteSalle.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurQuitteSalle);
	}
	
	/**
	 * Cette méthode permet de préparer l'événement de la création d'une 
	 * nouvelle table dans la salle courante. Cette méthode va passer tous 
	 * les joueurs de cette salle et pour ceux devant être avertis (tous 
	 * sauf le joueur courant passé en paramètre), on va obtenir un numéro 
	 * de commande, on va créer un InformationDestination et on va ajouter 
	 * l'événement dans la file d'événements du gestionnaire d'événements. 
	 * Lors de l'appel de cette fonction, la liste des joueurs est 
	 * synchronisée.
	 *
	 * @param int noTable : Le numéro de la table créé
	 * @param int tempsPartie : Le temps de la partie
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  a créé la table
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (creerTable).
	 */
	private void preparerEvenementNouvelleTable(int noTable, int tempsPartie, String nomUtilisateur, String tablName)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'une table a été créée
	    EvenementNouvelleTable nouvelleTable = new EvenementNouvelleTable(noTable, tempsPartie, tablName);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque élément est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de créer la table, alors on peut envoyer un 
			// événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
				nouvelleTable.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(nouvelleTable);
	}
	
	/**
	 * Cette méthode permet de préparer l'événement de la destruction d'une 
	 * table dans la salle courante. Cette méthode va passer tous les joueurs 
	 * de cette salle et pour ceux devant être avertis (tous sauf le joueur 
	 * courant passé en paramètre), on va obtenir un numéro de commande, on 
	 * va créer un InformationDestination et on va ajouter l'événement dans 
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel de 
	 * cette fonction, la liste des joueurs est synchronisée.
	 *
	 * @param int noTable : Le numéro de la table détruite
	 * 
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (detruireTable).
	 */
	private void preparerEvenementTableDetruite(int noTable)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'une table a été créée
	    EvenementTableDetruite tableDetruite = new EvenementTableDetruite(noTable);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque élément est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
		    // Obtenir un numéro de commande pour le joueur courant, créer 
		    // un InformationDestination et l'ajouter à l'événement
			tableDetruite.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
		            											objJoueur.obtenirProtocoleJoueur()));
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(tableDetruite);
	}

	/**
	 * Cette fonction permet de retourner le nom de la salle courante.
	 * If the room has two languages on return name in the needed lang
	 * if not on return the name in existing lang, or if the lang isn't 
	 * known on return the existing string 
	 * @return String : Le nom de la salle
	 */
	public String getRoomName(String lang)
	{
		if(lang.equals(""))
			return strNomSalle;
		
		boolean exist = false; 
		for(int i = 0; i < strNomSalle.length(); i++)
		{
			if(strNomSalle.charAt(i) == '/')
				exist = true;
		}
	
		if(exist)
		{
		   StringTokenizer nomSalle = new StringTokenizer(strNomSalle, "/");
		   String nomFr = nomSalle.nextToken().trim();
		   String nomEng = nomSalle.nextToken().trim();
		   return lang.equalsIgnoreCase("fr")? nomFr : nomEng;
		}
		return strNomSalle;
	}//end methode
	
	public Regles getRegles()
	{
	   return objRegles;
	}
	
	/**
	 * Cette fonction permet de déterminer si la salle possède un mot de passe
	 * pour y accéder ou non.
	 * 
	 * @return boolean : true si la salle est protégée par un mot de passe
	 * 					 false sinon
	 */
	public boolean protegeeParMotDePasse()
	{
		return !(getStrPassword() == null || getStrPassword().equals(""));
	}


	public String getGameType()
	{
		return gameType;
	}


	
	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}

	// return room description
	public String getRoomDescription(String lang) {
		if(lang.equals(""))
			return roomDescription;

		boolean exist = false; 
		for(int i = 0; i < roomDescription.length(); i++)
		{
			if(roomDescription.charAt(i) == '/')
				exist = true;
		}

		if(exist)
		{
			StringTokenizer descRoom = new StringTokenizer(roomDescription, "/");
			String descFr = descRoom.nextToken().trim();
			String descEng = descRoom.nextToken().trim();
			return lang.equalsIgnoreCase("fr")? descFr : descEng;
		}
		return roomDescription;
	}

	/*
	public void setStrCreatorUserName(String strCreatorUserName) {
		this.strCreatorUserName = strCreatorUserName;
	}*/

	public String getStrCreatorUserName() {
		return strCreatorUserName;
	}

	/*
	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}*/

	public int getRoomID() {
		return roomID;
	}

	/*
	public void setMasterTime(int masterTime) {
		this.masterTime = masterTime;
	}*/

	public int getMasterTime() {
		return masterTime;
	}

	public boolean isRoomCategories() {
		return roomCategories;
	}

	public ArrayList<Integer> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories() {
		Categories[] catValues = Categories.values();
        for(int i = 0; i < catValues.length; i++)
		{
			categories.add(catValues[i].getCode());
		}
			
	}// end methode
	
	public void setCategories(String categoriesString)
	{

		if(categoriesString != null){
			StringTokenizer cat = new StringTokenizer(categoriesString, ":");
			while(cat.hasMoreTokens())
			{
				categories.add(Integer.parseInt(cat.nextToken()));
			}
		}
		ListIterator<Integer> it = categories.listIterator();	
		while(it.hasNext())
			System.out.println(it.next());
	}// end mathode

	private void setLastNumber(int lastNumber) {
		this.lastNumber = lastNumber;
		
		if(this.lastNumber > 999)
			this.lastNumber = 0;
	}

	private int getLastNumber() {
		return lastNumber;
	}

	public String getStrPassword() {
		return strPassword;
	}

	public void setObjControleurJeu(ControleurJeu objControleurJeu) {
		this.objControleurJeu = objControleurJeu;
	}

	public ControleurJeu getObjControleurJeu() {
		return objControleurJeu;
	}

	public GenerateurPartie getGameFactory() {
		return gameFactory;
	}
	
}// end class 
