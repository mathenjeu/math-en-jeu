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

/**
 * @author Marc
 *
 */
public class EvenementJoueurDeplacePersonnage extends Evenement
{
	
//	 D�claration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui se deplace
    private String strNomUtilisateur;
    private Point objAnciennePosition;
    private Point objPositionJoueur;
    private String strCollision;
    
	public EvenementJoueurDeplacePersonnage(String nomUtilisateur, Point anciennePosition, Point positionJoueur, String collision )
    {
        // D�finir le nom d'utilisateur du joueur qui se deplace
        strNomUtilisateur = nomUtilisateur;
        objAnciennePosition = anciennePosition;
        objPositionJoueur = positionJoueur;
        strCollision = collision;
    }
	
	/**
	 * Cette fonction permet de g�n�rer le code XML de l'�v�nement d'un joueur
	 * qui se deplace et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations � qui 
	 * 					envoyer l'�v�nement
	 * @return String : Le code XML de l'�v�nement � envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
	    // D�claration d'une variable qui va contenir le code XML � retourner
	    String strCodeXML = "";
	    
		try
		{
	        // Appeler une fonction qui va cr�er un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Cr�er le noeud de commande � retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Cr�er le noeud du param�tre
			Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNouvellePosition = objDocumentXML.createElement("parametre");
			Element objNoeudParametreAnciennePosition = objDocumentXML.createElement("parametre");
			Element objNoeudParametreCollision = objDocumentXML.createElement("parametre");
			// Cr�er un noeud contenant le nom d'utilisateur du noeud param�tre
			Text objNoeudTexte = objDocumentXML.createTextNode(strNomUtilisateur);
			Text objNoeudTexteCollision = objDocumentXML.createTextNode(strCollision);
			
			Element objNoeudAnciennePosition = objDocumentXML.createElement("position");
			Element objNoeudPosition = objDocumentXML.createElement("position");
			
			// D�finir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "JoueurDeplacePersonnage");
			
			// On ajoute un attribut type qui va contenir le type
			// du param�tre
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
			
			// Ajouter le noeud param�tre au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
			objNoeudCommande.appendChild(objNoeudParametreAnciennePosition);
			objNoeudCommande.appendChild(objNoeudParametreNouvellePosition);
			objNoeudCommande.appendChild(objNoeudParametreCollision);
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

			// Transformer le document XML en code XML
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println("Une erreur est survenue lors de la transformation du document XML en chaine de caracteres");
		}
		catch (TransformerException te)
		{
			System.out.println("Une erreur est survenue lors de la conversion du document XML en chaine de caracteres");
		}
		
		return strCodeXML;
	}
}
