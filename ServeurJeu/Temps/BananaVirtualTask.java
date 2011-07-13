package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;


/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Banana on the player
 * after the amount of time declared in Banane (Seconds)
 * (reduced move possibility and harder questions)
 * 
 * last change September 2010
 *
 */

public class BananaVirtualTask extends TimerTask {

	// reference to the virtual player to suffer the banana
	private JoueurVirtuel vplayer;
	// boolean to cancel the task if the game is over
	private boolean runIt;
	
	public BananaVirtualTask(JoueurVirtuel player){
		this.vplayer = player;
		this.runIt = true;
	}
	

	// override abstract run methode 
	public void run() {
		if(runIt){
			vplayer.getBananaState().setisUnderBananaEffects(false);
		}
		runIt = false;		
    	//System.out.println("BananaTask virtuel!!!!");
	}// end run
	
	public void cancelTask(){
		
		this.runIt = false;
				
	}

}// end class
