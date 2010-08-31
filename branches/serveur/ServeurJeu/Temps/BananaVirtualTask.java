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

	private JoueurVirtuel vplayer;
	
	public BananaVirtualTask(JoueurVirtuel player){
		this.vplayer = player;
	}
	

	// override abstract run methode 
	public void run() {
		vplayer.getBananaState().setisUnderBananaEffects(false);
    	//System.out.println("BananaTask virtuel!!!!");
	}// end run

}// end class
