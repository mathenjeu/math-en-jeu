package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Oloieri Lilian
 *
 */
public class StopServerEvent extends Evenement {

	// Déclaration d'une variable qui va garder le nombre des seconds
	// apres lesquelles le serveur va etre fermer ou restarter 
	private int intSeconds;
	
	 /**
     * Constructeur de la classe StopServerEvent qui permet 
     * d'initialiser le nombre des secondes. 
     */
    public StopServerEvent( int intSeconds) {
		super();
		
		this.intSeconds = intSeconds;
		
	}
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement 
	 * et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererCodeXML(InformationDestination information) {
		
		// Déclaration d'une variable qui va contenir le code XML à retourner
	    String strCodeXML = "";
	    
		try
		{
	        // Appeler une fonction qui va créer un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Créer le noeud de commande à retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Créer les noeuds de paramètre
			Element objNoeudParametreSeconds = objDocumentXML.createElement("parametre");
									
			// Créer le noeud texte contenant le nombre des seconds 
			Text objNoeudTexteSeconds = objDocumentXML.createTextNode(Integer.toString(intSeconds));
									
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "ServerWillStop");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametreSeconds.setAttribute("type", "nrSeconds");
									
			// Ajouter les noeuds texte aux noeuds des paramètres
			objNoeudParametreSeconds.appendChild(objNoeudTexteSeconds);
									
			// Ajouter les noeuds paramètres au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreSeconds);
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

			// Transformer le document XML en code XML
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		
		if(ControleurJeu.modeDebug) System.out.println("Evenement: " + strCodeXML);
		return strCodeXML;
	}

}
