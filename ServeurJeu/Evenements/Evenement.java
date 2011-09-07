package ServeurJeu.Evenements;

import java.util.ArrayList;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ServeurJeu.Configuration.GestionnaireMessages;
import ClassesUtilitaires.UtilitaireXML;

/**
 * @author Jean-François Brind'Amour
 */
public class Evenement
{
	// Déclaration d'une liste de InformationDestination
	protected ArrayList<InformationDestination> lstInformationDestination;
	//un document XML dans lequel on peut ajouter des noeuds
	protected Document objDocumentXML;

	public Evenement()
	{
		lstInformationDestination = new ArrayList<InformationDestination>();
	    // Appeler une fonction qui va créer le document XML 
		objDocumentXML = UtilitaireXML.obtenirDocumentXML();
	}

	/**
	 * Cette fonction permet d'ajouter un nouveau InformationDestination à la
	 * liste d'InformationDestionation qui sert à savoir à qui envoyer 
	 * l'événement courant.
	 * 
	 * @param InformationDestionation information : Un objet contenant le numéro
	 * 						de commande ainsi que le ProtocoleJoueur du joueur
	 * 						à qui envoyer l'événement courant.
	 */
	public void ajouterInformationDestination(InformationDestination information)
	{
		// Ajouter l'InformationDestination à la fin de la liste
		lstInformationDestination.add(information);		
	}

	
	/**
	 * Cette méthode permet d'envoyer l'événement courant à tous les joueurs
	 * se trouvant dans la liste des InformationDestination.
	 */
	public void envoyerEvenement()
	{
		// Passer tous les InformationDestination se trouvant dans la liste de
		// l'événement courant et envoyer à chacun l'événement courant
		for (int i = 0; i < lstInformationDestination.size(); i++)
		{
			// Faire la référence vers l'objet InformationDestination courant
			InformationDestination information =  lstInformationDestination.get(i);
			
			// Envoyer l'événement au joueur courant
			information.obtenirProtocoleJoueur().envoyerMessage(genererStringXML(information));			
		}

	}
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un joueur
	 * et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererStringXML(InformationDestination information)
	{

		Element objNoeudCommande = objDocumentXML.getDocumentElement();
		objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));

		try {
			return UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}

		return ""; //TO DO ... 
	}
}
