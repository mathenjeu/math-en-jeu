import java.net.Socket;

/**
 * Class charged to implement the communication protocol
 * with applet that lead the application
 * 
 * @author BlackAndroid
 */
public class SmacCommunicator {
	
	private String sentMessage;
	private Socket socket;
	private String port;
	
	public SmacCommunicator(String port){
		this.port = port;
	}
	
	public void sendMessage(String msg, String type){
		
	}

}
