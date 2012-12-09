import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Used to manage the mathamaze game server
 * @author Oloieri Lilian
  */
public class ServerInterfaceApplet extends JApplet implements AppletCommander  {

	private static final long serialVersionUID = 1L;
	//private JLabel stateLabel;
	private JTextArea messageBoard;
	private ServerInterfaceConnection conn;
	private Thread connectionThr;
	//private ImageIcon img;
	private boolean isConnected;
   



	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	public void init() {

		try 
		{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {}

		rootPane.setSize(800, 800);
        rootPane.setPreferredSize(new Dimension(800, 800));
		String host = this.getCodeBase().getHost();
		//String host = "www.mathamaze.ca";
		String port = "8181";//this.getParameter("port");
		
		//now we do the connection to the server
		setConn(new ServerInterfaceConnection(this, host, port));
		connectionThr = new Thread(getConn(), "ServerInterfaceApplet" );
		connectionThr.start();

		// initiate the info label
		//stateLabel = new JLabel(" ", SwingConstants.RIGHT);
		messageBoard = new JTextArea("Port to connect:  " + port + "\n", 700, 50);
		messageBoard.append("Server :    " + host + "\n");

		//made the panels with components
		//img = new ImageIcon("logo.gif");
		construction();
	}// end method

	/* (non-Javadoc)
	 *
	 */
	public void start() {
		getConn().connectToServer();
		isConnected = getConn().connectionCheck();
		if(isConnected)
		{
			newMessage(" Server is on...");
			
		}else{
			newMessage(" Server is off... Trying to get it up...");
			standUpServer();
		}
	}

	/* (non-Javadoc)
	 * 
	 */
	public void stop() {
		destroy();

	}

	private void construction()
	{
		Container pane = this.getContentPane();
		BorderLayout principalLayout = new BorderLayout();
		principalLayout.setHgap(4);
		pane.setLayout(principalLayout);

		JPanel buttons = new JPanel();
		JPanel info = new JPanel();

		// fill buttons panel
		buttons.setPreferredSize(new Dimension(100, 200));
		GridLayout buttonsGrid = new GridLayout(7,1);
		buttonsGrid.setVgap(2);
		buttonsGrid.setHgap(2);
		buttons.setLayout(buttonsGrid);

		// add buttons
		JLabel nullLabelUp = new JLabel();
		JButton upButton = new JButton("Start");
		JButton downButton = new JButton("Stop");
		JButton connectButton = new JButton("Connect");		
		JButton disconnectButton = new JButton("Disconnect");
		JButton exitButton = new JButton("Exit");
		JLabel nullLabelDown = new JLabel("");

		//nullLabelUp.setIcon(img);
		buttons.add(nullLabelUp);
		buttons.add(upButton);
		buttons.add(downButton);
		buttons.add(connectButton);		
		buttons.add(disconnectButton);
		buttons.add(exitButton);
		buttons.add(nullLabelDown);

		info.setPreferredSize(new Dimension(600, 200));
		BoxLayout infoLayout = new BoxLayout(info, BoxLayout.Y_AXIS);
		info.setLayout(infoLayout);
		//info.setBackground(Color.blue);

		/*
		// add and transform state label
		//stateLabel.setBounds(40, 40, 500, 60);

		stateLabel.setSize(600, 30);
		stateLabel.setMinimumSize(new Dimension(400, 30));
		stateLabel.setOpaque(true);
		stateLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		stateLabel.setLocation(100, 10);
		info.add(stateLabel);*/

		//messageBoard.setSize(700, 50);
		//messageBoard.setLocation(120, 200);
		messageBoard.setLineWrap(true);
		//messageBoard.setRows(10);
		messageBoard.setMargin(new Insets(15,10,5,5));
		//messageBoard.setEditable(false);
		messageBoard.setAutoscrolls(true);
		//messageBoard.
		JScrollPane scrollPane = new JScrollPane(messageBoard);
		info.add(scrollPane);
		scrollPane.setLocation(50, 10);

		//messageBoard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		info.validate();

		ServerButtonsListener buttonsListener = new ServerButtonsListener(this, conn);
		upButton.addActionListener(buttonsListener);
		downButton.addActionListener(buttonsListener);
		connectButton.addActionListener(buttonsListener);		
		disconnectButton.addActionListener(buttonsListener);
		exitButton.addActionListener(buttonsListener);

		getContentPane().add(buttons, BorderLayout.WEST);
		getContentPane().add(info, BorderLayout.CENTER);
		validate();

	}

    /*
	public void setLabelOn(){
		stateLabel.setText("  Server is On  ");
		stateLabel.setBackground(Color.GREEN);
	}

	public void setLabelOff(){

		stateLabel.setBackground(Color.RED);
		stateLabel.setText("  Server is Off  ");
	}

	public void setLabelUncheked(){

		stateLabel.setBackground(Color.ORANGE);
		stateLabel.setText(" Unknown Server state. Server is not responding. ");
	}

	public void setLabelDisconnected(){

		stateLabel.setBackground(Color.LIGHT_GRAY);
		stateLabel.setText(" Disconnected from Server ");
	}

	public void setLabel(String label){

		stateLabel.setBackground(Color.ORANGE);
		stateLabel.setText(label);
	}
	*/

	/* (non-Javadoc)
	 * @see 
	 */          
	public void standUpServer() {

		newMessage(" Try to up the server...");

		try {
			URL serverURL = new URL("http://mathamaze.ca/cgi-bin/startServer.pl");

			/* HttpURLConnection connection = (HttpURLConnection)serverURL.openConnection();
    		   connection.setRequestMethod("GET");
    		   connection.setDoInput(true);
    		   connection.connect();*/


			//serverURL.openConnection();
			//InputStream moveStream = serverURL.openStream();

			// debug...
			//System.out.println("URL = " + serverURL);

			// "now see..."
			getAppletContext().showDocument(serverURL, "_blank");    

		}catch (MalformedURLException erl)
		{
			newMessage("Error!\n" + erl);
			showStatus("Error, look in Java Console for details!");		    	
		} catch (Exception err) {
			newMessage("Error!\n" + err);
			showStatus("Error, look in Java Console for details!");
		} 

		short i = 0;
		while(!isConnected && i < 3){
			getConn().connectToServer();
			isConnected = getConn().connectionCheck();
			i++;
		}
		if(isConnected)
		{
			newMessage(" Successfully connected to server! ");
		}else{
			newMessage("Error! Cannot connect to server! ");
		}

	}// end method

	/* (non-Javadoc)
	 * 
	 */
	public void newMessage(String text)
	{
		messageBoard.append(text + "\n");
	}

	public void sendCommand(String command) {
		getConn().sendCommand(command);		
	}

	private ServerInterfaceConnection getConn() {
		return conn;
	}

	private void setConn(ServerInterfaceConnection conn) {
		this.conn = conn;
	}

	public void exitApplication() {
		//JApplet appl = this;
		//JSObject win = (JSObject) JSObject.getWindow(appl);
		//win.eval("window.close()"); 
		//eval("self.close();");
		//JOptionPane.showMessageDialog(null, "ByeBye!");
		//System.exit( 0 );				
	}	

} // end class
