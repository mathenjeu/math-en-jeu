package ServeurJeu;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import ServeurJeu.GUI.ServerFrame;

public class Maitre implements Runnable 
{
	private static Logger objLogger = Logger.getLogger( Maitre.class );
	private ControleurJeu objJeu = null;
	private ServerFrame serverWindow;
	private static final int _STOP = 1;
	private static final int _STATUS = 2;
	private static final int _ON = 3;
	//private static final int _RESTART = 4;
	
	// Boolean to indicate if server is on or off
    private boolean isOn;
    
    // String for a command that server must do
    //private String commandToDo;
	
	public static void main(String[] args) 
	{
		String commande = null;
		if( args.length > 0 )
		{
			commande = args[0];
		}
		traiterCommande( commande );
	}
	
	public Maitre()
	{
		objJeu = new ControleurJeu();
		//this.commandToDo = "";
		
		//ServerFrame window = new ServerFrame(this);
		//window.showIt("Server MathEnJeu");
		this.isOn = false;
		
	}
	
	public static void traiterCommande( String commandes )
	{
		Maitre maitre = new Maitre();
						
		String commande;
		
		if(commandes != null)
		{
			//on enlève les \r de la commande
			commande = commandes.replaceAll("\\r","");
		}else
		{
			commande = "";
		}
		
		if(commande.equals("") || commande.equals( "demarrer" ))
		{
			System.out.println( "demarrer -- commande = " + commande );
			
			
			Thread thread = new Thread( maitre, "Maitre" );
			thread.start();
			maitre.demarrer();	
			
		}else if( commande.equals( "win" ) )
		{
			maitre.setServerWindow();
			maitre.getServerWindow().showIt("Server MathEnJeu");
			
            System.out.println( "demarrer -- commande = " + commande );
			
			
			Thread thread = new Thread( maitre, "Maitre" );
			thread.start();
			maitre.demarrer();	
		}
		else if( commande.equals( "arreter" ) )
		{
			System.out.println( "arreter" );
			try 
			{
				Socket socket = new Socket( "localhost", 6101 );   
				byte [] buffer = new byte[2];
				buffer[0] = (byte)_STOP;
				buffer[1] = (byte)0;
				socket.getOutputStream().write( buffer );
				if(maitre != null)
				   maitre.stopServer();
				

			} 
			catch (UnknownHostException e) 
			{
				//e.printStackTrace();
				objLogger.error( e.getMessage() );
				System.out.println( "Le serveur n'est pas en ligne" );
			} 
			catch (IOException e) 
			{
				//e.printStackTrace();
				objLogger.error( e.getMessage() );
				System.out.println( "Le serveur n'est pas en ligne" );
			}
		}
		else if( commande.equals( "status" ) )
		{
			System.out.println( "status" );
			try 
			{
				Socket socket = new Socket( "localhost", 6101 );  
				byte [] buffer = new byte[256];
				buffer[0] = (byte)_STATUS;
				buffer[1] = (byte)0;
				socket.getOutputStream().write( buffer );
				socket.getInputStream().read( buffer );
				System.out.println( new String( buffer ) );
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println( "Erreur : Mauvaise commande" );
		}
	}
	
	public void demarrer()
	{
		//System.out.println( "le serveur tests start "  + this.isOn);	
		if(!this.isOn){
		   System.out.println( "demarrer le serveur" );
		   this.isOn = true;
           objJeu.demarrer();		   
		}
	}
	
	public void stopServer()
	{
		//System.out.println( "le serveur test stop "  + this.isOn);		
		if(this.isOn){
			System.out.println( "arreter le serveur" );		
			this.isOn = false;
			objJeu.arreter();
			objJeu = new ControleurJeu();
			//System.exit( 0 );				
		}
		//System.out.println( "le serveur "  + this.isOn);
	}
	
	public void exitServer() {
		System.exit( 0 );
	}
	
	public void run()
	{
		try 
		{
			boolean arret = false;
			ServerSocket socketServeur = new ServerSocket( 6101, 2, InetAddress.getByName("localhost")); 
			while( !arret )
			{
				//System.out.println( "le maitre "  + this.commandToDo);

				Socket socket = socketServeur.accept();
				if( socket.getInetAddress().isLoopbackAddress() == true )
				{
					byte [] buffer = new byte[256];
					socket.getInputStream().read( buffer );
					byte commande = (byte)buffer[0];
					System.out.println(commande);
					if( commande == (byte)_STOP )
					{
						System.out.println( "arreter le serveur" );
						this.stopServer();
						socket.getOutputStream().write( (byte)_STOP);//buffer );
						System.exit( 0 );
					}
					else if( commande == (byte)_STATUS )
					{
						System.out.println( "obtenir le status du serveur" );
						//String message = "Le serveur est en ligne";
						//buffer = message.getBytes();
						socket.getOutputStream().write( (byte)_ON);//buffer );
					}
					else
					{
						System.out.println( "ERREUR : Mauvaise commande" );
					}
				}// end first if
			
			}
		} 
		catch (IOException e) 
		{	
			objLogger.error( e.getMessage() );
		}
	}

	/**
	 * @param serverMonitor the serverMonitor to set
	 */
	public void setServerWindow() {
		this.serverWindow  = new ServerFrame(this);
	}

	/**
	 * @return the serverMonitor
	 */
	public ServerFrame getServerWindow() {
		return serverWindow;
	}

	
    
	/*
	public void setCommandToDo(String commandToDo) {
		this.commandToDo = commandToDo;
	}

	public String getCommandToDo() {
		return commandToDo;
	}*/
}
