package ServeurJeu.Temps;

import java.util.TimerTask;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
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

	// reference to the human player to suffer the banana
	private JoueurHumain player;
	// boolean to cancel the task if the game is over
	private boolean runIt;
		
	public BananaHumainTask(JoueurHumain player){
		this.player = player;
		this.runIt = true;
	}
	
	// override abstract run methode 
	public void run() {
	    if(player != null && this.runIt && player.obtenirPartieCourante() != null){
	    	player.obtenirPartieCourante().getBananaState().setisUnderBananaEffects(false);
	    	player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() + 2);
	    	//System.out.println("BananaTask humain!!!!");
	    }	      
	}// end run
	
	public void cancelTask(){
		this.cancel();
		this.runIt = false;
		//Thread.currentThread().interrupt();
		
	}

}// end class
