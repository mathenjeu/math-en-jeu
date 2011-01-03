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
 */
public class EventPlayerPictureCanceled extends Evenement {

	// Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui a annuler son dessin
    private String strNomUtilisateur;
    
	// Déclaration d'une variable qui va garder le numéro Id du personnage 
	private int intIdPersonnage;
	
	 /**
     * Constructeur de la classe EventPlayerCanceledPicture qui permet 
     * d'initialiser le numéro Id du personnage et le nom d'utilisateur du 
     * joueur qui vient de annuler son dessin. 
     */
    public EventPlayerPictureCanceled(String strNomUtilisateur,
			int intIdPersonnage) {
		super();
		 // Définir le numéro Id du personnage et le nom d'utilisateur du joueur 
		this.strNomUtilisateur = strNomUtilisateur;
		this.intIdPersonnage = intIdPersonnage;
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
			Element objNoeudParametreIdPersonnage = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
						
			// Créer des noeuds texte contenant le numéro Id du personnage et le 
			// nom d'utilisateur des noeuds paramètre
			Text objNoeudTexteIdPersonnage = objDocumentXML.createTextNode(Integer.toString(intIdPersonnage));
			Text objNoeudTexteNomUtilisateur = objDocumentXML.createTextNode(strNomUtilisateur);
						
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "PlayerPictureCanceled");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametreIdPersonnage.setAttribute("type", "IdPersonnage");
			objNoeudParametreNomUtilisateur.setAttribute("type", "NomUtilisateur");
						
			// Ajouter les noeuds texte aux noeuds des paramètres
			objNoeudParametreIdPersonnage.appendChild(objNoeudTexteIdPersonnage);
			objNoeudParametreNomUtilisateur.appendChild(objNoeudTexteNomUtilisateur);
						
			// Ajouter les noeuds paramètres au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
			objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
						
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
