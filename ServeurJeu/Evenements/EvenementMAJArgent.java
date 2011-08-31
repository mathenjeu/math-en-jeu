package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author François Gingras
 */

public class EvenementMAJArgent extends Evenement
{
    // Déclaration d'une variable qui va garder le nom d'utilisateur du
    // joueur dont l'argent est mis à jour
    private String strNomUtilisateur;

    // Déclaration d'une variable qui va garder le nouvel argent
    private int intArgent;
    
    private String messxml;
	
    /**
     * Constructeur de la classe EvenementMAJArgent qui permet d'initialiser
     * le nom d'utilisateur du joueur et son nouvel argent 
     */
    public EvenementMAJArgent(String nomUtilisateur, int nouvelArgent)
    {
        // Définir le nom d'utilisateur du joueur
        strNomUtilisateur = nomUtilisateur;
        
        // Définir le nouvel argent du joueur
        intArgent = nouvelArgent;
        messxml = "";
        generateString();
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement pour la
	 * mise à jour d'argent et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
		return messxml;
	}
	
	private void generateString()
	{

		try
		{
	        // Appeler une fonction qui va créer un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Créer le noeud de commande à retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Créer les noeuds des paramètres
			Element objNoeudParametreNom = objDocumentXML.createElement("parametre");
			Element objNoeudParametreArgent = objDocumentXML.createElement("parametre");
			
			// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
			Text objNoeudTexteNom = objDocumentXML.createTextNode(strNomUtilisateur);
			Text objNoeudTexteArgent = objDocumentXML.createTextNode(Integer.toString(intArgent));
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(0));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "MAJArgent");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre pour le nom de l'utilisateur
			objNoeudParametreNom.setAttribute("type", "NomUtilisateur");
			
			// Ajouter le noeud texte avec le nom de l'utilisateur au noeud du paramètre
			objNoeudParametreNom.appendChild(objNoeudTexteNom);
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre pour l'argent'
			objNoeudParametreArgent.setAttribute("type", "Argent");
			
			// Ajouter le noeud texte avec l'argent au noeud du paramètre
			objNoeudParametreArgent.appendChild(objNoeudTexteArgent);
			
			// Ajouter les noeuds paramètres au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNom);
			objNoeudCommande.appendChild(objNoeudParametreArgent);
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

			// Transformer le document XML en code XML
			messxml = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		
	}
	
}
