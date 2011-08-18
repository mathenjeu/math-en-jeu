package ServeurJeu.Temps;

import java.util.TimerTask;
import ServeurJeu.ComposantesJeu.Joueurs.PlayerBananaState;

public class BananaStartTask extends TimerTask {

	// reference to the player to suffer the banana
	private PlayerBananaState playerState;
	// boolean to cancel the task if the game is over
	private boolean runIt;
	
	
	
	public BananaStartTask(PlayerBananaState playerState) {
		super();
		this.playerState = playerState;
		this.runIt = true;
	}

	public void run() {
		
		if(runIt)
		{
			playerState.bananaIsTossed();
		}
		runIt = false;
	}
	
	public void cancelTask(){
		runIt = false;				
	}
}