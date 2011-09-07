package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Jean-François Fournier
 */

public class EvenementMAJPointage extends Evenement
{
	// Déclaration d'une variable qui va garder le nom d'utilisateur du
	// joueur dont le pointage est mis à jour
	private String strNomUtilisateur;

	// Déclaration d'une variable qui va garder le nouveau pointage
	private int intPointage;

	/**
	 * Constructeur de la classe EvenementMAJPointage qui permet d'initialiser
	 * le nom d'utilisateur du joueur et son nouveau pointage 
	 */
	public EvenementMAJPointage(String nomUtilisateur, int nouveauPointage)
	{
		// Définir le nom d'utilisateur du joueur
		strNomUtilisateur = nomUtilisateur;

		// Définir le nouveau pointage du joueur
		intPointage = nouveauPointage;

		generateXML();
	}


	private void generateXML()
	{
		// Créer le noeud de commande à retourner
		Element objNoeudCommande = objDocumentXML.createElement("commande");

		// Créer les noeuds des paramètres
		Element objNoeudParametreNom = objDocumentXML.createElement("parametre");
		Element objNoeudParametrePointage = objDocumentXML.createElement("parametre");

		// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
		Text objNoeudTexteNom = objDocumentXML.createTextNode(strNomUtilisateur);
		Text objNoeudTextePointage = objDocumentXML.createTextNode(Integer.toString(intPointage));

		// Définir les attributs du noeud de commande
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "MAJPointage");

		// On ajoute un attribut type qui va contenir le type
		// du paramètre pour le nom de l'utilisateur
		objNoeudParametreNom.setAttribute("type", "NomUtilisateur");

		// Ajouter le noeud texte avec le nom de l'utilisateur au noeud du paramètre
		objNoeudParametreNom.appendChild(objNoeudTexteNom);

		// On ajoute un attribut type qui va contenir le type
		// du paramètre pour le pointage
		objNoeudParametrePointage.setAttribute("type", "Pointage");

		// Ajouter le noeud texte avec le pointage au noeud du paramètre
		objNoeudParametrePointage.appendChild(objNoeudTextePointage);

		// Ajouter les noeuds paramètres au noeud de commande
		objNoeudCommande.appendChild(objNoeudParametreNom);
		objNoeudCommande.appendChild(objNoeudParametrePointage);

		// Ajouter le noeud de commande au noeud racine dans le document
		objDocumentXML.appendChild(objNoeudCommande);

	}
}
