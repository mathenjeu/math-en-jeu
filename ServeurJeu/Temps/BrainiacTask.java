package ServeurJeu.Temps;

import java.util.TimerTask;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Brainiac on the player
 * after the needed amount of time (Seconds)
 * (more move possibility and softer questions)
 *
 * last change August 2011
 */

public class BrainiacTask extends TimerTask {

	// reference to human to apply brainiac to
	private Joueur player;
	// boolean to cancel the task if the game is over
	private boolean runIt;
		
	public BrainiacTask(Joueur player){
		this.player = player;
		this.runIt = true;
	}
	
	// override abstract run methode 
	public void run() {
	    if(runIt && player != null && player.getPlayerGameInfo() != null){
	    		
	    	player.getPlayerGameInfo().setOffBrainiac();	
	    }  
	    runIt = false;
	}// end run
	
	public void cancelTask(){
		this.runIt = false;		
	}
}
