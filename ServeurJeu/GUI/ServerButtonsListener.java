package ServeurJeu.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ServeurJeu.Maitre;

/**
 * Class for server GUI buttons
 * @author Oloieri Lilian
 *
 */
public class ServerButtonsListener implements ActionListener {

	private Maitre maitre;
	private ServerFrame frame;
	public ServerButtonsListener(Maitre maitre, ServerFrame serverFrame)
	{
		this.maitre = maitre;
		this.frame = serverFrame;
	}
	
	public void actionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();
		if(actionCommand.equals("Up")){
		   ServerBufferWorkingThread worker = new ServerBufferWorkingThread();
		   worker.start();
		   frame.setLabelOn();
		}
		else if(actionCommand.equals("Down")){
			maitre.stopServer();
			frame.setLabelOff();
		}
		else if(actionCommand.equals("Reset")){
			maitre.stopServer();
			ServerBufferWorkingThread worker = new ServerBufferWorkingThread();
			worker.start();
			frame.setLabelOn();
			
		}else if(actionCommand.equals("Exit")){
			maitre.exitServer();
						
		}

	}
	
	// Internal class used to avoid blocking Up and Reset buttons
	// by working server thread
	class ServerBufferWorkingThread extends Thread
	{
		private ServerBufferWorkingThread (){}
		
		public void run(){
		   maitre.demarrer();	
		}
	}

}
