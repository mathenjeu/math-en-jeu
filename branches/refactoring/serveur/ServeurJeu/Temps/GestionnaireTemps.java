/*
 * Created on 2006-03-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.Temps;


import java.util.Timer;

/**
 * This class is now a singleton
 * @author Marc
 *
 */
public class GestionnaireTemps extends Timer
{
  
  /**
   * Holder class for the singleton
   * @author Maxime
   *
   */
  private static class GestionnaireTempsHolder {
    private final static GestionnaireTemps INSTANCE = new GestionnaireTemps();
  }

  /**
   * Private constructor of the GestionnaireTemps
   *
   */
  private GestionnaireTemps() {}
  
  /**
   * This method return the only instance of this class.
   * @return instance of GestionnaireTemps
   */
  public static GestionnaireTemps getInstance() {
    return GestionnaireTempsHolder.INSTANCE;
  }
  
  
  /**
   * Add a task
   * @param t the task
   * @param stepSeconds the step in seconds
   */
	public void ajouterTache( Tache t, int stepSeconds )
	{
		this.scheduleAtFixedRate( t, stepSeconds * 1000, stepSeconds * 1000 );
	}
	
  /**
   * Remove a task
   * @param t the task to remove
   */
	public void enleverTache( Tache t )
	{
		if (t != null)
		{
			t.cancel();
		}
		this.purge();
	}
}
