import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import netscape.javascript.*;

/**
 * Class for server GUI buttons
 * @author Oloieri Lilian
 */
public class ServerButtonsListener implements ActionListener {

	private ServerInterfaceConnection conn;
	private ServerInterfaceApplet frame;
	private static final int _STOP = 1;
	
	public ServerButtonsListener(ServerInterfaceApplet serverInterfaceApplet)
	{		
		this.frame = serverInterfaceApplet;
		this.conn = this.frame.getConn();
	}
	
	public void actionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();
		if(actionCommand.equals("Start")){
			this.frame.standUpServer();
			
		}
		else if(actionCommand.equals("Stop")){
			frame.newMessage("Send stop to server " + _STOP);
			conn.sendCommand(_STOP);//frame.setLabelOff();
		}
		else if(actionCommand.equals("Reset")){
			
			conn.sendCommand(_STOP);
			//frame.setLabelOff();
			// Continue - DO the start by script
			//this.frame.standUpServer();

		}else if(actionCommand.equals("Exit")){
			Applet appl = this.frame;
			//JSObject win = (JSObject) JSObject.getWindow(appl);
			//win.eval("window.close()"); 
			//eval("self.close();");
			//JOptionPane.showMessageDialog(null, "ByeBye!");
			//System.exit( 0 );

			
		}
		else if(actionCommand.equals("Disconnect")){
			conn.disconnectFromServer();
			frame.setLabelDisconnected();
		
		}


	}
	/*
	// Internal class used to avoid blocking Up and Reset buttons
	// by working server thread
	class ServerBufferWorkingThread extends Thread
	{
		private ServerBufferWorkingThread (){}
		
		public void run(){
		   //maitre.demarrer();	
		}
	} */

}
