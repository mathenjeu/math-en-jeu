package ServeurJeu.ComposantesJeu;

/**
 * @author François Gingras
 */

public class WinTheGame
{
    protected Table table;
   /* Thread thread = new Thread(new theMainLoop());
    
    private class theMainLoop implements Runnable
    {
        public void run()
        {
        	
        	 *  ********************
        	 *  TODO 
        	 *  REPLACE THIS CODE TO START USING THE WIN THE GAME AGAIN.
        	 *  VERY IMPORTANT !!!
        	 *  ********************
        	
        	
           
            try
            {
                while(true)
                {
                    Thread.sleep(1000*(table.obtenirTempsTotal()*5+30));
                    table.preparerEvenementDeplacementWinTheGame();
                }
            }
            catch(InterruptedException e)
            {
                // Le thread du WinTheGame a été arrêté... ben coudonc,
                // c'est vraiment pas grave, c'est même supposé arriver
            }
            
        } 
    }  */ 

    public WinTheGame(Table t)
    {
        table = t;
    }
    
    public void demarrer()
    {
        //thread.start();
    }
    
    public void arreter()
    {
       // thread.interrupt();
    }
}