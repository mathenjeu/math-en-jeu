package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Monitoring.Moniteur;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementJoueurConnecte extends Evenement
{
	// Déclaration d'une variable qui va garder le nom d'utilisateur du
    // nouveau joueur qui vient de se connecter
    private String strNomUtilisateur;
    
    /**
     * Constructeur de la classe EvenementJoueurConnecte qui permet d'initialiser
     * le nom d'utilisateur du joueur qui vient de se connecter. 
     */
    public EvenementJoueurConnecte(String nomUtilisateur)
    {
        // Définir le nom d'utilisateur du nouveau joueur connecté
        strNomUtilisateur = nomUtilisateur;
    }
	
	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un nouveau
	 * joueur et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations à qui 
	 * 					envoyer l'événement
	 * @return String : Le code XML de l'événement à envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
		Moniteur.obtenirInstance().debut( "EvenementJoueurConnecte.genererCodeXML" );
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
			
			// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
			Text objNoeudTexte = objDocumentXML.createTextNode(strNomUtilisateur);
			
			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(0)); //information.obtenirNoCommande()
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "JoueurConnecte");
			
			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametre.setAttribute("type", "NomUtilisateur");
			
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
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		
		Moniteur.obtenirInstance().fin();
		if(ControleurJeu.modeDebug) System.out.println("Evenement: " + strCodeXML);
		return strCodeXML;
	}
}
