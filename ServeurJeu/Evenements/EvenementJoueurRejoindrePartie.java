package ServeurJeu.Evenements;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * @author Oloieri Lilian
 */

public class EvenementJoueurRejoindrePartie extends Evenement{
	// Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui a rejoindre la partie
	private String strNomUtilisateur;

	// Déclaration d'une variable qui va garder le numéro Id du personnage 
	// choisi par le joueur
	private int intIdPersonnage;

	// Déclaration d'une variable qui va garder les points du joueur
	private int intPointage;

	// Déclaration d'une variable qui va garder le role du joueur
	private int userRole;

	// Déclaration d'une variable qui va garder la clocolor du joueur
	private int userColor;

	private int xPosition;
	private int yPosition;


	/**
	 * Constructeur de la classe EvenementJoueurDemarrePartie qui permet 
	 * d'initialiser le numéro Id du personnage et le nom d'utilisateur du 
	 * joueur qui vient de démarrer la partie. 
	 */

	public EvenementJoueurRejoindrePartie(JoueurHumain player) {
		// Définir le numéro Id du personnage et le nom d'utilisateur du joueur 
		// qui a démarré la partie
		intIdPersonnage = player.obtenirPartieCourante().obtenirIdPersonnage();
		strNomUtilisateur = player.obtenirNom();
		intPointage = player.obtenirPartieCourante().obtenirPointage();
		userRole = player.getRole();
		userColor = player.obtenirPartieCourante().getClothesColor();
		xPosition = player.obtenirPartieCourante().obtenirPositionJoueur().x;
		yPosition = player.obtenirPartieCourante().obtenirPositionJoueur().y;

		generateXML();
	}


	private void generateXML()
	{
		// Créer le noeud de commande à retourner
		Element objNoeudCommande = objDocumentXML.createElement("commande");

		// Créer les noeuds de paramètre
		Element objNoeudParametreIdPersonnage = objDocumentXML.createElement("parametre");
		Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
		Element objNoeudParametrePointage = objDocumentXML.createElement("parametre");
		Element objNoeudParametreCloColor = objDocumentXML.createElement("parametre");
		Element objNoeudParametreRole = objDocumentXML.createElement("parametre");
		Element objNoeudParametreXPosition = objDocumentXML.createElement("parametre");
		Element objNoeudParametreYPosition = objDocumentXML.createElement("parametre");

		// Créer des noeuds texte contenant le numéro Id du personnage et le 
		// nom d'utilisateur des noeuds paramètre
		Text objNoeudTexteIdPersonnage = objDocumentXML.createTextNode(Integer.toString(intIdPersonnage));
		Text objNoeudTexteNomUtilisateur = objDocumentXML.createTextNode(strNomUtilisateur);
		Text objNoeudTextePointage = objDocumentXML.createTextNode(Integer.toString(intPointage));
		Text objNoeudTexteRole = objDocumentXML.createTextNode(Integer.toString(userRole));
		Text objNoeudTexteColor = objDocumentXML.createTextNode(Integer.toString(userColor));
		Text objNoeudTexteXPosition = objDocumentXML.createTextNode(Integer.toString(xPosition));
		Text objNoeudTexteYPosition = objDocumentXML.createTextNode(Integer.toString(yPosition));

		// Définir les attributs du noeud de commande
		objNoeudCommande.setAttribute("type", "Evenement");
		objNoeudCommande.setAttribute("nom", "JoueurRejoindrePartie");

		// On ajoute un attribut type qui va contenir le type
		// du paramètre
		objNoeudParametreIdPersonnage.setAttribute("type", "IdPersonnage");
		objNoeudParametreNomUtilisateur.setAttribute("type", "NomUtilisateur");
		objNoeudParametrePointage.setAttribute("type", "Pointage");
		objNoeudParametreCloColor.setAttribute("type", "Color");
		objNoeudParametreRole.setAttribute("type", "Role");
		objNoeudParametreXPosition.setAttribute("type", "xPosition");
		objNoeudParametreYPosition.setAttribute("type", "yPosition");

		// Ajouter les noeuds texte aux noeuds des paramètres
		objNoeudParametreIdPersonnage.appendChild(objNoeudTexteIdPersonnage);
		objNoeudParametreNomUtilisateur.appendChild(objNoeudTexteNomUtilisateur);
		objNoeudParametrePointage.appendChild(objNoeudTextePointage);
		objNoeudParametreRole.appendChild(objNoeudTexteRole);
		objNoeudParametreCloColor.appendChild(objNoeudTexteColor);
		objNoeudParametreXPosition.appendChild(objNoeudTexteXPosition);
		objNoeudParametreYPosition.appendChild(objNoeudTexteYPosition);

		// Ajouter les noeuds paramètres au noeud de commande
		objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
		objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
		objNoeudCommande.appendChild(objNoeudParametrePointage);
		objNoeudCommande.appendChild(objNoeudParametreRole);
		objNoeudCommande.appendChild(objNoeudParametreCloColor);
		objNoeudCommande.appendChild(objNoeudParametreXPosition);
		objNoeudCommande.appendChild(objNoeudParametreYPosition);	

		// Ajouter le noeud de commande au noeud racine dans le document
		objDocumentXML.appendChild(objNoeudCommande);
	}
}
