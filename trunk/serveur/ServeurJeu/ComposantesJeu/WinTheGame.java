package ServeurJeu.ComposantesJeu;

import ServeurJeu.Configuration.GestionnaireConfiguration;
/**
 * @author François Gingras
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
                    System.out.println("hey hey hey");
                    Thread.sleep(1000*intervalle);
                    table.preparerEvenementDeplacementWinTheGame();
                }
            }
            catch(InterruptedException e)
            {
                // Le thread du WinTheGame a été arrêté... ben coudonc
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
        //thread.join();
    }
}