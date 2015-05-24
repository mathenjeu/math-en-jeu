package ServeurJeu.ComposantesJeu.Joueurs;

import java.util.Timer;
import ServeurJeu.Temps.BananaStartTask;
import ServeurJeu.Temps.BananaTask;

/**
 * Used to treat the Banana's applied to users
 * both Virtual and Human
 * We can't have two banana's task in the same time, so we 
 * cancel the old one and put another one with the 
 * Scheduled time = + of the rest of first and 90 s 
 * for the second  
 */

public class PlayerBananaState {
	
	// timer task to start effets of Banana
	// we need to wait 5 sec to apply the effets
	// time to banana arrive to the player feets
	private BananaStartTask startTask;
	
	// timer task actually applied to player
	private BananaTask bTask;
	
	// time to end of the actual banana
	private long taskDate;
	
	// is Banana applied to our player?
	private boolean isUnderBananaEffects;
	
	// our player
	private Joueur player;
			
	private static final long BANANA_TIME = 90000;
	private static final int START_TIME = 5000;

	// constructor - in the first time we are not in the Banana
	public PlayerBananaState(Joueur player) {
		//super();
		this.player = player;
	}
	
	// setters and getters 
	public void setBTask(BananaTask bTask) {
		this.bTask = bTask;
	}

	public BananaTask getBTask() {
		return bTask;
	}

	public void setTaskDate(long taskDate) {
		this.taskDate = taskDate;
	}

	public long getTaskDate() {
		return taskDate;
	}

	/**
	 * @return the isUnderBananaEffects
	 */
	public boolean isUnderBananaEffects() {
		return isUnderBananaEffects;
	}

	/**
	 * @param isBananaOn the isBananaOn to set
	 */
	public void setisUnderBananaEffects(boolean isBananaOn) {
		this.isUnderBananaEffects = isBananaOn;
	}
	
	/*
	 * method used to start the effects of banana 
	 * after the delay specified in a constant START_TIME
	 */
	public void startBanana()
	{
		player.getPlayerGameInfo().setOffBrainiac();
		this.startTask = new BananaStartTask(this);
		
		Timer xTimer = new Timer();
		// used timer to put the effects of banana after the needed time
		xTimer.schedule(this.startTask, START_TIME);
	}
	
	/*
	 * Method used to set a Banana to player with all the
	 * side effets applayed
	 */
	public void bananaIsTossed()
	{
		if(player != null && player.getPlayerGameInfo().obtenirTable().obtenirGestionnaireTemps() != null){
			if(isUnderBananaEffects == false){				
		    	// effects of Banana
				isUnderBananaEffects = true;
				bTask = new BananaTask(player);
				player.getPlayerGameInfo().setOnBanana();
				// used timer to take out effects of banana after the needed time
				player.getPlayerGameInfo().obtenirTable().obtenirGestionnaireTemps().schedule(bTask, BANANA_TIME);
				taskDate = System.currentTimeMillis() + BANANA_TIME;
			    
			}else
			{
				bTask.cancelTask();
				long tempDate = taskDate  + BANANA_TIME;
				bTask = new BananaTask(player);
				player.getPlayerGameInfo().setOnBanana();
				player.getPlayerGameInfo().obtenirTable().obtenirGestionnaireTemps().schedule(bTask, tempDate);
				taskDate = tempDate;										
			}	
		}
		
	}// end of method
	
	/*
	 *  Used to set off the effects off Banana...
	 *  Now for the case if Brainiac is used on player
	 */
	public void setOffBanana()
	{
		if(isUnderBananaEffects){
			isUnderBananaEffects = false;
			bTask.cancelTask();			
		}
	}
	
	public void destruction()
	{
		if(this.bTask != null){
		   this.bTask.cancelTask();
		   this.bTask = null;
		}
		if(this.startTask != null)
		{
			this.startTask.cancelTask();
			this.startTask = null;
		}
		this.player = null;
	}
	
	public int getTaskTime()
	{
		if(this.isUnderBananaEffects)
		  return (int) ((this.taskDate - System.currentTimeMillis())/1000);
		else
		  return 0;
	}

	


}// end of the class
