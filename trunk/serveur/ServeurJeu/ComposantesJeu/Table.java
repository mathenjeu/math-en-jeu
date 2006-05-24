package ServeurJeu.ComposantesJeu;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.awt.Point;
import org.w3c.dom.Element;

import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Evenements.EvenementJoueurEntreTable;
import ServeurJeu.Evenements.EvenementJoueurQuitteTable;
import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementPartieDemarree;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ClassesUtilitaires.GenerateurPartie;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.Temps.*;
import ServeurJeu.Evenements.EvenementSynchroniserTemps;
import ServeurJeu.Evenements.EvenementPartieTerminee;

/**
 * @author Jean-François Brind'Amour
 */
public class Table implements ObservateurSynchroniser, ObservateurMinuterie
{
	// Déclaration d'une référence vers le gestionnaire d'événements
	private GestionnaireEvenements objGestionnaireEvenements;
	
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
	// Déclaration d'une référence vers la salle parente dans laquelle se 
	// trouve cette table 
	private Salle objSalle;
	
	// Cette variable va contenir le numéro de la table
	private int intNoTable;
	
	// Déclaration d'une constante qui définit le nombre maximal de joueurs 
	// dans une table
	private final int MAX_NB_JOUEURS = 4;
	
	private int intNbJoueurDemande; 
	
	// Cette variable va contenir le nom d'utilisateur du créateur de cette table
	private String strNomUtilisateurCreateur;
	
	// Déclaration d'une variable qui va garder le temps total défini pour 
	// cette table
	private int intTempsTotal;
	
	// Déclaration d'une variable qui va garder le temps restant (au départ 
	// il vaut la même chose que intTempsTotal)
	//private int intTempsRestant;
	
	// Cet objet est une liste des joueurs qui sont présentement sur cette table
	private TreeMap lstJoueurs;
	
	// Cet objet est une liste des joueurs qui attendent de joueur une partie
	private TreeMap lstJoueursEnAttente;
	
	// Déclaration d'une variable qui va permettre de savoir si la partie est 
	// commencée ou non
	private boolean bolEstCommencee;
	   
	// Déclaration d'une variable qui va permettre d'arrêter la partie en laissant
	// l'état de la partie à "commencée" tant que les joueurs sont à l'écran des pointages
	private boolean bolEstArretee;
	
	// Déclaration d'un tableau à 2 dimensions qui va contenir les informations 
	// sur les cases du jeu
	private Case[][] objttPlateauJeu;
	
	// Cet objet permet de déterminer les règles de jeu pour cette table
	private Regles objRegles;
	
	private GestionnaireTemps objGestionnaireTemps;
	private TacheSynchroniser objTacheSynchroniser;
	private Minuterie objMinuterie;
	

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
	public Table(GestionnaireEvenements gestionnaireEv, GestionnaireBD gestionnaireBD, 
				 Salle salleParente, int noTable, String nomUtilisateurCreateur, 
				 int tempsPartie, Regles reglesTable,
				 GestionnaireTemps gestionnaireTemps, TacheSynchroniser tacheSynchroniser ) 
	{
		super();
		
		// Faire la référence vers le gestionnaire d'événements et le 
		// gestionnaire de base de données
		objGestionnaireEvenements = gestionnaireEv;
		objGestionnaireBD = gestionnaireBD;
		
		// Garder en mémoire la référence vers la salle parente, le numéro de 
		// la table, le nom d'utilisateur du créateur de la table et le temps
		// total d'une partie
		objSalle = salleParente;
		intNoTable = noTable;
		strNomUtilisateurCreateur = nomUtilisateurCreateur;
		intTempsTotal = tempsPartie;
	//	intTempsRestant = tempsPartie;
		
		// Créer une nouvelle liste de joueurs
		lstJoueurs = new TreeMap();
		lstJoueursEnAttente = new TreeMap();
		
		// Au départ, aucune partie ne se joue sur la table
		bolEstCommencee = false;
		bolEstArretee = true;
		intNbJoueurDemande = MAX_NB_JOUEURS;//TODO intNbJoueurDemande = intNbJoueur; validation avec MAX_NB_JOUEURS
		
		// Définir les règles de jeu pour la salle courante
		objRegles = reglesTable;
		
		// Initialiser le plateau de jeu à null
		objttPlateauJeu = null;
		
		objGestionnaireTemps = gestionnaireTemps;
		objTacheSynchroniser = tacheSynchroniser;

	}
	
	public void creation()
	{

	}
	
	public void destruction()
	{
		arreterPartie();
	}
	
	/**
	 * Cette fonction permet au joueur d'entrer dans la table courante. 
	 * On suppose que le joueur n'est pas dans une autre table, que la table 
	 * courante n'est pas complète et qu'il n'y a pas de parties en cours. 
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
	 * 				  joueurs puissent entrer ou quitter la table en même temps.
	 * 				  On n'a pas à s'inquiéter que le joueur soit modifié
	 * 				  pendant le temps qu'on exécute cette fonction. De plus
	 * 				  on n'a pas à revérifier que la table existe bien (car
	 * 				  elle ne peut être supprimée en même temps qu'un joueur 
	 * 				  entre dans la table), qu'elle n'est pas complète ou 
	 * 				  qu'une partie est en cours (car toutes les fonctions 
	 * 				  permettant de changer ça sont synchronisées).
	 */
	public void entrerTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, TreeMap listePersonnageJoueurs)  throws NullPointerException
	{
	    // Empêcher d'autres thread de toucher à la liste des joueurs de 
	    // cette table pendant l'ajout du nouveau joueur dans cette table
	    synchronized (lstJoueurs)
	    {
	    	// Remplir la liste des personnages choisis
	    	remplirListePersonnageJoueurs(listePersonnageJoueurs);
	    	
			// Ajouter ce nouveau joueur dans la liste des joueurs de cette table
			lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
			
			// Le joueur est maintenant entré dans la table courante (il faut
			// créer un objet InformationPartie qui va pointer sur la table
			// courante)
			joueur.definirPartieCourante(new InformationPartie(objGestionnaireEvenements, objGestionnaireBD, joueur, this));
			
			// Si on doit générer le numéro de commande de retour, alors
			// on le génère, sinon on ne fait rien
			if (doitGenererNoCommandeRetour == true)
			{
				// Générer un nouveau numéro de commande qui sera 
			    // retourné au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

			// Empêcher d'autres thread de toucher à la liste des joueurs de 
		    // cette salle pendant qu'on parcourt tous les joueurs de la salle
			// pour leur envoyer un événement
		    synchronized (objSalle.obtenirListeJoueurs())
		    {
				// Préparer l'événement de nouveau joueur dans la table. 
				// Cette fonction va passer les joueurs et créer un 
				// InformationDestination pour chacun et ajouter l'événement 
				// dans la file de gestion d'événements
				preparerEvenementJoueurEntreTable(joueur.obtenirNomUtilisateur());		    	
		    }
	    }
	}

	/**
	 * Cette méthode permet au joueur passé en paramètres de quitter la table. 
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
	 * 				  joueurs entrent ou quittent une table en même temps.
	 * 				  On n'a pas à s'inquiéter que le joueur soit modifié
	 * 				  pendant le temps qu'on exécute cette fonction. Si on 
	 * 				  inverserait les synchronisations, ça pourrait créer un 
	 * 				  deadlock avec les personnes entrant dans la salle.
	 */
	public void quitterTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour)
	{
	    // Empêcher d'autres thread de toucher à la liste des tables de 
	    // cette salle pendant que le joueur quitte cette table
	    synchronized (objSalle.obtenirListeTables())
	    {
		    // Empêcher d'autres thread de toucher à la liste des joueurs de 
		    // cette table pendant que le joueur quitte cette table
		    synchronized (lstJoueurs)
		    {
		    	// Enlever le joueur de la liste des joueurs de cette table
				lstJoueurs.remove(joueur.obtenirNomUtilisateur());
				
				// Le joueur est maintenant dans aucune table
				joueur.definirPartieCourante(null);
				
				// Si on doit générer le numéro de commande de retour, alors
				// on le génère, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}

				// Empêcher d'autres thread de toucher à la liste des joueurs de 
			    // cette salle pendant qu'on parcourt tous les joueurs de la salle
				// pour leur envoyer un événement
			    synchronized (objSalle.obtenirListeJoueurs())
			    {
					// Préparer l'événement qu'un joueur a quitté la table. 
					// Cette fonction va passer les joueurs et créer un 
					// InformationDestination pour chacun et ajouter l'événement 
					// dans la file de gestion d'événements
					preparerEvenementJoueurQuitteTable(joueur.obtenirNomUtilisateur());
			    }

			    // S'il ne reste aucuns joueurs dans la table, alors on doit 
			    // détruire la table
			    if (lstJoueurs.size() == 0)
			    {
			    	//Arreter le gestionnaire de temps
			    	//objGestionnaireTemps.arreterGestionnaireTemps();
			    	// Détruire la table courante et envoyer les événements 
			    	// appropriés
			    	objSalle.detruireTable(this);
			    }
		    }
		}
	}
	
	/**
	 * Cette méthode permet au joueur passé en paramètres de démarrer la partie. 
	 * On suppose que le joueur est dans la table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de démarrer la partie
	 * @param int idPersonnage : Le numéro Id du personnage choisi par le joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return String : Succes : si le joueur est maintenant en attente
	 * 					DejaEnAttente : si le joueur était déjà en attente
	 * 					PartieEnCours : si une partie était en cours
	 * 
	 * Synchronisme : Cette fonction est synchronisée sur la liste des joueurs 
	 * 				  en attente, car il se peut qu'on ajouter ou retirer des
	 * 				  joueurs de la liste en attente en même temps. On n'a pas
	 * 				  à s'inquiéter que le même joueur soit mis dans la liste 
	 * 				  des joueurs en attente par un autre thread.
	 */
	public String demarrerPartie(JoueurHumain joueur, int idPersonnage, boolean doitGenererNoCommandeRetour)
	{
		// Cette variable va permettre de savoir si le joueur est maintenant
		// attente ou non
		String strResultatDemarrerPartie;
		
	    // Empêcher d'autres thread de toucher à la liste des joueurs en attente 
	    // de cette table pendant que le joueur tente de démarrer la partie
	    synchronized (lstJoueursEnAttente)
	    {
	    	// Si une partie est en cours alors on va retourner PartieEnCours
	    	if (bolEstCommencee == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
	    	}
	    	// Sinon si le joueur est déjà en attente, alors on va retourner 
	    	// DejaEnAttente
	    	else if (lstJoueursEnAttente.containsKey(joueur.obtenirNomUtilisateur()) == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.DejaEnAttente;
	    	}
	    	else
	    	{
	    		// La commande s'est effectuée avec succès
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.Succes;
	    		
	    		// Ajouter le joueur dans la liste des joueurs en attente
				lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);
				
				// Garder en mémoire le Id du personnage choisi par le joueur
				joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);
				
	    		// Si on doit générer le numéro de commande de retour, alors
				// on le génère, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// Générer un nouveau numéro de commande qui sera 
				    // retourné au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}
				
				// Empêcher d'autres thread de toucher à la liste des joueurs de 
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
					// Créer une nouvelle liste qui va garder les points des 
					// cases libres (n'ayant pas d'objets dessus)
					Vector lstPointsCaseLibre = new Vector();
					
					// Créer un tableau de points qui va contenir la position 
					// des joueurs
					Point[] objtPositionsJoueurs;
					
					// Création d'une nouvelle liste dont la clé est le nom 
					// d'utilisateur du joueur et le contenu est un point 
					// représentant la position du joueur
					TreeMap lstPositionsJoueurs = new TreeMap();
					
					//TODO: Peut-être devoir synchroniser cette partie, il 
					//      faut voir avec les autres bouts de code qui 
					// 		vérifient si la partie est commencée (c'est OK 
					//		pour entrerTable)
					// Changer l'état de la table pour dire que maintenant une 
					// partie est commencée
					bolEstCommencee = true;
		            
		            // Change l'état de la table pour dire que la partie
		            // n'est pas arrêtée (note: bolEstCommencee restera à true
		            // pendant que les joueurs sont à l'écran de pointage)
		            bolEstArretee = false;
		            
					// Générer le plateau de jeu selon les règles de la table et 
					// garder le plateau en mémoire dans la table
					objttPlateauJeu = GenerateurPartie.genererPlateauJeu(objRegles, intTempsTotal, lstPointsCaseLibre);
					
					// Obtenir la position des joueurs de cette table
					objtPositionsJoueurs = GenerateurPartie.genererPositionJoueurs(intNbJoueurDemande, lstPointsCaseLibre);
					
					// Créer un ensemble contenant tous les tuples de la liste 
					// lstJoueursEnAttente (chaque élément est un Map.Entry)
					Set lstEnsembleJoueurs = lstJoueursEnAttente.entrySet();
					
					// Obtenir un itérateur pour l'ensemble contenant les personnages
					Iterator objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();
					
					// Passer toutes les positions des joueurs et les définir
					for (int i = 0; i < objtPositionsJoueurs.length; i++)
					{
						// Comme les positions sont générées aléatoirement, on 
						// se fou un peu duquel on va définir la position en 
						// premier, on va donc passer simplement la liste des 
						// joueurs
						// Créer une référence vers le joueur courant 
					    // dans la liste (pas besoin de vérifier s'il y en a un 
						// prochain, car on a généré la position des joueurs 
						// selon cette liste
						JoueurHumain objJoueur = (JoueurHumain) (((Map.Entry)(objIterateurListeJoueurs.next())).getValue());
						
						// Définir la position du joueur courant
						objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[i]);
						
						// Ajouter la position du joueur dans la liste
						lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[i]);
					}
					
					// On peut maintenant vider la liste des joueurs en attente
					// car elle ne nous sert plus à rien
					lstJoueursEnAttente.clear();
					
					// Empêcher d'autres thread de toucher à la liste des joueurs de 
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
				}
	    	}
		}
	    
	    return strResultatDemarrerPartie;
	}
	
	public void arreterPartie()
	{
		if( bolEstArretee == false )
		{
			bolEstArretee = true;
			objTacheSynchroniser.enleverObservateur( this );
			objGestionnaireTemps.enleverTache( objMinuterie );
			objMinuterie = null;
			synchronized (lstJoueurs)
		    {
				preparerEvenementPartieTerminee();
				Iterator it = lstJoueurs.values().iterator();
				while( it.hasNext() )
				{
					//Mettre a jour les donnees des joueurs
					//TODO : si partie pas completee???
					JoueurHumain joueur = (JoueurHumain)it.next();
					objGestionnaireBD.mettreAJourJoueur( joueur, intTempsTotal );
					//TODO le temps total si partie pas terminée ???
				}
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
	 * 				  l'être par l'appelant de cette fonction tout dépendant
	 * 				  du traitement qu'elle doit faire
	 */
	public TreeMap obtenirListeJoueurs()
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
	 * 				  l'être par l'appelant de cette fonction tout dépendant
	 * 				  du traitement qu'elle doit faire
	 */
	public TreeMap obtenirListeJoueursEnAttente()
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
	 * Cette fonction permet de déterminer si la table est complète ou non 
	 * (elle est complète si le nombre de joueurs dans cette table égale le 
	 * nombre de joueurs maximum par table).
	 * 
	 * @return boolean : true si la table est complète
	 * 					 false sinon
	 * 
	 * Synchronisme : Cette fonction est synchronisée car il peut s'ajouter de
	 * 				  nouveaux joueurs ou d'autres peuvent quitter pendant la 
	 * 				  vérification.
	 */
	public boolean estComplete()
	{
	    // Empêcher d'autres Thread de toucher à la liste des joueurs de cette
	    // table pendant qu'on fait la vérification (un TreeMap n'est pas 
	    // synchronisé)
	    synchronized (lstJoueurs)
	    {
			// Si la taille de la liste de joueurs égale le nombre maximal de 
			// joueurs alors la table est complète, sinon elle ne l'est pas
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
	 * Cette fonction retourne les règles pour la table courante.
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
	 * où les clés seront le nom d'utilisateur du joueur et le contenu le 
	 * numéro du personnage. On suppose que le joueur courant n'est pas 
	 * encore dans la liste.
	 *  
	 * @param TreeMap listePersonnageJoueurs : La liste des personnages 
	 * 										   pour chaque joueur
	 * @throws NullPointerException : Si la liste des personnages est à nulle
	 */
	private void remplirListePersonnageJoueurs(TreeMap listePersonnageJoueurs) throws NullPointerException
	{
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Ajouter le joueur dans la liste des personnages (il se peut que 
			// le joueur n'aie pas encore de personnages, alors le id est 0)
			listePersonnageJoueurs.put(objJoueur.obtenirNomUtilisateur(), new Integer(objJoueur.obtenirPartieCourante().obtenirIdPersonnage()));
		}
		
		// Déclaration d'un compteur
		int i = 1;
		
		// Boucler tant qu'on n'a pas atteint le nombre maximal de 
		// joueurs moins le joueur courant car on ne le met pas dans la liste
		while (listePersonnageJoueurs.size() < MAX_NB_JOUEURS - 1)
		{
			// On ajoute un joueur inconnu ayant le personnage 0
			listePersonnageJoueurs.put("Inconnu" + Integer.toString(i), new Integer(0));
			
			i++;
		}
	}

	/**
	 * Cette méthode permet de préparer l'événement de l'entrée d'un joueur 
	 * dans la table courante. Cette méthode va passer tous les joueurs 
	 * de la salle courante et pour ceux devant être avertis (tous sauf le 
	 * joueur courant passé en paramètre), on va obtenir un numéro de commande, 
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
	private void preparerEvenementJoueurEntreTable(String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur est entré dans la table
	    EvenementJoueurEntreTable joueurEntreTable = new EvenementJoueurEntreTable(intNoTable, nomUtilisateur);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la salle (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = objSalle.obtenirListeJoueurs().entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
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
	 * de la salle courante et pour ceux devant être avertis (tous sauf le 
	 * joueur courant passé en paramètre), on va obtenir un numéro de commande, 
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
		Set lstEnsembleJoueurs = objSalle.obtenirListeJoueurs().entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
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
	 * de la table courante et pour ceux devant être avertis (tous sauf le 
	 * joueur courant passé en paramètre), on va obtenir un numéro de commande, 
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
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
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
	private void preparerEvenementPartieDemarree(TreeMap listePositionJoueurs)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs de la table qu'un joueur a démarré une partie
	    EvenementPartieDemarree partieDemarree = new EvenementPartieDemarree(intTempsTotal, listePositionJoueurs, objttPlateauJeu);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

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
	
	private void preparerEvenementSynchroniser()
	{
		//Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs de la table
	    EvenementSynchroniserTemps synchroniser = new EvenementSynchroniserTemps( objMinuterie.obtenirTempsActuel() );
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

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
	
	private void preparerEvenementPartieTerminee()
	{
//		Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs de la table
	    EvenementPartieTerminee partieTerminee = new EvenementPartieTerminee( lstJoueurs );
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

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
		arreterPartie();
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
}
