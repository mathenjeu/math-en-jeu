package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Banane;
import ServeurJeu.Temps.BananaVirtualTask;

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
	
	// timertask actually applied to player
	private BananaVirtualTask bTask;
	
	// time to end of the actual banana
	private long taskDate;
	
	// is Banana applied to our player?
	private boolean isUnderBananaEffects;
	
	// is the state of our player
	private JoueurVirtuel vplayer;
	
	private static long BANANATIME = 90000;

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
	 * Method used to set a Banana to player with all the
	 * side effets applayed
	 */
	public void bananaIsTossed()
	{
		 if(vplayer != null){

			//System.out.println("Banana is tossed to virtual !!!! " );
			if(this.isUnderBananaEffects == false){

				this.isUnderBananaEffects = true;
				this.bTask = Banane.utiliserBanane(vplayer, BANANATIME);
				this.taskDate = System.currentTimeMillis() + BANANATIME;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + BANANATIME;
				this.bTask = Banane.utiliserBanane(vplayer, tempDate);
				this.taskDate = tempDate;
				

			}	
		}	
	}// end of method
	
	public void destruction()
	{
		if(this.bTask != null){
		   this.bTask.cancelTask();
		   this.bTask.cancel();
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
			this.bTask.cancel();
		}
	}


}// end of the class

