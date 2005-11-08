package ServeurJeu.ComposantesJeu;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import Enumerations.RetourFonctions.ResultatEntreeTable;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Evenements.EvenementJoueurEntreSalle;
import ServeurJeu.Evenements.EvenementJoueurQuitteSalle;
import ServeurJeu.Evenements.EvenementNouvelleTable;
import ServeurJeu.Evenements.EvenementTableDetruite;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;

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
	
	// Cette variable va contenir le nom de la salle
	private String strNomSalle;

	// Cette variable va contenir le mot de passe permettant d'accéder à la salle
	private String strMotDePasse;
	
	// Cette variable va contenir le nom d'utilisateur du créateur de cette salle
	private String strNomUtilisateurCreateur;
	
	// Cet objet est une liste de numéros utilisés pour les tables (sert à 
	// générer de nouvelles tables)
	private TreeSet lstNoTables;
	
	// Cet objet est une liste des joueurs qui sont présentement dans cette salle
	private TreeMap lstJoueurs;
	
	// Cet objet est une liste des tables qui sont présentement dans cette salle
	private TreeMap lstTables;
	
	// Cet objet permet de déterminer les règles de jeu pour cette salle
	private Regles objRegles;
	
	/**
	 * Constructeur de la classe Salle qui permet d'initialiser les membres 
	 * privés de la salle. Ce constructeur a en plus un mot de passe permettant
	 * d'accéder à la salle.
	 * 
	 * @param GestionnaireEvenements gestionnaireEv : Le gestionnaire d'événements
	 * @param String nomSalle : Le nom de la salle
	 * @param String nomUtilisateurCreateur : Le nom d'utilisateur du créateur
	 * 										  de la salle
	 * @param String motDePasse : Le mot de passe
	 * @param Regles reglesSalle : Les règles de jeu pour la salle courante
	 */
	public Salle(GestionnaireEvenements gestionnaireEv, String nomSalle, 
	        	 String nomUtilisateurCreateur, String motDePasse, Regles reglesSalle) 
	{
		super();
		
		// Faire la référence vers le gestionnaire d'événements
		objGestionnaireEvenements = gestionnaireEv;
		
		// Garder en mémoire le nom de la salle, le nom d'utilisateur du 
		// créateur de la salle et le mot de passe
		strNomSalle = nomSalle;
		strNomUtilisateurCreateur = nomUtilisateurCreateur;
		strMotDePasse = motDePasse;
		
		// Créer une nouvelle liste de joueurs, de tables et de numéros
		lstJoueurs = new TreeMap();
		lstTables = new TreeMap();
		lstNoTables = new TreeSet();
		
		// Définir les règles de jeu pour la salle courante
		objRegles = reglesSalle;
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
		int intNoTable = 1;
		
		// Boucler tant qu'on n'a pas trouvé de numéro n'étant pas utilisé
		while (lstNoTables.contains(new Integer(intNoTable)) == true)
		{
			intNoTable++;
		}
		
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
		if (strMotDePasse.equals(motDePasse))
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
	public void quitterSalle(JoueurHumain joueur, boolean doitGenererNoCommandeRetour)
	{
		//TODO: Peut-être va-t-il falloir ajouter une synchronisation ici
		// 		lorsque la commande sortir joueur de la table sera codée
		// Si le joueur est en train de jouer dans une table, alors
		// il doit quitter cette table avant de quitter la salle
		if (joueur.obtenirPartieCourante() != null)
		{
		    // Quitter la table courante avant de quitter la salle
		    joueur.obtenirPartieCourante().obtenirTable().quitterTable(joueur, false);
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
	public int creerTable(JoueurHumain joueur, int tempsPartie, boolean doitGenererNoCommandeRetour)
	{
		// Déclaration d'une variable qui va contenir le numéro de la table
		int intNoTable;
		
	    // Empêcher d'autres thread de toucher à la liste des tables de 
	    // cette salle pendant la création de la table
	    synchronized (lstTables)
	    {
	    	// Créer une nouvelle table en passant les paramètres appropriés
	    	Table objTable = new Table(objGestionnaireEvenements, this, genererNoTable(), joueur.obtenirNomUtilisateur(), tempsPartie, objRegles);
	    	
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
				preparerEvenementNouvelleTable(objTable.obtenirNoTable(), tempsPartie, joueur.obtenirNomUtilisateur());
		    }

		    // Entrer dans la table on ne fait rien avec la liste des 
		    // personnages
		    objTable.entrerTable(joueur, false, new TreeMap());
		    
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
	 * 								est le Id du personnage choisi 
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
	public String entrerTable(JoueurHumain joueur, int noTable, boolean doitGenererNoCommandeRetour, TreeMap listePersonnageJoueurs)
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
				((Table) lstTables.get(new Integer(noTable))).entrerTable(joueur, doitGenererNoCommandeRetour, listePersonnageJoueurs);
				
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
	public TreeMap obtenirListeJoueurs()
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
	public TreeMap obtenirListeTables()
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
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
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
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
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
	private void preparerEvenementNouvelleTable(int noTable, int tempsPartie, String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'une table a été créée
	    EvenementNouvelleTable nouvelleTable = new EvenementNouvelleTable(noTable, tempsPartie);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
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
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
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
	 * 
	 * @return String : Le nom de la salle
	 */
	public String obtenirNomSalle()
	{
		return strNomSalle;
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
		return !(strMotDePasse == null || strMotDePasse.equals(""));
	}
}
