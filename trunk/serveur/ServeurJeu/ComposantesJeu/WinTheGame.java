package ServeurJeu.ComposantesJeu;

import ServeurJeu.Configuration.GestionnaireConfiguration;
/**
 * @author Fran�ois Gingras
 */

public class WinTheGame
{
    protected Table table;
    Thread thread = new Thread(new theMainLoop());
    
    private class theMainLoop implements Runnable
    {
        public void run()
        {
            int intervalle = GestionnaireConfiguration.obtenirInstance().obtenirNombreEntier("controleurjeu.salles-initiales.regles.intervalle-deplacement-winthegame");
            try
            {
                while(true)
                {
                    Thread.sleep(1000*intervalle);
                    table.preparerEvenementDeplacementWinTheGame();
                }
            }
            catch(InterruptedException e)
            {
                // Le thread du WinTheGame a �t� arr�t�... ben coudonc,
                // c'est vraiment pas grave, c'est m�me suppos� arriver
            }
        }
    }

    public WinTheGame(Table t)
    {
        table = t;
    }
    
    public void demarrer()
    {
        thread.start();
    }
    
    public void arreter()
    {
        thread.interrupt();
    }
}