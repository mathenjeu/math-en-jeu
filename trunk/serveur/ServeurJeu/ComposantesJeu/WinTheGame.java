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
                    System.out.println("hey hey hey!!");
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
    
    public void arreter() throws InterruptedException
    {//FRANCOIS l'arrêter dans detruire table?
        thread.interrupt();
        thread.join();
    }
}




/*
public class Example
{
    private static class MessageLoop implements Runnable
    {
        public void run()
        {
            String importantInfo = "Mares eat oats";
            try
            {
                for (int i = 0; i < 100; i++)
                {
                    Thread.sleep(1000);
                    System.out.println(importantInfo);
                }
            }
            catch(InterruptedException e)
            {
                 System.out.println("I wasn't done!");
            }
        }
    }

    public static void main(String args[]) throws InterruptedException
    {
        long patience = 1000 * 60 * 60;

        System.out.println("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();
        Thread t = new Thread(new MessageLoop());
        t.start();

        System.out.println("Waiting for MessageLoop thread to finish");
        while (t.isAlive())
        {
            System.out.println("Still waiting...");
            t.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && t.isAlive())
            {
                System.out.println("Tired of waiting!");
                t.interrupt();
                t.join();
            }
        }
        System.out.println("Finally!");
    }
}
*/