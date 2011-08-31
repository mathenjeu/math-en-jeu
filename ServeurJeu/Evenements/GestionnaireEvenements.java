package ServeurJeu.Evenements;

import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 * @author Jean-François Brind'Amour
 * 
 * changed Oloieri Lilian 2010
 */
public class GestionnaireEvenements implements Runnable
{
	// Déclaration d'une liste d'événements
	private LinkedList<Evenement> lstEvenements;

	// Cette variable permet de savoir s'il faut arrêter le thread ou non
	private boolean bolStopThread = false;

	private static final Logger objLogger = Logger.getLogger(GestionnaireEvenements.class);
	/**
	 * Constructeur de la classe GestionnaireEvenements qui permet d'initialiser
	 * la liste des événements
	 */
	public GestionnaireEvenements() 
	{
		super();

		// Créer une liste des événements
		lstEvenements = new LinkedList<Evenement>();
	}

	/**
	 * Cette méthode est appelée automatiquement par le thread du joueur et elle
	 * permet de traiter les événements qu'il y a en attente d'être envoyés.
	 */
	public void run()
	{
		Thread.currentThread().setPriority( Thread.currentThread().getPriority() + 1 );
		// Boucler tant qu'il ne faut pas arrêter le thread
		while (bolStopThread == false)
		{
			// Si la liste des événements contient au moins un événement, alors
			// on va traiter celui qui est au début du vecteur
			if (!lstEvenements.isEmpty())
			{
				// Faire la référence vers l'événement se trouvant au début de 
				// la liste
				Evenement evenementPrioritaire =  lstEvenements.poll();

				// Envoyer l'événement à tous les joueurs qui doivent le recevoir
				evenementPrioritaire.envoyerEvenement();

			}


			try
			{
				// Stopper le thread du gestionnaire d'événements pour
				// laisser un moment de répit au CPU
				Thread.sleep(5);

			}
			catch (InterruptedException ie) 
			{				
				objLogger.error(" Error - sleep is canceled in GestEven" + ie.getMessage());
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Cette méthode permet d'ajouter l'événement passé en paramètres à la liste
	 * des événements à traiter.
	 * 
	 * @param Evenement evenementATraiter : L'événement à ajouter à la fin de
	 * 										la liste d'événements
	 */
	public void ajouterEvenement(Evenement evenementATraiter)
	{
		// Ajouter un nouvel événement dans la liste d'événements à traiter
		lstEvenements.add(evenementATraiter);
	}

	/**
	 * Cette méthode permet d'arrêter le thread du gestionnaire d'événements.
	 * Il n'est plus possible de relancer le gestionnaire par la suite.
	 */
	public void arreterGestionnaireEvenements()
	{
		this.lstEvenements.clear();
		bolStopThread = true;
	}

	protected void finalize(){
		
		this.lstEvenements.clear();
		bolStopThread = true;
	}
}
