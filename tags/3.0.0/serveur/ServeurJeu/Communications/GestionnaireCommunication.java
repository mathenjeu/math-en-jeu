package ServeurJeu.Communications;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Vector;
import org.apache.log4j.Logger;
import ServeurJeu.ControleurJeu;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Configuration.GestionnaireMessages;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.TacheSynchroniser;

/**
 * @author Jean-François Brind'Amour
 */
public class GestionnaireCommunication 
{
	// Déclaration d'une référence vers le contrôleur de jeu
	private ControleurJeu objControleurJeu;
	
	// Déclaration d'une liste de ProtocoleJoueur des clients connectés au serveur
	private Vector<ProtocoleJoueur> lstProtocoleJoueur;
	
	// Déclaration d'un objet qui va permettre de vérifier l'état des connexions
	// entre le serveur et les clients
	private VerificateurConnexions objVerificateurConnexions;
	
	// Déclaration d'une référence vers le gestionnaire d'événements
	private GestionnaireEvenements objGestionnaireEvenements;
	
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
	// Cette variable contient le port sur lequel le serveur va écouter et 
	// recevoir les connexions clientes
	private int intPort;
	
	private String bindAddress;
	
	// Déclaration d'un socket pour le serveur
	private ServerSocket objSocketServeur;
	
	//private GestionnaireTemps objGestionnaireTemps;
	//private TacheSynchroniser objTacheSynchroniser;
	
	private boolean boolStopThread; 
	
	private static Logger objLogger = Logger.getLogger( GestionnaireCommunication.class );

    
	
	/**
	 * Constructeur de la classe GestionnaireCommunication qui permet d'initialiser
	 * le port d'écoute du serveur et la référence vers le contrôleur de jeu ainsi
	 * que vers le gestionnaire d'événements.
	 */
	public GestionnaireCommunication(ControleurJeu controleur, GestionnaireEvenements gestionnaireEv) 
	{
		super();
		
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		intPort = config.obtenirNombreEntier( "gestionnairecommunication.port" );
		bindAddress = config.obtenirString( "gestionnairecommunication.address" );
		
		// Garder la référence vers le contrôleur de jeu
		objControleurJeu = controleur;
		
		// Garder la référence vers le GestionnaireEvenements et vers le GestionnaireBD
		objGestionnaireEvenements = gestionnaireEv;
		objGestionnaireBD = controleur.obtenirGestionnaireBD();
		
		// Créer une liste des ProtocoleJoueur
		lstProtocoleJoueur = new Vector<ProtocoleJoueur>();
		
		// Créer le vérificateur de connexions
		objVerificateurConnexions = new VerificateurConnexions(this);
		
		//objGestionnaireTemps = controleur.obtenirGestionnaireTemps();
		//objTacheSynchroniser = controleur.obtenirTacheSynchroniser();
		
		
		// Créer un thread pour le vérificateur de connexions
		Thread threadVerificateur = new Thread(objVerificateurConnexions);
		
		// Démarrer le thread du vérificateur
		threadVerificateur.start();
		
		miseAJourInfo();
	}
	
	/**
	 * Cette méthode permet de démarrer l'écoute du serveur. Chaque connexion
	 * crée un nouveau thread pour gérer le protocole du joueur.
	 */
	public void ecouterConnexions()
	{
		try
		{
			// Créer un socket pour le serveur qui va écouter sur le port définit
			// par la variable "intPort"
			boolStopThread = false;
			objSocketServeur = new ServerSocket(intPort, 0, InetAddress.getByName(bindAddress));
		}
		catch (IOException e)
		{
			// L'écoute n'a pas pu être démarrée
			System.out.println(GestionnaireMessages.message("communication.erreur_demarrage") + intPort);
			System.out.println(GestionnaireMessages.message("communication.serveur_arrete"));
			boolStopThread = true;
		}
		
		// Boucler indéfiniment en écoutant sur le port "intPort" et en démarrant un
		// nouveau thread pour chacune des connexions établies
		while( !boolStopThread )
		{
			try
			{
				System.out.println(GestionnaireMessages.message("communication.attente"));
				
				// Accepter une connexion et créer un objet ProtocoleJoueur
				// qui va exécuter le protocole pour le joueur
				ProtocoleJoueur objJoueur = new ProtocoleJoueur(objControleurJeu, objVerificateurConnexions,
																objSocketServeur.accept());
				
				// Créer un thread pour le joueur demandant la connexion
				Thread threadJoueur = new Thread(objJoueur);
				
				// Démarrer le thread du joueur courant
				threadJoueur.start();
				
				// Ajouter le nouveau ProtocoleJoueur dans la liste (ici on n'a 
				// pas besoin de synchroniser la liste puisque le vecteur fait 
				// déjà cette synchronisation)
				lstProtocoleJoueur.add( objJoueur );
				miseAJourInfo();
			}
			catch (IOException e)
			{
				// Une erreur est survenue lors de l'acceptation de la connexion
				System.out.println(GestionnaireMessages.message("communication.erreur_accept"));
				objLogger.error( e.getMessage() );
				System.out.println(GestionnaireMessages.message("communication.serveur_arrete"));
				boolStopThread = true;
			}
		}
	}
	
	public void arreter()
	{
		try 
		{
			objSocketServeur.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		boolStopThread = true;
	}
	
	/**
	 * Cette méthode permet de supprimer le protocole joueur passé en paramètres
	 * de la liste des ProtocoleJoueur. Cela signifie que le joueur ne sera plus
	 * connecté physiquement au serveur de jeu. Il faut s'assurer que le 
	 * ProtocoleJoueur supprimé sera détruit ou terminera prochainement.
	 * 
	 * @param ProtocoleJoueur protocole : le protocole du joueur à supprimer de
	 * 									  la liste
	 */
	public void supprimerProtocoleJoueur(ProtocoleJoueur protocole)
	{
		// Enlever le protocole joueur de la liste des ProtocoleJoueur
		try
		{
			
			lstProtocoleJoueur.remove(protocole);
			miseAJourInfo();
		}
		catch( Exception e )
		{
			System.out.println(GestionnaireMessages.message("communication.erreur_protocole"));
		}
	}
	
	/**
	 * Cette fonction permet de retourner la liste des 
	 * ProtocoleJoueur des clients connectés au serveur de jeu.
	 * 
	 * @return Vector : la liste des ProtocoleJoueur des clients 
	 * 					présentement connectés au serveur de jeu
	 */
	public Vector<ProtocoleJoueur> obtenirListeProtocoleJoueur()
	{
		return lstProtocoleJoueur;
	}
	
	public void miseAJourInfo()
	{
		try
		{
			FileWriter writer = new FileWriter( "serveur.info" );
			writer.write( new Integer( lstProtocoleJoueur.size() ).toString() );
			objLogger.info( GestionnaireMessages.message("communication.nb_joueurs") + lstProtocoleJoueur.size() );
			writer.close();
		}
		catch( Exception e )
		{
			System.out.println(GestionnaireMessages.message("communication.erreur_fichier"));
			objLogger.error( e.getMessage() );
		}
	}
	
	/**
	 * Cette méthode est appelée automatiquement si le programme doit terminer.
	 * Lorsque l'application serveur doit terminer, alors il faut s'assurer que 
	 * tous les threads stopent et que tous les sockets soient fermés. La façon
	 * la plus rapide est de fermer le socket serveur. Cela aura pour effet de
	 * fermer tous les sockets obtenus par l'acceptation de connexions et comme
	 * ces sockets se fermeront, chaque thread arrêtera car une exception 
	 * surviendra pour chaque thread. 
	 */
	protected void finalize()
	{
		try
		{
			// Fermer le socket du serveur
			objSocketServeur.close();
		}
		catch (IOException e)
		{
			// Le socket du serveur est déjà fermé
			System.out.println(GestionnaireMessages.message("communication.erreur_socket"));
		}
		
		// Vider la liste des protocoles de joueurs
		lstProtocoleJoueur.clear();
		
		// Arrêter le thread de vérification des connexions
		objVerificateurConnexions.arreterVerificateurConnexions();
		
		// Arrêter le thread de gestionnaire d'événements
		objGestionnaireEvenements.arreterGestionnaireEvenements();
		
		// Fermer toutes les connexions ouvertes pour le gestionnaire de base de données
		objGestionnaireBD.arreterGestionnaireBD();
	}

}
