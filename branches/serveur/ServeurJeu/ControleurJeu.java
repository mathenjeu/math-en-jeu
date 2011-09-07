package ServeurJeu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Random;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import ClassesUtilitaires.UtilitaireXML;
import Enumerations.RetourFonctions.ResultatAuthentification;
import ServeurJeu.BD.DBConnectionsPoolManager;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Communications.GestionnaireCommunication;
import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Evenements.Evenement;
import ServeurJeu.Evenements.EvenementJoueurDeconnecte;
import ServeurJeu.Evenements.EvenementJoueurConnecte;
import ServeurJeu.Evenements.EvenementNouvelleSalle;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.Evenements.StopServerEvent;
import ServeurJeu.Monitoring.Moniteur;
import ServeurJeu.Monitoring.TacheLogMoniteur;
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.TacheSynchroniser;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Configuration.GestionnaireMessages;
import ServeurJeu.BD.SpyRooms;

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
public class ControleurJeu {
    // Cette modeDebug est vraie, toute reponse des joueurs sera bonne, et
    // on affichera dans la console des informations sur les communications

    public static boolean modeDebug;
    private static Logger objLogger = Logger.getLogger(ControleurJeu.class);
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
    private final HashMap<String, JoueurHumain> lstJoueursConnectes;
    // Déclaration d'une variable pour contenir une liste des joueurs
    // qui ont étés déconnectés et qui étaient en train de joueur une partie
    private final HashMap<String, JoueurHumain> lstJoueursDeconnectes;
    // Cet objet est une liste des salles créées qui se trouvent dans le serveur
    // de jeu. Chaque élément de cette liste a comme clé le id de la salle
    private HashMap<Integer, Salle> lstSalles;
    // Déclaration de l'objet Espion qui va inscrire des informations à proppos
    // du serveur en parallèle
    //private Espion objEspion;
      
    // Déclaration d'une map qui permet d'obtenir une liste de tous les
    // keywords disponible: keyword_id --> [language_id --> name]
    private TreeMap<Integer, TreeMap<Integer, String>> keywordsMap;
    // Déclaration d'une map qui permet d'obtenir une liste de tous les
    // langues disponible: language_id --> [translated_language_id --> name]
    private TreeMap<Integer, TreeMap<Integer, String>> languagesMap;
    // Déclaration d'une map qui permet d'obtenir une liste de tous les
    // 'game_types' disponible: game_type_id --> [name]
    private TreeMap<Integer, String> gameTypesMap;
    // Boolean to indicate if server is on or off
    //private boolean isOn;
    
    // thread to look for new rooms to add or old rooms to delete
    private SpyRooms objSpyDB;
    
    private static final Random objRandom = new Random();    

    /**
     * Constructeur de la classe ControleurJeu qui permet de créer le gestionnaire
     * des communications, le gestionnaire d'événements et le gestionnaire de bases
     * de données.
     */
    public ControleurJeu() {
        super();
       
        modeDebug = GestionnaireConfiguration.obtenirInstance().obtenirValeurBooleenne("controleurjeu.debug");
        // Initialiser la classe statique GestionnaireMessages
        GestionnaireMessages.initialiser();
        objLogger.info(GestionnaireMessages.message("controleur_jeu.serveur_demarre"));

        // Créer une liste des joueurs
        lstJoueursConnectes = new HashMap<String, JoueurHumain>();

        // Créer une liste des joueurs déconnectés
        lstJoueursDeconnectes = new HashMap<String, JoueurHumain>();      
    }
    

    /**
     * To start the controler actions
     */
    public void demarrer() {

    	// Créer une liste des salles
        lstSalles = new HashMap<Integer, Salle>();
    	
    	// Créer un nouveau gestionnaire d'événements
        objGestionnaireEvenements = new GestionnaireEvenements();

        // Créer un nouveau gestionnaire de base de données MySQL
        objGestionnaireBD = new GestionnaireBD(this);

        objGestionnaireTemps = new GestionnaireTemps();
        objTacheSynchroniser = new TacheSynchroniser();

        // Créer un nouveau gestionnaire de communication
        objGestionnaireCommunication = new GestionnaireCommunication(this, objGestionnaireEvenements);

        // fill the list of keywords
        keywordsMap = objGestionnaireBD.getKeywordsMap();
        languagesMap = objGestionnaireBD.getLanguagesMap();
        gameTypesMap = objGestionnaireBD.getGameTypesMap();
        
        //System.out.println("Le serveur demarre 3 ...");
    	 // Fills the rooms from DB
        objGestionnaireBD.fillsRooms();

        // update user accounts - put to all not connected
        objGestionnaireBD.updatePlayersAccounts();
    	
    	GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

        int intStepSynchro = config.obtenirNombreEntier("controleurjeu.synchro.step");
        objGestionnaireTemps.ajouterTache(objTacheSynchroniser, intStepSynchro);

        // Créer un thread pour le GestionnaireEvenements
        objGestionnaireEvenements = new GestionnaireEvenements();
        Thread threadEvenements = new Thread(objGestionnaireEvenements, "GestEvenem - Controleur");
      
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
        //Start spyDb to update periodically the rooms list
        // Add new rooms or out the olds
        int intSpyStep = config.obtenirNombreEntier("controleurjeu.monitoring.spyDB");
        objSpyDB = new SpyRooms(this, intSpyStep);
        //Start spy thread's
        Thread threadSpy = new Thread(objSpyDB, "SpyRooms");
        threadSpy.start();

        //Demarrer une tache de monitoring
        TacheLogMoniteur objTacheLogMoniteur = new TacheLogMoniteur();
        int intStepMonitor = config.obtenirNombreEntier("controleurjeu.monitoring.step");
        objGestionnaireTemps.ajouterTache(objTacheLogMoniteur, intStepMonitor);
       
        //Démarrer l'écoute des connexions clientes
        //Cette methode est la loop de l'application
        //Au retour, l'application se termine
        objGestionnaireCommunication.ecouterConnexions();
        
        
    }
    
    public void stopItLater(){
    	this.sendEventServerWillBeReset();
    }

    private void sendEventServerWillBeReset() {
    	GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
    	int nbSeconds = config.obtenirNombreEntier("controleurjeu.stopTimer");
    	// Créer un nouvel événement qui va permettre d'envoyer l'événement
        // aux joueurs qu'un nouveau joueur s'est connecté
        StopServerEvent stopServer = new StopServerEvent(nbSeconds);
        // Passer tous les joueurs connectés et leur envoyer un événement
        for (JoueurHumain objJoueur : lstJoueursConnectes.values()) {
        	stopServer.ajouterInformationDestination(
                    new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }

        // Ajouter le nouvel événement créé dans la liste d'événements à traiter
        objGestionnaireEvenements.ajouterEvenement(stopServer);
	}

	/**
     * Stop the controler
     */
    public void arreter() {
        //System.out.println("Le serveur arrete...");
        
    	
        for(JoueurHumain objJoueur: this.lstJoueursConnectes.values())
        {
        	 // Si le joueur courant est dans une salle, alors on doit le retirer de
            // cette salle (pas besoin de faire la synchronisation sur la salle
            // courante du joueur car elle ne peut être modifiée par aucun autre
            // thread que celui courant)
            if (objJoueur.obtenirSalleCourante() != null) {
                // Le joueur courant quitte la salle dans laquelle il se trouve
            	objJoueur.obtenirProtocoleJoueur().setBolStopThread(true);
                objJoueur.obtenirSalleCourante().quitterSalle(objJoueur, false, true);
            }
            
            objJoueur.obtenirProtocoleJoueur().definirJoueur(null);

        }
        
        
        for(Salle room: this.lstSalles.values())
        {
        	room.destroyRoom();
        }
        
        this.lstSalles.clear();
        
        this.objSpyDB.stopSpy();
        objGestionnaireCommunication.arreter();
        objGestionnaireCommunication = null;
        //objGestionnaireTemps.stopIt();
        try{
           objGestionnaireTemps.enleverTache(objTacheSynchroniser);
        }catch (IllegalStateException ex){
			   objLogger.error(" Synchroniser cancel ganerated timer end off... ");	
		}
        objTacheSynchroniser = null;
        objGestionnaireTemps = null;
        objGestionnaireEvenements.arreterGestionnaireEvenements();
        objGestionnaireEvenements = null;
        
        this.objGestionnaireBD.finalize();
        
        // to close all connections to the DB
        // if connection is not closed implicitly ... is never closed
        // DB connection memory leak
        DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        pool.release();
        
        //this.lstJoueursConnectes.clear();        
        //this.isOn = false;
    }

    /**
     * Cette fonction permet de déterminer si le joueur dont le nom d'utilisateur
     * est passé en paramètre est déjà connecté au serveur de jeu ou non.
     *
     * @param nomUtilisateur Le nom d'utilisateur du joueur
     * @return false Le joueur n'est pas connecté au serveur de jeu
     * 	       true  Le joueur est déjà connecté au serveur de jeu
     * @synchronism Cette fonction est synchronisée sur la liste des
     *              joueurs connectés.
     */
    public boolean joueurEstConnecte(String nomUtilisateur) {
        // Synchroniser l'accès à la liste des joueurs connectés
        synchronized (lstJoueursConnectes) {
            // Retourner si le joueur est déjà connecté au serveur de jeu ou non
            for (String nom : lstJoueursConnectes.keySet())
            {
                if (nom.equalsIgnoreCase(nomUtilisateur))
                    return true;
            }
            return false;
        }
    }

    /**
     * Cette fonction permet de valider que les informations du joueur passées
     * en paramètres sont correctes (elles existent et concordent). On suppose
     * que le joueur n'est pas connecté au serveur de jeu.
     *
     * @param protocole Le protocole du joueur
     * @param nomUtilisateur Le nom d'utilisateur du joueur (la capitalisation n'est pas importante)
     * @param motDePasse Le mot de passe du joueur
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
     *        numéro de commande pour le retour de l'appel de fonction
     * @return JoueurNonConnu Le nom d'utilisateur du joueur n'est pas connu par le
     *            serveur ou le mot de passe ne concorde pas au nom d'utilisateur donné
     *         JoueurDejaConnecte Le joueur a tenté de se connecter en même temps
     * 		  à deux endroits différents
     *         Succes L'authentification a réussie
     * @synchronism Cette fonction est synchronisée par rapport à la liste des
     *              joueurs connectés car on fait un synchronized sur elle,
     *              elle est synchronisé par rapport au joueur du protocole car
     *              les seules fonctions qui accèdent au protocole sont le
     *              VerificateurConnexions (fait juste un accès au protocole et
     *              non un accès au joueur du protocole donc c'est correct), le
     *              protocole lui-même (le protocole ne traite qu'une commande
     *              à la fois, donc on se fou que lui utilise son joueur) et la
     *              fonction deconnecterJoueur (elle ne peut pas être exécutée
     *              en même temps que l'authentification car le protocole ne
     *              traite qu'une commande à la fois, même si la demande vient
     *              du VerificateurConnexions).
     */
    public ResultatAuthentification authentifierJoueur(ProtocoleJoueur protocole, String nomUtilisateur,
            String motDePasse, boolean doitGenererNoCommandeRetour) {
    	
    	//On vÃ©rifie tout d'abord si le joueur est dÃ©jÃ  connectÃ©, si c'est le
    	//cas, le joueur doit nÃ©cessairement exister et la valeur de retour doit
    	//Ãªtre JoueurDejaConnecte (la mÃ©thode joueurEstConnecte obtient un lock sur
    	//la liste des joueurs connectÃ©s, donc on a pas besoin de l'obtenir ici.
    	if (joueurEstConnecte(nomUtilisateur))
    	{    		
    		if (joueurEstConnecte(nomUtilisateur))
        	{
        		return ResultatAuthentification.JoueurDejaConnecte;
        	}
    	}
        // Déterminer si le joueur dont le nom d'utilisateur est passé en
        // paramètres existe et mettre le résultat dans une variable booléenne
        // la méthode retourne le username de la BD pour que la capitalisation
        // soit correcte.
        String username = objGestionnaireBD.getUsername(nomUtilisateur, motDePasse);
        if (username == null)
        	return ResultatAuthentification.JoueurNonConnu;

        // Si les informations de l'utilisateur sont correctes, alors le
        // joueur est maintenant connecté au serveur de jeu
        // Créer un nouveau joueur humain contenant les bonnes informations
        JoueurHumain objJoueurHumain = new JoueurHumain(protocole, username,
        		protocole.obtenirAdresseIP(),
        		protocole.obtenirPort());       
        
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
        synchronized (lstJoueursConnectes) {
        	// Si le joueur est déjà présentement connecté, on ne peut
        	// pas finaliser la connexion du joueur.  On doit re-vÃ©rifier
        	// si le joueur est dÃ©jÃ  connectÃ© dans le cas peu probable
        	// oÃ¹ le joueur aurait lancÃ© une autre demande de connection
        	// depuis l'autre vÃ©rification au dÃ©but de la mÃ©thode.
        	if (joueurEstConnecte(username)) {
        		// On va retourner que le joueur est déjà connecté
        		return ResultatAuthentification.JoueurDejaConnecte;
        	} 
        	
        	// Définir la référence vers le joueur humain
        	protocole.definirJoueur(objJoueurHumain);

        	// Ajouter ce nouveau joueur dans la liste des joueurs connectés
        	// au serveur de jeu
        	lstJoueursConnectes.put(username, objJoueurHumain);
        	
        	//adjust server info
            obtenirGestionnaireCommunication().miseAJourInfo();

        	// Si on doit générer le numéro de commande de retour, alors
        	// on le génére, sinon on ne fait rien (ça devrait toujours
        	// être vrai, donc on le génére tout le temps)
        	if (doitGenererNoCommandeRetour == true) {
        		// Générer un nouveau numéro de commande qui sera
        		// retourné au client
        		protocole.genererNumeroReponse();
        	}

        	// Préparer l'événement de nouveau joueur. Cette fonction
        	// va passer les joueurs et créer un InformationDestination
        	// pour chacun et ajouter l'événement dans la file de gestion
        	// d'événements
        	preparerEvenementJoueurConnecte(nomUtilisateur);
        }
        
        // Trouver les informations sur le joueur dans la BD et remplir le
        // reste des champs tels que les droits
        objGestionnaireBD.remplirInformationsJoueur(objJoueurHumain);
        
        return ResultatAuthentification.Succes;
    }

    /**
     * Cette méthode permet de déconnecter le joueur passé en paramètres. Il
     * faut enlever toute trace du joueur du serveur de jeu et en aviser les
     * autres participants se trouvant au même endroit que le joueur déconnecté
     * (à une table de jeu).
     *
     * @param joueur Le joueur humain ayant fait la demande de déconnexion
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
     *        numéro de commande pour le retour de l'appel de fonction
     * @param ajouterJoueurDeconnecte Si on doit ajouter ce joueur à la liste
     *        des joueurs déconnectés.
     * @synchronism À ce niveau-ci, il n'y a pas vraiment de restrictions sur
     *              l'ordre d'arrivée des événements indiquant que le joueur
     *              a quitté la table ou la salle. De plus, aucune autre
     *              fonction ne peut modifier le joueur, puisque deux
     *              fonctions d'un même protocole ne peuvent pas être
     *              exécutées en même temps. Cependant, pour enlever un
     *              joueur de la liste des joueurs connectés, il faut
     *              s'assurer que personne d'autre ne va toucher à la liste
     *              des joueurs connectés.
     */
    public void deconnecterJoueur(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean ajouterJoueurDeconnecte) {

    	 //System.out.println("! Joueur deconnecter - controleur1 ");    
        // Si déconnection pendant une partie, ajouterJoueurDeconnecte = true
        // On va donc ajouter ce joueur à la liste des joueurs
        // déconnectés pour cette table et pour le contrôleur du jeu
        if (ajouterJoueurDeconnecte == true && joueur != null &&
                joueur.obtenirPartieCourante() != null &&
                joueur.obtenirPartieCourante().obtenirTable() != null &&
                joueur.obtenirPartieCourante().obtenirTable().estCommencee() == true &&
                joueur.obtenirPartieCourante().obtenirTable().estArretee() == false) {

        	//joueur.obtenirPartieCourante().writeInfo();
            // Ajouter ce joueur à la liste des joueurs déconnectés pour cette
            // table
        	
            joueur.obtenirPartieCourante().obtenirTable().ajouterJoueurDeconnecte(joueur);

            // Ajouter ce joueur à la liste des joueurs déconnectés du serveur
            ajouterJoueurDeconnecte(joueur);
            objLogger.info("! Joueur deconnecter - mettre dans la liste deccon ");     
        }

        // Enlever le protocole du joueur courant de la liste des
        // protocoles de joueurs
        objGestionnaireCommunication.supprimerProtocoleJoueur(joueur.obtenirProtocoleJoueur());
        
        // Si le joueur courant est dans une salle, alors on doit le retirer de
        // cette salle (pas besoin de faire la synchronisation sur la salle
        // courante du joueur car elle ne peut être modifiée par aucun autre
        // thread que celui courant)
        if (joueur.obtenirSalleCourante() != null) {
            // Le joueur courant quitte la salle dans laquelle il se trouve
            joueur.obtenirSalleCourante().quitterSalle(joueur, false, !ajouterJoueurDeconnecte);
        }
       
        // update in DB the connection reset to 0 table jos_comprofiler - cb_connected
        objGestionnaireBD.updatePlayerConnected(joueur.obtenirCleJoueur(), 0);

        // Empêcher d'autres thread de venir utiliser la liste des joueurs
        // connectés au serveur de jeu pendant qu'on déconnecte le joueur
        synchronized (lstJoueursConnectes) {
        	
        	//System.out.println("! Joueur deconnecter - controleur3 ");     
            // Enlever le joueur de la liste des joueurs connectés
            lstJoueursConnectes.remove(joueur.obtenirNom());
            // Enlever la référence du protocole du joueur vers son joueur humain
            // (cela va avoir pour effet que le protocole du joueur va penser que
            // le joueur n'est plus connecté au serveur de jeu)
            joueur.obtenirProtocoleJoueur().setBolStopThread(true);
            joueur.obtenirProtocoleJoueur().definirJoueur(null);
            
            //adjust server info
            obtenirGestionnaireCommunication().miseAJourInfo();

            // Si on doit générer le numéro de commande de retour, alors
            // on le génére, sinon on ne fait rien
            if (doitGenererNoCommandeRetour == true) {
                // Générer un nouveau numéro de commande qui sera
                // retourné au client
                joueur.obtenirProtocoleJoueur().genererNumeroReponse();
            }

            // Aviser tous les joueurs connectés au serveur de jeu qu'un joueur
            // s'est déconnecté
            preparerEvenementJoueurDeconnecte(joueur.obtenirNom());
            joueur.setObjProtocoleJoueur(null);

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
    public HashMap<String, JoueurHumain> obtenirListeJoueurs() {
        return lstJoueursConnectes;
    }

    /**
     * Cette fonction permet d'obtenir la liste des salles du serveur de jeu.
     * La vraie liste est retournée.
     *
     * @return TreeMap : La liste des salles du serveur de jeu (c'est la
     * 				     référence vers la liste du ControleurJeu, il faut donc
     *                   traiter le cas du multithreading)
     * @synchronism Cette fonction n'est pas synchronisée ici, mais elle doit
     * 				l'être par l'appelant de cette fonction tout dépendant
     * 				du traitement qu'elle doit faire
     */

    public HashMap<Integer, Salle> obtenirListeSalles() {
    	return lstSalles;
    }
    
    /**
     * Cette fonction permet d'obtenir la liste des salles du serveur de jeu
     * qui supporte la langue spÃ©cifiÃ©e.
     *
     * @param language une String de 2 caractÃ¨res qui indique la langue pour
     *                 laquelle on fait la recherche. 'fr'=franÃ§ais, 'en'=english
     * @return TreeMap La liste des salles supportant la langue spÃ©cifiÃ©e

     */
    public HashMap<Integer, Salle> obtenirListeSalles(String language) {
    	HashMap<Integer, Salle> sallesSupportees = new HashMap<Integer, Salle>();
    	synchronized (lstSalles) {
    		// On crée une liste de salles vide, et on parcourt toutes les salles connues
            for (Salle salle : lstSalles.values())
            	if (language.equals("") || salle.getName(language) != null)
            		sallesSupportees.put(salle.getRoomId(), salle);
    	}
    	return sallesSupportees;
    }

    public HashMap<Integer, Salle> obtenirListeSalles(String langue, String roomsType) {
    	HashMap<Integer, Salle> sallesSupportees = new HashMap<Integer, Salle>();
    	synchronized (lstSalles) {
            // On crée une liste de salles vide, et on parcourt toutes les salles connues
    		for (Salle salle : lstSalles.values())
    			if ((langue.equals("") || salle.getName(langue) != null) &&
    					salle.getRoomType().equals(roomsType))
    				sallesSupportees.put(salle.getRoomId(), salle);
    	}
    	return sallesSupportees;
    } /// end method
    
    public HashMap<Integer, Salle> obtenirListeSallesCreateur(String nomUtilisateur) {
    	HashMap<Integer, Salle> sallesUtilisateur = new HashMap<Integer, Salle>();
    	synchronized (lstSalles) {
    		// On crée une liste de salles vide, et on parcourt toutes les salles connues
            for (Salle salle : lstSalles.values())
            	if (salle.getCreatorUsername().equals(nomUtilisateur))
            		sallesUtilisateur.put(salle.getRoomId(), salle);
    	}
    	return sallesUtilisateur;
    }
    


    /**
     * Cette fonction permet de déterminer si la salle dont le id est passé
     * en paramètres existe déjà ou non.
     *
     * @param idRoom Le room_id de la salle
     * @return false La salle n'existe pas
     *         true  La salle existe déjà
     */
    public boolean salleExiste(int idRoom) {
        // Retourner si la salle existe déjà ou non
        synchronized (lstSalles) {
            return lstSalles.containsKey(idRoom);
        }
    }

    public String getRoomName(int idRoom, String lang) {
        // Retourner si la salle existe déjà ou non
        synchronized (lstSalles) {
            if (lstSalles.containsKey(idRoom)) {
                return lstSalles.get(idRoom).getRoomName(lang);
            } else {
                return "Not exist";
            }
        }
    }

    /**
     * Cette méthode permet d'ajouter une nouvelle salle dans la liste des
     * salles du contrôleur de jeu.
     *
     * @param nouvelleSalle La nouvelle salle à ajouter dans la liste
     * @synchronism add synchronism because need to add rooms dinamicaly during
     *              the time of life of controler
     */
    public void ajouterNouvelleSalle(Salle nouvelleSalle) {
        // Ajouter la nouvelle salle dans la liste des salles du
        // contrôleur de jeu
        synchronized (lstSalles) {
            lstSalles.put(nouvelleSalle.getRoomId(), nouvelleSalle);
            //System.out.println(nouvelleSalle.getRoomId() + " NEW " + nouvelleSalle.toString());
        }
    }

    /**
     * This methode is used to close the room for future games
     * @param roomId the room_id for the room to close.
     * TODO needs to handle the case where players are in the room
     */
    public void closeRoom(int roomId) {
        synchronized (lstSalles) {
            lstSalles.remove(roomId);
        }
      //adjust server info
      obtenirGestionnaireCommunication().miseAJourInfo();
    }// end methode

    /**
     * This methode is used to remove the rooms from the list of Controleur if
     * the date of expiration is arrived. In the same time on return for SpyRooms
     * the list of ID of rooms rested in the list
     * @return
     */
    public ArrayList<Integer> removeOldRooms() {
        ArrayList<Integer> rooms = new ArrayList<Integer>();
        synchronized (lstSalles) {

            for(Salle salle : lstSalles.values())
            {
                if (salle.getEndDate() != null && salle.getEndDate().before(new Date(System.currentTimeMillis())))
                    lstSalles.remove(salle);
                else
                    rooms.add(salle.getRoomId());
            }
        }
                
        return rooms;
    }

    /**
     * Cette fonction permet de valider que le mot de passe pour entrer dans la
     * salle est correct. On suppose suppose que le joueur n'est pas dans aucune
     * salle. Cette fonction va avoir pour effet de connecter le joueur dans la
     * salle dont le nom est passé en paramètres.
     *
     * @param joueur Le joueur demandant d'entrer dans la salle
     * @param idRoom Le room_id de la salle dans laquelle entrer
     * @param motDePasse Le mot de passe pour entrer dans la salle
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
     *        numéro de commande pour le retour de l'appel de fonction
     * @return false Le mot de passe pour entrer dans la salle n'est pas bon
     *         true  Le joueur a réussi à entrer dans la salle
     */
    public boolean entrerSalle(JoueurHumain joueur, int idRoom,
            String motDePasse, boolean doitGenererNoCommandeRetour) {
        synchronized (lstSalles) {
            // On retourne le résultat de l'entrée du joueur dans la salle
            return lstSalles.get(idRoom).entrerSalle(joueur, motDePasse, doitGenererNoCommandeRetour);
        }
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
    private void preparerEvenementJoueurConnecte(String nomUtilisateur) {
        // Créer un nouvel événement qui va permettre d'envoyer l'événement
        // aux joueurs qu'un nouveau joueur s'est connecté
        //EvenementJoueurConnecte joueurConnecte = new EvenementJoueurConnecte(nomUtilisateur);
        EvenementJoueurConnecte joueurConnecte = new EvenementJoueurConnecte(nomUtilisateur);

        // Passer tous les joueurs connectés et leur envoyer un événement
        for (JoueurHumain objJoueur : lstJoueursConnectes.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de se connecter au serveur de jeu, alors on peut
            // envoyer un événement à cet utilisateur
            if (!objJoueur.obtenirNom().equalsIgnoreCase(nomUtilisateur)) {
                // Obtenir un numéro de commande pour le joueur courant, créer
                // un InformationDestination et l'ajouter à l'événement
                joueurConnecte.ajouterInformationDestination(new InformationDestination(
                            objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                            objJoueur.obtenirProtocoleJoueur()));
            }
        }
              
	   
        // Ajouter le nouvel événement créé dans la liste d'événements à traiter
        objGestionnaireEvenements.ajouterEvenement(joueurConnecte);
    }

    /**
     * Cette méthode permet de préparer l'événement de la création d'une
     * nouvelle salle dans le serveur apres l'ajout d'elle dans BD.
     * Cette méthode va passer tous les joueurs connectés et pour ceux devant être avertis
     * (tous sauf le joueur courant passé en paramètre), on va obtenir un numéro
     * de commande, on va créer un InformationDestination et on va ajouter
     * l'événement dans la file d'événements du gestionnaire d'événements.
     * Lors de l'appel de cette fonction, la liste des joueurs est
     * synchronisée.
     *
     * @param nouvelleSalle la salle pour laquelle on prépare l'événement.
     * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
     * 				par l'appelant ().
     */
    public void preparerEvenementNouvelleSalle(Salle nouvelleSalle)
    {
        String createurSalle = nouvelleSalle.getCreatorUsername();
        
        String g_types = "";
        for (Integer gameTypeId: nouvelleSalle.getGameTypeIds()) {
            g_types += gameTypeId + ",";
        }
        if (g_types.endsWith(",")) {
            g_types = g_types.substring(0, g_types.length() - 1);
        }
        // Créer un nouvel événement qui va permettre d'envoyer l'événement
        // aux joueurs qu'une table a été créée
        EvenementNouvelleSalle evNouvelleSalle = new EvenementNouvelleSalle(
                nouvelleSalle.getRoomName(""),
                nouvelleSalle.protegeeParMotDePasse(),
                createurSalle,
                nouvelleSalle.getRoomDescription(""),
                nouvelleSalle.getMasterTime(),
                nouvelleSalle.getRoomId(),
                nouvelleSalle.getRoomType(),
                g_types
                );
        
        for (JoueurHumain objJoueur : lstJoueursConnectes.values())
        {
        	//On n'envoi pas d'événement au créateur de la salle
        	if (!objJoueur.obtenirNom().equalsIgnoreCase(createurSalle))                   
        		// Obtenir un numéro de commande pour le joueur courant, créer
        		// un InformationDestination et l'ajouter à l'événement
        		evNouvelleSalle.ajouterInformationDestination(
        				new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
        						objJoueur.obtenirProtocoleJoueur())
        		);
        }
        // Ajouter le nouvel événement créé dans la liste d'événements à traiter
        objGestionnaireEvenements.ajouterEvenement(evNouvelleSalle);
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
    private void preparerEvenementJoueurDeconnecte(String nomUtilisateur) {
        // Créer un nouvel événement qui va permettre d'envoyer l'événement
        // aux joueurs qu'un joueur s'est déconnecté
        EvenementJoueurDeconnecte joueurDeconnecte = new EvenementJoueurDeconnecte(nomUtilisateur);


        // Passer tous les joueurs connectés et leur envoyer un événement
        for (JoueurHumain objJoueur : lstJoueursConnectes.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de se déconnecter du serveur de jeu, alors on peut
            // envoyer un événement à cet utilisateur
            if (!objJoueur.obtenirNom().equalsIgnoreCase(nomUtilisateur)) {
                // Obtenir un numéro de commande pour le joueur courant, créer
                // un InformationDestination et l'ajouter à l'événement
                joueurDeconnecte.ajouterInformationDestination(new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel événement créé dans la liste d'événements à traiter
        objGestionnaireEvenements.ajouterEvenement(joueurDeconnecte);
    }

    public GestionnaireCommunication obtenirGestionnaireCommunication() {
        return objGestionnaireCommunication;
    }

    public GestionnaireEvenements obtenirGestionnaireEvenements() {
        return objGestionnaireEvenements;
    }

    public GestionnaireTemps obtenirGestionnaireTemps() {
        return objGestionnaireTemps;
    }

    public TacheSynchroniser obtenirTacheSynchroniser() {
        return objTacheSynchroniser;
    }

    public GestionnaireBD obtenirGestionnaireBD() {
        return objGestionnaireBD;
    }

    /*
     * Cette fonction ajouter un joueur à la liste des joueurs déconnectés. Si le
     * joueur tente de se reconnecter, il sera possible qu'il reprenne la partie
     */
    public void ajouterJoueurDeconnecte(JoueurHumain joueurHumain) {

        synchronized (lstJoueursDeconnectes) {
            lstJoueursDeconnectes.put(joueurHumain.obtenirNom(), joueurHumain);
        }
    }

    /*
     * Cette fonction va nous permettre de savoir si ce joueur a été
     * déconnecté pendant une partie.
     */
    public boolean estJoueurDeconnecte(String nomUtilisateur) {
        synchronized (lstJoueursDeconnectes) {
            return lstJoueursDeconnectes.containsKey(nomUtilisateur);
        }

    }

    /*
     * Cette fonction retourne une référence vers un objet JoueurHumain
     * d'un joueur déconnecté. Cet objet contient toutes les informations
     * à propos de la partie qui était en cours
     */
    public JoueurHumain obtenirJoueurHumainJoueurDeconnecte(String nomUtilisateur) {
        synchronized (lstJoueursDeconnectes) {
            return lstJoueursDeconnectes.get(nomUtilisateur);
        }
    }

    /*
     * Cette fonction permet d'enlever un joueur déconnecté de la liste
     * des joueurs déconnectés, soit parce qu'il vient de se reconnecter,
     * ou car la partie qu'il avait commencée et qui était en suspend est terminée
     */
    public void enleverJoueurDeconnecte(String nomUtilisateur) {
        synchronized (lstJoueursDeconnectes) {
            lstJoueursDeconnectes.remove(nomUtilisateur);
        }
    }

    public void clearDeconnectedPlayersList() {
    	synchronized (lstJoueursDeconnectes) {
    	   lstJoueursDeconnectes.clear();
    	}
    }

    public HashMap<String, JoueurHumain> obtenirListeJoueursDeconnectes() {
        synchronized (lstJoueursDeconnectes) {
            return lstJoueursDeconnectes;
        }
    }

    /**
     * Find the language_id corresponding to the 2-letter language string.
     * @param language the 2-letter language to look for ("fr","en")
     * @return the language id corresponding to the specified language short name.
     *         If not found, we return the id of "en", if this is still not found
     *         the first existing language_id is returned.
     */
    public int getLanguageId(String language) {
        Integer defaultLanguageId = null;
        for (Map.Entry<Integer,String> shortLanguageEntry : languagesMap.get(0).entrySet()) {
            if (language.equalsIgnoreCase(shortLanguageEntry.getValue()))
                return shortLanguageEntry.getKey();
            if (defaultLanguageId == null || shortLanguageEntry.getValue().equals("en"))
                defaultLanguageId = shortLanguageEntry.getKey();
        }
        return defaultLanguageId;

    }
    
    public String getLanguageShortName(int language_id) {
        String shortName = languagesMap.get(0).get(language_id);
        if (shortName != null)
            return shortName;
        return "en";
    }

    /**
     * @param language
     * @return the keywords
     */
    public TreeMap<Integer, String> getKeywordsMap(String language) {
        return keywordsMap.get(getLanguageId(language));
    }

    public TreeMap<Integer, String> getLanguagesMap(String language) {
        return languagesMap.get(getLanguageId(language));
    }

    public TreeMap<Integer, String> getGameTypesMap() {
        return gameTypesMap;
    }

	public void setNewTimer() {
		objGestionnaireTemps = new GestionnaireTemps();
	}
	
	public static int genererNbAleatoire(int max)
    {
    	//Random objRandom = new Random();
    	return objRandom.nextInt(max);
    }


	public int getActiveTablesNumber() {
		int nb = 0;
		
		for(Salle room : lstSalles.values())
			nb += room.getActiveTablesNUmber();
		return nb;
	}

}// end class

