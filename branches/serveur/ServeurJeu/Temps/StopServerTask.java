package ServeurJeu.Temps;

import java.util.TimerTask;
import ServeurJeu.Maitre;

/**
 * @author Oloieri Lilian
 * 
 * Used to stop server after the amount of time specified  
 * in mathenjeu.xml
 *
 */

public class StopServerTask extends TimerTask {

	private Maitre maitre;
		
	public StopServerTask(Maitre maitre) {
		this.maitre = maitre;		
	}

	public void run() {
		System.out.println("Timer Task ... Server will stop ... ");
		this.maitre.exitServerInWindow();
	}

}
