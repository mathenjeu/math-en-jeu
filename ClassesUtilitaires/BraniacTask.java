package ClassesUtilitaires;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Braniac on the player
 * after the amount of time declared in Braniac (Seconds)
 * (more move possibility and softer questions)
 *
 */

public class BraniacTask extends TimerTask {

	private JoueurHumain player;
	private JoueurVirtuel vplayer;
	
	public BraniacTask(JoueurHumain player){
		this.player = player;
	}
	
	public BraniacTask(JoueurVirtuel vplayer){
		this.vplayer = vplayer;
	}
	
	// override abstract run methode 
	public void run() {
	    if(player != null){
	    	player.obtenirPartieCourante().setInBraniacState(false);
	    	player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 1);
	    	System.out.println("BraniacTask humain!!!!");
	    }else{
	    	
	    	vplayer.setUnderBraniacEffect(false);
	    	System.out.println("BraniacTask virtuel!!!!");
	    }
	      
	}// end run
}
