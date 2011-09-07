package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

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

		this.intSeconds = intSeconds;
		generateXML();		
	}


	private void generateXML()
	{
		// Créer le noeud de commande à retourner
		Element objNoeudCommande = objDocumentXML.createElement("commande");

		// Créer les noeuds de paramètre
		Element objNoeudParametreSeconds = objDocumentXML.createElement("parametre");

		// Créer le noeud texte contenant le nombre des seconds 
		Text objNoeudTexteSeconds = objDocumentXML.createTextNode(Integer.toString(intSeconds));

		// Définir les attributs du noeud de commande
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
	}

}
