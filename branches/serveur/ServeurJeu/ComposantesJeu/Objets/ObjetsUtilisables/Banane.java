package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import ClassesUtilitaires.BananaTask;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import java.util.Timer;

/**
 * @author Fran�ois Gingras
 * changed Oloieri Lilian
 * last change 10 March 2010
 */
public class Banane extends ObjetUtilisable 
{
	// Cette constante sp�cifie le prix de l'objet courant
	public static final int PRIX = 1;

	// Cette constante affirme que l'objet courant n'est pas limit� 
	// lorsqu'on l'ach�te (c'est-�-dire qu'un magasin n'�puise jamais 
	// son stock de cet objet)
	public static final boolean EST_LIMITE = false;

	// Cette constante affirme que l'objet courant ne peut �tre arm� 
	// et d�pos� sur une case pour qu'un autre joueur tombe dessus. Elle 
	// ne peut seulement �tre utilis�e imm�diatement par le joueur
	public static final boolean PEUT_ETRE_ARME = false;

	// Cette constante d�finit le nom de cet objet
	public static final String TYPE_OBJET = "Banane";
	
	//private static final long Seconds = 90;

	/**
	 * Constructeur de la classe Banane qui permet de d�finir les propri�t�s 
	 * propres � l'objet courant.
	 *
	 * @param in id : Le num�ro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit �tre visible ou non
	 */
	public Banane(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BANANE, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}

	public static BananaTask utiliserBanane(JoueurHumain player, long delay)
	{
			// Create TimerTask and Timer.
			BananaTask bTask = new BananaTask(player);
			Timer bTimer = new Timer();
			//bkTimer.cancel();
			
			// effects of Banana
			player.obtenirPartieCourante().getBananaState().setisUnderBananaEffects(true);
			player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 2);
						
			// used timer to take out effects of banana after the needed time
			bTimer.schedule(bTask, delay);
			return bTask;
						
	}
	
	// if VitualPlayer use the Banana
	public static BananaTask utiliserBanane(JoueurVirtuel player, long delay)
	{
		
			// Create TimerTask and Timer.
			BananaTask bTask = new BananaTask(player);
			Timer bTimer = new Timer();
					
			// effects of Banana
			player.getBananaState().setisUnderBananaEffects(true);
									
			// used timer to take out effects of banana after the needed time
			bTimer.schedule(bTask, delay);
			return bTask;
			
	}
}// end class