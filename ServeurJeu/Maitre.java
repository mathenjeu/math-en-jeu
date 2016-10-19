package ServeurJeu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.GUI.ServerFrame;
import ServeurJeu.Temps.StopServerTask;

public class Maitre implements Runnable 
{
	private static Logger objLogger = Logger.getLogger( Maitre.class );
	private ControleurJeu objJeu = null;
	private ServerFrame serverWindow;
	private static final GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
	
	/*
	public static final String SETOFF = "OFF";
	public static final String SETON = "ON";
	public static final String EXIT = "EXIT";
	public static final String RESET = "RESET";
	*/
	
	public static final String SETOFF = "FERMER";
	public static final String SETON = "DEMARRER";
	public static final String EXIT = "SORTIR";
	public static final String RESET = "REMETTRE";
	
	// Boolean to indicate if server is on or off
	private boolean isOn;
	private boolean toReset;

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
		this.isOn = false;
		this.toReset = false;
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
		else
		{
			System.out.println( "Erreur : Mauvaise commande" );
		}
	}

	public void demarrer()
	{
		//System.out.println( "le serveur tests start "  + this.isOn);
		if(serverWindow != null)
			serverWindow.setLabelOn();
		toReset = false;
		objJeu = new ControleurJeu();
		if(!isOn){
			System.out.println( "demarre le serveur" );
			isOn = true;
			objJeu.demarrer();		   
		}
	}

	public void stopServer()
	{
		serverWindow.setLabelTerminating();
		//System.out.println( "le serveur test stop "  + this.isOn);		
		if(isOn){

			long nbSeconds = config.obtenirNombreEntier("controleurjeu.stopTimer");

			StopServerTask endTask = new StopServerTask(this);
			System.out.println( "arreter le serveur" );		
			isOn = false;
			objJeu.stopItLater();
			objJeu.obtenirGestionnaireTemps().putNewTask(endTask, nbSeconds * 1000);					
		}
		System.out.println( " server isOn = "  + isOn);
	}

	public void exitServerByCloseWindow() {

		 System.exit( 0 );
	}

	public void exitServerInWindow() {
		objJeu.arreter();
		objJeu = null;
		serverWindow.setLabelOff();
		
		if(toReset)
		{
			ServerBufferWorkingThread worker = new ServerBufferWorkingThread();
			worker.start();			
		}
	}
		

	public void run()
	{
		int port = config.obtenirNombreEntier("maitre.port");
		String address = config.obtenirString("maitre.address");
		boolean go = true;
		// it will accept only one server admin at the same time
		ServerSocket socketServeur = null;
		try {
			socketServeur = new ServerSocket( port, 5, InetAddress.getByName(address));
		} catch (UnknownHostException er) {
			objLogger.error( er.getMessage(), er );
		} catch (IOException er) {
			objLogger.error( er.getMessage(), er );
		} 
		Socket socket = null;
		PrintWriter out =  null;
		BufferedReader in = null;
		String inputLine, outputLine;

		try
		{
			while(go)
			{
				if(socketServeur != null){
					socket = socketServeur.accept();

					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					while ((inputLine = in.readLine()) != null) {	
						outputLine = treatMessage(inputLine);
						out.println(outputLine);
						out.flush();
					}
				}	
			}
						
			System.out.println( "arreter le serveur - fin du thread" );

		} 
		catch (IOException e) 
		{	
			objLogger.error( e.getMessage(), e );
		}
	}

	/**
	 * @param serverMonitor the serverMonitor to set
	 */
	private void setServerWindow() {
		this.serverWindow  = new ServerFrame(this);
	}

	/**
	 * @return the serverMonitor
	 */
	private ServerFrame getServerWindow() {
		return serverWindow;
	}

    private String treatMessage(String message)
    {
    	String answer = "";
    	if( message.endsWith("Stop") )
		{
			System.out.println( "arreter le serveur" );
			stopServer();
			answer = "Will stop...";
		}
		else if( message.endsWith("Status") )
		{
			System.out.println( "obtenir le status du serveur" );
			answer = "Server is On";
		}
		else
		{
			System.out.println( "ERREUR : Mauvaise commande" );
			answer = "Not known command";
		}
        return answer;
    }

	public void treatCommand(String actionCommand) {
		if(actionCommand.equals(Maitre.SETON)){
			   ServerBufferWorkingThread worker = new ServerBufferWorkingThread();
			   worker.start();			   
			}
			else if(actionCommand.equals(Maitre.SETOFF)){
				stopServer();				
			}
			else if(actionCommand.equals(Maitre.RESET)){
				toReset = true;
				if(isOn){
					stopServer();
				}else{
					ServerBufferWorkingThread worker = new ServerBufferWorkingThread();
					worker.start();
				}
			}else if(actionCommand.equals(Maitre.EXIT)){
				exitServerByCloseWindow();
							
			}		
	} // end treatCommand
	
	

	
	// Internal class used to avoid blocking On and Reset buttons
	// by working server thread
	class ServerBufferWorkingThread extends Thread
	{
		private ServerBufferWorkingThread (){}
		
		public void run(){
		   demarrer();	
		}
	}

}
