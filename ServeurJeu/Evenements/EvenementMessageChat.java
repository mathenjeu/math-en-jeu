package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import ServeurJeu.Monitoring.Moniteur;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author François Gingras
 * Y'a pas beaucoup de commentaires mais c'est pratiquement la même chose que
 * les autres événements comme EvenementMAJPointage
 */

public class EvenementMessageChat extends Evenement
{
        // Chaîne de caractère qui va garder en mémoire le nom du joueur qui a
        // envoyé le message
	private String joueurQuiEnvoieLeMessage;
        
        // Le message en tant que tel
        private String messageAEnvoyer;
	
    public EvenementMessageChat(String joueurQuiEnvoieLeMessage, String messageAEnvoyer)
    {
        this.joueurQuiEnvoieLeMessage = joueurQuiEnvoieLeMessage;
        this.messageAEnvoyer = messageAEnvoyer;
    }
	
	protected String genererCodeXML(InformationDestination information)
	{
		Moniteur.obtenirInstance().debut("EvenementUtiliserObjet.genererCodeXML");
		
		/*
		 * <commande no="57" nom="MessageChat" type="Evenement">
		 *     <parametre type="joueurQuiEnvoieLeMessage">AdversaireXYZ</parametre>
                 *     <parametre type="message">
                            |||| ICI on print directement le contenu de messageAEnvoyer |||||
                 *     </parametre>
		 * </commande>
		 *
		 */

	    String strCodeXML = "";
	    
		try
		{
                        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "MessageChat");
			
			Element objNoeudParametreJoueurQuiEnvoieLeMessage = objDocumentXML.createElement("parametre");
                        Element objNoeudParametreMessageAEnvoyer = objDocumentXML.createElement("parametre");
			
			Text objNoeudTexteJoueurQuiEnvoieLeMessage = objDocumentXML.createTextNode(joueurQuiEnvoieLeMessage);
			Text objNoeudTexteMessageAEnvoyer = objDocumentXML.createTextNode(messageAEnvoyer);
			
			objNoeudParametreJoueurQuiEnvoieLeMessage.setAttribute("type", "joueurQuiEnvoieLeMessage");
                        objNoeudParametreMessageAEnvoyer.setAttribute("type", "messageAEnvoyer");
			
			objNoeudParametreJoueurQuiEnvoieLeMessage.appendChild(objNoeudTexteJoueurQuiEnvoieLeMessage);
                        objNoeudParametreMessageAEnvoyer.appendChild(objNoeudTexteMessageAEnvoyer);
			
			objNoeudCommande.appendChild(objNoeudParametreJoueurQuiEnvoieLeMessage);
			objNoeudCommande.appendChild(objNoeudParametreMessageAEnvoyer);
			
			objDocumentXML.appendChild(objNoeudCommande);
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
                        strCodeXML = strCodeXML.replaceAll("&lt;", "<");
                        strCodeXML = strCodeXML.replaceAll("&gt;", ">");
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		Moniteur.obtenirInstance().fin();
		return strCodeXML;
	}
}
