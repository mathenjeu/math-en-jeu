package ServeurJeu.Communications;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author Jean-François Brind'Amour
 */
public class VerificateurConnexions implements Runnable
{
	// Déclaration d'une référence vers le gestionnaire des communications
	private final GestionnaireCommunication objGestionnaireCommunication;
	
	// Cette variable permet de savoir s'il faut arrêter le thread ou non
	private boolean bolStopThread;
	
	// Déclaration d'une variable qui va compter le nombre de fois que 
	// des messages ping ont été envoyés (une fois rendu à 10, ça va 
	// retourner à 0)
	private int intCompteurPing;
	
	// Déclaration d'une liste de ProtocoleJoueur pour les clients qui 
	// ont répondu au ping
	private LinkedList<ProtocoleJoueur> lstClientsPresents;
	
	/**
	 * Constructeur de la classe VerificateurConnexions qui permet de garder une 
	 * référence vers le gestionnaire de communication.
	 * 
	 * @param GestionnaireCommunication communication : Le gestionnaire des 
	 * 													communications
	 */
	public VerificateurConnexions(GestionnaireCommunication communication) 
	{
		super();
	
		// Faire la référence vers le gestionnaire de communications
		objGestionnaireCommunication = communication;
		
		// Créer un nouveau vecteur
		lstClientsPresents = new LinkedList<ProtocoleJoueur>();
	}
	
	/**
	 * Cette méthode est appelée automatiquement par le thread du joueur et elle
	 * permet de vérifier toutes les connexion avec le serveur pour savoir si 
	 * elles sont encore actives.
	 */
	public void run()
	{
		// Boucler tant qu'il ne faut pas arrêter le thread
		while (bolStopThread == false)
		{
			// Déclaration d'une liste de ProtocoleJoueur qui contient la 
			// référence vers la liste des ProtocoleJoueur du gestionnaire de 
			// communication
			LinkedList<ProtocoleJoueur> lstProtocoleJoueur = objGestionnaireCommunication.obtenirListeProtocoleJoueur();
			
			// Déclaration d'une liste de ProtocoleJoueur qui va contenir
			// une copie de la liste des ProtocoleJoueur du gestionnaire de 
			// communication
			LinkedList<ProtocoleJoueur> lstCopieProtocoleJoueur = null;
			
			// Empêcher d'autres threads de toucher à la liste des protocoles 
			// de joueur
			synchronized (lstProtocoleJoueur)
			{
				// Faire une copie de la liste des ProtocoleJoueur
				lstCopieProtocoleJoueur = (LinkedList<ProtocoleJoueur>) lstProtocoleJoueur.clone();
			}

			// Passer tous les objets ProtocoleJoueur et envoyer un message ping
			// à chacun pour savoir s'il est là
			for (int i = 0; i < lstCopieProtocoleJoueur.size(); i++)
			{
				// Envoyer un message ping au client courant
				((ProtocoleJoueur) lstCopieProtocoleJoueur.get(i)).envoyerEvenementPing(intCompteurPing);
			}
			
			try
			{
				// Stopper le thread du vérificateur pendant 60 - 100 secondes pour 
				// laisser un moment de répit au CPU
				Thread.sleep(60 * 1000);
			}
			catch (InterruptedException ie) {}
			
			// Passer tous les ProtocoleJoueur et vérifier s'ils ont 
			// répondus au ping. S'ils n'ont pas répondus, alors on va les
			// faire se fermer et s'assurer qu'ils se sont bien enlevé de 
			// la liste. On fait exception pour les joueurs en train de jouer
			// une partie, dans ce cas, on ne les déconnecte pas tant que
			// la partie est en cours
			for (int i = 0; i < lstCopieProtocoleJoueur.size(); i++)
			{
				// Faire la référence vers le ProtocoleJoueur courant
				ProtocoleJoueur protocole = (ProtocoleJoueur) lstCopieProtocoleJoueur.get(i);
				
				// Si le joueur est en train de jouer sur une table, alors
				// on attend, peut-être il se reconnectera et pourra
				// continuer à jouer
				// Si le joueur a fermé le browser ou fait refresh(), il sera déconnecté
				// dans la thread du ProtocoleJoueur, donc n'apparaîtra plus ici
                if (protocole.isPlaying() == false)
                {
    				// Empêcher d'autres threads de toucher à la liste des protocoles
    				// de joueur ayant répondus au ping
    				synchronized (lstClientsPresents)
    				{
    					// Si le protocole courant ne se trouve pas dans la liste des
    					// clients qui ont répondus, alors on peut arrêter le 
    					// ProtocoleJoueur
    					if (lstClientsPresents.contains(protocole) == false)
    					{
    						// Arrêter le ProtocoleJoueur
    						protocole.arreterProtocoleJoueur();
    						
    					}	
    				}
				}
			}				
			
			// Incrémenter le compteur de pings
			intCompteurPing++;
			
			// Si le compteur a dépassé 10, alors on le réinitialise à 0
			if (intCompteurPing >= 11)
			{
				intCompteurPing = 0;
			}
			
			// Vider la liste des clients
			lstClientsPresents.clear();
		}
	}
	
	/**
	 * Cette méthode permet de vérifier si le ping reçu est valide (le numéro
	 * du ping est bien celui courant) et si c'est le cas alors le joueur est
	 * encore connecté au serveur de jeu, il ne faut donc pas le détruire.
	 * 
	 * @param ProtocoleJoueur protocole : le protocole du joueur renvoyant la
	 * 									  réponse à un ping envoyé
	 * @param int compteurPing : le numéro que le joueur avait reçu en 
	 * 							 paramètres permettant d'identifier à quel
	 * 							 moment le ping a été fait
	 */
	public void confirmationPing(ProtocoleJoueur protocole, int compteurPing)
	{
		// Empêcher d'autres threads de toucher à la liste des protocoles
		// de joueur ayant répondus au ping
		synchronized (lstClientsPresents)
		{
			// Si le compteur du ping est le même que celui courant, alors
			// on peut ajouter le protocole du joueur dans la liste de ceux
			// à ne pas détruire
			if (compteurPing == intCompteurPing)
			{
				lstClientsPresents.add(protocole);
			}
		}
	}
	
	/**
	 * Cette méthode permet d'arrêter le thread du vérificateur de connexions.
	 * Il n'est plus possible de relancer le vérificateur par la suite.
	 */
	public void arreterVerificateurConnexions()
	{
		bolStopThread = false;
	}
}