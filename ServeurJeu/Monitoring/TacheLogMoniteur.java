package ServeurJeu.Monitoring;

import java.util.TimerTask;


public class TacheLogMoniteur extends TimerTask 
{

	
	public void run() 
	{
		Moniteur.obtenirInstance().log();
	}

}
