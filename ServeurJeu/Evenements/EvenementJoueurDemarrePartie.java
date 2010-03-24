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
public class EvenementJoueurDemarrePartie extends Evenement
{
	// Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui a démarré la partie
    private String strNomUtilisateur;
    
	// Déclaration d'une variable qui va garder le numéro Id du personnage 
	// choisi par le joueur
    private int intIdPersonnage;
    
    private String clothesColor;
    
    /**
     * Constructeur de la classe EvenementJoueurDemarrePartie qui permet 
     * d'initialiser le numéro Id du personnage et le nom d'utilisateur du 
     * joueur qui vient de démarrer la partie. 
     */
    public EvenementJoueurDemarrePartie(String nomUtilisateur, int idPersonnage, String cloColor)
    {
        // Définir le numéro Id du personnage et le nom d'utilisateur du joueur 
    	// qui a démarré la partie
    	intIdPersonnage = idPersonnage;
        strNomUtilisateur = nomUtilisateur;
        this.clothesColor = cloColor;
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un joueur
	 * démarrant une partie et de le retourner.
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
			Element objNoeudParametreIdPersonnage = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
			Element objNoeudParametreCouleur = objDocumentXML.createElement("parametre");
			
			// Créer des noeuds texte contenant le numéro Id du personnage et le 
			// nom d'utilisateur des noeuds paramètre
			Text objNoeudTexteIdPersonnage = objDocumentXML.createTextNode(Integer.toString(intIdPersonnage));
			Text objNoeudTexteNomUtilisateur = objDocumentXML.createTextNode(strNomUtilisateur);
			Text objNoeudTexteCouleur = objDocumentXML.createTextNode(clothesColor);
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "JoueurDemarrePartie");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametreIdPersonnage.setAttribute("type", "IdPersonnage");
			objNoeudParametreNomUtilisateur.setAttribute("type", "NomUtilisateur");
			objNoeudParametreCouleur.setAttribute("type", "ClothesColor");
			
			// Ajouter les noeuds texte aux noeuds des paramètres
			objNoeudParametreIdPersonnage.appendChild(objNoeudTexteIdPersonnage);
			objNoeudParametreNomUtilisateur.appendChild(objNoeudTexteNomUtilisateur);
			objNoeudParametreCouleur.appendChild(objNoeudTexteCouleur);
			
			// Ajouter les noeuds paramètres au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
			objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
			objNoeudCommande.appendChild(objNoeudParametreCouleur);
			
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
