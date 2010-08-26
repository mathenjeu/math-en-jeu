package ServeurJeu.Temps;

import java.util.TimerTask;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

/**
 * 
 * @author Oloieri Lilian
 * 
 * Used to take out the effects of Banana on the player
 * after the amount of time declared in Banane (Seconds)
 * (reduced move possibility and harder questions)
 * 
 * last change March 2010
 *
 */

public class BananaTask extends TimerTask {

	private JoueurHumain player;
	private JoueurVirtuel vplayer;
	
	public BananaTask(JoueurHumain player){
		this.player = player;
	}
	
	public BananaTask(JoueurVirtuel vplayer){
		this.vplayer = vplayer;
	}
	
	// override abstract run methode 
	public void run() {
	    if(player != null){
	    	player.obtenirPartieCourante().getBananaState().setisUnderBananaEffects(false);
	    	player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() + 2);
	    	//System.out.println("BananaTask humain!!!!");
	    }else{
	    	
	    	vplayer.getBananaState().setisUnderBananaEffects(false);
	    	//System.out.println("BananaTask virtuel!!!!");
	    }
	      
	}// end run

}// end class
