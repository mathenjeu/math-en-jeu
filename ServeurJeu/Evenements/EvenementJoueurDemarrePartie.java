package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Jean-François Brind'Amour
 */
public class EvenementJoueurDemarrePartie extends Evenement
{
	// Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui a démarré la partie
	private String strNomUtilisateur;

	// Déclaration d'une variable qui va garder le numéro Id du personnage 
	private int intIdPersonnage;     

	/**
	 * Constructeur de la classe EvenementJoueurDemarrePartie qui permet 
	 * d'initialiser le numéro Id du personnage et le nom d'utilisateur du 
	 * joueur qui vient de démarrer la partie. 
	 */
	public EvenementJoueurDemarrePartie(String nomUtilisateur, int idPersonnage)
	{
		// Définir le numéro Id du personnage et le nom d'utilisateur du joueur 
		// qui a démarré la partie
		intIdPersonnage = idPersonnage;
		strNomUtilisateur = nomUtilisateur;
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
		//objNoeudCommande.setAttribute("no", Integer.toString(1));
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "JoueurDemarrePartie");

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
