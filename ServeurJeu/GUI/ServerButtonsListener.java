package ServeurJeu.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class for server GUI buttons
 * @author Oloieri Lilian
 *
 */
public class ServerButtonsListener implements ActionListener {

	private final ServerFrame frame;
	public ServerButtonsListener(ServerFrame serverFrame)
	{
		this.frame = serverFrame;
	}
	
	public void actionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();
		frame.sendMessageToServer(actionCommand);

	}
}
