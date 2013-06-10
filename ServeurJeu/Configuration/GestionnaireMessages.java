package ServeurJeu.Configuration;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * @author Jean-François Fournier
 */
public class GestionnaireMessages {
	
	private static XMLConfiguration objConfigMessages;
	private static String strFichierConfig;
	
	/* Il faut initialiser la classe lorsque le serveur démarre sinon les
	 * appels à la fonction message() échoueront
	 */
	public static void initialiser()
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		strFichierConfig = config.obtenirString("controleurjeu.messages.fichierxml");
		
		try
		{
			objConfigMessages = new XMLConfiguration(strFichierConfig);
		}
		catch(Exception e)
		{
			
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
