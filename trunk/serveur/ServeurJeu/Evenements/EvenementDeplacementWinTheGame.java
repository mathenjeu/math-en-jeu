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
 */

public class EvenementDeplacementWinTheGame extends Evenement
{
    private int x;
    private int y;
    
    public EvenementDeplacementWinTheGame(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement pour la
	 * mise à jour de la position du WinTheGame et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
		Moniteur.obtenirInstance().debut( "EvenementMAJArgent.genererCodeXML" );
		
		/*
		 * <commande no="57" nom="DeplacementWinTheGame" type="Evenement">
		 *     <parametre type="x">18</parametre>
		 *     <parametre type="y">15</parametre>
		 * </commande>
		 *
		 */
                // Noter que le déplacement ne se fait pas ici, on ne fait
                // qu'envoyer les nouvelles informations
		 
                // Déclaration d'une variable qui va contenir le code XML à retourner
                String strCodeXML = "";
	    
		try
		{
	        // Appeler une fonction qui va créer un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Créer le noeud de commande à retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "DeplacementWinTheGame");
                        objNoeudCommande.setAttribute("x", Integer.toString(x));
                        objNoeudCommande.setAttribute("y", Integer.toString(y));
			
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
		
		Moniteur.obtenirInstance().fin();
		
		return strCodeXML;
	}
	
}
