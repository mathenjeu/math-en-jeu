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
 * last change 10 September 2010
 */

public class BrainiacVirtuelTask extends TimerTask {
	
	private JoueurVirtuel vplayer;
	
	public BrainiacVirtuelTask(JoueurVirtuel vplayer){
		this.vplayer = vplayer;
	}
	
	// override abstract run methode 
	public void run() {
	    if(vplayer != null){
	    	vplayer.getBrainiacState().setInBrainiac(false);
	    	//System.out.println("BraniacTask virtuel!!!!");
	    }
	      
	  
	}// end run
}
