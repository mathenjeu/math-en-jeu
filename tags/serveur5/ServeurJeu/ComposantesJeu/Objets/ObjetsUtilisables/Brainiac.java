package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import java.util.Timer;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.Temps.BrainiacHumainTask;
import ServeurJeu.Temps.BrainiacVirtuelTask;

/**
 * @author Oloieri Lilian
 *
 */

public class Brainiac extends ObjetUtilisable {

	public static final int PRIX = 1;

	// Cette constante affirme que l'objet courant n'est pas limité 
	// lorsqu'on l'achète (c'est-à-dire qu'un magasin n'épuise jamais 
	// son stock de cet objet)
	public static final boolean EST_LIMITE = false;

	// Cette constante affirme que l'objet courant ne peut être armé 
	// et déposé sur une case pour qu'un autre joueur tombe dessus. Elle 
	// ne peut seulement être utilisée immédiatement par le joueur
	public static final boolean PEUT_ETRE_ARME = false;

	// Cette constante définit le nom de cet objet
	public static final String TYPE_OBJET = "Brainiac";
	
	//private static final long Seconds = 90;

	/**
	 * Constructeur de la classe Braniac qui permet de définir les propriétés 
	 * propres à l'objet courant.
	 *
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être visible ou non
	 */
	public Brainiac(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BRAINIAC, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}

	public static BrainiacHumainTask utiliserBrainiac(JoueurHumain player, long delay)
	{
		//first cancel the Banana
		player.obtenirPartieCourante().getBananaState().setOffBanana();
		
		// player under Braniac
		// Create TimerTask and Timer.
		BrainiacHumainTask bTask = new BrainiacHumainTask(player);
		Timer bTimer = new Timer();
					
		// effect of Braniac - the rest in the BraniacState
		player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() + 1);
								
		// used timer to take out effects of brainiac after the needed time
		bTimer.schedule(bTask, delay);
		//System.out.println("BraniacTask !!!! " + bTimer + " " + Seconds + " " + bTask);
		return bTask;
			
	}
	
	
	
	public static BrainiacVirtuelTask utiliserBrainiac(JoueurVirtuel player, long delay)
	{		
		//first cancel the Banana
		player.getBananaState().setOffBanana();		
		
		// Create TimerTask and Timer.
		BrainiacVirtuelTask bTask = new BrainiacVirtuelTask(player);
		Timer bTimer = new Timer();
		
		player.getBrainiacState().setInBrainiac(true);
		// used timer to take out effects of braniac after the needed time
		bTimer.schedule(bTask, delay);
		
		return bTask;

	}// end method
}// end class
