package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Brainiac on the player
 * after the needed amount of time (Seconds)
 * (more move possibility and softer questions)
 *
 * last change 10 September 2010
 */

public class BrainiacHumainTask extends TimerTask {

	private JoueurHumain player;
		
	public BrainiacHumainTask(JoueurHumain player){
		this.player = player;
	}
	
	// override abstract run methode 
	public void run() {
	    if(player != null){
	    	
	    		//player.obtenirPartieCourante().setBraniacsNumberMinus();
	    		player.obtenirPartieCourante().getBrainiacState().setInBrainiac(false);
	    		player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 1);
	    		//System.out.println("BraniacTask humain!!!!");
	    	
	    }  
	}// end run
}
