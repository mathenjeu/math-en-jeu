package ServeurJeu.ComposantesJeu.Joueurs;

import java.awt.Point;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.ComposantesJeu.Tables.Table;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Fournier
 * 
 * changed Oloieri Lilian
 * last change August 2011
 */
public class JoueurVirtuel extends Joueur implements Runnable{

	// Déclaration d'une référence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;	

	// Cette variable permet de savoir s'il faut arrêter le thread ou non
	private boolean bolStopThread;

	// Déclaration d'une référence vers le controleur jeu
	//private ControleurJeu objControleurJeu;	

	// Constante pour accélérer les déplacements
	// (voir fonction pause() )
	private static final boolean ccFast = false;

	
	// Objet logger pour afficher les erreurs dans le fichier log
	static private Logger objLogger = Logger.getLogger(JoueurVirtuel.class);	
	
	// Déclaration d'une référence vers l'objet gardant l'information sur la
	// partie courant de la table où le joueur se trouve (null si le joueur 
	// n'est dans aucune table)
	private InformationPartieVirtuel objPartieCourante;


	/**
	 * Constructeur de la classe JoueurVirtuel qui permet d'initialiser les 
	 * membres privés du joueur virtuel
	 * 
	 * @param nom Nom du joueur virtuel
	 * @param niveauDifficulte Le niveau de difficulté pour ce joueur virtuel
	 * @param tableCourante La table sur laquelle le joueur joue
	 * @param idPersonnage l'id du personnage utilisé par le joueur virtuel
	 * 
	 */
	public JoueurVirtuel(String nom, Table tableCourante)
	{  
		super(nom);
				
		// Faire la référence vers le gestionnaire d'évenements
		objGestionnaireEv = tableCourante.getObjGestionnaireEvenements();

		// Cette variable sert à arrêter la thread lorsqu'à true
		bolStopThread = false;		
	
		setRole(1);
	}


	/**
	 * Cette méthode est appelée lorsqu'une partie commence. C'est la thread
	 * qui fait jouer le joueur virtuel.
	 * 
	 */
	public void run()
	{
		Thread.currentThread().setPriority( Thread.currentThread().getPriority() - 1 );
		try
		{	
			// Assigner le priorité TRACE au logger pour qu'il puisse
			// écrire les traces des exceptions si il en arrivent
			objLogger.setLevel((Level) Level.ALL);

			// Cette variable conserve la case sur laquelle le joueur virtuel
			// tente de se déplacer
			Point objPositionIntermediaire = null;			

			while(bolStopThread == false && objPartieCourante.obtenirTable().obtenirTempsRestant() > 2)
			{		
				objPartieCourante.analyseVirtualNextStep(objPositionIntermediaire);

			}// end while


			this.arreter();

		}
		catch (Exception e)
		{
			// Envoyer la trace de l'erreur dans le log
			objLogger.trace(GestionnaireMessages.message("joueur_virtuel.erreur_thread") + strNom, e);

			// Envoyer la trace de l'erreur à l'écran
			e.printStackTrace();

			this.arreter();

		}
	}


	
	protected void arreter(){
		
		this.bolStopThread = true;
	}
	
	
	/**
	 * Cette méthode permet de définir la référence vers l'information sur la 
	 * partie courante du joueur.
	 * 
	 * @param partieCourante L'information sur la partie courante du joueur.
         *       
	 */
	public void definirPartieCourante(InformationPartieVirtuel partieCourante)
	{
		objPartieCourante = partieCourante;
	}
	
	/**
	 * Cette fonction permet de retourner la référence vers l'information sur
	 * la partie courante de la table dans laquelle se trouve le joueur présentement.
	 * 
	 * @return InformationPartie : L'information sur la partie courante du joueur.
	 * 				   Si null est retourné, alors le joueur ne se trouve dans
	 * 				   aucune table.
	 */
	public InformationPartieVirtuel obtenirPartieCourante()
	{
		return objPartieCourante;
	}

	/*
	 * Cette fonction fait une pause de X secondes émulant une réflexion
	 * par le joueur virtuel
	 */
	protected void pause(Integer nbMiliSecondes)
	{
		try
		{
			if (!ccFast)
			{
				Thread.sleep(nbMiliSecondes);
			}
			else
			{
				Thread.sleep(500);
			}

		}
		catch(InterruptedException e)
		{ 
			objLogger.error("Error to did thead Virtual to sleep..." + e.getMessage(), e);
			pause(1);
		}

	}


	public GestionnaireEvenements obtenirGestionnaireEvenements()
	{
		return objGestionnaireEv;
	}

	

	/* Cette fonction permet à la boucle dans run() de s'arrêter
	 */
	public void arreterThread()
	{ 
		bolStopThread = true;    	        	                
	}
	
	public boolean getBoolStopThread()
	{
		return bolStopThread;
	}
	
	/*
	 * Cette méthode statique permet de valider un niveau de joueur virtuel en format String
	 * 
	 * @param String s : le Niveau des joueurs virtuel en chaîne de caractères (Facile, Intermediaire,..)
	 * @return boolean : true : si le paramètre est valide
	 * 					 false : si le paramètre n'est pas valide
	 */
	public static boolean validerParamNiveau(String s)
	{
		return (s.equals("Aucun") || s.equals("Facile") || s.equals("Intermediaire") ||
				s.equals("Difficile") || s.equals("TresDifficile"));
	}
	
	public InformationPartie getPlayerGameInfo()
	{
		return objPartieCourante;
	}
	
} // end class