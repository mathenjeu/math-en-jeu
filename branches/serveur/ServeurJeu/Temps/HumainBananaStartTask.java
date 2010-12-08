package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.HumainPlayerBananaState;

public class HumainBananaStartTask extends TimerTask {

	// reference to the human player to suffer the banana
	private HumainPlayerBananaState playerState;
	// boolean to cancel the task if the game is over
	private boolean runIt;
	
	
	
	public HumainBananaStartTask(HumainPlayerBananaState playerState) {
		super();
		this.playerState = playerState;
		this.runIt = true;
	}

	public void run() {
		
		if(runIt)
		{
			this.playerState.bananaIsTossed();
		}

	}
	
	public void cancelTask(){
		this.cancel();
		this.runIt = false;
		Thread.currentThread().interrupt();
		
	}

}
