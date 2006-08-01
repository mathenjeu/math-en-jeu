package ServeurJeu.Monitoring;

import java.io.FileWriter;
import org.apache.log4j.Logger;

import ServeurJeu.Configuration.GestionnaireConfiguration;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;


public class Moniteur 
{
	private static Logger logger = Logger.getLogger( Monitor.class );
	private static Moniteur _instance = null;
	private static Monitor _moniteur = null;
	private String _fichier = null;
	
	private Moniteur()
	{
		_fichier = GestionnaireConfiguration.obtenirInstance().obtenirString( "monitor.fichier-sortie" );
	}
	
	public static Moniteur obtenirInstance()
	{
		if( _instance == null )
		{
			_instance = new Moniteur();
		}
		return _instance;
	}
	
	public void debut( String id )
	{
		_moniteur = MonitorFactory.start( id );
	}
	
	public void fin()
	{
		_moniteur.stop();
	}
	
	public void log()
	{
		MonitorComposite monitors =  MonitorFactory.getComposite( "allMonitors" );
		String report = monitors.getReport();
		
		try
		{
			FileWriter writer = new FileWriter( _fichier );
			writer.write( report );
			writer.close();
		}
		catch( Exception e )
		{
			logger.error( e.getMessage() );
			logger.error( "Impossible d'ecrire dans le fichier de monitoring." );
		}
	}
	
}
