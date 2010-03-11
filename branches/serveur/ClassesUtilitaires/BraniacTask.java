package ClassesUtilitaires;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Braniac on the player
 * after the needed amount of time (Seconds)
 * (more move possibility and softer questions)
 *
 * last change 10 March 2010
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
	    	
	    		//player.obtenirPartieCourante().setBraniacsNumberMinus();
	    		player.obtenirPartieCourante().getBraniacState().setInBraniac(false);
	    		player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 1);
	    		//System.out.println("BraniacTask humain!!!!");
	    	
	    }else{
	    	
	    	vplayer.getBraniacState().setInBraniac(false);
	    	//System.out.println("BraniacTask virtuel!!!!");
	    }
	      
	  
	}// end run
}
