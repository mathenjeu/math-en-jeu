package ServeurJeu.ComposantesJeu.Joueurs;

import ClassesUtilitaires.BananaTask;
import ClassesUtilitaires.BraniacTask;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Banane;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Braniac;

/**
 * Used to treat the Banana's applyed to users
 * both Virtual and Humain
 * We can't have two banana's task in the same time, so we 
 * cancel the old one and put another one with the 
 * sheduled time = + of the rest of first and 90 s 
 * for the second  
 * 
 * @author Oloieri Lilian
 * date 10 March 2010
 */

public class PlayerBananaState {
	
	// timertask actually applayed to player
	private BananaTask bTask;
	
	// time to end of the actual banana
	private long taskDate;
	
	// is Banana applayed to our player?
	private boolean isUnderBananaEffects;
	
	// is the state to one of them
	private JoueurHumain player;
	private JoueurVirtuel vplayer;
	
	private static long bananaTime = 90000;

	// constructor - in the first time we are not in the Banana
	public PlayerBananaState(JoueurHumain player) {
		super();
		this.setisUnderBananaEffects(false);
		this.player = player;
	}
	
	public PlayerBananaState(JoueurVirtuel vplayer) {
		super();
		this.setisUnderBananaEffects(false);
		this.vplayer = vplayer;
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
	 * Method used to set a Banana to player with all the
	 * side effets applayed
	 */
	public void bananaIsTossed()
	{
		if(player != null){
			if(this.isUnderBananaEffects == false){
				
				this.isUnderBananaEffects = true;
			    this.bTask = Banane.utiliserBanane(player, bananaTime);
			    this.taskDate = System.currentTimeMillis() + bananaTime;
			    
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate - System.currentTimeMillis() + bananaTime;
				this.bTask = Banane.utiliserBanane(player, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);
				
								
			}	
		}
		else if(vplayer != null){

			if(this.isUnderBananaEffects == false){

				this.isUnderBananaEffects = true;
				this.bTask = Banane.utiliserBanane(vplayer, bananaTime);
				this.taskDate = System.currentTimeMillis() + bananaTime;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate - System.currentTimeMillis() + bananaTime;
				this.bTask = Banane.utiliserBanane(vplayer, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);

			}	
		}	
	}// end of method
	


}// end of the class
