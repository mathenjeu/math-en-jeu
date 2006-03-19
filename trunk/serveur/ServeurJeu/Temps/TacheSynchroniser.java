/*
 * Created on 2006-03-17
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
public class TacheSynchroniser extends Tache
{
	private TreeMap lstObservateurs;
	
	public TacheSynchroniser()
	{
		lstObservateurs = new TreeMap();
	}
	
	public void ajouterObservateur( ObservateurSynchroniser obs )
	{
		synchronized( lstObservateurs )
		{
			lstObservateurs.put( new Integer( obs.getObservateurSynchroniserId() ), obs );
		}
	}
	
	public void enleverObservateur( ObservateurSynchroniser obs )
	{
		synchronized( lstObservateurs )
		{
			lstObservateurs.remove( new Integer( obs.getObservateurSynchroniserId() ) );
		}
	}
	
	public void run()
	{
		//notifier les observateurs
		synchronized( lstObservateurs )
		{
			Iterator it = lstObservateurs.values().iterator();
			while( it.hasNext() )
			{
				ObservateurSynchroniser obs = (ObservateurSynchroniser)it.next();
				obs.synchronise();
			}
		}
	}
}
