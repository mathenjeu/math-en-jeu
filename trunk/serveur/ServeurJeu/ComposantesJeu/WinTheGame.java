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
                    Thread.sleep(1000*intervalle);
                    table.preparerEvenementDeplacementWinTheGame();
                }
            }
            catch(InterruptedException e)
            {
                // Le thread du WinTheGame a été arrêté... ben coudonc,
                // c'est vraiment pas grave, c'est même supposé arriver
            }
        }
    }
    
    //FRANCOIS s'arranger pour que le WinTheGame soit le plus loin possible de
    // tous les joueurs lorsqu'il est créé (ou du moins, à peu près à la même distance de tous les joueurs)

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