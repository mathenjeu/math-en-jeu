/**
 * 
 */
package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Braniac;

import ClassesUtilitaires.BraniacTask;

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
public class PlayerBraniacState {
	
	// timertask actually applayed to player
	private BraniacTask bTask;
	
	// time to end of the actual braniac
	private long taskDate;
	
	// is in Braniac our player?
	private boolean isInBraniac;
	
	// is the state to one of them
	private JoueurHumain player;
	private JoueurVirtuel vplayer;
	
	private static long branTime = 90000;

	// constructor - in the first time we are not in the Braniac
	public PlayerBraniacState(JoueurHumain player) {
		super();
		this.setInBraniac(false);
		this.player = player;
	}
	
	public PlayerBraniacState(JoueurVirtuel vplayer) {
		super();
		this.setInBraniac(false);
		this.vplayer = vplayer;
	}


	// setters and getters 
	public void setBTask(BraniacTask bTask) {
		this.bTask = bTask;
	}

	public BraniacTask getBTask() {
		return bTask;
	}

	public void setTaskDate(long taskDate) {
		this.taskDate = taskDate;
	}

	public long getTaskDate() {
		return taskDate;
	}

	public void setInBraniac(boolean isInBraniac) {
		this.isInBraniac = isInBraniac;
	}

	public boolean isInBraniac() {
		return isInBraniac;
	}
	
	
	/*
	 * Method used to set a Braniac to player with all the
	 * side effets applayed
	 */
	public void putTheOneBraniac()
	{
		if(player != null){
			if(this.isInBraniac == false){
				
				this.isInBraniac = true;
			    this.bTask = Braniac.utiliserBraniac(player, branTime);
			    this.taskDate = System.currentTimeMillis() + branTime;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + branTime;
				this.bTask = Braniac.utiliserBraniac(player, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);
				
								
			}	
		}
		else if(vplayer != null){

			if(this.isInBraniac == false){

				this.isInBraniac = true;
				this.bTask = Braniac.utiliserBraniac(vplayer, branTime);
				this.taskDate = System.currentTimeMillis() + branTime;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + branTime;
				this.bTask = Braniac.utiliserBraniac(vplayer, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);

			}	
		}	
	}// end of method
	

}//end of class 
