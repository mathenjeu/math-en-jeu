package ServeurJeu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Maitre implements Runnable
{
	private ControleurJeu objJeu = null;
	public static void main(String[] args) 
	{
		if( args.length == 0 || args[0].equals( "demarrer" ) )
		{
			System.out.println( "demarrer" );
			Maitre maitre = new Maitre();
			Thread thread = new Thread( maitre );
			thread.start();
			maitre.demarrer();
		}
		else if( args[0].equals( "arreter" ) )
		{
			System.out.println( "arreter" );
			try 
			{
				Socket socket = new Socket( "localhost", 6101 );
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
			//mauvais argument
		}
	}
	
	public Maitre()
	{
		objJeu = new ControleurJeu();
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
					System.out.println( "arreter par un socket local" );
					arret = true;
					objJeu.arreter();
					System.exit( 0 );
				}
			}
		} 
		catch (IOException e) 
		{	
			e.printStackTrace();
		}
	}
}
