package ServeurJeu.Temps;

import java.util.TimerTask;

import ServeurJeu.ControleurJeu;
import ServeurJeu.Maitre;
import ServeurJeu.Configuration.GestionnaireConfiguration;

/**
 * @author Oloieri Lilian
 * 
 * Used to stop server after the amount of time specified  
 * in mathenjeu.xml
 *
 */

public class StopServerTask extends TimerTask {

	private Maitre maitre;
	private ControleurJeu objJeu;
	
	public StopServerTask(Maitre maitre, ControleurJeu objJeu) {
		this.maitre = maitre;
		this.objJeu = objJeu;
	}

	public void run() {
		System.out.println("Server will stop ... ");
		this.objJeu.arreter();
		this.maitre.exitServer();
	}

}
