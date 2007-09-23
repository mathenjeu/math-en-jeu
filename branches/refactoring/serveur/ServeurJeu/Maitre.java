package ServeurJeu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class Maitre implements Runnable
{
	static private Logger objLogger = Logger.getLogger( Maitre.class );
	private ControleurJeu objJeu = null;
	private static final int _ARRETER = 1;
	private static final int _STATUS = 2;
	
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
    objJeu = ControleurJeu.getInstance();
	}
	
	public static void traiterCommande( String commande )
	{
		
		if(commande != null)
		{
			//on enlève les \r de la commande
			commande = commande.replaceAll("\\r","");
		}
		
		if( commande == null || commande.equals("") || commande.equals( "demarrer" ))
		{
			System.out.println( "demarrer" );
			Maitre maitre = new Maitre();
			Thread thread = new Thread( maitre );
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
				buffer[0] = (byte)_ARRETER;
				buffer[1] = (byte)0;
				socket.getOutputStream().write( buffer );
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
		objJeu.demarrer();
	}
	
	public void run()
	{
		try 
		{
			boolean arret = false;
			ServerSocket socketServeur = new ServerSocket( 6101 );
			while( !arret )
			{
				Socket socket = socketServeur.accept();
				if( socket.getInetAddress().isLoopbackAddress() == true )
				{
					byte [] buffer = new byte[256];
					socket.getInputStream().read( buffer );
					byte commande = (byte)buffer[0];
					if( commande == (byte)_ARRETER )
					{
						System.out.println( "arreter le serveur" );
						arret = true;
						objJeu.arreter();
						System.exit( 0 );
					}
					else if( commande == (byte)_STATUS )
					{
						System.out.println( "obtenir le status du serveur" );
						String message = "Le serveur est en ligne";
						buffer = message.getBytes();
						socket.getOutputStream().write( buffer );
					}
					else
					{
						System.out.println( "ERREUR : Mauvaise commande" );
					}
				}
			}
		} 
		catch (IOException e) 
		{	
			objLogger.error( e.getMessage() );
		}
	}
}
