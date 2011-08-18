package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.Joueur;

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

public class BananaTask extends TimerTask {

	// reference to the human player to suffer the banana
	private Joueur player;
	// boolean to cancel the task if the game is over
	private boolean runIt;
		
	public BananaTask(Joueur player){
		this.player = player;
		this.runIt = true;
	}
	
	// override abstract run methode 
	public void run() {
	    if(player != null && runIt && player.getPlayerGameInfo() != null){
	    	player.getPlayerGameInfo().setOffBanana();	    	
	    }
	    runIt = false;
	}// end run
	
	public void cancelTask(){
		this.runIt = false;
		player = null;
	}

}// end class
