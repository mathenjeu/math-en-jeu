package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ComposantesJeu.Joueurs.VirtualPlayerBananaState;

public class VirtualBananaStartTask extends TimerTask {

	// reference to the virtual player to suffer the banana
	private VirtualPlayerBananaState playerState;
	// boolean to cancel the task if the game is over
	private boolean runIt;
	
	public VirtualBananaStartTask( VirtualPlayerBananaState playerState) {
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
