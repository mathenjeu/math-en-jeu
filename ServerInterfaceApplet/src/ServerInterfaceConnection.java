import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Android
 *
 */
public class ServerInterfaceConnection implements Runnable {

	private ServerInterfaceApplet serverInterface;
	private Socket interfaceSocket;
	private OutputStream objOutput;
	private InputStream objInput;
	private String host;
	private int port;
	// to indicate that we don't want a connection with server
	// we prefer to be disconnected
	private boolean disconnectedFlag;
	private boolean runApplet;
	private static final int _STOP = 1;
	private static final int _STATUS = 2;
	private static final int _ON = 3;
	
	public ServerInterfaceConnection(ServerInterfaceApplet applet, String host, String port)
	{
		this.serverInterface = applet; 
        this.host = host;
        this.port = Integer.parseInt(port);
        this.disconnectedFlag = false;
        this.runApplet = true;          
	}
	
	public void run(){
		while(runApplet){
			if(interfaceSocket == null && !this.disconnectedFlag)
			{
				this.connectToServer();
			}
		}

	}
	
	public void sendCommand(int command) {
	
		if(interfaceSocket != null && interfaceSocket.isConnected() && !interfaceSocket.isClosed()){
			
			try {
				objOutput = interfaceSocket.getOutputStream();
				//objOutput.flush();
								
				// write the message to server
				byte message = (byte)command;
				objOutput.write(message);
				message = (byte)0;
				objOutput.write(message);

				// send the message
				objOutput.flush();
				
				serverInterface.newMessage("We send command :  " + command);
								
				byte [] buffer = new byte[2];
				objInput = interfaceSocket.getInputStream();
				objInput.read(buffer);
				byte commande = (byte)buffer[0];
				serverInterface.newMessage("Server answered :  " + commande);
				if( commande == (byte)_STOP )
				{
					this.serverInterface.setLabelOff();
					serverInterface.newMessage("ici STOP ");
					objOutput.close();
					interfaceSocket.close();
				}

			} catch (IOException e) {

				serverInterface.newMessage(" IO exception  :  " + e.getMessage());
				this.serverInterface.setLabelDisconnected();
			}
		}
		/*else if((byte)command != (byte)_STOP){
			this.connectToServer();
		}*/
	}
	
	
	/**
	 * We try to connect to the server socket
	 * and inform the admin about status of the server
	 * if it's down we try automatically to start it
	 * using CGI script on server
	 * @return 
	 */
	public boolean connectToServer(){
		this.disconnectedFlag = false;		
		try {
			interfaceSocket = new Socket(InetAddress.getByName(this.host), this.port);
			
		} catch (UnknownHostException e) {
			
			//e.printStackTrace();
			serverInterface.newMessage("Error : UnknownHostException " + e.getMessage());
			interfaceSocket = null;
			
		}catch (ConnectException e)
		{			
			//e.printStackTrace();
			serverInterface.newMessage("Error : ConnectException " + e.getMessage());
			interfaceSocket = null;
			//serverInterface.setLabelOff();
			
		} 
		catch (IOException e) {
			
			///e.printStackTrace();
			serverInterface.newMessage("Error : IOException " + e.getMessage());
			interfaceSocket = null;
			
		}
		
		return !(interfaceSocket == null && interfaceSocket.isConnected());
	}
	
	
	public void disconnectFromServer() {
		
		this.disconnectedFlag = true;
		try {
			if(this.interfaceSocket != null && this.interfaceSocket.isConnected()){
				this.interfaceSocket.close();
				serverInterface.setLabelDisconnected();				
			}
			else
				serverInterface.setLabelDisconnected();
		} catch (IOException e) {

			serverInterface.newMessage(e.getMessage());
		}
		
	}
	
	 protected void cleanSocket() {
	        if (interfaceSocket != null) {
	            try {
	                if (objOutput != null) {
	                    objOutput.close();
	                    objOutput = null;
	                }
	            } catch (Exception e) {} //Ignore errors
	            try {
	                if (objInput != null) {
	                    objInput.close();
	                    objInput = null;
	                }
	            } catch (Exception e) {} //Ignore errors
	            try {
	                if (interfaceSocket != null) {
	                    interfaceSocket.close();
	                    interfaceSocket = null;
	                }
	            } catch (Exception e) {} //Ignore errors
	        }
	    }

}
