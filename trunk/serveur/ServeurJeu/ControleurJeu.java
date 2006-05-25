package ServeurJeu;

import java.util.Date;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

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
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.TacheSynchroniser;

//TODO: Si un jour on doit modifier le nom d'utilisateur d'un joueur pendant 
//      le jeu, il va falloir ajouter des synchronisation � chaque fois qu'on 
//      fait des v�rifications avec le nom de l'utilisateur.
/**
 * Note importante concernant le traitement des commandes par le 
 * ProtocoleJoueur : Deux fonctions d'un m�me protocole ne peuvent pas �tre
 * trait�es en m�me temps car si le ProtocoleJoueur est en train d'en traiter
 * une, alors il n'est plus � l'�coute pour en recevoir une autre. Pour en 
 * traiter une autre, il doit attendre que le traitement de la premi�re soit
 * termin� et qu'elle retourne une valeur au client. Un autre protocole ne peut
 * pas TODO (pour l'instant) ex�cuter une fonction d'un autre protocole, la 
 * seule chose qui peut se produire est qu'un protocole envoit des �v�nements
 * � d'autres joueurs par leur ProtocoleJoueur, mais aucune fonction n'est
 * ex�cut�e. TODO Il faut peut-�tre v�rifier les conditions pour envoyer
 * l'�v�nement � un joueur, car elles pourraient acc�der � des donn�es 
 * importantes du joueur ou du protocole du joueur. M�me si le 
 * VerificateurConnexions tente d'arr�ter un protocole qui est en train de 
 * traiter une commande, c'est le socket du protocole qui est ferm�, et la
 * d�connexion du joueur va s'effectuer si on veut lire ou �crire sur le
 * socket. Cela veut donc dire qu'on n'a pas � valider que la m�me fonction
 * puisse �tre appel�e pour le m�me protocole et joueur. 
 *  
 * @author Jean-Fran�ois Brind'Amour
 */
public class ControleurJeu 
{
	static private Logger objLogger = Logger.getLogger( ControleurJeu.class );
	
	// Cet objet permet de g�rer toutes les interactions avec la base de donn�es
	private GestionnaireBD objGestionnaireBD;
	
	// Cet objet permet de g�rer toutes les communications entre le serveur et
	// les clients (les joueurs)
	private GestionnaireCommunication objGestionnaireCommunication;
	
	// Cet objet permet de g�rer tous les �v�nements devant �tre envoy�s du
	// serveur aux clients (l'�v�nement ping n'est pas g�r� par ce gestionnaire)
	private GestionnaireEvenements objGestionnaireEvenements;
	
	private TacheSynchroniser objTacheSynchroniser;
	
	private GestionnaireTemps objGestionnaireTemps;
	
	static private int intStepSynchro = 30;
	
	// Cet objet est une liste des joueurs qui sont connect�s au serveur de jeu 
	// (cela inclus les joueurs dans les salles ainsi que les joueurs jouant
	// pr�sentement dans des tables de jeu)
	private TreeMap lstJoueursConnectes;
	
	// Cet objet est une liste des salles cr��es qui se trouvent dans le serveur
	// de jeu. Chaque �l�ment de cette liste a comme cl� le nom de la salle
	private TreeMap lstSalles;
	
	/**
	 * Constructeur de la classe ControleurJeu qui permet de cr�er le gestionnaire 
	 * des communications, le gestionnaire d'�v�nements et le gestionnaire de bases 
	 * de donn�es. 
	 */
	public ControleurJeu() 
	{
		super();
		
		//DOMConfigurator.configure( "log4j.xml" );
		//BasicConfigurator.configure();

		objLogger.info( "Le serveur d�marre : " + new Date().toString() );
		
		// Cr�er une liste des joueurs
		lstJoueursConnectes = new TreeMap();
		
		// Cr�er une liste des salles
		lstSalles = new TreeMap();
		
		// Cr�er un nouveau gestionnaire d'�v�nements
		objGestionnaireEvenements = new GestionnaireEvenements();
		
		// Cr�er un nouveau gestionnaire de base de donn�es MySQL
		objGestionnaireBD = new GestionnaireBD(this);
		
		// Charger les salles en m�moire
		objGestionnaireBD.chargerSalles(objGestionnaireEvenements);
		
		objGestionnaireTemps = new GestionnaireTemps();
		objTacheSynchroniser = new TacheSynchroniser();
		objGestionnaireTemps.ajouterTache( objTacheSynchroniser, intStepSynchro );
		
		// Cr�er un nouveau gestionnaire de communication
		objGestionnaireCommunication = new GestionnaireCommunication(this, objGestionnaireEvenements, objGestionnaireBD, objGestionnaireTemps, objTacheSynchroniser);
		
		// Cr�er un thread pour le GestionnaireEvenements
		Thread threadEvenements = new Thread(objGestionnaireEvenements);
		
		// D�marrer le thread du gestionnaire d'�v�nements
		threadEvenements.start();
		
		// D�marrer l'�coute des connexions clientes
		objGestionnaireCommunication.ecouterConnexions();
	}
	
	/**
	 * Cette fonction permet de d�terminer si le joueur dont le nom d'utilisateur
	 * est pass� en param�tre est d�j� connect� au serveur de jeu ou non.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @return false : Le joueur n'est pas connect� au serveur de jeu 
	 * 		   true  : Le joueur est d�j� connect� au serveur de jeu
	 * @synchronism Cette fonction est synchronis�e sur la liste des 
	 * 				joueurs connect�s. 
	 */
	public boolean joueurEstConnecte(String nomUtilisateur)
	{
	    // Synchroniser l'acc�s � la liste des joueurs connect�s
	    synchronized (lstJoueursConnectes)
	    {
			// Retourner si le joueur est d�j� connect� au serveur de jeu ou non
			return lstJoueursConnectes.containsKey(nomUtilisateur);	        
	    }
	}

	/**
	 * Cette fonction permet de valider que les informations du joueur pass�es
	 * en param�tres sont correctes (elles existent et concordent). On suppose
	 * que le joueur n'est pas connect� au serveur de jeu.
	 * 
	 * @param ProtocoleJoueur protocole : Le protocole du joueur
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @param String motDePasse : Le mot de passe du joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 							g�n�rer un num�ro de commande pour le retour de
	 * 							l'appel de fonction
	 * @return JoueurNonConnu : Le nom d'utilisateur du joueur n'est pas connu par le 
	 * 				            serveur ou le mot de passe ne concorde pas au nom 
	 * 				            d'utilisateur donn�
	 * 		   JoueurDejaConnecte : Le joueur a tent� de se connecter en m�me temps 
	 * 								� deux endroits diff�rents  
	 * 		   Succes : L'authentification a r�ussie
	 * @synchronism  Cette fonction est synchronis�e par rapport � la liste des
	 * 				 joueurs connect�s car on fait un synchronized sur elle, 
	 * 				 elle est synchronis� par rapport au joueur du protocole car 
	 * 				 les seules fonctions qui acc�dent au protocole sont le 
	 * 				 VerificateurConnexions (fait juste un acc�s au protocole et 
	 * 				 non un acc�s au joueur du protocole donc c'est correct), le 
	 * 				 protocole lui-m�me (le protocole ne traite qu'une commande 
	 * 				 � la fois, donc on se fou que lui utilise son joueur) et la 
	 * 				 fonction deconnecterJoueur (elle ne peut pas �tre ex�cut�e 
	 * 				 en m�me temps que l'authentification car le protocole ne 
	 * 				 traite qu'une commande � la fois, m�me si la demande vient 
	 * 				 du VerificateurConnexions).
	 */
	public String authentifierJoueur(ProtocoleJoueur protocole, String nomUtilisateur, 
	        						 String motDePasse, boolean doitGenererNoCommandeRetour)
	{
	    // D�claration d'une variable qui va contenir le r�sultat � retourner
	    // � la fonction appelante, soit les valeurs de l'�num�ration 
	    // ResultatAuthentification
	    String strResultatAuthentification = ResultatAuthentification.JoueurNonConnu;
	    
		// D�terminer si le joueur dont le nom d'utilisateur est pass� en 
		// param�tres existe et mettre le r�sultat dans une variable bool�enne
		boolean bolResultatRecherche = objGestionnaireBD.joueurExiste(nomUtilisateur, motDePasse); 

		// Si les informations de l'utilisateur sont correctes, alors le 
		// joueur est maintenant connect� au serveur de jeu
		if (bolResultatRecherche == true)
		{
			// Cr�er un nouveau joueur humain contenant les bonnes informations
			JoueurHumain objJoueurHumain = new JoueurHumain(protocole, nomUtilisateur, 
															protocole.obtenirAdresseIP(),
															protocole.obtenirPort());
			
			// Trouver les informations sur le joueur dans la BD et remplir le 
			// reste des champs tels que les droits
			objGestionnaireBD.remplirInformationsJoueur(objJoueurHumain);
			
			// � ce moment, comme il se peut que le m�me joueur tente de se 
			// connecter en m�me temps par 2 protocoles de joueur, alors si
			// �a arrive on va le v�rifier juste une fois qu'on a fait tous 
			// les appels � la base de donn�es, il faut cependant s'assurer
			// que personne ne touche � la liste de joueurs pendant ce temps-l�.
			// C'est un cas qui ne devrait vraiment pas arriver souvent, car
			// normalement une erreur devrait �tre renvoy�e au client si 
			// celui-ci essaie de se connecter � deux endroits en m�me temps.
			// Pour des raisons de performance, on fonctionne comme cela, car 
			// chercher dans la base de donn�es peut �tre assez long
			synchronized (lstJoueursConnectes)
			{
				// Si le joueur est d�j� pr�sentement connect�, on ne peut
				// pas finaliser la connexion du joueur
				if (joueurEstConnecte(nomUtilisateur) == true)
				{
				    // On va retourner que le joueur est d�j� connect�
				    strResultatAuthentification = ResultatAuthentification.JoueurDejaConnecte;
				}
				else
				{
					// D�finir la r�f�rence vers le joueur humain
					protocole.definirJoueur(objJoueurHumain);
					
					// Ajouter ce nouveau joueur dans la liste des joueurs connect�s
					// au serveur de jeu
					lstJoueursConnectes.put(nomUtilisateur, objJoueurHumain);
					
					// Si on doit g�n�rer le num�ro de commande de retour, alors
					// on le g�n�re, sinon on ne fait rien (�a devrait toujours
					// �tre vrai, donc on le g�n�re tout le temps)
					if (doitGenererNoCommandeRetour == true)
					{
						// G�n�rer un nouveau num�ro de commande qui sera 
					    // retourn� au client
						protocole.genererNumeroReponse();					    
					}
					
				    // L'authentification a r�ussie
				    strResultatAuthentification = ResultatAuthentification.Succes;
					
					// Pr�parer l'�v�nement de nouveau joueur. Cette fonction 
				    // va passer les joueurs et cr�er un InformationDestination 
				    // pour chacun et ajouter l'�v�nement dans la file de gestion 
				    // d'�v�nements
					preparerEvenementJoueurConnecte(nomUtilisateur);
				}
			}
		}
		
		return strResultatAuthentification;
	}
	
	/**
	 * Cette m�thode permet de d�connecter le joueur pass� en param�tres. Il 
	 * faut enlever toute trace du joueur du serveur de jeu et en aviser les
	 * autres participants se trouvant au m�me endroit que le joueur d�connect� 
	 * (� une table de jeu).
	 * 
	 * @param JoueurHumain joueur : Le joueur humain ayant fait la demande 
	 * 								de d�connexion
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @synchronism � ce niveau-ci, il n'y a pas vraiment de restrictions sur
	 * 				l'ordre d'arriv�e des �v�nements indiquant que le joueur
	 * 				a quitt� la table ou la salle. De plus, aucune autre 
	 * 				fonction ne peut modifier le joueur, puisque deux 
	 * 				fonctions d'un m�me protocole ne peuvent pas �tre 
	 * 				ex�cut�es en m�me temps. Cependant, pour enlever un
	 * 				joueur de la liste des joueurs connect�s, il faut
	 * 				s'assurer que personne d'autre ne va toucher � la liste
	 * 				des joueurs connect�s.
	 */
	public void deconnecterJoueur(JoueurHumain joueur, boolean doitGenererNoCommandeRetour)
	{
		// Si le joueur courant est dans une salle, alors on doit le retirer de
		// cette salle (pas besoin de faire la synchronisation sur la salle 
		// courante du joueur car elle ne peut �tre modifi�e par aucun autre
		// thread que celui courant)
		if (joueur.obtenirSalleCourante() != null)
		{
			// Le joueur courant qui la salle dans laquelle il se trouve
			joueur.obtenirSalleCourante().quitterSalle(joueur, false);
		}
		
		// Emp�cher d'autres thread de venir utiliser la liste des joueurs
		// connect�s au serveur de jeu pendant qu'on d�connecte le joueur
		synchronized (lstJoueursConnectes)
		{
			// Enlever le joueur de la liste des joueurs connect�s
			lstJoueursConnectes.remove(joueur.obtenirNomUtilisateur());
			
			// Enlever la r�f�rence du protocole du joueur vers son joueur humain 
			// (cela va avoir pour effet que le protocole du joueur va penser que
			// le joueur n'est plus connect� au serveur de jeu)
			joueur.obtenirProtocoleJoueur().definirJoueur(null);
			
			// Si on doit g�n�rer le num�ro de commande de retour, alors
			// on le g�n�re, sinon on ne fait rien
			if (doitGenererNoCommandeRetour == true)
			{
				// G�n�rer un nouveau num�ro de commande qui sera 
			    // retourn� au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}
			
			// Aviser tous les joueurs connect�s au serveur de jeu qu'un joueur
			// s'est d�connect�
			preparerEvenementJoueurDeconnecte(joueur.obtenirNomUtilisateur());		    
		}
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des joueurs connect�s au serveur
	 * de jeu. La vraie liste est retourn�e.
	 * 
	 * @return TreeMap : La liste des joueurs connect�s au serveur de jeu 
	 *                   (c'est la r�f�rence vers la liste du ControleurJeu, il 
	 *                   faut donc traiter le cas du multithreading)
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle doit
	 * 				l'�tre par l'appelant de cette fonction tout d�pendant
	 * 				du traitement qu'elle doit faire
	 */
	public TreeMap obtenirListeJoueurs()
	{
		return lstJoueursConnectes;
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des salles du serveur de jeu.
	 * La vraie liste est retourn�e.
	 * 
	 * @return TreeMap : La liste des salles du serveur de jeu (c'est la 
	 * 				     r�f�rence vers la liste du ControleurJeu, il faut donc
	 *                   traiter le cas du multithreading)
	 * @synchronism Cette fonction n'est pas synchronis�e ici et il n'est pas
	 * 				vraiment n�cessaire de le faire dans la fonction appelante
	 * 				pour ce qui est de la corruption des donn�es suite � 
	 * 				l'ajout et/ou au retrait d'une salle, car �a ne peut pas
	 * 				se produire.
	 */
	public TreeMap obtenirListeSalles()
	{
		return lstSalles;
	}

	/**
	 * Cette fonction permet de d�terminer si la salle dont le nom est pass�
	 * en param�tres existe d�j� ou non.
	 * 
	 * @param String nomSalle : Le nom de la salle
	 * @return false : La salle n'existe pas 
	 * 		   true  : La salle existe d�j�
	 * @synchronism Cette fonction n'a pas besoin d'�tre synchronis�e car
	 * 				on ne peut pas ajouter ou enlever des salles par le
	 * 				serveur de jeu (sauf quand celui-ci d�marre, mais aucun
	 * 				joueur n'est connect� � ce moment-l�)
	 */
	public boolean salleExiste(String nomSalle)
	{
		// Retourner si la salle existe d�j� ou non
		return lstSalles.containsKey(nomSalle);	        
	}
	
	/**
	 * Cette m�thode permet d'ajouter une nouvelle salle dans la liste des 
	 * salles du contr�leur de jeu.
	 * 
	 * @param Salle nouvelleSalle : La nouvelle salle � ajouter dans la liste
	 * @synchronism Cette fonction n'a pas besoin d'�tre synchronis�e car
	 * 				elle est ex�cut�e seulement lors du d�marrage du serveur
	 * 				et il n'y a aucun joueur de connect� � ce moment l�.
	 */
	public void ajouterNouvelleSalle(Salle nouvelleSalle)
	{
	    // Ajouter la nouvelle salle dans la liste des salles du 
	    // contr�leur de jeu
	    lstSalles.put(nouvelleSalle.obtenirNomSalle(), nouvelleSalle);	        
	}
	
	/**
	 * Cette fonction permet de valider que le mot de passe pour entrer dans la
	 * salle est correct. On suppose suppose que le joueur n'est pas dans aucune
	 * salle. Cette fonction va avoir pour effet de connecter le joueur dans la
	 * salle dont le nom est pass� en param�tres.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la salle
	 * @param String nomSalle : Le nom de la salle dans laquelle entrer
	 * @param String motDePasse : Le mot de passe pour entrer dans la salle
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return false : Le mot de passe pour entrer dans la salle n'est pas
	 * 				   le bon
	 * 		   true  : Le joueur a r�ussi � entrer dans la salle
	 * @synchronism Cette fonction n'a pas besoin d'�tre synchronis�e, car 
	 * 				elle ne modifie pas la liste des salles et aucune autre
	 * 				fonction ne le fait. Cependant, la m�thode entrerSalle
	 * 				de la salle devra �tre synchronis�e.
	 */
	public boolean entrerSalle(JoueurHumain joueur, String nomSalle, 
	        				   String motDePasse, boolean doitGenererNoCommandeRetour)
	{
		// On retourne le r�sultat de l'entr�e du joueur dans la salle
		return ((Salle) lstSalles.get(nomSalle)).entrerSalle(joueur, motDePasse, doitGenererNoCommandeRetour);
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement de l'arriv�e d'un nouveau
	 * joueur. Cette m�thode va passer tous les joueurs connect�s et pour ceux 
	 * devant �tre avertis (tous sauf le joueur courant pass� en param�tre),
	 * on va obtenir un num�ro de commande, on va cr�er un 
	 * InformationDestination et on va ajouter l'�v�nement dans la file 
	 * d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel de cette
	 * fonction, la liste des joueurs connect�s est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de se connecter au serveur de jeu
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				par l'appelant (authentifierJoueur).
	 */
	private void preparerEvenementJoueurConnecte(String nomUtilisateur)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un nouveau joueur s'est connect�
	    EvenementJoueurConnecte joueurConnecte = new EvenementJoueurConnecte(nomUtilisateur);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// lstJoueursConnectes (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueursConnectes.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs connect�s et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de se connecter au serveur de jeu, alors on peut
			// envoyer un �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
				joueurConnecte.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurConnecte);
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement de la d�connexion d'un
	 * joueur. Cette m�thode va passer tous les joueurs connect�s et pour ceux 
	 * devant �tre avertis (tous sauf le joueur courant pass� en param�tre),
	 * on va obtenir un num�ro de commande, on va cr�er un 
	 * InformationDestination et on va ajouter l'�v�nement dans la file 
	 * d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel de cette
	 * fonction, la liste des joueurs connect�s est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de se d�connecter du serveur de jeu
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				par l'appelant (deconnecterJoueur).
	 */
	private void preparerEvenementJoueurDeconnecte(String nomUtilisateur)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur s'est d�connect�
	    EvenementJoueurDeconnecte joueurDeconnecte = new EvenementJoueurDeconnecte(nomUtilisateur);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// lstJoueursConnectes (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueursConnectes.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs connect�s et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de se d�connecter du serveur de jeu, alors on peut
			// envoyer un �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
			    joueurDeconnecte.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            												objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurDeconnecte);
	}
	
	/**
	 * Cette m�thode est le point d'entr�e du serveur. Elle ne fait que cr�er 
	 * un nouveau contr�leur de jeu.
	 * 
	 * @param String[] args : les arguments pass�s en param�tre lors de l'appel
	 * 						  de l'application 
	 */
	public static void main(String[] args) 
	{
		ControleurJeu objJeu = new ControleurJeu();
	}
}
