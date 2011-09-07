package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class EventPlayerSelectedPicture extends Evenement {
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
	public EventPlayerSelectedPicture(String strNomUtilisateur,
			int intIdPersonnage) {
		super();
		// Définir le numéro Id du personnage et le nom d'utilisateur du joueur 
		this.strNomUtilisateur = strNomUtilisateur;
		this.intIdPersonnage = intIdPersonnage;
		generateXML();
	}

	private void generateXML()
	{
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
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "PlayerSelectedPicture");

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

	}
}
