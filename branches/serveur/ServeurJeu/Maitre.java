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
	private GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
	
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
		//objJeu = new ControleurJeu();
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
		else
		{
			System.out.println( "Erreur : Mauvaise commande" );
		}
	}

	public void demarrer()
	{
		//System.out.println( "le serveur tests start "  + this.isOn);
		objJeu = new ControleurJeu();
		if(!isOn){
			System.out.println( "demarrer le serveur" );
			isOn = true;
			objJeu.demarrer();		   
		}
	}

	public void stopServer()
	{
		//System.out.println( "le serveur test stop "  + this.isOn);		
		if(isOn){

			long nbSeconds = config.obtenirNombreEntier("controleurjeu.stopTimer");

			StopServerTask endTask = new StopServerTask(this, objJeu);
			System.out.println( "arreter le serveur" );		
			isOn = false;
			objJeu.stopItLater();
			objJeu.obtenirGestionnaireTemps().putNewTask(endTask, nbSeconds * 1000);
			//objJeu = new ControleurJeu();
			//System.exit( 0 );				
		}
		System.out.println( "stop server "  + isOn);
	}

	public void exitServer() {

		 System.exit( 0 );
	}

	public void exitServer2() {

		objJeu = null;
		serverWindow.setLabelOff();
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
			objLogger.error( er.getMessage() );
		} catch (IOException er) {
			objLogger.error( er.getMessage() );
		} 
		Socket socket = null;
		PrintWriter out =  null;
		BufferedReader in = null;
		String inputLine, outputLine;

		try
		{
			while( go )
			{
				socket = socketServeur.accept();

				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				while ((inputLine = in.readLine()) != null) {	
				    outputLine = treatAppletMessage(inputLine);
				    out.println(outputLine);
				    out.flush();
				}

	
			}
			// to inform the applet about exit
			socket = socketServeur.accept();
			if(!socket.isClosed())
				out.println("Will close...");
			out.flush();
			System.out.println( "arreter le serveur" );

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

    private String treatAppletMessage(String message)
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

	/*
	public void setCommandToDo(String commandToDo) {
		this.commandToDo = commandToDo;
	}

	public String getCommandToDo() {
		return commandToDo;
	}*/
}
