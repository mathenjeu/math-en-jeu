import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * @author Oloieri Lilian
 *
 */
public class ServerInterfaceApplet extends JApplet  {

	private static final long serialVersionUID = 1L;
	//private static final int _STOP = 1;
	//private static final int _STATUS = 2;
	//private static final int _ON = 3;
	private JLabel stateLabel;
	private JTextArea messageBoard;
	private ServerInterfaceConnection conn;
	private Thread connectionThr;
	private ImageIcon img;
	private boolean isConnected;

	
	
	
	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	public void init() {

		this.rootPane.setSize(800, 200);
       
        String host = this.getCodeBase().getHost();
		//String host = "localhost";
		String port = "8181";//this.getParameter("port");
		if(port == null)
			port = "8181";
	    //now we do the connection to the server
		this.conn = new ServerInterfaceConnection(this, host, port);
		this.connectionThr = new Thread(conn, "ServerInterfaceApplet" );
		this.connectionThr.start();
	    
		// initiate the info label
		stateLabel = new JLabel(" ", SwingConstants.RIGHT);
		messageBoard = new JTextArea("Port to connect:  " + port + "\n", 700, 50);
		messageBoard.append("Server :    " + host + "\n");

		this.setLabelUncheked();
					    
	    //made the panels with components
		//img = new ImageIcon("logo.gif");
		this.construction();
	    
	}

	/* (non-Javadoc)
	 * @see java.applet.Applet#start()
	 */
	public void start() {
		isConnected = this.conn.connectToServer();
		if(isConnected)
		{
			this.setLabelOn();
		}else{
			this.setLabelOff();
			this.standUpServer();
		}
	}

	/* (non-Javadoc)
	 * @see java.applet.Applet#stop()
	 */
	public void stop() {
		this.destroy();
		
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
		  //buttons.setBackground(Color.red);
		  buttons.setPreferredSize(new Dimension(100, 200));
		  GridLayout buttonsGrid = new GridLayout(7,1);
		  buttonsGrid.setVgap(2);
		  buttonsGrid.setHgap(2);
		  buttons.setLayout(buttonsGrid);
		  
		  // add buttons
		  JLabel nullLabelUp = new JLabel();
		  JButton upButton = new JButton("Start");
		  JButton downButton = new JButton("Stop");
		  JButton resetButton = new JButton("Reset");
		  JButton exitButton = new JButton("Exit");
		  JButton disconnectButton = new JButton("Disconnect");
		  JLabel nullLabelDown = new JLabel("");

		  
		  //nullLabelUp.setIcon(img);
		  buttons.add(nullLabelUp);
		  buttons.add(upButton);
		  buttons.add(downButton);
		  buttons.add(resetButton);
		  buttons.add(exitButton);
		  buttons.add(disconnectButton);
		  buttons.add(nullLabelDown);
		  
		  info.setPreferredSize(new Dimension(600, 200));
		  BoxLayout infoLayout = new BoxLayout(info, BoxLayout.Y_AXIS);
		  info.setLayout(infoLayout);
		  //info.setBackground(Color.blue);
		  
		  // add and transform state label
		  //stateLabel.setBounds(40, 40, 500, 60);
  		  		  
		  stateLabel.setSize(600, 30);
		  stateLabel.setMinimumSize(new Dimension(400, 30));
		  stateLabel.setOpaque(true);
		  stateLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		  stateLabel.setLocation(100, 10);
		  info.add(stateLabel);
         	  		  
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
		 		  
		  ServerButtonsListener buttonsListener = new ServerButtonsListener(this);
		  upButton.addActionListener(buttonsListener);
		  downButton.addActionListener(buttonsListener);
		  resetButton.addActionListener(buttonsListener);
		  exitButton.addActionListener(buttonsListener);
		  disconnectButton.addActionListener(buttonsListener);
		  		  
		  this.getContentPane().add(buttons, BorderLayout.WEST);
		  this.getContentPane().add(info, BorderLayout.CENTER);
		  
		  this.validate();
		
	   }

	   	   
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

    /**
     *  Send command to the CGI script on server startServer.pl
     *  to stand up the game server with a bash serveur.sh script
     */          
       public void standUpServer() {

    	   this.setLabel(" Try to up the server...");

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
    		   isConnected = this.conn.connectToServer();
    		   i++;
    	   }
    	   if(isConnected)
    	   {
    		   this.setLabelOn();
    		   newMessage(" Successfully connected to server! ");
    	   }else{
    		   this.setLabelOff();
    		   newMessage("Error! Cannot connect to server! ");
    	   }

       }// end method
       
    public void newMessage(String text)
    {
    	this.messageBoard.append(text + "\n");
    }

	/**
	 * @return the conn
	 */
	public ServerInterfaceConnection getConn() {
		return conn;
	}


}
