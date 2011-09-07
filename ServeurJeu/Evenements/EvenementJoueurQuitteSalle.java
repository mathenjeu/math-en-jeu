package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementJoueurQuitteSalle extends Evenement
{
	// Déclaration d'une variable qui va garder le nom d'utilisateur du
	// joueur qui vient de quitter la salle
	private String strNomUtilisateur;

	/**
	 * Constructeur de la classe EvenementJoueurQuitteSalle qui permet 
	 * d'initialiser le nom d'utilisateur du joueur qui vient d'entrer. 
	 */
	public EvenementJoueurQuitteSalle(String nomUtilisateur)
	{
		// Définir le nom d'utilisateur du joueur qui a quitté
		strNomUtilisateur = nomUtilisateur;
		generateXML();
	}


	private void generateXML()
	{
		// Créer le noeud de commande à retourner
		Element objNoeudCommande = objDocumentXML.createElement("commande");

		// Créer le noeud du paramètre
		Element objNoeudParametre = objDocumentXML.createElement("parametre");

		// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
		Text objNoeudTexte = objDocumentXML.createTextNode(strNomUtilisateur);

		// Définir les attributs du noeud de commande
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "JoueurQuitteSalle");

		// On ajoute un attribut type qui va contenir le type
		// du paramètre
		objNoeudParametre.setAttribute("type", "NomUtilisateur");

		// Ajouter le noeud texte au noeud du paramètre
		objNoeudParametre.appendChild(objNoeudTexte);

		// Ajouter le noeud paramètre au noeud de commande
		objNoeudCommande.appendChild(objNoeudParametre);

		// Ajouter le noeud de commande au noeud racine dans le document
		objDocumentXML.appendChild(objNoeudCommande);
	}
}
