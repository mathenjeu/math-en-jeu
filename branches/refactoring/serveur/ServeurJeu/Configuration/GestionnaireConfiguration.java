package ServeurJeu.Configuration;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

//TODO : remove the obtenirXXX and let the 
public class GestionnaireConfiguration extends XMLConfiguration
{
  
  private static class GestionnaireConfigurationHolder {
    private final static GestionnaireConfiguration INSTANCE = new GestionnaireConfiguration();
  }
  
  static private Logger objLogger = Logger.getLogger( GestionnaireConfiguration.class );
  
  //private static GestionnaireConfiguration _instance = null;
  //private XMLConfiguration _config = null;
  private static final String FICHIER_CONFIG = "mathenjeu.xml";
  
  private GestionnaireConfiguration() {
    try {
      load(FICHIER_CONFIG);
    } catch (ConfigurationException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    }

  }
  
  /*
  private void init() { 
    try 
    {
      _config = new XMLConfiguration( FICHIER_CONFIG );
    } 
    catch (ConfigurationException e) 
    {
      e.printStackTrace();
    }
  }
  */
  
  public static GestionnaireConfiguration getInstance()
  {
    return GestionnaireConfigurationHolder.INSTANCE;
  }
  
  public int obtenirNombreEntier( String id )
  {
    return getInt( id );
  }
  
  public String obtenirString( String id )
  {
    return getString( id );
  }
  
  public float obtenirNombreDecimal( String id )
  {
    return getFloat( id );
  }
  
  public boolean obtenirValeurBooleenne( String id )
  {
    return getBoolean( id );
  }
        
  public List obtenirListe( String id )
  {
    return getList( id );
  }
        
        public Document getDocument()
        {
            return getDocument();
        }
}
