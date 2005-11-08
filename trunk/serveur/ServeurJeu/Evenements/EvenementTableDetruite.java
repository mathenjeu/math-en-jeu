package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementTableDetruite extends Evenement
{
	// Déclaration d'une variable qui va garder le numéro de la table qui a 
	// été détruite
    private int intNoTable;
    
    /**
     * Constructeur de la classe EvenementTableDetruite qui permet 
     * d'initialiser le numéro de la table. 
     */
    public EvenementTableDetruite(int noTable)
    {
        // Définir le numéro de la table qui a été créée
    	intNoTable = noTable;
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'une 
	 * table détruite et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
	    // Déclaration d'une variable qui va contenir le code XML à retourner
	    String strCodeXML = "";
	    
		try
		{
	        // Appeler une fonction qui va créer un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Créer le noeud de commande à retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Créer le noeud du paramètre
			Element objNoeudParametre = objDocumentXML.createElement("parametre");
			
			// Créer un noeud contenant le numéro de la table du noeud paramètre
			Text objNoeudTexte = objDocumentXML.createTextNode(Integer.toString(intNoTable));
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "TableDetruite");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametre.setAttribute("type", "NoTable");
			
			// Ajouter le noeud texte au noeud du paramètre
			objNoeudParametre.appendChild(objNoeudTexte);
			
			// Ajouter le noeud paramètre au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametre);
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

			// Transformer le document XML en code XML
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println("Une erreur est survenue lors de la transformation du document XML en chaine de caracteres");
		}
		catch (TransformerException te)
		{
			System.out.println("Une erreur est survenue lors de la conversion du document XML en chaine de caracteres");
		}
		
		return strCodeXML;
	}
}
