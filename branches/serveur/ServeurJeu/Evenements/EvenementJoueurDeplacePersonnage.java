/*
 * Created on 2006-03-12
 *
 */
package ServeurJeu.Evenements;

import java.awt.Point;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc
 *
 */
public class EvenementJoueurDeplacePersonnage extends Evenement
{

	//	 Déclaration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui se deplace
	private String strNomUtilisateur;
	private Point objAnciennePosition;
	private Point objPositionJoueur;
	private String strCollision;
	private int intNouveauPointage;
	private int intNouvelArgent;
	private int playerBonus;
	private String messxml;

	public EvenementJoueurDeplacePersonnage(String nomUtilisateur, Point anciennePosition, 
			Point positionJoueur, String collision, int nouveauPointage, int nouvelArgent, int bonus)
	{
		// Définir le nom d'utilisateur du joueur qui se deplace
		strNomUtilisateur = nomUtilisateur;
		objAnciennePosition = anciennePosition;
		objPositionJoueur = positionJoueur;
		strCollision = collision;
		intNouveauPointage = nouveauPointage;
		intNouvelArgent = nouvelArgent;
		playerBonus = bonus;
		messxml = "";
		generateString();
	}

	/**
	 * Cette fonction permet de générer le code XML de l'événement d'un joueur
	 * qui se deplace et de le retourner.
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

			// Créer le noeud du paramètre
			Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNouvellePosition = objDocumentXML.createElement("parametre");
			Element objNoeudParametreAnciennePosition = objDocumentXML.createElement("parametre");
			Element objNoeudParametreCollision = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNouveauPointage = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNouvelArgent = objDocumentXML.createElement("parametre");
			Element objNoeudParametreBonus = objDocumentXML.createElement("parametre");

			// Créer un noeud contenant le nom d'utilisateur du noeud paramètre
			Text objNoeudTexte = objDocumentXML.createTextNode(strNomUtilisateur);
			Text objNoeudTexteCollision = objDocumentXML.createTextNode(strCollision);

			Element objNoeudAnciennePosition = objDocumentXML.createElement("position");
			Element objNoeudPosition = objDocumentXML.createElement("position");

			// Définir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(0));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "JoueurDeplacePersonnage");

			// On ajoute un attribut type qui va contenir le type
			// du paramètre
			objNoeudParametreNomUtilisateur.setAttribute("type", "NomUtilisateur");
			objNoeudParametreNomUtilisateur.appendChild(objNoeudTexte);

			objNoeudAnciennePosition.setAttribute("x", new Integer( objAnciennePosition.x ).toString() );
			objNoeudAnciennePosition.setAttribute("y", new Integer( objAnciennePosition.y ).toString() );

			objNoeudPosition.setAttribute("x", new Integer( objPositionJoueur.x ).toString() );
			objNoeudPosition.setAttribute("y", new Integer( objPositionJoueur.y ).toString() );

			objNoeudParametreAnciennePosition.setAttribute("type", "AnciennePosition");
			objNoeudParametreAnciennePosition.appendChild( objNoeudAnciennePosition );

			objNoeudParametreNouvellePosition.setAttribute("type", "NouvellePosition");
			objNoeudParametreNouvellePosition.appendChild( objNoeudPosition );

			objNoeudParametreCollision.setAttribute("type", "Collision");
			objNoeudParametreCollision.appendChild( objNoeudTexteCollision );


			Text objNoeudTextePointage = objDocumentXML.createTextNode(Integer.toString(intNouveauPointage));
			objNoeudParametreNouveauPointage.setAttribute("type", "NouveauPointage");
			objNoeudParametreNouveauPointage.appendChild(objNoeudTextePointage);

			Text objNoeudTexteArgent = objDocumentXML.createTextNode(Integer.toString(intNouvelArgent));
			objNoeudParametreNouvelArgent.setAttribute("type", "NouvelArgent");
			objNoeudParametreNouvelArgent.appendChild(objNoeudTexteArgent);

			Text objNoeudTexteBonus = objDocumentXML.createTextNode(Integer.toString(playerBonus));
			objNoeudParametreBonus.setAttribute("type", "Bonus");
			objNoeudParametreBonus.appendChild(objNoeudTexteBonus);

			// Ajouter le noeud paramètre au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
			objNoeudCommande.appendChild(objNoeudParametreAnciennePosition);
			objNoeudCommande.appendChild(objNoeudParametreNouvellePosition);
			objNoeudCommande.appendChild(objNoeudParametreCollision);
			objNoeudCommande.appendChild(objNoeudParametreNouveauPointage);
			objNoeudCommande.appendChild(objNoeudParametreNouvelArgent);
			objNoeudCommande.appendChild(objNoeudParametreBonus);


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

		if(ControleurJeu.modeDebug) System.out.println("Evenement: " + messxml);
	}
}
