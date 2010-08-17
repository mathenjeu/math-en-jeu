/*
 * Created on 2006-03-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.Temps;


import java.util.Timer;

/**
 * @author Marc
 *
 */
public class GestionnaireTemps extends Timer
{
	public void ajouterTache( Tache t, int stepSeconds )
	{
		this.scheduleAtFixedRate( t, stepSeconds * 1000, stepSeconds * 1000 );
	}
	
	public void enleverTache( Tache t )
	{
		if (t != null)
		{
			t.cancel();
		}
		this.purge();
	}
	
	public void putNewTask( Tache t, int delay)
	{
		this.schedule(t, delay);
	}
}
