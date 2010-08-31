package ServeurJeu.Temps;

import java.util.TimerTask;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Banana on the player
 * after the amount of time declared in Banane (Seconds)
 * (reduced move possibility and harder questions)
 * 
 * last change September 2010 Oloieri Lilian
 *
 */

public class BananaHumainTask extends TimerTask {

	private JoueurHumain player;
		
	public BananaHumainTask(JoueurHumain player){
		this.player = player;
	}
	
	
	// override abstract run methode 
	public void run() {
	    if(player != null){
	    	player.obtenirPartieCourante().getBananaState().setisUnderBananaEffects(false);
	    	player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() + 2);
	    	//System.out.println("BananaTask humain!!!!");
	    }
	      
	}// end run

}// end class
