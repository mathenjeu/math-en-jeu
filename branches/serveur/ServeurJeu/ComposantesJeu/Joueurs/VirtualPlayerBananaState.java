package ServeurJeu.ComposantesJeu.Joueurs;

import java.util.Timer;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Banane;
import ServeurJeu.Temps.BananaVirtualTask;
import ServeurJeu.Temps.VirtualBananaStartTask;

/**
 * Used to treat the Banana's applied to users
 * both Virtual and Human
 * We can't have two banana's task in the same time, so we 
 * cancel the old one and put another one with the 
 * Scheduled time = + of the rest of first and 90 s 
 * for the second  
 * 
 * @author Oloieri Lilian
 * date 10 March 2010
 */

public class VirtualPlayerBananaState {
	// timer task to start effets of Banana
	// we need to wait 5 sec to apply the effets
	// time to banana arrive to the player feets
	private VirtualBananaStartTask startTask;
	
	// timertask actually applied to player
	private BananaVirtualTask bTask;
	
	// time to end of the actual banana
	private long taskDate;
	
	// is Banana applied to our player?
	private boolean isUnderBananaEffects;
	
	// is the state of our player
	private JoueurVirtuel vplayer;
	
	private static final long BANANA_TIME = 90000;
	private static final int START_TIME = 5000;

	// constructor - in the first time we are not in the Banana
	public VirtualPlayerBananaState(JoueurVirtuel player) {
		//super();
		//this.setisUnderBananaEffects(false);
		this.vplayer = player;
	}
	
	// setters and getters 
	public void setBTask(BananaVirtualTask bTask) {
		this.bTask = bTask;
	}

	public BananaVirtualTask getBTask() {
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
		this.startTask = new VirtualBananaStartTask(this);
		
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
		 if(vplayer != null){

			//System.out.println("Banana is tossed to virtual !!!! " );
			if(this.isUnderBananaEffects == false){

				this.isUnderBananaEffects = true;
				this.bTask = Banane.utiliserBanane(vplayer, BANANA_TIME);
				this.taskDate = System.currentTimeMillis() + BANANA_TIME;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + BANANA_TIME;
				this.bTask = Banane.utiliserBanane(vplayer, tempDate);
				this.taskDate = tempDate;
				

			}	
		}	
	}// end of method
	
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
		this.vplayer = null;
	}
	
	/*
	 *  Used to set off the effects off Banana...
	 *  Now for the case if Brainiac is used on player
	 */
	public void setOffBanana()
	{
		if(this.isUnderBananaEffects){
			this.isUnderBananaEffects = false;
			this.bTask.cancelTask();
		}
	}
	
	public int getTaskTime()
	{
		if(this.isUnderBananaEffects)
		  return (int) ((this.taskDate - System.currentTimeMillis())/1000);
		else
		  return 0;
	}


}// end of the class

