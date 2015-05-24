package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.Temps.BrainiacTask;

/**
 * Used to treat the Braniac's applyed to users
 * both Virtual and Humain
 * We can't have two braniac's in the same time, so we 
 * cancel the old one and put another one with the 
 * sheduled time = + of the rest of first and 90 s 
 * for the second  
 */
public class PlayerBrainiacState {
	
	// timertask actually applayed to player
	private BrainiacTask bTask;
	
	// time to end of the actual braniac
	private long taskDate;
	
	// is in Braniac our player?
	private boolean isInBrainiac;
	
	// is the state to one of them
	private Joueur player;
	
	private static long BRAIN_TIME = 60000;

	// constructor - in the first time we are not in the Braniac
	public PlayerBrainiacState(Joueur player) {
		super();
		//this.setInBrainiac(false);
		this.player = player;
	}
	
	
	// setters and getters 
	public void setBTask(BrainiacTask bTask) {
		this.bTask = bTask;
	}

	public BrainiacTask getBTask() {
		return bTask;
	}

	public void setTaskDate(long taskDate) {
		this.taskDate = taskDate;
	}

	public long getTaskDate() {
		return taskDate;
	}

	public void setInBrainiac(boolean isInBrainiac) {
		this.isInBrainiac = isInBrainiac;
	}

	public boolean isInBrainiac() {
		return isInBrainiac;
	}
	
	public int getTaskTime()
	{
		int timeTo = (int) ((this.taskDate - System.currentTimeMillis())/1000);
		if(this.isInBrainiac)
		  return timeTo;
		else
		  return 0;
	}
	
	
	/*
	 * Method used to set a Braniac to player with all the
	 * side effets applayed
	 */
	public void putTheOneBrainiac()
	{
		if(player != null && player.getPlayerGameInfo().obtenirTable().obtenirGestionnaireTemps() != null){
			if(isInBrainiac == false){
				
				isInBrainiac = true;
			    bTask = new BrainiacTask(player);
			    
				// player under Braniac
				// Create TimerTask
				bTask = new BrainiacTask(player);
				
				player.getPlayerGameInfo().setOnBrainiac();
				
				// used timer to take out effects of brainiac after the needed time
				player.getPlayerGameInfo().obtenirTable().obtenirGestionnaireTemps().schedule(bTask, BRAIN_TIME);
			    	
			    taskDate = System.currentTimeMillis() + BRAIN_TIME;
			}else
			{
				this.bTask.cancelTask();
				long tempDate = this.taskDate  + BRAIN_TIME;
				this.bTask = new BrainiacTask(player);
				player.getPlayerGameInfo().obtenirTable().obtenirGestionnaireTemps().schedule(bTask, tempDate);
				this.taskDate = tempDate;				
			}	
		}
		
	}// end of method
	
	public void destruction()
	{
		if(this.bTask != null){
		  this.bTask.cancelTask();
		}
		this.player = null;
	}
	
	/*
	 *  Used to set off the effects off Brainiac...
	 *  Now for the case if Banana is used on player
	 */
	public void setOffBrainiac() {
		if(this.isInBrainiac){
			this.isInBrainiac = false;
			this.bTask.cancelTask();			
		}
		
	}
	

}//end of class 
