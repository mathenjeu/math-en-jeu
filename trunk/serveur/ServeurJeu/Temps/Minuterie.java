/*
 * Created on 2006-03-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.Temps;


import java.util.TreeMap;
import java.util.Iterator;

/**
 * @author Marc
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Minuterie extends Tache 
{
	private int intTemps;
	private int intStep;
	private TreeMap lstObservateurs;
	private boolean bolStopped;
	
	public Minuterie( int tempsDepart, int step )
	{
		intTemps = tempsDepart;
		intStep = step;
		bolStopped = false;
		lstObservateurs = new TreeMap();
	}
	
	public void ajouterObservateur( ObservateurMinuterie obs )
	{
		synchronized( lstObservateurs )
		{
			lstObservateurs.put( new Integer( obs.getObservateurMinuterieId() ), obs );
		}
	}
	
	public void run()
	{
		if( bolStopped == false )
		{
			intTemps -= intStep;
			if( intTemps <= 0 )
			{
				bolStopped = true;
	            // notifier les observateurs
				synchronized( lstObservateurs )
				{
					Iterator it = lstObservateurs.values().iterator();
					while( it.hasNext() )
					{
						ObservateurMinuterie obs = (ObservateurMinuterie)it.next();
						obs.tempsEcoule();
					}
				}
			}
		}
	}
	
	public int obtenirTempsActuel()
	{
		return intTemps;
	}
}
