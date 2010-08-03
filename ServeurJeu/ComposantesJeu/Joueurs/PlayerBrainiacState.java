/**
 * 
 */
package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Brainiac;

import ClassesUtilitaires.BrainiacTask;

/**
 * Used to treat the Braniac's applyed to users
 * both Virtual and Humain
 * We can't have two braniac's in the same time, so we 
 * cancel the old one and put another one with the 
 * sheduled time = + of the rest of first and 90 s 
 * for the second  
 * 
 * @author Oloieri Lilian
 * date 10 March 2010
 */
public class PlayerBrainiacState {
	
	// timertask actually applayed to player
	private BrainiacTask bTask;
	
	// time to end of the actual braniac
	private long taskDate;
	
	// is in Braniac our player?
	private boolean isInBrainiac;
	
	// is the state to one of them
	private JoueurHumain player;
	private JoueurVirtuel vplayer;
	
	private static long brainTime = 90000;

	// constructor - in the first time we are not in the Braniac
	public PlayerBrainiacState(JoueurHumain player) {
		super();
		//this.setInBrainiac(false);
		this.player = player;
	}
	
	public PlayerBrainiacState(JoueurVirtuel vplayer) {
		super();
		//this.setInBrainiac(false);
		this.vplayer = vplayer;
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
	
	
	/*
	 * Method used to set a Braniac to player with all the
	 * side effets applayed
	 */
	public void putTheOneBrainiac()
	{
		if(player != null){
			if(this.isInBrainiac == false){
				
				this.isInBrainiac = true;
			    this.bTask = Brainiac.utiliserBrainiac(player, brainTime);
			    this.taskDate = System.currentTimeMillis() + brainTime;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + brainTime;
				this.bTask = Brainiac.utiliserBrainiac(player, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);
				
								
			}	
		}
		else if(vplayer != null){

			if(this.isInBrainiac == false){

				this.isInBrainiac = true;
				this.bTask = Brainiac.utiliserBrainiac(vplayer, brainTime);
				this.taskDate = System.currentTimeMillis() + brainTime;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + brainTime;
				this.bTask = Brainiac.utiliserBrainiac(vplayer, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);

			}	
		}	
	}// end of method
	

}//end of class 
