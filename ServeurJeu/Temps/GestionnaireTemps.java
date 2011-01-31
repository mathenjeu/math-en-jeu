/*
 * Created on 2006-03-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.Temps;


import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Marc
 *
 */
public class GestionnaireTemps extends Timer
{
	public void ajouterTache( TimerTask t, int stepSeconds )
	{
		this.scheduleAtFixedRate( t, stepSeconds * 1000, stepSeconds * 1000 );
	}
	
	public void enleverTache( TimerTask t )
	{
		if (t != null)
		{
			t.cancel();
		}
		this.purge();
	}
	
	public void putNewTask( TimerTask t, long delay)
	{
		this.schedule(t, delay);
	}
	
	/*
	public void putNewTask( BananaHumainTask t, int delay)
	{
		this.schedule(t, delay);
	}
	public void putNewTask( BananaVirtualTask t, int delay)
	{
		this.schedule(t, delay);
	}
	
	public void putNewTask( BrainiacHumainTask t, int delay)
	{
		this.schedule(t, delay);
	}
	public void putNewTask( BrainiacVirtuelTask t, int delay)
	{
		this.schedule(t, delay);
	} */
	
	public void stopIt()
	{
		//TO DO
		this.cancel();
	}
}
