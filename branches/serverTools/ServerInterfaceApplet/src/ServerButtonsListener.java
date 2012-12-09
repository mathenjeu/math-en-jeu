import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import netscape.javascript.*;

/**
 * Class for server GUI buttons
 * @author Oloieri Lilian
 */
public class ServerButtonsListener implements ActionListener {

	private AppletCommander frame;
	private ServerConnector connection;
		
	public ServerButtonsListener(AppletCommander serverInterfaceApplet, ServerConnector conn)
	{		
		this.frame = serverInterfaceApplet;
		this.connection = conn;
	}
	
	public void actionPerformed(ActionEvent evt) {
		
		String actionCommand = evt.getActionCommand();
		
		if(actionCommand.equals("Start")){
			frame.standUpServer();			
		}
		else if(actionCommand.equals("Stop")){
			connection.sendCommand("Stop");
		}		
		else if(actionCommand.equals("Exit")){
			frame.exitApplication();			
		}
		else if(actionCommand.equals("Disconnect")){
			connection.disconnectFromServer();		
		}
		else if(actionCommand.equals("Connect")){
			connection.connectToServer();
		}

	}	

}// end class
