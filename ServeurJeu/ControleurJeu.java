package ServeurJeu;

import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import Enumerations.RetourFonctions.ResultatAuthentification;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Communications.GestionnaireCommunication;
import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Evenements.EvenementJoueurDeconnecte;
import ServeurJeu.Evenements.EvenementJoueurConnecte;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.Monitoring.TacheLogMoniteur;
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.TacheSynchroniser;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.ComposantesJeu.Joueurs.ParametreIA;
import ServeurJeu.Configuration.GestionnaireMessages;



//TODO: Si un jour on doit modifier le nom d'utilisateur d'un joueur pendant 
//      le jeu, il va falloir ajouter des synchronisation à chaque fois qu'on 
//      fait des vérifications avec le nom de l'utilisateur.
/**
 * Note importante concernant le traitement des commandes par le 
 * ProtocoleJoueur : Deux fonctions d'un même protocole ne peuvent pas être
 * traitées en même temps car si le ProtocoleJoueur est en train d'en traiter
 * une, alors il n'est plus à l'écoute pour en recevoir une autre. Pour en 
 * traiter une autre, il doit attendre que le traitement de la première soit
 * terminé et qu'elle retourne une valeur au client. Un autre protocole ne peut
 * pas TODO (pour l'instant) exécuter une fonction d'un autre protocole, la 
 * seule chose qui peut se produire est qu'un protocole envoit des événements
 * à d'autres joueurs par leur ProtocoleJoueur, mais aucune fonction n'est
 * exécutée. TODO Il faut peut-être vérifier les conditions pour envoyer
 * l'événement à un joueur, car elles pourraient accéder à des données 
 * importantes du joueur ou du protocole du joueur. Même si le 
 * VerificateurConnexions tente d'arrêter un protocole qui est en train de 
 * traiter une commande, c'est le socket du protocole qui est fermé, et la
 * déconnexion du joueur va s'effectuer si on veut lire ou écrire sur le
 * socket. Cela veut donc dire qu'on n'a pas à valider que la même fonction
 * puisse être appelée pour le même protocole et joueur. 
 *  
 * @author Jean-François Brind'Amour
 */
public class ControleurJeu 
{
        // Cette modeDebug est vraie, toute reponse des joueurs sera bonne, et
        // on affichera dans la console des informations sur les communications
    public static boolean modeDebug;
        
	static private Logger objLogger = Logger.getLogger( ControleurJeu.class );
	
	// Cet objet permet de gérer toutes les interactions avec la base de données
	private GestionnaireBD objGestionnaireBD;
	
	// Cet objet permet de gérer toutes les communications entre le serveur et
	// les clients (les joueurs)
	private GestionnaireCommunication objGestionnaireCommunication;
	
	// Cet objet permet de gérer tous les événements devant être envoyés du
	// serveur aux clients (l'événement ping n'est pas géré par ce gestionnaire)
	private GestionnaireEvenements objGestionnaireEvenements;
	
	private TacheSynchroniser objTacheSynchroniser;
	
	private GestionnaireTemps objGestionnaireTemps;
	
	
	// Cet objet est une liste des joueurs qui sont connectés au serveur de jeu 
	// (cela inclus les joueurs dans les salles ainsi que les joueurs jouant
	// présentement dans des tables de jeu)
	private TreeMap lstJoueursConnectes;
	
    
    // Déclaration d'une variable pour contenir une liste des joueurs
    // qui ont étés déconnectés et qui étaient en train de joueur une partie
    private TreeMap lstJoueursDeconnectes;
	
	// Cet objet est une liste des salles créées qui se trouvent dans le serveur
	// de jeu. Chaque élément de cette liste a comme clé le nom de la salle
	private TreeMap lstSalles;
	
	// Déclaration de l'objet Espion qui va inscrire des informationsà proppos
	// du serveur en parallète
	//private Espion objEspion;
	
	// Déclaration d'un objet random pour générer des nombres aléatoires
	private Random objRandom;
	
	// Déclaration d'un objet pour conserver tous les paramètres
	// pour les joueurs virtuels
	private ParametreIA objParametreIA;
	
	
	
		
	/**
	 * Constructeur de la classe ControleurJeu qui permet de créer le gestionnaire 
	 * des communications, le gestionnaire d'événements et le gestionnaire de bases 
	 * de données. 
	 */
	public ControleurJeu() 
	{
		super();
                
                modeDebug = GestionnaireConfiguration.obtenirInstance().obtenirValeurBooleenne("controleurjeu.debug");
		
                // Initialiser la classe statique GestionnaireMessages
                GestionnaireMessages.initialiser();
        
		objLogger.info(GestionnaireMessages.message("controleur_jeu.serveur_demarre"));
		
		// Préparer l'objet pour créer les nombres aléatoires
        objRandom = new Random();
		
		// Créer une liste des joueurs
		lstJoueursConnectes = new TreeMap();
		
		// Créer une liste des joueurs déconnectés
		lstJoueursDeconnectes = new TreeMap();
		
		// Créer une liste des salles
		lstSalles = new TreeMap();
		
		// Créer un nouveau gestionnaire d'événements
		objGestionnaireEvenements = new GestionnaireEvenements();
		
		// Créer un nouveau gestionnaire de base de données MySQL
		objGestionnaireBD = new GestionnaireBD(this);
		
								
		objGestionnaireTemps = new GestionnaireTemps();
		objTacheSynchroniser = new TacheSynchroniser();

		// Créer un nouveau gestionnaire de communication
		objGestionnaireCommunication = new GestionnaireCommunication(this, objGestionnaireEvenements);
		
		////////////////////////////////////////////////////////////////
		String language = "en";
		// Fills the rooms from DB
		objGestionnaireBD.fillsRooms(language);   
	}
	
	public void demarrer()
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

		int intStepSynchro = config.obtenirNombreEntier( "controleurjeu.synchro.step" );
		objGestionnaireTemps.ajouterTache( objTacheSynchroniser, intStepSynchro );
		
        // Créer un thread pour le GestionnaireEvenements
		Thread threadEvenements = new Thread(objGestionnaireEvenements);
		
		// Démarrer le thread du gestionnaire d'événements
		threadEvenements.start();
		
		/***********************
		// Démarrer l'espion qui écrit dans un fichier périodiquement les
		// informations du serveur
		
		String fichier = config.obtenirString( "controleurjeu.info.fichier-sortie" );
		int delai = config.obtenirNombreEntier( "controleurjeu.info.delai" );
		objEspion = new Espion(this, fichier, delai, ClassesUtilitaires.Espion.MODE_FICHIER_TEXTE);

        // Démarrer la thread de l'espion
		Thread threadEspion = new Thread(objEspion);
		threadEspion.start();
        *********************************/
		
        // Créer une instance de la classe regroupant tous les paramètres
        // des joueurs virtuels
        objParametreIA = new ParametreIA();

		//Demarrer une tache de monitoring
		TacheLogMoniteur objTacheLogMoniteur = new TacheLogMoniteur();
		int intStepMonitor = config.obtenirNombreEntier( "controleurjeu.monitoring.step" );
		objGestionnaireTemps.ajouterTache( objTacheLogMoniteur, intStepMonitor );
		
		//Démarrer l'écoute des connexions clientes
		//Cette methode est la loop de l'application
		//Au retour, l'application se termine
		objGestionnaireCommunication.ecouterConnexions();
		System.out.println( "arret" );
	}
	
	public void arreter()
	{
		System.out.println( "Le serveur arrete..." );
		objGestionnaireCommunication.arreter();
	}
	
	/**
	 * Cette fonction permet de déterminer si le joueur dont le nom d'utilisateur
	 * est passé en paramètre est déjà connecté au serveur de jeu ou non.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @return false : Le joueur n'est pas connecté au serveur de jeu 
	 * 		   true  : Le joueur est déjà connecté au serveur de jeu
	 * @synchronism Cette fonction est synchronisée sur la liste des 
	 * 				joueurs connectés. 
	 */
	public boolean joueurEstConnecte(String nomUtilisateur)
	{
	    // Synchroniser l'accès à la liste des joueurs connectés
	    synchronized (lstJoueursConnectes)
	    {
			// Retourner si le joueur est déjà connecté au serveur de jeu ou non
			return lstJoueursConnectes.containsKey(nomUtilisateur.toLowerCase());	        
	    }
	}

	/**
	 * Cette fonction permet de valider que les informations du joueur passées
	 * en paramètres sont correctes (elles existent et concordent). On suppose
	 * que le joueur n'est pas connecté au serveur de jeu.
	 * 
	 * @param ProtocoleJoueur protocole : Le protocole du joueur
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @param String motDePasse : Le mot de passe du joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 							générer un numéro de commande pour le retour de
	 * 							l'appel de fonction
	 * @return JoueurNonConnu : Le nom d'utilisateur du joueur n'est pas connu par le 
	 * 				            serveur ou le mot de passe ne concorde pas au nom 
	 * 				            d'utilisateur donné
	 * 		   JoueurDejaConnecte : Le joueur a tenté de se connecter en même temps 
	 * 								à deux endroits différents  
	 * 		   Succes : L'authentification a réussie
	 * @synchronism  Cette fonction est synchronisée par rapport à la liste des
	 * 				 joueurs connectés car on fait un synchronized sur elle, 
	 * 				 elle est synchronisé par rapport au joueur du protocole car 
	 * 				 les seules fonctions qui accèdent au protocole sont le 
	 * 				 VerificateurConnexions (fait juste un accès au protocole et 
	 * 				 non un accès au joueur du protocole donc c'est correct), le 
	 * 				 protocole lui-même (le protocole ne traite qu'une commande 
	 * 				 à la fois, donc on se fou que lui utilise son joueur) et la 
	 * 				 fonction deconnecterJoueur (elle ne peut pas être exécutée 
	 * 				 en même temps que l'authentification car le protocole ne 
	 * 				 traite qu'une commande à la fois, même si la demande vient 
	 * 				 du VerificateurConnexions).
	 */
	public String authentifierJoueur(ProtocoleJoueur protocole, String nomUtilisateur, 
	        						 String motDePasse, boolean doitGenererNoCommandeRetour)
	{
				
	    // Déclaration d'une variable qui va contenir le résultat à retourner
	    // à la fonction appelante, soit les valeurs de l'énumération 
	    // ResultatAuthentification
	    String strResultatAuthentification = ResultatAuthentification.JoueurNonConnu;
	    
		// Déterminer si le joueur dont le nom d'utilisateur est passé en 
		// paramètres existe et mettre le résultat dans une variable booléenne
		boolean bolResultatRecherche = objGestionnaireBD.joueurExiste(nomUtilisateur, motDePasse); 

		// Si les informations de l'utilisateur sont correctes, alors le 
		// joueur est maintenant connecté au serveur de jeu
		if (bolResultatRecherche == true)
		{
			// Créer un nouveau joueur humain contenant les bonnes informations
			JoueurHumain objJoueurHumain = new JoueurHumain(protocole, nomUtilisateur, 
															protocole.obtenirAdresseIP(),
															protocole.obtenirPort());
			
			// Trouver les informations sur le joueur dans la BD et remplir le 
			// reste des champs tels que les droits
			objGestionnaireBD.remplirInformationsJoueur(objJoueurHumain);
			
			
			
			// À ce moment, comme il se peut que le même joueur tente de se 
			// connecter en même temps par 2 protocoles de joueur, alors si
			// ça arrive on va le vérifier juste une fois qu'on a fait tous 
			// les appels à la base de données, il faut cependant s'assurer
			// que personne ne touche à la liste de joueurs pendant ce temps-là.
			// C'est un cas qui ne devrait vraiment pas arriver souvent, car
			// normalement une erreur devrait être renvoyée au client si 
			// celui-ci essaie de se connecter à deux endroits en même temps.
			// Pour des raisons de performance, on fonctionne comme cela, car 
			// chercher dans la base de données peut être assez long
			synchronized (lstJoueursConnectes)
			{
				// Si le joueur est déjà présentement connecté, on ne peut
				// pas finaliser la connexion du joueur
				if (joueurEstConnecte(nomUtilisateur) == true)
				{
				    // On va retourner que le joueur est déjà connecté
				    strResultatAuthentification = ResultatAuthentification.JoueurDejaConnecte;
				}
				else
				{
					// Définir la référence vers le joueur humain
					protocole.definirJoueur(objJoueurHumain);
					
					// Ajouter ce nouveau joueur dans la liste des joueurs connectés
					// au serveur de jeu
					lstJoueursConnectes.put(nomUtilisateur.toLowerCase(), objJoueurHumain);
					
					// Si on doit générer le numéro de commande de retour, alors
					// on le génère, sinon on ne fait rien (ça devrait toujours
					// être vrai, donc on le génère tout le temps)
					if (doitGenererNoCommandeRetour == true)
					{
						// Générer un nouveau numéro de commande qui sera 
					    // retourné au client
						protocole.genererNumeroReponse();					    
					}
					
				    // L'authentification a réussie
				    strResultatAuthentification = ResultatAuthentification.Succes;
					
					// Préparer l'événement de nouveau joueur. Cette fonction 
				    // va passer les joueurs et créer un InformationDestination 
				    // pour chacun et ajouter l'événement dans la file de gestion 
				    // d'événements
					preparerEvenementJoueurConnecte(nomUtilisateur);
				}
			}
		}
		
		
		return strResultatAuthentification;
	}
	
	/**
	 * Cette méthode permet de déconnecter le joueur passé en paramètres. Il 
	 * faut enlever toute trace du joueur du serveur de jeu et en aviser les
	 * autres participants se trouvant au même endroit que le joueur déconnecté 
	 * (à une table de jeu).
	 * 
	 * @param JoueurHumain joueur : Le joueur humain ayant fait la demande 
	 * 								de déconnexion
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @synchronism À ce niveau-ci, il n'y a pas vraiment de restrictions sur
	 * 				l'ordre d'arrivée des événements indiquant que le joueur
	 * 				a quitté la table ou la salle. De plus, aucune autre 
	 * 				fonction ne peut modifier le joueur, puisque deux 
	 * 				fonctions d'un même protocole ne peuvent pas être 
	 * 				exécutées en même temps. Cependant, pour enlever un
	 * 				joueur de la liste des joueurs connectés, il faut
	 * 				s'assurer que personne d'autre ne va toucher à la liste
	 * 				des joueurs connectés.
	 */
	public void deconnecterJoueur(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean ajouterJoueurDeconnecte)
	{

		// Si déconnection pendant une partie, ajouterJoueurDeconnecte = true
		// On va donc ajouter ce joueur à la liste des joueurs
		// déconnectés pour cette table et pour le contrôleur du jeu
		if (ajouterJoueurDeconnecte == true && joueur != null &&
		    joueur.obtenirPartieCourante() != null &&
		    joueur.obtenirPartieCourante().obtenirTable() != null &&
		    joueur.obtenirPartieCourante().obtenirTable().estCommencee() == true &&
		    joueur.obtenirPartieCourante().obtenirTable().estArretee() == false)
		{
					
			// Ajouter ce joueur à la liste des joueurs déconnectés pour cette
			// table
			joueur.obtenirPartieCourante().obtenirTable().ajouterJoueurDeconnecte(joueur);
		    
		    // Ajouter ce joueur à la liste des joueurs déconnectés du serveur
		    ajouterJoueurDeconnecte(joueur);
		}
		
		// Si le joueur courant est dans une salle, alors on doit le retirer de
		// cette salle (pas besoin de faire la synchronisation sur la salle 
		// courante du joueur car elle ne peut être modifiée par aucun autre
		// thread que celui courant)
		if (joueur.obtenirSalleCourante() != null)
		{
			// Le joueur courant quitte la salle dans laquelle il se trouve
			joueur.obtenirSalleCourante().quitterSalle(joueur, false, !ajouterJoueurDeconnecte);
		}
		
		
		// Empêcher d'autres thread de venir utiliser la liste des joueurs
		// connectés au serveur de jeu pendant qu'on déconnecte le joueur
		synchronized (lstJoueursConnectes)
		{
			// Enlever le joueur de la liste des joueurs connectés
			lstJoueursConnectes.remove(joueur.obtenirNomUtilisateur().toLowerCase());
			
			// Enlever la référence du protocole du joueur vers son joueur humain 
			// (cela va avoir pour effet que le protocole du joueur va penser que
			// le joueur n'est plus connecté au serveur de jeu)
			joueur.obtenirProtocoleJoueur().definirJoueur(null);

			// Si on doit générer le numéro de commande de retour, alors
			// on le génère, sinon on ne fait rien
			if (doitGenererNoCommandeRetour == true)
			{
				// Générer un nouveau numéro de commande qui sera 
			    // retourné au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}
			
			// Aviser tous les joueurs connectés au serveur de jeu qu'un joueur
			// s'est déconnecté
			preparerEvenementJoueurDeconnecte(joueur.obtenirNomUtilisateur());		    
		}
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des joueurs connectés au serveur
	 * de jeu. La vraie liste est retournée.
	 * 
	 * @return TreeMap : La liste des joueurs connectés au serveur de jeu 
	 *                   (c'est la référence vers la liste du ControleurJeu, il 
	 *                   faut donc traiter le cas du multithreading)
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				l'être par l'appelant de cette fonction tout dépendant
	 * 				du traitement qu'elle doit faire
	 */
	public TreeMap obtenirListeJoueurs()
	{
		return lstJoueursConnectes;
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des salles du serveur de jeu.
	 * La vraie liste est retournée.
	 * 
	 * @return TreeMap : La liste des salles du serveur de jeu (c'est la 
	 * 				     référence vers la liste du ControleurJeu, il faut donc
	 *                   traiter le cas du multithreading)
	 * @synchronism Cette fonction n'est pas synchronisée ici et il n'est pas
	 * 				vraiment nécessaire de le faire dans la fonction appelante
	 * 				pour ce qui est de la corruption des données suite à 
	 * 				l'ajout et/ou au retrait d'une salle, car ça ne peut pas
	 * 				se produire.
	 */
	public TreeMap obtenirListeSalles(String language, String gameType)
	{
		
	        // On crée une liste de salles vide, et on parcourt toutes les salles connues
            TreeMap copieListeSalles = (TreeMap)lstSalles.clone();
            copieListeSalles.clear();
            Set keySet = lstSalles.keySet();
            Iterator it = keySet.iterator();
            boolean repeat = true;
           
            while (it.hasNext()&& repeat)
            { 
            	String key = (String)it.next();
                Salle salle = (Salle)lstSalles.get(key);
                                
                if(salle.getRegles().getTournamentState()){

                	copieListeSalles.clear();
                	key = "TournamentActive";
                	copieListeSalles.put(key,salle);
                  	repeat =  false;
                	            	
                }else{
                	// On vérifie si cette salle est du bon gameType
                	// et si elle permet de jouer dans la langue donnée
                	//Boolean estDuBonGameType = gameType.equals(salle.getGameType());
                	Boolean permetCetteLangue = objGestionnaireBD.roomLangControl(salle, language);

                	// Si les paramètres en entrée sont des strings vides,
                	// alors on ignore le paramètre correspondant
                	//if(gameType.equals("")) estDuBonGameType = true;
                	if(language.equals("")) permetCetteLangue = true;

                	// On ajoute la salle à la liste si elle correspond à ce qu'on veut
                	if(permetCetteLangue) copieListeSalles.put(key, salle);
                	
                }
            }
            
            return copieListeSalles;
	}

	/**
	 * Cette fonction permet de déterminer si la salle dont le nom est passé
	 * en paramètres existe déjà ou non.
	 * 
	 * @param String nomSalle : Le nom de la salle
	 * @return false : La salle n'existe pas 
	 * 		   true  : La salle existe déjà
	 * @synchronism Cette fonction n'a pas besoin d'être synchronisée car
	 * 				on ne peut pas ajouter ou enlever des salles par le
	 * 				serveur de jeu (sauf quand celui-ci démarre, mais aucun
	 * 				joueur n'est connecté à ce moment-là)
	 */
	public boolean salleExiste(String nomSalle)
	{
		// Retourner si la salle existe déjà ou non
		return lstSalles.containsKey(nomSalle);	        
	}
	
	/**
	 * Cette méthode permet d'ajouter une nouvelle salle dans la liste des 
	 * salles du contrôleur de jeu.
	 * 
	 * @param Salle nouvelleSalle : La nouvelle salle à ajouter dans la liste
	 * @synchronism Cette fonction n'a pas besoin d'être synchronisée car
	 * 				elle est exécutée seulement lors du démarrage du serveur
	 * 				et il n'y a aucun joueur de connecté à ce moment là.
	 */
	public void ajouterNouvelleSalle(Salle nouvelleSalle)
	{
	    // Ajouter la nouvelle salle dans la liste des salles du 
	    // contrôleur de jeu
	    lstSalles.put(nouvelleSalle.getRoomName(), nouvelleSalle);	        
	}
	
	/**
	 * Cette fonction permet de valider que le mot de passe pour entrer dans la
	 * salle est correct. On suppose suppose que le joueur n'est pas dans aucune
	 * salle. Cette fonction va avoir pour effet de connecter le joueur dans la
	 * salle dont le nom est passé en paramètres.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la salle
	 * @param String nomSalle : Le nom de la salle dans laquelle entrer
	 * @param String motDePasse : Le mot de passe pour entrer dans la salle
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								générer un numéro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return false : Le mot de passe pour entrer dans la salle n'est pas
	 * 				   le bon
	 * 		   true  : Le joueur a réussi à entrer dans la salle
	 * @synchronism Cette fonction n'a pas besoin d'être synchronisée, car 
	 * 				elle ne modifie pas la liste des salles et aucune autre
	 * 				fonction ne le fait. Cependant, la méthode entrerSalle
	 * 				de la salle devra être synchronisée.
	 */
	public boolean entrerSalle(JoueurHumain joueur, String nomSalle, 
	        				   String motDePasse, boolean doitGenererNoCommandeRetour)
	{
		// On retourne le résultat de l'entrée du joueur dans la salle
		return ((Salle) lstSalles.get(nomSalle)).entrerSalle(joueur, motDePasse, doitGenererNoCommandeRetour);
	}
	
	/**
	 * Cette méthode permet de préparer l'événement de l'arrivée d'un nouveau
	 * joueur. Cette méthode va passer tous les joueurs connectés et pour ceux 
	 * devant être avertis (tous sauf le joueur courant passé en paramètre),
	 * on va obtenir un numéro de commande, on va créer un 
	 * InformationDestination et on va ajouter l'événement dans la file 
	 * d'événements du gestionnaire d'événements. Lors de l'appel de cette
	 * fonction, la liste des joueurs connectés est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de se connecter au serveur de jeu
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (authentifierJoueur).
	 */
	private void preparerEvenementJoueurConnecte(String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un nouveau joueur s'est connecté
	    EvenementJoueurConnecte joueurConnecte = new EvenementJoueurConnecte(nomUtilisateur);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueursConnectes (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueursConnectes.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs connectés et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de se connecter au serveur de jeu, alors on peut
			// envoyer un événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
				joueurConnecte.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurConnecte);
	}
	
	/**
	 * Cette méthode permet de préparer l'événement de la déconnexion d'un
	 * joueur. Cette méthode va passer tous les joueurs connectés et pour ceux 
	 * devant être avertis (tous sauf le joueur courant passé en paramètre),
	 * on va obtenir un numéro de commande, on va créer un 
	 * InformationDestination et on va ajouter l'événement dans la file 
	 * d'événements du gestionnaire d'événements. Lors de l'appel de cette
	 * fonction, la liste des joueurs connectés est synchronisée.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de se déconnecter du serveur de jeu
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (deconnecterJoueur).
	 */
	private void preparerEvenementJoueurDeconnecte(String nomUtilisateur)
	{
	    // Créer un nouvel événement qui va permettre d'envoyer l'événement 
	    // aux joueurs qu'un joueur s'est déconnecté
	    EvenementJoueurDeconnecte joueurDeconnecte = new EvenementJoueurDeconnecte(nomUtilisateur);
	    
		// Créer un ensemble contenant tous les tuples de la liste 
		// lstJoueursConnectes (chaque élément est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueursConnectes.entrySet();
		
		// Obtenir un itérateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs connectés et leur envoyer un événement
		while (objIterateurListe.hasNext() == true)
		{
			// Créer une référence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de se déconnecter du serveur de jeu, alors on peut
			// envoyer un événement à cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un numéro de commande pour le joueur courant, créer 
			    // un InformationDestination et l'ajouter à l'événement
			    joueurDeconnecte.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            												objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(joueurDeconnecte);
	}
	
		
	public GestionnaireCommunication obtenirGestionnaireCommunication()
	{
		return objGestionnaireCommunication;
	}
	
	public GestionnaireEvenements obtenirGestionnaireEvenements()
	{
	    return objGestionnaireEvenements;
	}
	
	public GestionnaireTemps obtenirGestionnaireTemps()
	{
	    return objGestionnaireTemps;
	}
	
	public TacheSynchroniser obtenirTacheSynchroniser()
	{
	    return objTacheSynchroniser;
	}
	
	public GestionnaireBD obtenirGestionnaireBD()
	{
	   return objGestionnaireBD;
	}

    
    /*
     * Cette fonction ajouter un joueur à la liste des joueurs déconnectés. Si le
     * joueur tente de se reconnecter, il sera possible qu'il reprenne la partie
     */
    public void ajouterJoueurDeconnecte(JoueurHumain joueurHumain)
    {

        synchronized(lstJoueursDeconnectes)
        {
            lstJoueursDeconnectes.put(joueurHumain.obtenirNomUtilisateur().toLowerCase(), joueurHumain);
        }  
    }
    
    /*
     * Cette fonction va nous permettre de savoir si ce joueur a été
     * déconnecté pendant une partie.
     */
    public boolean estJoueurDeconnecte(String nomUtilisateur)
    {
       synchronized(lstJoueursDeconnectes)
       {
           return lstJoueursDeconnectes.containsKey(nomUtilisateur.toLowerCase()); 
       }

    }
    
    /*
     * Cette fonction retourne une référence vers un objet JoueurHumain
     * d'un joueur déconnecté. Cet objet contient toutes les informations
     * à propos de la partie qui était en cours
     */
    public JoueurHumain obtenirJoueurHumainJoueurDeconnecte(String nomUtilisateur)
    {
        synchronized(lstJoueursDeconnectes)
        {
            return (JoueurHumain) lstJoueursDeconnectes.get(nomUtilisateur);
        }
    }
    
    /*
     * Cette fonction permet d'enlever un joueur déconnecté de la liste
     * des joueurs déconnectés, soit parce qu'il vient de se reconnecter,
     * ou car la partie qu'il avait commencée et qui était en suspend est terminée
     */
    public void enleverJoueurDeconnecte(String nomUtilisateur)
    {
    	synchronized(lstJoueursDeconnectes)
    	{
    		lstJoueursDeconnectes.remove(nomUtilisateur.toLowerCase());
    	}
    }
    
    public TreeMap obtenirListeJoueursDeconnectes()
    {
    	return lstJoueursDeconnectes;
    }
    
    
    public int genererNbAleatoire(int max)
    {
    	return objRandom.nextInt(max);
    }
    
    public ParametreIA obtenirParametreIA()
    {
    	return objParametreIA;
    }
}
