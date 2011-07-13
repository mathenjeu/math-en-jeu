package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Brainiac on the player
 * after the needed amount of time (Seconds)
 * (more move possibility and softer questions)
 *
 * last change November 2010
 */

public class BrainiacVirtuelTask extends TimerTask {
	
	private JoueurVirtuel vplayer;
	// boolean to cancel the task if the game is over
	private boolean runIt;
	
	public BrainiacVirtuelTask(JoueurVirtuel vplayer){
		this.vplayer = vplayer;
		this.runIt = true;
	}
	
	// override abstract run methode 
	public void run() {
	    if(runIt && vplayer != null){
	    	vplayer.getBrainiacState().setInBrainiac(false);
	    	//System.out.println("BraniacTask virtuel!!!!");
	    }
	    runIt = false;
	}// end run
	
	public void cancelTask(){
		this.runIt = false;		
	}	
} // end class
