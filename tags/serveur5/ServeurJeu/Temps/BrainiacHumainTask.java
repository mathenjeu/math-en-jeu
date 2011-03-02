package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Brainiac on the player
 * after the needed amount of time (Seconds)
 * (more move possibility and softer questions)
 *
 * last change December 2010
 */

public class BrainiacHumainTask extends TimerTask {

	// reference to human to apply brainiac to
	private JoueurHumain player;
	// boolean to cancel the task if the game is over
	private boolean runIt;
		
	public BrainiacHumainTask(JoueurHumain player){
		this.player = player;
		this.runIt = true;
	}
	
	// override abstract run methode 
	public void run() {
	    if(this.runIt && player != null && player.obtenirPartieCourante() != null){
	    	
	    		//player.obtenirPartieCourante().setBraniacsNumberMinus();
	    		player.obtenirPartieCourante().getBrainiacState().setInBrainiac(false);
	    		player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 1);
	    		//System.out.println("BraniacTask humain!!!!");

	    }  
	}// end run
	
	public void cancelTask(){
		this.runIt = false;
		//Thread.currentThread().interrupt();
		
	}
}
