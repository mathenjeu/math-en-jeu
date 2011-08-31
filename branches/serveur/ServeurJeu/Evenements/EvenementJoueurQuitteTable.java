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
public class EvenementJoueurQuitteTable extends Evenement
{
	// Déclaration d'une variable qui va garder le numéro de la table que 
	// le joueur a quitté
    private int intNoTable;
    
	// Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui a quitté la table
    private String strNomUtilisateur;
    
    private String messxml;
    
    /**
     * Constructeur de la classe EvenementJoueurQuitteTable qui permet 
     * d'initialiser le numéro de la table et le nom d'utilisateur du 
     * joueur qui a quitté la table. 
     */
    public EvenementJoueurQuitteTable(int noTable, String nomUtilisateur)
    {
        // Définir le numéro de la table et le nom d'utilisateur du joueur 
    	// qui a quitté la table
    	intNoTable = noTable;
        strNomUtilisateur = nomUtilisateur;
        messxml = "";
        generateString();
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un joueur
	 * quittant la table et de le retourner.
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
				
				// Créer les noeuds de paramètre
				Element objNoeudParametreNoTable = objDocumentXML.createElement("parametre");
				Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
				
				// Créer des noeuds texte contenant le numéro de la table et le 
				// nom d'utilisateur des noeuds paramètre
				Text objNoeudTexteNoTable = objDocumentXML.createTextNode(Integer.toString(intNoTable));
				Text objNoeudTexteNomUtilisateur = objDocumentXML.createTextNode(strNomUtilisateur);
				
				// Définir les attributs du noeud de commande
				objNoeudCommande.setAttribute("no", Integer.toString(0));
				objNoeudCommande.setAttribute("type", "Evenement");
				objNoeudCommande.setAttribute("nom", "JoueurQuitteTable");
				
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
