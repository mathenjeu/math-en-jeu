package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementJoueurEntreTable extends Evenement
{
	// Déclaration d'une variable qui va garder le numéro de la table dans 
	// laquelle le joueur est entré
    private int intNoTable;
    
	// Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui est entré dans la table
    private String strNomUtilisateur;
    
    /**
     * Constructeur de la classe EvenementJoueurEntreTable qui permet 
     * d'initialiser le numéro de la table et le nom d'utilisateur du 
     * joueur qui vient d'entrer dans la table. 
     */
    public EvenementJoueurEntreTable(int noTable, String nomUtilisateur)
    {
        // Définir le numéro de la table et le nom d'utilisateur du joueur 
    	// qui est entré
    	intNoTable = noTable;
        strNomUtilisateur = nomUtilisateur;
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un joueur
	 * entrant dans la table et de le retourner.
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
			
			// Créer les noeuds de paramètre
			Element objNoeudParametreNoTable = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
			
			// Créer des noeuds texte contenant le numéro de la table et le 
			// nom d'utilisateur des noeuds paramètre
			Text objNoeudTexteNoTable = objDocumentXML.createTextNode(Integer.toString(intNoTable));
			Text objNoeudTexteNomUtilisateur = objDocumentXML.createTextNode(strNomUtilisateur);
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "JoueurEntreTable");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametreNoTable.setAttribute("type", "NoTable");
			objNoeudParametreNomUtilisateur.setAttribute("type", "NomUtilisateur");
			
			// Ajouter les noeuds texte aux noeuds des paramètres
			objNoeudParametreNoTable.appendChild(objNoeudTexteNoTable);
			objNoeudParametreNomUtilisateur.appendChild(objNoeudTexteNomUtilisateur);
			
			// Ajouter les noeuds paramètres au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNoTable);
			objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
			
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
		
		return strCodeXML;
	}
}
