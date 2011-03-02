package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Brainiac;
import ServeurJeu.Temps.BrainiacHumainTask;

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
public class HumainPlayerBrainiacState {
	
	// timertask actually applayed to player
	private BrainiacHumainTask bTask;
	
	// time to end of the actual braniac
	private long taskDate;
	
	// is in Braniac our player?
	private boolean isInBrainiac;
	
	// is the state to one of them
	private JoueurHumain player;
		
	private static long BRAIN_TIME = 60000;

	// constructor - in the first time we are not in the Braniac
	public HumainPlayerBrainiacState(JoueurHumain player) {
		super();
		//this.setInBrainiac(false);
		this.player = player;
	}
	
	
	// setters and getters 
	public void setBTask(BrainiacHumainTask bTask) {
		this.bTask = bTask;
	}

	public BrainiacHumainTask getBTask() {
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
			    this.bTask = Brainiac.utiliserBrainiac(player, BRAIN_TIME);
			    this.taskDate = System.currentTimeMillis() + BRAIN_TIME;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + BRAIN_TIME;
				this.bTask = Brainiac.utiliserBrainiac(player, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);
				
								
			}	
		}
		
	}// end of method
	
	public void destruction()
	{
		if(this.bTask != null){
		   this.bTask.cancel();
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
			this.bTask.cancel();
			player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 1);
		}
		
	}
	

}//end of class 
