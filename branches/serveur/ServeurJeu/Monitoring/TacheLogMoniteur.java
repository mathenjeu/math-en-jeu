package ServeurJeu.Monitoring;

import ServeurJeu.Temps.Tache;

public class TacheLogMoniteur extends Tache 
{

	@Override
	public void run() 
	{
		Moniteur.obtenirInstance().log();
	}

}
