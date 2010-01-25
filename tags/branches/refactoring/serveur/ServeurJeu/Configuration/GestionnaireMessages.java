package ServeurJeu.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Jean-François Fournier
 */
public class GestionnaireMessages {

  private static XMLConfiguration objConfigMessages;
  private static String strFichierConfig;
  
  static private Logger objLogger = Logger.getLogger( GestionnaireMessages.class );

  
  /**
   * Il faut initialiser la classe lorsque le serveur démarre sinon les
   * appels à la fonction message() échoueront
   *
   */
  public static void initialiser()
  {

    GestionnaireConfiguration config = GestionnaireConfiguration.getInstance();
    strFichierConfig = config.obtenirString("controleurjeu.messages.fichierxml");

    try
    {
      objConfigMessages = new XMLConfiguration(strFichierConfig);
    }
    catch(ConfigurationException e) {
      objLogger.log(Level.WARN, e.getMessage(), e);
    }
  }

  public static String message(String nomMessage)
  {
    String strMessage = objConfigMessages.getString(nomMessage);

    // Vérifier si on a trouvé le message dont le nom est le paramètre
    // nomMessage, si non, il faut spécifier l'erreur
    if (strMessage == null)
    {
      return "ERREUR: Message <" + nomMessage + "> non trouvé dans le fichier " + 
      strFichierConfig;
    }
    else
    {
      return strMessage;
    }

  }
}
